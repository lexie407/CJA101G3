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
        nameInput.addEventListener('input', function() {
            validateItineraryName(this);
        });
        
        nameInput.addEventListener('blur', function() {
            validateItineraryName(this);
        });
    }
    
    // 即時驗證行程描述
    const descInput = document.getElementById('itnDesc');
    if (descInput) {
        descInput.addEventListener('input', function() {
            validateItineraryDescription(this);
        });
    }
    
    // 表單提交驗證
    form.addEventListener('submit', function(e) {
        if (!validateForm()) {
            e.preventDefault();
            showValidationError('請修正表單中的錯誤後再提交');
        }
    });
}

/**
 * 驗證行程名稱
 */
function validateItineraryName(input) {
    const value = input.value.trim();
    const minLength = 2;
    const maxLength = 255;
    
    clearFieldError(input);
    
    if (!value) {
        showFieldError(input, '行程名稱不能為空');
        return false;
    }
    
    if (value.length < minLength) {
        showFieldError(input, `行程名稱長度不能少於 ${minLength} 個字元`);
        return false;
    }
    
    if (value.length > maxLength) {
        showFieldError(input, `行程名稱長度不能超過 ${maxLength} 個字元`);
        return false;
    }
    
    showFieldSuccess(input);
    return true;
}

/**
 * 驗證行程描述
 */
function validateItineraryDescription(input) {
    const value = input.value.trim();
    const maxLength = 500;
    
    clearFieldError(input);
    
    if (value.length > maxLength) {
        showFieldError(input, `行程描述長度不能超過 ${maxLength} 個字元`);
        return false;
    }
    
    // 更新字數計數
    updateCharacterCount(input, value.length, maxLength);
    return true;
}

/**
 * 驗證整個表單
 */
function validateForm() {
    let isValid = true;
    
    // 驗證行程名稱
    const nameInput = document.getElementById('itnName');
    if (nameInput && !validateItineraryName(nameInput)) {
        isValid = false;
    }
    
    // 驗證行程描述
    const descInput = document.getElementById('itnDesc');
    if (descInput && !validateItineraryDescription(descInput)) {
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
    charCount.classList.remove('warning', 'danger');
    if (currentLength > maxLength * 0.8) {
        charCount.classList.add('warning');
    }
    if (currentLength > maxLength) {
        charCount.classList.add('danger');
    }
    
    counter.textContent = `${currentLength}/${maxLength}`;
    
    // 根據字數添加樣式
    if (currentLength > maxLength * 0.9) {
        counter.classList.add('warning');
    } else {
        counter.classList.remove('warning');
    }
    
    if (currentLength > maxLength) {
        counter.classList.add('error');
    } else {
        counter.classList.remove('error');
    }
}

/**
 * 初始化重置確認
 */
function initializeResetConfirmation() {
    const resetButtons = document.querySelectorAll('button[type="reset"], .btn-reset');
    
    resetButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            if (!confirm('確定要重置表單嗎？所有未儲存的變更將會遺失。')) {
                e.preventDefault();
            }
        });
    });
}

/**
 * 顯示欄位錯誤
 */
function showFieldError(input, message) {
    clearFieldError(input);
    
    input.classList.add('error');
    
    const errorElement = document.createElement('div');
    errorElement.className = 'field-error';
    errorElement.textContent = message;
    
    input.parentElement.appendChild(errorElement);
}

/**
 * 顯示欄位成功
 */
function showFieldSuccess(input) {
    clearFieldError(input);
    input.classList.add('success');
}

/**
 * 清除欄位錯誤
 */
function clearFieldError(input) {
    input.classList.remove('error', 'success');
    
    const existingError = input.parentElement.querySelector('.field-error');
    if (existingError) {
        existingError.remove();
    }
}

/**
 * 顯示驗證錯誤訊息
 */
function showValidationError(message) {
    // 移除現有的錯誤訊息
    const existingError = document.querySelector('.validation-error');
    if (existingError) {
        existingError.remove();
    }
    
    // 建立新的錯誤訊息
    const errorDiv = document.createElement('div');
    errorDiv.className = 'alert alert-error validation-error';
    errorDiv.innerHTML = `
        <span class="material-icons">error</span>
        <span>${message}</span>
    `;
    
    // 插入到表單頂部
    const form = document.querySelector('.itinerary-form');
    if (form) {
        form.insertBefore(errorDiv, form.firstChild);
        
        // 滾動到錯誤訊息
        errorDiv.scrollIntoView({ behavior: 'smooth', block: 'center' });
        
        // 3秒後自動移除
        setTimeout(() => {
            errorDiv.remove();
        }, 5000);
    }
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