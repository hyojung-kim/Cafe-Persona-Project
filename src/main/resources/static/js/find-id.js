        window.addEventListener('DOMContentLoaded', () => {
            const alertBox = document.getElementById('alertBox');
            if (alertBox) {
                setTimeout(() => {
                    alertBox.style.display = 'none';
                }, 5000);
            }
        });

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

        /* ================= 폼 제출 검증 ================= */
        function validateForm(e) {
        	e.preventDefault(); // 먼저 기본 제출 막기

                const emailInput = document.getElementById("email");
                const email = emailInput.value.trim();
                const errorMsg = emailInput.nextElementSibling;

//        	const email = document.getElementById("email").value.trim();
////        	const verifiedUserEmail = document.getElementById("verifiedUserEmail")?.value; // 인증 완료 아이디
//            const errorMsg = document.getElementById("emailError")
//
//        	// 이메일 검증
//        	const emailValid = validateEmail();
//        	const emailConfirmValid = validateEmailConfirm();
//
//        	if (!mail || !emailConfirmValid) {
//        		e.preventDefault(); // 반드시 막기
//        		showAlert("확인해보자");
//        		return;
//        	}
//
        	// 검증 통과하면 폼 제출
        	errorMsg.classList.remove("d-none");
        	e.target.submit();
        }

document.addEventListener("DOMContentLoaded", function () {
    const form = document.querySelector("form[th\\:action='@{/user/findId}']");
    const emailInput = document.getElementById("email");

    if (emailInput) {
        emailInput.addEventListener("input", () => {
            const errorMsg = emailInput.nextElementSibling;
            errorMsg.classList.remove("d-none");
        });
    }

    if (form) {
        form.addEventListener("submit", validateFindIdForm);
    }
});

/* ================= 알림 모달 표시 ================= */
function showAlert(message) {
	const modalEl = document.getElementById("alertModal");
	const msgEl = document.getElementById("alertMessage");
	msgEl.innerText = message;

	const modal = new bootstrap.Modal(modalEl);

	// 모달 닫힐 때 페이지 새로고침
	// 이벤트 제거 후 다시 등록 (중복 방지)
	modalEl.removeEventListener("hidden.bs.modal", reloadPage);
	function reloadPage() {
		window.location.reload();
	}
	modalEl.addEventListener("hidden.bs.modal", reloadPage, { once: true });

	modal.show();
}