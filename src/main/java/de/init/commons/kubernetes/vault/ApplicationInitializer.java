package de.init.commons.kubernetes.vault;

import de.init.commons.kubernetes.vault.controller.VaultSecretController;
import de.init.commons.kubernetes.vault.watcher.DeploymentWatcher;
import de.init.commons.kubernetes.vault.watcher.SecretWatcher;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.Operator;
import io.javaoperatorsdk.operator.config.runtime.DefaultConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import javax.inject.Inject;


@Component
public class ApplicationInitializer implements ApplicationListener<ContextRefreshedEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(ApplicationInitializer.class);
    private final KubernetesClient client;
    private final DeploymentWatcher deploymentWatcher;
    private final SecretWatcher secretWatcher;

    @Inject
    public ApplicationInitializer(KubernetesClient client, DeploymentWatcher deploymentWatcher,
                                  SecretWatcher secretWatcher) {
        this.client = client;
        this.deploymentWatcher = deploymentWatcher;
        this.secretWatcher = secretWatcher;
    }


    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        client.apps().deployments().inAnyNamespace().watch(deploymentWatcher);
        client.secrets().inAnyNamespace().watch(secretWatcher);
//    client.configMaps().inAnyNamespace().watch(configMapWatcher);

        VaultSecretController vaultSecretController = new VaultSecretController(client);
        Operator operator = new Operator(client, DefaultConfigurationService.instance());
        operator.register(vaultSecretController);
    }
}