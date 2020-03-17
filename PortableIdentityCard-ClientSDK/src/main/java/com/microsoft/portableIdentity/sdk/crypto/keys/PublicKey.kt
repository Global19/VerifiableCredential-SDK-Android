package com.microsoft.portableIdentity.sdk.crypto.keys

import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.models.KeyUse
import com.microsoft.portableIdentity.sdk.crypto.models.Sha
import com.microsoft.portableIdentity.sdk.crypto.models.toKeyUse
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.*
import com.microsoft.portableIdentity.sdk.crypto.plugins.SubtleCryptoScope
import com.microsoft.portableIdentity.sdk.utilities.Base64Url
import com.microsoft.portableIdentity.sdk.utilities.ILogger
import com.microsoft.portableIdentity.sdk.utilities.stringToByteArray

/**
 * Represents a Public Key in JWK format.
 * @class
 * @abstract
 */
abstract class PublicKey (val key: JsonWebKey, internal var logger: ILogger): IKeyStoreItem {
    /**
     * Key type
     */
    open var kty: KeyType = toKeyType(key.kty, logger = logger)

    /**
     * Key ID
     */
    override var kid: String = key.kid ?: ""

    /**
     * Intended use
     */
    open var use: KeyUse? = key.use?.let { toKeyUse(it) }

    /**
     * Valid key operations (key_ops)
     */
    open var key_ops: List<KeyUsage>? = key.key_ops?.map {  toKeyUsage(it, logger = logger) }

    /**
     * Algorithm intended for use with this key
     */
    open var alg: String? = key.alg

    /**
     * Obtains the thumbprint for the jwk parameter
     * @param jwk JSON object representation of a JWK
     * @see https://tools.ietf.org/html/rfc7638
     */
    fun getThumbprint (crypto: CryptoOperations, sha: Algorithm = Sha.Sha512): String {
        // construct a JSON object with only required fields
        val json = this.minimumAlphabeticJwk()
        val jsonUtf8 = stringToByteArray(json)
        val digest = crypto.subtleCryptoFactory.getMessageDigest(sha.name, SubtleCryptoScope.Public)
        val hash = digest.digest(sha, jsonUtf8)
        // undocumented, but assumed base64url of hash is returned
        return Base64Url.encode(hash, logger = logger)
    }

    /**
     * Gets the minimum JWK with parameters in alphabetical order as specified by JWK Thumbprint
     * @see https://tools.ietf.org/html/rfc7638
     */
    abstract fun minimumAlphabeticJwk(): String

    abstract fun toJWK(): JsonWebKey
}