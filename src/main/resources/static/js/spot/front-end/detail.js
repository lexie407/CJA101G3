/* =================
   景點詳情頁 JavaScript
   依賴基礎層功能
   ================= */

// 全域變數
let spotData = null;
let map = null;
let infoWindow = null;
let isFavorited = false;
let isBookmarked = false;

// DOM 載入完成後初始化
document.addEventListener('DOMContentLoaded', function() {
    console.log('🚀 景點詳情頁初始化開始');
    
    // 初始化景點資料
    initSpotData();
    
    // 初始化其他功能
    initToastSystem();
    initFavoriteSystem();
    initBookmarkSystem();
    
    // 初始化地圖
    if (spotData) {
        initializeGoogleMaps();
    }
    
    console.log('✅ 景點詳情頁初始化完成');
});

// ========== 1. 資料初始化 ==========

/**
 * 初始化景點資料
 */
function initSpotData() {
    const dataContainer = document.getElementById('spot-data-container');
    if (!dataContainer) {
        console.warn('⚠️ 找不到景點資料容器');
        return;
    }
    
    spotData = {
        spotId: dataContainer.dataset.spotId,
        spotLat: dataContainer.dataset.spotLat,
        spotLng: dataContainer.dataset.spotLng,
        spotName: dataContainer.dataset.spotName,
        spotAddr: dataContainer.dataset.spotAddr,
        isFavorited: dataContainer.dataset.isFavorited === 'true'
    };
    
    isFavorited = spotData.isFavorited;
    
    console.log('📍 景點資料載入:', spotData);
}

// ========== 2. 地圖功能 ==========

/**
 * 初始化 Google Maps
 */
function initializeGoogleMaps() {
    console.log('🗺️ 開始載入 Google Maps 配置...');
    
    // 從伺服器獲取 API Key
    fetch('/api/spot/v2/maps/config')
        .then(response => {
            if (!response.ok) {
                throw new Error('無法取得 Google Maps API 配置');
            }
            return response.json();
        })
        .then(config => {
            if (!config.apiKey) {
                throw new Error('Google Maps API Key 未設定');
            }
            
            // 儲存配置以供後續使用
            window.googleMapsConfig = config;
            
            // 直接初始化嵌入式地圖
            initializeEmbedMap();
        })
        .catch(error => {
            console.error('❌ 獲取 Google Maps API 配置失敗:', error);
            showMapError('無法載入 Google Maps 配置，請檢查網路連接或重新整理頁面');
        });
}

/**
 * 初始化嵌入式地圖
 */
function initializeEmbedMap() {
    console.log('🗺️ 開始初始化嵌入式地圖...');
    
    try {
        // 檢查地圖容器是否存在
        const mapContainer = document.getElementById('map');
        if (!mapContainer) {
            throw new Error('找不到地圖容器元素');
        }

        // 確保地圖容器有正確的尺寸
        mapContainer.style.height = '400px';
        mapContainer.style.width = '100%';

        // 檢查並處理座標
        let lat = parseFloat(spotData.spotLat);
        let lng = parseFloat(spotData.spotLng);
        
        // 檢查座標是否有效
        const hasValidCoords = lat && lng && 
                             !isNaN(lat) && !isNaN(lng) && 
                             lat >= -90 && lat <= 90 && lng >= -180 && lng <= 180;
        
        // 準備地圖 URL 參數
        const spotName = encodeURIComponent(spotData.spotName);
        let mapUrl;
        
        if (hasValidCoords) {
            // 如果有有效座標，使用精確位置
            mapUrl = `https://www.google.com/maps/embed/v1/place?key=${window.googleMapsConfig.apiKey}&q=${spotName}&center=${lat},${lng}&zoom=16`;
            
            // 更新地圖資訊顯示
            updateMapInfo(lat, lng);
        } else {
            // 如果沒有有效座標，只使用景點名稱搜尋
            mapUrl = `https://www.google.com/maps/embed/v1/search?key=${window.googleMapsConfig.apiKey}&q=${spotName}`;
            
            // 更新地圖資訊顯示為警告
            showMapWarning();
        }
        
        // 使用嵌入式 iframe 顯示 Google Maps
        mapContainer.innerHTML = `
            <iframe 
                width="100%" 
                height="400" 
                frameborder="0" 
                style="border:0; border-radius: 8px;" 
                src="${mapUrl}" 
                allowfullscreen>
            </iframe>
        `;
        
        console.log('✅ 嵌入式地圖初始化完成');
    } catch (error) {
        console.error('❌ 地圖初始化失敗:', error);
        showMapError(error.message);
    }
}

