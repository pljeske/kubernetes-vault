package de.init.commons.kubernetes.vault.config;

import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KubernetesConfig {
  @Bean
  public KubernetesClient getKubernetesClient() {
    return new DefaultKubernetesClient();
  }
}
