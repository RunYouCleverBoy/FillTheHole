package com.rycbar.holefiller.internals

import com.rycbar.holefiller.api.Executor
import java.util.concurrent.Executors

internal class FixedThreadPoolExecutor(size: Int) : Executor() {
    private val executorService = Executors.newFixedThreadPool(size)
    override fun shutdown() {
        executorService.shutdown()
    }

    override fun submit(f: () -> Unit) {
        executorService.submit { f.invoke() }
    }
}