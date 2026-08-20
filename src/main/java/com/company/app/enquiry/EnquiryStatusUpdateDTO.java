package com.company.app.enquiry;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnquiryStatusUpdateDTO {
    @NotNull(message = "Status is required")
    private EnquiryStatus status;
    private String internalNotes;
    private String assignedTo;
}
