/**
 * 行程表單頁面 JavaScript
 * 負責處理表單驗證、互動效果等功能
 */

// DOM 載入完成後初始化
document.addEventListener('DOMContentLoaded', function() {
    console.log('行程表單頁面初始化...');
    
    initializeFormValidation();
    initializeFormInteractions();
    
    console.log('行程表單頁面初始化完成');
});

/**
 * 初始化表單驗證
 */
function initializeFormValidation() {
    const form = document.querySelector('.itinerary-form');
    if (!form) return;
    
    // 即時驗證行程名稱
    const nameInput = document.getElementById('itnName');
    if (nameInput) {
        // 初始狀態檢查
        if (nameInput.value.trim()) {
            validateItineraryName(nameInput);
        }
        
        // 即時驗證 - 輸入時
        nameInput.addEventListener('input', function() {
            validateItineraryName(this);
        });
        
        // 失去焦點時強制驗證
        nameInput.addEventListener('blur', function() {
            validateItineraryName(this, true);
        });
    }
    
    // 即時驗證行程描述
    const descInput = document.getElementById('itnDesc');
    if (descInput) {
        // 初始狀態檢查
        if (descInput.value.trim()) {
            validateItineraryDescription(descInput);
        }
        
        // 即時驗證
        descInput.addEventListener('input', function() {
            validateItineraryDescription(this);
        });
        
        // 失去焦點時強制驗證
        descInput.addEventListener('blur', function() {
            validateItineraryDescription(this, true);
        });
    }
    
    // 表單提交驗證
    form.addEventListener('submit', function(e) {
        if (!validateForm()) {
            e.preventDefault();
            showValidationError('請修正表單中的錯誤後再提交');
            
            // 滾動到第一個錯誤欄位
            const firstError = form.querySelector('.form-control.error');
            if (firstError) {
                firstError.focus();
                firstError.scrollIntoView({ behavior: 'smooth', block: 'center' });
            }
        }
    });
}

/**
 * 驗證行程名稱
 * @param {HTMLElement} input - 輸入元素
 * @param {boolean} showEmpty - 是否顯示空值錯誤（用於失焦時）
 * @returns {boolean} 驗證是否通過
 */
