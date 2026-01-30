/**
 * @fileoverview 온보딩 투어 컴포넌트
 *
 * 신규 사용자가 처음 로그인했을 때 시스템의 주요 기능을 안내하는
 * 가이드 투어를 제공하는 컴포넌트입니다.
 * Ant Design Tour 컴포넌트를 사용하여 단계별로 UI 요소를 하이라이트하고 설명합니다.
 * 투어 완료 후 localStorage에 상태를 저장하여 다음 방문 시 표시되지 않습니다.
 *
 * @module components/OnboardingTour
 */
import React, { useState, useEffect } from 'react';
import { Tour, type TourProps } from 'antd';

/** localStorage에 저장되는 온보딩 완료 상태 키 */
const TOUR_KEY = 'bsw_onboarding_complete';

/**
 * OnboardingTour 컴포넌트 속성 인터페이스
 *
 * @property siderRef - 사이드바 메뉴 영역 참조 (투어 Step 2에서 하이라이트)
 * @property headerRef - 상단 헤더 영역 참조 (투어 Step 3에서 하이라이트)
 * @property contentRef - 메인 콘텐츠 영역 참조 (투어 Step 4에서 하이라이트)
 */
interface OnboardingTourProps {
  siderRef?: React.RefObject<HTMLDivElement | null>;
  headerRef?: React.RefObject<HTMLDivElement | null>;
  contentRef?: React.RefObject<HTMLDivElement | null>;
}

/**
 * 온보딩 투어 컴포넌트
 *
 * 신규 사용자에게 시스템 사용법을 단계별로 안내하는 가이드 투어입니다.
 * 사이드바, 헤더, 콘텐츠 영역을 순차적으로 하이라이트하며 설명합니다.
 *
 * 동작 방식:
 * 1. 컴포넌트 마운트 시 localStorage 확인
 * 2. 온보딩 미완료 상태면 1.5초 후 투어 시작
 * 3. 투어 완료 시 localStorage에 완료 상태 저장
 *
 * @param props - 컴포넌트 속성
 * @param props.siderRef - 사이드바 DOM 참조
 * @param props.headerRef - 헤더 DOM 참조
 * @param props.contentRef - 콘텐츠 DOM 참조
 * @returns Ant Design Tour 컴포넌트
 */
const OnboardingTour: React.FC<OnboardingTourProps> = ({ siderRef, headerRef, contentRef }) => {
  /** 투어 표시 여부 상태 */
  const [open, setOpen] = useState(false);

  // 컴포넌트 마운트 시 온보딩 완료 여부 확인
  useEffect(() => {
    const done = localStorage.getItem(TOUR_KEY);
    if (!done) {
      // UI 렌더링 완료 후 투어 시작을 위해 1.5초 지연
      // (DOM 요소가 완전히 렌더링되어야 하이라이트 가능)
      const timer = setTimeout(() => setOpen(true), 1500);
      return () => clearTimeout(timer);
    }
  }, []);

  /**
   * 투어 닫기 핸들러
   *
   * 투어를 닫고 localStorage에 완료 상태를 저장합니다.
   * 이후 재방문 시 투어가 표시되지 않습니다.
   */
  const handleClose = () => {
    setOpen(false);
    localStorage.setItem(TOUR_KEY, 'true');
  };

  /** 투어 단계 정의 - 환영 메시지 → 사이드바 → 헤더 → 탭 인터페이스 순 */
  const steps: TourProps['steps'] = [
    {
      // Step 1: 환영 메시지 (특정 요소 하이라이트 없음)
      title: '부산 스마트 계량 시스템에 오신 것을 환영합니다!',
      description: '이 시스템은 차량 계량을 자동으로 관리합니다. 주요 기능을 안내해 드리겠습니다.',
      target: null, // null이면 화면 중앙에 모달 표시
    },
    {
      // Step 2: 사이드바 메뉴 안내
      title: '사이드바 메뉴',
      description: '왼쪽 메뉴에서 계량소 관제, 배차 관리, 통계 등 모든 기능에 접근할 수 있습니다.',
      target: siderRef?.current,
      placement: 'right', // 팝오버가 대상 요소 오른쪽에 표시
    },
    {
      // Step 3: 상단 헤더 안내
      title: '상단 도구 모음',
      description: '즐겨찾기, 테마 전환, 마이페이지를 이용할 수 있습니다.',
      target: headerRef?.current,
      placement: 'bottom', // 팝오버가 대상 요소 아래에 표시
    },
    {
      // Step 4: 멀티탭 인터페이스 안내
      title: '멀티탭 인터페이스',
      description: '여러 페이지를 탭으로 열어 동시에 작업할 수 있습니다. 탭을 드래그하여 순서를 변경하거나, 우클릭으로 탭을 관리하세요.',
      target: contentRef?.current,
      placement: 'top', // 팝오버가 대상 요소 위에 표시
    },
  ];

  return <Tour open={open} onClose={handleClose} steps={steps} />;
};

export default OnboardingTour;
