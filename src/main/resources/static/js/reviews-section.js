// /js/reviews-section.js
(function () {
  // === 하드닝: 현재 페이지 URL을 잘못 로드 중인 link/script 제거 (MIME 오류 즉시 방어) ===
  (function harden(){
    var path = window.location.pathname; // 예: /cafes/21/reviews
    document.querySelectorAll('link[rel="stylesheet"],script[src]').forEach(function (el) {
      var url = el.getAttribute('href') || el.getAttribute('src');
      if (!url) return;
      try { url = new URL(url, location.origin).pathname; } catch (_) {}
      if (url === path) {
        console.warn('Removing bad resource tag ->', el.outerHTML);
        el.remove();
      }
    });
  })();

  // === CSRF ===
  function getCookie(name){
    const m=document.cookie.match('(?:^|;\\s*)'+name+'=([^;]+)');
    return m?decodeURIComponent(m[1]):null;
  }
  function resolveCsrf(form){
    let t=document.querySelector('meta[name="_csrf"]')?.content||null;
    let h=document.querySelector('meta[name="_csrf_header"]')?.content||null;
    if(!t){ t=getCookie('XSRF-TOKEN'); h=h||'X-XSRF-TOKEN'; }
    if(!t&&form){
      const hid=form.querySelector('input[name="_csrf"]');
      if(hid){ t=hid.value; h=h||'X-CSRF-TOKEN'; }
    }
    return { token:t, header:h };
  }

  // === 섹션/URL ===
  var section = document.getElementById('reviewsSection');
  var sectionUrl = section?.getAttribute('data-review-section-url') || null;

  // === 프래그먼트 정리 (link/script 제거) ===
  function sanitizeFragment(html){
    var doc=new DOMParser().parseFromString(html,'text/html');
    doc.querySelectorAll('link[rel="stylesheet"],script').forEach(function (el){ el.remove(); });
    return doc.getElementById('reviewsSection') || doc.body;
  }
  function replaceSectionWith(node){
    if(!section) return;
    if(node.id==='reviewsSection'){
      section.replaceWith(node);
      section=document.getElementById('reviewsSection');
    } else {
      section.innerHTML=node.innerHTML;
    }
  }
  async function reloadSection(url){
    if(!section||!url) return;
    const res=await fetch(url,{ headers:{'X-Requested-With':'XMLHttpRequest'} });
    const html=await res.text();
    const safe=sanitizeFragment(html);
    replaceSectionWith(safe);
    bindPagination();
  }

  // === 페이지네이션 (클릭 가로채서 AJAX) ===
  function bindPagination(){
    if(!section) return;
    const nav=section.querySelector('.review-pagination, nav .pagination, .pagination');
    if(!nav||nav.__bound) return; nav.__bound=true;
    nav.addEventListener('click', function(e){
      const a=e.target.closest('a'); if(!a) return;
      const href=a.getAttribute('href'); if(!href) return;
      e.preventDefault(); reloadSection(href);
    });
  }

  // === 리뷰 작성 폼 AJAX ===
  (function(){
    const form=document.getElementById('reviewCreateForm'); if(!form) return;
    form.addEventListener('submit', async function(e){
      if(!form.checkValidity()){
        e.preventDefault(); e.stopPropagation();
        form.classList.add('was-validated');
        return;
      }
      e.preventDefault();

      const {token, header}=resolveCsrf(form);
      const fd=new FormData(form);
      try{
        const res=await fetch(form.getAttribute('action'),{
          method:'POST',
          headers:{ 'X-Requested-With':'XMLHttpRequest', ...(token&&header?{[header]:token}:{}) },
          body:fd
        });
        if(!res.ok){ throw new Error(await res.text()||('HTTP '+res.status)); }

        const data=await res.json();
        if(!data.ok) throw new Error('등록 실패');

        // 성공 → 섹션 새로고침
        if(section && sectionUrl){ await reloadSection(sectionUrl); }

        // 폼 리셋
        form.reset(); form.classList.remove('was-validated');
        const rating=form.querySelector('#rating'); if(rating) rating.value='4.0';
      }catch(err){
        console.error('review submit error:', err);
        alert('리뷰 등록에 실패했습니다. 잠시 후 다시 시도해 주세요.');
      }
    });
  })();

  // 최초 바인딩
  bindPagination();
})();
