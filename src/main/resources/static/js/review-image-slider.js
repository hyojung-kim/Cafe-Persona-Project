// /js/review-image-slider.js

document.addEventListener('DOMContentLoaded', function () {
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
});
