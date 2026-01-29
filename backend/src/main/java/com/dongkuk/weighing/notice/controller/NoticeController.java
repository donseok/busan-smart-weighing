package com.dongkuk.weighing.notice.controller;

import com.dongkuk.weighing.auth.security.UserPrincipal;
import com.dongkuk.weighing.global.common.dto.ApiResponse;
import com.dongkuk.weighing.notice.domain.NoticeCategory;
import com.dongkuk.weighing.notice.dto.NoticeCreateRequest;
import com.dongkuk.weighing.notice.dto.NoticeResponse;
import com.dongkuk.weighing.notice.dto.NoticeUpdateRequest;
import com.dongkuk.weighing.notice.service.NoticeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 공지사항 컨트롤러
 *
 * 공지사항 CRUD 및 발행/고정 관리를 위한 REST API 엔드포인트를 제공한다.
 * 관리자 전용 기능(등록/수정/삭제/발행/고정)과
 * 일반 사용자 기능(목록 조회/카테고리별 조회/검색/상세 조회)을 포함한다.
 *
 * @author 시스템
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    /** 공지사항을 등록한다. 관리자 전용. */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<NoticeResponse>> createNotice(
            @Valid @RequestBody NoticeCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        NoticeResponse response = noticeService.createNotice(
                request, principal.getUserId(), principal.getLoginId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response));
    }

    /** 공지사항을 수정한다. 관리자 전용. */
    @PutMapping("/{noticeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<NoticeResponse>> updateNotice(
            @PathVariable Long noticeId,
            @Valid @RequestBody NoticeUpdateRequest request) {
        NoticeResponse response = noticeService.updateNotice(noticeId, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /** 공지사항을 삭제한다. 관리자 전용. */
    @DeleteMapping("/{noticeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteNotice(@PathVariable Long noticeId) {
        noticeService.deleteNotice(noticeId);
        return ResponseEntity.ok(ApiResponse.ok(null, "공지사항이 삭제되었습니다"));
    }

    /** 공지사항 발행/비발행 상태를 토글한다. 관리자 전용. */
    @PatchMapping("/{noticeId}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<NoticeResponse>> togglePublish(@PathVariable Long noticeId) {
        NoticeResponse response = noticeService.togglePublish(noticeId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /** 공지사항 고정/해제 상태를 토글한다. 관리자 전용. */
    @PatchMapping("/{noticeId}/pin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<NoticeResponse>> togglePin(@PathVariable Long noticeId) {
        NoticeResponse response = noticeService.togglePin(noticeId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /** 공개된 공지사항 목록을 페이징 조회한다. 고정 공지가 우선 표시된다. */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<NoticeResponse>>> getPublishedNotices(
            @PageableDefault(size = 10, sort = "publishedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<NoticeResponse> response = noticeService.getPublishedNotices(pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /** 카테고리별 공개된 공지사항 목록을 페이징 조회한다. */
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<Page<NoticeResponse>>> getNoticesByCategory(
            @PathVariable NoticeCategory category,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<NoticeResponse> response = noticeService.getNoticesByCategory(category, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /** 전체 공지사항 목록을 조회한다 (비공개 포함). 관리자 전용. */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<NoticeResponse>>> getAllNotices(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<NoticeResponse> response = noticeService.getAllNotices(pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /** 제목 키워드로 공개된 공지사항을 검색한다. */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<NoticeResponse>>> searchNotices(
            @RequestParam String keyword,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<NoticeResponse> response = noticeService.searchNotices(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /** 공지사항 상세 정보를 조회한다. 조회수가 자동 증가한다. */
    @GetMapping("/{noticeId}")
    public ResponseEntity<ApiResponse<NoticeResponse>> getNoticeDetail(@PathVariable Long noticeId) {
        NoticeResponse response = noticeService.getNoticeDetail(noticeId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /** 고정된 공개 공지사항 목록을 조회한다. */
    @GetMapping("/pinned")
    public ResponseEntity<ApiResponse<List<NoticeResponse>>> getPinnedNotices() {
        List<NoticeResponse> response = noticeService.getPinnedNotices();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
