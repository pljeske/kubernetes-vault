package de.init.commons.kubernetes.vault.config;

import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KubernetesConfig {
  @Value("${kubernetes.api.url:}")
  private String kubernetesApiUrl;

  @Bean
  public KubernetesClient getKubernetesClient() {
    if (kubernetesApiUrl.equals("")) {
      return new DefaultKubernetesClient();
    } else {
      return new DefaultKubernetesClient(kubernetesApiUrl);
    }
  }
}
