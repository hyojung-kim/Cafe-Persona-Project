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

// URL에서 쿼리 파라미터를 가져오는 함수
function getParameterByName(name, url = window.location.href) {
    name = name.replace(/[\[\]]/g, '\\$&');
    var regex = new RegExp('[?&]' + name + '(=([^&#]*)|&|#|$)'),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';
    return decodeURIComponent(results[2].replace(/\+/g, ' '));
}

document.addEventListener("DOMContentLoaded", function() {
    // URL에 'error' 파라미터가 있는지 확인
//    const urlParams = new URLSearchParams(window.location.search);
//    const errorParam = getParameterByName('error');
//    if (errorParam !== null) {
//        // 'message' 파라미터가 있는지 확인하고 메시지 설정
//        const errorMessage = getParameterByName('message');
//        const errorElement = document.getElementById('loginConfirmError');
//        if (errorMessage && errorElement) {
//            errorElement.textContent = errorMessage;
//            errorElement.style.display = 'block'; // 메시지를 보이게 설정
//        }
//    }
    ////사업자 유저 구분용 추후에 써보기
// const urlParams = new URLSearchParams(window.location.search);
//    const errorMessage = urlParams.get('message');
//    const errorElement = document.getElementById('loginConfirmError');
//
//    // URL에 'error' 파라미터가 있고, 'message' 파라미터도 있다면 메시지 표시
//    if (urlParams.has('error') && errorMessage) {
//        errorElement.textContent = errorMessage;
//        errorElement.style.display = 'block';
//    } else {
//        errorElement.style.display = 'none'; // 오류가 없을 경우 숨기기
//    }
    const urlParams = new URLSearchParams(window.location.search);
    const errorParam = urlParams.get('error');
    const errorMessage = urlParams.get('message');
    const errorElement = document.getElementById('loginConfirmError');

    if (errorParam !== null && errorElement) {
        if (errorMessage) {
            errorElement.textContent = errorMessage;
        }
        errorElement.style.display = 'block';
    } else if (errorElement) {
        errorElement.style.display = 'none';
    }
});
