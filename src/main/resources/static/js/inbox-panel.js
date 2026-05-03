/**
 * CatConnect 인박스 패널 로직
 * 드롭다운 열기/닫기, 목록 fetch, 읽음/삭제/고정 처리
 */
var InboxPanel = (function () {
    var isFullPage = false;
    var currentFilter = '';
    var panelEl = null;
    var listEl = null;
    var emptyEl = null;

    function init(fullPage) {
        isFullPage = fullPage || false;

        if (isFullPage) {
            listEl = document.getElementById('inboxList');
            emptyEl = document.getElementById('inboxEmpty');
        } else {
            panelEl = document.getElementById('inboxPanel');
            listEl = panelEl ? panelEl.querySelector('.inbox-list') : null;
            emptyEl = panelEl ? panelEl.querySelector('.inbox-empty') : null;
        }

        bindTabs();
        bindMarkAllRead();
        bindSupportBtn();

        if (isFullPage) {
            loadItems();
        }
    }

    function toggle() {
        if (!panelEl) return;
        var isShown = panelEl.classList.contains('show');
        if (isShown) {
            panelEl.classList.remove('show');
        } else {
            panelEl.classList.add('show');
            loadItems();
        }
    }

    function close() {
        if (panelEl) panelEl.classList.remove('show');
    }

    function loadItems() {
        if (!listEl) return;
        var url = '/api/inbox?size=20';
        if (currentFilter) url += '&type=' + currentFilter;

        axios.get(url, { withCredentials: true })
            .then(function (res) {
                var items = res.data.data || [];
                renderItems(items);
            })
            .catch(function () {
                listEl.innerHTML = '<div class="inbox-empty"><p>로딩에 실패했습니다</p></div>';
            });
    }

    function renderItems(items) {
        listEl.innerHTML = '';

        if (items.length === 0) {
            if (emptyEl) emptyEl.style.display = 'block';
            return;
        }
        if (emptyEl) emptyEl.style.display = 'none';

        items.forEach(function (item) {
            var el = createItemEl(item);
            listEl.appendChild(el);
        });
    }

    function createItemEl(item) {
        var div = document.createElement('div');
        div.className = 'inbox-item' + (item.read ? '' : ' unread');
        div.dataset.id = item.id;

        var avatar = document.createElement('div');
        avatar.className = 'inbox-item-avatar';
        if (item.senderProfileImage) {
            avatar.innerHTML = '<img src="' + item.senderProfileImage + '" alt="">';
        } else {
            avatar.textContent = getTypeIcon(item.itemType);
        }

        var content = document.createElement('div');
        content.className = 'inbox-item-content';

        var title = document.createElement('div');
        title.className = 'inbox-item-title';
        title.textContent = (item.pinned ? '📌 ' : '') + (item.title || item.senderName || '알림');

        var preview = document.createElement('div');
        preview.className = 'inbox-item-preview';
        preview.textContent = item.preview || '';

        content.appendChild(title);
        content.appendChild(preview);

        var meta = document.createElement('div');
        meta.className = 'inbox-item-meta';

        var time = document.createElement('div');
        time.className = 'inbox-item-time';
        time.textContent = formatRelativeTime(item.updatedAt);
        meta.appendChild(time);

        div.appendChild(avatar);
        div.appendChild(content);
        div.appendChild(meta);

        div.addEventListener('click', function () {
            handleItemClick(item);
        });

        return div;
    }

    function handleItemClick(item) {
        // 읽음 처리
        if (!item.read) {
            axios.post('/api/inbox/' + item.id + '/read', {}, { withCredentials: true }).catch(function(){});
        }

        // 이동
        if (item.itemType === 'CHAT' && item.referenceId) {
            window.location.href = '/chat/' + item.referenceId;
        } else if (item.linkUrl) {
            window.location.href = item.linkUrl;
        }
    }

    function bindTabs() {
        var tabs = document.querySelectorAll('.inbox-tab');
        tabs.forEach(function (tab) {
            tab.addEventListener('click', function () {
                tabs.forEach(function (t) { t.classList.remove('active'); });
                tab.classList.add('active');
                currentFilter = tab.dataset.type || '';
                loadItems();
            });
        });
    }

    function bindMarkAllRead() {
        var btn = document.getElementById('markAllReadBtn');
        if (!btn) return;
        btn.addEventListener('click', function () {
            axios.post('/api/inbox/read-all', {}, { withCredentials: true })
                .then(function () {
                    loadItems();
                    // 배지 갱신
                    var badge = document.getElementById('inboxBadge');
                    if (badge) {
                        badge.style.display = 'none';
                        badge.textContent = '';
                    }
                })
                .catch(function () {});
        });
    }

    function bindSupportBtn() {
        var btn = document.getElementById('supportChatBtn');
        if (!btn) return;
        btn.addEventListener('click', function (e) {
            e.preventDefault();
            // 관리자 ID는 1번으로 가정 (MVP)
            axios.post('/api/chat/rooms', { targetUserId: 1, roomType: 'SUPPORT' }, { withCredentials: true })
                .then(function (res) {
                    var room = res.data.data;
                    window.location.href = '/chat/' + room.roomId;
                })
                .catch(function (err) {
                    var msg = (err.response && err.response.data && err.response.data.message) || '문의 생성에 실패했습니다.';
                    if (typeof UI !== 'undefined') UI.error(msg);
                });
        });
    }

    function getTypeIcon(type) {
        switch (type) {
            case 'CHAT': return '💬';
            case 'COMMENT': return '💬';
            case 'LIKE': return '♡';
            case 'REPORT_RESULT': return '⚖️';
            case 'SYSTEM': return '📢';
            case 'NOTICE': return '📢';
            default: return '💬';
        }
    }

    function formatRelativeTime(dateStr) {
        if (!dateStr) return '';
        var now = new Date();
        var d = new Date(dateStr);
        var diff = Math.floor((now - d) / 1000);

        if (diff < 60) return '방금';
        if (diff < 3600) return Math.floor(diff / 60) + '분 전';
        if (diff < 86400) return Math.floor(diff / 3600) + '시간 전';

        var isToday = now.toDateString() === d.toDateString();
        if (isToday) return Math.floor(diff / 3600) + '시간 전';

        var yesterday = new Date(now);
        yesterday.setDate(yesterday.getDate() - 1);
        if (yesterday.toDateString() === d.toDateString()) return '어제';

        return (d.getMonth() + 1) + '/' + d.getDate();
    }

    return {
        init: init,
        toggle: toggle,
        close: close,
        loadItems: loadItems
    };
})();
