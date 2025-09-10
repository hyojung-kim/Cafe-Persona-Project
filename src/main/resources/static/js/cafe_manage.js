// /static/js/cafe_manage.js
(function () {
  console.log("[cafe_manage] LOADED", new Date().toISOString(), location.pathname, location.search);

  // 브라우저 네비게이션 타입이 back_forward면 즉시 리로드 (크롬/파폭)
  try {
    var nav = performance.getEntriesByType('navigation')[0];
    if (nav && nav.type === 'back_forward') {
      console.log("[cafe_manage] back_forward detected → reload");
      location.reload();
      return;
    }
  } catch (_) {}

  window.addEventListener('pageshow', function (e) {
    console.log("[cafe_manage] pageshow", { persisted: e.persisted, flag: sessionStorage.getItem('force-reload') });
    if (e.persisted || sessionStorage.getItem('force-reload') === '1') {
      sessionStorage.removeItem('force-reload');
      location.reload();
    }
  });

  window.addEventListener('pagehide', function (e) {
    console.log("[cafe_manage] pagehide", { persisted: e.persisted });
    if (e.persisted) {
      sessionStorage.setItem('force-reload', '1');
    }
  });

  // 진입 직후 reauth 파라미터 제거 (복붙 위험 축소)
  (function cleanUrl() {
    try {
      var u = new URL(window.location.href);
      if (u.searchParams.has('reauth')) {
        u.searchParams.delete('reauth');
        var q = u.searchParams.toString();
        var newUrl = u.pathname + (q ? "?" + q : "") + u.hash;
        history.replaceState(null, '', newUrl);
        console.log("[cafe_manage] removed reauth from URL");
      }
    } catch (err) {
      console.error("[cafe_manage] URL cleanup error:", err);
    }
  })();
})();
