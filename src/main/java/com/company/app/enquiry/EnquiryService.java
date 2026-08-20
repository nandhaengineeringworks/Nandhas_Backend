package com.company.app.enquiry;

import com.company.app.catalogue.entity.CategoryType;
import com.company.app.catalogue.entity.Product;
import com.company.app.catalogue.repository.ProductRepository;
import com.company.app.common.PagedResponse;
import com.company.app.common.ResourceNotFoundException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnquiryService {

    private final EnquiryRepository enquiryRepository;
    private final ProductRepository productRepository;
    private final EmailNotificationService emailNotificationService;

    @Transactional
    public EnquiryResponseDTO submitEnquiry(EnquiryRequestDTO dto) {
        Product product = null;
        String productName = dto.getProductName();
        CategoryType productType = dto.getProductType();

        if (dto.getProductId() != null) {
            product = productRepository.findById(dto.getProductId()).orElse(null);
            if (product != null) {
                if (!StringUtils.hasText(productName)) {
                    productName = product.getName();
                }
                if (productType == null && product.getCategory() != null) {
                    productType = product.getCategory().getType();
                }
            }
        }

        Enquiry enquiry = Enquiry.builder()
                .product(product)
                .productName(productName)
                .productSku(dto.getProductSku())
                .productType(productType)
                .name(dto.getName())
                .phone(dto.getPhone())
                .email(dto.getEmail())
                .companyName(dto.getCompanyName())
                .city(dto.getCity())
                .state(dto.getState())
                .estimatedQuantity(dto.getEstimatedQuantity())
                .message(dto.getMessage())
                .status(EnquiryStatus.NEW)
                .build();

        Enquiry saved = enquiryRepository.save(enquiry);
        emailNotificationService.sendNewEnquiryNotification(saved);

        return EnquiryResponseDTO.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<EnquiryResponseDTO> getEnquiries(
            int page, int size, EnquiryStatus status, CategoryType productType, String search
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Specification<Enquiry> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (productType != null) {
                predicates.add(cb.equal(root.get("productType"), productType));
            }

            if (StringUtils.hasText(search)) {
                String like = "%" + search.toLowerCase() + "%";
                Predicate nameMatch = cb.like(cb.lower(root.get("name")), like);
                Predicate phoneMatch = cb.like(cb.lower(root.get("phone")), like);
                Predicate emailMatch = cb.like(cb.lower(root.get("email")), like);
                Predicate compMatch = cb.like(cb.lower(root.get("companyName")), like);
                Predicate prodMatch = cb.like(cb.lower(root.get("productName")), like);
                predicates.add(cb.or(nameMatch, phoneMatch, emailMatch, compMatch, prodMatch));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Enquiry> enquiryPage = enquiryRepository.findAll(spec, pageable);
        List<EnquiryResponseDTO> dtos = enquiryPage.getContent().stream()
                .map(EnquiryResponseDTO::fromEntity)
                .collect(Collectors.toList());

        return PagedResponse.from(enquiryPage, dtos);
    }

    @Transactional(readOnly = true)
    public EnquiryResponseDTO getEnquiryById(Long id) {
        Enquiry enquiry = enquiryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enquiry", "id", id));
        return EnquiryResponseDTO.fromEntity(enquiry);
    }

    @Transactional
    public EnquiryResponseDTO updateEnquiryStatus(Long id, EnquiryStatusUpdateDTO dto) {
        Enquiry enquiry = enquiryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enquiry", "id", id));

        enquiry.setStatus(dto.getStatus());
        if (StringUtils.hasText(dto.getInternalNotes())) {
            enquiry.setInternalNotes(dto.getInternalNotes());
        }
        if (StringUtils.hasText(dto.getAssignedTo())) {
            enquiry.setAssignedTo(dto.getAssignedTo());
        }

        Enquiry updated = enquiryRepository.save(enquiry);
        return EnquiryResponseDTO.fromEntity(updated);
    }

    @Transactional(readOnly = true)
    public String exportToCsv() {
        List<Enquiry> list = enquiryRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        StringBuilder sb = new StringBuilder();
        sb.append("ID,Name,Phone,Email,Company,Product Name,Type,Estimated Qty,City,State,Status,Date\n");

        for (Enquiry e : list) {
            sb.append(e.getId()).append(",")
                    .append(escapeCsv(e.getName())).append(",")
                    .append(escapeCsv(e.getPhone())).append(",")
                    .append(escapeCsv(e.getEmail())).append(",")
                    .append(escapeCsv(e.getCompanyName())).append(",")
                    .append(escapeCsv(e.getProductName())).append(",")
                    .append(e.getProductType() != null ? e.getProductType().name() : "").append(",")
                    .append(e.getEstimatedQuantity() != null ? e.getEstimatedQuantity() : "").append(",")
                    .append(escapeCsv(e.getCity())).append(",")
                    .append(escapeCsv(e.getState())).append(",")
                    .append(e.getStatus().name()).append(",")
                    .append(e.getCreatedAt()).append("\n");
        }
        return sb.toString();
    }

    private String escapeCsv(String val) {
        if (val == null) return "";
        String clean = val.replace("\"", "\"\"");
        if (clean.contains(",") || clean.contains("\n") || clean.contains("\"")) {
            return "\"" + clean + "\"";
        }
        return clean;
    }
}
