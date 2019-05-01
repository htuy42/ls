package com.sbf.ls

import com.htuy.common.Address
import com.htuy.netlib.sockets.Socket
import com.htuy.swarm.management.King
import com.htuy.swarm.management.Service
import kotlinx.coroutines.experimental.runBlocking
import java.io.Serializable
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

val TIME_TO_WORK_FOR = 5000

class Worker(val king: King) : Service {
    override var done: Boolean = false
    override lateinit var socketAddress: Address
    val random = Random()
    lateinit var currentReuquest: WorkResponse

    override fun startService() {
        thread {
            runBlocking {
                var res: Serializable = king.tryGetSynchToServiceRetry(COORDINATOR_SERVICE_TYPE, WorkRequest())!!
                while (res is NotReady) {
                    Thread.sleep(1000)
                    res = king.tryGetSynchToServiceRetry(COORDINATOR_SERVICE_TYPE, WorkRequest())!!
                }
                currentReuquest = res as WorkResponse
                work()
                null
            }
        }
    }

    fun work() = runBlocking {
        lateinit var es: ExecutorService
        while (true) {
            val actualWorkFor = TIME_TO_WORK_FOR * Random().nextDouble() * 2 + 1500 * currentReuquest.stuckness
            es = Executors.newFixedThreadPool(3)
            /// do work for x seconds
            val boxes = arrayListOf(LSBox(), LSBox(), LSBox())
            for (box in boxes) {
                if (random.nextBoolean() && currentReuquest.stuckness > 1) {
                    var base = currentReuquest.elites[0].assignment
                    for (x in 0..random.nextInt(10 + currentReuquest.stuckness)) {
                        var inner =
                            currentReuquest.propositions[random.nextInt(currentReuquest.propositions.size)].invoke(base)
                        while (inner == null) {
                            inner =
                                currentReuquest.propositions[random.nextInt(currentReuquest.propositions.size)].invoke(
                                    base
                                )
                        }
                        base = inner

                    }
                    box.store(ScoredAssignment(base, currentReuquest.objective.invoke(base)))
                } else {
                    for (elt in currentReuquest.elites) {
                        box.store(elt)
                    }
                }
            }
            val start = System.currentTimeMillis()
            val gotoExtremes = R.nextBoolean() && currentReuquest.stuckness > 2
            for (i in 0..2) {
                val ind = i
                es.submit {
                    val isPerTruckOnly = random.nextBoolean() && currentReuquest.stuckness > 1
                    val box = boxes[ind]
                    try {
                        while (System.currentTimeMillis() - start < actualWorkFor) {
                            val next = box.getNext(currentReuquest.stuckness)
                            val proposed = if (gotoExtremes) {
                                currentReuquest.extremeProps[random.nextInt(currentReuquest.propositions.size)].invoke(
                                    next
                                )
                            } else if (!isPerTruckOnly) {
                                currentReuquest.propositions[random.nextInt(currentReuquest.propositions.size)].invoke(
                                    next
                                )
                            } else {
                                currentReuquest.singleTruckProps[random.nextInt(currentReuquest.singleTruckProps.size)].invoke(
                                    next
                                )
                            }
                            if (proposed != null) {
                                var scored = ScoredAssignment(proposed, currentReuquest.objective(proposed))
                                for (i in 0 until 10) {
                                    if (scored.score < BIG_NUMBER || currentReuquest.stuckness == 1) {
                                        break
                                    }
                                    var newProp =
                                        currentReuquest.safeProps[random.nextInt(currentReuquest.safeProps.size)].invoke(
                                            scored.assignment
                                        )
                                    while (newProp == null) {
                                        newProp =
                                            currentReuquest.safeProps[random.nextInt(currentReuquest.safeProps.size)].invoke(
                                                scored.assignment
                                            )
                                    }
                                    scored = ScoredAssignment(newProp, currentReuquest.objective(proposed))
                                }
                                box.store(scored)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            es.shutdown()
            es.awaitTermination(10, TimeUnit.DAYS)
            val bBox = LSBox()
            for (box in boxes) {
                for (elt in box.bests) {
                    bBox.store(elt)
                }
            }
            king.tryPushToServiceRetry(COORDINATOR_SERVICE_TYPE, Result(bBox.bests.toList()))
            currentReuquest = king.tryGetSynchToServiceRetry(COORDINATOR_SERVICE_TYPE, WorkRequest()) as WorkResponse
        }
    }

    override fun register(socket: Socket) {
        // nothing to register
    }
}