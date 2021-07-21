package de.init.commons.kubernetes.vault;

import de.init.commons.kubernetes.vault.controller.VaultSecretController;
import de.init.commons.kubernetes.vault.crd.VaultSecret;
import de.init.commons.kubernetes.vault.service.ResourceCreatorService;
import de.init.commons.kubernetes.vault.vault.VaultConnector;
import de.init.commons.kubernetes.vault.watcher.DeploymentWatcher;
import de.init.commons.kubernetes.vault.watcher.SecretWatcher;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import io.fabric8.kubernetes.client.informers.SharedInformerFactory;
import io.javaoperatorsdk.operator.Operator;
import io.javaoperatorsdk.operator.config.runtime.DefaultConfigurationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.inject.Inject;

@SpringBootApplication
@EnableScheduling
public class KubernetesVaultApp implements CommandLineRunner {
  private final KubernetesClient client;
  private final DeploymentWatcher deploymentWatcher;
  private final SecretWatcher secretWatcher;
  private final VaultConnector vault;
  private final ResourceCreatorService resourceCreatorService;

  @Inject
  public KubernetesVaultApp(KubernetesClient client, DeploymentWatcher deploymentWatcher, SecretWatcher secretWatcher,
          VaultConnector vault, ResourceCreatorService resourceCreatorService) {
    this.client = client;
    this.deploymentWatcher = deploymentWatcher;
    this.secretWatcher = secretWatcher;
    this.vault = vault;
    this.resourceCreatorService = resourceCreatorService;
  }

  public static void main(String[] args) {
    SpringApplication.run(KubernetesVaultApp.class, args);
  }

  @Override
  public void run(String... args) {
//    client.apps().deployments().inAnyNamespace().watch(deploymentWatcher);
//    client.secrets().inAnyNamespace().watch(secretWatcher);
//    client.configMaps().inAnyNamespace().watch(configMapWatcher);

    VaultSecretController vaultSecretController = new VaultSecretController(client, vault, resourceCreatorService);
    Operator operator = new Operator(client, DefaultConfigurationService.instance());
    operator.register(vaultSecretController);

    SharedInformerFactory informerFactory = client.informers();
    SharedIndexInformer<VaultSecret> vaultSecretSharedIndexInformer = informerFactory
            .sharedIndexInformerForCustomResource(VaultSecret.class, 10 * 60 * 1000);

  }
}
