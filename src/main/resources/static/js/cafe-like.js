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
        ? '<i class="fa-solid fa-bookmark" id="activeBookmark"></i>'
        : '<i class="fa-regular fa-bookmark" id="disableBookmarkIcon"></i>'
    );
  }

  //// 현영 추가
    window.setBookmarkOff = function (cafeIdFromServer, newCount) {
      console.log("setBookmarkOff 실행됨");
      if (cafeIdFromServer == cafeId) {        // 현재 상세페이지와 같은 카페면 반영
        setLikedUI(false);
        if (badgeEl.length) badgeEl.addClass('hidden');
        if (countEl.length) countEl.text(String(newCount));
      }
    };
   //// 현영 추가 끝

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

    // 🔽 북마크 삭제 성공 시 상세페이지 UI를 끄는 함수 추가
//    window.setBookmarkOff = function () {
//      countEl.text(String(Number(countEl.text()) - 1)); // 카운트 -1
//      setLikedUI(false);                               // 버튼 OFF
//      badgeEl.toggleClass('hidden', true);             // 뱃지 숨김
//    };
$(function () {
  const box = $('#likeBox');
  if (!box.length) return;

  const liked = box.data('liked') === true || box.data('liked') === 'true';
  setLikedUI(liked); // ✅ 페이지 로딩할 때 초기화
});
});

// 🔽 my_bookmark 페이지에서 삭제된 북마크 처리 hy 추가
$(function () {
    const deletedIdsRaw = sessionStorage.getItem('deletedBookmarks');
    if (!deletedIdsRaw) return;

    const deletedIds = JSON.parse(deletedIdsRaw).map(String); // ID를 문자열로 비교할 수도 있습니다.
    if (deletedIds.length === 0) {
        sessionStorage.removeItem('deletedBookmarks'); // 비어있으면 제거
        return;
    }

    // 1. 상세 페이지 (temp_detail.html) 처리
    const box = $('#likeBox');
    if (box.length) {
        const cafeId = String(box.data('cafe-id'));
        if (deletedIds.includes(cafeId)) {
            // UI를 '좋아요 안 함' 상태로 변경
            setLikedUI(false);

//            box.data('liked', false);
            box.attr('data-liked', 'false'); // 데이터 속성 업데이트 (재접속 시 반영)

            // 카운트 업데이트 로직 (좋아요 상태였을 경우에만)
            const countEl = box.find('#likeCount');
            if (countEl.length) {
                 let currentCount = parseInt(countEl.text().trim(), 10);
                              if ((box.data('liked') === true || box.data('liked') === 'true') && currentCount > 0) {
                                 currentCount--;
                                 countEl.text(String(currentCount));
                 }
            }
        }
    }

    // 2. 목록 페이지 (cafe_list.html) 처리
    // ⚠️ 2-1에서 .cafe-item 클래스를 사용했다는 가정 하에 작성
    $('.cafe-item').each(function() {
         const listCafeId = String($(this).data('cafe-id'));

         if (deletedIds.includes(listCafeId)) {
            const likesSpan = $(this).find('.card-meta .likes'); // 카운트 요소 찾기

            if (likesSpan.length) {
                let currentText = likesSpan.text().trim();
                let match = currentText.match(/♥\s*(\d+)/);

                if (match) {
                    let currentCount = parseInt(match[1], 10);
                    if (currentCount > 0) {
                       currentCount--;
                       likesSpan.text(`♥ ${currentCount}`); // 카운트 -1 업데이트
                       console.log(`[북마크 삭제 반영] 목록페이지: 카페 ID ${listCafeId} 카운트 -1`);
                    }
                }
            }
         }
    });

    // 3. 작업 완료 후 세션 스토리지 클리어
    sessionStorage.removeItem('deletedBookmarks');
    console.log("[북마크 삭제 반영] 세션 저장소 클리어 완료.");
});