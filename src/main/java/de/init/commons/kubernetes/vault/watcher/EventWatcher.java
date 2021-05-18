package de.init.commons.kubernetes.vault.watcher;

import io.fabric8.kubernetes.api.model.HasMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface EventWatcher<T extends HasMetadata> {
  Logger LOG = LoggerFactory.getLogger(EventWatcher.class);
  void added(T resource);
  void modified(T resource);
  void deleted(T resource);
  default void error(T resource) {
    LOG.error("ERROR: {}", resource.getMetadata().getName());
  }
}
