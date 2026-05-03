/**
 * CatConnect 채팅방 JS
 * STOMP/SockJS 기반 실시간 메시지 송수신
 */
(function () {
    var roomId = document.getElementById('roomId').value;
    var currentUsername = document.getElementById('currentUsername').value;
    var currentUserId = document.getElementById('currentUserId').value;
    var messagesEl = document.getElementById('chatMessages');
    var inputEl = document.getElementById('chatMessageInput');
    var sendBtn = document.getElementById('chatSendBtn');
    var fileInput = document.getElementById('chatFileInput');
    var partnerNameEl = document.getElementById('chatPartnerName');
    var menuBtn = document.getElementById('chatMenuBtn');
    var dropdownMenu = document.getElementById('chatDropdownMenu');
    var loadingEl = document.getElementById('chatLoading');

    var stompClient = null;
    var nextCursor = null;
    var hasMore = true;
    var isLoadingHistory = false;
    var lastDateShown = null;
    var otherUser = null;

    // 초기화
    function init() {
        loadRoomInfo();
        loadHistory();
        connectWebSocket();
        bindEvents();
    }

    function loadRoomInfo() {
        axios.get('/api/chat/rooms', { withCredentials: true })
            .then(function (res) {
                var rooms = res.data.data || [];
                var room = rooms.find(function (r) { return r.roomId == roomId; });
                if (room && room.otherUser) {
                    otherUser = room.otherUser;
                    partnerNameEl.textContent = otherUser.username;
                }
            })
            .catch(function () {
                partnerNameEl.textContent = '채팅';
            });
    }

    function loadHistory() {
        if (!hasMore || isLoadingHistory) return;
        isLoadingHistory = true;
        loadingEl.style.display = 'block';

        var url = '/api/chat/rooms/' + roomId + '/messages?size=50';
        if (nextCursor) url += '&cursor=' + nextCursor;

        axios.get(url, { withCredentials: true })
            .then(function (res) {
                var data = res.data.data;
                hasMore = data.hasMore;
                nextCursor = data.nextCursor;

                var msgs = data.messages || [];
                if (msgs.length === 0 && !nextCursor) {
                    loadingEl.style.display = 'none';
                    isLoadingHistory = false;
                    return;
                }

                var fragment = document.createDocumentFragment();
                var prevDate = null;
                msgs.forEach(function (msg) {
                    var msgDate = formatDateDivider(msg.createdAt);
                    if (msgDate !== prevDate) {
                        fragment.appendChild(createDateDivider(msgDate));
                        prevDate = msgDate;
                    }
                    fragment.appendChild(createMessageEl(msg));
                });

                var firstChild = messagesEl.firstChild;
                if (firstChild) {
                    messagesEl.insertBefore(fragment, firstChild);
                } else {
                    messagesEl.appendChild(fragment);
                }

                if (!nextCursor) {
                    // 첫 로드, 스크롤 맨 아래
                    messagesEl.scrollTop = messagesEl.scrollHeight;
                }

                // 읽음 처리
                if (msgs.length > 0) {
                    var lastMsg = msgs[msgs.length - 1];
                    markAsRead(lastMsg.messageId);
                }

                loadingEl.style.display = 'none';
                isLoadingHistory = false;
            })
            .catch(function () {
                loadingEl.style.display = 'none';
                isLoadingHistory = false;
            });
    }

    function connectWebSocket() {
        // 중복 연결 방지
        if (stompClient && stompClient.connected) {
            return;
        }

        var socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);
        stompClient.debug = null; // 디버그 로그 끄기

        // 쿠키에서 JWT 토큰 추출
        var token = getCookie('jwtToken');

        stompClient.connect({ token: token }, function () {
            // 채팅방 구독
            stompClient.subscribe('/topic/chat/' + roomId, function (message) {
                var body = JSON.parse(message.body);
                handleWebSocketMessage(body);
            });

            // 에러 구독
            stompClient.subscribe('/user/queue/errors', function (message) {
                var body = JSON.parse(message.body);
                if (body.payload && body.payload.message) {
                    showError(body.payload.message);
                }
            });
        }, function () {
            stompClient = null;
            // 연결 실패 시 3초 후 재연결
            setTimeout(connectWebSocket, 3000);
        });
    }

    function handleWebSocketMessage(msg) {
        if (msg.type === 'MESSAGE') {
            var payload = msg.payload;
            appendMessage(payload);
            messagesEl.scrollTop = messagesEl.scrollHeight;

            // 자동 읽음 처리
            if (payload.senderName !== currentUsername) {
                markAsRead(payload.messageId);
            }
        } else if (msg.type === 'READ_RECEIPT') {
            // 읽음 표시 갱신
            updateReadReceipts(msg.payload.lastReadMessageId);
        }
    }

    var isSending = false;

    function sendMessage() {
        var content = inputEl.value.trim();
        if (!content || !stompClient || !stompClient.connected || isSending) return;

        isSending = true;
        stompClient.send('/app/chat/send', {}, JSON.stringify({
            roomId: parseInt(roomId),
            content: content,
            messageType: 'TEXT',
            fileId: null
        }));

        inputEl.value = '';
        setTimeout(function () { isSending = false; }, 300);
    }

    function sendImageMessage(fileId) {
        if (!stompClient || !stompClient.connected) return;

        stompClient.send('/app/chat/send', {}, JSON.stringify({
            roomId: parseInt(roomId),
            content: null,
            messageType: 'IMAGE',
            fileId: fileId
        }));
    }

    function markAsRead(messageId) {
        if (stompClient && stompClient.connected) {
            stompClient.send('/app/chat/read', {}, JSON.stringify({
                roomId: parseInt(roomId),
                lastReadMessageId: messageId
            }));
        }
    }

    function appendMessage(msg) {
        var msgDate = formatDateDivider(msg.createdAt);
        if (msgDate !== lastDateShown) {
            messagesEl.appendChild(createDateDivider(msgDate));
            lastDateShown = msgDate;
        }
        messagesEl.appendChild(createMessageEl(msg));
    }

    function createMessageEl(msg) {
        if (msg.messageType === 'SYSTEM') {
            var sysEl = document.createElement('div');
            sysEl.className = 'chat-system-message';
            sysEl.textContent = msg.content;
            return sysEl;
        }

        var isMine = msg.senderName === currentUsername;
        var row = document.createElement('div');
        row.className = 'chat-msg-row' + (isMine ? ' mine' : '');
        row.dataset.messageId = msg.messageId;

        var bubble = document.createElement('div');
        bubble.className = 'chat-bubble ' + (isMine ? 'mine' : 'other');

        if (msg.messageType === 'IMAGE' && msg.fileUrl) {
            var img = document.createElement('img');
            img.className = 'chat-msg-image';
            img.src = msg.fileUrl;
            img.alt = '첨부 이미지';
            img.onerror = function () {
                var placeholder = document.createElement('div');
                placeholder.className = 'chat-msg-image-error';
                placeholder.textContent = '이미지를 불러올 수 없습니다';
                img.parentNode.replaceChild(placeholder, img);
            };
            img.onclick = function () {
                window.open(msg.fileUrl, '_blank');
            };
            bubble.appendChild(img);
        } else {
            bubble.textContent = msg.content || '';
        }

        // 컨텍스트 메뉴 (길게 누르기 / 우클릭)
        bubble.addEventListener('contextmenu', function (e) {
            e.preventDefault();
            showContextMenu(e, msg);
        });

        var timeEl = document.createElement('span');
        timeEl.className = 'chat-msg-time';
        timeEl.textContent = formatTime(msg.createdAt);

        if (isMine) {
            var readEl = document.createElement('span');
            readEl.className = 'chat-msg-read';
            readEl.dataset.msgId = msg.messageId;
            row.appendChild(readEl);
        }

        row.appendChild(bubble);
        row.appendChild(timeEl);

        return row;
    }

    function createDateDivider(dateStr) {
        var div = document.createElement('div');
        div.className = 'chat-date-divider';
        div.innerHTML = '<span>' + dateStr + '</span>';
        lastDateShown = dateStr;
        return div;
    }

    function showContextMenu(event, msg) {
        // 기존 컨텍스트 메뉴 제거
        var existing = document.querySelector('.chat-msg-context');
        if (existing) existing.remove();

        var menu = document.createElement('div');
        menu.className = 'chat-msg-context';
        menu.style.left = event.pageX + 'px';
        menu.style.top = event.pageY + 'px';

        if (msg.messageType === 'TEXT' && msg.content) {
            var copyBtn = document.createElement('button');
            copyBtn.textContent = '복사';
            copyBtn.onclick = function () {
                navigator.clipboard.writeText(msg.content);
                menu.remove();
                if (typeof UI !== 'undefined') UI.success('복사되었습니다');
            };
            menu.appendChild(copyBtn);
        }

        if (msg.senderName !== currentUsername) {
            var reportBtn = document.createElement('button');
            reportBtn.textContent = '신고';
            reportBtn.onclick = function () {
                menu.remove();
                reportMessage(msg.messageId);
            };
            menu.appendChild(reportBtn);
        }

        document.body.appendChild(menu);

        // 외부 클릭 시 닫기
        setTimeout(function () {
            document.addEventListener('click', function closeCtx() {
                menu.remove();
                document.removeEventListener('click', closeCtx);
            });
        }, 0);
    }

    function updateReadReceipts(lastReadMessageId) {
        var readEls = document.querySelectorAll('.chat-msg-read');
        readEls.forEach(function (el) {
            var msgId = parseInt(el.dataset.msgId);
            if (msgId <= lastReadMessageId) {
                el.textContent = '✓✓';
            }
        });
    }

    function showError(message) {
        if (typeof UI !== 'undefined' && UI.error) {
            UI.error(message);
        } else {
            alert(message);
        }
    }

    function reportMessage(messageId) {
        if (typeof UI !== 'undefined' && UI.confirm) {
            UI.confirm('이 메시지를 신고하시겠습니까?', function () {
                axios.post('/api/reports', {
                    targetType: 'MESSAGE',
                    targetId: messageId,
                    reason: 'ABUSE',
                    detail: ''
                }, { withCredentials: true })
                    .then(function () { UI.success('신고가 접수되었습니다.'); })
                    .catch(function (err) {
                        var msg = (err.response && err.response.data && err.response.data.message) || '신고에 실패했습니다.';
                        UI.error(msg);
                    });
            });
        }
    }

    function bindEvents() {
        sendBtn.addEventListener('click', sendMessage);
        inputEl.addEventListener('keydown', function (e) {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                sendMessage();
            }
        });

        // 이미지 업로드
        fileInput.addEventListener('change', function () {
            var file = fileInput.files[0];
            if (!file) return;

            var formData = new FormData();
            formData.append('file', file);
            formData.append('fileType', 'CHAT');
            formData.append('referenceId', roomId);

            axios.post('/api/files/upload', formData, {
                withCredentials: true,
                headers: { 'Content-Type': 'multipart/form-data' }
            })
                .then(function (res) {
                    var fileData = res.data.data;
                    sendImageMessage(fileData.id);
                })
                .catch(function (err) {
                    var msg = (err.response && err.response.data && err.response.data.message) || '업로드에 실패했습니다.';
                    showError(msg);
                });

            fileInput.value = '';
        });

        // 스크롤 이벤트 (이전 메시지 로드)
        messagesEl.addEventListener('scroll', function () {
            if (messagesEl.scrollTop < 50 && hasMore && !isLoadingHistory) {
                loadHistory();
            }
        });

        // 더보기 메뉴
        menuBtn.addEventListener('click', function (e) {
            e.stopPropagation();
            dropdownMenu.style.display = dropdownMenu.style.display === 'none' ? 'block' : 'none';
        });

        document.addEventListener('click', function () {
            dropdownMenu.style.display = 'none';
        });

        // 차단
        document.getElementById('btnBlockUser').addEventListener('click', function () {
            if (!otherUser) return;
            if (typeof UI !== 'undefined' && UI.confirm) {
                UI.confirm('이 사용자를 차단하시겠습니까?', function () {
                    axios.post('/api/blocks', { targetUserId: otherUser.id }, { withCredentials: true })
                        .then(function () {
                            UI.success('차단되었습니다.');
                            dropdownMenu.style.display = 'none';
                        })
                        .catch(function (err) {
                            UI.handleApiError(err, '차단에 실패했습니다.');
                        });
                });
            }
        });

        // 나가기
        document.getElementById('btnLeaveRoom').addEventListener('click', function () {
            if (typeof UI !== 'undefined' && UI.confirm) {
                UI.confirm('채팅방을 나가시겠습니까?', function () {
                    axios.post('/api/chat/rooms/' + roomId + '/leave', {}, { withCredentials: true })
                        .then(function () {
                            window.location.href = '/inbox';
                        })
                        .catch(function (err) {
                            UI.handleApiError(err, '나가기에 실패했습니다.');
                        });
                });
            }
        });

        // 신고
        document.getElementById('btnReportUser').addEventListener('click', function () {
            if (!otherUser) return;
            if (typeof UI !== 'undefined' && UI.confirm) {
                UI.confirm('이 사용자를 신고하시겠습니까?', function () {
                    axios.post('/api/reports', {
                        targetType: 'CHAT_USER',
                        targetId: otherUser.id,
                        reason: 'ABUSE',
                        detail: ''
                    }, { withCredentials: true })
                        .then(function () { UI.success('신고가 접수되었습니다.'); })
                        .catch(function (err) {
                            UI.handleApiError(err, '신고에 실패했습니다.');
                        });
                });
            }
            dropdownMenu.style.display = 'none';
        });
    }

    // 유틸리티
    function formatTime(dateStr) {
        var d = new Date(dateStr);
        var h = d.getHours();
        var m = String(d.getMinutes()).padStart(2, '0');
        var ampm = h >= 12 ? '오후' : '오전';
        h = h % 12 || 12;
        return ampm + ' ' + h + ':' + m;
    }

    function formatDateDivider(dateStr) {
        var d = new Date(dateStr);
        var days = ['일', '월', '화', '수', '목', '금', '토'];
        return d.getFullYear() + '년 ' + (d.getMonth() + 1) + '월 ' + d.getDate() + '일 (' + days[d.getDay()] + ')';
    }

    function getCookie(name) {
        var value = '; ' + document.cookie;
        var parts = value.split('; ' + name + '=');
        if (parts.length === 2) return parts.pop().split(';').shift();
        return null;
    }

    // 실행
    document.addEventListener('DOMContentLoaded', init);
})();
