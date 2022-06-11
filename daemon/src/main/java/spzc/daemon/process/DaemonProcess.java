package spzc.daemon.process;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import spzc.daemon.domain.RotationProperties;
import spzc.daemon.domain.ServiceInstance;
import spzc.daemon.service.InstanceHealthTracker;
import spzc.daemon.service.IpTablesService;

@Slf4j
@Service
@RequiredArgsConstructor
public class DaemonProcess {

  private static final String ROTATION_LOG_FILE_NAME = "rotations.log";
  private static final int MILIS_TO_SECONDS_RATIO = 1000;
  private final Timer timer = new Timer();
  private final IpTablesService ipTablesService;
  private final InstanceHealthTracker instanceHealthTracker;
  private final RotationProperties rotationProperties;
  private final Random randomGenerator = new Random();
  private String previousInstanceIp = "";

  public void run() {
    instanceHealthTracker.scheduleHealthStatusUpdate();
    new Rotation().run();
  }

  class Rotation extends TimerTask {

    @Override
    public void run() {
      int delayInMilis = (rotationProperties.getMinRate() + randomGenerator.nextInt(
          rotationProperties.getMaxRate() - rotationProperties.getMinRate())) * MILIS_TO_SECONDS_RATIO;
      log.info("Scheduling next OS rotation in {} seconds", delayInMilis / 1000);
      timer.schedule(new Rotation(), delayInMilis);
      rotate();
    }

    private void rotate() {
      var randomlySelectedInstance = randomlySelectOtherInstance();
      var newIp = randomlySelectedInstance.getProperties().getIp();
      if (ipTablesService.setRoutingTo(newIp)) {
        randomlySelectedInstance.setLive(true);
        log.info("Rotated to {}", randomlySelectedInstance.getProperties().getIp());
        appendLogFile(newIp, randomlySelectedInstance.getProperties().getOs());
      }
    }

    private void appendLogFile(String ip, String os) {
      var timeStamp = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
      var file = new File(ROTATION_LOG_FILE_NAME);
      try {
        FileUtils.writeStringToFile(file, String.format("%s %s\n", timeStamp, ip), StandardCharsets.UTF_8, true);
      } catch (IOException e) {
        log.warn("Could not append log file.");
      }

    }

    private ServiceInstance randomlySelectOtherInstance() {
      var otherInstance = randomlySelectInstance();

      while (otherInstance.getProperties().getIp().equals(previousInstanceIp)) {
        otherInstance = randomlySelectInstance();
      }

      previousInstanceIp = otherInstance.getProperties().getIp();
      return otherInstance;
    }

    private ServiceInstance randomlySelectInstance() {
      var healthyInstances = instanceHealthTracker.getHealthyInstances();
      var instancesCount = healthyInstances.size();
      var randomInstanceIndex = randomGenerator.nextInt(instancesCount);
      return healthyInstances.get(randomInstanceIndex);
    }
  }
}