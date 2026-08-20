package com.company.app.enquiry;

import com.company.app.common.ApiResponse;
import com.company.app.firebase.FirebaseAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.util.StringUtils;

@RestController
@RequestMapping("/api/enquiries")
@RequiredArgsConstructor
@Tag(name = "Public - Enquiries", description = "Public Quote / Lead Request Submissions")
public class EnquiryPublicController {

    private final EnquiryService enquiryService;
    private final FirebaseAuthService firebaseAuthService;

    @PostMapping
    @Operation(summary = "Submit a product enquiry / quote request")
    public ResponseEntity<ApiResponse<EnquiryResponseDTO>> submitEnquiry(
            @Valid @RequestBody EnquiryRequestDTO dto,
            @RequestHeader(value = "X-Firebase-ID-Token", required = false) String firebaseIdToken
    ) {
        if (StringUtils.hasText(firebaseIdToken)) {
            FirebaseAuthService.VerifiedPhone verified = firebaseAuthService.verifyIdToken(firebaseIdToken);
            String submittedPhone = dto.getPhone().replaceAll("\\D", "");
            String verifiedPhone = verified.phoneNumber().replaceAll("\\D", "");
            String verifiedLastTen = verifiedPhone.substring(Math.max(0, verifiedPhone.length() - 10));
            if (!submittedPhone.endsWith(verifiedLastTen)) {
                throw new org.springframework.web.server.ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "The verified phone number does not match the enquiry phone number");
            }
        }
        EnquiryResponseDTO response = enquiryService.submitEnquiry(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thank you! Your enquiry has been received. Our sales specialist will contact you shortly.", response));
    }
}
