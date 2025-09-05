(function(){
  // 글자수 카운터
  var content = document.getElementById('content');
  var counter = document.getElementById('contentCounter');
  if (content && counter) {
    var update = function(){
      var len = (content.value || '').trim().length;
      counter.textContent = len + '자 / 최소 50자';
      counter.style.color = (len >= 50) ? '#10b981' : '#6b7280';
    };
    content.addEventListener('input', update);
    update();
  }

  // 이미지 URL 미리보기
  var grid = document.getElementById('previewGrid');
  if (grid) {
    var inputs = document.querySelectorAll('input[name="imageUrl"]');
    var render = function(){
      grid.innerHTML = '';
      inputs.forEach(function(inp){
        var url = (inp.value || '').trim();
        var box = document.createElement('div');
        if (url) {
          var img = document.createElement('img');
          img.className = 'rev-thumb';
          img.src = url;
          img.alt = 'preview';
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
    };
    inputs.forEach(function(inp){ inp.addEventListener('input', render); });
    render();
  }
})();
