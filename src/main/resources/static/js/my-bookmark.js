console.log("✅ my-bookmark.js 로드됨");

document.querySelectorAll(".bookmark-remove-form .btn-remove-bookmark").forEach(btn => {
  btn.addEventListener("click", function () {
    const form = this.closest(".bookmark-remove-form");
    const cafeId = form.dataset.cafeId;

    fetch(`/bookmark/remove/${cafeId}`, { method: "DELETE" })
      .then(res => res.json())
      .then(data => {
        if (data.success) {
          // 북마크 페이지 UI에서 항목 제거
          const target = document.querySelector(`#bookmark-${cafeId}`);
          if (target) target.remove();

          // 상세페이지 북마크 토글 버튼 상태도 OFF로 전환
          if (window.setBookmarkOff) {
            window.setBookmarkOff();
          }
        }
      })
      .catch(err => {
        console.error("❌ 북마크 삭제 실패:", err);
        alert("북마크 삭제 중 오류가 발생했습니다.");
      });
  });
});