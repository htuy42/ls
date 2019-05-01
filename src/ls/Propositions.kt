package com.sbf.ls

import java.util.*

val R = Random()

fun reverseRunParameterized(a : Assignment, truckIndex : Int) : Assignment?{
    if (a.truckCustomers[truckIndex].size <= 1) {
        return null
    }
    var truck = a.truckCustomers[truckIndex]
    var start = R.nextInt(truck.size)
    var end = R.nextInt(truck.size)
    if (end < start) {
        val tmp = end
        end = start
        start = tmp
    } else if (end == start) {
        return null
    }
    val copy = a.copyOf()
    truck = copy.truckCustomers[truckIndex]
    // integer division causes this value to be floored, which is what we want
    val numSwaps = (end - start) / 2
    var tmp = 0
    for (i in 0 until numSwaps) {
        tmp = truck[start + i]
        truck[start + i] = truck[end - i]
        truck[end - i] = tmp
    }
    return copy
}

fun reverseRun(a: Assignment): Assignment? {
    return reverseRunParameterized(a,R.nextInt(a.truckCustomers.size))
}

fun spliceTargetTrucks(a : Assignment, targets : List<Int>) : Assignment?{
    val from = R.nextInt(a.truckCustomers.size)
    if (a.truckCustomers[from].isEmpty()) {
        return null
    }
    var truck = a.truckCustomers[from]
    var start = R.nextInt(truck.size)
    var end = R.nextInt(truck.size)
    if (end < start) {
        val tmp = end
        end = start
        start = tmp
    } else if (end == start) {
        return null
    }
    var to = targets[R.nextInt(targets.size)]
    // to not required to be different
    val copy = a.copyOf()
    val toTruck = copy.truckCustomers[to]
    truck = copy.truckCustomers[from]
    val splicedOut = truck.subList(start, end)
    var startTo = if (toTruck.size == 0) {
        0
    } else {
        R.nextInt(toTruck.size)
    }
    toTruck.addAll(startTo, splicedOut)
    copy.truckCustomers[from] = ArrayList(truck.filterIndexed { i, v ->
        i < start || i >= end
    })
    return copy
}

fun splice(a: Assignment): Assignment? {
    val from = R.nextInt(a.truckCustomers.size)
    if (a.truckCustomers[from].isEmpty()) {
        return null
    }
    var truck = a.truckCustomers[from]
    var start = R.nextInt(truck.size)
    var end = R.nextInt(truck.size)
    if (end < start) {
        val tmp = end
        end = start
        start = tmp
    } else if (end == start) {
        return null
    }
    var to = R.nextInt(a.truckCustomers.size)
    // to not required to be different
    val copy = a.copyOf()
    val toTruck = copy.truckCustomers[to]
    truck = copy.truckCustomers[from]
    val splicedOut = truck.subList(start, end)
    var startTo = if (toTruck.size == 0) {
        0
    } else {
        R.nextInt(toTruck.size)
    }
    toTruck.addAll(startTo, splicedOut)
    copy.truckCustomers[from] = ArrayList(truck.filterIndexed { i, v ->
        i < start || i >= end
    })
    return copy
}

fun shuffle(a: Assignment): Assignment? {
    val truckIndex = R.nextInt(a.truckCustomers.size)
    if (a.truckCustomers[truckIndex].size <= 1) {
        return null
    }
    val copy = a.copyOf()
    copy.truckCustomers[truckIndex].shuffle()
    return copy
}

fun killATruck(a : Assignment) : Assignment?{
    val toKill = R.nextInt(a.truckCustomers.size)
    if(a.truckCustomers[toKill].isEmpty()){
        return null
    }
    val copy = a.copyOf()
    val saved = copy.truckCustomers[toKill]
    copy.truckCustomers[toKill] = arrayListOf()
    for(elt in saved){
        val truckToGive = R.nextInt(copy.truckCustomers.size)
        if(copy.truckCustomers[truckToGive].isEmpty()){
            copy.truckCustomers[truckToGive].add(elt)
        } else {
            val index = R.nextInt(copy.truckCustomers[truckToGive].size)
            copy.truckCustomers[truckToGive].add(index, elt)
        }
    }
    return copy
}

