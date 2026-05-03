/**
 * CatConnect 인박스 배지 + SSE 실시간 알림
 * - SSE로 실시간 배지 갱신 + 토스트 알림
 * - SSE 연결 실패 시 30초 polling fallback
 */
(function () {
    var POLL_INTERVAL = 30000;
    var badgeEl = null;
    var currentCount = 0;
    var sseConnected = false;
    var pollTimer = null;

    function init() {
        badgeEl = document.getElementById('inboxBadge');
        // badgeEl이 없으면 비로그인 상태이므로 종료
        if (!badgeEl) return;

        var initialCount = parseInt(badgeEl.textContent) || 0;
        currentCount = initialCount;
        updateBadgeVisibility();

        // SSE 연결 시도 (로그인 상태에서 항상 시작)
        connectSSE();
    }

    // === SSE 연결 ===
    function connectSSE() {
        if (typeof EventSource === 'undefined') {
            startPolling();
            return;
        }

        var eventSource = new EventSource('/api/sse/notifications');

        eventSource.addEventListener('connect', function () {
            sseConnected = true;
            // SSE 연결 성공 시 polling 중지
            if (pollTimer) {
                clearInterval(pollTimer);
                pollTimer = null;
            }
        });

        eventSource.addEventListener('chat', function (event) {
            try {
                var data = JSON.parse(event.data);
                if (data.unreadCount !== undefined) {
                    currentCount = data.unreadCount;
                    updateBadge(currentCount);
                }
                // 토스트 알림 (알림 설정 확인)
                if (isNotificationEnabled('chat') && data.senderName) {
                    showNotificationToast(data.senderName, data.roomId);
                }
            } catch (e) { /* ignore */ }
        });

        eventSource.onerror = function () {
            sseConnected = false;
            eventSource.close();
            // 5초 후 재연결 시도, 실패 시 polling fallback
            setTimeout(function () {
                if (!sseConnected) {
                    startPolling();
                    // 30초 후 SSE 재시도
                    setTimeout(connectSSE, 30000);
                }
            }, 5000);
        };
    }

    // === Polling fallback ===
    function startPolling() {
        if (pollTimer) return;
        pollTimer = setInterval(fetchUnreadCount, POLL_INTERVAL);
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

    // === 배지 갱신 ===
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

            badgeEl.classList.remove('pulse');
            void badgeEl.offsetWidth;
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

    // === 토스트 알림 ===
    function showNotificationToast(senderName, roomId) {
        // 기존 토스트 제거
        var existing = document.querySelector('.chat-notification-toast');
        if (existing) existing.remove();

        var toast = document.createElement('div');
        toast.className = 'chat-notification-toast';
        toast.innerHTML = '<div class="chat-toast-icon">💬</div>' +
            '<div class="chat-toast-content">' +
            '<strong>' + escapeHtml(senderName) + '</strong>' +
            '<span>새로운 메시지가 도착했습니다</span>' +
            '</div>';

        toast.addEventListener('click', function () {
            window.location.href = '/chat/' + roomId;
        });

        document.body.appendChild(toast);

        // 등장 애니메이션
        requestAnimationFrame(function () {
            toast.classList.add('show');
        });

        // 2초 후 사라짐
        setTimeout(function () {
            toast.classList.remove('show');
            setTimeout(function () { toast.remove(); }, 300);
        }, 2000);
    }

    // === 알림 설정 확인 ===
    function isNotificationEnabled(type) {
        try {
            var settings = JSON.parse(localStorage.getItem('notificationSettings') || '{}');
            // 기본값: 활성화
            return settings[type] !== false;
        } catch (e) {
            return true;
        }
    }

    // === 유틸 ===
    function escapeHtml(str) {
        if (!str) return '';
        var div = document.createElement('div');
        div.appendChild(document.createTextNode(str));
        return div.innerHTML;
    }

    // 전역 접근 가능하도록 노출 (알림 설정 페이지에서 사용)
    window.NotificationSettings = {
        get: function () {
            try {
                return JSON.parse(localStorage.getItem('notificationSettings') || '{}');
            } catch (e) { return {}; }
        },
        set: function (type, enabled) {
            var settings = this.get();
            settings[type] = enabled;
            localStorage.setItem('notificationSettings', JSON.stringify(settings));
        },
        isEnabled: function (type) {
            var settings = this.get();
            return settings[type] !== false;
        }
    };

    document.addEventListener('DOMContentLoaded', init);
})();
