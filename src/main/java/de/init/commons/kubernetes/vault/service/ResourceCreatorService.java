package de.init.commons.kubernetes.vault.service;

import de.init.commons.kubernetes.vault.crd.VaultSecret;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class ResourceCreatorService {
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    private final KubernetesClient client;

    @Inject
    public ResourceCreatorService(KubernetesClient client) {
        this.client = client;
    }

    public void createSecretInCluster(String name, String namespace, Map<String,String> annotations,
            Map<String,String> data) {

        Secret secret = new SecretBuilder()
                .withNewMetadata()
                .withName(name)
                .withAnnotations(annotations)
                .endMetadata()
                .addToData(data)
                .build();

        client.secrets().inNamespace(namespace).createOrReplace(secret);
    }

    public void updateVaultSecretInCluster(VaultSecret vaultSecret, LocalDateTime lastChecked) {
        vaultSecret.getStatus()
                .setLastCheckedForChanges(lastChecked.format(ResourceCreatorService.DATE_TIME_FORMATTER));
        client.customResources(VaultSecret.class).inNamespace(vaultSecret.getMetadata().getNamespace())
                .createOrReplace(vaultSecret);
    }
}
