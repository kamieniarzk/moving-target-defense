package spzc.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class ServiceApplication {
  // TODO można by ewentualnie coś dodać w tym serwisie żeby nie był pustą apką ale nie wiem czy to istotne
  public static void main(String[] args) {
    SpringApplication.run(ServiceApplication.class, args);
  }

  @GetMapping("/system/info")
  public String getSystemProperties() {
    var properties = System.getProperties();
    var osName = properties.get("os.name");
    var osVersion = properties.get("os.version");
    var osArch = properties.get("os.arch");
    return osName + " " + osVersion + " " + osArch;
  }
}
