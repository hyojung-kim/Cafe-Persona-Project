// /static/js/cafe-like.js
$(function () {
  const box = $('#likeBox');
  if (!box.length) return;

  const cafeId  = box.data('cafe-id');
  const btn     = $('#likeBtn');
  const countEl = $('#likeCount');
  const badgeEl = $('#bookmark-badge'); // 있으면 토글, 없으면 무시

  // CSRF (Thymeleaf 메타태그 사용 시)
  const csrfToken  = $('meta[name="_csrf"]').attr('content');
  const csrfHeader = $('meta[name="_csrf_header"]').attr('content');

  function setLikedUI(liked) {
    box.attr('data-liked', liked ? 'true' : 'false');
    btn.html(
      liked
        ? '<span class="label-liked">♥ 좋아요 취소</span>'
        : '<span class="label-unliked">♡ 좋아요</span>'
    );
  }

  btn.on('click', function () {
    btn.prop('disabled', true);

    $.ajax({
      url: "/cafe/like/" + cafeId,
      type: "POST",          // 상태 변경 → POST
      dataType: "json",      // 응답을 JSON으로 받음 {count, liked}
      headers: csrfToken && csrfHeader ? { [csrfHeader]: csrfToken } : {},
      cache: false
    })
    .done(function (res) {
      console.log("JSON 응답", res); // {count: 12, liked: true, bookmarked: true}
      countEl.text(String(res.count));
      setLikedUI(!!res.liked);
      badgeEl.toggleClass('hidden', !res.bookmarked);
    })
    .fail(function (xhr) {
      if (xhr.status === 401 || xhr.status === 403) {
        window.location.href = '/user/login';
      } else {
        console.error('좋아요 오류:', xhr.status, xhr.responseText);
        alert('좋아요 처리 중 오류가 발생했어요.');
      }
    })
    .always(function () {
      btn.prop('disabled', false);
    });
  });
});