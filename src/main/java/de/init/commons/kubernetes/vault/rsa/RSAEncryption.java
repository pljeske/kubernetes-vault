package de.init.commons.kubernetes.vault.rsa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class RSAEncryption {
  private static final Logger LOG = LoggerFactory.getLogger(RSAEncryption.class);
  public static final String PUBLIC_KEY_PATH = "./keys/public.key";
  public static final String TRANSFORMATION = "RSA/ECB/OAEPWITHSHA-512ANDMGF1PADDING";
  public static final String ALGORITHM = "RSA";
  private static final String PRIVATE_KEY_PATH = "./keys/private.key";

  private File publicKeyFile;
  private final PublicKey publicKey;
  private final PrivateKey privateKey;

  public RSAEncryption() throws NoSuchAlgorithmException, InvalidKeySpecException {
    byte[] privateKeyBytes = getKeyBytes(PRIVATE_KEY_PATH);
    EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);

    byte[] publicKeyBytes = getKeyBytes(PUBLIC_KEY_PATH);
    EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);

    KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
    publicKey = keyFactory.generatePublic(publicKeySpec);
    privateKey = keyFactory.generatePrivate(privateKeySpec);
  }

  private byte[] getKeyBytes(String path) {
    byte[] keyBytes;
    try {
      File keyFile = new File(path);
      keyBytes = Files.readAllBytes(keyFile.toPath());
      if (path.equals(PUBLIC_KEY_PATH)) {
        publicKeyFile = keyFile;
      }
    } catch (Exception e) {
      LOG.error("The following file was not found: {}", path, e);
      throw new RuntimeException();
    }
    return keyBytes;
  }

  public byte[] encrypt (String plainText) throws Exception {
    //Get Cipher Instance RSA With ECB Mode and OAEPWITHSHA-512ANDMGF1PADDING Padding
    Cipher cipher = Cipher.getInstance(TRANSFORMATION);

    //Initialize Cipher for ENCRYPT_MODE
    cipher.init(Cipher.ENCRYPT_MODE, publicKey);

    //Perform Encryption
    return cipher.doFinal(plainText.getBytes());
  }

  public String decrypt (byte[] cipherTextArray) throws NoSuchPaddingException,
      NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
    //Get Cipher Instance RSA With ECB Mode and OAEPWITHSHA-512ANDMGF1PADDING Padding
    Cipher cipher = Cipher.getInstance(TRANSFORMATION);

    //Initialize Cipher for DECRYPT_MODE
    cipher.init(Cipher.DECRYPT_MODE, privateKey);

    //Perform Decryption
    byte[] decryptedTextArray = cipher.doFinal(cipherTextArray);

    return new String(decryptedTextArray);
  }

  public String decryptBase64(String base64EncodedCipherText) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException,
      NoSuchAlgorithmException, NoSuchPaddingException {
    return decrypt(Base64.getDecoder().decode(base64EncodedCipherText));
  }

  public String decrypt(String cipherText) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException,
      NoSuchAlgorithmException, NoSuchPaddingException {
    return decrypt(cipherText.getBytes());
  }



  public File getPublicKeyFile(){
    return publicKeyFile;
  }
}
