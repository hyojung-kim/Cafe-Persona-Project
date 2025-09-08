// src/main/resources/static/js/review-like.js
(function(){
  const csrfToken  = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

  async function toggle(form) {
    const fd = new FormData();
    const hidden = form.querySelector('input[name="_csrf"]');
    if (hidden?.value) fd.append(hidden.name, hidden.value);

    const headers = { 'X-Requested-With': 'XMLHttpRequest' };
    if (csrfHeader && csrfToken) headers[csrfHeader] = csrfToken;

    let resp;
    try {
      resp = await fetch(form.action, { method: 'POST', headers, body: fd });
    } catch {
      form.submit(); // 네트워크 이슈 → 폴백
      return;
    }

    if (resp.status === 401 || resp.status === 403) { form.submit(); return; }
    const data = await resp.json().catch(()=>null);
    if (!data || !data.ok) { form.submit(); return; }

    // UI 갱신
    const box = form.closest('[data-review-box]');
    const btn = form.querySelector('.like-btn');
    const cnt = box?.querySelector('.like-count');
    if (btn) {
      if (data.liked) {
        btn.classList.remove('btn-outline-success');
        btn.classList.add('btn-success');
      } else {
        btn.classList.add('btn-outline-success');
        btn.classList.remove('btn-success');
      }
    }
    if (cnt) cnt.textContent = String(data.count);
  }

  document.addEventListener('submit', function(e){
    const form = e.target.closest('form.review-like-form');
    if (!form) return;
    e.preventDefault();
    toggle(form);
  });
})();
