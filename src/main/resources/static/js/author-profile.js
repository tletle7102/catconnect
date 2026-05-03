var currentProfileUsername = null;
var currentProfileUserId = null;

// 탭 이벤트 리스너 바인딩 (DOMContentLoaded 후)
document.addEventListener('DOMContentLoaded', function () {
    var tabBoards = document.getElementById('tab-boards');
    var tabComments = document.getElementById('tab-comments');
    if (tabBoards) {
        tabBoards.addEventListener('click', function () {
            if (currentProfileUsername) {
                loadAuthorBoards(currentProfileUsername, 0);
            }
        });
    }
    if (tabComments) {
        tabComments.addEventListener('click', function () {
            if (currentProfileUsername) {
                loadAuthorComments(currentProfileUsername, 0);
            }
        });
    }
});

/**
 * HTML 이스케이프 유틸
 */
function escapeHtml(str) {
    if (!str) return '';
    var div = document.createElement('div');
    div.appendChild(document.createTextNode(str));
    return div.innerHTML;
}

/**
 * 날짜 포맷 함수 (모달용)
 * - 오늘이면 HH:mm
 * - 오늘이 아니면 YYYY.MM.DD
 */
function formatDate(dateStr) {
    var date = new Date(dateStr);
    var now = new Date();
    var isToday = date.getFullYear() === now.getFullYear()
        && date.getMonth() === now.getMonth()
        && date.getDate() === now.getDate();
    if (isToday) {
        return String(date.getHours()).padStart(2, '0') + ':' + String(date.getMinutes()).padStart(2, '0');
    } else {
        return date.getFullYear() + '.' + String(date.getMonth() + 1).padStart(2, '0') + '.' + String(date.getDate()).padStart(2, '0');
    }
}

/**
 * 작성자 프로필 모달 열기
 * 비로그인 사용자는 로그인 필요 모달 표시
 */
function showAuthorProfile(username) {
    // 인증 체크: 각 페이지에서 설정한 전역 변수 확인
    var authenticated = (typeof isAuthenticated !== 'undefined' && isAuthenticated)
        || (typeof currentUsername !== 'undefined' && currentUsername !== null);
    if (!authenticated) {
        if (typeof UI !== 'undefined' && UI.showLoginRequired) {
            UI.showLoginRequired();
        }
        return;
    }

    currentProfileUsername = username;
    currentProfileUserId = null;
    var modal = new bootstrap.Modal(document.getElementById('authorProfileModal'));

    // 채팅 버튼 표시/숨김 (본인이면 숨김)
    var chatBtn = document.getElementById('btnStartChat');
    var actionsEl = document.getElementById('authorProfileActions');
    if (chatBtn && actionsEl) {
        if (typeof currentUsername !== 'undefined' && currentUsername === username) {
            actionsEl.style.display = 'none';
        } else {
            actionsEl.style.display = 'flex';
        }
    }

    // Reset content
    document.getElementById('authorProfileModalLabel').textContent = username;
    document.getElementById('authorBoardsList').innerHTML = '<li class="list-group-item text-center text-muted">로딩 중...</li>';
    document.getElementById('authorCommentsList').innerHTML = '';
    document.getElementById('authorBoardsPagination').innerHTML = '';
    document.getElementById('authorCommentsPagination').innerHTML = '';

    // Reset tabs to default (작성글)
    var boardsTab = document.getElementById('tab-boards');
    var commentsTab = document.getElementById('tab-comments');
    boardsTab.classList.add('active');
    commentsTab.classList.remove('active');
    document.getElementById('tabPanelBoards').classList.add('show', 'active');
    document.getElementById('tabPanelComments').classList.remove('show', 'active');

    modal.show();

    // Fetch profile info to get the display username for modal title
    axios.get('/api/users/profile/' + encodeURIComponent(username), { withCredentials: true })
        .then(function (response) {
            var profile = response.data.data || response.data;
            document.getElementById('authorProfileModalLabel').textContent = profile.username || username;
            currentProfileUserId = profile.id || null;
        })
        .catch(function (error) {
            console.error("프로필 조회 실패", error);
            document.getElementById('authorProfileModalLabel').textContent = username;
        });

    // Load boards tab by default
    loadAuthorBoards(username, 0);
}

/**
 * 작성자 게시글 목록 로드
 */
function loadAuthorBoards(username, page) {
    if (typeof page === 'undefined') page = 0;
    var listEl = document.getElementById('authorBoardsList');
    var paginationEl = document.getElementById('authorBoardsPagination');
    listEl.innerHTML = '<li class="list-group-item text-center text-muted">로딩 중...</li>';
    paginationEl.innerHTML = '';

    axios.get('/api/users/profile/' + encodeURIComponent(username) + '/boards', {
        params: { page: page, size: 10 },
        withCredentials: true
    })
        .then(function (response) {
            var pageData = response.data.data || response.data;
            var items = pageData.content || [];
            listEl.innerHTML = '';

            if (items.length === 0) {
                listEl.innerHTML = '<li class="list-group-item text-center text-muted">작성한 게시글이 없습니다.</li>';
                return;
            }

            items.forEach(function (board) {
                var li = document.createElement('li');
                li.className = 'list-group-item';
                li.innerHTML =
                    '<a href="/boards/' + board.id + '">' + escapeHtml(board.title) + '</a>'
                    + '<div class="item-meta">' + formatDate(board.createdDttm) + ' | 조회 ' + (board.viewCount || 0) + '</div>';
                listEl.appendChild(li);
            });

            // Pagination
            renderAuthorPagination(paginationEl, pageData, function (p) {
                loadAuthorBoards(username, p);
            });
        })
        .catch(function (error) {
            console.error("작성자 게시글 조회 실패", error);
            listEl.innerHTML = '<li class="list-group-item text-center text-danger">게시글을 불러올 수 없습니다.</li>';
        });
}

