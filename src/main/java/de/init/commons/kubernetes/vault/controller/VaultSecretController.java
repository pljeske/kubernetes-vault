package de.init.commons.kubernetes.vault.controller;

import de.init.commons.kubernetes.vault.crd.VaultSecret;
import de.init.commons.kubernetes.vault.crd.VaultSecretStatus;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * @author Peer-Lucas Jeske
 * created 09.07.2021
 */
@Controller
public class VaultSecretController implements ResourceController<VaultSecret> {
    private static final Logger LOG = LoggerFactory.getLogger(VaultSecretController.class);
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    private KubernetesClient client;

    public VaultSecretController(KubernetesClient client) {
        this.client = client;
    }

    @Override
    public DeleteControl deleteResource(VaultSecret resource, Context<VaultSecret> context) {
        LOG.info("Deleting resource: {}", resource.getMetadata().getName());
        return DeleteControl.DEFAULT_DELETE;
    }

    @Override
    public UpdateControl<VaultSecret> createOrUpdateResource(VaultSecret vaultSecret, Context<VaultSecret> context) {
        VaultSecretStatus status = vaultSecret.getStatus();

        LOG.info("Creating resource: {}", vaultSecret.getMetadata().getName());
        LOG.info("Password references: {}", Arrays.stream(vaultSecret.getSpec().getPasswords()).collect(Collectors.toList()));

        if (status == null) {
            status = new VaultSecretStatus();
        }
        status.setSecretCreated(true);
        status.setDateSecretCreated(LocalDateTime.now().format(DATE_TIME_FORMATTER));
        status.setDateSecretChanged(LocalDateTime.now().format(DATE_TIME_FORMATTER));

        vaultSecret.setStatus(status);

        return UpdateControl.updateCustomResourceAndStatus(vaultSecret);
    }

    private UpdateControl<VaultSecret> vaultSecretAdded(VaultSecret vaultSecret) {
        LOG.info("Creating resource: {}", vaultSecret.getMetadata().getName());
        LOG.info("Password references: {}", Arrays.stream(vaultSecret.getSpec().getPasswords()).collect(Collectors.toList()));

//        LocalDateTime now = LocalDateTime.now();
        VaultSecretStatus vaultSecretStatus = new VaultSecretStatus(true, "testdate", "testdate");

//        vaultSecret.setStatus(vaultSecretStatus);

        return UpdateControl.updateCustomResourceAndStatus(vaultSecret);
    }

    private UpdateControl<VaultSecret> vaultSecretModified(VaultSecret vaultSecret) {
        LOG.info("Updating resource: {}", vaultSecret.getMetadata().getName());
        return UpdateControl.noUpdate();
    }
}
