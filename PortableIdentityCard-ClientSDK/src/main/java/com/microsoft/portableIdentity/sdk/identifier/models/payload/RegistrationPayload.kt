package com.microsoft.portableIdentity.sdk.identifier.models.payload

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegistrationPayload (
    @SerialName("type")
    val type:String,
    @SerialName("suffixData")
    val suffixData: String,
    @SerialName("patchData")
    val patchData: String
)