/**
 * 작성자 댓글단 글 목록 로드
 */
function loadAuthorComments(username, page) {
    if (typeof page === 'undefined') page = 0;
    var listEl = document.getElementById('authorCommentsList');
    var paginationEl = document.getElementById('authorCommentsPagination');
    listEl.innerHTML = '<li class="list-group-item text-center text-muted">로딩 중...</li>';
    paginationEl.innerHTML = '';

    axios.get('/api/users/profile/' + encodeURIComponent(username) + '/comments', {
        params: { page: page, size: 10 },
        withCredentials: true
    })
        .then(function (response) {
            var pageData = response.data.data || response.data;
            var items = pageData.content || [];
            listEl.innerHTML = '';

            if (items.length === 0) {
                listEl.innerHTML = '<li class="list-group-item text-center text-muted">댓글단 글이 없습니다.</li>';
                return;
            }

            items.forEach(function (item) {
                var li = document.createElement('li');
                li.className = 'list-group-item';
                var commentText = item.content || '';
                var truncated = commentText.length > 50 ? commentText.substring(0, 50) + '...' : commentText;
                li.innerHTML =
                    '<a href="/boards/' + item.boardId + '">' + escapeHtml(item.boardTitle || '(제목 없음)') + '</a>'
                    + '<div class="item-meta">'
                    + escapeHtml(truncated) + ' | '
                    + formatDate(item.createdDttm)
                    + '</div>';
                listEl.appendChild(li);
            });

            // Pagination
            renderAuthorPagination(paginationEl, pageData, function (p) {
                loadAuthorComments(username, p);
            });
        })
        .catch(function (error) {
            console.error("작성자 댓글 조회 실패", error);
            listEl.innerHTML = '<li class="list-group-item text-center text-danger">댓글 목록을 불러올 수 없습니다.</li>';
        });
}

/**
 * 모달 내부 페이지네이션 렌더링
 */
function renderAuthorPagination(container, pageData, onPageClick) {
    container.innerHTML = '';
    var totalPages = pageData.totalPages || 0;
    if (totalPages <= 1) return;

    var currentPageNum = pageData.number || 0;
    var nav = document.createElement('nav');
    var ul = document.createElement('ul');
    ul.className = 'pagination pagination-sm';

    // 이전
    var prevLi = document.createElement('li');
    prevLi.className = 'page-item' + (currentPageNum === 0 ? ' disabled' : '');
    var prevA = document.createElement('a');
    prevA.className = 'page-link';
    prevA.href = '#';
    prevA.textContent = '이전';
    prevA.onclick = function (e) { e.preventDefault(); if (currentPageNum > 0) onPageClick(currentPageNum - 1); };
    prevLi.appendChild(prevA);
    ul.appendChild(prevLi);

    // 페이지 번호
    var startPage = Math.max(0, currentPageNum - 2);
    var endPage = Math.min(totalPages - 1, startPage + 4);
    if (endPage - startPage < 4) {
        startPage = Math.max(0, endPage - 4);
    }
    for (var i = startPage; i <= endPage; i++) {
        var li = document.createElement('li');
        li.className = 'page-item' + (i === currentPageNum ? ' active' : '');
        var a = document.createElement('a');
        a.className = 'page-link';
        a.href = '#';
        a.textContent = i + 1;
        (function (pageNum) {
            a.onclick = function (e) { e.preventDefault(); onPageClick(pageNum); };
        })(i);
        li.appendChild(a);
        ul.appendChild(li);
    }

    // 다음
    var nextLi = document.createElement('li');
    nextLi.className = 'page-item' + (currentPageNum >= totalPages - 1 ? ' disabled' : '');
    var nextA = document.createElement('a');
    nextA.className = 'page-link';
    nextA.href = '#';
    nextA.textContent = '다음';
    nextA.onclick = function (e) { e.preventDefault(); if (currentPageNum < totalPages - 1) onPageClick(currentPageNum + 1); };
    nextLi.appendChild(nextA);
    ul.appendChild(nextLi);

    nav.appendChild(ul);
    container.appendChild(nav);
}

/**
 * 프로필 모달에서 채팅하기 버튼 클릭 시
 */
function startChatWithAuthor() {
    if (!currentProfileUserId) {
        if (typeof UI !== 'undefined') UI.error('사용자 정보를 불러오는 중입니다. 잠시 후 다시 시도해주세요.');
        return;
    }

    axios.post('/api/chat/rooms', {
        targetUserId: currentProfileUserId,
        roomType: 'DIRECT'
    }, { withCredentials: true })
        .then(function (res) {
            var room = res.data.data;
            // 모달 닫기
            var modal = bootstrap.Modal.getInstance(document.getElementById('authorProfileModal'));
            if (modal) modal.hide();
            // 채팅방으로 이동
            window.location.href = '/chat/' + room.roomId;
        })
        .catch(function (err) {
            var msg = (err.response && err.response.data && err.response.data.message) || '채팅방 생성에 실패했습니다.';
            if (typeof UI !== 'undefined') UI.error(msg);
        });
}
