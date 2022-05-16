package spzc.daemon.monitoring;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "service")
public class ServiceInstancePropertiesList {

  private HealthCheckProperties healthCheck;
  private List<ServiceInstanceProperties> instances;

  @Data
  public static class ServiceInstanceProperties {
    private String url;
    private String os;
    private String configFileName;
    private String containerName;
  }

  @Data
  public static class HealthCheckProperties {
    private String path;
    private Integer rate;
  }
}