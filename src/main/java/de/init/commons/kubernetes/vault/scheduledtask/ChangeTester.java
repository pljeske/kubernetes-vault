package de.init.commons.kubernetes.vault.scheduledtask;

import com.bettercloud.vault.VaultException;
import de.init.commons.kubernetes.vault.crd.VaultSecret;
import de.init.commons.kubernetes.vault.service.ResourceCreatorService;
import de.init.commons.kubernetes.vault.service.VaultConnector;
import de.init.commons.kubernetes.vault.util.HashUtil;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;

import javax.inject.Inject;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@EnableAsync
public class ChangeTester {
    private static final Logger LOG = LoggerFactory.getLogger(ChangeTester.class);

    @Value("${vaultcheck.maxminutes}")
    private int maxMinutes;
    @Value("${vaultcheck.timedelay}")
    private int timeDelay;

    // TODO: do we need locks?
//    private Set<VaultSecret> lockedSecrets;

    private final VaultConnector vault;
    private final KubernetesClient client;
    private final ResourceCreatorService resourceCreatorService;

    @Inject
    public ChangeTester(VaultConnector vault, KubernetesClient client, ResourceCreatorService resourceCreatorService) {
        this.vault = vault;
        this.client = client;
        this.resourceCreatorService = resourceCreatorService;
//        this.lockedSecrets = new HashSet<>();
    }

//    @Async
    @Scheduled(fixedRateString = "${vaultcheck.timedelay}")
    public void checkVaultForChanges() {
        ZonedDateTime timeMinutesAgo = ZonedDateTime.now().minusMinutes(maxMinutes);

        List<VaultSecret> allVaultSecrets = client.customResources(VaultSecret.class).inAnyNamespace().list().getItems();
//        allVaultSecrets.removeAll(this.lockedSecrets);

        for (VaultSecret vaultSecret : allVaultSecrets) {

            String lastCheckedString = vaultSecret.getStatus().getLastCheckedForChanges();

            ZonedDateTime lastChecked = ZonedDateTime
                    .parse(lastCheckedString, ResourceCreatorService.DATE_TIME_FORMATTER);
            if (lastChecked.isBefore(timeMinutesAgo)) {
                checkForChangesInVault(vaultSecret);
            }
        }
    }

    private void checkForChangesInVault(VaultSecret vaultSecret) {
        try {
            Map<String,String> valuesInVault = vault.getCredentials(vaultSecret.getSpec().getSecretReference());
            String valuesInVaultHash = HashUtil.getHash(valuesInVault);
            String currentValuesHash = vaultSecret.getStatus().getSecretHash();

            if (!valuesInVaultHash.equals(currentValuesHash)) {
                LOG.info("Values in vault and secret differ. Creating new secret '{}' in namespace {}",
                        vaultSecret.getMetadata().getName(), vaultSecret.getMetadata().getNamespace());

                String name = vaultSecret.getMetadata().getName();
                String namespace = vaultSecret.getMetadata().getNamespace();
                Map<String,String> annotations = vaultSecret.getMetadata().getAnnotations();

                resourceCreatorService.createSecretInCluster(name, namespace, annotations, valuesInVault);
            }
        } catch (VaultException e) {
            LOG.error("There was a problem getting values from vault at path {}. Trying again in {} ms",
                    vaultSecret.getSpec().getSecretReference(), timeDelay, e);
        }
//        this.lockedSecrets.remove(vaultSecret);
    }
}
