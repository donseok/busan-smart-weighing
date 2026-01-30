/**
 * 테이블 페이지 레이아웃 컴포넌트
 *
 * 조회 페이지에서 검색 조건 영역은 고정하고,
 * 테이블(그리드) 영역만 스크롤되도록 하는 flex 레이아웃을 제공합니다.
 *
 * 사용법:
 *   <TablePageLayout>
 *     <FixedArea>제목, 검색 조건, 결과 건수</FixedArea>
 *     <ScrollArea>SortableTable</ScrollArea>
 *     <FixedArea>페이지네이션</FixedArea>
 *   </TablePageLayout>
 */
import type { FC, ReactNode } from 'react';

/** 전체 페이지를 flex column으로 배치하는 최상위 컨테이너 */
export const TablePageLayout: FC<{ children: ReactNode }> = ({ children }) => (
  <div style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
    {children}
  </div>
);

/** 고정 영역 (검색 조건, 제목, 페이지네이션 등 — 스크롤되지 않음) */
export const FixedArea: FC<{ children: ReactNode }> = ({ children }) => (
  <div style={{ flexShrink: 0 }}>{children}</div>
);

/** 스크롤 영역 (테이블/그리드 — 테이블 자체가 스크롤을 처리) */
export const ScrollArea: FC<{ children: ReactNode }> = ({ children }) => (
  <div style={{ flex: 1, minHeight: 0, overflow: 'hidden' }}>{children}</div>
);
