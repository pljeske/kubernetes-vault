package de.init.commons.kubernetes.vault.crd;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;

/**
 * @author Peer-Lucas Jeske
 * created 09.07.2021
 */
@Group("commons.init.de")
@Version("v1alpha1")
public class VaultSecret extends CustomResource<VaultSecretSpec, VaultSecretStatus> implements Namespaced {
}
