package com.rycbar.holefiller

object VicinityProcessorFactory {
    fun create(connectivityMode: ConnectivityMode): VicinityProcessor = when (connectivityMode) {
        ConnectivityMode.Connected4 -> FourConVicinityProcessor()
        ConnectivityMode.Connected8 -> EightConVicinityProcessor()
    }
}