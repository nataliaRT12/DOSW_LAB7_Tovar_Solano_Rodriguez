package edu.eci.dosw.todo.controller;

import edu.eci.dosw.todo.dto.AdminSummaryResponse;
import edu.eci.dosw.todo.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminService service;

    public AdminController(AdminService service) {
        this.service = service;
    }

    @GetMapping("/summary")
    public ResponseEntity<AdminSummaryResponse> summary() {
        return ResponseEntity.ok(service.summary());
    }
}
