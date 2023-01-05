package ru.deyev.credit.gateway.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import ru.deyev.credit.gateway.api.AdminApi;
import ru.deyev.credit.gateway.model.ApplicationDTO;
import ru.deyev.credit.gateway.model.AuditAction;
import ru.deyev.credit.gateway.service.AdminService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AdminController implements AdminApi {

    private final AdminService adminService;

    @CrossOrigin(origins = "*")
    @Override
    public ResponseEntity<List<ApplicationDTO>> getAllApplications() {
        return ResponseEntity.ok(adminService.getAllApplications());
    }

    @CrossOrigin(origins = "*")
    @Override
    public ResponseEntity<ApplicationDTO> getApplicationById(Long applicationId) {
        return ResponseEntity.ok(adminService.getApplicationById(applicationId));
    }

    @CrossOrigin(origins = "*")
    @Override
    public ResponseEntity<List<AuditAction>> getAllAuditActions() {
        return ResponseEntity.ok(adminService.getAllAuditActions());
    }

    @CrossOrigin(origins = "*")
    @Override
    public ResponseEntity<AuditAction> getAuditActionById(UUID auditId) {
        return ResponseEntity.ok(adminService.getAuditActionById(auditId));
    }
}
