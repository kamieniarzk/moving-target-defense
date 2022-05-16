package spzc.daemon.monitoring;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import spzc.daemon.monitoring.ServiceInstancePropertiesList.ServiceInstanceProperties;

@Data
@AllArgsConstructor
@Builder
public class ServiceInstance {
  private ServiceInstanceProperties properties;
  private Boolean up;
  private Boolean live;
  private Boolean safe;
}
