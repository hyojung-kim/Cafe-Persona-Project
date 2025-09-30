console.log("✅ my-bookmark.js 로드됨");

$(document).ready(function() {
    $('.btn-remove-bookmark').on('click', function() {
        const cafeId = $(this).data('cafe-id');
        const listItem = $(this).closest('li');

        // CSRF 토큰
        const csrfToken  = $('meta[name="_csrf"]').attr('content');
        const csrfHeader = $('meta[name="_csrf_header"]').attr('content');

        if (confirm('정말 북마크를 삭제하시겠어요?')) {
            $.ajax({
                url: '/mypage/my/bookmark/remove',
                type: 'POST',
                data: { cafeId: cafeId },
                headers: csrfToken && csrfHeader ? { [csrfHeader]: csrfToken } : {},
                success: function(response) {
                    //  성공 시 DOM에서 해당 항목 제거
                    listItem.remove();

                    // 목록이 비었는지 확인 후 메시지 표시 등 추가 처리 (선택)
//                    const ulList = $('#bookmark-list'); // 새로 부여한 ID 사용
                    alert("북마크가 삭제되었습니다.");
                    window.location.href = '/mypage/my/bookmark';

                    //  다른 페이지 상태 업데이트를 위해 ID 저장
                    let deletedIds = sessionStorage.getItem('deletedBookmarks');
                    deletedIds = deletedIds ? JSON.parse(deletedIds) : [];
                    deletedIds.push(cafeId);
                    sessionStorage.setItem('deletedBookmarks', JSON.stringify(deletedIds));
                },
                error: function(xhr) {
                    alert('북마크 삭제 중 오류가 발생했습니다.');
                    console.error('삭제 오류:', xhr.responseText);
                }
            });
        }
    });
});

//document.querySelectorAll(".bookmark-remove-form .btn-remove-bookmark").forEach(btn => {
//  btn.addEventListener("click", function () {
//    const form = this.closest(".bookmark-remove-form");
//    const cafeId = form.dataset.cafeId;
//
//    fetch(`/mypage/my/bookmark/remove?cafeId=${cafeId}`, { method: "POST" })
//      .then(res => res.json())
//      .then(data => {
//        if (data.success) {
//          // 1) 북마크 목록에서 제거
//          document.querySelector(`#bookmark-${data.cafeId}`)?.remove();
//
//          // 2) 상세페이지 토글 OFF + 카운트 갱신
//          if (window.setBookmarkOff) {
//            window.setBookmarkOff(data.cafeId, data.count);
//          }
//        }
//      })
//      .catch(err => {
//        console.error("북마크 삭제 실패:", err);
//        alert("북마크 삭제 중 오류가 발생했습니다.");
//      });
//  });
//});