package de.init.commons.kubernetes.vault.controller;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.vault.core.VaultKeyValueOperationsSupport;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.websocket.server.PathParam;

@RefreshScope
@RestController
public class SecretController implements ApplicationContextAware {
    private static ApplicationContext context;

    @Autowired
    private VaultTemplate vaultTemplate;

    @RequestMapping("/api/secret")
    public Object secret(@PathParam(value="path") String path) {
        String[] splitPath = path.split("/");
        String backend = splitPath[0];
        String app = splitPath[1];
        String key = splitPath[2];

        //        VaultResponse response = vaultTemplate
        //                .opsForKeyValue("secret", VaultKeyValueOperationsSupport.KeyValueBackend.KV_2).get("github");
        VaultResponse response = vaultTemplate
                .opsForKeyValue(backend, VaultKeyValueOperationsSupport.KeyValueBackend.KV_2).get(app);

        Object data = response.getData().get(key);

        System.out.println(data);
        return data;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }
}