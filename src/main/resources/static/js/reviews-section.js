// /js/reviews-section.js
(function () {
  var box = document.getElementById('reviews-section');
  if (!box) return;

  var baseUrl = box.getAttribute('data-review-section-url');
  if (!baseUrl) return;

  function load(url) {
    fetch(url, { headers: { 'X-Requested-With': 'XMLHttpRequest' } })
      .then(function (r) { return r.text(); })
      .then(function (html) {
        box.innerHTML = html;
        bindPagination();
      })
      .catch(function () {
        box.innerHTML = '<p>리뷰를 불러오지 못했습니다.</p>';
      });
  }

  function bindPagination() {
    var nav = box.querySelector('.review-pagination');
    if (!nav) return;

    nav.addEventListener('click', function (e) {
      var a = e.target.closest('a');
      if (!a) return;
      var href = a.getAttribute('href');
      if (!href) return;

      e.preventDefault();
      load(href);
    });
  }

  // 최초 1회 로드
  load(baseUrl);
})();
