// /js/review-list.js
(function () {
  const form = document.getElementById('reviewCreateForm');
  if (!form) return;

  const cafeId = form.getAttribute('data-cafe-id');
  const csrfToken  = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

  const ratingInputs = Array.from(form.querySelectorAll('input[name="rating"]'));
  const ratingDisplay = document.querySelector('[data-rating-display]');
  const ratingValue = document.getElementById('createRatingValue');
  const starWrapper = form.querySelector('.rating-picker__star-wrapper');

  const ratingValues = ratingInputs
    .map((input) => Number.parseFloat(input.value))
    .filter((value) => Number.isFinite(value))
    .sort((a, b) => a - b);

  const minRating = ratingValues.length ? ratingValues[0] : 0;
  const maxRating = ratingValues.length ? ratingValues[ratingValues.length - 1] : 5;

  const syncRating = (value) => {
    const numericValue = Number.parseFloat(value);
    const fallback = Number.isFinite(numericValue) ? numericValue : minRating;
    const rating = Math.min(maxRating, Math.max(minRating, fallback));
    if (ratingDisplay) {
      ratingDisplay.style.setProperty('--rating', rating.toString());
    }
    if (ratingValue) {
      ratingValue.textContent = rating.toFixed(1);
    }
  };

  const getCheckedRating = () => {
    const checked = ratingInputs.find((input) => input.checked);
    if (checked) {
      return checked.value;
    }
    const defaultChecked = ratingInputs.find((input) => input.defaultChecked);
    if (defaultChecked) {
      return defaultChecked.value;
    }
    return ratingValues.length ? ratingValues[Math.floor(ratingValues.length / 2)] : maxRating;
  };

  if (ratingInputs.length) {
    syncRating(getCheckedRating());
    ratingInputs.forEach((input) => {
      input.addEventListener('change', () => {
        syncRating(input.value);
      });
    });
  } else {
    syncRating(minRating);
  }

  const findRatingInput = (value) => {
    const numericValue = Number.parseFloat(value);
    if (!Number.isFinite(numericValue)) return null;
    return ratingInputs.find((input) => {
      const inputValue = Number.parseFloat(input.value);
      return Number.isFinite(inputValue) && Math.abs(inputValue - numericValue) < 0.001;
    }) || null;
  };

  const getRatingFromPosition = (clientX) => {
    if (!starWrapper || !ratingValues.length) return null;
    const rect = starWrapper.getBoundingClientRect();
    const width = rect.width;
    if (!width || Number.isNaN(width)) return null;
    const ratio = (clientX - rect.left) / width;
    const clamped = Math.min(0.999999, Math.max(0, ratio));
    const index = Math.min(ratingValues.length - 1, Math.floor(clamped * ratingValues.length));
    return ratingValues[index] ?? null;
  };

  if (starWrapper && ratingInputs.length) {
    starWrapper.addEventListener('pointermove', (event) => {
      if (event.pointerType !== 'mouse' && event.pointerType !== 'pen') return;
      const hoveredRating = getRatingFromPosition(event.clientX);
      if (hoveredRating != null) {
        syncRating(hoveredRating);
      }
    });

    starWrapper.addEventListener('pointerleave', () => {
      syncRating(getCheckedRating());
    });

    starWrapper.addEventListener('pointerdown', (event) => {
      if (event.button != null && event.pointerType === 'mouse' && event.button !== 0) {
        return;
      }
      const selectedRating = getRatingFromPosition(event.clientX);
      if (selectedRating == null) return;

      const targetInput = findRatingInput(selectedRating);
      if (!targetInput) return;

      event.preventDefault();
      targetInput.checked = true;
      targetInput.dispatchEvent(new Event('change', { bubbles: true }));
      syncRating(selectedRating);
    });

    starWrapper.addEventListener('click', (event) => {
      if (event.target.tagName === 'LABEL' && event.detail !== 0) {
        event.preventDefault();
      }
    });
  }

  form.addEventListener('submit', async (e) => {
    if (cafeId && localStorage.getItem('certifiedCafe_' + cafeId) !== 'true') {
      e.preventDefault();
      if (confirm('위치인증을 먼저 해주세요. 위치 인증 페이지로 이동하시겠습니까?')) {
        window.location.href = '/cafes/' + cafeId + '/reviews/location';
      }
      return;
    }
    if (!form.checkValidity()) {
      e.preventDefault();
      e.stopPropagation();
      form.classList.add('was-validated');
      return;
    }
    e.preventDefault();

    const fd = new FormData(form); // rating, content, images[]

    try {
      // 1) AJAX로 리뷰 생성 (컨트롤러가 JSON 반환)
      const res = await fetch(form.getAttribute('action'), {
        method: 'POST',
        headers: {
          'X-Requested-With': 'XMLHttpRequest',
          ...(csrfHeader && csrfToken ? { [csrfHeader]: csrfToken } : {})
          // Content-Type 은 FormData 사용 시 자동 지정됩니다.
        },
        body: fd
      });

      if (!res.ok) {
        // 컨트롤러는 400일 때 에러 목록 JSON/텍스트를 내려줄 수 있음
        const text = await res.text();
        throw new Error(text || `HTTP ${res.status}`);
      }

      const data = await res.json();
      if (!data.ok) throw new Error(data.message || '등록 실패');

      // 2) 최신 리뷰 섹션 로드해서 교체
      const oldSection = document.getElementById('reviewsSection');
      const sectionUrl = oldSection?.getAttribute('data-review-section-url');
      if (!oldSection || !sectionUrl) throw new Error('섹션 로드 URL을 찾을 수 없습니다.');
      const secRes = await fetch(sectionUrl, {
        headers: { 'X-Requested-With': 'XMLHttpRequest' }
      });
      if (!secRes.ok) throw new Error(`섹션 로드 실패: HTTP ${secRes.status}`);

      const html = await secRes.text();
      const wrapper = document.createElement('div');
      wrapper.innerHTML = html.trim();

      const newSection = wrapper.querySelector('#reviewsSection');
      if (!newSection || !oldSection) throw new Error('섹션 엘리먼트를 찾을 수 없습니다.');

      oldSection.replaceWith(newSection);

      // 3) 폼 리셋
      form.reset();
      form.classList.remove('was-validated');
      if (ratingInputs.length) {
        syncRating(getCheckedRating());
      } else {
        syncRating(minRating);
      }
      if (cafeId) {
        localStorage.removeItem('certifiedCafe_' + cafeId);
      }
    } catch (err) {
      console.error(err);
      alert('리뷰 등록에 실패했어요. 잠시 후 다시 시도해 주세요.');
    }
  });
})();

