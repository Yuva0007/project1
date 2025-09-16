package com.example.demo.services;

import com.example.demo.model.Grievance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final JavaMailSender mailSender;
    
    public void sendGrievanceSubmittedNotification(Grievance grievance) {
        try {
            if (grievance.getUser() == null) {
                log.info("Skipping submission email: no user linked to grievance {}", grievance.getId());
                return;
            }
            String subject = "Grievance Submitted - Tracking Number: " + grievance.getTrackingNumber();
            String message = String.format("""
                Dear %s,
                
                Your grievance has been successfully submitted with the following details:
                
                Title: %s
                Tracking Number: %s
                Submitted Date: %s
                Status: %s
                
                You can track your grievance using the tracking number above.
                
                Thank you for using our citizen grievance portal.
                
                Best regards,
                Citizen Grievance Portal Team
                """, 
                grievance.getUser().getName(),
                grievance.getTitle(),
                grievance.getTrackingNumber(),
                grievance.getSubmittedAt(),
                grievance.getStatus()
            );
            
            sendEmail(grievance.getUser().getEmail(), subject, message);
            log.info("Grievance submission notification sent to: {}", grievance.getUser().getEmail());
            
        } catch (Exception e) {
            log.error("Error sending grievance submission notification: {}", e.getMessage(), e);
        }
    }
    
    public void sendStatusUpdateNotification(Grievance grievance, Grievance.GrievanceStatus oldStatus, 
                                           Grievance.GrievanceStatus newStatus) {
        try {
            if (grievance.getUser() == null) {
                log.info("Skipping status email: no user linked to grievance {}", grievance.getId());
                return;
            }
            String subject = "Grievance Status Update - " + grievance.getTrackingNumber();
            String message = String.format("""
                Dear %s,
                
                The status of your grievance has been updated:
                
                Title: %s
                Tracking Number: %s
                Previous Status: %s
                New Status: %s
                Updated Date: %s
                
                You can track your grievance using the tracking number above.
                
                Best regards,
                Citizen Grievance Portal Team
                """, 
                grievance.getUser().getName(),
                grievance.getTitle(),
                grievance.getTrackingNumber(),
                oldStatus,
                newStatus,
                grievance.getUpdatedAt()
            );
            
            sendEmail(grievance.getUser().getEmail(), subject, message);
            log.info("Status update notification sent to: {}", grievance.getUser().getEmail());
            
        } catch (Exception e) {
            log.error("Error sending status update notification: {}", e.getMessage(), e);
        }
    }
    
    public void sendAssignmentNotification(Grievance grievance) {
        try {
            // Notify the citizen
            if (grievance.getUser() != null) {
                String citizenSubject = "Grievance Assigned - " + grievance.getTrackingNumber();
                String citizenMessage = String.format("""
                    Dear %s,
                    
                    Your grievance has been assigned to a department for review:
                    
                    Title: %s
                    Tracking Number: %s
                    Assigned Department: %s
                    Assigned Date: %s
                    
                    You will be notified of any further updates.
                    
                    Best regards,
                    Citizen Grievance Portal Team
                    """, 
                    grievance.getUser().getName(),
                    grievance.getTitle(),
                    grievance.getTrackingNumber(),
                    grievance.getDepartment() != null ? grievance.getDepartment().getName() : "TBD",
                    grievance.getAssignedAt()
                );
                
                sendEmail(grievance.getUser().getEmail(), citizenSubject, citizenMessage);
            } else {
                log.info("Skipping citizen assignment email: no user linked to grievance {}", grievance.getId());
            }
            
            // Notify the assigned officer if available
            if (grievance.getAssignedOfficer() != null) {
                String officerSubject = "New Grievance Assignment - " + grievance.getTrackingNumber();
                String officerMessage = String.format("""
                    Dear %s,
                    
                    A new grievance has been assigned to you:
                    
                    Title: %s
                    Tracking Number: %s
                    Priority: %s
                    Submitted by: %s
                    Submitted Date: %s
                    
                    Please review and take appropriate action.
                    
                    Best regards,
                    Citizen Grievance Portal Team
                    """, 
                    grievance.getAssignedOfficer().getName(),
                    grievance.getTitle(),
                    grievance.getTrackingNumber(),
                    grievance.getPriority(),
                    grievance.getUser().getName(),
                    grievance.getSubmittedAt()
                );
                
                sendEmail(grievance.getAssignedOfficer().getEmail(), officerSubject, officerMessage);
            }
            
            log.info("Assignment notifications sent for grievance: {}", grievance.getTrackingNumber());
            
        } catch (Exception e) {
            log.error("Error sending assignment notification: {}", e.getMessage(), e);
        }
    }
    
    public void sendOverdueNotification(Grievance grievance) {
        try {
            String subject = "Overdue Grievance Alert - " + grievance.getTrackingNumber();
            String message = String.format("""
                Dear %s,
                
                Your grievance has exceeded the expected resolution time:
                
                Title: %s
                Tracking Number: %s
                Expected Resolution Date: %s
                Current Status: %s
                
                We apologize for the delay and are working to resolve your grievance as soon as possible.
                
                Best regards,
                Citizen Grievance Portal Team
                """, 
                grievance.getUser().getName(),
                grievance.getTitle(),
                grievance.getTrackingNumber(),
                grievance.getExpectedResolutionDate(),
                grievance.getStatus()
            );
            
            sendEmail(grievance.getUser().getEmail(), subject, message);
            log.info("Overdue notification sent to: {}", grievance.getUser().getEmail());
            
        } catch (Exception e) {
            log.error("Error sending overdue notification: {}", e.getMessage(), e);
        }
    }
    
    private void sendEmail(String to, String subject, String message) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(to);
            mailMessage.setSubject(subject);
            mailMessage.setText(message);
            mailMessage.setFrom("noreply@grievanceportal.gov.in");
            
            mailSender.send(mailMessage);
            log.info("Email sent successfully to: {}", to);
            
        } catch (Exception e) {
            log.error("Error sending email to {}: {}", to, e.getMessage(), e);
        }
    }
}
