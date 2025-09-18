document.addEventListener("DOMContentLoaded", () => {
// ================== 휴대폰 번호 수정  ==================
document.addEventListener("DOMContentLoaded", () => {
  const updatePhoneBtn = document.getElementById("updatePhoneBtn");
  const phoneModal = document.getElementById("phoneModal");
  const phoneDisplay = document.getElementById("phoneDisplay");

  // 3-4-4 입력 합쳐둔 hidden
  const hidden = document.getElementById("phone");

  // CSRF 메타에서 가져오기(없으면 빈 값)
  const csrfMeta = document.querySelector('meta[name="_csrf"]');
  const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
  const csrfToken  = csrfMeta ? csrfMeta.content : "";
  const csrfHeader = csrfHeaderMeta ? csrfHeaderMeta.content : "X-CSRF-TOKEN";

  if (updatePhoneBtn && hidden) {
    updatePhoneBtn.addEventListener("click", async () => {
      const newPhone = (hidden.value || "").trim(); // 예: 010-1234-5678

      // 간단한 형식 체크 (필요 없으면 제거)
      if (!/^\d{2,3}-\d{3,4}-\d{4}$/.test(newPhone)) {
        alert("휴대폰 번호 형식을 확인해주세요. (예: 010-1234-5678)");
        return;
      }

      try {
        const res = await fetch("/mypage/account/update-phone", {
          method: "POST",
          headers: {
            "Content-Type": "application/x-www-form-urlencoded",
            [csrfHeader]: csrfToken
          },
          body: `phone=${encodeURIComponent(newPhone)}`
        });

        const result = await res.text();

        if (result === "success") {
          alert("휴대폰 번호가 변경되었습니다.");
          // 화면 갱신
          if (phoneDisplay) phoneDisplay.textContent = newPhone;
          // 모달 닫기
          phoneModal.classList.remove("show");
        } else {
          // 재인증 요구 정책이면 서버가 "fail"을 보낼 수 있음 → verify_password로 유도
          if (confirm("변경에 실패했습니다. 보안 인증을 다시 진행할까요?")) {
            const cont = location.pathname + location.search;
            location.href = "/mypage/verify_password?continue=" + encodeURIComponent(cont);
          }
        }
      } catch (e) {
        console.error(e);
        alert("서버 오류가 발생했습니다.");
      }
    });
  }
});





  // 모달 요소
  const phoneModal = document.getElementById("phoneModal");
  const openPhoneBtn = document.getElementById("openPhoneModal");
  const closePhoneBtn = document.getElementById("closePhoneModal");

  if (openPhoneBtn && phoneModal) {
    openPhoneBtn.addEventListener("click", () => {
      phoneModal.classList.add("show");
    });
  }
  if (closePhoneBtn && phoneModal) {
    closePhoneBtn.addEventListener("click", () => {
      phoneModal.classList.remove("show");
    });
  }

  // 3-4-4 입력
  const p1 = document.getElementById("phone1");
  const p2 = document.getElementById("phone2");
  const p3 = document.getElementById("phone3");
  const hidden = document.getElementById("phone");

  if (p1 && p2 && p3 && hidden) {
    const inputs = [p1, p2, p3];
    inputs.forEach((input, idx) => {
      input.addEventListener("input", () => {
        input.value = input.value.replace(/\D/g, "");
        const max = parseInt(input.maxLength || "4", 10);
        if (idx < 2 && input.value.length === max) {
          inputs[idx + 1].focus();
        }
        hidden.value = `${p1.value}-${p2.value}-${p3.value}`;
      });
      input.addEventListener("keydown", (e) => {
        if (e.key === "Backspace" && input.value.length === 0 && idx > 0) {
          inputs[idx - 1].focus();
        }
      });
    });
  }

  // 비번 모달도 동일 패턴으로
  const pwModal = document.getElementById("passwordModal");
  const openPwBtn = document.getElementById("openPasswordModal");
  const closePwBtn = document.getElementById("closePasswordModal");
  if (openPwBtn && pwModal) openPwBtn.addEventListener("click", () => pwModal.classList.add("show"));
  if (closePwBtn && pwModal) closePwBtn.addEventListener("click", () => pwModal.classList.remove("show"));
});
