package spzc.daemon;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import spzc.daemon.service.IpTablesService;

@Slf4j
@RequiredArgsConstructor
@SpringBootApplication
public class DaemonApplication implements CommandLineRunner {
  private final DaemonProcess daemonProcess;
  private final IpTablesService ipTablesService;

  public static void main(String[] args) {
    SpringApplication.run(DaemonApplication.class, args);
  }

  @Override
  public void run(final String... args) throws Exception {
//    ipTablesService.setRoutingTo("213.123.21.3");
    daemonProcess.run();
    Thread.currentThread().join();
  }
}
