package com.sbf.ls


import java.awt.Point
import java.io.File
import java.io.FileNotFoundException
import java.io.Serializable
import java.lang.Math.sqrt

import java.util.Scanner

// its not infinity, but its close!
val BIG_NUMBER = 100000000

class VRPInstance(fileName: String) : Serializable {
    // VRP Input Parameters
    internal var numCustomers: Int = 0                // the number of customers
    internal var numVehicles: Int = 0            // the number of vehicles
    internal var vehicleCapacity: Int = 0            // the capacity of the vehicles
    internal var demandOfCustomer: IntArray        // the demand of each customer
    internal var xCoordOfCustomer: DoubleArray    // the x coordinate of each customer
    internal var yCoordOfCustomer: DoubleArray    // the y coordinate of each customer

    init {
        var read: Scanner? = null
        try {
            read = Scanner(File(fileName))
        } catch (e: Exception) {
            println("Error: in VRPInstance() " + fileName + "\n" + e.message)
            System.exit(-1)
        }

        numCustomers = read!!.nextInt()
        numVehicles = read.nextInt()
        vehicleCapacity = read.nextInt()

        println("Number of customers: $numCustomers")
        println("Number of vehicles: $numVehicles")
        println("Vehicle capacity: $vehicleCapacity")

        demandOfCustomer = IntArray(numCustomers)
        xCoordOfCustomer = DoubleArray(numCustomers)
        yCoordOfCustomer = DoubleArray(numCustomers)

        for (i in 0 until numCustomers) {
            demandOfCustomer[i] = read.nextInt()
            xCoordOfCustomer[i] = read.nextDouble()
            yCoordOfCustomer[i] = read.nextDouble()
        }

        for (i in 0 until numCustomers)
            println(demandOfCustomer[i].toString() + " " + xCoordOfCustomer[i] + " " + yCoordOfCustomer[i])
    }

    fun isValid(assignment: Assignment) : Boolean{
        // truck load
        for(elt in assignment.truckCustomers){
            var cost = 0.0
            for(sub in elt){
                cost += demandOfCustomer[sub]
            }
            if(cost > vehicleCapacity){
                return false
            }
        }
        // customers served once
        val served = HashSet<Int>()
        for(elt in assignment.truckCustomers){
            for(sub in elt){
                if(sub in served){
                    return false
                }
                served.add(sub)
            }
        }
        return served.size == numCustomers - 1
    }

    fun getObjective() : Objective{
        return {
            if(!isValid(it)){
                // if its not valid, it gets a very big number as its score, and additionally we penalize it by how much
                // overloaded truck there is
                var badness = BIG_NUMBER
                for(elt in it.truckCustomers){
                    var cost = 0
                    for(sub in elt){
                        cost += demandOfCustomer[sub]
                    }
                    if(cost > vehicleCapacity){
                        badness += cost - vehicleCapacity
                    }
                }
                badness.toDouble()
            } else {
                var score = 0.0
                for (elt in it.truckCustomers) {
                    var xPos = xCoordOfCustomer[0]
                    var yPos = yCoordOfCustomer[0]
                    for (destination in elt) {
                        val nextX = xCoordOfCustomer[destination]
                        val nextY = yCoordOfCustomer[destination]
                        val xD = nextX - xPos
                        val yD = nextY - yPos
                        score += sqrt((xD * xD) + (yD * yD))
                        xPos = nextX
                        yPos = nextY
                    }
                    // we need to go home
                    val nextX = xCoordOfCustomer[0]
                    val nextY = yCoordOfCustomer[0]
                    val xD = nextX - xPos
                    val yD = nextY - yPos
                    score += sqrt((xD * xD) + (yD * yD))
                }
                score
            }
        }
    }

