package de.init.commons.kubernetes.vault.crd;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Peer-Lucas Jeske
 * created 09.07.2021
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VaultSecretSpec {
    private String secretReference;
}
