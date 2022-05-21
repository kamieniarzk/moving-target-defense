package spzc.daemon.service;

import java.io.ByteArrayOutputStream;

import org.springframework.stereotype.Service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DockerService {
  private static final String NGINX_CONTAINER_NAME = "nginx"; // todo może dodać do enva?
  private static final String NGINX_RELOAD_COMMAND = "nginx -s reload";
  private static final String COMMAND_SEPARATOR = " ";

  private final DockerClient dockerClient;

  public DockerService() {
    var dockerClientConfig = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
    var dockerHttpClient = new ApacheDockerHttpClient.Builder()
        .dockerHost(dockerClientConfig.getDockerHost())
        .sslConfig(dockerClientConfig.getSSLConfig())
        .build();
    this.dockerClient = DockerClientImpl.getInstance(dockerClientConfig, dockerHttpClient);
  }

  /**
   *
   * @return true if reloaded successfully, false otherwise
   */
  public boolean reloadNginx() {
    return executeCommand(getNginxReloadCommand());
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
      if (stdOut.size() != 0 || stdErr.size() != 0) {
        log.info(stdOut.toString());
        log.warn(stdErr.toString());
      }
    }
  }

  private ExecCreateCmdResponse getNginxReloadCommand() {
    return dockerClient.execCreateCmd(NGINX_CONTAINER_NAME)
        .withAttachStdout(true)
        .withAttachStderr(true)
        .withCmd(DockerService.NGINX_RELOAD_COMMAND.split(COMMAND_SEPARATOR))
        .exec();
  }
}