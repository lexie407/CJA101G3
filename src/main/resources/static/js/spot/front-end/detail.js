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
    
    // 初始化地圖
    if (spotData) {
        initializeMap();
    }
    
    // 初始化其他功能
    initToastSystem();
    initFavoriteSystem();
    initBookmarkSystem();
    
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
 * 初始化地圖系統
 */
async function initializeMap() {
    console.log('🗺️ 開始初始化 Google Maps...');
    try {
        const config = await loadGoogleMapsAPI();
        await waitForGoogleMaps();
        initGoogleMap();
        console.log('✅ Google Maps 載入成功');
    } catch (error) {
        console.error('❌ Google Maps API 載入失敗:', error.message);
        showToast('Google Maps 載入失敗，請檢查網路連線', 'error');
        showMapError();
    }
}

/**
 * 載入 Google Maps API
 */
function loadGoogleMapsAPI() {
    return new Promise((resolve, reject) => {
        if (window.google && window.google.maps) {
            resolve({ available: true });
            return;
        }
        
        fetch('/api/spot/google-maps-config')
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP ${response.status}: ${response.statusText}`);
                }
                return response.json();
            })
            .then(config => {
                console.log('📡 Google API 配置:', config);
                
                if (!config.available || !config.hasApiKey) {
                    throw new Error(config.message || 'Google Maps API 未正確設定');
                }
                
                if (!config.apiKey || config.apiKey === 'your-google-maps-api-key') {
                    throw new Error('Google Maps API Key 未設定，請聯繫管理員');
                }
                
                const apiUrl = config.mapsApiUrl ||
                    `https://maps.googleapis.com/maps/api/js?key=${config.apiKey}&libraries=places,geometry,marker&v=beta&loading=async`;
                
                console.log('🔗 載入 Google Maps API');
                
                const script = document.createElement('script');
                script.src = apiUrl;
                script.async = true;
                script.defer = true;
                script.onload = () => {
                    console.log('✅ Google Maps API script 載入完成');
                    resolve(config);
                };
                script.onerror = () => reject(new Error('Google Maps API script 載入失敗，請檢查網路連線'));
                document.head.appendChild(script);
            })
            .catch(error => {
                console.error('❌ 載入 Google Maps 配置失敗:', error);
                reject(error);
            });
    });
}

/**
 * 等待 Google Maps API 就緒
 */
function waitForGoogleMaps() {
    return new Promise((resolve, reject) => {
        let attempts = 0;
        const maxAttempts = 50;
        
        const checkGoogle = () => {
            attempts++;
            if (window.google && window.google.maps && window.google.maps.Map) {
                console.log('🎯 Google Maps API 已就緒');
                resolve();
            } else if (attempts >= maxAttempts) {
                reject(new Error('Google Maps API 載入超時'));
            } else {
                setTimeout(checkGoogle, 100);
            }
        };
        
        checkGoogle();
    });
}

/**
 * 初始化 Google Map
 */
