package de.init.commons.kubernetes.vault.controller;

import de.init.commons.kubernetes.vault.rsa.RSAEncryption;
import org.apache.tomcat.util.codec.binary.Base64;
import org.bouncycastle.jcajce.provider.asymmetric.RSA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.websocket.server.PathParam;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
public class KeyController {
  private RSAEncryption encryption;

  @Autowired
  public KeyController(RSAEncryption encryption) {
    this.encryption = encryption;
  }

  @GetMapping("/publickey")
  public ResponseEntity<Resource> download() throws IOException {
    File file = new File(getClass().getClassLoader().getResource(RSAEncryption.PUBLIC_KEY_PATH).getFile());

    HttpHeaders header = new HttpHeaders();
    header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=public.key");
    header.add("Cache-Control", "no-cache, no-store, must-revalidate");
    header.add("Pragma", "no-cache");
    header.add("Expires", "0");

    Path path = Paths.get(file.getAbsolutePath());
    ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));

    return ResponseEntity.ok()
        .headers(header)
        .contentLength(file.length())
        .contentType(MediaType.parseMediaType("application/octet-stream"))
        .body(resource);
  }

  @GetMapping("/encrypt")
  public String encryptText(@RequestParam(value = "text", required = true) String text) throws Exception {
    byte[] encryptedBytes = encryption.encrypt(text);
    return Base64.encodeBase64String(encryptedBytes);
  }
}
