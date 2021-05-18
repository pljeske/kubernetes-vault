package de.init.commons.kubernetes.vault.watcher;

import de.init.commons.kubernetes.vault.rsa.RSAEncryption;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

public abstract class AbstractWatcher<T extends HasMetadata> implements Watcher<T>, EventWatcher<T> {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractWatcher.class);
  public static final String DECRYPTED_FOR = "decrypted_for";

  @Value("${encryption.prefix}")
  protected String encryptionPrefix;
  @Value("${encryption.annotation}")
  protected String encryptionAnnotation;

  private final RSAEncryption encryption;
  protected final KubernetesClient client;

  protected AbstractWatcher(KubernetesClient client, RSAEncryption encryption) {
    this.client = client;
    this.encryption = encryption;
  }

  @Override
  public void eventReceived(Action action, T resource) {
    switch (action) {
      case ADDED: added(resource); break;
      case MODIFIED: modified(resource); break;
      case DELETED: deleted(resource); break;
      default: error(resource);
    }
  }

  @Override
  public void onClose(WatcherException e) {
    String classString = this.getClass().toString();
    LOG.error("{} closed because of an Exception.", classString, e);
  }

  @Override
  public void onClose() {
    String classString = this.getClass().toString();
    LOG.debug("{} closed gracefully.", classString);
  }

  public String decrypt(EnvVar variable) {
    String encrypted = variable.getValue().replaceFirst(encryptionPrefix, "").replace(" ", "");
    String decrypted;
    try {
      decrypted = encryption.decryptBase64(encrypted);
    } catch (Exception e) {
      decrypted = variable.getValue();
      LOG.error("Variable couldn't be decrypted: {}", variable.getName(), e);
    }
    return decrypted;
  }

  public String decrypt(String text) {
    String decrypted;
    try {
      decrypted = encryption.decrypt(text);
    } catch (Exception e) {
      decrypted = text;
      LOG.error("Variable couldn't be decrypted: {}", text, e);
    }
    return decrypted;
  }

  public Secret createSecret(Map<String,String> variables, T resource) {
    Map<String,String> annotations = Map.of(DECRYPTED_FOR, resource.getMetadata().getName());
    return new SecretBuilder()
        .withNewMetadata()
        .withNamespace(resource.getMetadata().getNamespace())
        .withName(resource.getMetadata().getName())
        .withAnnotations(annotations)
        .endMetadata()
        .withData(variables).build();
  }
}
