import React, { useState, useEffect } from 'react';
import { Tour, type TourProps } from 'antd';

const TOUR_KEY = 'bsw_onboarding_complete';

interface OnboardingTourProps {
  siderRef?: React.RefObject<HTMLDivElement | null>;
  headerRef?: React.RefObject<HTMLDivElement | null>;
  contentRef?: React.RefObject<HTMLDivElement | null>;
}

const OnboardingTour: React.FC<OnboardingTourProps> = ({ siderRef, headerRef, contentRef }) => {
  const [open, setOpen] = useState(false);

  useEffect(() => {
    const done = localStorage.getItem(TOUR_KEY);
    if (!done) {
      // Delay tour start to let the UI render
      const timer = setTimeout(() => setOpen(true), 1500);
      return () => clearTimeout(timer);
    }
  }, []);

  const handleClose = () => {
    setOpen(false);
    localStorage.setItem(TOUR_KEY, 'true');
  };

  const steps: TourProps['steps'] = [
    {
      title: '부산 스마트 계량 시스템에 오신 것을 환영합니다!',
      description: '이 시스템은 차량 계량을 자동으로 관리합니다. 주요 기능을 안내해 드리겠습니다.',
      target: null,
    },
    {
      title: '사이드바 메뉴',
      description: '왼쪽 메뉴에서 계량소 관제, 배차 관리, 통계 등 모든 기능에 접근할 수 있습니다.',
      target: siderRef?.current,
      placement: 'right',
    },
    {
      title: '상단 도구 모음',
      description: '즐겨찾기, 테마 전환, 마이페이지를 이용할 수 있습니다.',
      target: headerRef?.current,
      placement: 'bottom',
    },
    {
      title: '멀티탭 인터페이스',
      description: '여러 페이지를 탭으로 열어 동시에 작업할 수 있습니다. 탭을 드래그하여 순서를 변경하거나, 우클릭으로 탭을 관리하세요.',
      target: contentRef?.current,
      placement: 'top',
    },
  ];

  return <Tour open={open} onClose={handleClose} steps={steps} />;
};

export default OnboardingTour;
