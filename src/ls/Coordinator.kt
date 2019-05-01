package com.sbf.ls

import com.htuy.common.Address
import com.htuy.netlib.sockets.Socket
import com.htuy.swarm.management.Service
import java.util.*
import kotlin.concurrent.thread

val maxElites = 10

val COORDINATOR_SERVICE_TYPE = "coordinator"
val WORKER_SERVICE_TYPE = "worker"

class Coordinator : Service {

    var stuckness = 1

    override var done: Boolean = false
    override lateinit var socketAddress: Address
    lateinit var objective : Objective
    var elites : PriorityQueue<ScoredAssignment> = PriorityQueue(object : Comparator<ScoredAssignment>{
        override fun compare(o1: ScoredAssignment, o2: ScoredAssignment): Int {
            return o2.score.compareTo(o1.score)
        }
    })
    lateinit var propositions : List<Proposition>
    lateinit var singleTruckProps : List<Proposition>
    lateinit var safeProps : List<Proposition>
    lateinit var extremeProps : List<Proposition>

    fun start(input : VRPInstance,home : Address) {
        objective = input.getObjective()
        propositions = input.getPropositions()
        extremeProps = input.getExtremeProps()
        singleTruckProps = input.getSingleTruckPropositions()
        safeProps = input.getSafePropositions()
        val initial = input.getInitial()
        elites.add(ScoredAssignment(initial,objective(initial)))
        thread{
            var bestScore = Double.MAX_VALUE
            while(true){
                Thread.sleep(10000)
                synchronized(this){
                    val all = ArrayList<ScoredAssignment>()
                    while(elites.isNotEmpty()){
                        all.add(elites.poll())
                    }
                    val newBest = all[all.size - 1].score
                    home.tryPush(all[all.size - 1]){}
                    if (newBest >= bestScore){
                        stuckness++
                    } else{
                        stuckness = 1
                    }
                    bestScore = newBest
                    println("Best: ${all[all.size - 1].score}")
                    elites.addAll(all)
                }
            }
        }
    }

    override fun register(socket: Socket) {
        socket.registerTypeListener(Job::class.java){
            start(it.input,it.home)
            null
        }
        socket.registerTypeListener(WorkRequest::class.java){
            synchronized(this) {
                if(elites.size == 0){
                    NotReady()
                } else {
                    WorkResponse(elites.toList(), propositions,singleTruckProps,safeProps,extremeProps, objective,stuckness)
                }
            }
        }
        socket.registerTypeListener(Result::class.java){
            synchronized(this) {
                elites.addAll(it.elites)
                while (elites.size > maxElites) {
                    elites.poll()
                }
            }
            null
        }
    }
}