/**
 * 更新地圖資訊顯示
 */
function updateMapInfo(lat, lng) {
    const mapInfo = document.querySelector('.spot-detail-map-info');
    if (mapInfo) {
        mapInfo.innerHTML = `
            <div class="spot-detail-map-status spot-detail-map-status--success">
                <span class="material-icons">location_on</span>
                <span>座標：${lat.toFixed(6)}, ${lng.toFixed(6)}</span>
                <a href="https://www.google.com/maps/search/${encodeURIComponent(spotData.spotName)}/@${lat},${lng},17z" 
                   target="_blank" 
                   class="btn btn-sm btn-outline-primary mt-2">
                    <span class="material-icons">open_in_new</span>
                    在 Google 地圖中開啟
                </a>
            </div>
        `;
    }
}

/**
 * 顯示地圖警告
 */
function showMapWarning() {
    const mapInfo = document.querySelector('.spot-detail-map-info');
    if (mapInfo) {
        mapInfo.innerHTML = `
            <div class="spot-detail-map-status spot-detail-map-status--warning">
                <span class="material-icons">info</span>
                <span>此景點尚未設定精確座標，顯示的位置可能不準確</span>
                <a href="https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(spotData.spotName)}" 
                   target="_blank" 
                   class="btn btn-sm btn-outline-primary mt-2">
                    <span class="material-icons">map</span>
                    在 Google 地圖中搜尋
                </a>
            </div>
        `;
    }
}

/**
 * 顯示地圖錯誤
 */
function showMapError(message) {
    const mapContainer = document.getElementById('map');
    if (mapContainer) {
        mapContainer.innerHTML = `
            <div class="spot-detail-map-placeholder">
                <div class="spot-detail-map-placeholder__content">
                    <span class="material-icons spot-detail-map-placeholder__icon">error</span>
                    <p class="spot-detail-map-placeholder__text">${message || '載入地圖時發生錯誤'}</p>
                    <button class="btn btn-primary mt-3" onclick="initializeGoogleMaps()">
                        <span class="material-icons">refresh</span>
                        重新載入地圖
                    </button>
                </div>
            </div>
        `;
    }
    
    const mapInfo = document.querySelector('.spot-detail-map-info');
    if (mapInfo) {
        mapInfo.innerHTML = `
            <div class="spot-detail-map-status spot-detail-map-status--error">
                <span class="material-icons">error</span>
                <span>${message || '載入地圖時發生錯誤'}</span>
            </div>
        `;
    }
}

/**
 * 在 Google 地圖中開啟
 */
function openInGoogleMaps() {
    if (spotData && spotData.spotLat && spotData.spotLng) {
        const lat = parseFloat(spotData.spotLat);
        const lng = parseFloat(spotData.spotLng);
        const spotName = encodeURIComponent(spotData.spotName);
        // 使用景點名稱和座標組合的搜尋，確保直接定位到景點
        window.open(`https://www.google.com/maps/search/${spotName}/@${lat},${lng},17z`, '_blank');
    } else {
        // 如果沒有座標，則使用景點名稱搜尋
        window.open(`https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(spotData.spotName)}`, '_blank');
    }
}

// ========== 4. 收藏功能 ==========

/**
 * 初始化收藏系統
 */
function initFavoriteSystem() {
    updateFavoriteButton();
    setupFavoriteStorageListener();
    console.log('💖 收藏系統初始化完成');
}

/**
 * 設置收藏狀態變更監聽器
 */
function setupFavoriteStorageListener() {
    window.addEventListener('storage', function(e) {
        if (e.key === 'spotFavoriteChange') {
            const data = JSON.parse(e.newValue);
            
            // 確認是否為當前景點
            if (data.spotId === spotData.spotId) {
                // 更新收藏狀態
                isFavorited = data.isFavorited;
                updateFavoriteButton();
                
                // 如果有提供收藏數量，更新顯示
                if (data.favoriteCount !== undefined) {
                    updateFavoriteCount(data.favoriteCount);
                }
            }
        }
    });
}

/**
 * 切換收藏狀態
 */
