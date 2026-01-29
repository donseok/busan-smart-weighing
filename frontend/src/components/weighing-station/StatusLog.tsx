import React, { useRef, useEffect } from 'react';
import { theme } from 'antd';
import type { StatusLogEntry } from '../../types/weighingStation';

interface StatusLogProps {
  logs: StatusLogEntry[];
}

const LEVEL_COLORS: Record<StatusLogEntry['level'], string> = {
  info: '#94A3B8',
  success: '#39FF14',
  warning: '#F59E0B',
  error: '#F43F5E',
};

const StatusLog: React.FC<StatusLogProps> = ({ logs }) => {
  const { token } = theme.useToken();
  const containerRef = useRef<HTMLDivElement>(null);

  // 새 로그 추가 시 상단으로 스크롤 (최신이 위에 있으므로 불필요하지만 안전장치)
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
      {/* 헤더 */}
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
        <div style={{ display: 'flex', gap: 6 }}>
          <div style={{ width: 10, height: 10, borderRadius: '50%', background: '#F43F5E', boxShadow: '0 0 4px #F43F5E60' }} />
          <div style={{ width: 10, height: 10, borderRadius: '50%', background: '#F59E0B', boxShadow: '0 0 4px #F59E0B60' }} />
          <div style={{ width: 10, height: 10, borderRadius: '50%', background: '#10B981', boxShadow: '0 0 4px #10B98160' }} />
        </div>
        <span style={{ fontSize: 12, color: '#39FF14', fontFamily: 'monospace', fontWeight: 600 }}>
          상태 로그
        </span>
        <span style={{
          fontSize: 10,
          color: '#484F58',
          marginLeft: 'auto',
          fontFamily: 'monospace',
          background: '#1C2333',
          padding: '1px 8px',
          borderRadius: 4,
        }}>
          {logs.length} entries
        </span>
      </div>

      {/* 로그 본문 */}
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
          <div style={{ color: '#484F58', padding: '16px 0', textAlign: 'center' }}>
            로그가 없습니다
          </div>
        ) : (
          logs.map((entry) => (
            <div key={entry.id} style={{ display: 'flex', gap: 8, whiteSpace: 'nowrap' }}>
              <span style={{ color: '#484F58', flexShrink: 0 }}>
                [{entry.timestamp}]
              </span>
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
