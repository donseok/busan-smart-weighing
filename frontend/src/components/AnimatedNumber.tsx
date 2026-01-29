import React, { useEffect, useState, useRef } from 'react';

interface AnimatedNumberProps {
  value: number;
  duration?: number;
  decimals?: number;
  suffix?: string;
  style?: React.CSSProperties;
}

const AnimatedNumber: React.FC<AnimatedNumberProps> = ({
  value,
  duration = 600,
  decimals = 0,
  suffix = '',
  style,
}) => {
  const [displayValue, setDisplayValue] = useState(value);
  const prevValue = useRef(value);
  const frameRef = useRef<number>(0);

  useEffect(() => {
    const start = prevValue.current;
    const end = value;
    const diff = end - start;

    if (diff === 0) return;

    const startTime = performance.now();

    const animate = (currentTime: number) => {
      const elapsed = currentTime - startTime;
      const progress = Math.min(elapsed / duration, 1);
      // Ease-out cubic
      const eased = 1 - Math.pow(1 - progress, 3);
      setDisplayValue(start + diff * eased);

      if (progress < 1) {
        frameRef.current = requestAnimationFrame(animate);
      } else {
        prevValue.current = end;
      }
    };

    frameRef.current = requestAnimationFrame(animate);

    return () => {
      if (frameRef.current) {
        cancelAnimationFrame(frameRef.current);
      }
    };
  }, [value, duration]);

  return (
    <span style={style}>
      {displayValue.toFixed(decimals).replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
      {suffix}
    </span>
  );
};

export default AnimatedNumber;
