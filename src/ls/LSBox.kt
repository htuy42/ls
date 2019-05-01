package com.sbf.ls

import java.io.Serializable
import java.lang.Double.max
import java.util.*
import kotlin.collections.ArrayList

typealias Proposition = (Assignment) -> Assignment?
typealias Objective = (Assignment) -> Double


class LSBox : Serializable{
    val bests : ArrayList<ScoredAssignment> = ArrayList()

    var valids = ArrayList<ScoredAssignment>()
    var all = ArrayList<ScoredAssignment>()
    val rand = Random()

    fun getNext(stuckness : Int = 1) : Assignment{
        val r = rand.nextDouble()
        val bestWeight = max(0.0,.6 - .1 * stuckness)
        return if(r < bestWeight){
            bests[rand.nextInt(bests.size)].assignment
        } else if(r < .7)  {
            all[rand.nextInt(all.size)].assignment
        } else {
            if(valids.size > 0){
                valids[rand.nextInt(valids.size)].assignment
            } else {
                bests[rand.nextInt(bests.size)].assignment
            }
        }
    }

    fun store(toStore : ScoredAssignment){
        all.add(toStore)
        if(toStore.score < Double.MAX_VALUE){
            valids.add(toStore)
        }
        if(bests.size > 5){
            val worstInd =(0 until bests.size).maxBy { bests[it].score }!!
            bests[worstInd] = toStore
        } else {
            bests.add(toStore)
        }
        if(all.size > 300){
            all = ArrayList(all.subList(250,300))
        }
        if(valids.size > 300){
            valids = ArrayList(valids.subList(250,300))
        }
    }
}