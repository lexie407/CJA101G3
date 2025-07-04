/**
 * API匯入頁面 JavaScript - 符合 Material Design 3
 * 整合政府觀光資料開放平臺景點資料匯入功能
 */

document.addEventListener('DOMContentLoaded', function() {
    console.log('API匯入頁面初始化開始');
    
    // 初始化頁面
    initializePage();
    
    // 綁定事件監聽器
    bindEventListeners();
    
    console.log('API匯入頁面初始化完成');
});

/**
 * 初始化頁面
 */
function initializePage() {
    // 檢查必要元素是否存在
    const requiredElements = [
        'test-button',
        'import-button',
        'import-count',
        'city-count'
    ];
    
    requiredElements.forEach(id => {
        const element = document.getElementById(id);
        if (!element) {
            console.warn(`找不到必要元素: ${id}`);
        }
    });
    
    // 頁面載入動畫
    setTimeout(() => {
        document.querySelectorAll('.api-section').forEach((section, index) => {
            section.style.animationDelay = `${index * 0.1}s`;
        });
    }, 100);
}

/**
 * 綁定事件監聽器
 */
function bindEventListeners() {
    // API連線測試按鈕
    const testButton = document.getElementById('test-button');
    if (testButton) {
        testButton.addEventListener('click', handleApiTest);
    }
    
    // 全台景點匯入按鈕
    const importButton = document.getElementById('import-button');
    if (importButton) {
        importButton.addEventListener('click', handleImportAllSpots);
    }
    
    // 縣市匯入按鈕
    document.querySelectorAll('.city-btn').forEach(button => {
        button.addEventListener('click', handleCityImport);
    });
    
    // 輸入框驗證
    const importCountInput = document.getElementById('import-count');
    if (importCountInput) {
        importCountInput.addEventListener('input', validateImportCount);
    }
    
    const cityCountInput = document.getElementById('city-count');
    if (cityCountInput) {
        cityCountInput.addEventListener('input', validateCityCount);
    }
}

/**
 * 處理API連線測試
 */
