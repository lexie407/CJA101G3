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
        images: [],
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
                spotDesc: 10
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
            // 新增：圖片 input change 觸發驗證
            const imageInput = document.getElementById('multiImageInput');
            if (imageInput) {
                imageInput.addEventListener('change', () => {
                    this.updateFormValidation();
                });
            }
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
            // 景點描述即時字數統計
            if (this.elements.spotDesc && this.elements.descCharCount) {
                const updateDescCount = () => {
                    const len = this.elements.spotDesc.value.length;
                    this.elements.descCharCount.textContent = `${len}/500`;
                };
                this.elements.spotDesc.addEventListener('input', updateDescCount);
                updateDescCount();
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

            // === 圖片必填驗證（改用 this.images） ===
            if (!this.images || this.images.length === 0) {
                this.showToast('請至少上傳一張圖片', 'error');
                return;
            }

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

            // 收集表單資料
            const formData = new FormData(this.elements.form);
            
            // 添加圖片檔案
            this.images.forEach((file, index) => {
                formData.append('multiImages', file);
                // 如果有圖片描述，也一併加入
                const desc = document.querySelector(`#image-${index} .image-desc`)?.value || '';
                formData.append('multiImageDescs', desc);
            });

            // 使用 fetch 提交
            fetch('/api/spot/public/submit-form', {
                method: 'POST',
                body: formData
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    this.showToast('景點新增成功！即將跳轉...', 'success');
                    // 成功後跳轉到首頁或列表頁
                    setTimeout(() => {
                        window.location.href = '/front-end/spot/list.html';
                    }, 1500);
                } else {
                    this.showToast(data.message || '景點新增失敗', 'error');
                    this.state.isSubmitting = false;
                    this.updateSubmitButton(false);
                }
            })
            .catch(error => {
                console.error('Error:', error);
                this.showToast('系統錯誤，請稍後再試', 'error');
                this.state.isSubmitting = false;
                this.updateSubmitButton(false);
            });
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
                        if (value.length < 10) {
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
            // 圖片必填驗證（改用 this.images）
            const hasImage = this.images && this.images.length > 0;
            this.state.isFormValid = !hasErrors && !hasEmptyFields && hasImage;
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

    // ===== 多圖上傳區塊（會員新增景點） =====
    const input = document.getElementById('multiImageInput');
    const dropzone = document.getElementById('multiImageDropzone');
    const selectBtn = document.getElementById('multiImageSelectBtn');
    const list = document.getElementById('multiImageList');
    let helperText = dropzone?.parentElement.querySelector('.helper-text');
    // let images = [];
    const MAX_IMAGES = 8;
    const MAX_SIZE = 2 * 1024 * 1024; // 2MB
    const ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/jpg'];

    // 初始化 SpotAdd.images
    SpotAdd.images = [];

    if (!input || !dropzone || !selectBtn || !list) return;

    selectBtn.addEventListener('click', () => {
        if (!selectBtn.disabled) input.click();
    });
    dropzone.addEventListener('dragover', e => {
        if (isUploadFull()) return;
        e.preventDefault(); dropzone.classList.add('dragover');
    });
    dropzone.addEventListener('dragleave', e => { e.preventDefault(); dropzone.classList.remove('dragover'); });
    dropzone.addEventListener('drop', e => {
        if (isUploadFull()) return;
        e.preventDefault(); dropzone.classList.remove('dragover');
        handleFiles(e.dataTransfer.files);
    });
    input.addEventListener('change', e => handleFiles(e.target.files));

    function isUploadFull() {
        return SpotAdd.images.length >= MAX_IMAGES;
    }
    function updateHelper() {
        const remain = MAX_IMAGES - SpotAdd.images.length;
        if (helperText) {
            helperText.textContent = `可上傳多張圖片，每張可填寫描述，支援拖拉排序與刪除（剩餘${remain}張）`;
        }
        selectBtn.disabled = remain <= 0;
        dropzone.style.opacity = remain <= 0 ? 0.5 : 1;
        dropzone.style.pointerEvents = remain <= 0 ? 'none' : 'auto';
    }
    function handleFiles(files) {
        let totalCount = SpotAdd.images.length;
        for (let file of files) {
            if (totalCount >= MAX_IMAGES) {
                SpotAdd.showToast(`最多只能上傳${MAX_IMAGES}張圖片，請先刪除部分圖片再嘗試。`, 'error');
                break;
            }
            if (!ALLOWED_TYPES.includes(file.type)) {
                SpotAdd.showToast('僅允許 JPG、PNG 格式圖片，請重新選擇。', 'error');
                continue;
            }
            if (file.size > MAX_SIZE) {
                SpotAdd.showToast('單張圖片不可超過2MB，請壓縮後再上傳。', 'error');
                continue;
            }
            const reader = new FileReader();
            reader.onload = e => {
                SpotAdd.images.push({ file, url: e.target.result, desc: '' });
                renderList();
                SpotAdd.updateFormValidation(); // 新增後驗證
            };
            reader.readAsDataURL(file);
            totalCount++;
        }
        input.value = '';
        updateHelper();
        SpotAdd.updateFormValidation(); // 處理完也驗證
    }
    function renderList() {
        list.innerHTML = '';
        SpotAdd.images.forEach((img, idx) => {
            const item = document.createElement('div');
            item.className = 'multi-image-item';
            item.draggable = true;
            item.innerHTML = `
                <img src="${img.url}" class="multi-image-thumb">
                <input type="text" class="multi-image-desc" placeholder="描述(選填)" value="${img.desc || ''}">
                <button type="button" class="multi-image-delete">刪除</button>
            `;
            item.querySelector('.multi-image-delete').onclick = () => {
                SpotAdd.images.splice(idx, 1);
                renderList();
                updateHelper();
                SpotAdd.updateFormValidation(); // 刪除後驗證
            };
            item.querySelector('.multi-image-desc').oninput = e => {
                SpotAdd.images[idx].desc = e.target.value;
            };
            item.ondragstart = e => { e.dataTransfer.setData('text/plain', idx); };
            item.ondragover = e => e.preventDefault();
            item.ondrop = e => {
                e.preventDefault();
                const from = +e.dataTransfer.getData('text/plain');
                const to = idx;
                if (from !== to) {
                    const moved = SpotAdd.images.splice(from, 1)[0];
                    SpotAdd.images.splice(to, 0, moved);
                    renderList();
                    SpotAdd.updateFormValidation(); // 排序後驗證
                }
            };
            list.appendChild(item);
        });
        updateHelper();
        SpotAdd.updateFormValidation(); // 渲染完也驗證
    }
    // 表單送出時附加多圖資料
    const form = document.getElementById('spotAddForm');
    if (form) {
        form.addEventListener('submit', function(e) {
            e.preventDefault();
            const formData = new FormData(form);
            // 收集多圖
            if (!SpotAdd.images || SpotAdd.images.length === 0) {
                SpotAdd.showToast('請至少上傳一張圖片', 'error');
                return;
            }
            for (let i = 0; i < SpotAdd.images.length; i++) {
                formData.append('multiImages', SpotAdd.images[i].file);
            }
            fetch('/api/spot/public/submit-form', {
                method: 'POST',
                body: formData
            })
            .then(resp => resp.json())
            .then(data => {
                if (data.success) {
                    SpotAdd.showToast('景點與圖片已送出，待審核', 'success');
                    setTimeout(() => location.href = '/spot/list', 1200);
                } else {
                    SpotAdd.showToast(data.message || '儲存失敗', 'error');
                }
            })
            .catch(() => SpotAdd.showToast('儲存失敗', 'error'));
        });
    }

    // ===== 自動帶入地區、電話、官方網站（參考後台） =====
    function extractRegionFromAddress(address) {
        if (!address) return null;
        const regions = [
            '台北市', '臺北市', '新北市', '桃園市', '台中市', '臺中市', '台南市', '臺南市', '高雄市',
            '基隆市', '新竹市', '嘉義市', '新竹縣', '苗栗縣', '彰化縣', '南投縣', '雲林縣',
            '嘉義縣', '屏東縣', '宜蘭縣', '花蓮縣', '台東縣', '臺東縣', '澎湖縣', '金門縣', '連江縣'
        ];
        for (const region of regions) {
            if (address.includes(region)) {
                if (region === '臺北市') return '台北市';
                if (region === '臺中市') return '台中市';
                if (region === '臺南市') return '台南市';
                if (region === '臺東縣') return '台東縣';
                return region;
            }
        }
        return null;
    }

    function setupSpotInfoChangeListener() {
        // 僅保留清空欄位功能，不自動帶入
        const spotNameInput = document.getElementById('spotName');
        const spotLocInput = document.getElementById('spotLoc');
        if (!spotNameInput || !spotLocInput) return;
        let lastSpotName = spotNameInput.value;
        let lastSpotLoc = spotLocInput.value;
        function clearAutoFilledFields() {
            const telInput = document.getElementById('spotTel');
            const websiteInput = document.getElementById('spotWeb');
            if (telInput) {
                telInput.value = '';
                telInput.dispatchEvent(new Event('input', { bubbles: true }));
            }
            if (websiteInput) {
                websiteInput.value = '';
                websiteInput.dispatchEvent(new Event('input', { bubbles: true }));
            }
        }
        spotNameInput.addEventListener('input', function() {
            const currentValue = this.value.trim();
            if (currentValue !== lastSpotName && lastSpotName !== '') {
                clearAutoFilledFields();
            }
            lastSpotName = currentValue;
        });
        spotLocInput.addEventListener('input', function() {
            const currentValue = this.value.trim();
            if (currentValue !== lastSpotLoc && lastSpotLoc !== '') {
                clearAutoFilledFields();
            }
            lastSpotLoc = currentValue;
        });
    }

    function setupAddressAutoComplete() {
        const spotLocInput = document.getElementById('spotLoc');
        const regionSelect = document.getElementById('region');
        if (!spotLocInput || !regionSelect) return;
        setupSpotInfoChangeListener();
        spotLocInput.addEventListener('blur', function() {
            const address = this.value.trim();
            if (!address) return;
            const region = extractRegionFromAddress(address);
            if (region) {
                regionSelect.value = region;
                regionSelect.dispatchEvent(new Event('change', { bubbles: true }));
                //fetchGooglePlaceDataFront(true); // 註解掉自動帶入
            }
        });
    }

    // async function fetchGooglePlaceDataFront(silent = false, forceUpdate = false) {
    //     // 此功能前台停用
    // }

    document.addEventListener('DOMContentLoaded', function() {
        SpotAdd.init();
        setupAddressAutoComplete();
    });

    // 匯出模組供測試使用
    window.SpotAdd = SpotAdd;

})(); 

