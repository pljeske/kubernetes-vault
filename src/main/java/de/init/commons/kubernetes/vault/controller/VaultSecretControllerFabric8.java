package de.init.commons.kubernetes.vault.controller;

import com.bettercloud.vault.VaultException;
import de.init.commons.kubernetes.vault.crd.VaultSecret;
import de.init.commons.kubernetes.vault.crd.VaultSecretStatus;
import de.init.commons.kubernetes.vault.service.ResourceCreatorService;
import de.init.commons.kubernetes.vault.util.Base64Encoder;
import de.init.commons.kubernetes.vault.util.HashUtil;
import de.init.commons.kubernetes.vault.vault.VaultConnector;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import io.javaoperatorsdk.operator.api.UpdateControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.Map;

/**
 * @author Peer-Lucas Jeske
 * created 21.07.2021
 */
public class VaultSecretControllerFabric8 implements Watcher<VaultSecret> {
    private static final Logger LOG = LoggerFactory.getLogger(VaultSecretControllerFabric8.class);

    private final KubernetesClient client;
    private final VaultConnector vault;
    private final ResourceCreatorService resourceCreatorService;

    public VaultSecretControllerFabric8(KubernetesClient client, VaultConnector vault,
                                 ResourceCreatorService resourceCreatorService) {
        this.client = client;
        this.vault = vault;
        this.resourceCreatorService = resourceCreatorService;
    }

    @Override
    public void eventReceived(Action action, VaultSecret vaultSecret) {
        switch (action) {
            case ADDED:
                resourceAdded(vaultSecret);
                break;
            case MODIFIED:
                resourceModified(vaultSecret);
                break;
            case DELETED:
                resourceDeleted(vaultSecret);
                break;
            case ERROR:
                error(vaultSecret);
        }
    }

    @Override
    public void onClose(WatcherException e) {

    }

    private void resourceAdded(VaultSecret vaultSecret) {
        String namespace = vaultSecret.getMetadata().getNamespace();
        String name = vaultSecret.getMetadata().getName();
        String secretReference = vaultSecret.getSpec().getSecretReference();

        LOG.info("Creating or updating VaultSecret: {}", vaultSecret.getMetadata().getName());
        LOG.info("Vault reference: {}", vaultSecret.getSpec().getSecretReference());

        try {
            Map<String, String> secretData = vault.getCredentials(vaultSecret.getSpec().getSecretReference());
            String secretHash = HashUtil.getHash(secretData);
            Base64Encoder.encodeMapValues(secretData);

            Map<String, String> annotations = Map.of("vaultsecret", "true");

            resourceCreatorService.createSecretInCluster(vaultSecret.getMetadata().getName(),
                    vaultSecret.getMetadata().getNamespace(), annotations, secretData);

            VaultSecretStatus status = new VaultSecretStatus();
            status.setSecretCreated(true);
            status.setSecretHash(secretHash);
            status.setLastCheckedForChanges(ZonedDateTime.now().format(ResourceCreatorService.DATE_TIME_FORMATTER));

            vaultSecret.setStatus(status);

            client.customResources(VaultSecret.class).inNamespace(vaultSecret.getMetadata().getNamespace())
//            return UpdateControl.updateCustomResourceAndStatus(vaultSecret);
        } catch (VaultException e) {
            LOG.error("Getting the data from vault with reference '{}' failed.",
                    vaultSecret.getSpec().getSecretReference(), e);
//            return UpdateControl.noUpdate();
        }
    }

    private void resourceModified(VaultSecret vaultSecret) {

    }

    private void resourceDeleted(VaultSecret vaultSecret) {

    }

    private void error(VaultSecret vaultSecret) {

    }
}
