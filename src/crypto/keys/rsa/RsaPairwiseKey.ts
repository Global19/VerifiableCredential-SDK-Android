/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
 import CryptoFactory from "../plugin/CryptoFactory";
import { CryptoAlgorithm } from "../keyStore/IKeyStore";
import KeyTypeFactory, { KeyType } from "./KeyTypeFactory";
import W3cCryptoApiConstants from "../utilities/W3cCryptoApiConstants";
import PrivateKey from "../PrivateKey";
import base64url from "base64url";
import KeyUseFactory from "../KeyUseFactory";
import RsaPrivateKey from "./RsaPrivateKey";
const bigInt = require('big-integer');
import { BigIntegerStatic } from 'big-integer';

/**
 * Class to model RSA pairwise keys
 */
 export default class RsaPairwiseKey {

  /**
   * Buffer used for prime generation
   */
  private static deterministicKey: Buffer = Buffer.from('');
  
  // Statistics on the number of prime tests
  private static numberOfPrimeTests: number = 0;

  /**
   * Generate a pairwise key for the specified algorithms
   * @param cryptoFactory defining the key store and the used crypto api
   * @param personaMasterKey Master key for the current selected persona
   * @param algorithm for the key
   * @param personaId Id for the persona
   * @param peerId Id for the peer
   * @param extractable True if key is exportable
   * @param keyops Key operations
   */
  public static async generate(cryptoFactory: CryptoFactory, personaMasterKey: Buffer, algorithm: CryptoAlgorithm, personaId: string, peerId: string, extractable: boolean, keyops: string[]): Promise<PrivateKey> {
    // This method is currently breaking the subtle crypto pattern and needs to be fixed to be platform independent
 

    // Set the key size
    const keySize = algorithm.modulusLength || 1024;

    // Get deterministic base number for p
    const peerIdBuffer = Buffer.from(peerId);
    const pBase: Buffer = await RsaPairwiseKey.generateDeterministicNumberForPrime(cryptoFactory, keySize / 2, personaMasterKey, peerIdBuffer);

    // Get deterministic base number for q
    const qBase: Buffer = await this.generateDeterministicNumberForPrime(cryptoFactory, keySize / 2, pBase, peerIdBuffer);
    const p = RsaPairwiseKey.getPrime(pBase);
    const q = RsaPairwiseKey.getPrime(qBase);

    // compute key components
    const modulus = p.multiply(q);
    const pMinus = p.subtract(bigInt.one);
    const qMinus = q.subtract(bigInt.one);
    const phi = pMinus.multiply(qMinus);
    const e = bigInt(65537);
    const d = e.modInv(phi);
    const dp = d.mod(pMinus);
    const dq = d.mod(qMinus);
    const qi = q.modInv(p);
    return <RsaPrivateKey> {
      kty: KeyType.RSA,
      use: KeyUseFactory.create(algorithm),
      e: RsaPairwiseKey.toBase(e),
      n: RsaPairwiseKey.toBase(modulus),
      d: RsaPairwiseKey.toBase(d),
      p: RsaPairwiseKey.toBase(p),
      q: RsaPairwiseKey.toBase(q),
      dp: RsaPairwiseKey.toBase(dp),
      dq: RsaPairwiseKey.toBase(dq),
      qi: RsaPairwiseKey.toBase(qi)
    };
  } 

  
  /**
   * Uses primeBase as reference and generate the closest prime number
   */
  private static getPrime (primeBase: Buffer): any {
    const qArray = Array.from(primeBase);
    const prime: bigInt.BigIntegerStatic = RsaPairwiseKey.generatePrime(qArray);
    return new bigInt(prime);
  }

  /**
   * Generate a deterministic number that can be used as prime
   * @param cryptoFactory The crypto factory.
   * @param keySize Desired key size
   * @param personaMasterKey The persona master key
   * @param peerId The peer id
   */
   public static async generateDeterministicNumberForPrime (_cryptoFactory: CryptoFactory, primeSize: number, personaMasterKey: Buffer, peerId: Buffer): Promise<Buffer> {
    const numberOfRounds: number = primeSize / (8 * 64);
    this.deterministicKey = Buffer.from('');
    const rounds: Array<(cryptoFactory: CryptoFactory, inx: number, key: Buffer, data: Buffer) => Promise<Buffer>> = [];
    for (let inx = 0; inx < numberOfRounds ; inx++) {
      rounds.push((cryptoFactory: CryptoFactory, inx: number, key: Buffer, data: Buffer) => {
        return this.generateHashForPrime(cryptoFactory, inx, key, data);
      });
    }

    return this.executeRounds(crypto, rounds, 0, personaMasterKey, peerId);
  }

  /**
   * Generate a hash used as component for prime number
   * @param crypto The crypto object.
   * @param inx Round number
   * @param key Signature key
   * @param data Data to sign
   */
  private static async generateHashForPrime (cryptoFactory: CryptoFactory, _inx: number, key: Buffer, data: Buffer): Promise<Buffer> {
    // Get the subtle crypto
    const crypto: SubtleCrypto = cryptoFactory.getMessageAuthenticationCodeSigners(W3cCryptoApiConstants.Hmac);

    // Generate the master key
    const alg: CryptoAlgorithm = { name: W3cCryptoApiConstants.Hmac, hash: W3cCryptoApiConstants.Sha512 };
    const signingKey: JsonWebKey = {
      kty: 'oct',
      k: base64url.encode(key)
    };

    const importedKey = await crypto.importKey(W3cCryptoApiConstants.Jwk, signingKey, alg, false, ['sign']);
    const signature = await crypto.sign(alg, importedKey, data);
    RsaPairwiseKey.deterministicKey = Buffer.concat([RsaPairwiseKey.deterministicKey, Buffer.from(signature)]);
    return RsaPairwiseKey.deterministicKey;
  }

  /**
   * Execute all rounds
   * @param rounds Array of functions to execute
   * @param inx Current step
   * @param key Key to sign
   * @param data Data to sign
   */
  private static async executeRounds (cryptoFactory: CryptoFactory, rounds: Array<(cryptoFactory: CryptoFactory, inx: number, key: Buffer, data: Buffer) =>
    Promise<Buffer>>, inx: number, key: Buffer, data: Buffer): Promise<Buffer> {
    const signature: Buffer = await rounds[inx](cryptoFactory, inx, key, data);
    if (inx + 1 === rounds.length) {
      return this.deterministicKey;
    } else {
      await this.executeRounds(crypto, rounds, inx + 1, key, Buffer.from(signature));
      return this.deterministicKey;
    }
  }

  /**
   * Generate a prime number from the seed.
   * isProbablyPrime is based on the Miller-Rabin prime test.
   * @param primeSeed seed for prime generator
   */
  private static generatePrime (primeSeed: Array<number>): BigIntegerStatic {
    // make sure candidate is uneven, set high order bit
    primeSeed[primeSeed.length - 1] |= 0x1;
    primeSeed[0] |= 0x80;
    const two = bigInt(2);
    let prime = bigInt.fromArray(primeSeed, 256, false);
    this._numberOfPrimeTests = 1;
    while (true) {
      // 64 tests give 128 bit security
      if (prime.isProbablePrime(64)) {
        break;
      }
      prime = prime.add(two);
      this._numberOfPrimeTests++;
    }

    return prime;
  }

  /**
   * Convert big number to base64 url.
   * @param bigNumber Number to convert
   */
   private static toBase (bigNumber: any): string {
    let buf = Buffer.from(bigNumber.toArray(256).value);
    return base64url(buf);
  }
}
