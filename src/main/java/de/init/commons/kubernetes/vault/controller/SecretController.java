package de.init.commons.kubernetes.vault.controller;

import com.bettercloud.vault.VaultException;
import de.init.commons.kubernetes.vault.vault.VaultConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.websocket.server.PathParam;
import java.util.Collections;
import java.util.Map;

//@RefreshScope
@RestController
public class SecretController {
    private static final Logger LOG = LoggerFactory.getLogger(SecretController.class);
    @Inject
    private VaultConnector vault;

    @GetMapping("/api/secret")
    public Map<String,String> getCredentials(@PathParam(value="path") String path) {
        try {
            return vault.getCredentials(path);
        } catch (VaultException e) {
            LOG.error("Something went wrong", e);
            return Collections.emptyMap();
        }
    }
//public class SecretController implements ApplicationContextAware {
//    private static ApplicationContext context;
//
//    @Autowired
//    private VaultTemplate vaultTemplate;
//
//    @RequestMapping("/api/secret")
//    public Object secret(@PathParam(value="path") String path) {
//        String[] splitPath = path.split("/");
//        String backend = splitPath[0];
//        String app = splitPath[1];
//        String key = splitPath[2];
//
//        //        VaultResponse response = vaultTemplate
//        //                .opsForKeyValue("secret", VaultKeyValueOperationsSupport.KeyValueBackend.KV_2).get("github");
//        VaultResponse response = vaultTemplate
//                .opsForKeyValue(backend, VaultKeyValueOperationsSupport.KeyValueBackend.KV_2).get(app);
//
//        Object data = response.getData().get(key);
//
//        System.out.println(data);
//        return data;
//    }
//
//    @Override
//    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//        context = applicationContext;
//    }
}