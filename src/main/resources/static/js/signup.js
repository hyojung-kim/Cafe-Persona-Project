/* ================= 공통 알림 ================= */
function showAlert(msg) {
    document.getElementById("alertMessage").innerText = msg;
    new bootstrap.Modal(document.getElementById("alertModal")).show();
}

/* ================= 비밀번호 유효성 ================= */
function validatePassword() {
    const pwInput = document.getElementById("password");
    const pw = pwInput.value;
    const error = document.getElementById("passwordError");
    const regex = /^(?=.*[!@#$%^&*(),.?":{}|<>]).{8,}$/;

    if (pw.length === 0) {
        error.classList.remove("text-success");
        error.classList.add("text-danger");
        error.innerText = "비밀번호는 8자 이상, 특수문자를 포함해야 합니다.";
        return false;
    }

    if (!regex.test(pw)) {
        error.classList.remove("text-success");
        error.classList.add("text-danger");
        error.innerText = "조건을 만족하지 않는 비밀번호입니다.";
        return false;
    } else {
        error.classList.remove("text-danger");
        error.classList.add("text-success");
        error.innerText = "사용 가능한 비밀번호입니다.";
        return true;
    }
}

/* ================= 비밀번호 확인 ================= */
function validatePasswordConfirm() {
    const pw = document.getElementById("password").value;
    const pw2 = document.getElementById("passwordConfirm").value;
    const error = document.getElementById("passwordConfirmError");

    if (pw2.length === 0) {
        error.classList.add("d-none");
        return false;
    }

    if (pw !== pw2) {
        error.classList.remove("d-none");
        return false;
    } else {
        error.classList.add("d-none");
        return true;
    }
}

/* ================= AJAX 중복 체크 ================= */
async function checkDuplicate(field, value) {
    if (!value.trim()) return null;
    let url = `/user/check-${field}?${field}=${encodeURIComponent(value)}`;
    try {
        const res = await fetch(url, {headers: {"Accept": "application/json"}});
        return await res.json(); // true = 사용 가능, false = 중복
    } catch (e) {
        console.error(`${field} check 실패:`, e);
        return null;
    }
}

/* ================= 필드 검증 유틸 ================= */
function attachCheck(inputId, errorId, successId, field) {
    const input = document.getElementById(inputId);
    const error = document.getElementById(errorId);
    const success = document.getElementById(successId);
    let timer = null;
    let lastResult = null;

    async function runCheck() {
        const available = await checkDuplicate(field, input.value);
        if (available === true) {
            error.classList.add("d-none");
            success.classList.remove("d-none");
            lastResult = true;
        } else if (available === false) {
            success.classList.add("d-none");
            error.classList.remove("d-none");
            lastResult = false;
        } else {
            success.classList.add("d-none");
            error.classList.add("d-none");
            lastResult = null;
        }
    }

    input.addEventListener("input", () => {
        error.classList.add("d-none");
        success.classList.add("d-none");
        clearTimeout(timer);
        timer = setTimeout(runCheck, 300);
    });

    input.addEventListener("blur", runCheck);

    return () => lastResult;
}

/* ================== 메인 실행 ================== */
const checkUsername = attachCheck("username", "usernameError", "usernameSuccess", "username");
const checkEmail = attachCheck("email", "emailError", "emailSuccess", "email");
const checkNickname = attachCheck("nickname", "nicknameError", "nicknameSuccess", "nickname");

async function validateForm(e) {
    if (!validatePassword() || !validatePasswordConfirm()) {
        showAlert("비밀번호를 확인해주세요.");
        e.preventDefault();
        return false;
    }
    if (checkUsername() === false) { showAlert("아이디가 이미 존재합니다."); e.preventDefault(); return false; }
    if (checkEmail() === false) { showAlert("이메일이 이미 존재합니다."); e.preventDefault(); return false; }
    if (checkNickname() === false) { showAlert("닉네임이 이미 존재합니다."); e.preventDefault(); return false; }
    return true;
}
