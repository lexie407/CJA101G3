/**
 * 新增景點頁面 JavaScript
 * @description 處理表單驗證、提交流程和用戶互動
 * @version 2.0
 * 
 * 功能包含：
 * - 即時表單驗證
 * - 字元計數顯示
 * - 提交狀態管理
 * - 錯誤處理
 * - 無障礙功能增強
 * - Toast 通知系統
 */

(function() {
    'use strict';

    // 檢查動畫偏好
    const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

    /**
     * 主要模組物件
     */
    const SpotAdd = {
        // 配置選項
        config: {
            animationDuration: prefersReducedMotion ? 0 : 300,
            validationDelay: 500,
            maxLengths: {
                spotName: 100,
                spotLoc: 100,
                spotDesc: 1000
            },
            minLengths: {
                spotName: 2,
                spotLoc: 3,
                spotDesc: 20
            },
            requiredFields: ['spotName', 'spotLoc', 'spotDesc']
        },

        // 狀態管理
        state: {
            isSubmitting: false,
            validationErrors: {},
            isFormValid: false
        },

        // DOM 元素快取
        elements: {},

        /**
         * 初始化
         */
        init() {
            this.cacheElements();
            this.bindEvents();
            this.setupValidation();
            this.setupCharacterCounters();
            this.setupAccessibility();
            this.initializeTooltips();
            console.log('新增景點頁面已初始化');
        },

        /**
         * 快取 DOM 元素
         */
        cacheElements() {
            this.elements = {
                form: document.getElementById('spotAddForm'),
                submitBtn: document.getElementById('submitBtn'),
                spotName: document.getElementById('spotName'),
                spotLoc: document.getElementById('spotLoc'),
                spotDesc: document.getElementById('spotDesc'),
                descCharCount: document.getElementById('descCharCount'),
                errorAlert: document.getElementById('errorAlert'),
                successAlert: document.getElementById('successAlert')
            };

            // 快取錯誤顯示元素
            this.config.requiredFields.forEach(fieldName => {
                this.elements[`${fieldName}Error`] = document.getElementById(`${fieldName}Error`);
            });
        },

        /**
         * 綁定事件
         */
        bindEvents() {
            // 表單提交事件
            if (this.elements.form) {
                this.elements.form.addEventListener('submit', (e) => {
                    this.handleFormSubmit(e);
                });
            }

            // 輸入框即時驗證
            this.config.requiredFields.forEach(fieldName => {
                const field = this.elements[fieldName];
                if (field) {
                    // 輸入事件
                    field.addEventListener('input', () => {
                        this.clearFieldError(fieldName);
                        this.updateCharacterCount(fieldName);
                        this.debounce(() => {
                            this.validateField(fieldName);
                            this.updateFormValidation();
                        }, this.config.validationDelay)();
                    });

                    // 失焦驗證
                    field.addEventListener('blur', () => {
                        this.validateField(fieldName);
                        this.updateFormValidation();
                    });

                    // 聚焦時清除錯誤狀態
                    field.addEventListener('focus', () => {
                        this.clearFieldError(fieldName);
                    });
                }
            });

            // 按鈕點擊增強效果
            document.addEventListener('click', (e) => {
                if (e.target.closest('.spot-add-btn')) {
                    const button = e.target.closest('.spot-add-btn');
                    this.addRippleEffect(button, e);
                }
            });

            // 鍵盤快捷鍵
            document.addEventListener('keydown', (e) => {
                this.handleKeyboardShortcuts(e);
            });
        },

        /**
         * 設定表單驗證
         */
        setupValidation() {
            if (!this.elements.form) return;

            // 禁用瀏覽器預設驗證
            this.elements.form.setAttribute('novalidate', 'true');

            // 初始驗證狀態
            this.updateFormValidation();
        },

        /**
         * 設定字元計數器
         */
        setupCharacterCounters() {
            // 描述字元計數
            if (this.elements.spotDesc && this.elements.descCharCount) {
                this.updateCharacterCount('spotDesc');
            }
        },

        /**
         * 設定無障礙功能
         */
        setupAccessibility() {
            // 為必填欄位添加 aria-required
            this.config.requiredFields.forEach(fieldName => {
                const field = this.elements[fieldName];
                if (field) {
                    field.setAttribute('aria-required', 'true');
                    field.setAttribute('aria-describedby', `${fieldName}Help ${fieldName}Error`);
                }
            });

            // 為表單添加 aria-label
            if (this.elements.form) {
                this.elements.form.setAttribute('aria-label', '新增景點表單');
            }
        },

        /**
         * 初始化工具提示
         */
        initializeTooltips() {
            // 這裡可以初始化任何工具提示庫
            // 例如 Bootstrap Tooltips 或自定義工具提示
        },

        /**
         * 處理表單提交
         */
        handleFormSubmit(e) {
            e.preventDefault();

            if (this.state.isSubmitting) {
                return;
            }

            // 驗證所有欄位
            const isValid = this.validateAllFields();

            if (!isValid) {
                this.showToast('請修正表單中的錯誤後再提交', 'error');
                this.focusFirstError();
                return;
            }

            // 開始提交流程
            this.startSubmission();
        },

        /**
         * 開始提交流程
         */
        startSubmission() {
            this.state.isSubmitting = true;
            this.updateSubmitButton(true);
            
            // 顯示提交中的 toast
            this.showToast('正在提交景點資訊...', 'info');

            // 提交表單
            setTimeout(() => {
                this.elements.form.submit();
            }, 500);
        },

        /**
         * 驗證所有欄位
         */
        validateAllFields() {
            let isValid = true;
            this.state.validationErrors = {};

            this.config.requiredFields.forEach(fieldName => {
                if (!this.validateField(fieldName)) {
                    isValid = false;
                }
            });

            this.updateFormValidation();
            return isValid;
        },

        /**
         * 驗證單個欄位
         */
        validateField(fieldName) {
            const field = this.elements[fieldName];
            if (!field) return true;

            const value = field.value.trim();
            const errors = [];

            // 必填驗證
            if (!value) {
                errors.push('此欄位為必填');
            } else {
                // 最小長度驗證
                const minLength = this.config.minLengths[fieldName];
                if (minLength && value.length < minLength) {
                    errors.push(`至少需要 ${minLength} 個字元`);
                }

                // 最大長度驗證
                const maxLength = this.config.maxLengths[fieldName];
                if (maxLength && value.length > maxLength) {
                    errors.push(`不能超過 ${maxLength} 個字元`);
                }

                // 特殊驗證規則
                switch (fieldName) {
                    case 'spotName':
                        if (!/^[a-zA-Z0-9\u4e00-\u9fa5\s\-\(\)\.]+$/.test(value)) {
                            errors.push('景點名稱包含無效字元');
                        }
                        break;
                    case 'spotLoc':
                        if (!/^[a-zA-Z0-9\u4e00-\u9fa5\s\-\(\)\.]+$/.test(value)) {
                            errors.push('地點資訊包含無效字元');
                        }
                        break;
                    case 'spotDesc':
                        if (value.length < 20) {
                            errors.push('描述內容太短，請提供更詳細的資訊');
                        }
                        break;
                }
            }

            // 更新驗證狀態
            if (errors.length > 0) {
                this.state.validationErrors[fieldName] = errors;
                this.showFieldError(fieldName, errors[0]);
                return false;
            } else {
                delete this.state.validationErrors[fieldName];
                this.clearFieldError(fieldName);
                return true;
            }
        },

        /**
         * 顯示欄位錯誤
         */
        showFieldError(fieldName, message) {
            const field = this.elements[fieldName];
            const errorElement = this.elements[`${fieldName}Error`];

            if (field) {
                field.classList.add('error');
                field.setAttribute('aria-invalid', 'true');
            }

            if (errorElement) {
                errorElement.textContent = message;
                errorElement.classList.add('show');
            }
        },

        /**
         * 清除欄位錯誤
         */
        clearFieldError(fieldName) {
            const field = this.elements[fieldName];
            const errorElement = this.elements[`${fieldName}Error`];

            if (field) {
                field.classList.remove('error');
                field.setAttribute('aria-invalid', 'false');
            }

            if (errorElement) {
                errorElement.textContent = '';
                errorElement.classList.remove('show');
            }
        },

        /**
         * 更新字元計數
         */
        updateCharacterCount(fieldName) {
            const field = this.elements[fieldName];
            const maxLength = this.config.maxLengths[fieldName];

            if (fieldName === 'spotDesc' && field && this.elements.descCharCount) {
                const currentLength = field.value.length;
                this.elements.descCharCount.textContent = currentLength;

                // 根據字元數量改變顏色
                const percentage = currentLength / maxLength;
                if (percentage >= 0.9) {
                    this.elements.descCharCount.style.color = 'var(--md-sys-color-error)';
                } else if (percentage >= 0.7) {
                    this.elements.descCharCount.style.color = 'var(--md-sys-color-secondary)';
                } else {
                    this.elements.descCharCount.style.color = 'var(--md-sys-color-on-surface-variant)';
                }
            }
        },

        /**
         * 更新表單驗證狀態
         */
        updateFormValidation() {
            const hasErrors = Object.keys(this.state.validationErrors).length > 0;
            const hasEmptyFields = this.config.requiredFields.some(fieldName => {
                const field = this.elements[fieldName];
                return !field || !field.value.trim();
            });

            this.state.isFormValid = !hasErrors && !hasEmptyFields;
            this.updateSubmitButton();
        },

        /**
         * 更新提交按鈕狀態
         */
        updateSubmitButton(isLoading = false) {
            if (!this.elements.submitBtn) return;

            if (isLoading || this.state.isSubmitting) {
                this.elements.submitBtn.classList.add('loading');
                this.elements.submitBtn.disabled = true;
            } else {
                this.elements.submitBtn.classList.remove('loading');
                this.elements.submitBtn.disabled = !this.state.isFormValid;
            }
        },

        /**
         * 聚焦到第一個錯誤欄位
         */
        focusFirstError() {
            const firstErrorField = this.config.requiredFields.find(fieldName => 
                this.state.validationErrors[fieldName]
            );

            if (firstErrorField && this.elements[firstErrorField]) {
                this.elements[firstErrorField].focus();
                this.elements[firstErrorField].scrollIntoView({
                    behavior: 'smooth',
                    block: 'center'
                });
            }
        },

        /**
         * 處理鍵盤快捷鍵
         */
        handleKeyboardShortcuts(e) {
            // Ctrl/Cmd + Enter 提交表單
            if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
                e.preventDefault();
                if (this.state.isFormValid && !this.state.isSubmitting) {
                    this.handleFormSubmit(e);
                }
            }

            // Escape 取消提交（如果正在提交中）
            if (e.key === 'Escape' && this.state.isSubmitting) {
                this.cancelSubmission();
            }
        },

        /**
         * 取消提交
         */
        cancelSubmission() {
            this.state.isSubmitting = false;
            this.updateSubmitButton();
            this.showToast('已取消提交', 'info');
        },

        /**
         * 添加按鈕波紋效果
         */
        addRippleEffect(button, event) {
            if (prefersReducedMotion) return;

            const rect = button.getBoundingClientRect();
            const size = Math.max(rect.width, rect.height);
            const x = event.clientX - rect.left - size / 2;
            const y = event.clientY - rect.top - size / 2;

            const ripple = document.createElement('div');
            ripple.style.cssText = `
                position: absolute;
                width: ${size}px;
                height: ${size}px;
                left: ${x}px;
                top: ${y}px;
                background: rgba(255, 255, 255, 0.3);
                border-radius: 50%;
                transform: scale(0);
                animation: ripple 0.6s ease-out;
                pointer-events: none;
                z-index: 1;
            `;

            button.style.position = 'relative';
            button.style.overflow = 'hidden';
            button.appendChild(ripple);

            setTimeout(() => {
                ripple.remove();
            }, 600);
        },

        /**
         * 防抖函數
         */
        debounce(func, wait) {
            let timeout;
            return function executedFunction(...args) {
                const later = () => {
                    clearTimeout(timeout);
                    func(...args);
                };
                clearTimeout(timeout);
                timeout = setTimeout(later, wait);
            };
        },

        /**
         * 顯示 Toast 通知
         */
        showToast(message, type = 'info') {
            // 創建 toast 元素
            const toast = document.createElement('div');
            toast.className = `spot-add-toast spot-add-toast--${type}`;
            toast.innerHTML = `
                <div class="spot-add-toast__icon">
                    <span class="material-icons">${this.getToastIcon(type)}</span>
                </div>
                <div class="spot-add-toast__content">
                    <div class="spot-add-toast__message">${message}</div>
                </div>
                <button type="button" class="spot-add-toast__close">
                    <span class="material-icons">close</span>
                </button>
            `;

            // 添加樣式
            toast.style.cssText = `
                position: fixed;
                top: 20px;
                right: 20px;
                background: var(--md-sys-color-surface-container-high);
                color: var(--md-sys-color-on-surface);
                border: 1px solid var(--md-sys-color-outline-variant);
                border-radius: 12px;
                padding: 1rem;
                box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
                z-index: 10000;
                display: flex;
                align-items: center;
                gap: 0.75rem;
                max-width: 400px;
                min-width: 300px;
                transform: translateX(100%);
                transition: transform 0.3s ease;
            `;

            // 根據類型調整顏色
            if (type === 'error') {
                toast.style.background = 'var(--md-sys-color-error-container)';
                toast.style.color = 'var(--md-sys-color-on-error-container)';
                toast.style.borderColor = 'var(--md-sys-color-error)';
            } else if (type === 'success') {
                toast.style.background = 'var(--md-sys-color-primary-container)';
                toast.style.color = 'var(--md-sys-color-on-primary-container)';
                toast.style.borderColor = 'var(--md-sys-color-primary)';
            }

            document.body.appendChild(toast);

            // 顯示動畫
            setTimeout(() => {
                toast.style.transform = 'translateX(0)';
            }, 10);

            // 關閉按鈕事件
            const closeBtn = toast.querySelector('.spot-add-toast__close');
            closeBtn.addEventListener('click', () => {
                this.hideToast(toast);
            });

            // 自動關閉
            setTimeout(() => {
                this.hideToast(toast);
            }, type === 'error' ? 5000 : 3000);
        },

        /**
         * 隱藏 Toast
         */
        hideToast(toast) {
            toast.style.transform = 'translateX(100%)';
            setTimeout(() => {
                if (toast.parentNode) {
                    toast.parentNode.removeChild(toast);
                }
            }, 300);
        },

        /**
         * 獲取 Toast 圖標
         */
        getToastIcon(type) {
            const icons = {
                'info': 'info',
                'success': 'check_circle',
                'error': 'error',
                'warning': 'warning'
            };
            return icons[type] || 'info';
        }
    };

    /**
     * 全域函數 - 關閉提示
     */
    window.closeAlert = function(alertId) {
        const alert = document.getElementById(alertId);
        if (alert) {
            alert.style.opacity = '0';
            alert.style.transform = 'translateY(-10px)';
            setTimeout(() => {
                alert.style.display = 'none';
            }, 300);
        }
    };

    /**
     * 添加 CSS 動畫
     */
    const style = document.createElement('style');
    style.textContent = `
        @keyframes ripple {
            to {
                transform: scale(4);
                opacity: 0;
            }
        }
        
        .spot-add-toast__icon {
            flex-shrink: 0;
        }
        
        .spot-add-toast__content {
            flex: 1;
        }
        
        .spot-add-toast__message {
            font-size: 0.9rem;
            line-height: 1.4;
        }
        
        .spot-add-toast__close {
            background: none;
            border: none;
            cursor: pointer;
            padding: 0.25rem;
            border-radius: 50%;
            transition: background-color 0.2s ease;
            display: flex;
            align-items: center;
            justify-content: center;
            flex-shrink: 0;
        }
        
        .spot-add-toast__close:hover {
            background: rgba(0, 0, 0, 0.1);
        }
        
        .spot-add-toast__close .material-icons {
            font-size: 1.1rem;
        }
    `;
    document.head.appendChild(style);

    /**
     * 頁面載入完成後初始化
     */
    document.addEventListener('DOMContentLoaded', () => {
        SpotAdd.init();
    });

    // 匯出模組供測試使用
    window.SpotAdd = SpotAdd;

})(); 