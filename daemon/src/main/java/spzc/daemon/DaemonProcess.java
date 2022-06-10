package spzc.daemon;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

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
  private static final int MILIS_TO_SECONDS_RATIO = 1000;
  private final Timer timer = new Timer();
  private final IpTablesService ipTablesService;
  private final InstanceHealthTracker instanceHealthTracker;
  private final RotationProperties rotationProperties;

  public void run() {
    instanceHealthTracker.scheduleHealthStatusUpdate();
    new Rotation().run();
  }

  class Rotation extends TimerTask {
    private final Random randomGenerator = new Random();
    private String previousInstanceIp = "";

    @Override
    public void run() {
      int delayInMilis = (rotationProperties.getMinRate() + randomGenerator.nextInt(rotationProperties.getMaxRate())) * MILIS_TO_SECONDS_RATIO;
      log.info("Scheduling next OS rotation in {} seconds", delayInMilis / 1000);
      timer.schedule(new Rotation(), delayInMilis);
      rotate();
    }

    private void rotate() {
      var randomlySelectedInstance = ranndomlySelectOtherInstance();
      log.info("rotate to {}", randomlySelectedInstance);
      if (ipTablesService.setRoutingTo(randomlySelectedInstance.getProperties().getIp())) {
        randomlySelectedInstance.setLive(true);
      }
    }

    private ServiceInstance ranndomlySelectOtherInstance() {
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