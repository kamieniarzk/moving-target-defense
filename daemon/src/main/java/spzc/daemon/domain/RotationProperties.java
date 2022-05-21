package spzc.daemon.domain;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "rotation")
public class RotationProperties {
  private Integer minRate;
  private Integer maxRate;
}