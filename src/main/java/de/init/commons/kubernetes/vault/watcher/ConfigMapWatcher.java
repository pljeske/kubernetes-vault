package de.init.commons.kubernetes.vault.watcher;

import de.init.commons.kubernetes.vault.rsa.RSAEncryption;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;

public class ConfigMapWatcher implements Watcher<ConfigMap> {
  private RSAEncryption encryption;

  public ConfigMapWatcher(RSAEncryption encryption) {
    this.encryption = encryption;
  }

  @Override
  public void eventReceived(Action action, ConfigMap configMap) {
    // decrypt encrypted config map values (& start rolling update?)
  }

  @Override
  public void onClose(WatcherException e) {

  }
}
