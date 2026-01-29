import { useEffect, useCallback } from 'react';

interface ShortcutHandler {
  key: string;
  ctrl?: boolean;
  shift?: boolean;
  handler: () => void;
  description: string;
}

/**
 * 키보드 단축키 훅
 *
 * 전역 키보드 단축키를 등록합니다.
 * 입력 필드(input, textarea, select)에 포커스 시 무시됩니다.
 */
export function useKeyboardShortcuts(shortcuts: ShortcutHandler[]) {
  const handleKeyDown = useCallback(
    (e: KeyboardEvent) => {
      // 입력 필드에서는 단축키 무시
      const target = e.target as HTMLElement;
      if (
        target.tagName === 'INPUT' ||
        target.tagName === 'TEXTAREA' ||
        target.tagName === 'SELECT' ||
        target.isContentEditable
      ) {
        // Escape는 항상 허용
        if (e.key !== 'Escape') return;
      }

      for (const shortcut of shortcuts) {
        const ctrlMatch = shortcut.ctrl ? (e.ctrlKey || e.metaKey) : !e.ctrlKey && !e.metaKey;
        const shiftMatch = shortcut.shift ? e.shiftKey : !e.shiftKey;
        const keyMatch = e.key.toLowerCase() === shortcut.key.toLowerCase();

        if (ctrlMatch && shiftMatch && keyMatch) {
          e.preventDefault();
          shortcut.handler();
          return;
        }
      }
    },
    [shortcuts],
  );

  useEffect(() => {
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [handleKeyDown]);
}