function validateItineraryName(input, showEmpty = false) {
    const value = input.value.trim();
    const minLength = 2;
    const maxLength = 50;
    
    // 清除先前的錯誤
    clearFieldError(input);
    
    // 空值檢查 - 僅當showEmpty為true或有值時檢查
    if ((showEmpty && !value) || (value && value.length === 0)) {
        showFieldError(input, '行程名稱不能為空');
        return false;
    }
    
    // 如果為空且不顯示空值錯誤，則直接返回
    if (!value && !showEmpty) {
        return false;
    }
    
    // 最小長度檢查
    if (value.length < minLength) {
        showFieldError(input, `行程名稱長度不能少於 ${minLength} 個字元`);
        return false;
    }
    
    // 最大長度檢查
    if (value.length > maxLength) {
        showFieldError(input, `行程名稱長度不能超過 ${maxLength} 個字元`);
        return false;
    }
    
    // 特殊字符檢查 - 擴展禁止字符列表
    const forbiddenCharsRegex = /[<>\{\}\[\]\\|"']/;
    if (forbiddenCharsRegex.test(value)) {
        showFieldError(input, '行程名稱不能包含以下特殊字符: < > { } [ ] \\ | " \'');
        return false;
    }
    
    // 敏感詞檢查
    const sensitiveWords = ['色情', '賭博', '詐騙', '毒品', '違法', '黃色', '賭場', 'casino', 'porn', 'drug', 'illegal'];
    const lowerValue = value.toLowerCase();
    
    for (const word of sensitiveWords) {
        if (lowerValue.includes(word.toLowerCase())) {
            showFieldError(input, `行程名稱包含敏感詞: ${word}`);
            return false;
        }
    }
    
    // 驗證通過
    showFieldSuccess(input);
    return true;
}

/**
 * 驗證行程描述
 * @param {HTMLElement} input - 輸入元素
 * @param {boolean} showEmpty - 是否顯示空值警告（用於失焦時）
 * @returns {boolean} 驗證是否通過
 */
function validateItineraryDescription(input, showEmpty = false) {
    const value = input.value.trim();
    const maxLength = 500;
    const minRecommendedLength = 10; // 降低到10個字元
    
    // 清除先前的錯誤
    clearFieldError(input);
    
    // 更新字數計數
    updateCharacterCount(input, value.length, maxLength);
    
    // 空值檢查 (僅警告，不阻止提交)
    if (showEmpty && !value) {
        showFieldWarning(input, '建議填寫行程描述，幫助用戶了解行程內容');
        return true; // 仍然返回true，因為這只是警告
    }
    
    // 最大長度檢查
    if (value.length > maxLength) {
        showFieldError(input, `行程描述長度不能超過 ${maxLength} 個字元`);
        return false;
    }
    
    // 建議最小長度檢查 (僅警告，不阻止提交)
    if (value.length > 0 && value.length < minRecommendedLength) {
        showFieldWarning(input, `行程描述建議至少 ${minRecommendedLength} 個字元，目前: ${value.length}`);
        return true; // 仍然返回true，因為這只是警告
    }
    
    // 驗證通過
    if (value.length > 0) {
        showFieldSuccess(input);
    }
    return true;
}

/**
 * 驗證整個表單
 */
function validateForm() {
    let isValid = true;
    
    // 驗證行程名稱
    const nameInput = document.getElementById('itnName');
    if (nameInput && !validateItineraryName(nameInput, true)) {
        isValid = false;
    }
    
    // 驗證行程描述
    const descInput = document.getElementById('itnDesc');
    if (descInput && !validateItineraryDescription(descInput, true)) {
        isValid = false;
    }
    
    // 驗證建立者ID（編輯時不需要驗證）
    const crtIdInput = document.getElementById('crtId');
    if (crtIdInput && !crtIdInput.readOnly) {
        const crtId = parseInt(crtIdInput.value);
        if (!crtId || crtId <= 0) {
            showFieldError(crtIdInput, '請輸入有效的建立者ID');
            isValid = false;
        } else {
            clearFieldError(crtIdInput);
        }
    }
    
    return isValid;
}

/**
 * 初始化表單互動效果
 */
function initializeFormInteractions() {
    // 狀態開關
    initializeStatusSwitch();
    
    // 字數計數器
    initializeCharacterCounters();
    
    // 表單重置確認
    initializeResetConfirmation();
    
    // 添加描述欄位的引導文字
    addDescriptionPlaceholder();
    
    // 初始化提交按鈕狀態
    initializeSubmitButton();
}

/**
 * 初始化狀態開關
 */
function initializeStatusSwitch() {
    const statusCheckbox = document.getElementById('statusCheckbox');
    const statusText = document.querySelector('.status-text');
    const statusBadge = document.querySelector('.status-badge');
    const hiddenInput = document.getElementById('isPublic');
    
    if (statusCheckbox && statusText && statusBadge && hiddenInput) {
        statusCheckbox.addEventListener('change', function() {
            updateStatusDisplay(this.checked, statusText, statusBadge, hiddenInput);
        });
        
        // 初始化狀態顯示
        updateStatusDisplay(statusCheckbox.checked, statusText, statusBadge, hiddenInput);
    }
}

/**
 * 更新狀態顯示
 */
function updateStatusDisplay(isPublic, statusText, statusBadge, hiddenInput) {
    if (isPublic) {
        statusText.textContent = '公開';
        statusBadge.className = 'status-badge badge-success';
        statusBadge.innerHTML = '<i class="material-icons">visibility</i>';
        hiddenInput.value = '1';
    } else {
        statusText.textContent = '私人';
        statusBadge.className = 'status-badge badge-secondary';
        statusBadge.innerHTML = '<i class="material-icons">visibility_off</i>';
        hiddenInput.value = '0';
    }
}

/**
 * 初始化字數計數器
 */
function initializeCharacterCounters() {
    // 行程名稱字數計數
    const nameInput = document.getElementById('itnName');
    if (nameInput) {
        const maxLength = 50;
        const currentLength = nameInput.value.length;
        updateCharacterCount(nameInput, currentLength, maxLength);
        
        nameInput.addEventListener('input', function() {
            updateCharacterCount(this, this.value.length, maxLength);
        });
    }
    
    // 行程描述字數計數
    const descInput = document.getElementById('itnDesc');
    if (descInput) {
        const maxLength = 500;
        const currentLength = descInput.value.length;
        updateCharacterCount(descInput, currentLength, maxLength);
        
        descInput.addEventListener('input', function() {
            updateCharacterCount(this, this.value.length, maxLength);
        });
    }
}

/**
 * 更新字數計數
 */
function updateCharacterCount(input, currentLength, maxLength) {
    // 找到對應的 helper-text 中的 char-count 元素
    const formGroup = input.closest('.form-group');
    if (!formGroup) return;
    
    const helperText = formGroup.querySelector('.helper-text');
    if (!helperText) return;
    
    let charCount = helperText.querySelector('.char-count');
    if (!charCount) return;
    
    // 更新計數顯示
    charCount.textContent = `(${currentLength}/${maxLength})`;
    
    // 根據字數添加樣式
    charCount.classList.remove('warning', 'danger', 'success');
    
    if (currentLength === 0) {
        // 空值
        charCount.classList.add('normal');
    } else if (currentLength <= maxLength * 0.8) {
        // 安全範圍
        charCount.classList.add('success');
    } else if (currentLength <= maxLength) {
        // 接近上限
        charCount.classList.add('warning');
    } else {
        // 超過上限
        charCount.classList.add('danger');
    }
}

/**
 * 初始化表單重置確認
 */
function initializeResetConfirmation() {
    const form = document.querySelector('.itinerary-form');
    const resetBtn = form ? form.querySelector('button[type="reset"]') : null;
    
    if (resetBtn) {
        resetBtn.addEventListener('click', function(e) {
            if (!confirm('確定要重置表單嗎？所有已填寫的資料將會清空。')) {
                e.preventDefault();
            }
        });
    }
}

/**
 * 顯示欄位錯誤
 */
function showFieldError(input, message) {
    const formGroup = input.closest('.form-group');
    if (!formGroup) return;
    
    // 清除先前的狀態
    clearFieldStatus(formGroup);
    
    // 添加錯誤樣式
    input.classList.add('error');
    formGroup.classList.add('has-error');
    
    // 創建或更新錯誤訊息
    let errorElement = formGroup.querySelector('.error-message');
    if (!errorElement) {
        errorElement = document.createElement('div');
        errorElement.className = 'error-message';
        
        // 插入到 helper-text 之後
        const helperText = formGroup.querySelector('.helper-text');
        if (helperText) {
            helperText.insertAdjacentElement('afterend', errorElement);
        } else {
            formGroup.appendChild(errorElement);
        }
    }
    
    // 設置錯誤訊息
    errorElement.innerHTML = `<i class="material-icons">error</i> ${message}`;
    
    // 添加動畫效果
    errorElement.style.animation = 'fadeIn 0.3s';
}

/**
 * 顯示欄位警告
 */
function showFieldWarning(input, message) {
    const formGroup = input.closest('.form-group');
    if (!formGroup) return;
    
    // 清除先前的狀態
    clearFieldStatus(formGroup);
    
    // 添加警告樣式
    input.classList.add('warning');
    formGroup.classList.add('has-warning');
    
    // 創建或更新警告訊息
    let warningElement = formGroup.querySelector('.warning-message');
    if (!warningElement) {
        warningElement = document.createElement('div');
        warningElement.className = 'warning-message';
        
        // 插入到 helper-text 之後
        const helperText = formGroup.querySelector('.helper-text');
        if (helperText) {
            helperText.insertAdjacentElement('afterend', warningElement);
        } else {
            formGroup.appendChild(warningElement);
        }
    }
    
    // 設置警告訊息
    warningElement.innerHTML = `<i class="material-icons">warning</i> ${message}`;
    
    // 添加動畫效果
    warningElement.style.animation = 'fadeIn 0.3s';
}

/**
 * 顯示欄位成功
 */
function showFieldSuccess(input) {
    const formGroup = input.closest('.form-group');
    if (!formGroup) return;
    
    // 清除先前的狀態
    clearFieldStatus(formGroup);
    
    // 添加成功樣式
    input.classList.add('success');
    formGroup.classList.add('has-success');
    
    // 可選：添加成功圖標
    const inputContainer = formGroup.querySelector('.input-container');
    if (inputContainer) {
        let successIcon = inputContainer.querySelector('.success-icon');
        if (!successIcon) {
            successIcon = document.createElement('i');
            successIcon.className = 'material-icons success-icon';
            successIcon.textContent = 'check_circle';
            inputContainer.appendChild(successIcon);
        }
    }
}

/**
 * 清除欄位錯誤
 */
function clearFieldError(input) {
    const formGroup = input.closest('.form-group');
    if (!formGroup) return;
    
    clearFieldStatus(formGroup);
}

/**
 * 清除欄位所有狀態
 */
function clearFieldStatus(formGroup) {
    // 移除輸入框狀態
    const input = formGroup.querySelector('.form-control');
    if (input) {
        input.classList.remove('error', 'warning', 'success');
    }
    
    // 移除表單組狀態
    formGroup.classList.remove('has-error', 'has-warning', 'has-success');
    
    // 移除錯誤訊息
    const errorMessage = formGroup.querySelector('.error-message');
    if (errorMessage) {
        errorMessage.remove();
    }
    
    // 移除警告訊息
    const warningMessage = formGroup.querySelector('.warning-message');
    if (warningMessage) {
        warningMessage.remove();
    }
    
    // 移除成功圖標
    const successIcon = formGroup.querySelector('.success-icon');
    if (successIcon) {
        successIcon.remove();
    }
}

/**
 * 顯示表單驗證錯誤
 */
function showValidationError(message) {
    // 檢查是否已有錯誤訊息
    let errorContainer = document.querySelector('.form-validation-error');
    
    // 如果沒有，創建一個
    if (!errorContainer) {
        errorContainer = document.createElement('div');
        errorContainer.className = 'form-validation-error';
        
        // 插入到表單開頭
        const form = document.querySelector('.itinerary-form');
        if (form) {
            form.insertAdjacentElement('afterbegin', errorContainer);
        }
    }
    
    // 設置錯誤訊息
    errorContainer.innerHTML = `
        <div class="alert alert-error">
            <i class="material-icons">error</i>
            <span>${message}</span>
        </div>
    `;
    
    // 滾動到錯誤訊息
    errorContainer.scrollIntoView({ behavior: 'smooth', block: 'center' });
    
    // 添加動畫效果
    errorContainer.style.animation = 'shake 0.5s';
    
    // 3秒後自動隱藏
    setTimeout(() => {
        errorContainer.style.animation = 'fadeOut 0.5s';
        setTimeout(() => {
            errorContainer.remove();
        }, 500);
    }, 5000);
}

/**
 * 自動儲存功能（可選）
 */
function enableAutoSave() {
    const form = document.querySelector('.itinerary-form');
    if (!form) return;
    
    const inputs = form.querySelectorAll('input, textarea, select');
    
    inputs.forEach(input => {
        input.addEventListener('input', debounce(function() {
            saveFormData();
        }, 1000));
    });
}

/**
 * 儲存表單資料到 localStorage
 */
function saveFormData() {
    const form = document.querySelector('.itinerary-form');
    if (!form) return;
    
    const formData = new FormData(form);
    const data = {};
    
    for (let [key, value] of formData.entries()) {
        data[key] = value;
    }
    
    localStorage.setItem('itinerary_form_draft', JSON.stringify(data));
    console.log('表單資料已自動儲存');
}

/**
 * 載入表單資料從 localStorage
 */
function loadFormData() {
    const savedData = localStorage.getItem('itinerary_form_draft');
    if (!savedData) return;
    
    try {
        const data = JSON.parse(savedData);
        const form = document.querySelector('.itinerary-form');
        if (!form) return;
        
        Object.keys(data).forEach(key => {
            const input = form.querySelector(`[name="${key}"]`);
            if (input) {
                input.value = data[key];
            }
        });
        
        console.log('已載入儲存的表單資料');
    } catch (e) {
        console.error('載入表單資料失敗:', e);
    }
}

/**
 * 清除儲存的表單資料
 */
function clearSavedFormData() {
    localStorage.removeItem('itinerary_form_draft');
}

/**
 * 防抖動函數
 */
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
} 

