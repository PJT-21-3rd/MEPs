// src/composables/useClickOutside.js
import { onMounted, onUnmounted } from 'vue';

/**
 * 특정 DOM 요소 외부 클릭을 감지
 * @param {Ref} targetRef - 감지할 기준이 되는 DOM
 * @param {Function} callback - 외부 클릭 시 실행할 함수
 */
export function useClickOutside(targetRef, callback) {
  const handleClick = (event) => {
    // targetRef가 존재하고, 클릭된 요소가 targetRef 안에 없을 때만 콜백 실행
    if (targetRef.value && !targetRef.value.contains(event.target)) {
      callback();
    }
  };

  onMounted(() => {
    document.addEventListener('click', handleClick);
  });

  onUnmounted(() => {
    document.removeEventListener('click', handleClick);
  });
}
