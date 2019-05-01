package com.sbf.ls

import com.htuy.common.Configurator
import com.htuy.swarm.extending.extensionChildMain

fun main(args : Array<String>){
    extensionChildMain(args,mapOf(WORKER_SERVICE_TYPE to {k, a -> Worker(k)}, COORDINATOR_SERVICE_TYPE to {k,a -> Coordinator()}))
}