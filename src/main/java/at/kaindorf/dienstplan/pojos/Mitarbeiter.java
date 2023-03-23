package at.kaindorf.dienstplan.pojos;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Mitarbeiter {
    private String firstname;
    private String lastname;
    private double workingNumbers;
    private double totalWorkingNumbers;
    private String workRole;
    private List<String> calenderDays;
}