/**
 * 添加描述欄位的引導文字
 */
function addDescriptionPlaceholder() {
    const descInput = document.getElementById('itnDesc');
    if (descInput) {
        descInput.setAttribute('placeholder', '簡單介紹這趟行程的特色或亮點...');
    }
}

/**
 * 初始化提交按鈕狀態
 */
function initializeSubmitButton() {
    const form = document.querySelector('.itinerary-form');
    const submitBtn = document.getElementById('submitBtn');
    
    if (form && submitBtn) {
        // 監聽表單變化，更新提交按鈕狀態
        const formInputs = form.querySelectorAll('input, textarea, select');
        formInputs.forEach(input => {
            input.addEventListener('input', function() {
                updateSubmitButtonState();
            });
        });
        
        // 初始狀態檢查
        updateSubmitButtonState();
    }
}

/**
 * 更新提交按鈕狀態
 */
function updateSubmitButtonState() {
    const submitBtn = document.getElementById('submitBtn');
    if (!submitBtn) return;
    
    const nameInput = document.getElementById('itnName');
    const nameValid = nameInput && nameInput.value.trim().length >= 2;
    
    // 如果表單有效，啟用提交按鈕
    if (nameValid) {
        submitBtn.disabled = false;
        submitBtn.classList.remove('disabled');
    } else {
        submitBtn.disabled = true;
        submitBtn.classList.add('disabled');
    }
}

