package de.init.commons.kubernetes.vault.watcher;

import de.init.commons.kubernetes.vault.rsa.RSAEncryption;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DeploymentWatcher extends AbstractWatcher<Deployment> {
  private static final Logger LOG = LoggerFactory.getLogger(DeploymentWatcher.class);

  @Inject
  public DeploymentWatcher(KubernetesClient client, RSAEncryption encryption) {
    super(client, encryption);
  }

  @Override
  public void added(Deployment resource) {
    Map<String,String> annotations = resource.getMetadata().getAnnotations();
    if (annotations.containsKey(encryptionAnnotation) && annotations.get(encryptionAnnotation).equals("true")) {
      List<Container> containers = resource.getSpec().getTemplate().getSpec().getContainers();
      for (Container container : containers) {
        List<EnvVar> environmentVariables = container.getEnv();

        Map<String,String> decryptedVariables = new HashMap<>();
        List<EnvVar> variablesToRemove = new ArrayList<>();

        for (EnvVar variable : environmentVariables) {
          if (variable.getValue() != null && variable.getValue().startsWith(encryptionPrefix)) {
            String decrypted = decrypt(variable);
            decryptedVariables.put(variable.getName(), Base64.toBase64String(decrypted.getBytes(StandardCharsets.UTF_8)));
            variablesToRemove.add(variable);
          }
        }

        if (!decryptedVariables.isEmpty()) {
          Secret secret = createSecret(decryptedVariables, resource);

          secret = client.secrets().inNamespace(resource.getMetadata().getNamespace()).createOrReplace(secret);
          EnvFromSource envFromSource = new EnvFromSourceBuilder()
              .withNewSecretRef().withName(secret.getMetadata().getName()).endSecretRef().build();
          List<EnvFromSource> envFromSources = container.getEnvFrom();
          envFromSources.add(envFromSource);
          container.setEnvFrom(envFromSources);

          variablesToRemove.forEach(var -> container.getEnv().remove(var));
        }
      }
      // annotate successful decryption
      resource.getMetadata().getAnnotations().put(encryptionAnnotation, "false");
      client.apps().deployments().inNamespace(resource.getMetadata().getNamespace()).createOrReplace(resource);
    }
  }

  @Override
  public void modified(Deployment resource) {
    LOG.debug("TEST: {}", resource.getMetadata().getAnnotations());
    Map<String,String> annotations = resource.getMetadata().getAnnotations();
    if (annotations.containsKey(encryptionAnnotation) && annotations.get(encryptionAnnotation).equals("true")) {
      added(resource);
    }
  }

  @Override
  public void deleted(Deployment resource) {
    String namespace = resource.getMetadata().getNamespace();
    List<Secret> secrets = client.secrets().inNamespace(namespace).list().getItems();
    for (Secret secret : secrets) {
      if (secret.getMetadata().getAnnotations().containsKey(DECRYPTED_FOR)) {
        String value = secret.getMetadata().getAnnotations().get(DECRYPTED_FOR);
        if (value.equals(resource.getMetadata().getName())) {
          client.secrets().delete(secret);
        }
      }
    }
  }


//  private void updateKubernetesResources(Deployment deployment, Container container, List<EnvVar> variablesToRemove, Secret secret) {
//    secret = client.secrets().inNamespace(deployment.getMetadata().getNamespace()).createOrReplace(secret);
//    EnvFromSource envFromSource = new EnvFromSourceBuilder()
//        .withNewSecretRef().withName(secret.getMetadata().getName()).endSecretRef().build();
//    List<EnvFromSource> envFromSources = container.getEnvFrom();
//    envFromSources.add(envFromSource);
//    container.setEnvFrom(envFromSources);
//
//    variablesToRemove.forEach(var -> container.getEnv().remove(var));
//
//    client.apps().deployments().inNamespace(deployment.getMetadata().getNamespace()).replace(deployment);
////    client.apps().deployments().inNamespace(deployment.getMetadata().getNamespace()).createOrReplace(deployment);
//  }
}