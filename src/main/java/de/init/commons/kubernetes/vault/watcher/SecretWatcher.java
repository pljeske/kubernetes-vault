package de.init.commons.kubernetes.vault.watcher;

import de.init.commons.kubernetes.vault.rsa.RSAEncryption;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public class SecretWatcher extends AbstractWatcher<Secret>{
  private static final Logger LOG = LoggerFactory.getLogger(SecretWatcher.class);

  @Inject
  protected SecretWatcher(KubernetesClient client, RSAEncryption encryption) {
    super(client, encryption);
  }

  @Override
  public void added(Secret resource) {
    Map<String,String> annotations = resource.getMetadata().getAnnotations();
    if (annotations != null && annotations.containsKey(encryptionAnnotation) && annotations.get(encryptionAnnotation).equals("true")){
      // TODO: is it necessary to find deployments that use the secret since secret has to be created first anyway?
//      Set<Deployment> deploymentsToRestart = new HashSet<>();
//      List<Deployment> deployments = client.apps().deployments().inNamespace(resource.getMetadata().getNamespace()).list().getItems();
//      deployments.forEach(deployment -> {
//        List<Volume> volumes = deployment.getSpec().getTemplate().getSpec().getVolumes();
//        volumes.forEach(volume -> {
//          SecretVolumeSource secretVolumeSource = volume.getSecret();
//          if (secretVolumeSource.getSecretName().equals(resource.getMetadata().getName())){
//            deploymentsToRestart.add(deployment);
//          }
//        });
//
//        deployment.getSpec().getTemplate().getSpec().getContainers().forEach(container -> {
//          container.getEnvFrom().forEach(envFromSource -> {
//            if(envFromSource.getSecretRef().getName().equals(resource.getMetadata().getName())){
//              deploymentsToRestart.add(deployment);
//            }
//          });
//        });
//      });

      // decrypt secret
      Map<String,String> variables = resource.getData();
      Map<String,String> decryptedVariables = new HashMap<>();

      variables.forEach((key, value) -> {
        String decrypted = decryptBase64(value);
        String base64Encoded = Base64.getEncoder().encodeToString(decrypted.getBytes(StandardCharsets.UTF_8));
        decryptedVariables.put(key, base64Encoded);
      });
      resource.setData(decryptedVariables);
      resource.getMetadata().getAnnotations().put(encryptionAnnotation, "false");

      client.secrets().inNamespace(resource.getMetadata().getNamespace()).createOrReplace(resource);

//      // restart deployments
//      deploymentsToRestart.forEach(deployment -> {
//        // TODO: restart oder was anderes?
//        client.apps().deployments().inNamespace(resource.getMetadata().getNamespace()).withName(deployment.getMetadata().getName())
//            .rolling().withTimeout(60, TimeUnit.SECONDS).restart();
//      });
    }
  }

  @Override
  public void modified(Secret resource) {
    Map<String,String> annotations = resource.getMetadata().getAnnotations();
    if (annotations.containsKey(encryptionAnnotation) && annotations.get(encryptionAnnotation).equals("true")) {
      added(resource);
    }
  }

  @Override
  public void deleted(Secret resource) {
    LOG.info("Secret {} was deleted.", resource.getMetadata().getName());
  }
}
