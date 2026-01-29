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

/**
 * 공지사항 서비스
 *
 * 공지사항 CRUD, 발행/비발행 토글, 고정/해제 토글, 목록 조회,
 * 검색, 상세 조회(조회수 증가)를 처리하는 비즈니스 로직.
 *
 * @author 시스템
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final NoticeRepository noticeRepository;

    /** 공지사항을 등록한다. 작성자 정보를 함께 저장한다. */
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

    /** 공지사항을 수정한다. 제목, 내용, 카테고리, 고정 여부를 변경할 수 있다. */
    @Transactional
    public NoticeResponse updateNotice(Long noticeId, NoticeUpdateRequest request) {
        Notice notice = findNoticeById(noticeId);
        notice.update(request.title(), request.content(), request.category(), request.isPinned());
        log.info("공지사항 수정: noticeId={}, title={}", noticeId, request.title());
        return NoticeResponse.from(notice);
    }

    /** 공지사항을 삭제한다 (물리 삭제). */
    @Transactional
    public void deleteNotice(Long noticeId) {
        Notice notice = findNoticeById(noticeId);
        noticeRepository.delete(notice);
        log.info("공지사항 삭제: noticeId={}, title={}", noticeId, notice.getTitle());
    }

    /** 공지사항 발행/비발행 상태를 토글한다. 발행 시 발행일시가 설정된다. */
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

    /** 공지사항 고정/해제 상태를 토글한다. */
    @Transactional
    public NoticeResponse togglePin(Long noticeId) {
        Notice notice = findNoticeById(noticeId);
        notice.togglePin();
        log.info("공지사항 고정 토글: noticeId={}, isPinned={}", noticeId, notice.isPinned());
        return NoticeResponse.from(notice);
    }

    /** 공개된 공지사항 목록을 페이징 조회한다 (고정 우선, 최신순). */
    public Page<NoticeResponse> getPublishedNotices(Pageable pageable) {
        return noticeRepository.findPublishedNotices(pageable)
                .map(NoticeResponse::listFrom);
    }

    /** 카테고리별 공개된 공지사항 목록을 페이징 조회한다. */
    public Page<NoticeResponse> getNoticesByCategory(NoticeCategory category, Pageable pageable) {
        return noticeRepository.findPublishedNoticesByCategory(category, pageable)
                .map(NoticeResponse::listFrom);
    }

    /** 전체 공지사항 목록을 페이징 조회한다 (관리자용, 비공개 포함). */
    public Page<NoticeResponse> getAllNotices(Pageable pageable) {
        return noticeRepository.findAllNotices(pageable)
                .map(NoticeResponse::listFrom);
    }

    /** 제목 키워드로 공개된 공지사항을 검색한다. */
    public Page<NoticeResponse> searchNotices(String keyword, Pageable pageable) {
        return noticeRepository.searchByTitle(keyword, pageable)
                .map(NoticeResponse::listFrom);
    }

    /** 공지사항 상세 정보를 조회한다. 조회 시 조회수가 자동 증가한다. */
    @Transactional
    public NoticeResponse getNoticeDetail(Long noticeId) {
        Notice notice = findNoticeById(noticeId);
        notice.incrementViewCount();
        return NoticeResponse.from(notice);
    }

    /** 고정된 공개 공지사항 목록을 조회한다. */
    public List<NoticeResponse> getPinnedNotices() {
        return noticeRepository.findByIsPublishedTrueAndIsPinnedTrueOrderByPublishedAtDesc()
                .stream()
                .map(NoticeResponse::listFrom)
                .toList();
    }

    /** 공지사항 ID로 엔티티를 조회하고, 존재하지 않으면 예외를 발생시킨다. */
    private Notice findNoticeById(Long noticeId) {
        return noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTICE_001));
    }
}
