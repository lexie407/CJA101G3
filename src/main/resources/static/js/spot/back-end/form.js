/**
 * 景點表單頁面 JavaScript
 * 符合 Material Design 3 設計規範
 */

// 自定義 Toast 通知函數
function showToast(message, type = 'success') {
    console.log('=== Toast 顯示 ===');
    console.log('訊息:', message);
    console.log('類型:', type);
    
    // 創建或獲取 toast 容器
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        container.style.cssText = `
            position: fixed;
            top: 80px;
            right: 24px;
            z-index: 9999;
            pointer-events: none;
        `;
        document.body.appendChild(container);
    }
    
    // 創建 toast 元素
    const toast = document.createElement('div');
    const colors = {
        success: { bg: 'var(--md-sys-color-primary)', text: 'var(--md-sys-color-on-primary)' },
        error: { bg: 'var(--md-sys-color-error)', text: 'var(--md-sys-color-on-error)' },
        warning: { bg: 'var(--md-sys-color-secondary)', text: 'var(--md-sys-color-on-secondary)' }
    };
    
    const color = colors[type] || colors.success;
    
    toast.style.cssText = `
        position: relative;
        min-width: 320px;
        max-width: 480px;
        padding: 16px 20px;
        margin-bottom: 12px;
        background-color: ${color.bg};
        color: ${color.text};
        border-radius: 8px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        opacity: 0;
        transform: translateX(100%);
        transition: all 0.3s ease;
        pointer-events: auto;
        font-size: 14px;
        font-weight: 500;
        line-height: 1.5;
        display: flex;
        align-items: center;
        gap: 12px;
    `;
    
    // 添加圖標
    const icon = document.createElement('i');
    icon.className = 'material-icons';
    icon.style.fontSize = '20px';
    icon.textContent = type === 'success' ? 'check_circle' : 
                      type === 'error' ? 'error' : 'warning';
    
    // 添加訊息文字
    const messageSpan = document.createElement('span');
    messageSpan.innerHTML = message.replace(/\n/g, '<br>');
    
    toast.appendChild(icon);
    toast.appendChild(messageSpan);
    container.appendChild(toast);
    
    // 動畫顯示
    setTimeout(() => {
        toast.style.opacity = '1';
        toast.style.transform = 'translateX(0)';
    }, 50);
    
    // 自動隱藏
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        setTimeout(() => {
            if (toast.parentNode) {
                toast.parentNode.removeChild(toast);
            }
        }, 300);
    }, 4000);
}





// 表單驗證函數
function validateForm() {
    const spotName = document.getElementById('spotName');
    const spotLoc = document.getElementById('spotLoc');
    const spotDesc = document.getElementById('spotDesc');
    
    // 驗證景點名稱
    if (!spotName.value.trim()) {
        showToast('請輸入景點名稱', 'error');
        spotName.focus();
        return false;
    }
    
    if (spotName.value.trim().length > 50) {
        showToast('景點名稱不能超過50個字元', 'error');
        spotName.focus();
        return false;
    }
    
    // 驗證景點地址
    if (!spotLoc.value.trim()) {
        showToast('請輸入景點地址', 'error');
        spotLoc.focus();
        return false;
    }
    
    if (spotLoc.value.trim().length > 100) {
        showToast('景點地址不能超過100個字元', 'error');
        spotLoc.focus();
        return false;
    }
    
    // 驗證景點描述
    if (spotDesc.value.trim().length > 500) {
        showToast('景點描述不能超過500個字元', 'error');
        spotDesc.focus();
        return false;
    }
    
    return true;
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
        { id: 'spotName', max: 50 },
        { id: 'spotLoc', max: 100 },
        { id: 'spotDesc', max: 500 }
    ];
    
    fields.forEach(field => {
        const input = document.getElementById(field.id);
        const helperText = input?.parentElement?.querySelector('.helper-text');
        const charCount = helperText?.querySelector('.char-count');
        
        if (input && helperText && charCount) {
            function updateCounter() {
                const length = input.value.length;
                const remaining = field.max - length;
                const percentage = (length / field.max) * 100;
                
                // 更新字數顯示
                charCount.textContent = `(${length}/${field.max})`;
                
                // 根據使用率改變顏色
                if (percentage > 90) {
                    charCount.style.color = 'var(--md-sys-color-error)';
                    if (percentage === 100) {
                        charCount.textContent = `(${length}/${field.max}) ⚠️`;
                    }
                } else if (percentage > 75) {
                    charCount.style.color = 'var(--md-sys-color-secondary)';
                } else {
                    charCount.style.color = 'var(--md-sys-color-primary)';
                }
                

                
                // 添加輸入動畫效果
                if (input.classList.contains('error')) {
                    input.classList.remove('error');
                }
                
                if (length > field.max) {
                    input.classList.add('error');
                    input.style.borderColor = 'var(--md-sys-color-error)';
                } else {
                    input.style.borderColor = '';
                }
            }
            
            input.addEventListener('input', updateCounter);
            input.addEventListener('paste', () => setTimeout(updateCounter, 10));
            updateCounter(); // 初始化
        }
    });
}

// 表單提交處理
function setupFormSubmission() {
    const form = document.getElementById('spotForm');
    if (!form) return;
    
    form.addEventListener('submit', function(e) {
        e.preventDefault();
        
        console.log('表單提交驗證開始');
        
        if (!validateForm()) {
            return;
        }
        
        // 顯示提交中狀態
        const submitBtn = form.querySelector('#submitBtn');
        const originalText = submitBtn.innerHTML;
        
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<i class="material-icons">hourglass_empty</i>處理中...';
        submitBtn.style.cursor = 'not-allowed';
        
        // 添加提交動畫
        submitBtn.style.transform = 'scale(0.98)';
        
        // 顯示提交中的Toast
        showToast('正在提交景點資訊...', 'warning');
        
        // 提交表單
        setTimeout(() => {
            console.log('表單驗證通過，正在提交...');
            this.submit();
        }, 800);
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

// 初始化
document.addEventListener('DOMContentLoaded', function() {
    console.log('景點表單頁面初始化');
    
    // 初始化各項功能
    updateStatusSwitch();
    setupCharacterCounters();
    setupFormSubmission();
    setupInputEffects();
    setupKeyboardShortcuts();
    setupAutoSave();
    showBackendMessages();
    

    
    
    console.log('景點表單頁面初始化完成');
}); 