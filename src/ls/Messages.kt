package com.sbf.ls

import com.htuy.common.Address
import java.io.Serializable

class WorkRequest : Serializable

data class WorkResponse(val elites : List<ScoredAssignment>, val propositions : List<Proposition>, val singleTruckProps : List<Proposition>, val safeProps : List<Proposition>, val extremeProps : List<Proposition>, val objective : Objective, val stuckness : Int) : Serializable

data class Result(val elites : List<ScoredAssignment>) : Serializable

class NotReady : Serializable

class Job(val input : VRPInstance, val home : Address) : Serializable