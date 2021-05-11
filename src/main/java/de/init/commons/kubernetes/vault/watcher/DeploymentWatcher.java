package de.init.commons.kubernetes.vault.watcher;

import de.init.commons.kubernetes.vault.rsa.RSAEncryption;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.EnvFromSource;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class DeploymentWatcher implements Watcher<Deployment> {
  private static final Logger LOG = LoggerFactory.getLogger(DeploymentWatcher.class);
  public static final String ENCRYPTION_ANNOTATION = "ENCRYPTED:";
  private final KubernetesClient client;
  private final RSAEncryption encryption;

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

        for (EnvVar variable : environmentVariables) {
          if (variable.getValue().startsWith(ENCRYPTION_ANNOTATION)) {
            String encrypted = variable.getValue().replaceFirst(ENCRYPTION_ANNOTATION, "").replace(" ", "");
            String decrypted;
            try {
              decrypted = encryption.decrypt(encrypted);
            } catch (Exception e) {
              decrypted = variable.getValue();
              LOG.error("Variable couldn't be decrypted: {}", variable.getName());
            }
            variable.setValue(decrypted);
          }
        }
        client.apps().deployments().inNamespace(deployment.getMetadata().getNamespace()).createOrReplace(deployment);
      }
    }
  }

  @Override
  public void onClose(WatcherException e) {
    LOG.info("DeploymentWatcher closed.");
  }
}