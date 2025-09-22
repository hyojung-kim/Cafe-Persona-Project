// /js/review-image-slider.js

document.addEventListener('DOMContentLoaded', function () {
  syncReviewImageAspectRatios();
  initReviewImageSlider();
  initReviewImageMagnifier();
});

function syncReviewImageAspectRatios() {
  document.querySelectorAll('.review-image-wrapper').forEach(function (wrapper) {
    var img = wrapper.querySelector('.review-image');
    if (!img) {
      return;
    }

    function applyRatio() {
      if (!img.naturalWidth || !img.naturalHeight) {
        return;
      }
      // 리뷰 상세에서는 이미지를 항상 정사각형으로 보여주기 위해
      // 기본 CSS에서 지정한 1:1 비율을 그대로 유지한다.
      wrapper.style.setProperty('--image-aspect-ratio', '1');
    }

    if (img.complete) {
      applyRatio();
      if (!img.naturalWidth || !img.naturalHeight) {
        img.addEventListener('load', applyRatio, { once: true });
      }
    } else {
      img.addEventListener('load', applyRatio, { once: true });
    }
  });
}

function initReviewImageSlider() {
  document.querySelectorAll('.review-image-slider').forEach(function (slider) {
    var track = slider.querySelector('.slider-track');
    var slides = slider.querySelectorAll('.slide');
    var prevBtn = slider.querySelector('.prev-btn');
    var nextBtn = slider.querySelector('.next-btn');
    var index = 0;
    var visible = parseInt(slider.dataset.visible || '3', 10);
    var slideWidth = 0;

    function applyWidths() {
      slideWidth = slider.clientWidth / visible;
      slides.forEach(function (s) { s.style.width = slideWidth + 'px'; });
    }

    function update() {
      if (slides.length <= visible) {
        prevBtn.style.display = 'none';
        nextBtn.style.display = 'none';
        track.style.transform = 'translateX(0)';
        return;
      }
      track.style.transform = 'translateX(' + (-index * slideWidth) + 'px)';
      prevBtn.disabled = index === 0;
      nextBtn.disabled = index >= slides.length - visible;
    }

    prevBtn.addEventListener('click', function () {
      if (index > 0) {
        index--;
        update();
      }
    });

    nextBtn.addEventListener('click', function () {
      if (index < slides.length - visible) {
        index++;
        update();
      }
    });

    window.addEventListener('resize', function () {
      applyWidths();
      update();
    });

    applyWidths();
    update();
  });
}

function initReviewImageMagnifier() {
  var lensSize = 160;
  var zoomLevel = 2.2;

  document.querySelectorAll('.review-image-wrapper').forEach(function (wrapper) {
    var img = wrapper.querySelector('.review-image');
    if (!img) {
      return;
    }

    var lens = document.createElement('div');
    lens.className = 'review-magnifier';
    lens.setAttribute('aria-hidden', 'true');
    lens.style.width = lensSize + 'px';
    lens.style.height = lensSize + 'px';

    function updateLensBackground() {
      var rect = img.getBoundingClientRect();
      lens.style.backgroundImage = 'url(' + (img.currentSrc || img.src) + ')';
      if (rect.width > 0 && rect.height > 0) {
        lens.style.backgroundSize = (rect.width * zoomLevel) + 'px ' + (rect.height * zoomLevel) + 'px';
      }
    }

    function moveLens(clientX, clientY) {
      var rect = img.getBoundingClientRect();
      if (!rect.width || !rect.height) {
        return;
      }

      var offsetX = Math.max(0, Math.min(clientX - rect.left, rect.width));
      var offsetY = Math.max(0, Math.min(clientY - rect.top, rect.height));

      lens.style.left = offsetX + 'px';
      lens.style.top = offsetY + 'px';
      lens.style.backgroundPosition = (offsetX / rect.width) * 100 + '% ' + (offsetY / rect.height) * 100 + '%';
    }

    function showLens(clientX, clientY) {
      updateLensBackground();
      if (!wrapper.contains(lens)) {
        wrapper.appendChild(lens);
        requestAnimationFrame(function () {
          lens.classList.add('is-visible');
        });
      }
      moveLens(clientX, clientY);
    }

    function hideLens() {
      if (!wrapper.contains(lens)) {
        return;
      }
      lens.classList.remove('is-visible');
      window.setTimeout(function () {
        if (wrapper.contains(lens) && !lens.classList.contains('is-visible')) {
          wrapper.removeChild(lens);
        }
      }, 160);
    }

    wrapper.addEventListener('pointerenter', function (event) {
      if (event.pointerType !== 'mouse') {
        return;
      }
      showLens(event.clientX, event.clientY);
    });

    wrapper.addEventListener('pointermove', function (event) {
      if (event.pointerType !== 'mouse') {
        return;
      }
      if (!wrapper.contains(lens)) {
        showLens(event.clientX, event.clientY);
      } else {
        moveLens(event.clientX, event.clientY);
      }
    });

    wrapper.addEventListener('pointerleave', function (event) {
      if (event.pointerType !== 'mouse') {
        return;
      }
      hideLens();
    });

    wrapper.addEventListener('pointercancel', function (event) {
      if (event.pointerType !== 'mouse') {
        return;
      }
      hideLens();
    });

    window.addEventListener('resize', updateLensBackground);
    img.addEventListener('load', updateLensBackground);
  });
}
