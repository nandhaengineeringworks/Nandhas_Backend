package com.company.app.admin;

import com.company.app.catalogue.entity.CategoryType;
import com.company.app.catalogue.entity.ProductStatus;
import com.company.app.catalogue.repository.ProductRepository;
import com.company.app.enquiry.Enquiry;
import com.company.app.enquiry.EnquiryRepository;
import com.company.app.enquiry.EnquiryResponseDTO;
import com.company.app.enquiry.EnquiryStatus;
import com.company.app.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final ProductRepository productRepository;
    private final EnquiryRepository enquiryRepository;
    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public DashboardStatsDTO getDashboardStats() {
        long totalProducts = productRepository.count();
        long publishedProducts = productRepository.countByStatus(ProductStatus.PUBLISHED);
        long machineryProducts = productRepository.countByCategoryType(CategoryType.MACHINERY);
        long interiorProducts = productRepository.countByCategoryType(CategoryType.INTERIOR);

        long totalEnquiries = enquiryRepository.count();
        long newEnquiries = enquiryRepository.countByStatus(EnquiryStatus.NEW);
        long contactedEnquiries = enquiryRepository.countByStatus(EnquiryStatus.CONTACTED);
        long closedEnquiries = enquiryRepository.countByStatus(EnquiryStatus.CLOSED);
        long machineryEnquiries = enquiryRepository.countByProductType(CategoryType.MACHINERY);
        long interiorEnquiries = enquiryRepository.countByProductType(CategoryType.INTERIOR);

        long totalOrders = orderRepository.count();
        BigDecimal totalRevenue = orderRepository.calculateTotalRevenue();
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        List<Enquiry> recentList = enquiryRepository.findTop10ByOrderByCreatedAtDesc();
        List<EnquiryResponseDTO> recentDtos = recentList.stream()
                .map(EnquiryResponseDTO::fromEntity)
                .collect(Collectors.toList());

        Map<String, Long> leadsByStatus = new HashMap<>();
        for (EnquiryStatus status : EnquiryStatus.values()) {
            leadsByStatus.put(status.name(), enquiryRepository.countByStatus(status));
        }

        return DashboardStatsDTO.builder()
                .totalProducts(totalProducts)
                .publishedProducts(publishedProducts)
                .machineryProducts(machineryProducts)
                .interiorProducts(interiorProducts)
                .totalEnquiries(totalEnquiries)
                .newEnquiries(newEnquiries)
                .contactedEnquiries(contactedEnquiries)
                .closedEnquiries(closedEnquiries)
                .machineryEnquiries(machineryEnquiries)
                .interiorEnquiries(interiorEnquiries)
                .totalOrders(totalOrders)
                .totalRevenue(totalRevenue)
                .recentEnquiries(recentDtos)
                .leadsByStatus(leadsByStatus)
                .build();
    }
}
