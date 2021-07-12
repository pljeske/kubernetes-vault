package de.init.commons.kubernetes.vault.crd;

import lombok.Data;

/**
 * @author Peer-Lucas Jeske
 * created 09.07.2021
 */
@Data
public class VaultSecretSpec {
    private String[] passwords;
}
