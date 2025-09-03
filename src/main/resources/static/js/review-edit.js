(function () {
  // 글자수 카운터
  var content = document.getElementById('content');
  var counter = document.getElementById('contentCounter');
  function updateCounter() {
    if (!content || !counter) return;
    var len = (content.value || '').trim().length;
    counter.textContent = len + '자 / 최소 50자';
    counter.style.color = (len >= 50) ? 'var(--ok)' : 'var(--muted)';
  }
  if (content && counter) {
    content.addEventListener('input', updateCounter);
    updateCounter();
  }

  // 이미지 URL 미리보기
  var grid = document.getElementById('previewGrid');
  var inputs = document.querySelectorAll('input[name="imageUrl"]');

  function renderPreview() {
    if (!grid) return;
    grid.innerHTML = '';
    inputs.forEach(function (inp) {
      var url = (inp.value || '').trim();
      var box = document.createElement('div');
      if (url) {
        var img = document.createElement('img');
        img.className = 'rev-thumb';
        img.src = url;
        img.alt = 'preview';
        img.onerror = function () {
          // 잘못된 URL이면 플레이스홀더 표시
          img.remove();
          var empty = document.createElement('div');
          empty.className = 'rev-thumb';
          empty.style.display = 'flex';
          empty.style.alignItems = 'center';
          empty.style.justifyContent = 'center';
          empty.style.color = '#9ca3af';
          empty.textContent = 'Invalid URL';
          box.appendChild(empty);
        };
        box.appendChild(img);
      } else {
        var empty = document.createElement('div');
        empty.className = 'rev-thumb';
        empty.style.display = 'flex';
        empty.style.alignItems = 'center';
        empty.style.justifyContent = 'center';
        empty.style.color = '#9ca3af';
        empty.textContent = 'No Image';
        box.appendChild(empty);
      }
      grid.appendChild(box);
    });
  }

  if (inputs && inputs.length) {
    inputs.forEach(function (inp) { inp.addEventListener('input', renderPreview); });
    renderPreview();
  }
})();
