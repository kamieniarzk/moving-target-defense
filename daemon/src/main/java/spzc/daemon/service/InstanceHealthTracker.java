package spzc.daemon.service;

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

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import spzc.daemon.domain.ServiceInstance;
import spzc.daemon.domain.ServiceInstancePropertiesList;
import spzc.daemon.domain.ServiceInstancePropertiesList.HealthCheckProperties;
import spzc.daemon.domain.ServiceInstancePropertiesList.ServiceInstanceProperties;

@Slf4j
@Service
public class InstanceHealthTracker {
  private static final int OK_HTTP_STATUS = 200;
  private static final int INSTANCE_DOWN_STATUS = -1;
  private static final int EXECUTOR_THREAD_COUNT = 5;
  private final HttpClient httpClient;
  @Getter
  private final List<ServiceInstance> serviceInstances;
  private final HealthCheckProperties healthCheckProperties;

  public InstanceHealthTracker(ServiceInstancePropertiesList serviceInstanceProperties) {
    this.serviceInstances = serviceInstanceProperties.getInstances().stream()
        .map(instanceProperties -> new ServiceInstance(instanceProperties, true, true, true))
        .collect(Collectors.toUnmodifiableList());
    this.healthCheckProperties = serviceInstanceProperties.getHealthCheck();
    this.httpClient = HttpClient.newHttpClient();
  }

  public void scheduleHealthStatusUpdate() {
    var threadFactory = new ThreadFactoryBuilder().setNameFormat("health-service%d").build();
    var executorService = Executors.newScheduledThreadPool(EXECUTOR_THREAD_COUNT, threadFactory);
    executorService.scheduleAtFixedRate(this::checkHealth, 0, healthCheckProperties.getRate(), TimeUnit.SECONDS);
  }

  public void checkHealth() {
    log.debug("Scheduled health check in progress.");
    serviceInstances.forEach(this::updateInstanceHealthStatus);
    logLiveInstances();
  }

  public List<ServiceInstance> getHealthyInstances() {
    return getServiceInstances().stream()
        .filter(ServiceInstance::getUp)
        .collect(Collectors.toUnmodifiableList());
  }

  private void updateInstanceHealthStatus(ServiceInstance instance) {
    var httpRequest = getHealthcheckHttpRequest(instance);
    var responseStatus = getHealthStatus(httpRequest)
        .map(HttpResponse::statusCode)
        .orElseGet(() -> {
          log.warn("Service at [{}], os [{}] is down!", instance.getProperties().getIp(), instance.getProperties().getOs());
          return INSTANCE_DOWN_STATUS;
        });

    instance.setUp(responseStatus == OK_HTTP_STATUS);
  }

  private HttpRequest getHealthcheckHttpRequest(ServiceInstance instance) {
    var healthcheckUrl = "http://" + instance.getProperties().getIp() + ":" + healthCheckProperties.getPort()  + healthCheckProperties.getPath();
    return HttpRequest.newBuilder()
        .GET()
        .uri(URI.create(healthcheckUrl))
        .build();
  }

  private void logLiveInstances() {
    var upInstanceIps = getServiceInstances().stream().map(ServiceInstance::getProperties).map(ServiceInstanceProperties::getIp).collect(Collectors.toUnmodifiableList());
    var upInstanceIpsString = new StringBuilder();
    for (String instance : upInstanceIps) {
      upInstanceIpsString.append("\n- ").append(instance);
    }
    log.debug("Live instances are: {}", upInstanceIpsString);
  }

  private Optional<HttpResponse<String>> getHealthStatus(HttpRequest request) {
    try {
      return Optional.of(httpClient.send(request, BodyHandlers.ofString()));
    } catch (IOException | InterruptedException exception) {
      log.warn("Exception caught while getting health status at {}", request.uri());
      return Optional.empty();
    }
  }
}
