package com.chardon.faceval.android.rest.model

data class ServerInfo(
    val name: String,
    val instance: InstanceInfo,
)

data class InstanceInfo(
    val instanceId: String,
//    val hostName: // TODO: 2021-5-31
)
