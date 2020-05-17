package com.rycbar.holefiller.api

abstract class Executor {
    abstract fun submit(f: () -> Unit)
    abstract fun shutdown()
}