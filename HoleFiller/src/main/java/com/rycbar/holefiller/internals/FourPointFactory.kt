package com.rycbar.holefiller.internals

import com.rycbar.holefiller.api.ConnectivityMode

internal object VicinityProcessorFactory {
    fun create(connectivityMode: ConnectivityMode): VicinityProcessor = when (connectivityMode) {
        ConnectivityMode.Connected4 -> FourConVicinityProcessor()
        ConnectivityMode.Connected8 -> EightConVicinityProcessor()
    }
}