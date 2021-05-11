package de.init.commons.kubernetes.vault;
import de.init.commons.kubernetes.vault.rsa.RSAEncryption;
import de.init.commons.kubernetes.vault.watcher.ConfigMapWatcher;
import de.init.commons.kubernetes.vault.watcher.DeploymentWatcher;
import de.init.commons.kubernetes.vault.watcher.LicenseWatcher;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KubernetesVaultApp implements CommandLineRunner {
  @Autowired
  private RSAEncryption encryption;
  @Autowired
  private KubernetesClient client;
  @Autowired
  private DeploymentWatcher deploymentWatcher;

  public static void main(String[] args) {
    SpringApplication.run(KubernetesVaultApp.class, args);
  }

  @Override
  public void run(String... args) throws Exception {
    client.apps().deployments().inAnyNamespace().watch(deploymentWatcher);
    client.secrets().inAnyNamespace().watch(new LicenseWatcher(client, encryption));
    client.configMaps().inAnyNamespace().watch(new ConfigMapWatcher(encryption));
  }
}
