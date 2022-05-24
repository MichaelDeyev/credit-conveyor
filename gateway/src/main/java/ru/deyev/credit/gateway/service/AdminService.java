package ru.deyev.credit.gateway.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.deyev.credit.gateway.feign.DealFeignClient;
import ru.deyev.credit.gateway.model.ApplicationDTO;

import java.util.List;

@Service
@AllArgsConstructor
public class AdminService {

    private final DealFeignClient dealFeignClient;

    public ApplicationDTO getApplicationById(Long applicationId) {
        return dealFeignClient.getApplicationById(applicationId);
    }

    public List<ApplicationDTO> getAllApplications() {
        return dealFeignClient.getAllApplications();
    }
}
