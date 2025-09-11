console.log("✅ login-form.js 로드됨");

    // 모든 input-with-clear 처리
    document.querySelectorAll('.input-with-clear').forEach(wrapper => {
        const input = wrapper.querySelector('input');
        const clearBtn = wrapper.querySelector('.clear-btn');

        // 버튼 클릭 시 값 초기화
        clearBtn.addEventListener('click', () => {
            input.value = '';
            input.focus();
            clearBtn.style.display = 'none';
        });

        // 입력 시 버튼 표시/숨김
        input.addEventListener('input', () => {
            if (input.value.length > 0) {
                clearBtn.style.display = 'block';
            } else {
                clearBtn.style.display = 'none';
            }
        });
    });

        // 비밀번호 보이기 토글
        const pwInput = document.getElementById("password");
        const toggleBtn = document.querySelector(".toggle-btn");

        pwInput.addEventListener("input", () => {
           if (pwInput.value.length > 0) {
              toggleBtn.style.display = "block";
           } else {
              toggleBtn.style.display = "none";
           }
        });

        // 버튼 클릭 시 보이기/숨기기 토글
        toggleBtn.addEventListener("click", () => {
           if (pwInput.type === "password") {
           pwInput.type = "text";
           toggleBtn.classList.replace("bi-eye", "bi-eye-slash");
        } else {
           pwInput.type = "password";
           toggleBtn.classList.replace("bi-eye-slash", "bi-eye");
        }
       });

//document.addEventListener("DOMContentLoaded", function () {
//    	const pw = document.getElementById("password").value.trim();
//    	const pw2 = document.getElementById("passwordConfirm").value.trim();
//    const errorMsg = document.getElementById("loginConfirmError");

    // 서버에서 ?error=true 같은 파라미터 내려줄 경우
//    const params = new URLSearchParams(window.location.search);
//    if (params.has("error")) {
//        errorMsg.classList.remove("d-none"); // 메시지 표시
//    }
//});
//    // 로그인 시도 시 입력값 확인 (선택적으로 추가)
//    const loginForm = document.getElementById("loginForm");
//    loginForm.addEventListener("submit", (e) => {
//        const username = document.getElementById("username").value.trim();
//        const password = document.getElementById("password").value.trim();
//        const errorMsg = document.getElementById("loginConfirmError");
//
//        if (!username || !password) {
//            e.preventDefault();
//            errorMsg.textContent = "아이디와 비밀번호를 입력해주세요.";
//            errorMsg.classList.remove("d-none");
//        }
//    });


//function validateLoginConfirm() {
//    const username = document.getElementById("username").value.trim();
//    const password = document.getElementById("password").value.trim();
//    const errorMsg = document.getElementById("loginConfirmError");
//
//    // 에러 없으면 숨기고 true 반환
//    errorMsg.classList.add("d-none");
//    return true;
//}
//
//// error msg
//document.addEventListener("DOMContentLoaded", function () {
//    const errorMsg = document.getElementById("loginConfirmError");
//    const urlParams = new URLSearchParams(window.location.search);
//
//    // URL에 'error' 파라미터가 있는지 확인
//    if (urlParams.has("error")) {
//        // 'd-none' 클래스를 제거하여 메시지를 보이게 함
//        errorMsg.classList.remove("d-none");
//    }
//// 폼 제출 시 validateLoginConfirm() 실행
//document.addEventListener("DOMContentLoaded", function () {
//    const loginForm = document.getElementById("loginForm");
//    loginForm.addEventListener("submit", (e) => {
//        if (!validateLoginConfirm()) {
//            errorMsg.textContent = "아이디 혹은 비밀번호가 일치하지 않습니다.";
//            e.preventDefault(); // 조건 불만족 → 폼 전송 막음
//        }
//    });
//});
