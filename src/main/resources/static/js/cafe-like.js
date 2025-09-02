// /static/js/cafe-like.js
$(function () {
const box = $('#likeBox');
if (!box.length) return;

const cafeId  = box.data('cafe-id'); // 이제 숫자(or 문자열)로 정상 주입됨
const btn     = $('#likeBtn');
const countEl = $('#likeCount');

function setLikedUI(liked) {
box.attr('data-liked', liked ? 'true' : 'false');
btn.html(liked ? '<span class="label-liked">♥ 좋아요 취소</span>'
: '<span class="label-unliked">♡ 좋아요</span>');
}

btn.on('click', function () {
btn.prop('disabled', true);
$.ajax({
url: "/cafe/like/" + cafeId,
type: "GET",
cache: false,
success: function (response, status, xhr) {
const finalURL = xhr.responseURL || '';
if (finalURL.includes('/user/login') ||
(typeof response === 'string' && response.trim().startsWith('<!DOCTYPE'))) {
          window.location.href = '/user/login';
          return;
        }
        const n = parseInt(response, 10);
        if (!Number.isNaN(n)) countEl.text(n.toString());
        const likedNow = (box.attr('data-liked') === 'true');
        setLikedUI(!likedNow);
      },
      error: function (xhr) {
        if (xhr.status === 401 || xhr.status === 403) {
          window.location.href = '/user/login';
        } else {
          console.error('좋아요 오류:', xhr.status, xhr.responseText);
          alert('좋아요 처리 중 오류가 발생했어요.');
        }
      },
      complete: function () { btn.prop('disabled', false); }
        });
        });
        });