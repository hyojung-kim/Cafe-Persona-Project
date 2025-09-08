// review-detail.js
(function () {
  // ===== 공통(CSRF) =====
  const csrfToken  = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

  // ===== 좋아요 / 취소 AJAX (낙관적 업데이트) =====
  const likeForm   = document.querySelector('form[action$="/like"]');
  const unlikeForm = document.querySelector('form[action$="/unlike"]');
  const likeCountEl = document.querySelector('[th\\:text="${review.likeCount}"], .like-count, b[th\\:text="${review.likeCount}"]')
                      || document.querySelector('.card .text-muted b'); // 백업 선택자
  let likeCount = likeCountEl ? parseInt(likeCountEl.textContent, 10) || 0 : 0;

  async function postForm(formEl) {
   const res = await fetch(formEl.getAttribute('action'), {
      method: 'POST',
      headers: {
        'X-Requested-With': 'XMLHttpRequest',
        ...(csrfHeader && csrfToken ? { [csrfHeader]: csrfToken } : {})
      },
      // 폼은 비어있지만, 표준 폼 전송과 동일하게 본문 없이 보냄 (CSRF 헤더만)
    });
    if (!res.ok) {
      // 컨트롤러가 AJAX 분기 없지만, redirect 결과(200) 또는 오류 코드만 온다
      const text = await res.text().catch(()=>'');
      throw new Error(text || `HTTP ${res.status}`);
    }
  }

  function updateLikeCount(show) {
    if (likeCountEl) likeCountEl.textContent = String(show);
  }

  if (likeForm) {
    likeForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const prev = likeCount;
      // 낙관적 +1
      likeCount = prev + 1;
      updateLikeCount(likeCount);
      disableLikeButtons(true);
      try {
        await postForm(likeForm);
      } catch (err) {
        // 실패 시 롤백
        likeCount = prev;
        updateLikeCount(likeCount);
        alert('좋아요 처리에 실패했어요. 잠시 후 다시 시도해 주세요.');
      } finally {
        disableLikeButtons(false);
      }
    });
  }

  if (unlikeForm) {
    unlikeForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const prev = likeCount;
      // 낙관적 -1 (하한 0)
      likeCount = Math.max(0, prev - 1);
      updateLikeCount(likeCount);
      disableLikeButtons(true);
      try {
        await postForm(unlikeForm);
      } catch (err) {
        // 실패 시 롤백
        likeCount = prev;
        updateLikeCount(likeCount);
        alert('좋아요 취소에 실패했어요. 잠시 후 다시 시도해 주세요.');
      } finally {
        disableLikeButtons(false);
      }
    });
  }

  function disableLikeButtons(disabled) {
    likeForm?.querySelector('button[type="submit"]')?.toggleAttribute('disabled', disabled);
    unlikeForm?.querySelector('button[type="submit"]')?.toggleAttribute('disabled', disabled);
  }

  // ===== 라이트박스(모달) =====
  // 이미지 섹션의 썸네일을 클릭하면 큰 이미지 모달로 표시
  const imageContainer = document.querySelector('section.card img[alt="review image"]')
                        ? document : null;

  // 모달 엘리먼트 생성
  const lb = document.createElement('div');
  lb.style.cssText = [
    'position:fixed','inset:0','display:none','align-items:center','justify-content:center',
    'background:rgba(0,0,0,.7)','z-index:1050','padding:24px'
  ].join(';');
  lb.innerHTML = `
    <div style="
      max-width: min(100%, 1080px);
      max-height: 90vh;
      display:flex;align-items:center;justify-content:center;
      position:relative;">
      <img id="lb-img" src="" alt="image" style="max-width:100%;max-height:90vh;border-radius:10px;box-shadow:0 10px 30px rgba(0,0,0,.3)"/>
      <button id="lb-close" aria-label="닫기" style="
        position:absolute;top:-10px;right:-10px;
        background:#111;color:#fff;border:none;border-radius:999px;
        width:36px;height:36px;cursor:pointer;font-size:18px;">×</button>
    </div>`;
  document.body.appendChild(lb);
  const lbImg = lb.querySelector('#lb-img');
  const lbClose = lb.querySelector('#lb-close');

  function openLightbox(src) {
    lbImg.src = src;
    lb.style.display = 'flex';
    document.documentElement.style.overflow = 'hidden';
  }
  function closeLightbox() {
    lb.style.display = 'none';
    lbImg.src = '';
    document.documentElement.style.overflow = '';
  }
  lb.addEventListener('click', (e) => {
    if (e.target === lb || e.target === lbClose) closeLightbox();
  });
  document.addEventListener('keydown', (e) => {
    if (lb.style.display !== 'none' && e.key === 'Escape') closeLightbox();
  });

  if (imageContainer) {
    document.querySelectorAll('section.card img[alt="review image"]').forEach((img) => {
      img.style.cursor = 'zoom-in';
      img.addEventListener('click', () => openLightbox(img.src));
    });
  }
})();
