// /js/review-like.js
$(function () {
  const csrfToken  = $('meta[name="_csrf"]').attr('content');
  const csrfHeader = $('meta[name="_csrf_header"]').attr('content');

  function setLikedUI(box, btn, liked) {
    box.attr('data-liked', liked ? 'true' : 'false');
    btn.html(
      liked ? '<span class="label-liked">♥ 좋아요 취소</span>'
            : '<span class="label-unliked">♡ 좋아요</span>'
    );
  }

  $(document).on('click', '.review-like-btn', function () {
    const btn = $(this);
    const box = btn.closest('.review-like-box');
    const reviewId = box.data('review-id');
    const countEl = box.find('.review-like-count');

    btn.prop('disabled', true);

    $.ajax({
      url: '/reviews/' + reviewId + '/like',
      type: 'POST',
      dataType: 'json',
      headers: csrfToken && csrfHeader ? { [csrfHeader]: csrfToken } : {},
      cache: false
    })
    .done(function (res) {
      countEl.text(String(res.count));
      setLikedUI(box, btn, !!res.liked);
    })
    .fail(function (xhr) {
      if (xhr.status === 401 || xhr.status === 403) {
        window.location.href = '/user/login';
      } else {
        console.error('리뷰 좋아요 오류:', xhr.status, xhr.responseText);
        alert('좋아요 처리 중 오류가 발생했어요.');
      }
    })
    .always(function () {
      btn.prop('disabled', false);
    });
  });
});
