package com.dongkuk.weighing.notice.service;

import com.dongkuk.weighing.global.common.exception.BusinessException;
import com.dongkuk.weighing.global.common.exception.ErrorCode;
import com.dongkuk.weighing.notice.domain.Notice;
import com.dongkuk.weighing.notice.domain.NoticeCategory;
import com.dongkuk.weighing.notice.domain.NoticeRepository;
import com.dongkuk.weighing.notice.dto.NoticeCreateRequest;
import com.dongkuk.weighing.notice.dto.NoticeResponse;
import com.dongkuk.weighing.notice.dto.NoticeUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final NoticeRepository noticeRepository;

    /**
     * 공지사항 등록 (관리자)
     */
    @Transactional
    public NoticeResponse createNotice(NoticeCreateRequest request, Long authorId, String authorName) {
        Notice notice = Notice.builder()
                .title(request.title())
                .content(request.content())
                .category(request.category())
                .authorId(authorId)
                .authorName(authorName)
                .isPublished(request.isPublished())
                .isPinned(request.isPinned())
                .build();

        Notice saved = noticeRepository.save(notice);
        log.info("공지사항 등록: noticeId={}, title={}, author={}",
                saved.getNoticeId(), saved.getTitle(), authorName);
        return NoticeResponse.from(saved);
    }

    /**
     * 공지사항 수정 (관리자)
     */
    @Transactional
    public NoticeResponse updateNotice(Long noticeId, NoticeUpdateRequest request) {
        Notice notice = findNoticeById(noticeId);
        notice.update(request.title(), request.content(), request.category(), request.isPinned());
        log.info("공지사항 수정: noticeId={}, title={}", noticeId, request.title());
        return NoticeResponse.from(notice);
    }

    /**
     * 공지사항 삭제 (관리자)
     */
    @Transactional
    public void deleteNotice(Long noticeId) {
        Notice notice = findNoticeById(noticeId);
        noticeRepository.delete(notice);
        log.info("공지사항 삭제: noticeId={}, title={}", noticeId, notice.getTitle());
    }

    /**
     * 공지사항 발행/비발행 토글 (관리자)
     */
    @Transactional
    public NoticeResponse togglePublish(Long noticeId) {
        Notice notice = findNoticeById(noticeId);
        if (notice.isPublished()) {
            notice.unpublish();
            log.info("공지사항 비발행: noticeId={}", noticeId);
        } else {
            notice.publish();
            log.info("공지사항 발행: noticeId={}", noticeId);
        }
        return NoticeResponse.from(notice);
    }

    /**
     * 공지사항 고정/해제 토글 (관리자)
     */
    @Transactional
    public NoticeResponse togglePin(Long noticeId) {
        Notice notice = findNoticeById(noticeId);
        notice.togglePin();
        log.info("공지사항 고정 토글: noticeId={}, isPinned={}", noticeId, notice.isPinned());
        return NoticeResponse.from(notice);
    }

    /**
     * 공개된 공지사항 목록 조회 (일반 사용자)
     */
    public Page<NoticeResponse> getPublishedNotices(Pageable pageable) {
        return noticeRepository.findPublishedNotices(pageable)
                .map(NoticeResponse::listFrom);
    }

    /**
     * 카테고리별 공지사항 목록 조회 (일반 사용자)
     */
    public Page<NoticeResponse> getNoticesByCategory(NoticeCategory category, Pageable pageable) {
        return noticeRepository.findPublishedNoticesByCategory(category, pageable)
                .map(NoticeResponse::listFrom);
    }

    /**
     * 전체 공지사항 목록 조회 (관리자)
     */
    public Page<NoticeResponse> getAllNotices(Pageable pageable) {
        return noticeRepository.findAllNotices(pageable)
                .map(NoticeResponse::listFrom);
    }

    /**
     * 공지사항 검색
     */
    public Page<NoticeResponse> searchNotices(String keyword, Pageable pageable) {
        return noticeRepository.searchByTitle(keyword, pageable)
                .map(NoticeResponse::listFrom);
    }

    /**
     * 공지사항 상세 조회 (조회수 증가)
     */
    @Transactional
    public NoticeResponse getNoticeDetail(Long noticeId) {
        Notice notice = findNoticeById(noticeId);
        notice.incrementViewCount();
        return NoticeResponse.from(notice);
    }

    /**
     * 고정된 공지사항 목록
     */
    public List<NoticeResponse> getPinnedNotices() {
        return noticeRepository.findByIsPublishedTrueAndIsPinnedTrueOrderByPublishedAtDesc()
                .stream()
                .map(NoticeResponse::listFrom)
                .toList();
    }

    private Notice findNoticeById(Long noticeId) {
        return noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTICE_001));
    }
}