/**
 * 表單提交處理
 */
document.addEventListener('DOMContentLoaded', function() {
    const form = document.querySelector('.itinerary-form');
    const submitBtn = document.getElementById('submitBtn');
    
    if (form && submitBtn) {
        form.addEventListener('submit', function(e) {
            // 驗證表單
            if (!validateForm()) {
                e.preventDefault();
                showValidationError('請修正表單中的錯誤後再提交');
                
                // 滾動到第一個錯誤欄位
                const firstError = form.querySelector('.form-control.error');
                if (firstError) {
                    firstError.focus();
                    firstError.scrollIntoView({ behavior: 'smooth', block: 'center' });
                }
                return;
            }
            
            // 顯示載入狀態
            showLoadingState(submitBtn);
        });
    }
});

/**
 * 顯示載入狀態
 */
function showLoadingState(button) {
    if (!button) return;
    
    // 保存原始內容
    const originalContent = button.innerHTML;
    button.setAttribute('data-original-content', originalContent);
    
    // 設置載入狀態
    button.innerHTML = `
        <span class="spinner"></span>
        <span>處理中...</span>
    `;
    button.disabled = true;
    button.classList.add('loading');
    
    // 如果表單提交失敗或超時，恢復按鈕狀態
    setTimeout(() => {
        if (button.classList.contains('loading')) {
            button.innerHTML = button.getAttribute('data-original-content');
            button.disabled = false;
            button.classList.remove('loading');
        }
    }, 10000); // 10秒後超時
} 