function submitWithPageReset() {
    // 필터 바꾸면 페이지는 0으로
    const pageEl = document.getElementById('page');
    if (pageEl) pageEl.value = 0;
    document.getElementById('searchForm').submit();
  }

  function removeFilter(field) {
    const el = document.getElementById(field);
    if (!el) return;

    // 문자열/선택값 → 빈값
    // boolean(체크박스 파생) → 'false'로 통일(백엔드 파싱에 맞춰 필요시 ''로)
    if (field === 'parking' || field === 'openNow') {
      el.value = ''; // 또는 'false' (컨트롤러에서 Boolean 파싱 규칙에 맞추세요)
    } else {
      el.value = '';
    }
    submitWithPageReset();
  }

  function resetFilters() {
    const ids = ['kw','city','district','sort','dir','parking','openNow'];
    ids.forEach(id => {
      const el = document.getElementById(id);
      if (el) el.value = '';
    });
    submitWithPageReset();
  }