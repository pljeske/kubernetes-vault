package de.init.commons.kubernetes.vault;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KubernetesVaultApp {

  public static void main(String[] args) {
    SpringApplication.run(KubernetesVaultApp.class, args);
  }
}
