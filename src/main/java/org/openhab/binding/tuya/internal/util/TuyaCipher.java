/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tuya.internal.util;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cipher class for encrypting and decrypting messages.
 *
 * @author Wim Vissers.
 *
 */
public class TuyaCipher {

    private static final String UDP_KEY = "yGAdlopoPVldABfn";
    private static byte[] udpKey;
    private static Logger logger;

    private byte[] key;

    public TuyaCipher(byte[] key) {
        this.key = key;
        if (udpKey == null) {
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                md.update(UDP_KEY.getBytes());
                udpKey = md.digest();
            } catch (NoSuchAlgorithmException e) {
                // Should not happen.
            }
        }
        if (logger == null) {
            logger = LoggerFactory.getLogger(getClass());
        }
    }

    public TuyaCipher(String key) throws UnsupportedEncodingException {
        this(key.getBytes("UTF-8"));
    }

    /**
     * Encrypt an input buffer with the key specified in the constructor.
     *
     * @param buffer the input buffer.
     * @return the encrypted output.
     * @throws UnsupportedEncodingException
     */
    public byte[] encrypt(byte[] buffer) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return cipher.doFinal(buffer);
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
                | BadPaddingException e1) {
            // Should not happen
            return null;
        }
    }

    /**
     * Decrypt an input buffer with the key specified in the constructor.
     *
     * @param buffer the input buffer.
     * @return the encrypted output.
     * @throws IllegalBlockSizeException
     * @throws UnsupportedEncodingException
     */
    public byte[] decrypt(byte[] buffer) throws IllegalBlockSizeException {
        byte[] input = null;
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            /**
             * boolean base64 = false;
             * if (false) {
             * if (this.version.equals("3.3")) {
             * // Remove 3.3 header
             * // input = Arrays.copyOfRange(buffer, 15, buffer.length);
             * input = Arrays.copyOfRange(buffer, 19, buffer.length - 1);
             * } else {
             * // Data has version number and is encoded in base64
             * // Remove prefix of version number and MD5 hash
             * input = Arrays.copyOfRange(buffer, 19, buffer.length);
             * base64 = true;
             * }
             * } else {
             * input = buffer;
             * }
             */
            input = buffer;
            return cipher.doFinal(input);
        } catch (BadPaddingException e0) {
            SecretKeySpec secretKey = new SecretKeySpec(udpKey, "AES");
            Cipher cipher;
            try {
                cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
                cipher.init(Cipher.DECRYPT_MODE, secretKey);
                return cipher.doFinal(input);
            } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException
                    | BadPaddingException e) {
                logger.error("Error decrypting packet.", e);
                return null;
            }
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e1) {
            logger.error("Unexpected error when decrypting.", e1);
            return null;
        }
    }

}
