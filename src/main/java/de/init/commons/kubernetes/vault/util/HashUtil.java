package de.init.commons.kubernetes.vault.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public class HashUtil {
    private static final Logger LOG = LoggerFactory.getLogger(HashUtil.class);
    private static final String ALGORITHM = "SHA-256";
    public static final String ERROR_MESSAGE = "<ALGORITHM NOT FOUND>";
    private HashUtil(){ }

    public static String getHash(Map<String,String> map) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(ALGORITHM);

            for (Map.Entry<String,String> entry : map.entrySet()) {
                messageDigest.update(entry.getKey().getBytes(StandardCharsets.UTF_8));
                messageDigest.update(entry.getValue().getBytes(StandardCharsets.UTF_8));
            }

            return new String(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            LOG.error("The {} algorithm wasn't found.", ALGORITHM, e);
            return ERROR_MESSAGE;
        }

    }
}
