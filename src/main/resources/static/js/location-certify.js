let map;
let currentMarker;
let cafeLat, cafeLng;
let watchId;
let stayTimer = null;
let certified = false;

const DISTANCE_THRESHOLD = 50000000; // meters
const STAY_DURATION = 2000; // milliseconds

function initMap() {
    const mapContainer = document.getElementById('map');
    const options = { center: new kakao.maps.LatLng(37.5665,126.9780), level:3 };
    map = new kakao.maps.Map(mapContainer, options);

    const places = new kakao.maps.services.Places();
    if (cafeAddress) {
        places.keywordSearch(cafeAddress, function(result, status){
            if (status === kakao.maps.services.Status.OK) {
                const first = result[0];
                cafeLat = parseFloat(first.y);
                cafeLng = parseFloat(first.x);
                const pos = new kakao.maps.LatLng(cafeLat, cafeLng);
                new kakao.maps.Marker({map:map, position:pos});
                map.setCenter(pos);
                document.getElementById('cafeLocation').textContent = '카페 위치: ' + cafeAddress;
            }
        });
    }

    if (navigator.geolocation) {
        watchId = navigator.geolocation.watchPosition(updateLocation, handleError, {enableHighAccuracy:true});
    } else {
        document.getElementById('myLocation').textContent = '이 브라우저는 위치 정보를 지원하지 않습니다.';
    }
}

function updateLocation(position) {
    const lat = position.coords.latitude;
    const lng = position.coords.longitude;
    const pos = new kakao.maps.LatLng(lat, lng);
    if (currentMarker) {
        currentMarker.setMap(null);
    }
    currentMarker = new kakao.maps.Marker({map:map, position:pos});

    document.getElementById('myLocation').textContent = '내 위치: ' + lat.toFixed(6) + ', ' + lng.toFixed(6);

    if (cafeLat && cafeLng) {
        const dist = calculateDistance(lat, lng, cafeLat, cafeLng);
        document.getElementById('distanceInfo').textContent = '카페까지 거리: ' + Math.round(dist) + 'm';

        if (!certified) {
            if (dist <= DISTANCE_THRESHOLD) {
                if (!stayTimer) {
                    stayTimer = setTimeout(() => {
                        certified = true;
                        onCertificationSuccess();
                    }, STAY_DURATION);
                }
            } else {
                if (stayTimer) {
                    clearTimeout(stayTimer);
                    stayTimer = null;
                }
            }
        }
    }
}

function handleError(err) {
    console.error(err);
    document.getElementById('myLocation').textContent = '위치를 가져올 수 없습니다.';
}

function calculateDistance(lat1, lon1, lat2, lon2) {
    const R = 6371000;
    const toRad = v => v * Math.PI / 180;
    const dLat = toRad(lat2 - lat1);
    const dLon = toRad(lon2 - lon1);
    const a = Math.sin(dLat/2) * Math.sin(dLat/2) +
              Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) *
              Math.sin(dLon/2) * Math.sin(dLon/2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    return R * c;
}

function onCertificationSuccess() {
    alert('위치 인증이 성공했습니다.');
    if (watchId) {
        navigator.geolocation.clearWatch(watchId);
    }
    if (typeof cafeId !== 'undefined') {
        localStorage.setItem('certifiedCafe_' + cafeId, 'true');
        window.location.href = '/cafes/' + cafeId + '/reviews';
    }
}

function loadKakao() {
    if (window.kakao && window.kakao.maps) {
        window.kakao.maps.load(initMap);
    } else {
        setTimeout(loadKakao, 50);
    }
}

loadKakao();
