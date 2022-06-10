package spzc.daemon.domain;

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
    private String ip;
    private String os;
  }

  @Data
  public static class HealthCheckProperties {
    private String path;
    private Integer rate;
    private Integer port;
  }
}