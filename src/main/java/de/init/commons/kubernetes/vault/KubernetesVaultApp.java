package de.init.commons.kubernetes.vault;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.vault.authentication.SessionManager;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class KubernetesVaultApp {
  private static final Logger LOG = LoggerFactory.getLogger(KubernetesVaultApp.class);

  @Autowired
  private SessionManager sessionManager;

  public static void main(String[] args) {
    SpringApplication.run(KubernetesVaultApp.class, args);
  }

  @PostConstruct
  public void testVaultConnection() {
    LOG.info("Got vault token: {}", sessionManager.getSessionToken());
  }
}
