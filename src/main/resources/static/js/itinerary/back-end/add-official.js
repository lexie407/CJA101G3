/**
 * 官方行程新增頁面腳本
 * 處理表單驗證、字數統計和狀態切換
 */
document.addEventListener('DOMContentLoaded', function() {
    // 表單元素
    const form = document.getElementById('itineraryForm');
    const nameInput = document.getElementById('itnName');
    const descTextarea = document.getElementById('itnDesc');
    const statusCheckbox = document.getElementById('statusCheckbox');
    const submitBtn = document.getElementById('submitBtn');
    const selectedSpotsContainer = document.getElementById('selectedSpotsContainer');
    
    // 字數統計元素
    const nameCharCount = nameInput.parentElement.nextElementSibling.querySelector('.char-count');
    const descCharCount = descTextarea.nextElementSibling.querySelector('.char-count');
    
    // 特殊字元檢查的正則表達式 (允許中英文、數字、常見標點符號)
    const specialCharsRegex = /[^\u4e00-\u9fa5a-zA-Z0-9\s.,;:!?()[\]{}'"，。；：！？（）【】「」『』、]/;
    
    // 敏感詞列表
    const sensitiveWords = ['色情', '賭博', '詐騙', '毒品', '違法', '黃色', '賭場', 'casino', 'porn', 'drug', 'illegal'];
    
    // 防抖計時器
    let nameValidationTimeout = null;
    let descValidationTimeout = null;
    
    /**
     * 字數統計功能
     * @param {HTMLElement} input - 輸入元素
     * @param {HTMLElement} countElement - 字數顯示元素
     * @param {number} maxLength - 最大字數
     */
    function updateCharCount(input, countElement, maxLength) {
        if (!countElement) return; // 安全檢查
        
        const currentLength = input.value.length;
        countElement.textContent = `(${currentLength}/${maxLength})`;
        
        // 根據字數變更樣式
        countElement.classList.remove('warning', 'error');
        if (currentLength >= maxLength * 0.9) {
            countElement.classList.add('error');
        } else if (currentLength >= maxLength * 0.7) {
            countElement.classList.add('warning');
        }
    }
    
    /**
     * 檢查是否包含特殊字元
     * @param {string} text - 要檢查的文字
     * @return {boolean} 是否包含特殊字元
     */
    function containsSpecialChars(text) {
        return specialCharsRegex.test(text);
    }
    
    /**
     * 檢查是否包含敏感詞
     * @param {string} text - 要檢查的文字
     * @return {string|null} 找到的敏感詞，如果沒有則返回null
     */
    function containsSensitiveWords(text) {
        const lowerText = text.toLowerCase();
        for (const word of sensitiveWords) {
            if (lowerText.includes(word.toLowerCase())) {
                return word;
            }
        }
        return null;
    }
    
    /**
     * 顯示驗證訊息
     */
    function showValidationMessage(element, message) {
        // 檢查是否已經存在驗證訊息元素
        let validationMessage;
        
        // 特殊處理行程描述的錯誤訊息
        if (element.id === 'itnDesc') {
            validationMessage = document.querySelector('#itnDesc + .validation-message');
            
            // 如果已經存在相同的錯誤訊息，則不重複顯示
            if (validationMessage && validationMessage.innerHTML === message) {
                return;
            }
            
            // 如果驗證訊息元素不存在，則創建一個
            if (!validationMessage) {
                validationMessage = document.createElement('div');
                validationMessage.className = 'validation-message';
                // 將驗證訊息插入到textarea後面
                element.insertAdjacentElement('afterend', validationMessage);
            }
        } else {
            // 行程名稱和其他輸入框的處理
            validationMessage = element.parentElement.querySelector('.validation-message');
            
            // 如果已經存在相同的錯誤訊息，則不重複顯示
            if (validationMessage && validationMessage.innerHTML === message) {
                return;
            }
            
            // 如果驗證訊息元素不存在，則創建一個
            if (!validationMessage) {
                validationMessage = document.createElement('div');
                validationMessage.className = 'validation-message';
                // 將驗證訊息插入到input-container後面
                element.parentElement.insertAdjacentElement('afterend', validationMessage);
            }
        }
        
        // 更新訊息內容
        validationMessage.innerHTML = message;
        validationMessage.classList.add('active');
    }
    
    /**
     * 隱藏驗證訊息
     */
    function hideValidationMessage(element) {
        let validationMessage;
        
        // 特殊處理行程描述的錯誤訊息
        if (element.id === 'itnDesc') {
            validationMessage = document.querySelector('#itnDesc + .validation-message');
        } else {
            validationMessage = element.parentElement.parentElement.querySelector('.validation-message');
        }
        
        if (validationMessage) {
            validationMessage.classList.remove('active');
            // 等動畫結束後移除元素
            setTimeout(() => {
                validationMessage.remove();
            }, 300);
        }
    }
    
    /**
     * 驗證行程名稱
     */
    function validateName(showError = true) {
        const name = nameInput.value.trim();
        const validationMessages = [];
        let isValid = true;
        
        // 清除先前的錯誤和樣式
        nameInput.classList.remove('invalid', 'valid');
        hideValidationMessage(nameInput);
        
        // 空值檢查
        if (name === '') {
            validationMessages.push('行程名稱不能為空');
            isValid = false;
        }
        // 長度檢查
        else if (name.length < 2) {
            validationMessages.push('行程名稱至少需要2個字元');
            isValid = false;
        }
        else if (name.length > 50) {
            validationMessages.push('行程名稱不能超過50個字元');
            isValid = false;
        }
        // 特殊字元檢查
        else if (containsSpecialChars(name)) {
            validationMessages.push('行程名稱包含不支援的特殊字元');
            isValid = false;
        }
        // 敏感詞檢查
        else {
            const sensitiveWord = containsSensitiveWords(name);
            if (sensitiveWord) {
                validationMessages.push(`行程名稱包含敏感詞: ${sensitiveWord}`);
                isValid = false;
            }
        }
        
        if (showError && !isValid && validationMessages.length > 0) {
            nameInput.classList.add('invalid');
            showValidationMessage(nameInput, validationMessages.join('<br>'));
        } else if (isValid) {
            nameInput.classList.add('valid');
        }
        
        return isValid;
    }
    
    /**
     * 驗證行程描述
     */
    function validateDesc(showError = true) {
        const desc = descTextarea.value.trim();
        const validationMessages = [];
        let isValid = true;
        
        // 清除先前的錯誤和樣式
        descTextarea.classList.remove('invalid', 'warning', 'valid');
        hideValidationMessage(descTextarea);
        
        // 空值檢查
        if (desc === '') {
            validationMessages.push('行程描述不能為空');
            isValid = false;
        }
        // 長度檢查
        else if (desc.length < 5) {
            validationMessages.push('行程描述至少需要5個字元');
            isValid = false;
        }
        else if (desc.length > 500) {
            validationMessages.push('行程描述不能超過500個字元');
            isValid = false;
        }
        
        // 敏感詞檢查
        const sensitiveWord = containsSensitiveWords(desc);
        if (sensitiveWord) {
            validationMessages.push(`行程描述包含可能敏感的詞: ${sensitiveWord}，請確認內容適合所有用戶`);
            // 這裡只是警告，不阻止提交
            if (showError) {
                descTextarea.classList.add('warning');
            }
        }
        
        if (showError && !isValid && validationMessages.length > 0) {
            descTextarea.classList.add('invalid');
            showValidationMessage(descTextarea, validationMessages.join('<br>'));
        } else if (isValid && !sensitiveWord) {
            descTextarea.classList.add('valid');
        }
        
        return isValid;
    }
    
    // 監聽輸入事件，更新字數統計和即時驗證（使用防抖）
    nameInput.addEventListener('input', function() {
        if (nameCharCount) {
            updateCharCount(this, nameCharCount, 50);
        }
        
        // 清除先前的計時器
        if (nameValidationTimeout) {
            clearTimeout(nameValidationTimeout);
        }
        
        // 設置新的計時器
        nameValidationTimeout = setTimeout(() => {
            validateName(true);
        }, 500);
    });
    
    // 監聽失去焦點事件
    nameInput.addEventListener('blur', function() {
        if (nameValidationTimeout) {
            clearTimeout(nameValidationTimeout);
        }
        validateName(true);
    });
    
    // 監聽獲得焦點事件 - 保持錯誤訊息不變
    nameInput.addEventListener('focus', function() {
        // 不做任何操作，保持錯誤訊息
    });
    
    descTextarea.addEventListener('input', function() {
        if (descCharCount) {
            updateCharCount(this, descCharCount, 500);
        }
        
        // 清除先前的計時器
        if (descValidationTimeout) {
            clearTimeout(descValidationTimeout);
        }
        
        // 設置新的計時器
        descValidationTimeout = setTimeout(() => {
            validateDesc(true);
        }, 500);
    });
    
    // 監聽失去焦點事件
    descTextarea.addEventListener('blur', function() {
        if (descValidationTimeout) {
            clearTimeout(descValidationTimeout);
        }
        validateDesc(true);
    });
    
    // 監聽獲得焦點事件 - 保持錯誤訊息不變
    descTextarea.addEventListener('focus', function() {
        // 不做任何操作，保持錯誤訊息
    });
    
    // 初始化字數統計
    if (nameCharCount) {
        updateCharCount(nameInput, nameCharCount, 50);
    }
    if (descCharCount) {
        updateCharCount(descTextarea, descCharCount, 500);
    }
    
    // 公開狀態切換
    statusCheckbox.addEventListener('change', function() {
        // 設置 checkbox 的值
        this.value = this.checked ? '1' : '0';
        
        const statusText = this.parentElement.nextElementSibling.querySelector('.status-text');
        const statusBadge = this.parentElement.nextElementSibling.querySelector('.status-badge');
        
        if (this.checked) {
            statusText.textContent = '公開';
            statusBadge.classList.remove('badge-warning');
            statusBadge.classList.add('badge-success');
            statusBadge.querySelector('i').textContent = 'visibility';
        } else {
            statusText.textContent = '不公開';
            statusBadge.classList.remove('badge-success');
            statusBadge.classList.add('badge-warning');
            statusBadge.querySelector('i').textContent = 'visibility_off';
        }
    });
    
    // 表單提交前驗證
    form.addEventListener('submit', function(event) {
        event.preventDefault(); // 防止表單直接提交
        
        let isValid = true;
        
        // 驗證行程名稱
        if (!validateName(true)) {
            isValid = false;
            nameInput.classList.add('shake');
            setTimeout(() => nameInput.classList.remove('shake'), 500);
        }
        
        // 驗證行程描述
        if (!validateDesc(true)) {
            isValid = false;
            descTextarea.classList.add('shake');
            setTimeout(() => descTextarea.classList.remove('shake'), 500);
        }
        
        // 檢查是否選擇了景點
        if (window.validateSpotSelection) {
            const spotValid = window.validateSpotSelection(true);
            if (!spotValid) {
                isValid = false;
            }
        }
        
        if (isValid) {
            // 如果驗證通過，提交表單
            form.submit();
        }
    });
    
    /**
     * 顯示表單總體錯誤訊息
     * @param {string} message - 錯誤訊息
     */
    function showFormError(message) {
        // 檢查是否已有錯誤訊息
        let errorAlert = document.querySelector('.alert.alert-error');
        
        if (!errorAlert) {
            // 創建錯誤訊息元素
            errorAlert = document.createElement('div');
            errorAlert.className = 'alert alert-error';
            errorAlert.innerHTML = `<i class="material-icons">error</i><span>${message}</span>`;
            
            // 插入到表單前面
            const contentContainer = document.querySelector('.content-container');
            if (contentContainer) {
                contentContainer.insertBefore(errorAlert, contentContainer.firstChild);
            }
        } else {
            // 更新現有錯誤訊息
            errorAlert.querySelector('span').textContent = message;
        }
        
        // 添加動畫效果
        errorAlert.classList.add('shake');
        setTimeout(() => errorAlert.classList.remove('shake'), 500);
        
        // 滾動到錯誤訊息
        errorAlert.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
    
    // 確保按鈕顯示正常
    const formActions = document.querySelector('.form-actions');
    const buttons = document.querySelectorAll('.btn');
    
    if (formActions) formActions.style.display = 'flex';
    buttons.forEach(btn => btn.style.display = 'inline-flex');
    
    // 動態時間更新功能
    function updateCurrentTime() {
        const currentTimeElement = document.getElementById('current-time');
        if (currentTimeElement) {
            const now = new Date();
            const timeString = now.getFullYear() + '/' + 
                              String(now.getMonth() + 1).padStart(2, '0') + '/' + 
                              String(now.getDate()).padStart(2, '0') + ' ' + 
                              String(now.getHours()).padStart(2, '0') + ':' + 
                              String(now.getMinutes()).padStart(2, '0');
            currentTimeElement.textContent = timeString;
        }
    }

    // 初始化時間顯示
    updateCurrentTime();
    
    // 每分鐘更新時間
    setInterval(updateCurrentTime, 60000);

    console.log('官方行程新增頁面載入完成');
}); 