package de.init.commons.kubernetes.vault.rsa;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class RSAEncryption {
  public static final String PUBLIC_KEY_PATH = "keys/public.key";
  private static final String PRIVATE_KEY_PATH = "keys/private.key";
  private static final String TRANSFORMATION = "RSA/ECB/OAEPWITHSHA-512ANDMGF1PADDING";
  private static final String RSA = "RSA";

  private final PublicKey publicKey;
  private final PrivateKey privateKey;


  public static void main(String[] args) throws Exception {
    RSAEncryption encrypter = new RSAEncryption();

    String plainText = "Hallo Lucas!";
    // Encryption
    byte[] cipherTextArray = encrypter.encrypt(plainText);
    String encryptedText = Base64.getEncoder().encodeToString(cipherTextArray);
    System.out.println("Encrypted Text : "+encryptedText);

    // Decryption
    String decryptedText = encrypter.decrypt(cipherTextArray);
    System.out.println("DeCrypted Text : "+decryptedText);
  }

  public RSAEncryption() throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
    File privateKeyFile = new File(getClass().getClassLoader().getResource(PRIVATE_KEY_PATH).getFile());
    byte[] privateKeyBytes = Files.readAllBytes(privateKeyFile.toPath());
    EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);

    File publicKeyFile = new File(getClass().getClassLoader().getResource(PUBLIC_KEY_PATH).getFile());
    byte[] publicKeyBytes = Files.readAllBytes(publicKeyFile.toPath());
    KeyFactory keyFactory = KeyFactory.getInstance(RSA);
    EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);

    publicKey = keyFactory.generatePublic(publicKeySpec);
    privateKey = keyFactory.generatePrivate(privateKeySpec);
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

  public String decrypt(String cipherText) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException,
      NoSuchAlgorithmException, NoSuchPaddingException {
    return decrypt(Base64.getDecoder().decode(cipherText));
  }
}
