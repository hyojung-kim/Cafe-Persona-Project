// src/main/resources/static/js/cafe-detail.js
// 리뷰 작성 폼 기본 검증 (이미지 URL 최대 5개, 별점 0.5 단위, 내용 5자 이상)
(function(){
  // CSRF
  const CSRF_TOKEN  = document.querySelector('meta[name="_csrf"]')?.content;
  const CSRF_HEADER = document.querySelector('meta[name="_csrf_header"]')?.content;

  // 폼 비동기 제출
  const form = document.getElementById('reviewCreateForm');
  if (form) {
    form.addEventListener('submit', async (e) => {
      e.preventDefault();

      // 입력 값
      const ratingEl   = form.querySelector('input[name="rating"]');
      const contentEl  = form.querySelector('textarea[name="content"]');
      const imageEls   = form.querySelectorAll('input[name="imageUrl"]');

      const rating  = Number(ratingEl?.value ?? NaN);
      const content = (contentEl?.value ?? '').trim();

      // ✅ 클라이언트 유효성 (서버에서도 검증함)
      if (!Number.isFinite(rating) || rating < 1.0 || rating > 5.0 || (Math.round(rating * 10) % 5 !== 0)) {
        alert('별점은 1.0 ~ 5.0 범위의 0.5 단위여야 합니다.');
        ratingEl?.focus();
        return;
      }
      if (content.length < 5) {
        alert('리뷰 내용은 최소 5자 이상 작성해 주세요.');
        contentEl?.focus();
        return;
      }

      // ✅ cafeId: data-attribute 우선, 없으면 경로에서 추출
      const cafeId = form.dataset.cafeId
        || (function(){
             try { return form.action.split('/cafes/')[1].split('/')[0]; } catch { return ''; }
           })();

      // ✅ FormData 구성: 빈 이미지 URL은 제외해서 전송
      const raw = new FormData(form);
      const fd = new FormData();
      // 이미지 외 필드 복사
      raw.forEach((val, key) => {
        if (key !== 'imageUrl') fd.append(key, val);
      });
      // 이미지 URL만 깨끗이 추가
      imageEls.forEach(inp => {
        const v = (inp.value || '').trim();
        if (v) fd.append('imageUrl', v);
      });

      try {
        const res = await fetch(form.action, {
          method: 'POST',
          headers: {
            'X-Requested-With': 'XMLHttpRequest',
            ...(CSRF_HEADER ? { [CSRF_HEADER]: CSRF_TOKEN } : {})
          },
          body: fd
        });

        const data = await res.json().catch(() => ({}));
        if (!res.ok || !data.ok) {
          alert(data.message || '등록 중 오류가 발생했습니다.');
          return;
        }

        // 성공 처리: 폼 리셋 + 섹션 리로드
        form.reset();
        await reloadReviewsSection(cafeId);

      } catch (err) {
        console.error(err);
        alert('네트워크 오류로 등록을 완료하지 못했습니다.');
      }
    });
  }

  // 리뷰 섹션만 교체
  async function reloadReviewsSection(cafeId) {
    const target = document.getElementById('reviewsSection');
    if (!target || !cafeId) return;

    const url  = `/cafe/detail/${cafeId}/reviews/section?rpage=0&rsize=5`;
    const html = await fetch(url, { headers: { 'X-Requested-With': 'XMLHttpRequest' }}).then(r => r.text());

    const temp = document.createElement('div');
    temp.innerHTML = html.trim();

    const newSection = temp.querySelector('#reviewsSection') || temp.firstElementChild;
    if (newSection) {
      target.replaceWith(newSection);
    } else {
      target.innerHTML = html;
    }
  }
})();
