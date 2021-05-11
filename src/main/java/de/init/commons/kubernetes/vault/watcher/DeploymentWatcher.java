package de.init.commons.kubernetes.vault.watcher;

import de.init.commons.kubernetes.vault.rsa.RSAEncryption;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DeploymentWatcher implements Watcher<Deployment> {
  private static final Logger LOG = LoggerFactory.getLogger(DeploymentWatcher.class);
//  public static final String ENCRYPTION_ANNOTATION = "ENCRYPTED:";
  private final KubernetesClient client;
  private final RSAEncryption encryption;

  @Value("${encryption.prefix}")
  private String encryptionPrefix;

  @Autowired
  public DeploymentWatcher(KubernetesClient client, RSAEncryption encryption) {
    this.client = client;
    this.encryption = encryption;
  }
  @Override
  public void eventReceived(Action action, Deployment deployment) {
    if (action.equals(Action.ADDED) || action.equals(Action.MODIFIED)) {
      List<Container> containers = deployment.getSpec().getTemplate().getSpec().getContainers();
      for (Container container : containers) {
        List<EnvVar> environmentVariables = container.getEnv();

        Map<String,String> decryptedVariables = new HashMap<>();
        List<EnvVar> variablesToRemove = new ArrayList<>();

        for (EnvVar variable : environmentVariables) {
          if (variable.getValue().startsWith(encryptionPrefix)) {
            String decrypted = decryptEnvVar(variable);
            decryptedVariables.put(variable.getName(), Base64.toBase64String(decrypted.getBytes(StandardCharsets.UTF_8)));
            variablesToRemove.add(variable);
          }
        }
        if (!decryptedVariables.isEmpty()) {
          Secret secret = createSecret(decryptedVariables, deployment);
          updateKubernetesResources(deployment, container, variablesToRemove, secret);
        }

      }
    }
  }

  public String decryptEnvVar(EnvVar variable) {
    String encrypted = variable.getValue().replaceFirst(encryptionPrefix, "").replace(" ", "");
    String decrypted;
    try {
      decrypted = encryption.decrypt(encrypted);
    } catch (Exception e) {
      decrypted = variable.getValue();
      LOG.error("Variable couldn't be decrypted: {}", variable.getName(), e);
    }
    return decrypted;
  }

  private void updateKubernetesResources(Deployment deployment, Container container, List<EnvVar> variablesToRemove, Secret secret) {
    secret = client.secrets().inNamespace(deployment.getMetadata().getNamespace()).createOrReplace(secret);
    EnvFromSource envFromSource = new EnvFromSourceBuilder()
        .withNewSecretRef().withName(secret.getMetadata().getName()).endSecretRef().build();
    List<EnvFromSource> envFromSources = container.getEnvFrom();
    envFromSources.add(envFromSource);
    container.setEnvFrom(envFromSources);

    variablesToRemove.forEach(var -> container.getEnv().remove(var));

    client.apps().deployments().inNamespace(deployment.getMetadata().getNamespace()).createOrReplace(deployment);
  }

  @Override
  public void onClose(WatcherException e) {
    LOG.error("DeploymentWatcher closed because of an Exception.", e);
  }

  public static Secret createSecret(Map<String,String> variables, Deployment deployment) {
    return new SecretBuilder()
        .withNewMetadata()
        .withName(deployment.getMetadata().getName())
        .endMetadata()
        .withData(variables)
        .build();
  }
}