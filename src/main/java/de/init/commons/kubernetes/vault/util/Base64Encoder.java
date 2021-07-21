package de.init.commons.kubernetes.vault.util;

import org.bouncycastle.util.encoders.Base64;

/**
 * @author Peer-Lucas Jeske
 * created 21.07.2021
 */
public final class Base64Encoder {
    private Base64Encoder(){
    }

    public static String encode(String clearText) {
        byte[] encodedBytes = Base64.encode(clearText.getBytes());
        return new String(encodedBytes);
    }

    public static String decode(String cipherText) {
        byte[] decodedBytes = Base64.decode(cipherText);
        return new String(decodedBytes);
    }
}
