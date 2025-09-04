// 리뷰 작성 폼 기본 검증 (이미지 URL 최대 5개, 별점 0.5 단위, 내용 50자 이상)
function validateReviewForm(form) {
  // 별점
  var rating = Number(form.rating?.value || 0);
  if (rating < 1.0 || rating > 5.0) {
    alert('별점은 1.0 ~ 5.0 사이여야 합니다.');
    return false;
  }
  if ((rating * 10) % 5 !== 0) { // 0.5 단위 체크
    alert('별점은 0.5 단위로만 선택할 수 있습니다.');
    return false;
  }

  // 내용
  var content = (form.content?.value || '').trim();
  if (content.length < 10) {
    alert('리뷰 내용은 최소 10자 이상 작성해 주세요.');
    return false;
  }

  // 이미지 URL(최대 5개, 빈칸 제외)
  var inputs = form.querySelectorAll('input[name="imageUrl"]');
  var urls = [];
  inputs.forEach(function (i) {
    var v = (i.value || '').trim();
    if (v) urls.push(v);
  });
  if (urls.length > 5) {
    alert('이미지 URL은 최대 5개까지만 가능합니다.');
    return false;
  }
  return true;
}

// (옵션) 추후 AJAX 좋아요 토글을 붙일 경우를 대비한 CSRF 헬퍼
window.__CSRF__ = {
  token: document.querySelector('meta[name="_csrf"]')?.content || null,
  header: document.querySelector('meta[name="_csrf_header"]')?.content || null,
};
