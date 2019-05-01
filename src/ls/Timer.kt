package com.sbf.ls


class Timer {
    private var startTime: Long = 0
    private var stopTime: Long = 0
    private var running: Boolean = false

    private val nano = 1000000000.0

    val time: Double
        get() {
            val elapsed: Double
            if (running) {
                elapsed = (System.nanoTime() - startTime) / nano
            } else {
                elapsed = (stopTime - startTime) / nano
            }
            return elapsed
        }

    fun reset() {
        this.startTime = 0
        this.running = false
    }

    fun start() {
        this.startTime = System.nanoTime()
        this.running = true
    }

    fun stop() {
        if (running) {
            this.stopTime = System.nanoTime()
            this.running = false
        }
    }
}