async function toggleFavorite(retryCount = 0) {
    // 最大重試次數
    const MAX_RETRIES = 3;
    
    if (!spotData) {
        showToast('景點資料載入中，請稍候', 'warning');
        return;
    }
    
    const favoriteBtn = document.getElementById('favoriteBtn');
    if (!favoriteBtn) {
        showToast('找不到收藏按鈕', 'error');
        return;
    }
    
    // 檢查按鈕是否已被禁用，避免重複點擊
    if (favoriteBtn.disabled || favoriteBtn.classList.contains('loading')) {
        return;
    }
    
    favoriteBtn.disabled = true;
    
    // 保存原始狀態，用於恢復
    const isActive = favoriteBtn.classList.contains('active');
    const icon = favoriteBtn.querySelector('.material-icons');
    const originalText = icon ? icon.textContent : 'favorite_border';
    
    // 顯示載入狀態
    if (icon) {
        icon.textContent = 'sync';
    }
    favoriteBtn.classList.add('loading');
    
    // 設置超時處理，確保按鈕不會永久停在載入狀態
    const timeout = setTimeout(() => {
        restoreButtonState(favoriteBtn, originalText, isActive);
    }, 5000); // 5秒後自動恢復
    
    try {
        // 首先檢查當前收藏狀態，確保與後端同步
        const statusResponse = await fetch(`/api/spot/favorites/${spotData.spotId}/status`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        // 如果是未登入，直接進行切換（會返回401）
        if (statusResponse.status !== 401 && statusResponse.ok) {
            const statusResult = await statusResponse.json();
            const serverStatus = statusResult.success && statusResult.data;
            
            // 如果前端狀態與後端不一致，先更新前端狀態
            if (serverStatus !== isActive) {
                console.log(`收藏狀態不同步，後端: ${serverStatus}, 前端: ${isActive}`);
                // 更新內部狀態變數
                isFavorited = serverStatus;
                // 更新UI
                updateFavoriteButton();
            }
        }
        
        // 使用新的 API 端點
        const response = await fetch(`/api/spot/favorites/${spotData.spotId}/toggle`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        // 清除超時處理
        clearTimeout(timeout);
        
        if (response.ok) {
            const result = await response.json();
            
            if (result.success) {
                // 從響應數據中獲取收藏狀態和數量
                isFavorited = result.data.isFavorited;
                const favoriteCount = result.data.favoriteCount || 0;
                
                // 更新UI
                updateFavoriteButton();
                updateFavoriteCount(favoriteCount);
                
                // 顯示提示訊息
                showToast(result.message || (isFavorited ? '已加入收藏' : '已移除收藏'), 
                          isFavorited ? 'success' : 'info');
                
                // 通知其他頁面更新收藏狀態
                localStorage.setItem('spotFavoriteChange', JSON.stringify({
                    spotId: spotData.spotId,
                    isFavorited: isFavorited,
                    favoriteCount: favoriteCount,
                    timestamp: new Date().getTime()
                }));
            } else {
                // API 返回錯誤
                showToast(result.message || '操作失敗，請稍後再試', 'error');
                restoreButtonState(favoriteBtn, originalText, isActive);
            }
        } else {
            try {
                const error = await response.json();
                
                // 處理401未授權錯誤（未登入）
                if (response.status === 401) {
                    // 顯示自定義登入提示對話框，並傳入按鈕和原始圖示文字
                    showLoginDialog('收藏功能需要登入', '請先登入會員後再收藏景點', favoriteBtn, originalText, isActive);
                    return;
                }
                
                showToast(error.message || '操作失敗，請稍後再試', 'error');
                restoreButtonState(favoriteBtn, originalText, isActive);
            } catch (e) {
                // 處理401未授權錯誤（未登入）
                if (response.status === 401) {
                    // 顯示自定義登入提示對話框，並傳入按鈕和原始圖示文字
                    showLoginDialog('收藏功能需要登入', '請先登入會員後再收藏景點', favoriteBtn, originalText, isActive);
                    return;
                }
                
                showToast('操作失敗，請稍後再試', 'error');
                restoreButtonState(favoriteBtn, originalText, isActive);
            }
        }
    } catch (error) {
        // 清除超時處理（如果尚未清除）
        clearTimeout(timeout);
        
        console.error('收藏操作失敗:', error);
        
        // 如果是死鎖或並發問題，嘗試重試
        if (retryCount < MAX_RETRIES) {
            console.log(`重試收藏操作 (${retryCount + 1}/${MAX_RETRIES})...`);
            // 短暫延遲後重試
            setTimeout(() => {
                toggleFavorite(retryCount + 1);
            }, 500 * (retryCount + 1)); // 逐漸增加延遲時間
            return;
        }
        
        showToast('網路連線異常，請稍後再試', 'error');
        restoreButtonState(favoriteBtn, originalText, isActive);
    } finally {
        // 如果不是重試，才移除載入狀態
        if (retryCount === 0) {
            favoriteBtn.disabled = false;
            favoriteBtn.classList.remove('loading');
        }
    }
}

/**
 * 恢復按鈕狀態
 * @param {HTMLElement} button 按鈕元素
 * @param {string} iconText 圖示文字
 * @param {boolean} isActive 是否為活動狀態
 */
function restoreButtonState(button, iconText, isActive) {
    if (!button) return;
    
    // 恢復圖示
    const icon = button.querySelector('.material-icons');
    if (icon) {
        icon.textContent = isActive ? 'favorite' : (iconText || 'favorite_border');
    }
    
    // 恢復類別
    if (isActive) {
        button.classList.add('active');
    } else {
        button.classList.remove('active');
    }
    
    // 恢復標題
    button.title = isActive ? '取消收藏' : '加入收藏';
    
    // 恢復可用狀態
    button.classList.remove('loading');
    button.disabled = false;
}

/**
 * 更新收藏按鈕狀態
 */
function updateFavoriteButton() {
    const favoriteBtn = document.getElementById('favoriteBtn');
    if (!favoriteBtn) return;
    
    const icon = favoriteBtn.querySelector('.material-icons');
    
    if (isFavorited) {
        favoriteBtn.classList.add('active');
        icon.textContent = 'favorite';
        favoriteBtn.title = '取消收藏';
    } else {
        favoriteBtn.classList.remove('active');
        icon.textContent = 'favorite_border';
        favoriteBtn.title = '收藏景點';
    }
}

/**
 * 更新收藏次數顯示
 */
function updateFavoriteCount(count) {
    const favoriteCountElements = [
        document.getElementById('favoriteCount'),
        document.getElementById('sidebarFavoriteCount')
    ];
    
    favoriteCountElements.forEach(element => {
        if (element) {
            element.textContent = count || 0;
        }
    });
}

// ========== 5. 書籤功能 ==========

/**
 * 初始化書籤系統
 */
function initBookmarkSystem() {
    // 從 localStorage 讀取書籤狀態
    const bookmarks = JSON.parse(localStorage.getItem('spotBookmarks') || '[]');
    isBookmarked = bookmarks.includes(spotData?.spotId);
    updateBookmarkButton();
    console.log('🔖 書籤系統初始化完成');
}

/**
 * 切換書籤狀態
 */
function toggleBookmark() {
    if (!spotData) {
        showToast('景點資料載入中，請稍候', 'warning');
        return;
    }
    
    let bookmarks = JSON.parse(localStorage.getItem('spotBookmarks') || '[]');
    
    if (isBookmarked) {
        // 移除書籤
        bookmarks = bookmarks.filter(id => id !== spotData.spotId);
        isBookmarked = false;
        showToast('已移除書籤', 'info');
    } else {
        // 添加書籤
        bookmarks.push(spotData.spotId);
        isBookmarked = true;
        showToast('已加入書籤', 'success');
    }
    
    localStorage.setItem('spotBookmarks', JSON.stringify(bookmarks));
    updateBookmarkButton();
}

/**
 * 更新書籤按鈕狀態
 */
function updateBookmarkButton() {
    const bookmarkBtn = document.querySelector('.spot-detail-action-btn--bookmark');
    if (!bookmarkBtn) return;
    
    const icon = bookmarkBtn.querySelector('.material-icons');
    
    if (isBookmarked) {
        bookmarkBtn.classList.add('active');
        icon.textContent = 'bookmark';
        bookmarkBtn.title = '移除書籤';
    } else {
        bookmarkBtn.classList.remove('active');
        icon.textContent = 'bookmark_border';
        bookmarkBtn.title = '加入書籤';
    }
}

// ========== 6. 快速操作功能 ==========

/**
 * 加入行程
 */
function addToItinerary() {
    showToast('行程功能開發中，敬請期待', 'info');
    // TODO: 實作加入行程功能
}

/**
 * 查看揪團
 */
function joinGroupActivity() {
    if (!spotData) {
        showToast('景點資料載入中，請稍候', 'warning');
        return;
    }
    
    // 跳轉到揪團頁面
    window.location.href = `/groupactivity/list?spotId=${spotData.spotId}`;
}


// ========== 8. Toast 通知系統 ==========

let toastContainer = null;

/**
 * 初始化 Toast 系統
 */
function initToastSystem() {
    // 建立 Toast 容器
    toastContainer = document.createElement('div');
    toastContainer.className = 'toast-container';
    toastContainer.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        z-index: 10000;
        display: flex;
        flex-direction: column;
        gap: 0.5rem;
        pointer-events: none;
    `;
    document.body.appendChild(toastContainer);
    
    console.log('🍞 Toast 系統初始化完成');
}

/**
 * 顯示 Toast 通知
 */
function showToast(message, type = 'info', duration = 3000) {
    if (!toastContainer) {
        console.warn('Toast 系統未初始化');
        return;
    }
    
    // 建立 Toast 元素
    const toast = document.createElement('div');
    toast.className = `toast toast--${type}`;
    
    // 設定樣式
    const colors = {
        success: { bg: '#4caf50', icon: 'check_circle' },
        error: { bg: '#f44336', icon: 'error' },
        warning: { bg: '#ff9800', icon: 'warning' },
        info: { bg: '#2196f3', icon: 'info' }
    };
    
    const color = colors[type] || colors.info;
    
    toast.style.cssText = `
        background: ${color.bg};
        color: white;
        padding: 0.75rem 1rem;
        border-radius: 8px;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        display: flex;
        align-items: center;
        gap: 0.5rem;
        font-size: 0.9rem;
        font-weight: 500;
        max-width: 350px;
        word-wrap: break-word;
        pointer-events: auto;
        transform: translateX(100%);
        transition: transform 0.3s ease;
    `;
    
    toast.innerHTML = `
        <span class="material-icons" style="font-size: 1.2rem;">${color.icon}</span>
        <span>${message}</span>
    `;
    
    // 添加到容器
    toastContainer.appendChild(toast);
    
    // 動畫進入
    setTimeout(() => {
        toast.style.transform = 'translateX(0)';
    }, 10);
    
    // 自動移除
    setTimeout(() => {
        toast.style.transform = 'translateX(100%)';
        setTimeout(() => {
            if (toast.parentNode) {
                toast.parentNode.removeChild(toast);
            }
        }, 300);
    }, duration);
    
    // 點擊關閉
    toast.addEventListener('click', () => {
        toast.style.transform = 'translateX(100%)';
        setTimeout(() => {
            if (toast.parentNode) {
                toast.parentNode.removeChild(toast);
            }
        }, 300);
    });
}

// ========== 9. 全域函數 ==========

// 將主要函數暴露到全域範圍，供 HTML 呼叫
window.toggleFavorite = toggleFavorite;
window.toggleBookmark = toggleBookmark;
window.scrollToMap = scrollToMap;
window.openInGoogleMaps = openInGoogleMaps;
window.addToItinerary = addToItinerary;
window.joinGroupActivity = joinGroupActivity;

/**
 * 顯示登入提示對話框
 * @param {string} title 對話框標題
 * @param {string} message 對話框訊息
 * @param {HTMLElement} buttonElement 觸發對話框的按鈕元素
 * @param {string} originalIconText 按鈕原始圖示文字
 * @param {boolean} isActive 按鈕是否為活動狀態
 */
function showLoginDialog(title, message, buttonElement, originalIconText, isActive) {
    // 檢查是否已存在對話框，避免重複顯示
    if (document.getElementById('login-dialog')) {
        return;
    }
    
    // 創建對話框元素
    const dialog = document.createElement('div');
    dialog.id = 'login-dialog';
    dialog.className = 'spot-detail-dialog';
    dialog.style.cssText = `
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background-color: rgba(0, 0, 0, 0.5);
        display: flex;
        justify-content: center;
        align-items: center;
        z-index: 10000;
    `;
    
    // 對話框內容
    const content = document.createElement('div');
    content.className = 'spot-detail-dialog__content';
    content.style.cssText = `
        background-color: var(--md-sys-color-surface);
        border-radius: 16px;
        padding: 24px;
        width: 90%;
        max-width: 400px;
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
        text-align: center;
        animation: dialogFadeIn 0.3s ease;
    `;
    
    // 對話框標題
    const titleEl = document.createElement('h3');
    titleEl.className = 'spot-detail-dialog__title';
    titleEl.style.cssText = `
        margin-top: 0;
        margin-bottom: 16px;
        color: var(--md-sys-color-on-surface);
        font-size: 1.5rem;
        font-weight: 500;
    `;
    titleEl.textContent = title;
    
    // 對話框訊息
    const messageEl = document.createElement('p');
    messageEl.className = 'spot-detail-dialog__message';
    messageEl.style.cssText = `
        margin-bottom: 24px;
        color: var(--md-sys-color-on-surface-variant);
        font-size: 1rem;
    `;
    messageEl.textContent = message;
    
    // 按鈕容器
    const buttons = document.createElement('div');
    buttons.className = 'spot-detail-dialog__buttons';
    buttons.style.cssText = `
        display: flex;
        justify-content: center;
        gap: 16px;
    `;
    
    // 取消按鈕
    const cancelButton = document.createElement('button');
    cancelButton.className = 'spot-detail-dialog__button spot-detail-dialog__button--cancel';
    cancelButton.style.cssText = `
        padding: 10px 20px;
        border: none;
        border-radius: 20px;
        background-color: var(--md-sys-color-surface-variant);
        color: var(--md-sys-color-on-surface-variant);
        font-weight: 500;
        cursor: pointer;
        transition: background-color 0.2s ease;
    `;
    cancelButton.textContent = '稍後再說';
    cancelButton.addEventListener('click', () => {
        closeDialog(dialog);
        
        // 恢復按鈕狀態
        restoreButtonState(buttonElement, originalIconText, isActive);
    });
    
    // 登入按鈕
    const loginButton = document.createElement('button');
    loginButton.className = 'spot-detail-dialog__button spot-detail-dialog__button--login';
    loginButton.style.cssText = `
        padding: 10px 20px;
        border: none;
        border-radius: 20px;
        background-color: var(--md-sys-color-primary);
        color: var(--md-sys-color-on-primary);
        font-weight: 500;
        cursor: pointer;
        transition: background-color 0.2s ease;
    `;
    loginButton.textContent = '前往登入';
    loginButton.addEventListener('click', () => {
        // 保存當前URL，登入後可以返回
        localStorage.setItem('redirectAfterLogin', window.location.href);
        
        // 跳轉到登入頁面
        window.location.href = '/members/login';
    });
    
    // 組裝對話框
    buttons.appendChild(cancelButton);
    buttons.appendChild(loginButton);
    content.appendChild(titleEl);
    content.appendChild(messageEl);
    content.appendChild(buttons);
    dialog.appendChild(content);
    
    // 添加到頁面
    document.body.appendChild(dialog);
    
    // 添加動畫樣式
    const style = document.createElement('style');
    style.textContent = `
        @keyframes dialogFadeIn {
            from {
                opacity: 0;
                transform: translateY(-20px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }
        
        .spot-detail-dialog__button:hover {
            filter: brightness(1.1);
        }
    `;
    document.head.appendChild(style);
    
    // 點擊背景關閉對話框
    dialog.addEventListener('click', (e) => {
        if (e.target === dialog) {
            closeDialog(dialog);
            
            // 恢復按鈕狀態
            restoreButtonState(buttonElement, originalIconText, isActive);
        }
    });
    
    // ESC 鍵關閉對話框
    document.addEventListener('keydown', function escHandler(e) {
        if (e.key === 'Escape') {
            closeDialog(dialog);
            
            // 恢復按鈕狀態
            restoreButtonState(buttonElement, originalIconText, isActive);
            
            document.removeEventListener('keydown', escHandler);
        }
    });
}

/**
 * 關閉對話框
 * @param {HTMLElement} dialog 對話框元素
 */
function closeDialog(dialog) {
    dialog.style.opacity = '0';
    setTimeout(() => {
        if (dialog.parentNode) {
            dialog.parentNode.removeChild(dialog);
        }
    }, 300);
}

console.log('🌟 景點詳情頁 JavaScript 載入完成'); 