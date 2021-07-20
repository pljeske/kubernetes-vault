package de.init.commons.kubernetes.vault.vault;

import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultKeyValueOperationsSupport;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import javax.inject.Inject;
import java.util.Map;

@Service
public class VaultConnector {
    private VaultTemplate vaultTemplate;

    @Inject
    public VaultConnector(VaultTemplate vaultTemplate) {
        this.vaultTemplate = vaultTemplate;
    }

    public String getPassword(String path, String app, String key) {
//        VaultResponse response = vaultTemplate
//                .opsForKeyValue("secret", VaultKeyValueOperationsSupport.KeyValueBackend.KV_2).get("github");
//        String password = response.getData().get("github.oauth2.key");

        VaultResponse response = vaultTemplate.opsForKeyValue(path,
                VaultKeyValueOperationsSupport.KeyValueBackend.KV_2).get(app);
        Map<String,Object> responseData = response.getData();

//        if (value.getClass().isAssignableFrom(String.class)) {
//            return (String) value;
//        }
        return null;
    }
}
