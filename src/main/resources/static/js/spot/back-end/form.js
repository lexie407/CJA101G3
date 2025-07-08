/**
 * 景點表單頁面 JavaScript
 * 符合 Material Design 3 設計規範
 */

// 自定義 Toast 通知函數
function showToast(title, message, type = 'info') {
    // 檢查是否有 Toast 容器，如果沒有則創建
    let toastContainer = document.querySelector('.toast-container');
    if (!toastContainer) {
        toastContainer = document.createElement('div');
        toastContainer.className = 'toast-container position-fixed bottom-0 end-0 p-3';
        toastContainer.style.position = 'fixed';
        toastContainer.style.bottom = '20px';
        toastContainer.style.right = '20px';
        toastContainer.style.zIndex = '9999';
        document.body.appendChild(toastContainer);
    }
    
    // 創建 Toast 元素
    const toastId = 'toast-' + Date.now();
    const toast = document.createElement('div');
    toast.id = toastId;
    toast.style.minWidth = '250px';
    toast.style.backgroundColor = type === 'error' ? '#dc3545' : '#fff';
    toast.style.color = type === 'error' ? '#fff' : '#000';
    toast.style.boxShadow = '0 0.5rem 1rem rgba(0, 0, 0, 0.15)';
    toast.style.borderRadius = '0.25rem';
    toast.style.marginBottom = '10px';
    toast.style.opacity = '0';
    toast.style.transition = 'opacity 0.3s ease-in-out';
    
    // Toast 內容
    toast.innerHTML = `
        <div style="padding: 0.75rem 1.25rem; background-color: ${type === 'error' ? '#c82333' : '#f8f9fa'}; border-bottom: 1px solid ${type === 'error' ? '#bd2130' : '#dee2e6'}; display: flex; justify-content: space-between; align-items: center;">
            <strong style="margin-right: auto;">${title}</strong>
            <button type="button" style="background: transparent; border: 0; font-size: 1.25rem; font-weight: 700; line-height: 1; color: ${type === 'error' ? '#fff' : '#000'}; opacity: 0.5; cursor: pointer;" onclick="this.parentElement.parentElement.remove();">×</button>
        </div>
        <div style="padding: 0.75rem 1.25rem;">
            ${message}
        </div>
    `;
    
    // 添加到容器
    toastContainer.appendChild(toast);
    
    // 顯示 Toast
    setTimeout(() => {
        toast.style.opacity = '1';
    }, 10);
    
    // 自動移除
    setTimeout(() => {
        toast.style.opacity = '0';
        setTimeout(() => {
            toast.remove();
        }, 300);
    }, 5000);
}

