// /js/reviews-section.js
(function () {
  var box = document.getElementById('reviewsSection');
  if (!box) return;

  var baseUrl = box.getAttribute('data-review-section-url');
  if (!baseUrl) return;

  function load(url) {
    fetch(url, { headers: { 'X-Requested-With': 'XMLHttpRequest' } })
      .then(function (r) { return r.text(); })
      .then(function (html) {
        // 프래그먼트 전체 교체
        var wrap = document.createElement('div');
        wrap.innerHTML = html;
        var next = wrap.querySelector('#reviewsSection');
        if (next) box.replaceWith(next);
      })
      .catch(function () {
        box.innerHTML = '<div class="card-body"><p class="text-danger m-0">리뷰를 불러오지 못했습니다.</p></div>';
      });
  }

  // 페이지네이션 위임
  document.addEventListener('click', function (e) {
    var a = e.target.closest('.review-pagination a');
    if (!a) return;
    var href = a.getAttribute('href');
    if (!href) return;
    e.preventDefault();
    load(href);
  });
})();
