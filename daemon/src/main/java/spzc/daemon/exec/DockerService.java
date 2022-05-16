package spzc.daemon.exec;

import java.io.ByteArrayOutputStream;

import org.springframework.stereotype.Service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;

import lombok.extern.slf4j.Slf4j;
import spzc.daemon.monitoring.ServiceInstance;

@Slf4j
@Service
public class DockerService {
  private static final String NGINX_CONFIG_LOCATION = "/etc/nginx/";

  private final DockerClient dockerClient;

  public DockerService() {
    var dockerClientConfig = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
    var dockerHttpClient = new ApacheDockerHttpClient.Builder()
        .dockerHost(dockerClientConfig.getDockerHost())
        .sslConfig(dockerClientConfig.getSSLConfig())
        .build();
    this.dockerClient = DockerClientImpl.getInstance(dockerClientConfig, dockerHttpClient);
  }

  public boolean setLiveInstance(ServiceInstance serviceInstance) {
    var filePath = NGINX_CONFIG_LOCATION + serviceInstance.getProperties().getConfigFileName();
    var command = "nginx -c " + filePath;
    var execCreateCmdResponse = getExecCreateCmdResponse(serviceInstance.getProperties().getContainerName(), command);
    return executeCommand(execCreateCmdResponse);
  }

  private boolean executeCommand(ExecCreateCmdResponse execCreateCmdResponse) {
    var stdOut = new ByteArrayOutputStream();
    var stdErr = new ByteArrayOutputStream();
    try {
      dockerClient
          .execStartCmd(execCreateCmdResponse.getId())
          .withTty(true)
          .exec(new ExecStartResultCallback(stdOut, stdErr))
          .awaitCompletion();
      return stdErr.size() == 0;
    } catch (Exception exception) {
      log.warn("Exception occurred while trying to reload nginx config.", exception);
      return false;
    } finally {
      log.info(stdOut.toString());
      log.warn(stdErr.toString());
    }
  }

  private ExecCreateCmdResponse getExecCreateCmdResponse(String containerId, String command) {
    return dockerClient.execCreateCmd(containerId)
        .withAttachStdout(true)
        .withAttachStderr(true)
        .withCmd(command.split(" "))
        .exec();
  }
}