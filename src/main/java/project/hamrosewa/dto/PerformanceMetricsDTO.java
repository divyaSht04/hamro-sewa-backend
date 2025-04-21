package project.hamrosewa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceMetricsDTO {
    private int totalClients;
    private int completedJobs;
    private double totalEarnings;
    private int responseRate;
}