// 表單驗證函數
function validateForm() {
    const spotName = document.getElementById('spotName');
    const spotLoc = document.getElementById('spotLoc');
    const spotDesc = document.getElementById('spotDesc');
    const tel = document.getElementById('tel');
    const website = document.getElementById('website');
    
    // 先清除所有錯誤提示
    hideAllFieldErrors();
    
    let hasError = false;
    let firstErrorField = null;
    
    // 1. 景點名稱驗證 (必填)
    if (!spotName.value.trim()) {
        showFieldError('spotName', '景點名稱不能為空');
        spotName.classList.add('error');
        spotName.style.borderColor = 'var(--md-sys-color-error)';
        hasError = true;
        if (!firstErrorField) firstErrorField = spotName;
    } else {
        const nameLength = spotName.value.trim().length;
        if (nameLength < 2 || nameLength > 50) {
            showFieldError('spotName', '字數必須在2-50字之間');
            spotName.classList.add('error');
            spotName.style.borderColor = 'var(--md-sys-color-error)';
            hasError = true;
            if (!firstErrorField) firstErrorField = spotName;
        } else {
            // 特殊字符檢查：禁止 < > { } [ ] | \ " '
            const forbiddenChars = /[<>{}[\]|\\\"']/;
            if (forbiddenChars.test(spotName.value)) {
                showFieldError('spotName', '景點名稱不能包含特殊字符 < > { } [ ] | \\ " \'');
                spotName.classList.add('error');
                spotName.style.borderColor = 'var(--md-sys-color-error)';
                hasError = true;
                if (!firstErrorField) firstErrorField = spotName;
            }
        }
    }
    
    // 2. 景點地址驗證 (必填)
    if (!spotLoc.value.trim()) {
        showFieldError('spotLoc', '請輸入有效的景點地址');
        spotLoc.classList.add('error');
        spotLoc.style.borderColor = 'var(--md-sys-color-error)';
        hasError = true;
        if (!firstErrorField) firstErrorField = spotLoc;
    } else {
        const locLength = spotLoc.value.trim().length;
        if (locLength < 5 || locLength > 100) {
            showFieldError('spotLoc', '地址字數必須在5-100字之間');
            spotLoc.classList.add('error');
            spotLoc.style.borderColor = 'var(--md-sys-color-error)';
            hasError = true;
            if (!firstErrorField) firstErrorField = spotLoc;
        } else {
            // 檢查特殊字符
            const forbiddenChars = /[<>{}[\]|\\\"']/;
            if (forbiddenChars.test(spotLoc.value)) {
                showFieldError('spotLoc', '景點地址不能包含特殊字符 < > { } [ ] | \\ " \'');
                spotLoc.classList.add('error');
                spotLoc.style.borderColor = 'var(--md-sys-color-error)';
                hasError = true;
                if (!firstErrorField) firstErrorField = spotLoc;
            }
        }
    }
    
    // 3. 景點描述驗證 (必填)
    if (!spotDesc.value.trim()) {
        showFieldError('spotDesc', '景點描述至少需要5個字');
        spotDesc.classList.add('error');
        spotDesc.style.borderColor = 'var(--md-sys-color-error)';
        hasError = true;
        if (!firstErrorField) firstErrorField = spotDesc;
    } else {
        const descLength = spotDesc.value.trim().length;
        if (descLength < 10) {
            showFieldError('spotDesc', '景點描述至少需要5個字');
            spotDesc.classList.add('error');
            spotDesc.style.borderColor = 'var(--md-sys-color-error)';
            hasError = true;
            if (!firstErrorField) firstErrorField = spotDesc;
        } else if (descLength > 500) {
            showFieldError('spotDesc', '景點描述不能超過500個字');
            spotDesc.classList.add('error');
            spotDesc.style.borderColor = 'var(--md-sys-color-error)';
            hasError = true;
            if (!firstErrorField) firstErrorField = spotDesc;
        }
    }
    
    // 4. 聯絡電話驗證 (選填，簡化格式檢查)
    if (tel.value.trim()) {
        // 簡單格式檢查：只檢查是否為數字、空格、連字號
        const telPattern = /^[\d\-\s]+$/;
        if (!telPattern.test(tel.value.trim())) {
            showFieldError('tel', '電話格式不正確，請只使用數字、連字號和空格');
            tel.classList.add('error');
            tel.style.borderColor = 'var(--md-sys-color-error)';
            hasError = true;
            if (!firstErrorField) firstErrorField = tel;
        }
    }
    
    // 5. 官方網站驗證 (選填，簡單URL檢查)
    if (website.value.trim()) {
        const url = website.value.trim();
        if (!url.startsWith('http://') && !url.startsWith('https://')) {
            showFieldError('website', '網站URL必須包含http://或https://');
            website.classList.add('error');
            website.style.borderColor = 'var(--md-sys-color-error)';
            hasError = true;
            if (!firstErrorField) firstErrorField = website;
        }
    }
    
    // 如果有錯誤，顯示提示並聚焦到第一個錯誤欄位
    if (hasError) {
        showToast('驗證錯誤', '請檢查並修正表單中的錯誤', 'error');
        if (firstErrorField) {
            firstErrorField.focus();
            firstErrorField.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }
        return false;
    }
    
    return true;
}

// 錯誤提示管理
function showFieldError(fieldId, message) {
    const errorElement = document.getElementById(fieldId + 'Error');
    if (errorElement) {
        errorElement.textContent = message;
        errorElement.classList.add('show');
        errorElement.style.display = 'block';
    }
}

function hideFieldError(fieldId) {
    const errorElement = document.getElementById(fieldId + 'Error');
    if (errorElement) {
        errorElement.classList.remove('show');
        errorElement.style.display = 'none';
    }
}

function hideAllFieldErrors() {
    const errorElements = document.querySelectorAll('.error-message');
    errorElements.forEach(element => {
        element.classList.remove('show');
        element.style.display = 'none';
    });
}

// 即時驗證功能
function setupRealTimeValidation() {
    const spotName = document.getElementById('spotName');
    const spotLoc = document.getElementById('spotLoc');
    const spotDesc = document.getElementById('spotDesc');
    const tel = document.getElementById('tel');
    const website = document.getElementById('website');
    
    // 景點名稱即時驗證
    if (spotName) {
        spotName.addEventListener('input', function() {
            const value = this.value.trim();
            const length = value.length;
            const helperText = this.parentElement?.querySelector('.helper-text');
            
            if (helperText && !helperText.querySelector('.char-count')) {
                return; // 如果沒有字符計數器，跳過
            }
            
            // 移除之前的錯誤狀態
            this.classList.remove('error');
            this.style.borderColor = '';
            hideFieldError('spotName');
            
            if (length === 0) {
                // 空值時不顯示錯誤，等提交時再檢查
                return;
            }
            
            if (length < 2) {
                this.classList.add('error');
                this.style.borderColor = 'var(--md-sys-color-error)';
                showFieldError('spotName', '景點名稱至少需要2個字');
            } else if (length > 50) {
                this.classList.add('error');
                this.style.borderColor = 'var(--md-sys-color-error)';
                showFieldError('spotName', '景點名稱不能超過50個字');
            } else {
                // 檢查特殊字符
                const forbiddenChars = /[<>{}[\]|\\\"']/;
                if (forbiddenChars.test(value)) {
                    this.classList.add('error');
                    this.style.borderColor = 'var(--md-sys-color-error)';
                    showFieldError('spotName', '景點名稱不能包含特殊字符 < > { } [ ] | \\ " \'');
                }
            }
            
            // 更新驗證摘要
            setTimeout(updateValidationSummary, 50);
        });
    }
    
    // 景點地址即時驗證
    if (spotLoc) {
        spotLoc.addEventListener('input', function() {
            const value = this.value.trim();
            const length = value.length;
            
            this.classList.remove('error');
            this.style.borderColor = '';
            hideFieldError('spotLoc');
            
            if (length === 0) {
                return;
            }
            
            if (length < 5) {
                this.classList.add('error');
                this.style.borderColor = 'var(--md-sys-color-error)';
                showFieldError('spotLoc', '景點地址至少需要5個字');
            } else if (length > 100) {
                this.classList.add('error');
                this.style.borderColor = 'var(--md-sys-color-error)';
                showFieldError('spotLoc', '景點地址不能超過100個字');
            } else {
                // 檢查特殊字符
                const forbiddenChars = /[<>{}[\]|\\\"']/;
                if (forbiddenChars.test(value)) {
                    this.classList.add('error');
                    this.style.borderColor = 'var(--md-sys-color-error)';
                    showFieldError('spotLoc', '景點地址不能包含特殊字符 < > { } [ ] | \\ " \'');
                }
            }
            
            // 更新驗證摘要
            setTimeout(updateValidationSummary, 50);
        });
    }
    
    // 景點描述即時驗證
    if (spotDesc) {
        spotDesc.addEventListener('input', function() {
            const value = this.value.trim();
            const length = value.length;
            
            this.classList.remove('error');
            this.style.borderColor = '';
            hideFieldError('spotDesc');
            
            if (length === 0) {
                return;
            }
            
            if (length < 10) {
                this.classList.add('error');
                this.style.borderColor = 'var(--md-sys-color-error)';
                showFieldError('spotDesc', '景點描述至少需要5個字');
            } else if (length > 500) {
                this.classList.add('error');
                this.style.borderColor = 'var(--md-sys-color-error)';
                showFieldError('spotDesc', '景點描述不能超過500個字');
            }
            
            // 更新驗證摘要
            setTimeout(updateValidationSummary, 50);
        });
    }
    
    // 電話號碼即時驗證
    if (tel) {
        tel.addEventListener('input', function() {
            const value = this.value.trim();
            
            this.classList.remove('error');
            this.style.borderColor = '';
            hideFieldError('tel');
            
            if (value.length === 0) {
                return; // 選填欄位，空值時不驗證
            }
            
            const telPattern = /^[\d\-\s]+$/;
            if (!telPattern.test(value)) {
                this.classList.add('error');
                this.style.borderColor = 'var(--md-sys-color-error)';
                showFieldError('tel', '電話格式不正確，請只使用數字、連字號和空格');
            }
            
            // 更新驗證摘要
            setTimeout(updateValidationSummary, 50);
        });
    }
    
    // 網站URL即時驗證
    if (website) {
        website.addEventListener('input', function() {
            const value = this.value.trim();
            
            this.classList.remove('error');
            this.style.borderColor = '';
            hideFieldError('website');
            
            if (value.length === 0) {
                return; // 選填欄位，空值時不驗證
            }
            
            if (!value.startsWith('http://') && !value.startsWith('https://')) {
                this.classList.add('error');
                this.style.borderColor = 'var(--md-sys-color-error)';
                showFieldError('website', '網站URL必須包含http://或https://');
            }
            
            // 更新驗證摘要
            setTimeout(updateValidationSummary, 50);
        });
    }
}

// 驗證狀態摘要
function updateValidationSummary() {
    const spotName = document.getElementById('spotName');
    const spotLoc = document.getElementById('spotLoc');
    const spotDesc = document.getElementById('spotDesc');
    const tel = document.getElementById('tel');
    const website = document.getElementById('website');
    
    let validationErrors = [];
    
    // 檢查必填欄位
    if (!spotName.value.trim()) {
        validationErrors.push('景點名稱為必填');
    } else {
        const nameLength = spotName.value.trim().length;
        if (nameLength < 2 || nameLength > 50) {
            validationErrors.push('景點名稱字數不符合要求');
        }
        const forbiddenChars = /[<>{}[\]|\\\"']/;
        if (forbiddenChars.test(spotName.value)) {
            validationErrors.push('景點名稱包含特殊字符');
        }
    }
    
    if (!spotLoc.value.trim()) {
        validationErrors.push('景點地址為必填');
    } else {
        const locLength = spotLoc.value.trim().length;
        if (locLength < 5 || locLength > 100) {
            validationErrors.push('景點地址字數不符合要求');
        }
        const forbiddenChars = /[<>{}[\]|\\\"']/;
        if (forbiddenChars.test(spotLoc.value)) {
            validationErrors.push('景點地址包含特殊字符');
        }
    }
    
    if (!spotDesc.value.trim()) {
        validationErrors.push('景點描述為必填');
    } else {
        const descLength = spotDesc.value.trim().length;
        if (descLength < 10 || descLength > 500) {
            validationErrors.push('景點描述字數不符合要求');
        }
    }
    
    // 檢查選填欄位格式
    if (tel.value.trim()) {
        const telPattern = /^[\d\-\s]+$/;
        if (!telPattern.test(tel.value.trim())) {
            validationErrors.push('電話格式不正確');
        }
    }
    
    if (website.value.trim()) {
        const url = website.value.trim();
        if (!url.startsWith('http://') && !url.startsWith('https://')) {
            validationErrors.push('網站URL格式不正確');
        }
    }
    
    // 更新提交按鈕狀態
    const submitBtn = document.getElementById('submitBtn');
    if (submitBtn) {
        if (validationErrors.length === 0) {
            submitBtn.disabled = false;
            submitBtn.classList.remove('btn-disabled');
            submitBtn.title = '表單驗證通過，可以提交';
        } else {
            submitBtn.disabled = true;
            submitBtn.classList.add('btn-disabled');
            submitBtn.title = `表單驗證失敗：${validationErrors.join('、')}`;
        }
    }
    
    return validationErrors.length === 0;
}

// 更新狀態開關
function updateStatusSwitch() {
    const statusCheckbox = document.getElementById('statusCheckbox');
    const spotStatusInput = document.querySelector('input[name="spotStatus"]');
    const statusText = document.querySelector('.status-text');
    const statusBadge = document.querySelector('.status-badge');

    if (statusCheckbox && spotStatusInput && statusText) {
        // 檢查是否為新增模式（沒有景點ID的隱藏欄位）
        const isAddMode = !document.querySelector('input[name="spotId"]');
        
        if (isAddMode) {
            // 新增模式：強制設為待審核狀態，禁用狀態開關
            spotStatusInput.value = '0';
            statusCheckbox.checked = false;
            statusCheckbox.disabled = true;
            statusText.textContent = '待審核';
            statusText.style.color = 'var(--md-sys-color-secondary)';
            
            // 更新狀態徽章
            if (statusBadge) {
                statusBadge.className = 'status-badge badge-secondary';
                const icon = statusBadge.querySelector('.material-icons');
                if (icon) icon.textContent = 'hourglass_empty';
            }
            
            // 添加說明文字
            const helperText = statusText.closest('.form-group').querySelector('.helper-text');
            if (helperText) {
                helperText.textContent = '新增的景點需要經過審核後才能上架';
                helperText.style.color = 'var(--md-sys-color-secondary)';
            }
        } else {
            // 編輯模式：使用原有邏輯
            statusCheckbox.addEventListener('change', function() {
                const isChecked = this.checked;
                spotStatusInput.value = isChecked ? '1' : '3';
                statusText.textContent = isChecked ? '上架' : '下架';
                
                // 更新狀態徽章
                if (statusBadge) {
                    statusBadge.className = isChecked ? 
                        'status-badge badge-success' : 
                        'status-badge badge-secondary';
                    const icon = statusBadge.querySelector('.material-icons');
                    if (icon) {
                        icon.textContent = isChecked ? 'visibility' : 'visibility_off';
                    }
                }
                
                // 添加視覺回饋
                statusText.style.color = isChecked ? 
                    'var(--md-sys-color-primary)' : 
                    'var(--md-sys-color-on-surface-variant)';
                    
                // 添加切換動畫
                statusBadge.style.transform = 'scale(1.1)';
                setTimeout(() => {
                    statusBadge.style.transform = '';
                }, 200);
            });
            
            // 初始化狀態文字顏色和徽章
            const isChecked = statusCheckbox.checked;
            statusText.style.color = isChecked ? 
                'var(--md-sys-color-primary)' : 
                'var(--md-sys-color-on-surface-variant)';
                
            if (statusBadge) {
                statusBadge.className = isChecked ? 
                    'status-badge badge-success' : 
                    'status-badge badge-secondary';
                const icon = statusBadge.querySelector('.material-icons');
                if (icon) {
                    icon.textContent = isChecked ? 'visibility' : 'visibility_off';
                }
            }
                
            // 初始化隱藏欄位的值
            spotStatusInput.value = isChecked ? '1' : '3';
        }
    }
}

// 增強的字數計數器
function setupCharacterCounters() {
    const fields = [
        { id: 'spotName', max: 50, min: 2, required: true },
        { id: 'spotLoc', max: 100, min: 5, required: true },
        { id: 'spotDesc', max: 500, min: 10, required: true }
    ];
    
    fields.forEach(field => {
        const input = document.getElementById(field.id);
        const helperText = input?.parentElement?.querySelector('.helper-text');
        const charCount = helperText?.querySelector('.char-count');
        
        if (input && helperText && charCount) {
            function updateCounter() {
                const length = input.value.length;
                const percentage = (length / field.max) * 100;
                
                // 更新字數顯示
                charCount.textContent = `(${length}/${field.max})`;
                
                // 移除之前的錯誤樣式
                input.classList.remove('error');
                input.style.borderColor = '';
                
                // 清除對應的錯誤提示
                hideFieldError(field.id);
                
                // 根據驗證規則改變顏色和樣式
                if (length > field.max) {
                    // 超過最大限制
                    charCount.style.color = 'var(--md-sys-color-error)';
                    charCount.textContent = `(${length}/${field.max}) ⚠️ 超過限制`;
                    input.classList.add('error');
                    input.style.borderColor = 'var(--md-sys-color-error)';
                    showFieldError(field.id, `字數不能超過${field.max}個字`);
                } else if (field.required && length < field.min && length > 0) {
                    // 必填欄位但字數不足
                    charCount.style.color = 'var(--md-sys-color-secondary)';
                    charCount.textContent = `(${length}/${field.max}) 至少需要${field.min}字`;
                    input.classList.add('error');
                    input.style.borderColor = 'var(--md-sys-color-error)';
                    showFieldError(field.id, `至少需要${field.min}個字`);
                } else if (field.required && length === 0) {
                    // 必填欄位為空
                    charCount.style.color = 'var(--md-sys-color-secondary)';
                    charCount.textContent = `(${length}/${field.max}) 必填`;
                } else if (percentage > 90) {
                    // 接近上限
                    charCount.style.color = 'var(--md-sys-color-secondary)';
                    charCount.textContent = `(${length}/${field.max}) 接近上限`;
                } else if (percentage > 75) {
                    // 使用率較高
                    charCount.style.color = 'var(--md-sys-color-secondary)';
                    charCount.textContent = `(${length}/${field.max})`;
                } else {
                    // 正常狀態
                    charCount.style.color = 'var(--md-sys-color-primary)';
                    charCount.textContent = `(${length}/${field.max})`;
                }
                
                // 特殊字符檢查（景點名稱和地址）
                if ((field.id === 'spotName' || field.id === 'spotLoc') && input.value.length > 0) {
                    const forbiddenChars = /[<>{}[\]|\\\"']/;
                    if (forbiddenChars.test(input.value)) {
                        charCount.style.color = 'var(--md-sys-color-error)';
                        charCount.textContent = `(${length}/${field.max}) ⚠️ 含特殊字符`;
                        input.classList.add('error');
                        input.style.borderColor = 'var(--md-sys-color-error)';
                        const fieldName = field.id === 'spotName' ? '景點名稱' : '景點地址';
                        showFieldError(field.id, `${fieldName}不能包含特殊字符 < > { } [ ] | \\ " \'`);
                    }
                }
            }
            
            input.addEventListener('input', () => {
                updateCounter();
                setTimeout(updateValidationSummary, 50); // 延遲更新驗證摘要
            });
            input.addEventListener('paste', () => setTimeout(() => {
                updateCounter();
                updateValidationSummary();
            }, 10));
            updateCounter(); // 初始化
        }
    });
}

// 表單提交處理
function setupFormSubmission() {
    const form = document.getElementById('spotForm');
    if (!form) return;

    form.addEventListener('submit', async function(e) {
        e.preventDefault();
        
        if (!validateForm()) {
            return;
        }

        // 建立 FormData 物件
        const formData = new FormData(form);
        
        // 添加多圖上傳
        const imageFiles = document.querySelectorAll('#multiImageList input[type="file"]');
        imageFiles.forEach((file, index) => {
            if (file.files[0]) {
                formData.append('multiImages', file.files[0]);
            }
        });

        try {
            const response = await fetch(form.action, {
                method: 'POST',
                body: formData
            });

            const result = await response.json();
            
            if (result.success) {
                showToast('成功', '景點資料已儲存');
                // 重導向到列表頁面
                window.location.href = '/admin/spot/spotlist';
            } else {
                showToast('錯誤', result.message || '儲存失敗，請檢查資料', 'error');
            }
        } catch (error) {
            console.error('提交表單時發生錯誤:', error);
            showToast('錯誤', '系統錯誤，請稍後再試', 'error');
        }
    });
}

// 輸入框焦點效果
function setupInputEffects() {
    const inputs = document.querySelectorAll('.form-control');
    
    inputs.forEach(input => {
        // 焦點進入效果
        input.addEventListener('focus', function() {
            this.parentElement.classList.add('focused');
            
            // 添加焦點動畫
            this.style.transform = 'scale(1.02)';
            setTimeout(() => {
                this.style.transform = '';
            }, 200);
        });
        
        // 焦點離開效果
        input.addEventListener('blur', function() {
            this.parentElement.classList.remove('focused');
        });
        
        // 輸入時的動畫效果
        input.addEventListener('input', function() {
            if (this.value.length > 0) {
                this.parentElement.classList.add('has-value');
            } else {
                this.parentElement.classList.remove('has-value');
            }
        });
        
        // 初始化狀態
        if (input.value.length > 0) {
            input.parentElement.classList.add('has-value');
        }
    });
}

// 顯示後端訊息
function showBackendMessages() {
    // 檢查成功訊息
    const successAlert = document.querySelector('.alert-success');
    if (successAlert) {
        const message = successAlert.querySelector('span')?.textContent;
        if (message) {
            showToast(message, 'success');
        }
    }
    
    // 檢查錯誤訊息
    const errorAlert = document.querySelector('.alert-error');
    if (errorAlert) {
        const message = errorAlert.querySelector('span')?.innerHTML;
        if (message) {
            showToast(message, 'error');
        }
    }
}

// 鍵盤快捷鍵
function setupKeyboardShortcuts() {
    document.addEventListener('keydown', function(e) {
        // Ctrl/Cmd + Enter 提交表單
        if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
            e.preventDefault();
            const submitBtn = document.getElementById('submitBtn');
            if (submitBtn && !submitBtn.disabled) {
                submitBtn.click();
            }
        }
        
        // Escape 鍵返回列表
        if (e.key === 'Escape') {
            const backBtn = document.querySelector('.btn-secondary');
            if (backBtn) {
                if (confirm('確定要離開此頁面嗎？未儲存的變更將會遺失。')) {
                    backBtn.click();
                }
            }
        }
    });
}

// 自動儲存功能（本地儲存）
function setupAutoSave() {
    const form = document.getElementById('spotForm');
    if (!form) return;
    
    const formData = {};
    const inputs = form.querySelectorAll('input[type="text"], textarea');
    
    // 載入已儲存的資料
    inputs.forEach(input => {
        const savedValue = localStorage.getItem(`spotForm_${input.id}`);
        if (savedValue && !input.value) {
            input.value = savedValue;
        }
    });
    
    // 自動儲存
    inputs.forEach(input => {
        input.addEventListener('input', function() {
            localStorage.setItem(`spotForm_${this.id}`, this.value);
        });
    });
    
    // 表單提交後清除儲存的資料
    form.addEventListener('submit', function() {
        inputs.forEach(input => {
            localStorage.removeItem(`spotForm_${input.id}`);
        });
    });
}

/**
 * 設置地址自動完成功能
 * 當輸入地址時自動提取地區並設置到地區下拉選單
 */
function setupAddressAutoComplete() {
    const spotNameInput = document.getElementById('spotName');
    const spotLocInput = document.getElementById('spotLoc');
    const regionSelect = document.getElementById('region');
    
    if (!spotLocInput || !regionSelect) return;
    
    // 設置景點資訊變化監聽
    setupSpotInfoChangeListener();
    
    // 監聽地址輸入框的blur事件
    spotLocInput.addEventListener('blur', function() {
        const address = this.value.trim();
        if (!address) return;
        
        // 提取地區
        const region = extractRegionFromAddress(address);
        if (region) {
            // 設置地區下拉選單
            regionSelect.value = region;
            
            // 觸發change事件，確保任何監聽地區變化的代碼都能正確執行
            const event = new Event('change', { bubbles: true });
            regionSelect.dispatchEvent(event);
            
            console.log('自動設置地區:', region);
            
            // 自動獲取Google Places資訊（電話、網站等）
            fetchGooglePlaceData(true);
        }
    });
}

/**
 * 設置景點資訊變化監聽器
 * 當景點名稱或地址變化時，清空相關的自動填充欄位
 */
function setupSpotInfoChangeListener() {
    const spotNameInput = document.getElementById('spotName');
    const spotLocInput = document.getElementById('spotLoc');
    
    if (!spotNameInput || !spotLocInput) return;
    
    let lastSpotName = spotNameInput.value;
    let lastSpotLoc = spotLocInput.value;
    
    // 清空相關欄位的函數
    function clearAutoFilledFields() {
        const telInput = document.getElementById('tel');
        const websiteInput = document.getElementById('website');
        const googleRatingInput = document.getElementById('googleRating');
        const googlePlaceIdInput = document.getElementById('googlePlaceId');
        
        // 清空電話欄位
        if (telInput) {
            telInput.value = '';
            telInput.dispatchEvent(new Event('input', { bubbles: true }));
        }
        
        // 清空網站欄位
        if (websiteInput) {
            websiteInput.value = '';
            websiteInput.dispatchEvent(new Event('input', { bubbles: true }));
        }
        
        // 清空Google評分
        if (googleRatingInput) {
            googleRatingInput.value = '';
        }
        
        // 清空Google Place ID
        if (googlePlaceIdInput) {
            googlePlaceIdInput.value = '';
        }
        
        console.log('已清空自動填充的欄位（電話、網站、Google評分）');
    }
    
    // 監聽景點名稱變化
    spotNameInput.addEventListener('input', function() {
        const currentValue = this.value.trim();
        if (currentValue !== lastSpotName && lastSpotName !== '') {
            clearAutoFilledFields();
            console.log('景點名稱已變更，清空相關欄位');
        }
        lastSpotName = currentValue;
    });
    
    // 監聽地址變化
    spotLocInput.addEventListener('input', function() {
        const currentValue = this.value.trim();
        if (currentValue !== lastSpotLoc && lastSpotLoc !== '') {
            clearAutoFilledFields();
            console.log('景點地址已變更，清空相關欄位');
        }
        lastSpotLoc = currentValue;
    });
}

/**
 * 從地址中提取地區（縣市）
 * @param {string} address 地址
 * @return {string|null} 提取的地區，如果無法提取則返回null
 */
function extractRegionFromAddress(address) {
    if (!address) return null;
    
    // 台灣縣市列表
    const regions = [
        '台北市', '臺北市', '新北市', '桃園市', '台中市', '臺中市', '台南市', '臺南市', '高雄市',
        '基隆市', '新竹市', '嘉義市', '新竹縣', '苗栗縣', '彰化縣', '南投縣', '雲林縣',
        '嘉義縣', '屏東縣', '宜蘭縣', '花蓮縣', '台東縣', '臺東縣', '澎湖縣', '金門縣', '連江縣'
    ];
    
    // 尋找地址中的縣市
    for (const region of regions) {
        if (address.includes(region)) {
            // 處理台/臺的差異
            if (region === '臺北市') return '台北市';
            if (region === '臺中市') return '台中市';
            if (region === '臺南市') return '台南市';
            if (region === '臺東縣') return '台東縣';
            return region;
        }
    }
    
    return null;
}

/**
 * 從Google Places API獲取景點評分和資訊
 * @param {boolean} silent 是否靜默模式（不顯示Toast通知）
 * @param {boolean} forceUpdate 是否強制更新電話和網站欄位（不檢查是否為空）
 */
async function fetchGooglePlaceData(silent = false, forceUpdate = false) {
    const spotName = document.getElementById('spotName').value.trim();
    const spotLoc = document.getElementById('spotLoc').value.trim();
    const googleRatingInput = document.getElementById('googleRating');
    const googlePlaceIdInput = document.getElementById('googlePlaceId');
    
    if (!spotName || !spotLoc) {
        if (!silent) {
            showToast('請先填寫景點名稱和地址', 'warning');
        }
        return;
    }
    
    try {
        if (!silent) {
            showToast('正在從Google獲取景點資訊...', 'info');
        }
        
        console.log('發送請求到API: /admin/spot/api/google-place-info');
        console.log('請求參數:', { name: spotName, address: spotLoc });
        
        // 獲取CSRF令牌（如果有的話）
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
        
        // 調用後端API來獲取Google Places資訊
        const headers = {
            'Content-Type': 'application/json'
        };
        
        // 如果有CSRF令牌，添加到請求頭中
        if (csrfToken && csrfHeader) {
            console.log('添加CSRF令牌到請求頭');
            headers[csrfHeader] = csrfToken;
        }
        
        const response = await fetch('/admin/spot/api/google-place-info', {
            method: 'POST',
            headers: headers,
            body: JSON.stringify({
                name: spotName,
                address: spotLoc
            }),
            credentials: 'same-origin'
        });
        
        console.log('API響應狀態:', response.status);
        
        if (!response.ok) {
            console.error('API響應錯誤:', response.status, response.statusText);
            throw new Error(`HTTP錯誤: ${response.status} - ${response.statusText}`);
        }
        
        const data = await response.json();
        console.log('API響應數據:', JSON.stringify(data, null, 2)); // 輸出完整響應數據，格式化顯示
        
        if (data.success) {
            // 檢查是否有錯誤信息
            if (data.error) {
                console.error('API返回錯誤:', data.error);
                if (!silent) {
                    showToast(`獲取Google Places資訊失敗: ${data.error}`, 'error');
                }
                return;
            }
            
            let hasInfo = false; // 跟踪是否獲取到任何信息
            
            // 處理評分 - 特別注意評分可能是0（有效值）
            console.log('評分數據:', data.rating);
            if (data.rating !== undefined && data.rating !== null) {
                googleRatingInput.value = data.rating;
                console.log('設置評分:', data.rating);
                if (!silent) {
                    showToast(`成功獲取Google評分: ${data.rating}`, 'success');
                }
                hasInfo = true;
            } else {
                googleRatingInput.value = '';
                console.log('未找到評分數據');
                if (!silent) {
                    showToast('此景點在Google上沒有評分', 'info');
                }
            }
            
            // 保存Google Place ID
            if (data.placeId) {
                googlePlaceIdInput.value = data.placeId;
                console.log('設置Place ID:', data.placeId);
                hasInfo = true;
            }
            
            // 處理電話和網站資訊
            const telInput = document.getElementById('tel');
            const websiteInput = document.getElementById('website');
            
            // 檢查電話 - 如果forceUpdate為true或欄位為空時填充
            console.log('電話數據:', data.phoneNumber);
            if (data.phoneNumber && data.phoneNumber.trim() !== '') {
                if (forceUpdate || !telInput.value || telInput.value.trim() === '') {
                    telInput.value = data.phoneNumber;
                    // 觸發input事件，確保樣式更新
                    telInput.dispatchEvent(new Event('input', { bubbles: true }));
                    console.log('填充電話:', data.phoneNumber);
                    hasInfo = true;
                } else {
                    console.log('電話欄位已有值，不覆蓋:', telInput.value);
                }
            } else {
                console.log('未找到電話數據或數據為空');
            }
            
            // 檢查網站 - 如果forceUpdate為true或欄位為空時填充
            console.log('網站數據:', data.website);
            if (data.website && data.website.trim() !== '') {
                if (forceUpdate || !websiteInput.value || websiteInput.value.trim() === '') {
                    websiteInput.value = data.website;
                    // 觸發input事件，確保樣式更新
                    websiteInput.dispatchEvent(new Event('input', { bubbles: true }));
                    console.log('填充網站:', data.website);
                    hasInfo = true;
                } else {
                    console.log('網站欄位已有值，不覆蓋:', websiteInput.value);
                }
            } else {
                console.log('未找到網站數據或數據為空');
            }
            
            // 處理 googleSearchUrl（如果有）
            if (data.googleSearchUrl && !hasInfo && !silent) {
                console.log('提供Google搜索URL作為替代:', data.googleSearchUrl);
                const searchUrl = data.googleSearchUrl;
                
                // 創建一個按鈕，讓用戶可以直接在Google上搜索
                const message = `未找到景點資訊，您可以 <a href="${searchUrl}" target="_blank" style="color: white; text-decoration: underline;">在Google上搜索</a>`;
                showToast(message, 'info');
            }
            
            // 如果是靜默模式且成功獲取了資訊，顯示一個簡短的成功通知
            if (silent && hasInfo) {
                showToast('已自動填充景點資訊', 'success');
            } else if (silent && !hasInfo) {
                console.log('未獲取到任何有用的景點資訊');
            } else if (!silent && !hasInfo && data.message) {
                // 如果沒有獲取到資訊，且有錯誤消息，顯示錯誤消息
                showToast(data.message, 'warning');
            }
        } else {
            console.error('API響應成功但數據無效:', data);
            if (!silent) {
                throw new Error(data.message || '無法獲取Google Places資訊');
            }
        }
    } catch (error) {
        console.error('獲取Google Places資訊失敗:', error);
        if (!silent) {
            showToast(`獲取Google Places資訊失敗: ${error.message}`, 'error');
        }
    }
}

// Google Places API 整合
document.addEventListener('DOMContentLoaded', function() {
    const fetchGoogleRatingBtn = document.getElementById('fetchGoogleRating');
    if (fetchGoogleRatingBtn) {
        fetchGoogleRatingBtn.addEventListener('click', () => fetchGooglePlaceData(false, true));
    }
});

// ===== 圖片上傳處理 =====
function setupMultiImageUpload() {
    const MAX_IMAGES = 1;
    const MAX_SIZE = 2 * 1024 * 1024; // 2MB
    const ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/jpg'];
    
    const imageContainer = document.getElementById('imageContainer');
    const imageInput = document.getElementById('imageInput');
    
    if (!imageInput || !imageContainer) return;
    
    imageInput.addEventListener('change', function(e) {
        const file = e.target.files[0];
        if (!file) return;
        
        // 檢查檔案類型
        if (!ALLOWED_TYPES.includes(file.type)) {
            showToast('錯誤', '僅允許上傳 JPG、PNG 格式圖片', 'error');
            this.value = '';
            return;
        }
        
        // 檢查檔案大小
        if (file.size > MAX_SIZE) {
            showToast('錯誤', '單張圖片不可超過2MB', 'error');
            this.value = '';
            return;
        }
        
        // 清空現有預覽
        imageContainer.innerHTML = '';
        
        // 建立預覽
        const reader = new FileReader();
        reader.onload = function(e) {
            const preview = document.createElement('div');
            preview.className = 'image-preview';
            preview.innerHTML = `
                <img src="${e.target.result}" alt="預覽圖" style="max-width: 200px; max-height: 200px;">
                <button type="button" class="btn btn-danger btn-sm mt-2">刪除</button>
            `;
            
            // 刪除按鈕事件
            preview.querySelector('button').addEventListener('click', function() {
                preview.remove();
                imageInput.value = '';
            });
            
            imageContainer.appendChild(preview);
        };
        reader.readAsDataURL(file);
    });
}

// 初始化
document.addEventListener('DOMContentLoaded', function() {
    console.log('景點表單頁面初始化');
    
    // 初始化各項功能
    updateStatusSwitch();
    setupCharacterCounters();
    setupRealTimeValidation();
    setupFormSubmission();
    setupInputEffects();
    setupKeyboardShortcuts();
    setupAutoSave();
    showBackendMessages();
    setupAddressAutoComplete();
    setupSpotInfoChangeListener();
    
    // 初始化驗證摘要
    setTimeout(updateValidationSummary, 100);
    
    console.log('景點表單頁面初始化完成');
}); 