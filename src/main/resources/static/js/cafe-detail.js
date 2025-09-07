// /static/js/cafe-detail.js
(function () {
  const form = document.getElementById('reviewCreateForm');
  const section = document.getElementById('reviewsSection');
  if (!form || !section) return;

  // CSRF 메타
  const CSRF_TOKEN = document.querySelector('meta[name="_csrf"]')?.content;
  const CSRF_HEADER = document.querySelector('meta[name="_csrf_header"]')?.content;

  // 카페 id (템플릿에서 data-cafe-id 세팅됨)
  const cafeId = form.getAttribute('data-cafe-id');

  // 간단 토스트 (Bootstrap alert)
  function toast(msg, kind = 'info') {
    const div = document.createElement('div');
    div.className = `alert alert-${kind} position-fixed top-0 start-50 translate-middle-x mt-3 shadow`;
    div.style.zIndex = 1080;
    div.textContent = msg;
    document.body.appendChild(div);
    setTimeout(() => div.remove(), 1800);
  }

  // 부트스트랩 폼 검증 표시
  (function enableBootstrapValidation() {
    form.addEventListener('submit', (event) => {
      if (!form.checkValidity()) {
        event.preventDefault();
        event.stopPropagation();
      }
      form.classList.add('was-validated');
    }, false);
  })();

  // 리뷰 섹션 프래그먼트 새로고침
  async function refreshReviews(page = 0, size = 5) {
    const url = `/cafe/detail/${encodeURIComponent(cafeId)}/reviews/section?rpage=${page}&rsize=${size}`;
    const res = await fetch(url, { headers: { 'X-Requested-With': 'XMLHttpRequest' } });
    if (!res.ok) throw new Error('리뷰 섹션을 불러오지 못했습니다.');
    const html = await res.text();

    // 새 섹션으로 교체
    const temp = document.createElement('div');
    temp.innerHTML = html.trim();
    const newSection = temp.querySelector('#reviewsSection') || temp.firstElementChild;
    if (newSection) section.replaceWith(newSection);
  }

  // 폼 제출 → AJAX
  form.addEventListener('submit', async (e) => {
    e.preventDefault();

    // HTML5 검증 미통과 시 중단
    if (!form.checkValidity()) return;

    try {
      const fd = new FormData(form); // rating, content, images(File[]), (선택) imageUrl[]

      // AJAX 라우트 태우기 위한 헤더 + CSRF
      const headers = { 'X-Requested-With': 'XMLHttpRequest' };
      if (CSRF_HEADER && CSRF_TOKEN) headers[CSRF_HEADER] = CSRF_TOKEN;

      const res = await fetch(form.action, {
        method: 'POST',
        headers,
        body: fd
      });

      if (!res.ok) {
        let message = '등록에 실패했습니다.';
        try {
          const err = await res.json();
          message = err?.message || message;
        } catch (_) {}
        toast(message, 'danger');
        return;
      }

      const data = await res.json();
      if (!data.ok) {
        toast(data.message || '등록에 실패했습니다.', 'danger');
        return;
      }

      // 성공: 폼 리셋, 검증 초기화, 섹션 새로고침
      form.reset();
      form.classList.remove('was-validated');
      await refreshReviews(0, 5);
      toast('리뷰가 등록되었습니다.', 'success');
    } catch (err) {
      console.error(err);
      toast('네트워크 오류가 발생했습니다.', 'danger');
    }
  });
})();
