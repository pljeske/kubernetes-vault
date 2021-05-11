package de.init.commons.kubernetes.vault.watcher;

import de.init.commons.kubernetes.vault.rsa.RSAEncryption;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LicenseWatcher implements Watcher<Secret> {
  public static final String RSA_ANNOTATION = "encrypted";
  private final RSAEncryption encryption;
  private final KubernetesClient client;

  @Autowired
  public LicenseWatcher(KubernetesClient client, RSAEncryption encryption) {
    this.encryption = encryption;
    this.client = client;
  }

  @Override
  public void eventReceived(Action action, Secret secret) {
//    Map<String,String> annotations = secret.getMetadata().getAnnotations();
//    Map<String,String> data = secret.getData();
//    if (annotations.containsKey(RSA_ANNOTATION)) {
//      if ("true".equals(annotations.get(RSA_ANNOTATION))) {
//        for (String key : data.keySet()) {
//          try {
//            String decryptedText = encryption.decrypt(data.get(key));
//          } catch (IllegalBlockSizeException e) {
//            e.printStackTrace();
//          } catch (InvalidKeyException e) {
//            e.printStackTrace();
//          } catch (BadPaddingException e) {
//            e.printStackTrace();
//          } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//          } catch (NoSuchPaddingException e) {
//            e.printStackTrace();
//          }
////          client.secrets().inNamespace(secret.getMetadata().getNamespace()).withName(secret.getMetadata().getName()).edit().
//        }
////        secret.getData().forEach((key, value) -> );
//      }
//    }
  }

  @Override
  public void onClose(WatcherException e) {

  }
}