// 單圖預覽功能
(function() {
    const input = document.getElementById('spotImageInput');
    const preview = document.getElementById('spotImagePreview');
    if (!input || !preview) return;
    input.addEventListener('change', function() {
        preview.innerHTML = '';
        if (input.files && input.files.length > 0) {
            const file = input.files[0];
            if (!file.type.startsWith('image/')) {
                preview.innerHTML = '<span style="color:#f44336;">僅支援圖片檔案</span>';
                input.value = '';
                return;
            }
            const reader = new FileReader();
            reader.onload = function(e) {
                preview.innerHTML = `<img src="${e.target.result}" style="max-width:120px;max-height:120px;border-radius:10px;border:1.5px solid #e0e0e0;box-shadow:0 2px 8px rgba(0,0,0,0.10);background:#fff;display:block;" alt="預覽圖"/>`;
            };
            reader.readAsDataURL(file);
        }
    });
})(); 

// === 強化自動帶入地區功能 ===
(function() {
    document.addEventListener('DOMContentLoaded', function() {
        var spotLocInput = document.getElementById('spotLoc');
        var regionSelect = document.getElementById('region');
        if (!spotLocInput || !regionSelect) return;
        spotLocInput.addEventListener('blur', function() {
            var address = this.value.trim();
            if (!address) return;
            var regions = [
                '台北市', '臺北市', '新北市', '桃園市', '台中市', '臺中市', '台南市', '臺南市', '高雄市',
                '基隆市', '新竹市', '嘉義市', '新竹縣', '苗栗縣', '彰化縣', '南投縣', '雲林縣',
                '嘉義縣', '屏東縣', '宜蘭縣', '花蓮縣', '台東縣', '臺東縣', '澎湖縣', '金門縣', '連江縣'
            ];
            var found = null;
            for (var i = 0; i < regions.length; i++) {
                if (address.includes(regions[i])) {
                    found = regions[i];
                    break;
                }
            }
            if (found) {
                // 正規化
                if (found === '臺北市') found = '台北市';
                if (found === '臺中市') found = '台中市';
                if (found === '臺南市') found = '台南市';
                if (found === '臺東縣') found = '台東縣';
                regionSelect.value = found;
                regionSelect.dispatchEvent(new Event('change', { bubbles: true }));
            } else {
                // 若無法自動判斷，清空選擇
                regionSelect.value = '';
            }
        });
    });
})();

// === 圖片必填驗證 ===
(function() {
    document.addEventListener('DOMContentLoaded', function() {
        var form = document.getElementById('spotAddForm');
        var imgInput = document.getElementById('spotImageInput');
        if (!form || !imgInput) return;
        form.addEventListener('submit', function(e) {
            if (!imgInput.value) {
                e.preventDefault();
                alert('請上傳一張景點圖片');
                imgInput.focus();
            }
        });
    });
})();