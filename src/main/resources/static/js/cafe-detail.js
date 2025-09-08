// /js/cafe-detail.js
(function () {
  const form = document.getElementById('reviewCreateForm');
  if (!form) return;

  const csrfToken  = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

  // 부트스트랩 클라이언트 검증(선택)
  form.addEventListener('submit', async (e) => {
    if (!form.checkValidity()) {
      e.preventDefault();
      e.stopPropagation();
      form.classList.add('was-validated');
      return;
    }
    e.preventDefault();

    const fd = new FormData(form); // rating, content, images[] 자동 수집

    try {
      const res = await fetch(form.getAttribute('action'), {
        method: 'POST',
        headers: {
          ...(csrfHeader && csrfToken ? { [csrfHeader]: csrfToken } : {})
          // Content-Type 은 FormData 사용 시 자동으로 boundary 포함되어 지정됩니다. 절대 직접 세팅하지 마세요.
        },
        body: fd
      });

      if (!res.ok) {
        const text = await res.text();
        throw new Error(text || `HTTP ${res.status}`);
      }

      // 서버가 보내주는 프래그먼트 HTML을 #reviewsSection에 교체
      const html = await res.text();
      const wrapper = document.createElement('div');
      wrapper.innerHTML = html.trim();

      const newSection = wrapper.querySelector('#reviewsSection');
      if (!newSection) throw new Error('프래그먼트에 #reviewsSection 이 없습니다.');

      document.getElementById('reviewsSection').replaceWith(newSection);

      // 폼 리셋
      form.reset();
      form.classList.remove('was-validated');
      const rating = form.querySelector('#rating');
      if (rating) rating.value = '4.0';
    } catch (err) {
      console.error(err);
      alert('리뷰 등록에 실패했어요. 잠시 후 다시 시도해 주세요.');
    }
  });
})();