function initGoogleMap() {
    let lat = parseFloat(spotData.spotLat);
    let lng = parseFloat(spotData.spotLng);
    let hasValidCoords = false;
    
    console.log('原始座標值:', { 
        spotLat: spotData.spotLat, 
        spotLng: spotData.spotLng, 
        lat, 
        lng 
    });
    
    // 驗證座標有效性
    if (spotData.spotLat !== null && spotData.spotLng !== null && 
        spotData.spotLat !== undefined && spotData.spotLng !== undefined &&
        spotData.spotLat !== '' && spotData.spotLng !== '' &&
        !isNaN(lat) && !isNaN(lng) &&
        lat >= -90 && lat <= 90 &&
        lng >= -180 && lng <= 180) {
        console.log('✅ 使用有效景點座標:', lat, lng);
        hasValidCoords = true;
    } else {
        // 使用台灣中心點
        lat = 23.8;
        lng = 121.0;
        console.log('⚠️ 座標無效，使用台灣中心點:', lat, lng);
        hasValidCoords = false;
    }
    
    const zoomLevel = hasValidCoords ? 16 : 8;
    const center = { lat: lat, lng: lng };
    
    // 建立地圖
    map = new google.maps.Map(document.getElementById("map"), {
        zoom: zoomLevel,
        center: center,
        mapTypeControl: true,
        streetViewControl: true,
        fullscreenControl: true,
        gestureHandling: 'greedy',
        mapTypeId: 'roadmap',
        styles: [
            {
                featureType: 'poi',
                elementType: 'labels',
                stylers: [{ visibility: 'on' }]
            }
        ]
    });
    
    // 建立資訊視窗
    infoWindow = new google.maps.InfoWindow({ 
        maxWidth: 350,
        pixelOffset: new google.maps.Size(0, -10)
    });
    
    // 建立標記
    const marker = new google.maps.Marker({
        position: center,
        map: map,
        title: hasValidCoords ? 
            `${spotData.spotName}\n${spotData.spotAddr || ''}\n點擊在Google Maps中查看實際景點` : 
            `${spotData.spotName}\n${spotData.spotAddr || ''}\n點擊搜尋位置`,
        icon: {
            url: hasValidCoords ?
                'https://maps.google.com/mapfiles/ms/icons/red-dot.png' :
                'https://maps.google.com/mapfiles/ms/icons/yellow-dot.png',
            scaledSize: new google.maps.Size(40, 40)
        },
        animation: google.maps.Animation.DROP
    });
    
    // 標記點擊事件 - 總是直接開啟Google Maps
    marker.addListener('click', () => {
        openInGoogleMaps();
    });
    
    // 標記雙擊事件 - 顯示資訊視窗
    marker.addListener('dblclick', () => {
        const content = createInfoWindowContent(hasValidCoords);
        infoWindow.setContent(content);
        infoWindow.open(map, marker);
    });
    
    // 顯示歡迎訊息（僅有效座標）
    if (hasValidCoords) {
        setTimeout(() => {
            const welcomeContent = `
                <div class="info-window-content" style="text-align: center; padding: 10px;">
                    <h6 style="margin-bottom: 8px; color: #1976d2;">
                        <span class="material-icons" style="vertical-align: middle; margin-right: 4px; color: #d32f2f;">place</span>
                        ${spotData.spotName}
                    </h6>
                    <p style="margin-bottom: 8px; font-size: 0.9rem; color: #4caf50;">
                        <span class="material-icons" style="vertical-align: middle; margin-right: 4px; font-size: 1rem;">check_circle</span>
                        已定位到精確位置
                    </p>
                    <div style="background: #e3f2fd; border: 1px solid #2196f3; padding: 8px; border-radius: 4px; margin-bottom: 8px;">
                        <small style="color: #1976d2;">
                            <span class="material-icons" style="vertical-align: middle; margin-right: 4px; font-size: 1rem;">info</span>
                            點擊標記直接跳轉到Google Maps景點
                        </small>
                    </div>
                    <small style="color: #666;">雙擊標記顯示詳細資訊</small>
                </div>
            `;
            infoWindow.setContent(welcomeContent);
            infoWindow.open(map, marker);
            
            // 3秒後自動關閉
            setTimeout(() => {
                infoWindow.close();
            }, 3000);
        }, 1000);
    }
    
    // 地圖點擊關閉資訊視窗
    map.addListener('click', () => {
        infoWindow.close();
    });
}

/**
 * 建立資訊視窗內容
 */
function createInfoWindowContent(hasValidCoords) {
    if (hasValidCoords) {
        return `
            <div class="info-window-content" style="text-align: center; padding: 15px;">
                <h6 style="margin-bottom: 10px; color: #1976d2;">
                    <span class="material-icons" style="vertical-align: middle; margin-right: 4px; color: #d32f2f;">place</span>
                    ${spotData.spotName}
                </h6>
                <p style="margin-bottom: 12px; font-size: 0.9rem; color: #666;">${spotData.spotAddr}</p>
                <button onclick="openInGoogleMaps()" style="
                    background: #1976d2; 
                    color: white; 
                    border: none; 
                    padding: 8px 16px; 
                    border-radius: 6px; 
                    cursor: pointer;
                    font-size: 0.9rem;
                    display: inline-flex;
                    align-items: center;
                    gap: 6px;
                ">
                    <span class="material-icons" style="font-size: 1rem;">open_in_new</span>
                    在 Google Maps 中開啟
                </button>
            </div>
        `;
    } else {
        return `
            <div class="info-window-content" style="text-align: center; padding: 15px;">
                <h6 style="margin-bottom: 10px; color: #1976d2;">
                    <span class="material-icons" style="vertical-align: middle; margin-right: 4px; color: #ff9800;">location_off</span>
                    ${spotData.spotName}
                </h6>
                <p style="margin-bottom: 10px; font-size: 0.9rem; color: #666;">${spotData.spotAddr}</p>
                <div style="background: #fff3cd; border: 1px solid #ffeaa7; padding: 8px; border-radius: 4px; margin-bottom: 12px;">
                    <small style="color: #856404;">
                        <span class="material-icons" style="vertical-align: middle; margin-right: 4px; font-size: 1rem;">warning</span>
                        座標位置未設定
                    </small>
                </div>
                <button onclick="openInGoogleMaps()" style="
                    background: #ff9800; 
                    color: white; 
                    border: none; 
                    padding: 8px 16px; 
                    border-radius: 6px; 
                    cursor: pointer;
                    font-size: 0.9rem;
                    display: inline-flex;
                    align-items: center;
                    gap: 6px;
                ">
                    <span class="material-icons" style="font-size: 1rem;">search</span>
                    Google Maps 搜尋
                </button>
            </div>
        `;
    }
}

/**
 * 顯示地圖錯誤
 */
