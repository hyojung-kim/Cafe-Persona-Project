// cafe-register.js (single source of truth) — UPDATED
document.addEventListener("DOMContentLoaded", () => {
  const pickBtn   = document.getElementById("btn-photo-pick");
  const inputEl   = document.getElementById("photos");
  const previewEl = document.getElementById("photo-preview");

  if (!pickBtn || !inputEl || !previewEl) return; // 요소 없으면 즉시 종료

  // ===== 설정 =====
  const MAX_FILES = 10;
  const MAX_SIZE_MB = 5;

  // ===== cafeId / CSRF 획득 =====
  const cafeId =
    (document.getElementById("cafeId") && document.getElementById("cafeId").value) ||
    new URLSearchParams(location.search).get("cafeId") ||
    pickBtn.getAttribute("data-cafe-id") ||
    "";

  // Spring Security 기본 hidden csrf (폼 안에 이미 있음)
  // ex) <input type="hidden" name="_csrf" value="...">
  const csrfInput =
    document.querySelector('input[name="_csrf"]') ||
    document.querySelector('input[name*="csrf"]');

  // 헤더 이름은 보통 X-CSRF-TOKEN (환경에 따라 다를 수 있으니 안전하게 필드+헤더 모두 보냄)
  const CSRF_HEADER_NAME = "X-CSRF-TOKEN";
  const csrfToken = csrfInput ? csrfInput.value : "";

  // cafeId 없으면 업로드 버튼 잠금 (관리 페이지로 이동해서 올리도록 유도)
  if (!cafeId) {
    pickBtn.disabled = true;
    pickBtn.title = "사업장을 먼저 생성(등록)한 뒤 사진을 추가할 수 있어요.";
  }

  // ===== 파일 누적 버퍼 (프리뷰/삭제용) =====
  let buffer;
  try {
    buffer = new DataTransfer();
  } catch (e) {
    buffer = null; // 일부 브라우저 미지원 시 null
  }

  // 버튼 → 파일 선택창
  pickBtn.addEventListener("click", () => inputEl.click());

  // 미리보기 타일 생성 (+ 삭제 버튼)
  function appendPreviewTile(file) {
    const url = URL.createObjectURL(file);
    const tile = document.createElement("div");
    tile.className = "photo-tile";
    tile.style.display = "inline-flex";
    tile.style.flexDirection = "column";
    tile.style.alignItems = "center";
    tile.style.gap = "6px";

    const img = document.createElement("img");
    img.src = url;
    img.alt = file.name;
    img.className = "preview-img";
    img.style.maxWidth  = "150px";
    img.style.maxHeight = "150px";
    img.style.objectFit = "cover";
    img.style.borderRadius = "8px";

    const name = document.createElement("span");
    name.className = "photo-name";
    name.textContent = file.name;
    name.style.fontSize = "12px";
    name.style.color = "#6b7280";
    name.style.maxWidth = "150px";
    name.style.whiteSpace = "nowrap";
    name.style.overflow = "hidden";
    name.style.textOverflow = "ellipsis";
    name.style.textAlign = "center";

    const del = document.createElement("button");
    del.type = "button";
    del.textContent = "삭제";
    del.className = "btn btn-sm btn-outline-secondary";
    del.addEventListener("click", () => {
      // 버퍼에서 해당 파일 제거
      if (buffer) {
        const items = Array.from(buffer.items);
        const idx = items.findIndex(it => {
          const f = it.getAsFile();
          return f && f.name === file.name && f.size === file.size && f.type === file.type;
        });
        if (idx > -1) {
          buffer.items.remove(idx);
          inputEl.files = buffer.files;
        }
      }
      URL.revokeObjectURL(url);
      tile.remove();
    });

    tile.appendChild(img);
    tile.appendChild(name);
    tile.appendChild(del);
    previewEl.appendChild(tile);
  }

  // ===== 업로드 함수 (AJAX: fetch + FormData) =====
  async function uploadFiles(files) {
    if (!cafeId) {
      alert("사업장을 먼저 등록해 주세요. (cafeId 없음)");
      return;
    }
    if (!files || files.length === 0) return;

    const fd = new FormData();
    for (const f of files) {
      fd.append("photos", f); // 컨트롤러 @RequestParam("photos") 와 키 이름 일치
    }

    // CSRF: 필드와 헤더를 모두 보냄(환경 호환성 ↑)
    if (csrfInput && csrfInput.name && csrfToken) {
      fd.append(csrfInput.name, csrfToken);
    }

    try {
      const res = await fetch(`/cafe/${encodeURIComponent(cafeId)}/images`, {
        method: "POST",
        headers: csrfToken ? { [CSRF_HEADER_NAME]: csrfToken } : undefined,
        body: fd,
      });

      if (res.ok || (res.status >= 200 && res.status < 400)) {
        // 성공: 간단히 알림 → 필요하면 새로고침(주석 해제)
        toast("이미지 업로드 완료");
        // location.reload();
      } else {
        const txt = await res.text();
        console.error("Upload failed:", res.status, txt);
        alert(`이미지 업로드 실패(${res.status})`);
      }
    } catch (err) {
      console.error("Upload error:", err);
      alert("이미지 업로드 중 오류가 발생했습니다.");
    }
  }

  function toast(msg) {
    // 페이지에 토스트 영역이 있다면 거기에, 없으면 alert
    const box = document.querySelector(".alert.alert-success span");
    if (box) {
      box.textContent = msg;
      const wrap = document.querySelector(".alert.alert-success");
      if (wrap) wrap.style.display = "block";
    } else {
      // 간단 대체
      console.log(msg);
    }
  }

  // ===== 파일 선택 시: 검증 → 프리뷰 → 누적 → 서버 업로드 =====
  inputEl.addEventListener("change", async (e) => {
    const selected = Array.from(e.target.files || []);
    if (!selected.length) return;

    // 현재 누적 수
    const currentCount = buffer ? buffer.files.length : previewEl.querySelectorAll("img.preview-img").length;
    if (currentCount + selected.length > MAX_FILES) {
      alert(`이미지는 최대 ${MAX_FILES}장까지 업로드할 수 있어요.`);
      inputEl.value = "";
      return;
    }

    // 용량 검사
    const tooLarge = selected.find(f => f.size > MAX_SIZE_MB * 1024 * 1024);
    if (tooLarge) {
      alert(`각 이미지 용량은 ${MAX_SIZE_MB}MB 이하여야 해요: ${tooLarge.name}`);
      inputEl.value = "";
      return;
    }

    // 누적/프리뷰 반영
    selected.forEach(file => {
      if (!file.type.startsWith("image/")) return;
      if (buffer) buffer.items.add(file);
      appendPreviewTile(file);
    });
    if (buffer) inputEl.files = buffer.files;

    // === 선택분 즉시 업로드 ===
    await uploadFiles(selected);

    // 업로드 후 input 값 초기화 (다음 선택 대비)
    inputEl.value = "";
  });
});
