package de.init.commons.kubernetes.vault.watcher;

import de.init.commons.kubernetes.vault.rsa.RSAEncryption;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ConfigMapWatcher extends AbstractWatcher<ConfigMap> {
  private static final Logger LOG = LoggerFactory.getLogger(ConfigMapWatcher.class);

  @Autowired
  public ConfigMapWatcher(KubernetesClient client, RSAEncryption encryption) {
    super(client, encryption);
  }

  @Override
  public void added(ConfigMap resource) {
    Map<String,String> decryptedVariables = new HashMap<>();
    Map<String,String> variables = resource.getData();
    variables.forEach((key, value) -> {
      if (value.startsWith(encryptionPrefix)) {
        value = decrypt(value);
      }
      decryptedVariables.put(key, value);
    });
    resource.setData(decryptedVariables);
//    client.configMaps().inNamespace(resource.getMetadata().getNamespace()).replace(resource);
    client.configMaps().inNamespace(resource.getMetadata().getNamespace()).createOrReplace(resource);
  }

  @Override
  public void modified(ConfigMap resource) {
    LOG.debug("ConfigMap {} in namespace {} modified.", resource.getMetadata().getName(), resource.getMetadata().getNamespace());
  }

  @Override
  public void deleted(ConfigMap resource) {
    LOG.debug("ConfigMap {} in namespace {} deleted.", resource.getMetadata().getName(), resource.getMetadata().getNamespace());
  }

//  private List<Deployment> findDeployments(ConfigMap resource) {
//
//    client.apps().deployments().inNamespace(resource.getMetadata().getNamespace()).withInvolvedObject(resource).list().getItems();
//  }
}