async function handleApiTest() {
    console.log('開始API連線測試');
    
    try {
        setLoadingState('test', true);
        hideResult('test');
        
        const response = await fetch('/admin/spot/api/test-api', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        if (!response.ok) {
            throw new Error(`HTTP錯誤: ${response.status} ${response.statusText}`);
        }
        
        const data = await response.json();
        console.log('API測試成功:', data);
        
        showResult('test', data, 'success');
        showToast('API連線測試成功！', 'success');
        
    } catch (error) {
        console.error('API測試失敗:', error);
        
        const errorData = {
            error: '連線測試失敗',
            message: error.message,
            timestamp: new Date().toLocaleString()
        };
        
        showResult('test', errorData, 'error');
        showToast('API連線測試失敗，請檢查網路連線', 'error');
        
    } finally {
        setLoadingState('test', false);
    }
}

/**
 * 處理全台景點匯入
 */
async function handleImportAllSpots() {
    console.log('開始全台景點匯入');
    
    const countInput = document.getElementById('import-count');
    const count = countInput ? parseInt(countInput.value) : 50;
    
    if (!validateImportCount()) {
        return;
    }
    
    // 確認對話框
    if (!confirm(`確定要匯入 ${count} 筆全台景點資料嗎？\n此操作可能需要數分鐘時間。`)) {
        return;
    }
    
    try {
        setLoadingState('import', true);
        hideResult('import');
        
        const response = await fetch(`/admin/spot/api/import-spots?limit=${count}`, {
            method: 'POST',
            headers: getCsrfHeaders()
        });
        
        if (!response.ok) {
            throw new Error(`HTTP錯誤: ${response.status} ${response.statusText}`);
        }
        
        const data = await response.json();
        console.log('景點匯入成功:', data);
        
        showResult('import', data, 'success');
        // 正確顯示匯入筆數
        const importedCount = data.data?.successCount || data.data?.imported || 0;
        showToast(`成功匯入 ${importedCount} 筆景點資料！`, 'success');
        
    } catch (error) {
        console.error('景點匯入失敗:', error);
        
        const errorData = {
            error: '景點匯入失敗',
            message: error.message,
            timestamp: new Date().toLocaleString()
        };
        
        showResult('import', errorData, 'error');
        showToast('景點匯入失敗，請稍後再試', 'error');
        
    } finally {
        setLoadingState('import', false);
    }
}

/**
 * 處理縣市景點匯入
 */
async function handleCityImport(event) {
    const button = event.currentTarget;
    const city = button.getAttribute('data-city');
    const cityName = button.querySelector('span').textContent;
    
    console.log(`開始匯入 ${cityName} 景點`);
    
    const countInput = document.getElementById('city-count');
    const count = countInput ? parseInt(countInput.value) : 30;
    
    if (!validateCityCount()) {
        return;
    }
    
    // 確認對話框
    if (!confirm(`確定要匯入 ${count} 筆 ${cityName} 景點資料嗎？`)) {
        return;
    }
    
    try {
        setLoadingState('city', true);
        hideResult('city');
        
        const response = await fetch(`/admin/spot/api/import-spots-by-city?city=${city}&limit=${count}`, {
            method: 'POST',
            headers: getCsrfHeaders()
        });
        
        if (!response.ok) {
            throw new Error(`HTTP錯誤: ${response.status} ${response.statusText}`);
        }
        
        const data = await response.json();
        console.log(`${cityName} 景點匯入成功:`, data);
        
        showResult('city', data, 'success');
        // 正確顯示匯入筆數
        const importedCount = data.data?.successCount || data.data?.imported || 0;
        showToast(`成功匯入 ${importedCount} 筆 ${cityName} 景點資料！`, 'success');
        
    } catch (error) {
        console.error(`${cityName} 景點匯入失敗:`, error);
        
        const errorData = {
            error: `${cityName} 景點匯入失敗`,
            message: error.message,
            timestamp: new Date().toLocaleString()
        };
        
        showResult('city', errorData, 'error');
        showToast(`${cityName} 景點匯入失敗，請稍後再試`, 'error');
        
    } finally {
        setLoadingState('city', false);
    }
}

/**
 * 驗證匯入筆數
 */
function validateImportCount() {
    const input = document.getElementById('import-count');
    if (!input) return false;
    
    const value = parseInt(input.value);
    const min = parseInt(input.getAttribute('min')) || 10;
    const max = parseInt(input.getAttribute('max')) || 200;
    
    if (isNaN(value) || value < min || value > max) {
        input.style.borderColor = 'var(--md-sys-color-error)';
        showToast(`請輸入有效的匯入筆數 (${min}-${max})`, 'error');
        return false;
    }
    
    input.style.borderColor = '';
    return true;
}

/**
 * 驗證縣市匯入筆數
 */
function validateCityCount() {
    const input = document.getElementById('city-count');
    if (!input) return false;
    
    const value = parseInt(input.value);
    const min = parseInt(input.getAttribute('min')) || 10;
    const max = parseInt(input.getAttribute('max')) || 100;
    
    if (isNaN(value) || value < min || value > max) {
        input.style.borderColor = 'var(--md-sys-color-error)';
        showToast(`請輸入有效的匯入筆數 (${min}-${max})`, 'error');
        return false;
    }
    
    input.style.borderColor = '';
    return true;
}

/**
 * 設定載入狀態
 */
function setLoadingState(type, isLoading) {
    const spinner = document.getElementById(`${type}-spinner`);
    
    // 設定按鈕狀態
    let buttons = [];
    if (type === 'city') {
        buttons = document.querySelectorAll('.city-btn');
    } else {
        const button = document.getElementById(`${type}-button`);
        if (button) buttons = [button];
    }
    
    // 更新按鈕狀態
    buttons.forEach(button => {
        button.disabled = isLoading;
        if (isLoading) {
            button.style.opacity = '0.6';
            button.style.cursor = 'not-allowed';
        } else {
            button.style.opacity = '';
            button.style.cursor = '';
        }
    });
    
    // 顯示/隱藏載入動畫
    if (spinner) {
        spinner.style.display = isLoading ? 'block' : 'none';
    }
}

/**
 * 顯示結果
 */
function showResult(type, data, resultType = 'info') {
    const resultArea = document.getElementById(`${type}-result`);
    if (!resultArea) return;
    
    const resultContent = resultArea.querySelector('.result-content');
    if (!resultContent) return;
    
    // 格式化數據
    let formattedData;
    if (typeof data === 'object') {
        // 如果是匯入結果，特別處理
        if (data.data && typeof data.data === 'object' && 
            (data.data.hasOwnProperty('successCount') || data.data.hasOwnProperty('imported'))) {
            const result = data.data;
            
            // 處理全台匯入結果
            if (result.hasOwnProperty('successCount')) {
                formattedData = `
📊 匯入結果統計：
✅ 成功匯入：${result.successCount || 0} 筆
⏭️ 跳過重複：${result.skippedCount || 0} 筆
❌ 匯入失敗：${result.errorCount || 0} 筆
📝 總處理筆數：${(result.successCount || 0) + (result.skippedCount || 0) + (result.errorCount || 0)} 筆

${data.message || '匯入完成'}
`;
            }
            // 處理其他格式的匯入結果
            else if (result.hasOwnProperty('imported')) {
                formattedData = `
📊 匯入結果：
✅ 成功匯入：${result.imported || 0} 筆
${data.message || '匯入完成'}
`;
            }
            else {
                formattedData = JSON.stringify(data, null, 2);
            }
        }
        // 處理測試連線等其他結果
        else {
            formattedData = JSON.stringify(data, null, 2);
        }
    } else {
        formattedData = String(data);
    }
    
    // 設定內容
    resultContent.innerHTML = `<pre style="white-space: pre-wrap; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;">${formattedData}</pre>`;
    
    // 設定樣式
    resultArea.style.display = 'block';
    
    // 根據結果類型設定顏色
    if (resultType === 'success') {
        resultArea.style.borderLeftColor = 'var(--md-sys-color-primary)';
    } else if (resultType === 'error') {
        resultArea.style.borderLeftColor = 'var(--md-sys-color-error)';
    }
    
    // 滾動到結果區域
    resultArea.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

/**
 * 隱藏結果
 */
function hideResult(type) {
    const resultArea = document.getElementById(`${type}-result`);
    if (resultArea) {
        resultArea.style.display = 'none';
    }
}

/**
 * 顯示Toast通知
 */
function showToast(message, type = 'info') {
    // 移除現有的toast
    const existingToast = document.querySelector('.toast');
    if (existingToast) {
        existingToast.remove();
    }
    
    // 創建toast元素
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    
    // 設定圖標
    let icon = 'info';
    if (type === 'success') icon = 'check_circle';
    else if (type === 'error') icon = 'error';
    else if (type === 'warning') icon = 'warning';
    
    toast.innerHTML = `
        <i class="material-icons">${icon}</i>
        <span>${message}</span>
    `;
    
    // 添加樣式
    Object.assign(toast.style, {
        position: 'fixed',
        top: '20px',
        right: '20px',
        padding: '12px 16px',
        borderRadius: '8px',
        color: 'white',
        fontSize: '14px',
        fontWeight: '500',
        display: 'flex',
        alignItems: 'center',
        gap: '8px',
        zIndex: '10000',
        minWidth: '300px',
        boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
        animation: 'slideInRight 0.3s ease-out'
    });
    
    // 設定顏色
    if (type === 'success') {
        toast.style.background = 'linear-gradient(135deg, #10b981, #059669)';
    } else if (type === 'error') {
        toast.style.background = 'linear-gradient(135deg, #ef4444, #dc2626)';
    } else if (type === 'warning') {
        toast.style.background = 'linear-gradient(135deg, #f59e0b, #d97706)';
    } else {
        toast.style.background = 'linear-gradient(135deg, #3b82f6, #2563eb)';
    }
    
    // 添加到頁面
    document.body.appendChild(toast);
    
    // 自動移除
    setTimeout(() => {
        if (toast.parentNode) {
            toast.style.animation = 'slideOutRight 0.3s ease-in';
            setTimeout(() => {
                if (toast.parentNode) {
                    toast.remove();
                }
            }, 300);
        }
    }, 4000);
}

/**
 * 獲取CSRF標頭
 */
function getCsrfHeaders() {
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
    
    const headers = {
        'Content-Type': 'application/json'
    };
    
    if (csrfToken && csrfHeader) {
        headers[csrfHeader] = csrfToken;
    }
    
    return headers;
}

// 添加CSS動畫
const style = document.createElement('style');
style.textContent = `
    @keyframes slideInRight {
        from {
            transform: translateX(100%);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }
    
    @keyframes slideOutRight {
        from {
            transform: translateX(0);
            opacity: 1;
        }
        to {
            transform: translateX(100%);
            opacity: 0;
        }
    }
    
    .toast .material-icons {
        font-size: 18px !important;
    }
`;
document.head.appendChild(style); 