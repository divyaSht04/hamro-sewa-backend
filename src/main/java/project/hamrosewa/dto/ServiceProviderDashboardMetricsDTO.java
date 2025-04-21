package project.hamrosewa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceProviderDashboardMetricsDTO {
    private int totalServices;
    private double totalRevenue;
    private double averageRating;
    private int percentChangeServices;
    private int percentChangeRevenue;
    private int percentChangeRating;
}
