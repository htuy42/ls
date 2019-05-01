package com.sbf.ls

import java.io.Serializable

class Assignment(
    val truckCustomers : MutableList<MutableList<Int>> = arrayListOf()
) : Serializable {
    fun copyOf() : Assignment {
        val newTC : MutableList<MutableList<Int>> = arrayListOf()
        for(elt in truckCustomers) {
            newTC.add(ArrayList(elt))
        }
        return Assignment(newTC)
    }

    override fun toString() : String{
        val builder = StringBuilder()
        builder.append("0 ")
        for(elt in truckCustomers){
            builder.append("0 ")
            for(customer in elt){
                builder.append("$customer ")
            }
            builder.append("0 ")
        }
        return builder.trim().toString()
    }

}

class ScoredAssignment(val assignment : Assignment, val score : Double) : Serializable