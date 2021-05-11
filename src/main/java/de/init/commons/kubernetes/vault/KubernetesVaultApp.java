package de.init.commons.kubernetes.vault;
import de.init.commons.kubernetes.vault.watcher.ConfigMapWatcher;
import de.init.commons.kubernetes.vault.watcher.DeploymentWatcher;
import de.init.commons.kubernetes.vault.watcher.LicenseWatcher;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KubernetesVaultApp implements CommandLineRunner {
  @Autowired
  private KubernetesClient client;
  @Autowired
  private DeploymentWatcher deploymentWatcher;
  @Autowired
  private LicenseWatcher licenseWatcher;
  @Autowired
  private ConfigMapWatcher configMapWatcher;

  public static void main(String[] args) {
    SpringApplication.run(KubernetesVaultApp.class, args);
  }

  @Override
  public void run(String... args) {
    client.apps().deployments().inAnyNamespace().watch(deploymentWatcher);
    client.secrets().inAnyNamespace().watch(licenseWatcher);
    client.configMaps().inAnyNamespace().watch(configMapWatcher);
  }
}
