package de.init.commons.kubernetes.vault.watcher;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.EnvFromSource;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DeploymentWatcher implements Watcher<Deployment> {
  private static final Logger LOG = LoggerFactory.getLogger(DeploymentWatcher.class);
  private KubernetesClient client;

  public DeploymentWatcher(KubernetesClient client) {
    this.client = client;
  }
  @Override
  public void eventReceived(Action action, Deployment deployment) {
    if (action.equals(Action.ADDED) || action.equals(Action.MODIFIED)) {
      List<Container> containers = deployment.getSpec().getTemplate().getSpec().getContainers();
      for (Container container : containers) {
        List<EnvVar> environmentVariables = container.getEnv();
        List<EnvFromSource> environmentFromSource = container.getEnvFrom();
        environmentVariables.forEach(envVar -> LOG.debug(envVar.toString()));
        environmentFromSource.forEach(envFromSource -> LOG.debug(envFromSource.toString()));
      }
    }
  }

  @Override
  public void onClose(WatcherException e) {
    LOG.info("DeplomentWatcher closed.");
  }
}