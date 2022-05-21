package spzc.daemon.service;

import java.io.File;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import spzc.daemon.domain.ServiceInstance;

@Slf4j
@Service
public class ConfigFileService {
  private static final String NGINX_CONFIG_FILE_LOCATION = "../nginx/conf/default.conf";

  public void overrideNginxConfigFile(ServiceInstance serviceInstance) {
    var fileName = serviceInstance.getProperties().getConfigFileName();
    var fileContent = getFileFromResourcesAsString(fileName);
    var configFile = new File(NGINX_CONFIG_FILE_LOCATION);
    try {
      FileUtils.write(configFile, fileContent, Charset.defaultCharset());
      log.info("Updated nginx config to [{}]", fileName);
    } catch (Exception e) {
      log.warn("Failed to update nginx config to [{}]", fileName, e);
    }
  }

  private String getFileFromResourcesAsString(String fileName) {
    try (var resourceBytes = ConfigFileService.class.getResourceAsStream("/" + fileName)) {
      log.info("Reading [{}] content into memory", fileName);
      return new String(resourceBytes.readAllBytes());
    } catch (Exception e) {
      log.warn("Error while reading file {}", fileName, e);
      throw new IllegalStateException("Could not read [{}] into memory");
    }
  }
}
