// /js/cafe-register.js  — 프리뷰/검증 + 단일 폼 일괄 제출
document.addEventListener("DOMContentLoaded", () => {
  const pickBtn   = document.getElementById("btn-photo-pick");
  const inputEl   = document.getElementById("photos");
  const previewEl = document.getElementById("photo-preview");

  if (!pickBtn || !inputEl || !previewEl) return;

  const MAX_FILES   = 10;
  const MAX_SIZE_MB = 5;

  // 누적 버퍼(프리뷰/삭제 UX용)
  let buffer;
  try { buffer = new DataTransfer(); } catch { buffer = null; }

  // 파일 선택창 열기
  pickBtn.addEventListener("click", () => inputEl.click());

  // 프리뷰 타일 생성(+ 삭제)
  function appendPreviewTile(file) {
    const url  = URL.createObjectURL(file);
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

  // 파일 선택 시: 검증 → 프리뷰 → 누적 (제출은 폼에서 한 번에)
  inputEl.addEventListener("change", (e) => {
    const selected = Array.from(e.target.files || []);
    if (!selected.length) return;

    const currentCount = buffer ? buffer.files.length : previewEl.querySelectorAll("img.preview-img").length;
    if (currentCount + selected.length > MAX_FILES) {
      alert(`이미지는 최대 ${MAX_FILES}장까지 업로드할 수 있어요.`);
      inputEl.value = "";
      return;
    }

    const tooLarge = selected.find(f => f.size > MAX_SIZE_MB * 1024 * 1024);
    if (tooLarge) {
      alert(`각 이미지 용량은 ${MAX_SIZE_MB}MB 이하여야 해요: ${tooLarge.name}`);
      inputEl.value = "";
      return;
    }

    selected.forEach(file => {
      if (!file.type.startsWith("image/")) return;
      if (buffer) buffer.items.add(file);
      appendPreviewTile(file);
    });

    if (buffer) {
      inputEl.files = buffer.files; // 누적 반영
    }
  });
});
