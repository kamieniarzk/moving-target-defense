package spzc.service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import java.util.Optional;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TestController {

  private final TestRepository testRepository;

  @GetMapping("/students/long")
  public List<Student> getAllStudentsLong() throws InterruptedException {
    Thread.sleep(1000);
    return testRepository.getStudents();
  }

  @GetMapping("/students/{id}")
  public Optional<Student> getStudentById(@PathVariable Integer id) {
    return testRepository.getStudentById(id);
  }

  @GetMapping("/courses")
  public List<Course> getAllCourses() {
    return testRepository.getCourses();
  }

  @GetMapping("/courses/long")
  public List<Course> getAllCoursesLong() throws InterruptedException {
    Thread.sleep(1000);
    return testRepository.getCourses();
  }

  @GetMapping("/courses/{id}")
  public Optional<Course> getCourseById(@PathVariable Integer id) {
    return testRepository.getCourseById(id);
  }

  @GetMapping("/courses/{code}")
  public Optional<Course> getCourseById(@PathVariable String code) {
    return testRepository.getCourseByCode(code);
  }

  @GetMapping("/system/info")
  public String getSystemProperties() {
    var properties = System.getProperties();
    var osName = properties.get("os.name");
    var osVersion = properties.get("os.version");
    var osArch = properties.get("os.arch");
    return osName + " " + osVersion + " " + osArch;
  }

  @GetMapping("/system/ip")
  public String getIp() {
    try (Socket socket = new Socket()) {
      socket.connect(new InetSocketAddress("google.com", 80));
      return socket.getLocalAddress().getHostAddress();
    } catch (IOException e) {
      return "127.0.0.1";
    }
  }
}
