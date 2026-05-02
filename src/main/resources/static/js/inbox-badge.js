/**
 * CatConnect 인박스 배지 polling
 * 30초 간격으로 미확인 카운트를 서버에서 가져와 배지 갱신
 */
(function () {
    var POLL_INTERVAL = 30000;
    var badgeEl = null;
    var currentCount = 0;

    function init() {
        badgeEl = document.getElementById('inboxBadge');
        if (!badgeEl) return;

        // 초기 카운트는 서버사이드 렌더링으로 이미 표시됨
        var initialCount = parseInt(badgeEl.textContent) || 0;
        currentCount = initialCount;
        updateBadgeVisibility();

        // polling 시작
        setInterval(fetchUnreadCount, POLL_INTERVAL);
    }

    function fetchUnreadCount() {
        var xhr = new XMLHttpRequest();
        xhr.open('GET', '/api/inbox/unread-count', true);
        xhr.withCredentials = true;
        xhr.onreadystatechange = function () {
            if (xhr.readyState === 4 && xhr.status === 200) {
                try {
                    var res = JSON.parse(xhr.responseText);
                    var count = (res.data && res.data.count) || 0;
                    if (count !== currentCount) {
                        currentCount = count;
                        updateBadge(count);
                    }
                } catch (e) { /* ignore */ }
            }
        };
        xhr.send();
    }

    function updateBadge(count) {
        if (!badgeEl) return;

        if (count <= 0) {
            badgeEl.style.opacity = '0';
            setTimeout(function () {
                badgeEl.style.display = 'none';
                badgeEl.textContent = '';
            }, 150);
        } else {
            badgeEl.style.display = 'inline-block';
            badgeEl.style.opacity = '1';
            badgeEl.textContent = count > 9 ? '9+' : count;

            // 펄스 효과
            badgeEl.classList.remove('pulse');
            void badgeEl.offsetWidth; // reflow 트리거
            badgeEl.classList.add('pulse');
        }
    }

    function updateBadgeVisibility() {
        if (!badgeEl) return;
        if (currentCount <= 0) {
            badgeEl.style.display = 'none';
        } else {
            badgeEl.style.display = 'inline-block';
        }
    }

    document.addEventListener('DOMContentLoaded', init);
})();
