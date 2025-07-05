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
    
    /**
     * 字數統計功能
     * @param {HTMLElement} input - 輸入元素
     * @param {HTMLElement} countElement - 字數顯示元素
     * @param {number} maxLength - 最大字數
     */
    function updateCharCount(input, countElement, maxLength) {
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
    
    // 監聽輸入事件，更新字數統計
    nameInput.addEventListener('input', function() {
        updateCharCount(this, nameCharCount, 50);
        
        // 檢查特殊字元
        if (containsSpecialChars(this.value)) {
            showValidationMessage(this, '行程名稱包含不支援的特殊字元');
            this.classList.add('invalid');
        } else {
            hideValidationMessage(this);
            this.classList.remove('invalid');
        }
    });
    
    descTextarea.addEventListener('input', function() {
        updateCharCount(this, descCharCount, 500);
    });
    
    // 初始化字數統計
    updateCharCount(nameInput, nameCharCount, 50);
    updateCharCount(descTextarea, descCharCount, 500);
    
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
        let isValid = true;
        
        // 驗證行程名稱
        if (nameInput.value.trim().length < 2) {
            nameInput.classList.add('invalid');
            isValid = false;
            showValidationMessage(nameInput, '行程名稱至少需要2個字元');
        } else if (nameInput.value.trim().length > 50) {
            nameInput.classList.add('invalid');
            isValid = false;
            showValidationMessage(nameInput, '行程名稱不能超過50個字元');
        } else if (containsSpecialChars(nameInput.value)) {
            nameInput.classList.add('invalid');
            isValid = false;
            showValidationMessage(nameInput, '行程名稱包含不支援的特殊字元');
        } else if (nameInput.value.trim() === '') {
            nameInput.classList.add('invalid');
            isValid = false;
            showValidationMessage(nameInput, '行程名稱不能為空');
        } else {
            nameInput.classList.remove('invalid');
            hideValidationMessage(nameInput);
        }
        
        // 驗證行程描述
        if (descTextarea.value.trim().length < 10) {
            descTextarea.classList.add('invalid');
            isValid = false;
            showValidationMessage(descTextarea, '行程描述至少需要10個字元');
        } else if (descTextarea.value.trim().length > 500) {
            descTextarea.classList.add('invalid');
            isValid = false;
            showValidationMessage(descTextarea, '行程描述不能超過500個字元');
        } else if (descTextarea.value.trim() === '') {
            descTextarea.classList.add('invalid');
            isValid = false;
            showValidationMessage(descTextarea, '行程描述不能為空');
        } else {
            descTextarea.classList.remove('invalid');
            hideValidationMessage(descTextarea);
        }
        
        // 檢查是否選擇了景點
        const spotSelectedList = document.getElementById('spotSelectedList');
        const spotCards = spotSelectedList.querySelectorAll('.spot-card');
        if (spotCards.length === 0) {
            // 顯示警告，但不阻止提交
            const spotSelector = document.querySelector('.spot-selector-section');
            if (spotSelector) {
                showValidationMessage(spotSelector, '建議選擇至少一個景點，但不是必須的');
                setTimeout(() => {
                    hideValidationMessage(spotSelector);
                }, 3000);
            }
        }
        
        if (!isValid) {
            event.preventDefault();
            // 滾動到第一個錯誤
            const firstInvalid = document.querySelector('.invalid');
            if (firstInvalid) {
                firstInvalid.scrollIntoView({ behavior: 'smooth', block: 'center' });
                firstInvalid.focus();
            }
        } else {
            // 禁用提交按鈕，防止重複提交
            submitBtn.disabled = true;
            submitBtn.innerHTML = '<i class="material-icons">hourglass_empty</i><span>處理中...</span>';
        }
    });
    
    /**
     * 顯示驗證錯誤訊息
     * @param {HTMLElement} element - 輸入元素
     * @param {string} message - 錯誤訊息
     */
    function showValidationMessage(element, message) {
        let validationMessage = element.parentElement.querySelector('.validation-message');
        
        if (!validationMessage) {
            validationMessage = document.createElement('div');
            validationMessage.className = 'validation-message';
            if (element.parentElement.classList.contains('input-container')) {
                element.parentElement.after(validationMessage);
            } else {
                element.after(validationMessage);
            }
        }
        
        validationMessage.textContent = message;
        validationMessage.classList.add('active');
    }
    
    /**
     * 隱藏驗證錯誤訊息
     * @param {HTMLElement} element - 輸入元素
     */
    function hideValidationMessage(element) {
        const validationMessage = element.parentElement.querySelector('.validation-message') || 
                                 element.nextElementSibling;
        if (validationMessage && validationMessage.classList.contains('validation-message')) {
            validationMessage.classList.remove('active');
        }
    }
    
    // 確保按鈕顯示正常
    const formActions = document.querySelector('.form-actions');
    const buttons = document.querySelectorAll('.btn');
    
    if (formActions) formActions.style.display = 'flex';
    buttons.forEach(btn => btn.style.display = 'inline-flex');
    
    console.log('官方行程新增頁面載入完成');
}); 