package com.microsoft.did.sdk.crypto.protocols.jose.jws

import com.nimbusds.jose.*
import com.nimbusds.jose.crypto.factories.DefaultJWSSignerFactory
import com.nimbusds.jose.crypto.factories.DefaultJWSVerifierFactory
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.util.Base64URL
import java.security.KeyStore
import java.security.PublicKey

class JwsToken private constructor(
    private val jwsObject: JWSObject
) {
    private val builder = JWSHeader.Builder(jwsObject.header)

    companion object {
        fun deserialize(jws: String): JwsToken {
            return JwsToken(JWSObject.parse(jws))
        }
    }

    constructor(content: ByteArray) : this(JWSObject(JWSHeader(JWSAlgorithm.ES256K), Payload(content)))

    constructor(content: String) : this(JWSObject(JWSHeader(JWSAlgorithm.ES256K), Payload(Base64URL.encode(content))))

    fun getKeyId(): String {
        return jwsObject.header.keyID
    }

    fun setKeyId(string: String) {
        builder.keyID(string)
    }

    fun setType(type: JOSEObjectType) {
        builder.type(type)
    }

    fun setHeader(headerKey: String, headerValue: String) {
        builder.customParam(headerKey, headerValue)
    }

    fun serialize(): String {
        return jwsObject.serialize()
    }

    fun sign(keyId: String, keyStore: KeyStore) {
        val out = JWSObject(builder.build(), jwsObject.payload)
        val key = JWK.load(keyStore, keyId, null)
        val signer = DefaultJWSSignerFactory().createJWSSigner(key, jwsObject.header.algorithm)
        out.sign(signer)
    }

    fun verify(publicKeys: List<PublicKey> = emptyList()): Boolean {
        for (key in publicKeys) {
            val verifier = DefaultJWSVerifierFactory().createJWSVerifier(jwsObject.header, key)
            if (jwsObject.verify(verifier)) {
                return true
            }
        }
        return false
    }

    /**
     * Plaintext payload content
     */
    fun content(): String {
        return jwsObject.payload.toString()
    }
}