function showMapError() {
    const mapElement = document.getElementById('map');
    if (mapElement) {
        mapElement.innerHTML = `
            <div style="
                display: flex; 
                flex-direction: column; 
                align-items: center; 
                justify-content: center; 
                height: 100%; 
                background: var(--md-sys-color-surface-container-low); 
                border-radius: 12px;
                color: var(--md-sys-color-on-surface-variant);
                text-align: center;
                padding: 2rem;
                border: 1px solid var(--md-sys-color-outline-variant);
            ">
                <span class="material-icons" style="font-size: 4rem; color: var(--md-sys-color-outline); margin-bottom: 1rem;">map</span>
                <h3 style="margin-bottom: 0.5rem; color: var(--md-sys-color-on-surface);">地圖載入失敗</h3>
                <p style="margin-bottom: 1rem; font-size: 0.9rem;">Google Maps API 未正確設定或網路連線異常</p>
                <div style="display: flex; gap: 0.75rem; flex-wrap: wrap; justify-content: center;">
                    <button onclick="location.reload()" style="
                        background: var(--md-sys-color-primary); 
                        color: var(--md-sys-color-on-primary); 
                        border: none; 
                        padding: 0.75rem 1.25rem; 
                        border-radius: 12px; 
                        cursor: pointer;
                        font-size: 0.9rem;
                        font-weight: 500;
                        display: inline-flex;
                        align-items: center;
                        gap: 0.5rem;
                        transition: all 0.2s ease;
                    " onmouseover="this.style.transform='translateY(-2px)'" onmouseout="this.style.transform='translateY(0)'">
                        <span class="material-icons" style="font-size: 1rem;">refresh</span>
                        重新載入
                    </button>
                    <button onclick="openInGoogleMaps()" style="
                        background: var(--md-sys-color-secondary-container); 
                        color: var(--md-sys-color-on-secondary-container); 
                        border: none; 
                        padding: 0.75rem 1.25rem; 
                        border-radius: 12px; 
                        cursor: pointer;
                        font-size: 0.9rem;
                        font-weight: 500;
                        display: inline-flex;
                        align-items: center;
                        gap: 0.5rem;
                        transition: all 0.2s ease;
                    " onmouseover="this.style.transform='translateY(-2px)'" onmouseout="this.style.transform='translateY(0)'">
                        <span class="material-icons" style="font-size: 1rem;">open_in_new</span>
                        Google Maps 查看
                    </button>
                </div>
            </div>
        `;
    }
}

// ========== 3. 地圖操作功能 ==========

/**
 * 滾動到地圖位置
 */
function scrollToMap() {
    const mapElement = document.getElementById('map');
    if (mapElement) {
        mapElement.scrollIntoView({ 
            behavior: 'smooth', 
            block: 'center' 
        });
        showToast('已滾動到地圖位置', 'info');
    }
}

/**
 * 在 Google Maps 中開啟
 */
function openInGoogleMaps() {
    if (!spotData) {
        showToast('景點資料載入中，請稍候', 'warning');
        return;
    }
    
    let url;
    const lat = parseFloat(spotData.spotLat);
    const lng = parseFloat(spotData.spotLng);
    const hasValidCoords = !isNaN(lat) && !isNaN(lng) && 
                          lat >= -90 && lat <= 90 && lng >= -180 && lng <= 180;
    
    if (hasValidCoords) {
        // 優先嘗試使用景點名稱+座標的組合搜尋，這樣更容易找到正確的景點
        const spotName = spotData.spotName;
        const spotAddr = spotData.spotAddr;
        
        // 建立搜尋查詢字串，包含景點名稱和地址
        let searchQuery = spotName;
        if (spotAddr && spotAddr !== spotName) {
            searchQuery += ` ${spotAddr}`;
        }
        
        // 使用Google Maps的place搜尋，並提供座標作為參考位置
        const encodedQuery = encodeURIComponent(searchQuery);
        url = `https://www.google.com/maps/search/${encodedQuery}/@${lat},${lng},17z?hl=zh-TW`;
        
        showToast('正在 Google Maps 中開啟景點位置', 'success');
        console.log(`🗺️ 開啟Google Maps: ${searchQuery} (${lat}, ${lng})`);
    } else {
        // 沒有座標時，使用景點名稱和地址搜尋
        let searchQuery = spotData.spotName;
        if (spotData.spotAddr && spotData.spotAddr !== spotData.spotName) {
            searchQuery += ` ${spotData.spotAddr}`;
        }
        
        const encodedQuery = encodeURIComponent(searchQuery);
        url = `https://www.google.com/maps/search/${encodedQuery}?hl=zh-TW`;
        
        showToast('正在 Google Maps 中搜尋景點', 'info');
        console.log(`🔍 Google Maps搜尋: ${searchQuery}`);
    }
    
    // 在新視窗開啟
    window.open(url, '_blank', 'noopener,noreferrer');
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

/**
 * 檢舉景點
 */
function reportSpot() {
    if (!spotData) {
        showToast('景點資料載入中，請稍候', 'warning');
        return;
    }
    
    const confirmed = confirm(`確定要檢舉「${spotData.spotName}」嗎？\n\n檢舉後將由管理員審核處理。`);
    
    if (confirmed) {
        // 跳轉到檢舉頁面
        window.location.href = `/report/add?type=spot&targetId=${spotData.spotId}`;
    }
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
window.reportSpot = reportSpot;

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