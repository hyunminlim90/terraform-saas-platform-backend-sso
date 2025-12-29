package click.opentofu.sso.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import click.opentofu.sso.dto.User;
import click.opentofu.sso.service.LoadApprovalsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(path = "/api/v1/request")
@RequiredArgsConstructor
public class LoadApprovals {
    
    private final LoadApprovalsService loadApprovalsService;

    @CrossOrigin(
        origins = {
            "https://studio.opentofu.click"
        },
        allowCredentials = "true"
    )
    @PostMapping(path = "/accounts/load-approvals")
    public ResponseEntity<Map<String, Object>> loadApprovals (
        @RequestBody User user
    ) {
        Map<String, Object> response = loadApprovalsService.loadApprovals(user);
        return ResponseEntity.ok().body(response);
    }

    @CrossOrigin(
        origins = {
            "https://studio.opentofu.click"
        },
        allowCredentials = "true"
    )
    @PostMapping(path = "/accounts/load-all")
    public ResponseEntity<List<Map<String, Object>>> loadAll (
    ) {
        List<Map<String, Object>> response = loadApprovalsService.loadAll();
        return ResponseEntity.ok().body(response);
    }

    @CrossOrigin(
        origins = {
            "https://studio.opentofu.click"
        },
        allowCredentials = "true"
    )
    @PostMapping(path = "/accounts/approve")
    public ResponseEntity<Map<String, Object>> approve (
        @RequestBody User user
    ) {
        Map<String, Object> response = loadApprovalsService.approve(user);
        return ResponseEntity.ok().body(response);
    }
}