    fun addATruck(a : Assignment) : Assignment?{
        val truckToAdd = (0 until a.truckCustomers.size).firstOrNull { a.truckCustomers[it].isEmpty() } ?: return null
        val numCustomers = R.nextInt((numCustomers * 2) / numVehicles)
        val newTruckCustomers = arrayListOf<Int>()
        val copy = a.copyOf()
        for(i in 0 until numCustomers) {
            val randTruckIndex = R.nextInt(copy.truckCustomers.size)
            if(copy.truckCustomers[randTruckIndex].isEmpty()) {
                continue
            }
            val randCustomerIndex = R.nextInt(copy.truckCustomers[randTruckIndex].size)
            newTruckCustomers.add(copy.truckCustomers[randTruckIndex][randCustomerIndex])
            copy.truckCustomers[randTruckIndex].removeAt(randCustomerIndex)
        }
        copy.truckCustomers[truckToAdd] = newTruckCustomers
        return copy
    }

    fun twoOptMove(a : Assignment) : Assignment?{
        val objective = getObjective()
        val prevScore = objective(a)
        val truckIndexs = (0 until a.truckCustomers.size).shuffled()
        val cpy = a.copyOf()
        for(takeFromTruck in truckIndexs){
            for(takeInd in 0 until a.truckCustomers[takeFromTruck].size){
                for(putToTruck in truckIndexs){
                    for(putInd in 0 until a.truckCustomers[putToTruck].size){
                        val tmp = cpy.truckCustomers[takeFromTruck][takeInd]
                        cpy.truckCustomers[takeFromTruck].removeAt(takeInd)
                        cpy.truckCustomers[putToTruck].add(putInd,tmp)
                        if(objective(cpy) < prevScore){
                            return cpy
                        }
                        cpy.truckCustomers[putToTruck].removeAt(putInd)
                        cpy.truckCustomers[takeFromTruck].add(takeInd,tmp)
                    }
                }
            }
        }
        return null
    }

    fun scoreSingleTruck(stops : List<Int>) : Double{
        var score = 0.0
        var xPos = xCoordOfCustomer[0]
        var yPos = yCoordOfCustomer[0]
        for (destination in stops) {
            val nextX = xCoordOfCustomer[destination]
            val nextY = yCoordOfCustomer[destination]
            val xD = nextX - xPos
            val yD = nextY - yPos
            score += sqrt((xD * xD) + (yD * yD))
            xPos = nextX
            yPos = nextY
        }
        // we need to go home
        val nextX = xCoordOfCustomer[0]
        val nextY = yCoordOfCustomer[0]
        val xD = nextX - xPos
        val yD = nextY - yPos
        score += sqrt((xD * xD) + (yD * yD))
        return score
    }

    fun escoreTruck(truck : List<Int>) : Double{
        var score = 0.0

        for(elt in truck){
            val xPos = xCoordOfCustomer[elt]
            val yPos = yCoordOfCustomer[elt]
            for(other in truck){
                if(other == elt){
                    continue
                }
                val nextX = xCoordOfCustomer[other]
                val nextY = yCoordOfCustomer[other]
                val xD = nextX - xPos
                val yD = nextY - yPos
                score += sqrt((xD * xD) + (yD * yD))
            }
        }
        score /= truck.size * truck.size
        return score
    }

