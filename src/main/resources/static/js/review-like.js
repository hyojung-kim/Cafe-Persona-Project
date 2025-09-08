// /js/review-like.js
(function(){
  // CSRF 메타에서 읽기
  const csrfToken  = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

  function parseResponse(r) {
    const ct = r.headers.get('content-type') || '';
    if (ct.includes('application/json')) return r.json();
    return r.text().then(t => ({ ok: r.ok, html: t }));
  }

  function updateUi(form, payload) {
    try {
      const box = form.parentElement;
      const countEl = box.querySelector('.like-count');
      if (typeof payload.count === 'number' && countEl) {
        countEl.textContent = String(payload.count);
      }
      const btn = form.querySelector('.like-btn');
      if (btn && typeof payload.liked === 'boolean') {
        btn.classList.toggle('btn-success', payload.liked);
        btn.classList.toggle('btn-outline-success', !payload.liked);
      }
    } catch (_) {}
  }

  async function toggleLike(form) {
    // FormData에 CSRF 파라미터도 함께 넣기 (서버 설정이 헤더/파라미터 둘 중 하나만 허용해도 통과)
    const fd = new FormData();
    const hiddenCsrf = form.querySelector('input[name="_csrf"]');
    if (hiddenCsrf && hiddenCsrf.value) fd.append(hiddenCsrf.name, hiddenCsrf.value);

    const headers = { 'X-Requested-With': 'XMLHttpRequest' };
    if (csrfHeader && csrfToken) headers[csrfHeader] = csrfToken;

    let resp;
    try {
      resp = await fetch(form.action, { method: 'POST', headers, body: fd, redirect: 'follow' });
    } catch (e) {
      // 네트워크 오류 → 폴백: 일반 제출
      form.submit();
      return;
    }

    if (resp.status === 401 || resp.status === 403) {
      // 인증/CSRF 실패 → 폴백
      form.submit();
      return;
    }

    const data = await parseResponse(resp);

    // JSON 성공
    if (data && data.ok && typeof data.count !== 'undefined') {
      updateUi(form, data);
      return;
    }

    // JSON이 아니거나 실패 → 폴백
    form.submit();
  }

  function onSubmit(e){
    const form = e.target.closest('form.review-like-form');
    if(!form) return;
    e.preventDefault();
    toggleLike(form);
  }

  // 프래그먼트 교체 후에도 동작하도록 이벤트 위임
  document.addEventListener('submit', function(e){
    if (e.target.matches('form.review-like-form')) onSubmit(e);
  });
})();

