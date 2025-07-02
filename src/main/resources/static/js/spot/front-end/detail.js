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
                    `https://maps.googleapis.com/maps/api/js?key=${config.apiKey}&libraries=places,geometry&loading=async`;
                
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
    console.log('💖 收藏系統初始化完成');
}

/**
 * 切換收藏狀態
 */
async function toggleFavorite() {
    if (!spotData) {
        showToast('景點資料載入中，請稍候', 'warning');
        return;
    }
    
    const favoriteBtn = document.getElementById('favoriteBtn');
    if (favoriteBtn) {
        favoriteBtn.disabled = true;
    }
    
    try {
        const response = await fetch('/api/spot/favorite', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                spotId: spotData.spotId,
                action: isFavorited ? 'remove' : 'add'
            })
        });
        
        const result = await response.json();
        
        if (result.success) {
            isFavorited = !isFavorited;
            updateFavoriteButton();
            updateFavoriteCount(result.favoriteCount);
            
            const message = isFavorited ? '已加入收藏' : '已移除收藏';
            const type = isFavorited ? 'success' : 'info';
            showToast(message, type);
        } else {
            showToast(result.message || '操作失敗，請稍後再試', 'error');
        }
    } catch (error) {
        console.error('收藏操作失敗:', error);
        showToast('網路連線異常，請稍後再試', 'error');
    } finally {
        if (favoriteBtn) {
            favoriteBtn.disabled = false;
        }
    }
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

// ========== 6. 分享功能 ==========

/**
 * 分享景點
 */
function shareSpot() {
    if (!spotData) {
        showToast('景點資料載入中，請稍候', 'warning');
        return;
    }
    
    const shareData = {
        title: `${spotData.spotName} - 島遊Kha`,
        text: `來看看這個景點：${spotData.spotName}`,
        url: window.location.href
    };
    
    // 使用 Web Share API（如果支援）
    if (navigator.share) {
        navigator.share(shareData)
            .then(() => showToast('分享成功', 'success'))
            .catch(err => {
                console.log('分享取消或失敗:', err);
                fallbackShare();
            });
    } else {
        fallbackShare();
    }
}

/**
 * 備用分享方法
 */
function fallbackShare() {
    // 複製連結到剪貼簿
    copyLink();
}

/**
 * 分享到 Facebook
 */
function shareToFacebook() {
    if (!spotData) {
        showToast('景點資料載入中，請稍候', 'warning');
        return;
    }
    
    const url = encodeURIComponent(window.location.href);
    const text = encodeURIComponent(`來看看這個景點：${spotData.spotName}`);
    const facebookUrl = `https://www.facebook.com/sharer/sharer.php?u=${url}&quote=${text}`;
    
    window.open(facebookUrl, '_blank', 'width=600,height=400');
    showToast('正在開啟 Facebook 分享', 'info');
}

/**
 * 分享到 LINE
 */
function shareToLine() {
    if (!spotData) {
        showToast('景點資料載入中，請稍候', 'warning');
        return;
    }
    
    const text = encodeURIComponent(`來看看這個景點：${spotData.spotName} ${window.location.href}`);
    const lineUrl = `https://social-plugins.line.me/lineit/share?url=${text}`;
    
    window.open(lineUrl, '_blank', 'width=600,height=400');
    showToast('正在開啟 LINE 分享', 'info');
}

/**
 * 複製連結
 */
async function copyLink() {
    try {
        await navigator.clipboard.writeText(window.location.href);
        showToast('連結已複製到剪貼簿', 'success');
    } catch (err) {
        console.error('複製失敗:', err);
        
        // 備用方法
        const textArea = document.createElement('textarea');
        textArea.value = window.location.href;
        textArea.style.position = 'fixed';
        textArea.style.opacity = '0';
        document.body.appendChild(textArea);
        textArea.select();
        
        try {
            document.execCommand('copy');
            showToast('連結已複製到剪貼簿', 'success');
        } catch (err) {
            showToast('複製失敗，請手動複製網址', 'error');
        }
        
        document.body.removeChild(textArea);
    }
}

// ========== 7. 快速操作功能 ==========

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
window.shareSpot = shareSpot;
window.shareToFacebook = shareToFacebook;
window.shareToLine = shareToLine;
window.copyLink = copyLink;
window.scrollToMap = scrollToMap;
window.openInGoogleMaps = openInGoogleMaps;
window.addToItinerary = addToItinerary;
window.joinGroupActivity = joinGroupActivity;
window.reportSpot = reportSpot;

console.log('🌟 景點詳情頁 JavaScript 載入完成'); 