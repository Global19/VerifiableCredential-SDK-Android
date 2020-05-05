/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.registrars

import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.identifier.Identifier
import com.microsoft.portableIdentity.sdk.identifier.SidetreePayloadProcessor
import com.microsoft.portableIdentity.sdk.identifier.models.payload.RegistrationPayload
import com.microsoft.portableIdentity.sdk.utilities.Base64Url
import com.microsoft.portableIdentity.sdk.utilities.Constants
import com.microsoft.portableIdentity.sdk.utilities.Serializer
import com.microsoft.portableIdentity.sdk.utilities.controlflow.RegistrarException
import com.microsoft.portableIdentity.sdk.utilities.controlflow.Result
import javax.inject.Inject
import kotlin.random.Random

/**
 * Registrar implementation for the Sidetree long form identifier
 * @class
 * @implements Registrar
 */
class SidetreeRegistrar @Inject constructor(private val serializer: Serializer
) : Registrar() {

    override suspend fun register(
        signatureKeyReference: String,
        recoveryKeyReference: String,
        cryptoOperations: CryptoOperations
    ): Result<Identifier> {
        return try {
            val alias = Base64Url.encode(Random.nextBytes(8))
            val payloadProcessor = SidetreePayloadProcessor(cryptoOperations, signatureKeyReference, recoveryKeyReference, serializer)
            val registrationPayload = payloadProcessor.generateCreatePayload(alias)
            val registrationPayloadEncoded = registrationPayload.suffixData+"."+registrationPayload.patchData

            val identifierLongForm = computeLongFormIdentifier(payloadProcessor, registrationPayload, registrationPayloadEncoded)

            Result.Success(
                transformIdentifierDocumentToIdentifier(
                    payloadProcessor,
                    registrationPayload,
                    identifierLongForm,
                    alias,
                    signatureKeyReference,
                    recoveryKeyReference
                )
            )
        } catch (exception: Exception) {
            Result.Failure(RegistrarException("Unable to create an identifier", exception))
        }
    }

    private fun computeUniqueSuffix(payloadProcessor: SidetreePayloadProcessor, registrationPayload: RegistrationPayload): String {
        val uniqueSuffix = payloadProcessor.computeUniqueSuffix(registrationPayload.suffixData)
        return "did:${Constants.METHOD_NAME}:$uniqueSuffix"
    }

    private fun computeLongFormIdentifier(
        payloadProcessor: SidetreePayloadProcessor,
        registrationPayload: RegistrationPayload,
        registrationPayloadEncoded: String
    ): String {
        val identifierShortForm = computeUniqueSuffix(payloadProcessor, registrationPayload)
        return "$identifierShortForm?${Constants.INITIAL_STATE_LONGFORM}=$registrationPayloadEncoded"
    }

    private fun transformIdentifierDocumentToIdentifier(
        payloadProcessor: SidetreePayloadProcessor,
        registrationPayload: RegistrationPayload,
        identifierLongForm: String,
        alias: String,
        signatureKeyReference: String,
        recoveryKeyReference: String
    ): Identifier {
        val nextUpdateCommitmentHash = payloadProcessor.extractNextUpdateCommitmentHash(registrationPayload)
        val nextRecoveryCommitmentHash = payloadProcessor.extractNextRecoveryCommitmentHash(registrationPayload)

        val personaSigKeyRef = "${alias}_$signatureKeyReference"
        val personaRecKeyRef = "${alias}_$recoveryKeyReference"

        return Identifier(
            identifierLongForm,
            alias,
            personaSigKeyRef,
            "",
            personaRecKeyRef,
            nextUpdateCommitmentHash,
            nextRecoveryCommitmentHash,
            Constants.IDENTIFIER_SECRET_KEY_NAME
        )
    }
}