// 리뷰 작성 폼 기본 검증 (이미지 URL 최대 5개, 별점 0.5 단위, 내용 50자 이상)
(function(){
  // CSRF
  const CSRF_TOKEN = document.querySelector('meta[name="_csrf"]')?.content;
  const CSRF_HEADER = document.querySelector('meta[name="_csrf_header"]')?.content;

  // 폼 비동기 제출
  const form = document.getElementById('reviewCreateForm');
  if (form) {
    form.addEventListener('submit', async (e) => {
      e.preventDefault();

      // 간단 유효성 (서버에서도 검증함)
      const rating = form.querySelector('input[name="rating"]').value;
      const content = form.querySelector('textarea[name="content"]').value.trim();
      if (!content || content.length < 5) {
        alert('리뷰 내용은 최소 5자 이상 작성해 주세요.');
        return;
      }
      const cafeId = form.action.split('/cafes/')[1].split('/')[0];

      // FormData → 그대로 전송 (ModelAttribute 바인딩용)
      const fd = new FormData(form);

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
          const msg = data.message || '등록 중 오류가 발생했습니다.';
          alert(msg);
          return;
        }

        // 폼 비우기
        form.reset();

        // ✅ 리뷰 섹션만 다시 로드
        await reloadReviewsSection(cafeId);

      } catch (err) {
        console.error(err);
        alert('네트워크 오류로 등록을 완료하지 못했습니다.');
      }
    });
  }

  async function reloadReviewsSection(cafeId) {
    const target = document.getElementById('reviewsSection');
    if (!target) return;
    // 첫 페이지 새로고침 (원하면 현재 페이지 유지 로직으로 바꿔도 됨)
    const url = `/cafe/detail/${cafeId}/reviews/section?rpage=0&rsize=5`;
    const html = await fetch(url, { headers: { 'X-Requested-With': 'XMLHttpRequest' }}).then(r => r.text());
    // 받는 건 <section ...> 프래그먼트이므로 바깥을 그대로 교체
    const temp = document.createElement('div');
    temp.innerHTML = html.trim();
    const newSection = temp.querySelector('#reviewsSection') || temp.firstElementChild;
    if (newSection) {
      target.replaceWith(newSection);
    } else {
      // 서버가 프래그먼트를 순수 body 없이 반환하는 경우를 대비
      target.innerHTML = html;
    }
  }
})();
