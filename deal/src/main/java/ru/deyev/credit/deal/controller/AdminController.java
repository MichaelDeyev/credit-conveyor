package ru.deyev.credit.deal.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.deyev.credit.deal.model.ApplicationDTO;
import ru.deyev.credit.deal.model.ApplicationStatus;
import ru.deyev.credit.deal.service.AdminService;

import java.util.List;

@RestController
@RequestMapping("deal/admin")
@AllArgsConstructor
public class AdminController {

    private AdminService adminService;

    @GetMapping("application/{applicationId}")
    public ApplicationDTO getApplicationById(@PathVariable Long applicationId) {
        return adminService.getApplicationById(applicationId);
    }

    @GetMapping("application/all")
    public List<ApplicationDTO> getAllApplications() {
        return adminService.getAllApplications();
    }

    @PutMapping("application/{applicationId}/status")
    public void updateApplicationStatusById(@PathVariable Long applicationId, @RequestParam String statusName) {
        adminService.updateApplicationStatusById(applicationId, ApplicationStatus.fromValue(statusName));
    }
}
