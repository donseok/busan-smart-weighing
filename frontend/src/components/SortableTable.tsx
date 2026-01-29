import React, { useState, useEffect, useMemo, useCallback } from 'react';
import { Table, Button, Tooltip, Skeleton } from 'antd';
import { HolderOutlined, ReloadOutlined } from '@ant-design/icons';
import type { TableProps, ColumnsType } from 'antd/es/table';
import type { ColumnType } from 'antd/es/table';
import {
  DndContext,
  closestCenter,
  KeyboardSensor,
  PointerSensor,
  useSensor,
  useSensors,
  DragEndEvent,
} from '@dnd-kit/core';
import {
  arrayMove,
  SortableContext,
  sortableKeyboardCoordinates,
  horizontalListSortingStrategy,
  useSortable,
} from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';

/**
 * 정렬 가능 테이블 컴포넌트의 속성 인터페이스
 *
 * Ant Design Table의 기본 속성을 확장하며, 컬럼 드래그 재배치 및
 * 자동 정렬 기능을 추가로 제공합니다.
 *
 * @template T - 테이블 데이터 레코드 타입
 * @property columns - 테이블 컬럼 정의 배열
 * @property tableKey - 로컬스토리지에 컬럼 순서를 저장할 고유 키
 * @property enableSort - 컬럼별 정렬 기능 활성화 여부 (기본값: true)
 * @property enableColumnDrag - 컬럼 드래그 재배치 기능 활성화 여부 (기본값: true)
 * @property skeletonRows - 초기 로딩 시 표시할 스켈레톤 행 수 (기본값: 5)
 */
interface SortableTableProps<T> extends Omit<TableProps<T>, 'columns'> {
  columns: ColumnsType<T>;
  tableKey: string;
  enableSort?: boolean;
  enableColumnDrag?: boolean;
  skeletonRows?: number;
}

/**
 * 드래그 가능한 헤더 셀 컴포넌트의 속성 인터페이스
 *
 * @property id - 드래그 식별자 (컬럼 키)
 * @property children - 헤더 셀 내용
 * @property style - 추가 CSS 스타일
 * @property className - CSS 클래스명
 */
interface DraggableHeaderCellProps {
  id: string;
  children: React.ReactNode;
  style?: React.CSSProperties;
  className?: string;
}

/**
 * 드래그 가능한 헤더 셀 컴포넌트
 *
 * dnd-kit의 useSortable 훅을 사용하여 드래그앤드롭으로
 * 컬럼 순서를 변경할 수 있는 테이블 헤더 셀입니다.
 * 드래그 중일 때 투명도 변경 및 배경색 강조 효과가 적용됩니다.
 *
 * @param props - 컴포넌트 속성
 * @returns 드래그 가능한 th 요소 JSX
 */
const DraggableHeaderCell: React.FC<DraggableHeaderCellProps> = ({
  id,
  children,
  style,
  className,
  ...rest
}) => {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({ id });

  /** 드래그 상태에 따른 동적 스타일 (드래그 중 투명도 감소 + 배경색 변경) */
  const cellStyle: React.CSSProperties = {
    ...style,
    transform: CSS.Transform.toString(transform),
    transition,
    cursor: 'move',
    opacity: isDragging ? 0.5 : 1,
    background: isDragging ? 'rgba(6, 182, 212, 0.1)' : undefined,
    position: 'relative',
  };

  return (
    <th
      ref={setNodeRef}
      style={cellStyle}
      className={className}
      {...attributes}
      {...listeners}
      {...rest}
    >
      {/* 드래그 핸들 아이콘과 헤더 내용을 가로로 배치 */}
      <div style={{ display: 'flex', alignItems: 'center', gap: 4, whiteSpace: 'nowrap' }}>
        <HolderOutlined style={{ color: '#999', fontSize: 12 }} />
        {children}
      </div>
    </th>
  );
};

/**
 * 일반 헤더 셀 컴포넌트 (드래그 비활성화 컬럼용)
 *
 * actions 컬럼 등 드래그가 불필요한 컬럼에 사용됩니다.
 *
 * @param props - 컴포넌트 속성
 * @returns 일반 th 요소 JSX
 */
const NormalHeaderCell: React.FC<{ children: React.ReactNode; style?: React.CSSProperties; className?: string }> = ({
  children,
  style,
  className,
  ...rest
}) => (
  <th style={{ ...style, whiteSpace: 'nowrap' }} className={className} {...rest}>
    {children}
  </th>
);

/**
 * 정렬 가능 테이블 컴포넌트
 *
 * Ant Design Table을 확장하여 다음 기능을 제공하는 범용 테이블입니다:
 * - 컬럼 헤더 드래그앤드롭으로 컬럼 순서 변경 (로컬스토리지에 순서 저장)
 * - 컬럼별 자동 정렬 기능 (숫자, 문자열 자동 감지)
 * - 컬럼 순서 초기화 버튼
 *
 * @template T - 테이블 데이터 레코드 타입
 * @param props - 컴포넌트 속성
 * @param props.columns - 컬럼 정의 배열
 * @param props.tableKey - 로컬스토리지 저장 키
 * @param props.enableSort - 정렬 기능 활성화 여부
 * @param props.enableColumnDrag - 컬럼 드래그 활성화 여부
 * @returns 정렬 가능 테이블 JSX
 */
