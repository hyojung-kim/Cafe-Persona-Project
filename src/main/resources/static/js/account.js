// ================== 휴대폰 번호 수정 (복붙용 완성본) ==================

(() => {
  "use strict";

  // --- 디버그 on/off ---
  const DEBUG = true;
  const log = (...args) => { if (DEBUG) console.log("[account.js]", ...args); };
  const err = (...args) => console.error("[account.js]", ...args);

  // --- 컨텍스트 경로(meta로 주입 권장) ---
  const ctxMeta = document.querySelector('meta[name="ctx"]');
  const CTX = (ctxMeta && ctxMeta.content) ? ctxMeta.content : ""; // 예: "" 또는 "/cafe"

  // --- CSRF (없으면 빈 값) ---
  const csrfMeta = document.querySelector('meta[name="_csrf"]');
  const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
  const csrfToken  = csrfMeta ? csrfMeta.content : "";
  const csrfHeader = csrfHeaderMeta ? csrfHeaderMeta.content : "X-CSRF-TOKEN";

  // --- 공통 요소 (휴대폰) ---
  const phoneModal    = document.getElementById("phoneModal");
  const openPhoneBtn  = document.getElementById("openPhoneModal");
  const closePhoneBtn = document.getElementById("closePhoneModal");
  const phoneDisplay  = document.getElementById("phoneDisplay");

  // 3-4-4 입력
  const p1 = document.getElementById("phone1");
  const p2 = document.getElementById("phone2");
  const p3 = document.getElementById("phone3");
  const hiddenPhone = document.getElementById("phone");
  const phoneInputsContainer = document.querySelector(".phone-inputs"); // (선택) 유도문 힌트 컨테이너

  // 저장(수정) 버튼
  const updatePhoneBtn = document.getElementById("updatePhoneBtn");

  // --- 초기 로그 ---
  log("readyState:", document.readyState);
  ["openPhoneModal","closePhoneModal","updatePhoneBtn","phoneModal","phoneDisplay","phone1","phone2","phone3","phone"]
    .forEach(id => log(id, !!document.getElementById(id)));
  log("CTX:", CTX || "(empty)");
  log("csrfHeader:", csrfHeader, "csrfToken exists:", !!csrfToken);

  // --- 유틸: 입력 초기화 & 유도문 표시 토글 ---
  function updateGuideState() {
    const hasAny = !!(p1?.value || p2?.value || p3?.value);
    phoneInputsContainer?.classList.toggle("has-value", hasAny); // .has-value일 때 힌트 숨기도록 CSS가 있다면 사용
  }
  function resetPhoneInputs() {
    if (p1) p1.value = "";
    if (p2) p2.value = "";
    if (p3) p3.value = "";
    if (hiddenPhone) hiddenPhone.value = "";
    updateGuideState();
  }

  // --- 모달 열기/닫기 (휴대폰) ---
  if (openPhoneBtn && phoneModal) {
    openPhoneBtn.addEventListener("click", () => {
      resetPhoneInputs();                 // 열 때 항상 비우기 → 유도문 보이도록
      phoneModal.classList.add("show");
      setTimeout(() => p1?.focus(), 0);   // UX: 첫 칸 포커스
      log("openPhoneModal clicked → modal.show");
    });
  }
  if (closePhoneBtn && phoneModal) {
    closePhoneBtn.addEventListener("click", () => {
      resetPhoneInputs();                 // 닫을 때도 비우기(선택)
      phoneModal.classList.remove("show");
      log("closePhoneModal clicked → modal.hide");
    });
  }

  // --- 3-4-4 입력 처리 ---
  if (p1 && p2 && p3 && hiddenPhone) {
    const inputs = [p1, p2, p3];
    inputs.forEach((input, idx) => {
      input.addEventListener("input", () => {
        input.value = input.value.replace(/\D/g, "");
        const max = parseInt(input.maxLength || "4", 10);
        if (idx < 2 && input.value.length === max) inputs[idx + 1].focus();
        hiddenPhone.value = `${p1.value}-${p2.value}-${p3.value}`;
        updateGuideState();               // 값 생기면 힌트 숨김
        log("compose phone:", hiddenPhone.value);
      });
      input.addEventListener("keydown", (e) => {
        if (e.key === "Backspace" && input.value.length === 0 && idx > 0) {
          inputs[idx - 1].focus();
        }
      });
    });
  } else {
    log("phone inputs not ready → p1/p2/p3/hidden missing?");
  }

  // --- 저장 클릭 → 서버 전송 (휴대폰) ---
  if (updatePhoneBtn && hiddenPhone) {
    updatePhoneBtn.addEventListener("click", async () => {
      const newPhone = (hiddenPhone.value || "").trim(); // 예: 010-1234-5678
      log("updatePhoneBtn clicked. newPhone:", newPhone);

      if (!/^\d{2,3}-\d{3,4}-\d{4}$/.test(newPhone)) {
        alert("휴대폰 번호 형식을 확인해주세요. (예: 010-1234-5678)");
        return;
      }

      const url = `${CTX}/mypage/account/update-phone`;
      log("fetch url:", url);

      try {
        const res = await fetch(url, {
          method: "POST",
          credentials: "same-origin", // 크로스 오리진이면 "include"
          headers: {
            "Content-Type": "application/x-www-form-urlencoded",
            [csrfHeader]: csrfToken
          },
          body: `phone=${encodeURIComponent(newPhone)}`
        });

        const text = await res.text();
        const ctype = (res.headers.get("content-type") || "").toLowerCase();
        const body = (text || "").trim().toLowerCase();

        log("[update-phone] status:", res.status,
            "redirected:", res.redirected,
            "finalURL:", res.url,
            "contentType:", ctype,
            "body(raw):", text);

        if (res.ok && body === "success") {
          alert("휴대폰 번호가 변경되었습니다.");
          if (phoneDisplay) phoneDisplay.textContent = newPhone;
          resetPhoneInputs();             // 성공 후 비우기 → 다음 열 때 유도문 표시
          phoneModal?.classList.remove("show");
          return;
        }

        if (res.status === 401) {
          alert("로그인이 필요합니다. 다시 로그인 후 시도해주세요.");
          location.href = `${CTX}/user/login?continue=` + encodeURIComponent(location.pathname + location.search);
          return;
        }
        if (res.status === 403 || res.redirected || ctype.includes("text/html")) {
          if (confirm("변경에 실패했습니다. 보안 인증을 다시 진행할까요?")) {
            const cont = location.pathname + location.search;
            location.href = `${CTX}/mypage/verify_password?continue=` + encodeURIComponent(cont);
          }
          return;
        }

        alert("변경에 실패했습니다: " + text);
      } catch (e) {
        err("fetch error:", e);
        alert("서버 오류가 발생했습니다.");
      }
    });
  } else {
    log("updatePhoneBtn or hiddenPhone missing → click handler not bound");
  }

  // ================== 비밀번호 변경 (재인증 체크 없이 간단 버전) ==================

  // 요소
  const pwModal    = document.getElementById("passwordModal");
  const openPwBtn  = document.getElementById("openPasswordModal");
  const closePwBtn = document.getElementById("closePasswordModal");
  const newPwInput  = document.getElementById("newPassword");
  const newPw2Input = document.getElementById("newPasswordConfirm");
  const updatePwBtn = document.getElementById("updatePasswordBtn");
  const pwMsg  = document.getElementById("passwordMessage");
  const pwForm = document.getElementById("passwordForm"); // form이 있으면 기본 제출 막음

  // 유틸
  function resetPasswordInputs() {
    if (newPwInput) newPwInput.value = "";
    if (newPw2Input) newPw2Input.value = "";
    if (pwMsg) pwMsg.textContent = "";
  }
  function validatePasswordFormat(pw) {
    // 예시 정책: 8~64자, 영문/숫자/특수문자 중 2종 이상
    if (pw.length < 8 || pw.length > 64) return "비밀번호는 8~64자여야 합니다.";
    const classes = [
      /[a-zA-Z]/.test(pw),
      /\d/.test(pw),
      /[^a-zA-Z0-9]/.test(pw)
    ].filter(Boolean).length;
    if (classes < 2) return "영문, 숫자, 특수문자 중 두 가지 이상을 조합해주세요.";
    return "";
  }
  function showPwMsg(msg, ok = false) {
    if (!pwMsg) return;
    pwMsg.textContent = msg;
    pwMsg.classList.toggle("ok", ok);
    pwMsg.classList.toggle("err", !ok && !!msg);
  }

  // 모달 열고 닫기
  if (openPwBtn && pwModal) {
    openPwBtn.addEventListener("click", () => {
      resetPasswordInputs();
      pwModal.classList.add("show");
      setTimeout(() => newPwInput?.focus(), 0);
      log("openPasswordModal clicked → modal.show");
    });
  }
  if (closePwBtn && pwModal) {
    closePwBtn.addEventListener("click", () => {
      resetPasswordInputs();
      pwModal.classList.remove("show");
      log("closePasswordModal clicked → modal.hide");
    });
  }

  // form 기본 submit 방지
  if (pwForm) pwForm.addEventListener("submit", (e) => e.preventDefault());

  // 실시간 검증 메시지
  if (newPwInput) {
    newPwInput.addEventListener("input", () => {
      const v = newPwInput.value || "";
      const msg = validatePasswordFormat(v);
      if (v && !msg) showPwMsg("사용 가능한 형식입니다.", true);
      else if (v) showPwMsg(msg, false);
      else showPwMsg("", false);
    });
  }
  if (newPw2Input) {
    newPw2Input.addEventListener("input", () => {
      if (!newPwInput?.value) return;
      const same = newPw2Input.value === newPwInput.value;
      if (same) showPwMsg("새 비밀번호가 일치합니다.", true);
      else showPwMsg("새 비밀번호 확인이 일치하지 않습니다.", false);
    });
  }

  // 저장 클릭 → 서버 전송 (현재 비밀번호 없이)
  if (updatePwBtn) {
    updatePwBtn.addEventListener("click", async () => {
      const newPw  = (newPwInput?.value  || "").trim();
      const newPw2 = (newPw2Input?.value || "").trim();

      const fmtMsg = validatePasswordFormat(newPw);
      if (fmtMsg) { alert(fmtMsg); newPwInput?.focus(); return; }
      if (newPw !== newPw2) { alert("새 비밀번호 확인이 일치하지 않습니다."); newPw2Input?.focus(); return; }

      const url = `${CTX}/mypage/account/update-password`;
      updatePwBtn.disabled = true;

      try {
        const res = await fetch(url, {
          method: "POST",
          credentials: "same-origin",
          headers: {
            "Content-Type": "application/x-www-form-urlencoded",
            [csrfHeader]: csrfToken
          },
          body: `newPassword=${encodeURIComponent(newPw)}&newPasswordConfirm=${encodeURIComponent(newPw2)}`
        });

        const text  = await res.text();
        const ctype = (res.headers.get("content-type") || "").toLowerCase();
        const bodyL = (text || "").trim().toLowerCase();

        log("[update-password] status:", res.status,
            "redirected:", res.redirected,
            "finalURL:", res.url,
            "contentType:", ctype,
            "body(raw):", text);

        if (res.ok && (bodyL === "success" || bodyL === "ok")) {
          alert("비밀번호가 변경되었습니다. 다시 로그인해 주세요.");
          resetPasswordInputs();
          pwModal?.classList.remove("show");
          // 보안상 재로그인 유도
          location.href = `${CTX}/user/login?continue=` + encodeURIComponent(location.pathname + location.search);
          return;
        }

        if (res.status === 401) {
          alert("로그인이 필요합니다. 다시 로그인 후 시도해주세요.");
          location.href = `${CTX}/user/login?continue=` + encodeURIComponent(location.pathname + location.search);
          return;
        }

        if (res.status === 403 || res.redirected || ctype.includes("text/html")) {
          // 서버에서 세션/재인증 만료로 판단 시
          if (confirm("변경에 실패했습니다. 보안 인증을 다시 진행할까요?")) {
            const cont = location.pathname + location.search;
            location.href = `${CTX}/mypage/verify_password?continue=` + encodeURIComponent(cont);
          }
          return;
        }

        alert("변경에 실패했습니다: " + text);
      } catch (e) {
        err("fetch error (password):", e);
        alert("서버 오류가 발생했습니다.");
      } finally {
        updatePwBtn.disabled = false;
      }
    });
  } else {
    log("updatePasswordBtn missing → password click handler not bound");
  }

})();
