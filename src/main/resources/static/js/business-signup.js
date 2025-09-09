/* ================= 공통 알림 ================= */
function showAlert(msg) {
    document.getElementById("alertMessage").innerText = msg;
    new bootstrap.Modal(document.getElementById("alertModal")).show();
}

/* ================= AJAX 중복 체크 ================= */
async function checkDuplicate(field, value) {
    if (!value.trim()) return null;
    let url = `/businessuser/check-${field}?${field}=${encodeURIComponent(value)}`;
    try {
        const res = await fetch(url, { headers: { "Accept": "application/json" } });
        return await res.json();
    } catch (e) {
        console.error(`${field} check 실패:`, e);
        return null;
    }
}

/* ================= 아이디 검증 ================= */
function attachUsernameCheck() {
    return attachCheck("username", "usernameError", "usernameSuccess", "username");
}

function attachCheck(inputId, errorId, successId, field) {
    const input = document.getElementById(inputId);
    const error = document.getElementById(errorId);
    const success = document.getElementById(successId);

    let timer = null;
    let lastResult = null;

    async function runCheck() {
        const available = await checkDuplicate(field, input.value);

        if (!input.value) {
            if (error) error.classList.add("hidden");
            if (success) success.classList.add("hidden");
            lastResult = null;
            return;
        }

        if (available === true) {
            if (success) success.classList.remove("hidden");
            if (error) error.classList.add("hidden");
            lastResult = true;
        } else if (available === false) {
                      if (success) success.classList.add("hidden");
                      if (error) {
                          error.classList.remove("hidden");
                          switch (inputId) {
                              case "username":
                                  error.innerText = "이미 사용중인 아이디입니다.";
                                  break;
                          }
                      }
                      lastResult = false;
        } else {
            if (success) success.classList.add("hidden");
            if (error) error.classList.add("hidden");
            lastResult = null;
        }
    }

    input.addEventListener("input", () => {
        if (error) error.classList.add("hidden");
        if (success) success.classList.add("hidden");
        clearTimeout(timer);
        timer = setTimeout(runCheck, 300);
    });

    input.addEventListener("blur", runCheck);

    return () => lastResult;
}

/* ================= 이메일 검증 ================= */
function attachEmailCheck() {
    const input = document.getElementById("email");
    const error = document.getElementById("emailError");
    const success = document.getElementById("emailSuccess");

    let timer = null;
    let lastResult = null;

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    const allowedDomains = ["gmail.com", "naver.com", "daum.net", "kakao.com"];

    async function runCheck() {
        const email = input.value.trim();

        if (!email) {
            error.classList.add("hidden");
            success.classList.add("hidden");
            lastResult = null;
            return;
        }

        if (!emailRegex.test(email)) {
            error.classList.remove("hidden");
            success.classList.add("hidden");
            error.innerText = "유효한 이메일 형식이 아닙니다.";
            lastResult = false;
            return;
        }

                const domain = email.split("@")[1];
                if (!allowedDomains.includes(domain)) {
                    error.classList.remove("hidden");
                    success.classList.add("hidden");
                    error.innerText = "허용되지 않은 이메일 도메인입니다.";
                    lastResult = false;
                    return;
                }

        const available = await checkDuplicate("email", email);

        if (available === true) {
            success.classList.remove("hidden");
            error.classList.add("hidden");
            success.innerText = "사용 가능한 이메일입니다.";
            lastResult = true;
        } else if (available === false) {
            error.classList.remove("hidden");
            success.classList.add("hidden");
            error.innerText = "이미 등록된 이메일입니다.";
            lastResult = false;
        } else {
            error.classList.add("hidden");
            success.classList.add("hidden");
            lastResult = null;
        }
    }

    input.addEventListener("input", () => {
        error.classList.add("hidden");
        success.classList.add("hidden");
        clearTimeout(timer);
        timer = setTimeout(runCheck, 400);
    });
    input.addEventListener("blur", runCheck);

    return () => lastResult;
}

/* ================= 닉네임 검증 ================= */
function attachNicknameCheck() {
    const input = document.getElementById("nickname");
    const msg = document.getElementById("nicknameMessage");

    async function runCheck() {
        const available = await checkDuplicate("nickname", input.value);

        if (!input.value) {
            msg.classList.remove("text-success");
            msg.classList.add("text-danger");
            msg.innerText = "2~10자, 특수문자 제외";
        } else if (available === true) {
            msg.classList.remove("text-danger");
            msg.classList.add("text-success");
            msg.innerText = "사용 가능한 닉네임입니다.";
        } else if (available === false) {
            msg.classList.remove("text-success");
            msg.classList.add("text-danger");
            msg.innerText = "이미 사용중인 닉네임입니다.";
        } else {
            msg.classList.remove("text-success");
            msg.classList.add("text-danger");
            msg.innerText = "2~10자, 특수문자 제외";
        }
    }

    input.addEventListener("input", runCheck);
    input.addEventListener("blur", runCheck);
}



