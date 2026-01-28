package com.dongkuk.weighing.notice.service;

import com.dongkuk.weighing.global.common.exception.BusinessException;
import com.dongkuk.weighing.global.common.exception.ErrorCode;
import com.dongkuk.weighing.notice.domain.Notice;
import com.dongkuk.weighing.notice.domain.NoticeCategory;
import com.dongkuk.weighing.notice.domain.NoticeRepository;
import com.dongkuk.weighing.notice.dto.NoticeCreateRequest;
import com.dongkuk.weighing.notice.dto.NoticeResponse;
import com.dongkuk.weighing.notice.dto.NoticeUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NoticeServiceTest {

    @InjectMocks
    private NoticeService noticeService;

    @Mock
    private NoticeRepository noticeRepository;

    private Notice testNotice;

    @BeforeEach
    void setUp() {
        testNotice = Notice.builder()
                .title("테스트 공지사항")
                .content("테스트 내용입니다.")
                .category(NoticeCategory.GENERAL)
                .authorId(1L)
                .authorName("admin")
                .isPublished(true)
                .isPinned(false)
                .build();
    }

    @Test
    @DisplayName("공지사항 등록 성공")
    void createNotice_Success() {
        // Given
        NoticeCreateRequest request = new NoticeCreateRequest(
                "테스트 공지", "테스트 내용", NoticeCategory.GENERAL, true, false
        );
        given(noticeRepository.save(any(Notice.class))).willReturn(testNotice);

        // When
        NoticeResponse response = noticeService.createNotice(request, 1L, "admin");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.title()).isEqualTo("테스트 공지사항");
        assertThat(response.category()).isEqualTo(NoticeCategory.GENERAL);
        verify(noticeRepository).save(any(Notice.class));
    }

    @Test
    @DisplayName("공지사항 수정 성공")
    void updateNotice_Success() {
        // Given
        Long noticeId = 1L;
        NoticeUpdateRequest request = new NoticeUpdateRequest(
                "수정된 제목", "수정된 내용", NoticeCategory.SYSTEM, true
        );
        given(noticeRepository.findById(noticeId)).willReturn(Optional.of(testNotice));

        // When
        NoticeResponse response = noticeService.updateNotice(noticeId, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(testNotice.getTitle()).isEqualTo("수정된 제목");
        assertThat(testNotice.getCategory()).isEqualTo(NoticeCategory.SYSTEM);
    }

    @Test
    @DisplayName("존재하지 않는 공지사항 수정 시 예외 발생")
    void updateNotice_NotFound() {
        // Given
        Long noticeId = 999L;
        NoticeUpdateRequest request = new NoticeUpdateRequest(
                "수정된 제목", "수정된 내용", NoticeCategory.SYSTEM, false
        );
        given(noticeRepository.findById(noticeId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> noticeService.updateNotice(noticeId, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOTICE_001);
    }

    @Test
    @DisplayName("공개된 공지사항 목록 조회 성공")
    void getPublishedNotices_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Notice> noticePage = new PageImpl<>(List.of(testNotice));
        given(noticeRepository.findPublishedNotices(pageable)).willReturn(noticePage);

        // When
        Page<NoticeResponse> result = noticeService.getPublishedNotices(pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).title()).isEqualTo("테스트 공지사항");
    }

    @Test
    @DisplayName("공지사항 상세 조회 시 조회수 증가")
    void getNoticeDetail_IncreasesViewCount() {
        // Given
        Long noticeId = 1L;
        int initialViewCount = testNotice.getViewCount();
        given(noticeRepository.findById(noticeId)).willReturn(Optional.of(testNotice));

        // When
        NoticeResponse response = noticeService.getNoticeDetail(noticeId);

        // Then
        assertThat(response).isNotNull();
        assertThat(testNotice.getViewCount()).isEqualTo(initialViewCount + 1);
    }

    @Test
    @DisplayName("공지사항 발행 토글 성공")
    void togglePublish_Success() {
        // Given
        Long noticeId = 1L;
        boolean initialPublishState = testNotice.isPublished();
        given(noticeRepository.findById(noticeId)).willReturn(Optional.of(testNotice));

        // When
        noticeService.togglePublish(noticeId);

        // Then
        assertThat(testNotice.isPublished()).isNotEqualTo(initialPublishState);
    }

    @Test
    @DisplayName("공지사항 고정 토글 성공")
    void togglePin_Success() {
        // Given
        Long noticeId = 1L;
        boolean initialPinState = testNotice.isPinned();
        given(noticeRepository.findById(noticeId)).willReturn(Optional.of(testNotice));

        // When
        noticeService.togglePin(noticeId);

        // Then
        assertThat(testNotice.isPinned()).isNotEqualTo(initialPinState);
    }

    @Test
    @DisplayName("공지사항 삭제 성공")
    void deleteNotice_Success() {
        // Given
        Long noticeId = 1L;
        given(noticeRepository.findById(noticeId)).willReturn(Optional.of(testNotice));

        // When
        noticeService.deleteNotice(noticeId);

        // Then
        verify(noticeRepository).delete(testNotice);
    }
}