    // attempts to destroy a single truck by distributing its work, and in so doing improve the total score.
    // it chooses a worst truck semi randomly (worst of 15 it chooses randomly). It splits its work as best it can into
    // two trucks, and then messes around with those trucks for a while to see if it can find an overall improvement
    fun fixWorsts(a : Assignment) : Assignment?{
        val obj = getObjective()
        val origScore = obj(a)
        var worstScore = 0.0
        var worstInd = -1
        for(x in 0..15){
            val ind = R.nextInt(a.truckCustomers.size)
            val score = scoreSingleTruck(a.truckCustomers[ind])
            if(score > worstScore){
                worstScore = score
                worstInd = ind
            }
        }
        if(worstInd == -1){
            return null
        }
        val unused = a.truckCustomers.indices.firstOrNull{a.truckCustomers[it].isEmpty()} ?: return null
        val copy = a.copyOf()
        val toPut = copy.truckCustomers[worstInd]
        if(toPut.size < 3){
            return null
        }
        var bstF1 : List<Int> = listOf()
        var bstF2 : List<Int> = listOf()
        var bstScore = Double.MAX_VALUE

        // find a good way to split them
        for(i in 0..200){
            toPut.shuffle()
            val fst = toPut.subList(0,toPut.size / 2)
            val other = toPut.subList(toPut.size / 2,toPut.size)
            val score = escoreTruck(fst) + escoreTruck(other)
            if(score < bstScore){
                bstScore = score
                bstF1 = fst
                bstF2 = other
            }
        }
        copy.truckCustomers[worstInd] = ArrayList(bstF1)
        copy.truckCustomers[unused] = ArrayList(bstF2)
        var bstCopy = copy
        val allCopies = mutableListOf(copy)
        var bstCopyScore = obj(copy)
        for(attempt in 0..50){
            val copyToUse = R.nextInt(3)
            var copyCopy = if(copyToUse == 0){
                copy.copyOf()
            } else if(copyToUse == 1){
                bstCopy.copyOf()
            } else {
                allCopies[R.nextInt(allCopies.size)].copyOf()
            }
            for(subAttempt in 0..50){
                val method = R.nextInt(2)
                copyCopy = if(method == 0){
                    spliceTargetTrucks(copyCopy,listOf(worstInd,unused))?:copyCopy
                } else {
                    if(R.nextBoolean()){
                        reverseRunParameterized(copyCopy,worstInd) ?: copyCopy
                    } else {
                        reverseRunParameterized(copyCopy,unused) ?: copyCopy
                    }
                }
                val score = obj(copyCopy)
                if(score < origScore){
                    return copyCopy
                } else if(score < bstCopyScore) {
                    bstCopy = copyCopy
                    bstCopyScore = score
                }
            }
        }
        return null
    }

    fun modifyASingleTruck(a : Assignment) : Assignment?{


        var truck = R.nextInt(a.truckCustomers.size)
        while(a.truckCustomers[truck].size < 4){
            truck = R.nextInt(a.truckCustomers.size)
        }
        val prevScore = scoreSingleTruck(a.truckCustomers[truck])
        fun recursiveDo(left : Set<Int>, accumulated : MutableList<Int>) : MutableList<Int>?{
            if(left.size == 0){
                return if(scoreSingleTruck(accumulated) < prevScore){
                    accumulated
                } else {
                    null
                }
            }
            else {
                for(elt in left){
                    val subLeft = left.minus(elt)
                    accumulated.add(elt)
                    val scored = recursiveDo(subLeft,accumulated)
                    if(scored != null){
                        return scored
                    } else {
                        accumulated.removeAt(accumulated.size - 1)
                    }
                }
            }
            return null
        }
        if(a.truckCustomers.size <= 9) {
            val newOrder = recursiveDo(HashSet(a.truckCustomers[truck]), arrayListOf())
            if(newOrder != null){
                val copy = a.copyOf()
                copy.truckCustomers[truck] = newOrder
                return copy

            }
        }
        return null
    }

    fun getExtremeProps() : List<Proposition>{
        return listOf(this::twoOptMove,this::modifyASingleTruck,this::fixWorsts)
    }

    fun getPropositions() : List<Proposition>{
        return listOf(::splice,::reverseRun,::shuffle)
    }

    fun getSingleTruckPropositions() : List<Proposition>{
        return listOf(::reverseRun,::shuffle,::killATruck,this::addATruck)
    }

    fun getSafePropositions() : List<Proposition>{
        return listOf(::reverseRun,::splice)
    }

    fun getInitial() : Assignment{
        val lst : MutableList<MutableList<Int>> = arrayListOf()
        var currentIndex = 1
        var currentSubLst = ArrayList<Int>()
        var currentSubLoad = 0
        while(currentIndex < numCustomers){
            val cost = demandOfCustomer[currentIndex]
            assert(cost <= vehicleCapacity)
            if(cost + currentSubLoad > vehicleCapacity){
                lst.add(currentSubLst)
                currentSubLoad = 0
                currentSubLst = arrayListOf()
            } else {
                currentSubLoad += cost
                currentSubLst.add(currentIndex)
                currentIndex++
            }
        }
        lst.add(currentSubLst)
        while(lst.size < numVehicles){
            lst.add(arrayListOf())
        }
        val res = Assignment(lst)
        return res
    }


}

