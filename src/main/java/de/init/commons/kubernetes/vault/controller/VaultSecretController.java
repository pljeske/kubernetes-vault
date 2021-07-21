package de.init.commons.kubernetes.vault.controller;

import com.bettercloud.vault.VaultException;
import de.init.commons.kubernetes.vault.crd.VaultSecret;
import de.init.commons.kubernetes.vault.crd.VaultSecretStatus;
import de.init.commons.kubernetes.vault.service.ResourceCreatorService;
import de.init.commons.kubernetes.vault.util.Base64Encoder;
import de.init.commons.kubernetes.vault.util.HashUtil;
import de.init.commons.kubernetes.vault.vault.VaultConnector;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.Context;
import io.javaoperatorsdk.operator.api.Controller;
import io.javaoperatorsdk.operator.api.DeleteControl;
import io.javaoperatorsdk.operator.api.ResourceController;
import io.javaoperatorsdk.operator.api.UpdateControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author Peer-Lucas Jeske
 * created 09.07.2021
 */
@Controller
public class VaultSecretController implements ResourceController<VaultSecret> {
    private static final Logger LOG = LoggerFactory.getLogger(VaultSecretController.class);

    private final KubernetesClient client;
    private final VaultConnector vault;
    private final ResourceCreatorService resourceCreatorService;

    public VaultSecretController(KubernetesClient client, VaultConnector vault,
            ResourceCreatorService resourceCreatorService) {
        this.client = client;
        this.vault = vault;
        this.resourceCreatorService = resourceCreatorService;
    }

    @Override
    public DeleteControl deleteResource(VaultSecret resource, Context<VaultSecret> context) {
        LOG.info("Deleting resource: {}", resource.getMetadata().getName());
        // TODO: implement test to find out if "ownerReference" was used so Kubernetes garbage collection would take
        //  care of the deletion
        client.secrets().inNamespace(resource.getMetadata().getNamespace())
                .withName(resource.getMetadata().getName()).delete();
        return DeleteControl.DEFAULT_DELETE;
    }

    @Override
    public UpdateControl<VaultSecret> createOrUpdateResource(VaultSecret vaultSecret, Context<VaultSecret> context) {
        LOG.info("Creating or updating VaultSecret: {}", vaultSecret.getMetadata().getName());
        LOG.info("Vault reference: {}", vaultSecret.getSpec().getSecretReference());

        LOG.info("TEST");

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
            status.setLastCheckedForChanges(LocalDateTime.now().format(ResourceCreatorService.DATE_TIME_FORMATTER));

            vaultSecret.setStatus(status);

            return UpdateControl.updateCustomResourceAndStatus(vaultSecret);
        } catch (VaultException e) {
            LOG.error("Getting the data from vault with reference '{}' failed.",
                    vaultSecret.getSpec().getSecretReference(), e);
            return UpdateControl.noUpdate();
        }
    }
}