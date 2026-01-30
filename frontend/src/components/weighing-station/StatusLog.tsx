import React, { useRef, useEffect } from 'react';
import { theme } from 'antd';
import type { StatusLogEntry } from '../../types/weighingStation';

/**
 * 상태 로그 컴포넌트의 속성 인터페이스
 *
 * @property logs - 표시할 상태 로그 엔트리 배열 (최신이 상단)
 */
interface StatusLogProps {
  logs: StatusLogEntry[];
}

/**
 * 로그 레벨별 텍스트 색상 매핑
 *
 * - info: 회색 (일반 정보)
 * - success: 네온 초록 (성공)
 * - warning: 노란색 (경고)
 * - error: 빨간색 (오류)
 */
const LEVEL_COLORS: Record<StatusLogEntry['level'], string> = {
  info: '#CBD5E1',
  success: '#39FF14',
  warning: '#F59E0B',
  error: '#F43F5E',
};

/**
 * 상태 로그 컴포넌트
 *
 * 계량소 운영 중 발생하는 이벤트와 상태 변경을 터미널 스타일로 표시합니다.
 * 다크 배경에 모노스페이스 폰트를 사용하여 시스템 콘솔 느낌을 제공하며,
 * 각 로그 엔트리는 타임스탬프와 레벨별 색상으로 구분됩니다.
 * 상단의 트래픽 라이트(빨/노/초) 장식은 터미널 창 헤더를 모방합니다.
 *
 * @param props - 컴포넌트 속성
 * @param props.logs - 상태 로그 엔트리 배열
 * @returns 상태 로그 패널 JSX
 */
const StatusLog: React.FC<StatusLogProps> = ({ logs }) => {
  const { token } = theme.useToken();
  /** 로그 컨테이너 DOM 참조 (자동 스크롤용) */
  const containerRef = useRef<HTMLDivElement>(null);

  /** 새 로그 추가 시 컨테이너를 상단으로 스크롤 (최신 로그가 위에 표시) */
  useEffect(() => {
    if (containerRef.current) {
      containerRef.current.scrollTop = 0;
    }
  }, [logs.length]);

  return (
    <div
      style={{
        background: '#0D1117',
        border: `1px solid ${token.colorBorder}`,
        borderRadius: 12,
        overflow: 'hidden',
        boxShadow: '0 4px 16px rgba(0,0,0,0.2)',
      }}
    >
      {/* 터미널 스타일 헤더 (트래픽 라이트 + 제목 + 엔트리 수) */}
      <div
        style={{
          padding: '8px 16px',
          borderBottom: '1px solid #21262D',
          background: 'linear-gradient(135deg, #161B22, #0D1117)',
          display: 'flex',
          alignItems: 'center',
          gap: 8,
        }}
      >
        {/* macOS 스타일 트래픽 라이트 장식 (빨강/노랑/초록) */}
        <div style={{ display: 'flex', gap: 6 }}>
          <div style={{ width: 10, height: 10, borderRadius: '50%', background: '#F43F5E', boxShadow: '0 0 4px #F43F5E60' }} />
          <div style={{ width: 10, height: 10, borderRadius: '50%', background: '#F59E0B', boxShadow: '0 0 4px #F59E0B60' }} />
          <div style={{ width: 10, height: 10, borderRadius: '50%', background: '#10B981', boxShadow: '0 0 4px #10B98160' }} />
        </div>
        <span style={{ fontSize: 12, color: '#39FF14', fontFamily: 'monospace', fontWeight: 600 }}>
          상태 로그
        </span>
        {/* 로그 엔트리 총 개수 표시 배지 */}
        <span style={{
          fontSize: 10,
          color: '#8B949E',
          marginLeft: 'auto',
          fontFamily: 'monospace',
          background: '#1C2333',
          padding: '1px 8px',
          borderRadius: 4,
        }}>
          {logs.length} entries
        </span>
      </div>

      {/* 로그 본문 영역 (스크롤 가능, 고정 높이 220px) */}
      <div
        ref={containerRef}
        style={{
          height: 220,
          overflowY: 'auto',
          padding: '8px 16px',
          fontFamily: "'JetBrains Mono', 'Consolas', 'Courier New', monospace",
          fontSize: 12,
          lineHeight: 1.7,
        }}
      >
        {logs.length === 0 ? (
          /* 로그가 없을 때 빈 상태 메시지 */
          <div style={{ color: '#8B949E', padding: '16px 0', textAlign: 'center' }}>
            로그가 없습니다
          </div>
        ) : (
          /* 각 로그 엔트리: [타임스탬프] 메시지 형식으로 표시 */
          logs.map((entry) => (
            <div key={entry.id} style={{ display: 'flex', gap: 8, whiteSpace: 'nowrap' }}>
              {/* 타임스탬프 */}
              <span style={{ color: '#8B949E', flexShrink: 0 }}>
                [{entry.timestamp}]
              </span>
              {/* 로그 메시지 (레벨별 색상 적용) */}
              <span style={{ color: LEVEL_COLORS[entry.level], overflow: 'hidden', textOverflow: 'ellipsis' }}>
                {entry.message}
              </span>
            </div>
          ))
        )}
      </div>
    </div>
  );
};

export default StatusLog;
