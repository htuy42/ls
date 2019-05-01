package com.sbf.ls

import com.htuy.common.Address
import com.htuy.common.Configurator
import com.htuy.netlib.sockets.InternetSockets
import com.htuy.swarm.extending.api.SwarmApi
import kotlinx.coroutines.experimental.runBlocking
import java.lang.Math.ceil
import java.nio.file.Paths

val CHILD_NAME = "lschildserver"

fun main(args : Array<String>) = runBlocking{
    Configurator().run()
    val input = args[0]
	val path = Paths.get(input)
	val filename = path.getFileName().toString()
	System.out.println("Instance: " + input)

	val watch = Timer()
	watch.start()

    val api = SwarmApi()
    api.host(CHILD_NAME)

    var best : ScoredAssignment = ScoredAssignment(Assignment(mutableListOf()),Double.MAX_VALUE)

    val hostAddr = Address.anyPortLocal()
    InternetSockets().listenOn(hostAddr){
        it.registerTypeListener(ScoredAssignment::class.java){
            best = it
//            println("Got score ${best.score}")
            null
        }
    }
    // *tries* to silence the api. Difficult to do completely because it spawns a bunch of subprocesses,
    // some of which might behave in ways that are difficult to predict due to browns lovely ssh systems
    api.silence()
    api.king.startService(WORKER_SERVICE_TYPE,10,"")
    api.king.tryPushToServiceRetry(COORDINATOR_SERVICE_TYPE,Job(VRPInstance(input),hostAddr))

    var prevBest = best.score
    println("Starting watch loop")
    // if we don't improve for 50 seconds, or if 250 seconds have elapsed, end!
    var cnt = 0
    while(watch.time < 280){
        Thread.sleep(10000)
        println("${watch.time} seconds here")
        if(best.score == prevBest){
            cnt += 1
            if(cnt >= 5){
                break
            }
        } else {
            cnt = 0
        }
        prevBest = best.score
    }


	watch.stop()

    api.kill()

	System.out.println("Instance: " + filename +
					   " Time: " 	+ String.format("%.2f",watch.time) +
					   " Result: " 	+ String.format("%.2f",best.score) + " Solution: ${best.assignment}")

    // we have to kill by force. The api is often a bit uncooperative about actually going away otherwise
    System.exit(0)
}

