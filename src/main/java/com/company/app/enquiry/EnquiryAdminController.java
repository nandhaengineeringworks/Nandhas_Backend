package com.company.app.enquiry;

import com.company.app.catalogue.entity.CategoryType;
import com.company.app.common.ApiResponse;
import com.company.app.common.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/enquiries")
@RequiredArgsConstructor
@Tag(name = "Admin - Enquiries & Leads CRM", description = "Admin Lead Pipeline & Management APIs")
@SecurityRequirement(name = "BearerAuth")
@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_PRODUCT_MANAGER', 'ROLE_SALES')")
public class EnquiryAdminController {

    private final EnquiryService enquiryService;

    @GetMapping
    @Operation(summary = "Get lead submissions with status/type filter & pagination")
    public ResponseEntity<ApiResponse<PagedResponse<EnquiryResponseDTO>>> getEnquiries(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false) EnquiryStatus status,
            @RequestParam(required = false) CategoryType productType,
            @RequestParam(required = false) String search
    ) {
        PagedResponse<EnquiryResponseDTO> response = enquiryService.getEnquiries(page, size, status, productType, search);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get lead detail by ID")
    public ResponseEntity<ApiResponse<EnquiryResponseDTO>> getEnquiryById(@PathVariable Long id) {
        EnquiryResponseDTO response = enquiryService.getEnquiryById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update lead pipeline status and internal notes")
    public ResponseEntity<ApiResponse<EnquiryResponseDTO>> updateEnquiryStatus(
            @PathVariable Long id,
            @Valid @RequestBody EnquiryStatusUpdateDTO dto
    ) {
        EnquiryResponseDTO updated = enquiryService.updateEnquiryStatus(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Lead status updated", updated));
    }

    @GetMapping("/export/csv")
    @Operation(summary = "Export all enquiries to CSV format")
    public ResponseEntity<String> exportEnquiriesCsv() {
        String csv = enquiryService.exportToCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=enquiries_leads.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }
}
