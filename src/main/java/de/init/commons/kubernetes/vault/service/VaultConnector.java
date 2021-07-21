package de.init.commons.kubernetes.vault.service;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.response.LogicalResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class VaultConnector {
    private final Vault vault;

    public VaultConnector(@Value("${vault.address}") String vaultAddress,
                          @Value("${vault.root-token}") String vaultToken) throws VaultException {
        VaultConfig vaultConfig = new VaultConfig()
                .address(vaultAddress)
                .token(vaultToken)
                .build();
        vault = new Vault(vaultConfig);
    }

    public Map<String, String> getCredentials(String path) throws VaultException {
        LogicalResponse response = vault.logical().read(path);
        return response.getData();
    }
}
