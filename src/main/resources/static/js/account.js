/* ================= CSRF 설정 ================= */
const csrfToken = document.querySelector('meta[name="_csrf"]').content;
const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

/* ================= 모달 열기/닫기 ================= */
const modal = document.getElementById("passwordModal");
const openBtn = document.getElementById("openPasswordModal");
const closeBtn = document.getElementById("closePasswordModal");

openBtn?.addEventListener("click", () => modal.classList.add("show"));
closeBtn?.addEventListener("click", () => modal.classList.remove("show"));

/* ================= 비밀번호 검증 ================= */
function validatePassword(pw) {
    const regex = /^(?=.*[!@#$%^&*(),.?":{}|<>]).{8,}$/;
    return regex.test(pw);
}

function validatePasswordConfirm(pw, pwConfirm) {
    return pw === pwConfirm;
}

/* ================= 비밀번호 변경 AJAX ================= */
document.getElementById("updatePasswordBtn")?.addEventListener("click", async () => {
    const pw = document.getElementById("newPassword").value.trim();
    const pwConfirm = document.getElementById("confirmPassword").value.trim();

    if (!validatePassword(pw)) {
        alert("비밀번호 조건을 확인해주세요.");
        return;
    }
    if (!validatePasswordConfirm(pw, pwConfirm)) {
        alert("비밀번호 확인이 일치하지 않습니다.");
        return;
    }

    try {
        const res = await fetch("/mypage/account/update-password", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded",
                [csrfHeader]: csrfToken
            },
            body: `newPassword=${encodeURIComponent(pw)}`
        });

        const result = await res.text();
if (result === "success") {
    alert("비밀번호가 변경되었습니다. 다시 로그인 해주세요.");
    modal.classList.remove("show");
    window.location.href = "/user/login"; // 로그인 페이지로 이동
} else {
    alert("비밀번호 변경에 실패했습니다.");
}

    } catch (err) {
        console.error(err);
        alert("서버 오류 발생");
    }
});


/* ================= 휴대폰 번호 모달 열기/닫기 ================= */
const phoneModal = document.getElementById("phoneModal");
const openPhoneBtn = document.getElementById("openPhoneModal");
const closePhoneBtn = document.getElementById("closePhoneModal");

openPhoneBtn?.addEventListener("click", () => phoneModal.classList.add("show"));
closePhoneBtn?.addEventListener("click", () => phoneModal.classList.remove("show"));

/* ================= 휴대폰 번호 변경 AJAX ================= */
document.getElementById("updatePhoneBtn")?.addEventListener("click", async () => {
    const newPhone = document.getElementById("newPhone").value.trim();

    if (!newPhone) {
        alert("휴대폰 번호를 입력해주세요.");
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
            phoneModal.classList.remove("show");

            // 화면에 표시된 번호 갱신
            const phoneDisplay = document.getElementById("phoneDisplay");
            if (phoneDisplay) phoneDisplay.textContent = newPhone;
        } else {
            alert("휴대폰 번호 변경에 실패했습니다.");
        }
    } catch (err) {
        console.error(err);
        alert("서버 오류 발생");
    }
});

  // BFCache로 돌아온 경우(뒤/앞 이동), 서버로 다시 요청 보내도록 강제
  window.addEventListener('pageshow', async (evt) => {
    if (evt.persisted) {
      location.replace('/mypage/verify_password?continue=' + encodeURIComponent(location.pathname + location.search));
    }
  });



