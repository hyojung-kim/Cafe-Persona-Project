// review-edit.js
(function () {
  var MAX_IMAGES = 5;

  // ===== 글자수 카운터 =====
  var content = document.getElementById('content');
  var counter = document.getElementById('contentCounter');
  if (content && counter) {
    var updateCount = function () {
      var len = (content.value || '').trim().length;
      counter.textContent = len + '자 / 최소 5자';
      counter.style.color = (len >= 5) ? '#10b981' : '#6b7280';
    };
    content.addEventListener('input', updateCount);
    updateCount();
  }

  // ===== 이미지 URL/파일 합산 미리보기 및 개수 제한 =====
  var grid = document.getElementById('previewGrid');
  var urlInputs = Array.prototype.slice.call(document.querySelectorAll('input[name="imageUrl"]'));
  var fileInput = document.querySelector('input[type="file"][name="images"]');
  var form = document.getElementById('reviewEditForm');
  var submitBtn = form ? form.querySelector('button[type="submit"]') : null;

  // 간단 플레이스홀더 박스 생성
  function placeholderBox(text) {
    var empty = document.createElement('div');
    empty.className = 'rev-thumb';
    empty.style.display = 'flex';
    empty.style.alignItems = 'center';
    empty.style.justifyContent = 'center';
    empty.style.color = '#9ca3af';
    empty.style.border = '1px dashed #e5e7eb';
    empty.textContent = text || 'No Image';
    return empty;
  }

  function imgThumb(src) {
    var img = document.createElement('img');
    img.className = 'rev-thumb';
    img.src = src;
    img.alt = 'preview';
    img.loading = 'lazy';
    img.referrerPolicy = 'no-referrer';
    img.onerror = function () {
      // 로드 실패 시 플레이스홀더로 교체
      var parent = img.parentNode;
      if (parent) parent.replaceChild(placeholderBox('Load Error'), img);
    };
    return img;
  }

  function currentUrlValues() {
    // 비어있지 않은 URL만 카운트/미리보기 대상으로
    return urlInputs
      .map(function (inp) { return (inp.value || '').trim(); })
      .filter(function (v) { return v.length > 0; });
  }

  function currentFiles() {
    return (fileInput && fileInput.files) ? Array.from(fileInput.files) : [];
  }

  function renderPreview() {
    if (!grid) return;

    var urls = currentUrlValues();
    var files = currentFiles();

    grid.innerHTML = '';

    // URL 미리보기
    urls.forEach(function (u) {
      var box = document.createElement('div');
      box.appendChild(imgThumb(u));
      grid.appendChild(box);
    });

    // 파일 미리보기
    files.forEach(function (f) {
      var box = document.createElement('div');
      try {
        var objUrl = URL.createObjectURL(f);
        var img = imgThumb(objUrl);
        img.onload = function () { URL.revokeObjectURL(objUrl); };
        img.onerror = function () { URL.revokeObjectURL(objUrl); };
        box.appendChild(img);
      } catch (e) {
        box.appendChild(placeholderBox('File'));
      }
      grid.appendChild(box);
    });

    // 부족한 칸은 플레이스홀더(선택)
    var total = urls.length + files.length;
    for (var i = total; i < Math.min(MAX_IMAGES, 5); i++) {
      grid.appendChild(placeholderBox('Empty'));
    }

    // 제한 체크
    var over = total > MAX_IMAGES;
    if (submitBtn) submitBtn.disabled = over;
    if (form) {
      if (over) {
        form.setAttribute('data-over-limit', 'true');
      } else {
        form.removeAttribute('data-over-limit');
      }
    }
  }

  // URL 입력 변경 시 미리보기 갱신
  urlInputs.forEach(function (inp) {
    inp.addEventListener('input', renderPreview);
  });

  // 파일 변경 시 미리보기 갱신 + 즉시 제한 안내
  if (fileInput) {
    fileInput.addEventListener('change', function () {
      var urls = currentUrlValues();
      var files = currentFiles();
      if (urls.length + files.length > MAX_IMAGES) {
        // 초과 시 안내하고 제출은 막되, 사용자가 파일 선택을 줄이도록 유도
        alert('이미지는 URL과 파일 합쳐 최대 ' + MAX_IMAGES + '장까지 가능합니다.');
      }
      renderPreview();
    });
  }

  // 제출 시 최종 제한 + 글자수 검사
  if (form) {
    form.addEventListener('submit', function (e) {
      if (!form.checkValidity()) {
        e.preventDefault();
        e.stopPropagation();
        form.classList.add('was-validated');
        return;
      }

      var total = currentUrlValues().length + currentFiles().length;
      if (total > MAX_IMAGES) {
        e.preventDefault();
        e.stopPropagation();
        alert('이미지는 최대 ' + MAX_IMAGES + '장까지만 업로드할 수 있어요.');
        return;
      }
    });
  }

  // 초기 렌더
  renderPreview();
})();

