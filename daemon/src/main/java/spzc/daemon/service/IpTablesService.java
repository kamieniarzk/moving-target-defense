package spzc.daemon.service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class IpTablesService {

  private static final String DNAT_COMMAND =
      "iptables --table nat --append PREROUTING --protocol ALL --destination ${PUBLIC_IP} --jump DNAT --to-destination ${SERVICE_IP}";
  private static final String SNAT_COMMAND =
      "iptables --table nat --append POSTROUTING --protocol ALL --destination ${SERVICE_IP} --jump SNAT --to-source ${PUBLIC_IP}";
  private static final String FLUSH_NAT_COMMAND = "iptables --table nat --flush";

  private final CommandExecutor commandExecutor;
  private String publicIp;


  @PostConstruct
  public void setPublicIp() {
    try (Socket socket = new Socket()) {
      socket.connect(new InetSocketAddress("google.com", 80));
      publicIp = socket.getLocalAddress().getHostAddress();
    } catch (IOException e) {
      publicIp = "192.168.100.248";
    }
  }

  public boolean setRoutingTo(String serviceIp) {
    flushNatRules();
    try {
      var dnatCommand = commandExecutor.executeCommand(getDnatCommand(serviceIp));
      var snatCommand = commandExecutor.executeCommand(getSnatCommand(serviceIp));
      return dnatCommand && snatCommand;
    } catch (IOException | InterruptedException e) {
      log.info("Exception caught while changing routing to {}", serviceIp);
      flushNatRules();
      return false;
    }
  }

  private void flushNatRules() {
    try {
      commandExecutor.executeCommand(FLUSH_NAT_COMMAND);
    } catch (IOException | InterruptedException e) {
      log.warn("Failed to flush NAT table rules.");
    }
  }

  private String getSnatCommand(String serviceIp) {
    return SNAT_COMMAND
        .replace("${PUBLIC_IP}", publicIp)
        .replace("${SERVICE_IP}", serviceIp);
  }

  private String getDnatCommand(String serviceIp) {
    return DNAT_COMMAND
        .replace("${PUBLIC_IP}", publicIp)
        .replace("${SERVICE_IP}", serviceIp);
  }
}

