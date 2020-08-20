// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.credential.service.models.presentationexchange

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CredentialIssuanceMetadata(
    @SerialName("manifest")
    var issuerContract: String = ""
) {
    @SerialName("did")
    val issuerDid: String = ""
}