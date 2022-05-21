package spzc.daemon.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import spzc.daemon.domain.ServiceInstancePropertiesList.ServiceInstanceProperties;

@Data
@AllArgsConstructor
@Builder
public class ServiceInstance {
  private ServiceInstanceProperties properties;
  private Boolean up;
  private Boolean live;
  private Boolean safe;
}
