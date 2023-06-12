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
    private int assignedHours;
    private String position;
    private List<String> calenderDays;

    public int getRemainingHours() {
        return 40 - assignedHours;
    }
    public void addAssignedHours(int hours) {
        this.assignedHours += hours;
    }

    public void resetAssignedHours() {
        this.assignedHours = 0;
    }
}
