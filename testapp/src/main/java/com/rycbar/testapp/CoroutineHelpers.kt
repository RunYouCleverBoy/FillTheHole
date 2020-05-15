package com.rycbar.testapp

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun launchUI(f: suspend CoroutineScope.() -> Unit) = CoroutineScope(Dispatchers.Main).launch(block = f)
suspend fun <T> withBlocking(f : () -> T): T {
    val deferred = CompletableDeferred<T>()
    CoroutineScope(Dispatchers.Default).launch {
        deferred.complete(f.invoke())
    }
    return deferred.await()
}
