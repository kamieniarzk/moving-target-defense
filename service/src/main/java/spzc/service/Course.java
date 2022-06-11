package spzc.service;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Course {
  private Integer id;
  private String code;
  private String name;
  private Integer ects;
  private String institute;
}
