package com.nobodies.platform.user.controller;

import com.nobodies.platform.common.api.ApiResponse;
import com.nobodies.platform.user.dto.UpdateRoleRequest;
import com.nobodies.platform.user.dto.UserResponse;
import com.nobodies.platform.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public ApiResponse<List<UserResponse>> listUsers() {
        return ApiResponse.ok(userService.listUsers());
    }

    @PatchMapping("/{id}/role")
    public ApiResponse<Void> updateRole(@PathVariable Long id, @Valid @RequestBody UpdateRoleRequest request) {
        userService.updateRole(id, request);
        return ApiResponse.ok(null);
    }
}
