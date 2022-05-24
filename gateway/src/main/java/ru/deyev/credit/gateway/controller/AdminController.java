package ru.deyev.credit.gateway.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.deyev.credit.gateway.api.AdminApi;
import ru.deyev.credit.gateway.model.ApplicationDTO;
import ru.deyev.credit.gateway.service.AdminService;

import java.util.List;

@RestController
@AllArgsConstructor
public class AdminController implements AdminApi {

    private final AdminService adminService;

    @Override
    public ResponseEntity<List<ApplicationDTO>> getAllApplications() {
        return ResponseEntity.ok(adminService.getAllApplications());
    }

    @Override
    public ResponseEntity<ApplicationDTO> getApplicationById(Long applicationId) {
        return ResponseEntity.ok(adminService.getApplicationById(applicationId));
    }
}
