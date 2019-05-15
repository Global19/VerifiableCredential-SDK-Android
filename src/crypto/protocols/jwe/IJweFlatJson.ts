/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
import IJwsSignature from './IJweRecipient';

/**
 * JWS flattened json format
 */
export default interface IJwsFlatJson extends IJwsSignature {

  /**
   * The application-specific payload.
   */
  payload: string,
}
