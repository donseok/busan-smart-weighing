package com.dongkuk.weighing.audit.controller;

import com.dongkuk.weighing.audit.domain.AuditActionType;
import com.dongkuk.weighing.audit.domain.AuditEntityType;
import com.dongkuk.weighing.audit.dto.AuditLogResponse;
import com.dongkuk.weighing.audit.dto.AuditSearchCondition;
import com.dongkuk.weighing.audit.service.AuditLogService;
import com.dongkuk.weighing.global.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/admin/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getAuditLogs(
            @RequestParam(required = false) Long actorId,
            @RequestParam(required = false) AuditActionType actionType,
            @RequestParam(required = false) AuditEntityType entityType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        AuditSearchCondition condition = new AuditSearchCondition(
                actorId, actionType, entityType, startDate, endDate
        );
        Page<AuditLogResponse> response = auditLogService.getAuditLogs(condition, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{auditLogId}")
    public ResponseEntity<ApiResponse<AuditLogResponse>> getAuditLog(
            @PathVariable Long auditLogId
    ) {
        AuditLogResponse response = auditLogService.getAuditLog(auditLogId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
