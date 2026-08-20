package com.company.app.enquiry;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailNotificationService {

    @Value("${app.mail.admin-notification-email:admin@machinery-interior.com}")
    private String adminEmail;

    public void sendNewEnquiryNotification(Enquiry enquiry) {
        // Log formatted email notification dispatch (AWS SES / SMTP template)
        log.info("============== NEW LEAD / ENQUIRY NOTIFICATION ==============");
        log.info("To: {}", adminEmail);
        log.info("Lead Name: {}", enquiry.getName());
        log.info("Phone: {}", enquiry.getPhone());
        log.info("Email: {}", enquiry.getEmail());
        log.info("Product: {}", enquiry.getProductName());
        log.info("Company: {}", enquiry.getCompanyName());
        log.info("Quantity: {}", enquiry.getEstimatedQuantity());
        log.info("Message: {}", enquiry.getMessage());
        log.info("=============================================================");
    }
}
