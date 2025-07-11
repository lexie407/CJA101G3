/**
 * 景點地圖頁面 JavaScript
 * @version 1.1
 * @description 提供地圖顯示、篩選、Google Maps API 集成、新版Tab介面等功能
 */

(function() {
    'use strict';

    // 全域變數
    let map;
    let markers = [];
    let allSpots = [];
    let filteredSpots = [];
    let infoWindow;
    let isGoogleMapsLoaded = false;
    
    const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    
    // 台灣各縣市的地理中心座標
    const regionCenters = {
        '台北市': { lat: 25.0330, lng: 121.5654, zoom: 11 },
        '新北市': { lat: 25.0173, lng: 121.4629, zoom: 10 },
        '桃園市': { lat: 24.9936, lng: 121.3010, zoom: 10 },
        '台中市': { lat: 24.1477, lng: 120.6736, zoom: 10 },
        '台南市': { lat: 22.9999, lng: 120.2269, zoom: 10 },
        '高雄市': { lat: 22.6273, lng: 120.3014, zoom: 10 },
        '基隆市': { lat: 25.1276, lng: 121.7392, zoom: 12 },
        '新竹市': { lat: 24.8138, lng: 120.9675, zoom: 12 },
        '嘉義市': { lat: 23.4801, lng: 120.4491, zoom: 12 },
        '新竹縣': { lat: 24.8387, lng: 121.0177, zoom: 10 },
        '苗栗縣': { lat: 24.5602, lng: 120.8214, zoom: 10 },
        '彰化縣': { lat: 24.0518, lng: 120.5161, zoom: 10 },
        '南投縣': { lat: 23.9609, lng: 120.9718, zoom: 9 },
        '雲林縣': { lat: 23.7092, lng: 120.4313, zoom: 10 },
        '嘉義縣': { lat: 23.4518, lng: 120.2554, zoom: 10 },
        '屏東縣': { lat: 22.5519, lng: 120.5487, zoom: 10 },
        '宜蘭縣': { lat: 24.7021, lng: 121.7377, zoom: 10 },
        '花蓮縣': { lat: 23.9871, lng: 121.6015, zoom: 9 },
        '台東縣': { lat: 22.7972, lng: 121.1713, zoom: 9 },
        '澎湖縣': { lat: 23.5711, lng: 119.5794, zoom: 11 },
        '金門縣': { lat: 24.4324, lng: 118.3170, zoom: 11 },
        '連江縣': { lat: 26.1605, lng: 119.9512, zoom: 11 }
    };

    /**
     * 初始化所有功能
     */
    function init() {
        console.log('🚀 景點地圖系統初始化中...');
        
        document.addEventListener('DOMContentLoaded', function() {
            console.log('🚀 DOM 載入完成，開始初始化地圖系統...');
            initializeMap();
            initEventListeners();
            initKeyboardShortcuts();
            initAccessibility();
        });
    }

    /**
     * 初始化地圖系統
     */
    async function initializeMap() {
        console.log('🚀 開始初始化地圖系統...');
        
        try {
            const config = await loadGoogleMapsAPI();
            await waitForGoogleMaps();
            initGoogleMap();
            isGoogleMapsLoaded = true;
            console.log('✅ Google Maps 系統載入成功');
        } catch (error) {
            console.warn('⚠️ Google Maps API 載入失敗，切換到 OpenStreetMap:', error.message);
            isGoogleMapsLoaded = false;
            await initOpenStreetMap();
        }
        
        await loadSpots();
        await loadRegionStats();
    }

    /**
     * 等待Google Maps API完全載入
     */
    function waitForGoogleMaps() {
        return new Promise((resolve, reject) => {
            let attempts = 0;
            const maxAttempts = 50; // 5 seconds
            const checkGoogle = () => {
                if (window.google && window.google.maps && window.google.maps.Map) {
                    console.log('🎯 Google Maps API 已就緒');
                    resolve();
                } else if (attempts >= maxAttempts) {
                    reject(new Error('Google Maps API 載入超時'));
                } else {
                    attempts++;
                    setTimeout(checkGoogle, 100);
                }
            };
            checkGoogle();
        });
    }

    /**
     * 動態載入 Google Maps API
     */
    function loadGoogleMapsAPI() {
        return new Promise((resolve, reject) => {
            if (window.google && window.google.maps) {
                resolve({});
                return;
            }
            fetch('/api/spot/google-maps-config')
                .then(response => response.json())
                .then(config => {
                    console.log('📡 Google API 配置:', config);
                    if (!config.available || !config.apiKey) {
                        throw new Error(config.message || 'Google API 未設定');
                    }
                    const script = document.createElement('script');
                    script.src = config.mapsApiUrl;
                    script.async = true;
                    script.defer = true;
                    script.onload = () => {
                        console.log('✅ Google Maps API 腳本載入完成');
                        resolve(config);
                    };
                    script.onerror = () => reject(new Error('Google Maps API 腳本載入失敗'));
                    document.head.appendChild(script);
                })
                .catch(error => reject(error));
        });
    }

    /**
     * Google Maps 初始化
     */
    function initGoogleMap() {
        const taiwanCenter = { lat: 23.8, lng: 121.0 };
        map = new google.maps.Map(document.getElementById("map"), {
            zoom: 8,
            center: taiwanCenter,
            mapTypeControl: true,
            streetViewControl: true,
            fullscreenControl: true,
            gestureHandling: 'greedy'
            // 移除自訂樣式，使用預設地圖外觀
        });
        
        infoWindow = new google.maps.InfoWindow({ maxWidth: 350 });

        google.maps.event.addListener(infoWindow, 'closeclick', () => {
            unhighlightListItem(null);
        });
        
        google.maps.event.addListenerOnce(map, 'idle', hideMapLoading);
        console.log('✅ Google Maps 初始化完成');
    }

    /**
     * 使用 OpenStreetMap (備用方案)
     */
    async function initOpenStreetMap() {
        try {
            await loadLeafletResources();
            const taiwanCenter = [23.8, 121.0];
            map = L.map('map').setView(taiwanCenter, 8);
            const tileLayer = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                attribution: '© OpenStreetMap contributors'
            }).addTo(map);
            tileLayer.on('load', hideMapLoading);
            setTimeout(hideMapLoading, 2000);
            console.log('✅ OpenStreetMap 初始化成功');
        } catch (error) {
            console.error('❌ OpenStreetMap 載入失敗:', error);
            showError('地圖載入失敗，請重新整理頁面');
        }
    }

    /**
     * 載入 Leaflet 資源
     */
    function loadLeafletResources() {
        return new Promise((resolve, reject) => {
            if (window.L) {
                resolve();
                return;
            }
            const link = document.createElement('link');
            link.rel = 'stylesheet';
            link.href = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.css';
            document.head.appendChild(link);
            const script = document.createElement('script');
            script.src = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.js';
            script.onload = resolve;
            script.onerror = () => reject(new Error('Leaflet 載入失敗'));
            document.head.appendChild(script);
        });
    }

    /**
     * 載入景點資料
     */
    async function loadSpots() {
        showMapLoading('載入景點資料中...');
        try {
            console.log('🔍 正在載入景點資料...');
            const response = await fetch('/api/spot/public/list');
            if (!response.ok) throw new Error(`HTTP ${response.status}`);
            const result = await response.json();
            console.log('📊 API 回應:', result);
            
            if (result.success && Array.isArray(result.data)) {
                allSpots = result.data.map(spot => ({
                    ...spot,
                    region: spot.spotLoc ? (spot.spotLoc.split(/[市縣]/)[0] + (spot.spotLoc.includes('市') ? '市' : '縣')) : '其他',
                    town: spot.spotLoc ? spot.spotLoc.substring(3, 6) : ''
                }));
                filteredSpots = [...allSpots];
                console.log(`✅ 成功載入 ${allSpots.length} 個景點`);
                displaySpots(filteredSpots);
                displaySpotList(filteredSpots);
                updateStats();
            } else {
                throw new Error('API 回應格式異常');
            }
        } catch (error) {
            console.error('❌ 載入景點資料時發生錯誤:', error);
            showError('載入景點資料失敗：' + error.message);
        } finally {
            hideMapLoading();
        }
    }

    /**
     * 載入區域統計
     */
    async function loadRegionStats() {
        const regionStats = {};
        allSpots.forEach(spot => {
            regionStats[spot.region] = (regionStats[spot.region] || 0) + 1;
        });
        displayRegionFilters(regionStats);
        console.log('✅ 區域統計載入完成');
    }

    /**
     * 顯示區域篩選按鈕
     */
    function displayRegionFilters(regionStats) {
        const container = document.getElementById('regionFilters');
        if (!container) return;
        
        const allButton = container.querySelector('[data-region="all"]');
        container.innerHTML = ''; 
        if (allButton) container.appendChild(allButton);
        
        const sortedRegions = Object.entries(regionStats).sort(([,a], [,b]) => b - a);
        
        sortedRegions.forEach(([region, count]) => {
            const badge = document.createElement('button');
            badge.className = 'spot-map-region-badge';
            badge.dataset.region = region;
            badge.textContent = `${region} (${count})`;
            container.appendChild(badge);
        });
    }

    /**
     * 顯示景點標記
     */
    function displaySpots(spots) {
        clearMarkers();
        spots.forEach(spot => {
            if (spot.spotLat && spot.spotLng) createMarker(spot);
        });
    }

    /**
     * 創建地圖標記 (自動判斷地圖類型)
     */
    function createMarker(spot) {
        if (isGoogleMapsLoaded) createGoogleMarker(spot);
        else createLeafletMarker(spot);
    }

    /**
     * Google Maps 標記
     */
    function createGoogleMarker(spot) {
        // 檢查座標是否有效
        if (!spot.spotLat || !spot.spotLng || 
            isNaN(spot.spotLat) || isNaN(spot.spotLng) ||
            spot.spotLat < -90 || spot.spotLat > 90 ||
            spot.spotLng < -180 || spot.spotLng > 180) {
            console.warn(`⚠️ 景點 ${spot.spotName} 座標無效:`, spot.spotLat, spot.spotLng);
            return null;
        }

        const position = { lat: spot.spotLat, lng: spot.spotLng };
        
        // 使用傳統 Marker API，避免需要 Map ID
        const marker = new google.maps.Marker({
            map: map,
            position: position,
            title: spot.spotName,
            icon: {
                url: 'data:image/svg+xml;charset=UTF-8,' + encodeURIComponent(`
                    <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24">
                        <path fill="#1976d2" d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z"/>
                    </svg>
                `),
                scaledSize: new google.maps.Size(32, 32),
                anchor: new google.maps.Point(16, 32)
            }
        });

        marker.spotId = spot.spotId;
        
        marker.addListener('click', () => {
            infoWindow.setContent(createInfoWindowContent(spot));
            infoWindow.open(map, marker);
            highlightListItem(spot.spotId);
            scrollToListItem(spot.spotId);
        });
        
        markers.push(marker);
        return marker;
    }

    /**
     * Leaflet 標記 (OpenStreetMap 備用方案)
     */
    function createLeafletMarker(spot) {
        if (!window.L) return null;
        
        // 檢查座標是否有效
        if (!spot.spotLat || !spot.spotLng || 
            isNaN(spot.spotLat) || isNaN(spot.spotLng) ||
            spot.spotLat < -90 || spot.spotLat > 90 ||
            spot.spotLng < -180 || spot.spotLng > 180) {
            console.warn(`⚠️ 景點 ${spot.spotName} 座標無效:`, spot.spotLat, spot.spotLng);
            return null;
        }

        const marker = L.marker([spot.spotLat, spot.spotLng])
            .addTo(map)
            .bindPopup(createInfoWindowContent(spot));
        
        marker.spotId = spot.spotId;
        
        marker.on('click', () => {
            highlightListItem(spot.spotId);
            scrollToListItem(spot.spotId);
        });
        
        markers.push(marker);
        return marker;
    }

    /**
     * 創建標記內容
     */
    function createMarkerContent(spot) {
        const container = document.createElement('div');
        container.className = 'custom-marker';
        
        // 根據評分決定樣式
        const hasHighRating = spot.googleRating && spot.googleRating >= 4.0;
        container.innerHTML = `
            <div class="marker-pin ${hasHighRating ? 'marker-pin--high-rating' : ''}">
                <span class="material-icons">place</span>
            </div>
        `;
        return container;
    }

    /**
     * 創建資訊視窗內容
     */
    function createInfoWindowContent(spot) {
        const ratingHtml = spot.googleRating ? 
            `<div class="spot-rating">
                <span class="material-icons">star</span>
                ${spot.googleRating.toFixed(1)} (${spot.googleTotalRatings || 0} 則評價)
             </div>` : '';

        return `
            <div class="spot-info-window">
                <h3 class="spot-info-window__title">${spot.spotName}</h3>
                ${ratingHtml}
                <div class="spot-info-window__content">
                    <p><span class="material-icons">place</span> ${spot.spotLoc || '無地址資訊'}</p>
                    ${spot.tel ? `<p><span class="material-icons">phone</span> ${spot.tel}</p>` : ''}
                    ${spot.spotDesc ? `<p class="spot-info-window__desc">${spot.spotDesc}</p>` : ''}
                    <a href="/spot/detail/${spot.spotId}" class="spot-info-window__link" target="_blank">
                        <span class="material-icons">visibility</span>
                        查看詳情
                    </a>
                </div>
            </div>
        `;
    }

    /**
     * 顯示景點列表
     */
    function displaySpotList(spots) {
        const container = document.getElementById('spotList');
        if (!container) return;
        if (spots.length === 0) {
            container.innerHTML = `<div class="spot-map-empty"><i class="material-icons">search_off</i><p>無符合條件景點</p></div>`;
            return;
        }
        container.innerHTML = spots.map(spot => `
            <div class="spot-map-card" data-spot-id="${spot.spotId}">
                <div class="spot-map-card__content">
                    <h6 class="spot-map-card__title">${spot.spotName}</h6>
                    <small class="spot-map-card__location">
                        <i class="material-icons">place</i> ${spot.region} ${spot.town}
                    </small>
                    ${spot.googleRating ? `<div class="spot-map-rating-stars spot-map-rating-stars--small">${generateStarRating(spot.googleRating, 'small')}</div>` : ''}
                </div>
            </div>`).join('');
        updateSpotCount(spots.length);
        addListItemEventListeners();
    }
    
    /**
     * 聚焦到指定景點 (從列表點擊)
     */
    function focusOnSpot(spotId) {
        const spot = allSpots.find(s => s.spotId === spotId);
        if (spot && spot.spotLat && spot.spotLng) {
            if (isGoogleMapsLoaded) {
                map.setCenter({ lat: spot.spotLat, lng: spot.spotLng });
                map.setZoom(15);
                const marker = markers.find(m => m.spotId === spotId);
                if (marker) google.maps.event.trigger(marker, 'click');
            } else {
                map.setView([spot.spotLat, spot.spotLng], 15);
                const marker = markers.find(m => m.spotId === spotId);
                if (marker) marker.openPopup();
            }
        }
    }

    /**
     * 篩選功能
     */
    window.applyFilters = function() {
        const regionFilter = document.querySelector('.spot-map-region-badge.active')?.dataset.region || 'all';
        const ratingFilter = document.getElementById('ratingFilter')?.value || 'all';
        const searchTerm = document.getElementById('searchInput')?.value.toLowerCase().trim() || '';
        
        console.log('🔍 套用篩選條件:', {
            region: regionFilter,
            rating: ratingFilter,
            search: searchTerm
        });
        
        filteredSpots = allSpots.filter(spot => {
            if (regionFilter !== 'all' && spot.region !== regionFilter) return false;
            if (ratingFilter !== 'all' && (!spot.googleRating || spot.googleRating < parseFloat(ratingFilter))) return false;
            if (searchTerm && !spot.spotName.toLowerCase().includes(searchTerm) && !(spot.spotLoc && spot.spotLoc.toLowerCase().includes(searchTerm))) return false;
            return true;
        });
        
        console.log(`📊 篩選結果: ${filteredSpots.length} / ${allSpots.length} 個景點`);
        
        displaySpots(filteredSpots);
        displaySpotList(filteredSpots);
        updateStats();
        
        // 自動調整地圖視圖到篩選後的景點
        if (filteredSpots.length > 0) {
            setTimeout(() => {
                fitMapToFilteredSpots();
            }, 100); // 延遲一點時間確保標記已經建立
        } else {
            console.log('⚠️ 沒有符合篩選條件的景點');
            // 如果沒有結果，顯示台灣全景
            if (isGoogleMapsLoaded) {
                map.setCenter({ lat: 23.8, lng: 121.0 });
                map.setZoom(8);
            } else if (window.L) {
                map.setView([23.8, 121.0], 8);
            }
        }
    };

    /**
     * 清除篩選
     */
    window.clearFilters = function() {
        document.querySelectorAll('.spot-map-region-badge.active').forEach(b => b.classList.remove('active'));
        const allBtn = document.querySelector('.spot-map-region-badge[data-region="all"]');
        if (allBtn) allBtn.classList.add('active');
        const ratingFilter = document.getElementById('ratingFilter');
        if (ratingFilter) ratingFilter.value = 'all';
        const searchInput = document.getElementById('searchInput');
        if (searchInput) searchInput.value = '';
        applyFilters();
        showMessage('已清除所有篩選條件', 'info');
        
        // 清除篩選後，顯示所有景點並調整地圖視圖
        setTimeout(() => {
            window.fitMapToBounds();
        }, 200);
    };

    /**
     * 按區域篩選
     */
    function filterByRegion(region) {
        console.log('🔍 篩選地區:', region);
        document.querySelectorAll('.spot-map-region-badge').forEach(btn => btn.classList.remove('active'));
        const targetBtn = document.querySelector(`[data-region="${region}"]`);
        if (targetBtn) {
            targetBtn.classList.add('active');
            console.log('✅ 地區按鈕已啟用:', region);
        } else {
            console.warn('⚠️ 找不到地區按鈕:', region);
        }
        applyFilters();
    }

    /**
     * 更新統計資訊
     */
    function updateStats() {
        const totalElement = document.getElementById('totalSpots');
        const mapElement = document.getElementById('mapSpots');
        if (totalElement) totalElement.textContent = allSpots.length;
        if (mapElement) mapElement.textContent = filteredSpots.length;
    }

    function updateSpotCount(count) {
        const element = document.getElementById('spotCount');
        if (element) element.textContent = count;
    }

    // ========== UI互動相關 ==========

    function addListItemEventListeners() {
        document.querySelectorAll('.spot-map-card[data-spot-id]').forEach(card => {
            const spotId = parseInt(card.dataset.spotId, 10);
            card.addEventListener('mouseenter', () => highlightMarker(spotId));
            card.addEventListener('mouseleave', () => unhighlightMarker(spotId));
            card.addEventListener('click', () => focusOnSpot(spotId));
        });
    }

    function highlightMarker(spotId) {
        markers.forEach(marker => {
            if (marker.spotId === spotId && isGoogleMapsLoaded) {
                // 使用傳統 Marker 的高亮方式
                marker.setIcon({
                    url: 'https://maps.google.com/mapfiles/ms/icons/blue-dot.png',
                    scaledSize: new google.maps.Size(40, 40)
                });
                marker.setZIndex(10);
            }
        });
    }

    function unhighlightMarker(spotId) {
        markers.forEach(marker => {
            if ((marker.spotId === spotId || spotId === null) && isGoogleMapsLoaded) {
                // 恢復原始圖標
                marker.setIcon({
                    url: 'https://maps.google.com/mapfiles/ms/icons/red-dot.png',
                    scaledSize: new google.maps.Size(32, 32)
                });
                marker.setZIndex(1);
            }
        });
    }
    
    function highlightListItem(spotId) {
        unhighlightListItem(null);
        const listItem = document.querySelector(`.spot-map-card[data-spot-id="${spotId}"]`);
        if (listItem) listItem.classList.add('active');
    }

    function unhighlightListItem(spotId) {
        document.querySelectorAll('.spot-map-card.active').forEach(item => {
            if (spotId === null || item.dataset.spotId != spotId) {
                item.classList.remove('active');
            }
        });
    }

    function scrollToListItem(spotId) {
        const listContainer = document.getElementById('spotList');
        const listItem = document.querySelector(`.spot-map-card[data-spot-id="${spotId}"]`);
        if (listContainer && listItem) {
            listItem.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }
    }
    
    // ========== 工具函式 ==========

    function hideMapLoading() {
        const overlay = document.getElementById('mapLoadingOverlay');
        if (overlay) {
            overlay.style.display = 'none';
            console.log('✅ 地圖載入覆蓋層已隱藏');
        }
    }

    function showMapLoading(message = '載入中...') {
        const overlay = document.getElementById('mapLoadingOverlay');
        if (overlay) {
            overlay.style.display = 'flex';
            overlay.innerHTML = `
                <div class="spot-map-loading__spinner">
                    <span class="material-icons">map</span>
                </div>
                <p>${message}</p>
            `;
        }
    }

    function showError(message) {
        console.error('❌ 錯誤:', message);
        showMessage(message, 'error');
    }

    function showMessage(message, type = 'info') {
        const container = document.createElement('div');
        container.className = `alert alert-${type}`;
        container.style.cssText = `position:fixed; top:20px; right:20px; z-index:9999; padding:15px; border-radius:8px; background:#333; color:white;`;
        container.textContent = message;
        document.body.appendChild(container);
        setTimeout(() => container.remove(), 3000);
    }

    function generateStarRating(rating, size = '') {
        const full = Math.floor(rating), half = rating % 1 >= 0.5, empty = 5 - full - (half ? 1 : 0);
        return '★'.repeat(full) + (half ? '☆' : '') + '☆'.repeat(empty) + ` ${rating.toFixed(1)}`;
    }

    function truncateText(text, maxLength) {
        return text.length > maxLength ? text.substr(0, maxLength) + '...' : text;
    }

    function clearMarkers() {
        markers.forEach(marker => {
            if (isGoogleMapsLoaded) marker.map = null;
            else map.removeLayer(marker);
        });
        markers = [];
    }

    /**
     * 初始化所有事件監聽器
     */
    function initEventListeners() {
        // Tab 切換
        document.querySelectorAll('.spot-map-tab-link').forEach(link => {
            link.addEventListener('click', () => {
                document.querySelectorAll('.spot-map-tab-link, .spot-map-tab-pane').forEach(el => el.classList.remove('active'));
                link.classList.add('active');
                const targetPane = document.getElementById(link.dataset.tab);
                if (targetPane) targetPane.classList.add('active');
            });
        });

        // 搜尋框即時搜尋
        let searchTimeout;
        const searchInput = document.getElementById('searchInput');
        if (searchInput) {
            searchInput.addEventListener('input', () => {
                clearTimeout(searchTimeout);
                searchTimeout = setTimeout(applyFilters, 300);
            });
        }

        // 進階篩選自動套用
        const ratingFilter = document.getElementById('ratingFilter');
        if (ratingFilter) {
            ratingFilter.addEventListener('change', applyFilters);
        }
        
        const regionFilters = document.getElementById('regionFilters');
        if (regionFilters) {
            regionFilters.addEventListener('click', (e) => {
                if (e.target.classList.contains('spot-map-region-badge')) {
                    filterByRegion(e.target.dataset.region);
                }
            });
        }
    }

    function initKeyboardShortcuts() {
        document.addEventListener('keydown', function(e) {
            if ((e.ctrlKey || e.metaKey) && e.key === 'f') {
                e.preventDefault();
                const searchInput = document.getElementById('searchInput');
                if (searchInput) {
                    searchInput.focus();
                    searchInput.select();
                }
            }
        });
    }

    function initAccessibility() {
        // 基本無障礙功能
    }

    // 全域可呼叫函式
    window.fitMapToBounds = () => {
        if (markers.length === 0) return;
        if (isGoogleMapsLoaded) {
            const bounds = new google.maps.LatLngBounds();
            markers.forEach(marker => bounds.extend(marker.position));
            map.fitBounds(bounds);
        } else {
            const group = new L.featureGroup(markers);
            map.fitBounds(group.getBounds());
        }
    };

    /**
     * 調整地圖視圖到篩選後的景點
     */
    function fitMapToFilteredSpots() {
        if (markers.length === 0) return;
        
        console.log('🎯 調整地圖視圖到篩選後的景點，標記數量:', markers.length);
        
        // 檢查是否有特定地區篩選
        const activeRegionBtn = document.querySelector('.spot-map-region-badge.active');
        const selectedRegion = activeRegionBtn ? activeRegionBtn.dataset.region : 'all';
        
        // 顯示調整提示
        if (selectedRegion !== 'all') {
            showMessage(`正在調整地圖到 ${selectedRegion} 地區`, 'info');
        }
        
        if (isGoogleMapsLoaded) {
            // 如果選擇了特定地區且有對應的中心座標，使用預設中心
            if (selectedRegion !== 'all' && regionCenters[selectedRegion]) {
                const center = regionCenters[selectedRegion];
                map.setCenter({ lat: center.lat, lng: center.lng });
                map.setZoom(center.zoom);
                console.log(`🎯 設定地圖中心到 ${selectedRegion}:`, center);
            } else {
                // 否則根據標記自動調整邊界
                const bounds = new google.maps.LatLngBounds();
                markers.forEach(marker => {
                    if (marker.position) {
                        bounds.extend(marker.position);
                    }
                });
                
                // 檢查是否有有效的邊界
                if (!bounds.isEmpty()) {
                    map.fitBounds(bounds);
                    // 如果只有一個景點，設定合適的縮放級別
                    if (markers.length === 1) {
                        setTimeout(() => {
                            if (map.getZoom() > 15) {
                                map.setZoom(15);
                            }
                        }, 100);
                    }
                }
            }
        } else if (window.L && markers.length > 0) {
            // OpenStreetMap (Leaflet) 的處理
            if (selectedRegion !== 'all' && regionCenters[selectedRegion]) {
                const center = regionCenters[selectedRegion];
                map.setView([center.lat, center.lng], center.zoom);
                console.log(`🎯 設定地圖中心到 ${selectedRegion}:`, center);
            } else {
                const group = new L.featureGroup(markers);
                map.fitBounds(group.getBounds());
            }
        }
    }
    
    window.getCurrentLocation = () => {
        if (!navigator.geolocation) {
            return showError('瀏覽器不支援定位');
        }
        navigator.geolocation.getCurrentPosition(
            (pos) => {
                const center = { lat: pos.coords.latitude, lng: pos.coords.longitude };
                if (isGoogleMapsLoaded) map.setCenter(center);
                else map.setView(center, 13);
                showMessage('已定位到您目前的位置');
            },
            () => showError('無法取得您的位置'),
            { enableHighAccuracy: true }
        );
    };

    // 自動初始化
    init();

})(); 