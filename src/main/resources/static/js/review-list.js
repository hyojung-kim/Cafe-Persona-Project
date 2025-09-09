// /js/cafe-list.js
(function () {
  const form = document.getElementById('reviewCreateForm');
  if (!form) return;

  const csrfToken  = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

  form.addEventListener('submit', async (e) => {
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
    } catch (err) {
      console.error(err);
      alert('리뷰 등록에 실패했어요. 잠시 후 다시 시도해 주세요.');
    }
  });
})();

