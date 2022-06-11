package spzc.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Repository;

import lombok.Getter;

@Repository
public class TestRepository {
  @Getter
  private final List<Student> students = new ArrayList<>();
  @Getter
  private final List<Course> courses = new ArrayList<>();

  public Optional<Student> getStudentById(Integer id) {
    return students.stream()
        .filter(student -> id.equals(student.getId()))
        .findFirst();
  }

  public Optional<Course> getCourseById(Integer id) {
    return courses.stream()
        .filter(course -> id.equals(course.getId()))
        .findFirst();
  }

  public Optional<Course> getCourseByCode(String code) {
    return courses.stream()
        .filter(course -> code.equals(course.getCode()))
        .findFirst();
  }

  @PostConstruct
  public void initializeData() {
    var spzc = new Course(1, "SPZC", "Systemy i protokoły zabezpieczeń w cyberprzestrzeni", 4, "II");
    var aso = new Course(2, "ASO", "Analiza semantyczna obrazu", 4, "IMiO");
    var pobr = new Course(3, "POBR", "Przetwarzanie cyfrowe obrazów", 4, "II");
    var stup = new Course(4, "STUP", "Przedsiębiorczość startupowa", 2, "Wydział Zarządzania");
    courses.addAll(List.of(spzc, aso, pobr, stup));

    var kacper = new Student(1, "Kacper", "Kamieniarz", "293065", courses);
    var janek = new Student(2, "Jan", "Radzimiński", "293052", courses);
    students.addAll(List.of(kacper, janek));
  }
}
