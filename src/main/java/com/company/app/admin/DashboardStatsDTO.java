package com.company.app.admin;

import com.company.app.enquiry.EnquiryResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    private long totalProducts;
    private long publishedProducts;
    private long machineryProducts;
    private long interiorProducts;
    private long totalEnquiries;
    private long newEnquiries;
    private long contactedEnquiries;
    private long closedEnquiries;
    private long machineryEnquiries;
    private long interiorEnquiries;
    private long totalOrders;
    private BigDecimal totalRevenue;
    private List<EnquiryResponseDTO> recentEnquiries;
    private Map<String, Long> leadsByStatus;
}
