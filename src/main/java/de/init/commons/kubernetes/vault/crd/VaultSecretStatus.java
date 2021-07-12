package de.init.commons.kubernetes.vault.crd;

import lombok.Data;

/**
 * @author Peer-Lucas Jeske
 * created 09.07.2021
 */
@Data
public class VaultSecretStatus {
    private boolean secretCreated;
    private String dateSecretCreated;
    private String dateSecretChanged;

    public VaultSecretStatus(boolean secretCreated, String dateSecretCreated, String dateSecretChanged) {
        this.secretCreated = secretCreated;
        this.dateSecretChanged = dateSecretChanged;
        this.dateSecretCreated = dateSecretCreated;
    }

    public VaultSecretStatus(){
    }
}
