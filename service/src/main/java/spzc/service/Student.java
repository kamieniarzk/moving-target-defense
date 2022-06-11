package spzc.service;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Student {
  private Integer id;
  private String name;
  private String surname;
  private String indexNumber;
  private List<Course> courses;
}
