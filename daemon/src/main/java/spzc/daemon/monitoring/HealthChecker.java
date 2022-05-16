package spzc.daemon.monitoring;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import spzc.daemon.monitoring.ServiceInstancePropertiesList.HealthCheckProperties;

@Slf4j
@Service
public class HealthChecker {
  private static final int OK_HTTP_STATUS = 200;
  private static final int EXECUTOR_THREAD_COUNT = 5;
  private final HttpClient httpClient;
  @Getter
  private final List<ServiceInstance> serviceInstances;
  private final HealthCheckProperties healthCheckProperties;

  public HealthChecker(ServiceInstancePropertiesList serviceInstanceProperties) {
    this.serviceInstances = serviceInstanceProperties.getInstances().stream()
        .map(instanceProperties -> new ServiceInstance(instanceProperties, true, true, true))
        .collect(Collectors.toUnmodifiableList());
    this.healthCheckProperties = serviceInstanceProperties.getHealthCheck();
    this.httpClient = HttpClient.newHttpClient();
  }

  public void scheduleHealthStatusUpdate() {
    var executorService = Executors.newScheduledThreadPool(EXECUTOR_THREAD_COUNT);
    executorService.scheduleAtFixedRate(this::checkHealth, 0, healthCheckProperties.getRate(), TimeUnit.SECONDS);
  }

  public void checkHealth() {
    serviceInstances.forEach(this::updateInstanceHealthStatus);
  }

  private void updateInstanceHealthStatus(ServiceInstance instance) {
    var httpRequest = HttpRequest.newBuilder()
        .GET()
        .uri(URI.create(instance.getProperties().getOs() + healthCheckProperties.getPath()))
        .build();

    var httpResponse = getHealthStatus(httpRequest);
    httpResponse.ifPresent(stringHttpResponse -> instance.setUp(stringHttpResponse.statusCode() == OK_HTTP_STATUS));
  }

  private Optional<HttpResponse<String>> getHealthStatus(HttpRequest request) {
    try {
      return Optional.of(httpClient.send(request, BodyHandlers.ofString()));
    } catch (IOException | InterruptedException exception) {
      log.warn("Exception caught while getting health status at {}", request.uri(), exception);
      return Optional.empty();
    }
  }
}
