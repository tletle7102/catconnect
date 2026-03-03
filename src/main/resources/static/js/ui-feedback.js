/**
 * UI 피드백 시스템 (Alert, Toast, Modal)
 * Bootstrap 5 기반 공통 UI 컴포넌트
 */

const UI = (function() {
    'use strict';

    // Toast 컨테이너 생성
    function ensureToastContainer() {
        let container = document.getElementById('toast-container');
        if (!container) {
            container = document.createElement('div');
            container.id = 'toast-container';
            container.className = 'toast-container position-fixed top-0 end-0 p-3';
            container.style.zIndex = '1100';
            document.body.appendChild(container);
        }
        return container;
    }

    // Modal 컨테이너 생성
    function ensureModalContainer() {
        let container = document.getElementById('ui-modal-container');
        if (!container) {
            container = document.createElement('div');
            container.id = 'ui-modal-container';
            document.body.appendChild(container);
        }
        return container;
    }

    /**
     * Toast 알림 표시
     * @param {string} message - 표시할 메시지
     * @param {string} type - 타입 (success, error, warning, info)
     * @param {number} duration - 표시 시간 (ms), 기본 3000ms
     */
    function toast(message, type = 'info', duration = 3000) {
        const container = ensureToastContainer();
        const toastId = 'toast-' + Date.now();

        const bgClass = {
            success: 'bg-success',
            error: 'bg-danger',
            warning: 'bg-warning',
            info: 'bg-primary'
        }[type] || 'bg-primary';

        const icon = {
            success: '&#10004;',
            error: '&#10006;',
            warning: '&#9888;',
            info: '&#8505;'
        }[type] || '&#8505;';

        const toastHtml = `
            <div id="${toastId}" class="toast align-items-center text-white ${bgClass} border-0" role="alert" aria-live="assertive" aria-atomic="true">
                <div class="d-flex">
                    <div class="toast-body d-flex align-items-center">
                        <span class="me-2">${icon}</span>
                        <span>${escapeHtml(message)}</span>
                    </div>
                    <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
                </div>
            </div>
        `;

        container.insertAdjacentHTML('beforeend', toastHtml);

        const toastElement = document.getElementById(toastId);
        const bsToast = new bootstrap.Toast(toastElement, {
            autohide: true,
            delay: duration
        });

        toastElement.addEventListener('hidden.bs.toast', function() {
            toastElement.remove();
        });

        bsToast.show();
    }

    /**
     * 성공 Toast
     */
    function success(message, duration) {
        toast(message, 'success', duration);
    }

    /**
     * 에러 Toast
     */
    function error(message, duration) {
        toast(message, 'error', duration);
    }

    /**
     * 경고 Toast
     */
    function warning(message, duration) {
        toast(message, 'warning', duration);
    }

    /**
     * 정보 Toast
     */
    function info(message, duration) {
        toast(message, 'info', duration);
    }

    /**
     * Alert 모달 표시 (확인 버튼만)
     * @param {string} message - 표시할 메시지
     * @param {string} title - 제목 (선택)
     * @returns {Promise} - 확인 버튼 클릭 시 resolve
     */
    function alert(message, title = '알림') {
        return new Promise((resolve) => {
            const container = ensureModalContainer();
            const modalId = 'alert-modal-' + Date.now();

            const modalHtml = `
                <div class="modal fade" id="${modalId}" tabindex="-1" aria-labelledby="${modalId}-label" aria-hidden="true" data-bs-backdrop="static">
                    <div class="modal-dialog modal-dialog-centered">
                        <div class="modal-content" style="background-color: #0e2a35; color: #fcf6e5; border-color: #fcf6e5;">
                            <div class="modal-header border-bottom" style="border-color: #fcf6e5 !important;">
                                <h5 class="modal-title" id="${modalId}-label">${escapeHtml(title)}</h5>
                                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                            </div>
                            <div class="modal-body">
                                ${escapeHtml(message)}
                            </div>
                            <div class="modal-footer border-top" style="border-color: #fcf6e5 !important;">
                                <button type="button" class="btn btn-primary" data-bs-dismiss="modal">확인</button>
                            </div>
                        </div>
                    </div>
                </div>
            `;

            container.insertAdjacentHTML('beforeend', modalHtml);

            const modalElement = document.getElementById(modalId);
            const bsModal = new bootstrap.Modal(modalElement);

            modalElement.addEventListener('hidden.bs.modal', function() {
                modalElement.remove();
                resolve();
            });

            bsModal.show();
        });
    }

    /**
     * Confirm 모달 표시 (확인/취소 버튼)
     * @param {string} message - 표시할 메시지
     * @param {string} title - 제목 (선택)
     * @param {Object} options - 옵션 { confirmText, cancelText, confirmClass }
     * @returns {Promise<boolean>} - 확인 시 true, 취소 시 false
     */
    function confirm(message, title = '확인', options = {}) {
        const {
            confirmText = '확인',
            cancelText = '취소',
            confirmClass = 'btn-primary'
        } = options;

        return new Promise((resolve) => {
            const container = ensureModalContainer();
            const modalId = 'confirm-modal-' + Date.now();

            const modalHtml = `
                <div class="modal fade" id="${modalId}" tabindex="-1" aria-labelledby="${modalId}-label" aria-hidden="true" data-bs-backdrop="static">
                    <div class="modal-dialog modal-dialog-centered">
                        <div class="modal-content" style="background-color: #0e2a35; color: #fcf6e5; border-color: #fcf6e5;">
                            <div class="modal-header border-bottom" style="border-color: #fcf6e5 !important;">
                                <h5 class="modal-title" id="${modalId}-label">${escapeHtml(title)}</h5>
                                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                            </div>
                            <div class="modal-body">
                                ${escapeHtml(message)}
                            </div>
                            <div class="modal-footer border-top" style="border-color: #fcf6e5 !important;">
                                <button type="button" class="btn btn-secondary" data-action="cancel">${escapeHtml(cancelText)}</button>
                                <button type="button" class="btn ${confirmClass}" data-action="confirm">${escapeHtml(confirmText)}</button>
                            </div>
                        </div>
                    </div>
                </div>
            `;

            container.insertAdjacentHTML('beforeend', modalHtml);

            const modalElement = document.getElementById(modalId);
            const bsModal = new bootstrap.Modal(modalElement);
            let confirmed = false;

            modalElement.querySelector('[data-action="confirm"]').addEventListener('click', function() {
                confirmed = true;
                bsModal.hide();
            });

            modalElement.querySelector('[data-action="cancel"]').addEventListener('click', function() {
                confirmed = false;
                bsModal.hide();
            });

            modalElement.addEventListener('hidden.bs.modal', function() {
                modalElement.remove();
                resolve(confirmed);
            });

            bsModal.show();
        });
    }

    /**
     * 삭제 확인 모달 (위험 동작용)
     * @param {string} message - 표시할 메시지
     * @param {string} title - 제목 (선택)
     * @returns {Promise<boolean>}
     */
    function confirmDelete(message, title = '삭제 확인') {
        return confirm(message, title, {
            confirmText: '삭제',
            cancelText: '취소',
            confirmClass: 'btn-danger'
        });
    }

    /**
     * Prompt 모달 표시 (입력 필드 포함)
     * @param {string} message - 표시할 메시지
     * @param {string} title - 제목 (선택)
     * @param {Object} options - 옵션 { defaultValue, placeholder, inputType, required }
     * @returns {Promise<string|null>} - 입력값 또는 취소 시 null
     */
    function prompt(message, title = '입력', options = {}) {
        const {
            defaultValue = '',
            placeholder = '',
            inputType = 'text',
            required = false
        } = options;

        return new Promise((resolve) => {
            const container = ensureModalContainer();
            const modalId = 'prompt-modal-' + Date.now();
            const inputId = 'prompt-input-' + Date.now();

            const inputElement = inputType === 'textarea'
                ? `<textarea id="${inputId}" class="form-control" style="background-color: #1a3f4e; color: #fcf6e5; border-color: #fcf6e5;" placeholder="${escapeHtml(placeholder)}" rows="3" ${required ? 'required' : ''}>${escapeHtml(defaultValue)}</textarea>`
                : `<input type="${inputType}" id="${inputId}" class="form-control" style="background-color: #1a3f4e; color: #fcf6e5; border-color: #fcf6e5;" value="${escapeHtml(defaultValue)}" placeholder="${escapeHtml(placeholder)}" ${required ? 'required' : ''}>`;

            const modalHtml = `
                <div class="modal fade" id="${modalId}" tabindex="-1" aria-labelledby="${modalId}-label" aria-hidden="true" data-bs-backdrop="static">
                    <div class="modal-dialog modal-dialog-centered">
                        <div class="modal-content" style="background-color: #0e2a35; color: #fcf6e5; border-color: #fcf6e5;">
                            <div class="modal-header border-bottom" style="border-color: #fcf6e5 !important;">
                                <h5 class="modal-title" id="${modalId}-label">${escapeHtml(title)}</h5>
                                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                            </div>
                            <div class="modal-body">
                                <p>${escapeHtml(message)}</p>
                                ${inputElement}
                            </div>
                            <div class="modal-footer border-top" style="border-color: #fcf6e5 !important;">
                                <button type="button" class="btn btn-secondary" data-action="cancel">취소</button>
                                <button type="button" class="btn btn-primary" data-action="confirm">확인</button>
                            </div>
                        </div>
                    </div>
                </div>
            `;

            container.insertAdjacentHTML('beforeend', modalHtml);

            const modalElement = document.getElementById(modalId);
            const inputEl = document.getElementById(inputId);
            const bsModal = new bootstrap.Modal(modalElement);
            let result = null;

            modalElement.querySelector('[data-action="confirm"]').addEventListener('click', function() {
                const value = inputEl.value.trim();
                if (required && !value) {
                    inputEl.classList.add('is-invalid');
                    inputEl.focus();
                    return;
                }
                result = value;
                bsModal.hide();
            });

            modalElement.querySelector('[data-action="cancel"]').addEventListener('click', function() {
                result = null;
                bsModal.hide();
            });

            // Enter 키로 확인
            inputEl.addEventListener('keydown', function(e) {
                if (e.key === 'Enter' && inputType !== 'textarea') {
                    e.preventDefault();
                    modalElement.querySelector('[data-action="confirm"]').click();
                }
            });

            modalElement.addEventListener('hidden.bs.modal', function() {
                modalElement.remove();
                resolve(result);
            });

            modalElement.addEventListener('shown.bs.modal', function() {
                inputEl.focus();
                inputEl.select();
            });

            bsModal.show();
        });
    }

    /**
     * 로딩 오버레이 표시
     * @param {string} message - 표시할 메시지 (선택)
     * @returns {Object} - { hide: Function } 숨기기 함수 반환
     */
    function loading(message = '처리 중...') {
        const overlayId = 'loading-overlay-' + Date.now();

        const overlayHtml = `
            <div id="${overlayId}" class="position-fixed top-0 start-0 w-100 h-100 d-flex justify-content-center align-items-center" style="background-color: rgba(0, 0, 0, 0.5); z-index: 1200;">
                <div class="text-center">
                    <div class="spinner-border text-light mb-3" role="status" style="width: 3rem; height: 3rem;">
                        <span class="visually-hidden">Loading...</span>
                    </div>
                    <p class="text-light">${escapeHtml(message)}</p>
                </div>
            </div>
        `;

        document.body.insertAdjacentHTML('beforeend', overlayHtml);

        return {
            hide: function() {
                const overlay = document.getElementById(overlayId);
                if (overlay) {
                    overlay.remove();
                }
            },
            updateMessage: function(newMessage) {
                const overlay = document.getElementById(overlayId);
                if (overlay) {
                    const msgEl = overlay.querySelector('p');
                    if (msgEl) {
                        msgEl.textContent = newMessage;
                    }
                }
            }
        };
    }

    /**
     * API 에러 처리 헬퍼
     * @param {Error} err - axios 에러 객체
     * @param {string} defaultMessage - 기본 에러 메시지
     */
    function handleApiError(err, defaultMessage = '오류가 발생했습니다.') {
        console.error('API Error:', err);
        const message = err.response?.data?.message || defaultMessage;
        error(message);
    }

    /**
     * URL 파라미터에서 메시지 확인 및 표시
     * 페이지 로드 시 ?message= 또는 ?error= 파라미터 처리
     */
    function checkUrlMessages() {
        const urlParams = new URLSearchParams(window.location.search);
        const message = urlParams.get('message');
        const errorMsg = urlParams.get('error');

        if (message) {
            success(decodeURIComponent(message));
            // URL에서 파라미터 제거
            urlParams.delete('message');
            updateUrlParams(urlParams);
        }

        if (errorMsg) {
            error(decodeURIComponent(errorMsg));
            // URL에서 파라미터 제거
            urlParams.delete('error');
            updateUrlParams(urlParams);
        }
    }

    /**
     * URL 파라미터 업데이트 (히스토리 교체)
     */
    function updateUrlParams(params) {
        const newUrl = window.location.pathname + (params.toString() ? '?' + params.toString() : '');
        window.history.replaceState({}, '', newUrl);
    }

    /**
     * HTML 이스케이프
     */
    function escapeHtml(text) {
        if (text === null || text === undefined) return '';
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    // Public API
    return {
        toast: toast,
        success: success,
        error: error,
        warning: warning,
        info: info,
        alert: alert,
        confirm: confirm,
        confirmDelete: confirmDelete,
        prompt: prompt,
        loading: loading,
        handleApiError: handleApiError,
        checkUrlMessages: checkUrlMessages
    };
})();

// 페이지 로드 시 URL 메시지 확인
document.addEventListener('DOMContentLoaded', function() {
    UI.checkUrlMessages();
});
