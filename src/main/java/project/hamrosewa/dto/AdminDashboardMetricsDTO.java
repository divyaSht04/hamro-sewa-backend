package project.hamrosewa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardMetricsDTO {
    private int totalUsers;
    private int totalServiceProviders;
    private double totalRevenue;
    private double averageRating;
}
