package de.init.commons.kubernetes.vault.controller;

import com.bettercloud.vault.VaultException;
import de.init.commons.kubernetes.vault.crd.VaultSecret;
import de.init.commons.kubernetes.vault.crd.VaultSecretStatus;
import de.init.commons.kubernetes.vault.util.Base64Encoder;
import de.init.commons.kubernetes.vault.vault.VaultConnector;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * @author Peer-Lucas Jeske
 * created 09.07.2021
 */
@Controller
public class VaultSecretController implements ResourceController<VaultSecret> {
    private static final Logger LOG = LoggerFactory.getLogger(VaultSecretController.class);
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    private final KubernetesClient client;
    private final VaultConnector vault;

    public VaultSecretController(KubernetesClient client, VaultConnector vault) {
        this.client = client;
        this.vault = vault;
    }

    @Override
    public DeleteControl deleteResource(VaultSecret resource, Context<VaultSecret> context) {
        LOG.info("Deleting resource: {}", resource.getMetadata().getName());
        client.secrets().inNamespace(resource.getMetadata().getNamespace())
                .withName(resource.getMetadata().getName()).delete();
        return DeleteControl.DEFAULT_DELETE;
    }

    @Override
    public UpdateControl<VaultSecret> createOrUpdateResource(VaultSecret vaultSecret, Context<VaultSecret> context) {
        LOG.info("Creating or updating VaultSecret: {}", vaultSecret.getMetadata().getName());
        LOG.info("Vault reference: {}", vaultSecret.getSpec().getSecretReference());

        try {
            Map<String, String> secretData = vault.getCredentials(vaultSecret.getSpec().getSecretReference());

            // Base64 encode the vault entries
            for (Map.Entry<String, String> entry : secretData.entrySet()) {
                String unencoded = entry.getValue();
                secretData.put(entry.getKey(), Base64Encoder.encode(unencoded));
            }

            Map<String, String> annotations = Map.of("vaultsecret", "true");
            Secret secret = new SecretBuilder()
                    .withNewMetadata()
                    .withName(vaultSecret.getMetadata().getName())
                    .withAnnotations(annotations)
                    .endMetadata()
                    .addToData(secretData)
                    .build();

            client.secrets().inNamespace(vaultSecret.getMetadata().getNamespace()).createOrReplace(secret);

            VaultSecretStatus status = new VaultSecretStatus();
            status.setSecretCreated(true);
            status.setDateSecretCreated(LocalDateTime.now().format(DATE_TIME_FORMATTER));
            status.setDateSecretChanged(LocalDateTime.now().format(DATE_TIME_FORMATTER));

            vaultSecret.setStatus(status);

            return UpdateControl.updateCustomResourceAndStatus(vaultSecret);
        } catch (VaultException e) {
            LOG.error("Getting the data from vault with reference '{}' failed.",
                    vaultSecret.getSpec().getSecretReference(), e);
            return UpdateControl.noUpdate();
        }
    }
}