function SortableTable<T extends object>({
  columns,
  tableKey,
  enableSort = true,
  enableColumnDrag = true,
  skeletonRows = 5,
  ...tableProps
}: SortableTableProps<T>) {
  /** 현재 컬럼 순서 상태 (컬럼 키 배열) */
  const [columnOrder, setColumnOrder] = useState<string[]>([]);

  /** 로컬스토리지에서 저장된 컬럼 순서를 불러오거나 기본 순서로 초기화 */
  useEffect(() => {
    const saved = localStorage.getItem(`table-column-order-${tableKey}`);
    if (saved) {
      try {
        const parsed = JSON.parse(saved);
        setColumnOrder(parsed);
      } catch {
        // JSON 파싱 실패 시 기본 컬럼 순서로 초기화
        setColumnOrder(columns.map((col) => getColumnKey(col)));
      }
    } else {
      setColumnOrder(columns.map((col) => getColumnKey(col)));
    }
  }, [tableKey, columns]);

  /**
   * 컬럼 객체에서 고유 키를 추출하는 유틸리티 함수
   *
   * @param col - 컬럼 정의 객체
   * @returns 컬럼 키 문자열 (key 또는 dataIndex 사용)
   */
  const getColumnKey = (col: ColumnType<T>): string => {
    return (col.key as string) || (col.dataIndex as string) || '';
  };

  /**
   * 자동 정렬 기능이 추가된 컬럼 목록
   *
   * sorter가 없고 dataIndex가 있는 컬럼에 자동으로 정렬 함수를 추가합니다.
   * 숫자 타입은 수치 비교, 문자열 타입은 한국어 로케일 비교를 사용합니다.
   */
  const columnsWithSorter = useMemo(() => {
    if (!enableSort) return columns;

    return columns.map((col) => {
      const column = col as ColumnType<T>;
      // 이미 sorter가 있거나, actions 컬럼이거나, dataIndex가 없으면 스킵
      if (column.sorter || column.key === 'actions' || !column.dataIndex) {
        return col;
      }

      return {
        ...column,
        sorter: (a: T, b: T) => {
          const aValue = (a as Record<string, unknown>)[column.dataIndex as string];
          const bValue = (b as Record<string, unknown>)[column.dataIndex as string];

          // null/undefined 처리 (null 값은 항상 앞으로)
          if (aValue == null && bValue == null) return 0;
          if (aValue == null) return -1;
          if (bValue == null) return 1;

          // 숫자 비교
          if (typeof aValue === 'number' && typeof bValue === 'number') {
            return aValue - bValue;
          }

          // 문자열 비교 (한국어 로케일)
          return String(aValue).localeCompare(String(bValue), 'ko');
        },
        sortDirections: ['ascend', 'descend'] as ('ascend' | 'descend')[],
      };
    });
  }, [columns, enableSort]);

  /**
   * 사용자 정의 순서가 적용된 최종 컬럼 목록
   *
   * 저장된 컬럼 순서에 따라 컬럼을 재배치하고,
   * 새로 추가된 컬럼(순서에 없는 것들)은 뒤에 추가합니다.
   */
  const orderedColumns = useMemo(() => {
    if (!enableColumnDrag || columnOrder.length === 0) {
      return columnsWithSorter;
    }

    const columnMap = new Map<string, ColumnType<T>>();
    columnsWithSorter.forEach((col) => {
      const key = getColumnKey(col as ColumnType<T>);
      columnMap.set(key, col as ColumnType<T>);
    });

    const ordered: ColumnsType<T> = [];
    columnOrder.forEach((key) => {
      const col = columnMap.get(key);
      if (col) {
        ordered.push(col);
        columnMap.delete(key);
      }
    });

    // 새로 추가된 컬럼들 (기존 순서에 없는 것들)을 뒤에 추가
    columnMap.forEach((col) => ordered.push(col));

    return ordered;
  }, [columnsWithSorter, columnOrder, enableColumnDrag]);

  /** 드래그앤드롭 센서 설정 (포인터 + 키보드) */
  const sensors = useSensors(
    useSensor(PointerSensor, {
      activationConstraint: {
        distance: 5, // 5px 이상 움직여야 드래그 시작 (클릭과 구분)
      },
    }),
    useSensor(KeyboardSensor, {
      coordinateGetter: sortableKeyboardCoordinates,
    })
  );

  /**
   * 드래그 종료 핸들러
   *
   * 드래그한 컬럼의 새로운 위치를 계산하고,
   * 변경된 순서를 상태와 로컬스토리지에 동시에 저장합니다.
   */
  const handleDragEnd = useCallback(
    (event: DragEndEvent) => {
      const { active, over } = event;

      if (over && active.id !== over.id) {
        setColumnOrder((items) => {
          const oldIndex = items.indexOf(active.id as string);
          const newIndex = items.indexOf(over.id as string);
          const newOrder = arrayMove(items, oldIndex, newIndex);

          // 변경된 컬럼 순서를 로컬스토리지에 영구 저장
          localStorage.setItem(
            `table-column-order-${tableKey}`,
            JSON.stringify(newOrder)
          );

          return newOrder;
        });
      }
    },
    [tableKey]
  );

  /**
   * 컬럼 순서 초기화 핸들러
   *
   * 컬럼 순서를 원래 정의된 기본 순서로 복원하고,
   * 로컬스토리지에서 저장된 순서를 삭제합니다.
   */
  const handleResetColumnOrder = useCallback(() => {
    const defaultOrder = columns.map((col) => getColumnKey(col as ColumnType<T>));
    setColumnOrder(defaultOrder);
    localStorage.removeItem(`table-column-order-${tableKey}`);
  }, [columns, tableKey]);

  /** 드래그 가능한 컬럼 ID 목록 (actions 컬럼 제외) */
  const draggableColumnIds = useMemo(() => {
    return orderedColumns
      .filter((col) => (col as ColumnType<T>).key !== 'actions')
      .map((col) => getColumnKey(col as ColumnType<T>));
  }, [orderedColumns]);

  /**
   * 커스텀 헤더 컴포넌트 설정
   *
   * actions 컬럼은 NormalHeaderCell을, 나머지는 DraggableHeaderCell을 사용하여
   * 드래그앤드롭 기능을 선택적으로 적용합니다.
   */
  const components = useMemo(() => {
    if (!enableColumnDrag) return undefined;

    return {
      header: {
        cell: (props: { children: React.ReactNode; style?: React.CSSProperties; className?: string; 'data-column-key'?: string }) => {
          const columnKey = props['data-column-key'];
          // actions 컬럼은 드래그 비활성화
          if (!columnKey || columnKey === 'actions') {
            return <NormalHeaderCell {...props} />;
          }
          return <DraggableHeaderCell id={columnKey} {...props} />;
        },
      },
    };
  }, [enableColumnDrag]);

  /** 각 컬럼에 data-column-key 속성을 추가하여 헤더 셀에서 컬럼을 식별 가능하게 함 */
  const finalColumns = useMemo((): ColumnsType<T> => {
    return orderedColumns.map((col) => {
      const column = col as ColumnType<T>;
      const key = getColumnKey(column);
      return {
        ...column,
        onHeaderCell: () => ({
          'data-column-key': key,
        } as unknown as React.HTMLAttributes<HTMLElement>),
      } as ColumnType<T>;
    });
  }, [orderedColumns]);

  /** 초기 로딩 상태: 데이터가 없고 loading 중이면 스켈레톤 표시 */
  const isInitialLoading = tableProps.loading && (!tableProps.dataSource || tableProps.dataSource.length === 0);

  if (isInitialLoading) {
    return (
      <div>
        {enableColumnDrag && (
          <div style={{ marginBottom: 8, display: 'flex', justifyContent: 'flex-end' }}>
            <Tooltip title="컬럼 순서 초기화">
              <Button size="small" icon={<ReloadOutlined />} onClick={handleResetColumnOrder}>
                컬럼 순서 초기화
              </Button>
            </Tooltip>
          </div>
        )}
        <Table<T>
          columns={finalColumns}
          dataSource={[]}
          locale={{ emptyText: (
            <div style={{ padding: '12px 0' }}>
              {Array.from({ length: skeletonRows }).map((_, i) => (
                <Skeleton key={i} active title={false} paragraph={{ rows: 1, width: '100%' }} style={{ marginBottom: 8 }} />
              ))}
            </div>
          )}}
          pagination={false}
        />
      </div>
    );
  }

  /* 컬럼 드래그가 비활성화된 경우 DndContext 없이 일반 테이블만 렌더링 */
  if (!enableColumnDrag) {
    return (
      <Table<T>
        {...tableProps}
        columns={finalColumns}
      />
    );
  }

  return (
    <div>
      {/* 컬럼 순서 초기화 버튼 */}
      <div style={{ marginBottom: 8, display: 'flex', justifyContent: 'flex-end' }}>
        <Tooltip title="컬럼 순서 초기화">
          <Button
            size="small"
            icon={<ReloadOutlined />}
            onClick={handleResetColumnOrder}
          >
            컬럼 순서 초기화
          </Button>
        </Tooltip>
      </div>
      {/* dnd-kit 드래그앤드롭 컨텍스트로 감싸진 테이블 */}
      <DndContext
        sensors={sensors}
        collisionDetection={closestCenter}
        onDragEnd={handleDragEnd}
      >
        <SortableContext
          items={draggableColumnIds}
          strategy={horizontalListSortingStrategy}
        >
          <Table<T>
            {...tableProps}
            columns={finalColumns}
            components={components}
          />
        </SortableContext>
      </DndContext>
    </div>
  );
}

export default SortableTable;
