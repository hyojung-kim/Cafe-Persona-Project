console.log("✅ login-form.js 로드됨");

// 모든 input-with-clear 처리
document.querySelectorAll(".input-with-clear").forEach((wrapper) => {
	const input = wrapper.querySelector("input");
	const clearBtn = wrapper.querySelector(".clear-btn");

	// 버튼 클릭 시 값 초기화
	clearBtn.addEventListener("click", () => {
		input.value = "";
		input.focus();
		clearBtn.style.display = "none";
	});

	// 입력 시 버튼 표시/숨김
	input.addEventListener("input", () => {
		if (input.value.length > 0) {
			clearBtn.style.display = "block";
		} else {
			clearBtn.style.display = "none";
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
