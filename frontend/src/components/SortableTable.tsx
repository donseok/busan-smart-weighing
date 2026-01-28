import React, { useState, useEffect, useMemo, useCallback } from 'react';
import { Table, Button, Tooltip } from 'antd';
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

interface SortableTableProps<T> extends Omit<TableProps<T>, 'columns'> {
  columns: ColumnsType<T>;
  tableKey: string; // 로컬스토리지 저장 키
  enableSort?: boolean; // 정렬 기능 활성화 (기본 true)
  enableColumnDrag?: boolean; // 컬럼 이동 기능 활성화 (기본 true)
}

// 드래그 가능한 헤더 셀 컴포넌트
interface DraggableHeaderCellProps {
  id: string;
  children: React.ReactNode;
  style?: React.CSSProperties;
  className?: string;
}

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
      <div style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
        <HolderOutlined style={{ color: '#999', fontSize: 12 }} />
        {children}
      </div>
    </th>
  );
};

// 일반 헤더 셀 (드래그 비활성화 컬럼용)
const NormalHeaderCell: React.FC<{ children: React.ReactNode; style?: React.CSSProperties; className?: string }> = ({
  children,
  style,
  className,
  ...rest
}) => (
  <th style={style} className={className} {...rest}>
    {children}
  </th>
);

function SortableTable<T extends object>({
  columns,
  tableKey,
  enableSort = true,
  enableColumnDrag = true,
  ...tableProps
}: SortableTableProps<T>) {
  // 컬럼 순서 상태
  const [columnOrder, setColumnOrder] = useState<string[]>([]);

  // 로컬스토리지에서 컬럼 순서 불러오기
  useEffect(() => {
    const saved = localStorage.getItem(`table-column-order-${tableKey}`);
    if (saved) {
      try {
        const parsed = JSON.parse(saved);
        setColumnOrder(parsed);
      } catch {
        // 초기 순서 설정
        setColumnOrder(columns.map((col) => getColumnKey(col)));
      }
    } else {
      setColumnOrder(columns.map((col) => getColumnKey(col)));
    }
  }, [tableKey, columns]);

  // 컬럼 키 추출 함수
  const getColumnKey = (col: ColumnType<T>): string => {
    return (col.key as string) || (col.dataIndex as string) || '';
  };

  // 정렬 기능이 추가된 컬럼
  const columnsWithSorter = useMemo(() => {
    if (!enableSort) return columns;

    return columns.map((col) => {
      const column = col as ColumnType<T>;
      // 이미 sorter가 있거나, actions 컬럼이면 스킵
      if (column.sorter || column.key === 'actions' || !column.dataIndex) {
        return col;
      }

      return {
        ...column,
        sorter: (a: T, b: T) => {
          const aValue = (a as Record<string, unknown>)[column.dataIndex as string];
          const bValue = (b as Record<string, unknown>)[column.dataIndex as string];

          // null/undefined 처리
          if (aValue == null && bValue == null) return 0;
          if (aValue == null) return -1;
          if (bValue == null) return 1;

          // 숫자 비교
          if (typeof aValue === 'number' && typeof bValue === 'number') {
            return aValue - bValue;
          }

          // 문자열 비교
          return String(aValue).localeCompare(String(bValue), 'ko');
        },
        sortDirections: ['ascend', 'descend'] as ('ascend' | 'descend')[],
      };
    });
  }, [columns, enableSort]);

  // 순서가 적용된 컬럼
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

    // 새로 추가된 컬럼들 (순서에 없는 것들)
    columnMap.forEach((col) => ordered.push(col));

    return ordered;
  }, [columnsWithSorter, columnOrder, enableColumnDrag]);

  // 드래그앤드롭 센서 설정
  const sensors = useSensors(
    useSensor(PointerSensor, {
      activationConstraint: {
        distance: 5,
      },
    }),
    useSensor(KeyboardSensor, {
      coordinateGetter: sortableKeyboardCoordinates,
    })
  );

  // 드래그 종료 핸들러
  const handleDragEnd = useCallback(
    (event: DragEndEvent) => {
      const { active, over } = event;

      if (over && active.id !== over.id) {
        setColumnOrder((items) => {
          const oldIndex = items.indexOf(active.id as string);
          const newIndex = items.indexOf(over.id as string);
          const newOrder = arrayMove(items, oldIndex, newIndex);

          // 로컬스토리지에 저장
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

  // 컬럼 순서 초기화
  const handleResetColumnOrder = useCallback(() => {
    const defaultOrder = columns.map((col) => getColumnKey(col as ColumnType<T>));
    setColumnOrder(defaultOrder);
    localStorage.removeItem(`table-column-order-${tableKey}`);
  }, [columns, tableKey]);

  // 드래그 가능한 컬럼 ID 목록 (actions 제외)
  const draggableColumnIds = useMemo(() => {
    return orderedColumns
      .filter((col) => (col as ColumnType<T>).key !== 'actions')
      .map((col) => getColumnKey(col as ColumnType<T>));
  }, [orderedColumns]);

  // 커스텀 헤더 컴포넌트
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

  // 컬럼에 data-column-key 속성 추가
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
