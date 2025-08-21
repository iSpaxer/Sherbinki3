package ru.stm.shcherbinki3.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import ru.stm.shcherbinki3.model.redis.AccountInfo;
import ru.stm.shcherbinki3.security.DefaultAuthenticationPrincipal;
import ru.stm.shcherbinki3.service.JwtRedisService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api" + "/v${app.version}/")
@Tag(name = "Account API")
public class AccountController {

    private final JwtRedisService jwtRedisService;

    @Autowired
    public AccountController(JwtRedisService jwtRedisService) {this.jwtRedisService = jwtRedisService;}

    @GetMapping("/devices")
    @Operation(summary = "Get list of active devices for the current account",
            description = "Returns a list of all active sessions/devices associated with the authenticated account.",
            security = {@SecurityRequirement(name = "JWT")}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved active devices"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<AccountInfo>> getAccountInfo(
            @AuthenticationPrincipal DefaultAuthenticationPrincipal authenticationPrincipal) {
        return ResponseEntity.ok(jwtRedisService.getActiveAccounts(authenticationPrincipal.getToken()));
    }

    @PostMapping("/logout/others")
    @Operation(summary = "Logout from all other accounts",
            description = "Logs out from all sessions/devices except the current one.",
            security = {@SecurityRequirement(name = "JWT")}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully logged out from other sessions"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> logoutOthers(
            @AuthenticationPrincipal DefaultAuthenticationPrincipal authenticationPrincipal) {
        jwtRedisService.logoutOthers(authenticationPrincipal.getToken());
        return ResponseEntity.status(HttpStatus.OK)
                .build();
    }

}