/* ================= 비밀번호 검증 ================= */
function validatePassword() {
  const pw = document.getElementById("password").value || "";
  const msg = document.getElementById("passwordError");
  const ok = /^(?=.*[!@#$%^&*(),.?":{}|<>]).{8,}$/.test(pw);

      if (ok) {
        // 조건 만족 시에만 성공 문구/초록색
        msg.classList.remove("text-danger");
        msg.classList.add("text-success");
        msg.innerText = "사용 가능한 비밀번호입니다.";
        return true;
      } else {
        // 조건 미달일 땐 항상 기본 가이드만 붉은색으로 유지
        msg.classList.remove("text-success");
        msg.classList.add("text-danger");
        msg.innerText = "8자 이상, 특수문자 포함";
        return false;
      }


}

function validatePasswordConfirm() {
    const pw = document.getElementById("password").value;
    const pw2 = document.getElementById("passwordConfirm").value;
    const msg = document.getElementById("passwordConfirmError");




    if (!pw2) {
        msg.classList.add("hidden");
        return false;
    }

    if (pw !== pw2) {
        msg.classList.remove("hidden");
        msg.classList.remove("text-success");
        msg.classList.add("text-danger");
        msg.innerText = "비밀번호가 일치하지 않습니다.";
        return false;
    } else {
        msg.classList.remove("hidden");
        msg.classList.remove("text-danger");
        msg.classList.add("text-success");
        msg.innerText = "비밀번호가 일치합니다.";
        return true;
    }
}

/* ================= 주소 검색 팝업 ================= */
function openAddressPopup() {
    // 기존 팝업 열기 방식 유지
    window.open('/jusoPopup', '주소검색', 'width=500,height=600,scrollbars=yes');
}

/* 팝업에서 선택 후 부모 창에 값 넣기 */
function jusoCallBack(selectedAddress) {
    const addressInput = document.getElementById("address");
    const detailInput = document.getElementById("addressDetail");

    if (addressInput) {
        addressInput.value = selectedAddress.roadAddr || selectedAddress.jibunAddr || '';
        detailInput.focus();
    }
}



// 알림팝업 띄우고 포커스 주기
function alertAndFocus(msg, inputId) {
    showAlert(msg);
    const el = document.getElementById(inputId);
    if (el) {
        // setTimeout으로 모달이 뜬 뒤 포커스가 덮어쓰이지 않게 살짝 지연
        setTimeout(() => { el.focus(); el.select && el.select(); }, 150);
    }
}





/* ================== 메인 실행 ================== */
const checkUsername = attachUsernameCheck();
const checkEmail = attachEmailCheck();
attachNicknameCheck();

document.getElementById("password").addEventListener("input", validatePassword);
document.getElementById("passwordConfirm").addEventListener("input", validatePasswordConfirm);

/* ================= 폼 제출 검증 ================= */
async function validateForm(e) {
    if (!validatePassword() || !validatePasswordConfirm()) {
        showAlert("비밀번호를 확인해주세요.");
        e.preventDefault();
        return false;
    }

    // ⬇️ 제출 직전에 최신 값으로 서버 중복 체크(반드시 await)
        const username = document.getElementById("username")?.value?.trim() || "";
        const email = document.getElementById("email")?.value?.trim() || "";

        // 서버와 즉시 통신해서 true/false/null을 받는다
        const [uAvail, eAvail, bAvail] = await Promise.all([
            username ? checkDuplicate("username", username) : null,
            email ? checkDuplicate("email", email) : null,
        ]);

        if (uAvail === false) {
            e.preventDefault();
            alertAndFocus("아이디가 이미 존재합니다.", "username");
            return false;
        }
        if (eAvail === false) {
            e.preventDefault();
            alertAndFocus("이메일이 이미 존재합니다.", "email");
            return false;
        }

        // 네트워크 에러 등으로 확인 실패했을 때는 기존 로직대로 제출(서버에서 한 번 더 검증)
        return true;
    }

document.addEventListener("DOMContentLoaded", () => {
    // 휴대폰 번호 입력 칸 3개
    const phone1 = document.getElementById("phone1");
    const phone2 = document.getElementById("phone2");
    const phone3 = document.getElementById("phone3");

    // 실제 서버에 보낼 hidden input
    const phoneHidden = document.getElementById("phone");

    if (phone1 && phone2 && phone3 && phoneHidden) {
        const phoneInputs = [phone1, phone2, phone3];

        phoneInputs.forEach((input, idx) => {
            // 입력 시 이벤트
            input.addEventListener("input", () => {
                // 숫자만 허용
                input.value = input.value.replace(/\D/g, "");

                // 자동 포커스 이동
                if (idx < 2 && input.value.length === parseInt(input.maxLength)) {
                    phoneInputs[idx + 1].focus();
                }

                // hidden input에 합치기
                phoneHidden.value = `${phone1.value}-${phone2.value}-${phone3.value}`;
            });

            // 백스페이스로 이전 칸 이동
            input.addEventListener("keydown", (e) => {
                if (e.key === "Backspace" && input.value.length === 0 && idx > 0) {
                    phoneInputs[idx - 1].focus();
                }
            });
        });
    }
});





