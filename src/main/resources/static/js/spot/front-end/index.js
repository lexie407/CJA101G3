/**
 * 景點首頁 JavaScript
 * @version 2.1
 * @description 提供景點首頁的交互效果和動態篩選功能
 */

(function() {
    'use strict';

    // 全局變數
    let currentFilters = {};
    let allSpots = []; // 存儲所有景點資料
    let filteredSpots = []; // 存儲篩選後的景點資料

    // 檢查動畫偏好
    const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

    /**
     * 初始化所有功能
     */
    function init() {
        // 基礎功能初始化
        initScrollAnimations();
        initCardHoverEffects();
        initButtonEffects();
        initLazyLoading();
        initKeyboardNavigation();
        initAccessibility();
        
        // 新的篩選功能初始化
        initDynamicFilters();
        initCategoryButtons();
        initRegionButtons();
        initSortFunctionality();
        initSearchBox(); // 新增：首頁搜尋功能
        
        // 初始化收藏功能
        initFavoriteButtons();
        
        // 獲取初始景點資料
        loadInitialSpots();
        
        // 初始顯示所有景點
        setTimeout(() => {
            updateSpotDisplay();
            updateStatsDisplay();
        }, 100);
        
        // 頁面載入完成後的效果
        document.addEventListener('DOMContentLoaded', function() {
            if (!prefersReducedMotion) {
                fadeInContent();
            }
        });
        
        // 監聽收藏狀態變更事件
        listenToFavoriteChanges();
    }

    /**
     * 初始化收藏按鈕
     */
    function initFavoriteButtons() {
        // 移除可能存在的重複事件監聽器
        document.removeEventListener('loginDialogCancelled', handleLoginDialogCancelled);
        
        // 不再需要綁定點擊事件，因為 SpotFavorites 模組會處理
        // 只需確保按鈕不處於載入狀態
        const favoriteButtons = document.querySelectorAll('.spot-index-spot-card__favorite');
        favoriteButtons.forEach(button => {
            if (window.SpotCommon && window.SpotCommon.ensureButtonNormalState) {
                window.SpotCommon.ensureButtonNormalState(button);
            }
        });
        
        // 檢查收藏狀態
        checkInitialFavoriteStatus();
        
        // 監聽登入對話框取消事件
        document.addEventListener('loginDialogCancelled', handleLoginDialogCancelled);
    }
    
    /**
     * 處理登入對話框取消事件
     */
    function handleLoginDialogCancelled(e) {
        try {
            if (e.detail && e.detail.button) {
                const button = e.detail.button;
                const originalHTML = e.detail.originalHTML || '<span class="material-icons">favorite_border</span>';
                
                // 確保按鈕存在且仍在DOM中
                if (button && document.body.contains(button)) {
                    button.innerHTML = originalHTML;
                    button.disabled = false;
                    button.classList.remove('favorited');
                    button.title = '加入收藏';
                    console.log('登入對話框已取消，恢復按鈕狀態', button);
                } else {
                    console.warn('按鈕不存在或已從DOM中移除');
                }
            } else {
                console.warn('事件詳情中缺少按鈕信息');
            }
            
            // 額外的安全措施：檢查所有處於載入狀態的按鈕
            document.querySelectorAll('.spot-index-spot-card__favorite .loading-spinner').forEach(spinner => {
                const button = spinner.closest('.spot-index-spot-card__favorite');
                if (button) {
                    button.innerHTML = '<span class="material-icons">favorite_border</span>';
                    button.disabled = false;
                    button.classList.remove('favorited');
                    button.title = '加入收藏';
                    console.log('發現並修復處於載入狀態的按鈕', button);
                }
            });
        } catch (error) {
            console.error('處理登入對話框取消事件時出錯', error);
        }
    }
    
    /**
     * 檢查初始收藏狀態
     */
    function checkInitialFavoriteStatus() {
        if (!window.SpotCommon) return;
        
        // 獲取所有景點ID
        const spotCards = document.querySelectorAll('.spot-index-spot-card');
        const spotIds = Array.from(spotCards)
            .map(card => card.getAttribute('data-spot-id'))
            .filter(id => id); // 過濾掉空值
            
        if (spotIds.length === 0) return;
        
        // 批量檢查收藏狀態
        window.SpotCommon.checkBatchFavoriteStatus(spotIds)
            .then(statusMap => {
                if (!statusMap) return;
                
                // 更新按鈕狀態
                spotIds.forEach(spotId => {
                    const isFavorited = statusMap[spotId];
                    if (isFavorited !== undefined) {
                        const button = document.querySelector(`.spot-index-spot-card__favorite[data-spot-id="${spotId}"]`);
                        if (button && window.SpotFavorites) {
                            window.SpotFavorites.updateFavoriteButtonState(button, isFavorited);
                        }
                    }
                });
            })
            .catch(error => console.error('檢查收藏狀態失敗:', error));
    }
    
    /**
     * 顯示提示訊息 - 已停用，不再顯示任何提示
     */
    function showToast(message, type = 'info') {
        // 提示訊息功能已停用
        console.log('提示訊息(已停用顯示):', message, type);
        return null;
    }
    
    /**
     * 關閉提示訊息
     */
    function closeToast(toast) {
        if (!toast || !document.body.contains(toast)) return;
        
        // 清除自動關閉的timeout
        if (toast.dataset.timeoutId) {
            clearTimeout(parseInt(toast.dataset.timeoutId));
        }
        
        // 移除顯示類
        toast.classList.remove('toast--show');
        
        // 動畫結束後移除元素
        setTimeout(() => {
            if (document.body.contains(toast)) {
                toast.remove();
            }
        }, 300);
    }
    
    /**
     * 監聽收藏狀態變更事件
     */
    function listenToFavoriteChanges() {
        window.addEventListener('storage', function(e) {
            if (e.key === 'spotFavoriteChange') {
                try {
                    const data = JSON.parse(e.newValue);
                    if (data && data.spotId) {
                        // 更新對應的收藏按鈕
                        const button = document.querySelector(`.spot-index-spot-card__favorite[data-spot-id="${data.spotId}"]`);
                        if (button) {
                            updateFavoriteButtonState(button, data.isFavorited);
                        }
                    }
                } catch (error) {
                    console.error('處理收藏變更事件失敗:', error);
                }
            }
        });
    }

    /**
     * 載入初始景點資料
     */
    function loadInitialSpots() {
        // 從現有的HTML中提取景點資料
        const spotCards = document.querySelectorAll('.spot-index-spot-card');
        allSpots = Array.from(spotCards).map((card, index) => {
            const nameEl = card.querySelector('.spot-index-spot-card__name');
            const locationEl = card.querySelector('.spot-index-spot-card__location span:last-child');
            const ratingEl = card.querySelector('.spot-index-spot-card__rating span:last-child');
            const descEl = card.querySelector('.spot-index-spot-card__desc');
            const linkEl = card.querySelector('.spot-index-spot-card__link');
            const imageEl = card.querySelector('.spot-index-spot-card__image img');
            const categoryEl = card.querySelector('.spot-index-spot-card__category-badge');
            
            const location = locationEl ? locationEl.textContent.trim() : '';
            return {
                id: index + 1,
                name: nameEl ? nameEl.textContent.trim() : '',
                location: location,
                rating: ratingEl ? parseFloat(ratingEl.textContent.trim()) : 4.5,
                description: descEl ? descEl.textContent.trim() : '',
                link: linkEl ? linkEl.href : '',
                image: imageEl ? imageEl.src : '',
                category: categoryEl ? categoryEl.textContent.trim() : '熱門',
                region: getRegionFromLocation(location),
                ticketType: Math.random() > 0.5 ? 'free' : 'paid', // 隨機分配門票類型
                type: getSpotType(index), // 根據索引分配景點類型
                element: card
            };
        });
        
        filteredSpots = [...allSpots];
        console.log('載入景點資料:', allSpots.length, '個景點');
        
        // 檢查地區分布
        const regionCount = {};
        allSpots.forEach(spot => {
            regionCount[spot.region] = (regionCount[spot.region] || 0) + 1;
        });
        console.log('地區分布:', regionCount);
    }

    /**
     * 根據地址推測地區（依據用戶提供的縣市對應表）
     */
    function getRegionFromLocation(location) {
        // 北台灣
        if (/台北市|新北市|基隆市|桃園市|新竹市|新竹縣|宜蘭縣/.test(location)) {
            return '北台灣';
        }
        // 中台灣
        if (/台中市|彰化縣|南投縣|雲林縣|苗栗縣/.test(location)) {
            return '中台灣';
        }
        // 南台灣
        if (/高雄市|台南市|嘉義市|嘉義縣|屏東縣|澎湖縣/.test(location)) {
            return '南台灣';
        }
        // 東台灣
        if (/花蓮縣|台東縣/.test(location)) {
            return '東台灣';
        }
        return '其他';
    }

    /**
     * 根據索引分配景點類型
     */
    function getSpotType(index) {
        const types = ['nature', 'culture', 'leisure', 'food'];
        return types[index % types.length];
    }

    /**
     * 初始化動態篩選功能
     */
    function initDynamicFilters() {
        // 監聽所有篩選下拉選單的變化
        const filterSelects = document.querySelectorAll('.spot-index-filter-select');
        filterSelects.forEach(select => {
            select.addEventListener('change', function(e) {
                e.preventDefault(); // 防止預設行為
                
                const filterType = this.getAttribute('data-filter-type');
                const filterValue = this.value;
                
                if (filterValue) {
                    // 添加篩選條件
                    addFilter(filterType, filterValue, this.options[this.selectedIndex].text);
                } else {
                    // 移除篩選條件
                    removeFilter(filterType);
                }
                
                // 立即應用篩選
                applyFiltersInstantly();
            });
        });
    }

    /**
     * 初始化分類按鈕
     */
    function initCategoryButtons() {
        const categoryButtons = document.querySelectorAll('.spot-index-category-item');
        categoryButtons.forEach(button => {
            button.addEventListener('click', function(e) {
                e.preventDefault(); // 防止預設行為
                e.stopPropagation(); // 防止事件冒泡
                
                // 移除其他按鈕的active狀態
                categoryButtons.forEach(btn => btn.classList.remove('active'));
                // 添加當前按鈕的active狀態
                this.classList.add('active');
                
                const category = this.textContent.trim();
                
                // 根據按鈕類型應用不同的篩選
                switch(category) {
                    case '熱門':
                        // 移除分類篩選，顯示所有景點
                        removeFilter('category');
                        // 按熱門度（評分）排序
                        addFilter('sort', 'popular', '熱門優先');
                        break;
                        
                    case '精選':
                        // 添加精選篩選（高評分景點）
                        addFilter('category', 'featured', '精選');
                        break;
                        
                    case '最新':
                        // 添加最新篩選
                        removeFilter('category'); // 先移除分類篩選
                        addFilter('sort', 'newest', '最新優先');
                        break;
                        
                    default:
                        // 其他分類按正常方式處理
                        addFilter('category', category, category);
                        break;
                }
                
                applyFiltersInstantly();
                
                return false; // 確保不會有任何導航行為
            });
        });
    }

    // 新增：地區切換時 AJAX 後端撈資料
    function fetchSpotsByRegion(region) {
        let url = '/api/spot/public/list';
        if (region && region !== '全部地區') {
            url += '?region=' + encodeURIComponent(region) + '&limit=6';
        } else {
            url += '?limit=6';
        }
        fetch(url)
            .then(res => res.json())
            .then(data => {
                if (data && data.success && Array.isArray(data.data)) {
                    renderSpots(data.data);
                } else {
                    renderSpots([]);
                }
            })
            .catch(() => renderSpots([]));
    }
    function renderSpots(spotArr) {
        const grid = document.querySelector('.spot-index-popular__grid');
        if (!grid) return;
        grid.innerHTML = '';
        if (!spotArr || spotArr.length === 0) {
            showEmptyState();
            return;
        } else {
            hideEmptyState && hideEmptyState();
        }
        spotArr.forEach(spot => {
            // 這裡建議用模板字串或複製現有卡片結構
            const card = document.createElement('div');
            card.className = 'spot-index-spot-card';
            card.innerHTML = `
                <div class="spot-index-spot-card__image">
                    <img src="${spot.firstPictureUrl || '/images/spot/default.jpg'}" alt="${spot.spotName}" class="spot-index-spot-card__img" loading="lazy">
                </div>
                <div class="spot-index-spot-card__content">
                    <h3 class="spot-index-spot-card__name">${spot.spotName}</h3>
                    <p class="spot-index-spot-card__location"><span class="material-icons">place</span>${spot.spotLoc || ''}</p>
                    <p class="spot-index-spot-card__desc">${spot.spotDesc ? (spot.spotDesc.length > 60 ? spot.spotDesc.substring(0, 60) + '...' : spot.spotDesc) : '暫無描述'}</p>
                    <a href="/spot/user/detail/${spot.spotId}" class="spot-index-spot-card__link">查看詳情</a>
                </div>
            `;
            grid.appendChild(card);
        });
    }
    // 切換地區時更新網址參數
    function updateRegionUrl(region) {
        const params = new URLSearchParams(window.location.search);
        params.set('region', region);
        history.replaceState({}, '', `${location.pathname}?${params}`);
    }
    // 攔截地區按鈕點擊
    function initRegionButtons() {
        const regionButtons = document.querySelectorAll('.spot-index-region-item');
        regionButtons.forEach(button => {
            button.addEventListener('click', function(e) {
                e.preventDefault();
                e.stopPropagation();
                regionButtons.forEach(btn => btn.classList.remove('active'));
                this.classList.add('active');
                // 取得地區文字
                const textNodes = Array.from(this.childNodes).filter(node => node.nodeType === Node.TEXT_NODE);
                const region = textNodes.map(node => node.textContent.trim()).join('').trim();
                // AJAX 撈資料
                fetchSpotsByRegion(region);
                // 更新網址
                updateRegionUrl(region);
                return false;
            });
        });
    }
    // 頁面初始載入時根據 URL 參數自動載入對應地區
    document.addEventListener('DOMContentLoaded', function() {
        const params = new URLSearchParams(window.location.search);
        const region = params.get('region') || '全部地區';
        // 設定對應按鈕 active
        const regionButtons = document.querySelectorAll('.spot-index-region-item');
        regionButtons.forEach(btn => {
            const textNodes = Array.from(btn.childNodes).filter(node => node.nodeType === Node.TEXT_NODE);
            const btnRegion = textNodes.map(node => node.textContent.trim()).join('').trim();
            if (btnRegion === region) {
                btn.classList.add('active');
            } else {
                btn.classList.remove('active');
            }
        });
        fetchSpotsByRegion(region);
    });

    /**
     * 初始化排序功能
     */
    function initSortFunctionality() {
        const sortSelect = document.querySelector('.spot-index-sort-select');
        if (sortSelect) {
            sortSelect.addEventListener('change', function(e) {
                e.preventDefault(); // 防止預設行為
                
                const sortValue = this.value;
                const sortText = this.options[this.selectedIndex].text;
                addFilter('sort', sortValue, sortText);
                applyFiltersInstantly();
            });
        }
    }

    /**
     * 添加篩選條件
     */
    function addFilter(type, value, displayText) {
        currentFilters[type] = {
            value: value,
            displayText: displayText
        };
        
        updateFilterTags();
        showToast(`已添加篩選條件：${displayText}`, 'success');
    }

    /**
     * 移除篩選條件
     */
    function removeFilter(type) {
        if (currentFilters[type]) {
            delete currentFilters[type];
            updateFilterTags();
            resetFilterControl(type);
            showToast('已移除篩選條件', 'info');
        }
    }

    /**
     * 立即應用篩選
     */
    function applyFiltersInstantly() {
        // 重置為所有景點
        filteredSpots = [...allSpots];
        
        // 應用各種篩選條件
        Object.keys(currentFilters).forEach(filterType => {
            const filter = currentFilters[filterType];
            
            switch(filterType) {
                case 'rating':
                    const minRating = parseFloat(filter.value);
                    filteredSpots = filteredSpots.filter(spot => spot.rating >= minRating);
                    break;
                    

                    
                case 'ticket':
                    filteredSpots = filteredSpots.filter(spot => spot.ticketType === filter.value);
                    break;
                    
                case 'type':
                    filteredSpots = filteredSpots.filter(spot => spot.type === filter.value);
                    break;
                    
                case 'category':
                    // 分類篩選 - 根據分類名稱篩選
                    if (filter.value === 'featured') {
                        // 精選景點 - 評分高於4.5的景點
                        filteredSpots = filteredSpots.filter(spot => spot.rating >= 4.5);
                    } else {
                        // 其他分類按正常方式處理
                        filteredSpots = filteredSpots.filter(spot => 
                            spot.category === filter.value || 
                            spot.type === filter.value.toLowerCase()
                        );
                    }
                    break;
                    
                case 'region':
                    filteredSpots = filteredSpots.filter(spot => spot.region === filter.value);
                    break;
            }
        });
        
        // 應用排序
        if (currentFilters.sort) {
            applySorting(currentFilters.sort.value);
        }
        
        // 更新顯示
        updateSpotDisplay();
        updateStatsDisplay();
    }

    /**
     * 應用排序
     */
    function applySorting(sortType) {
        switch(sortType) {
            case 'popular':
                // 按熱門度排序（可以根據收藏數、瀏覽數等）
                filteredSpots.sort((a, b) => b.rating - a.rating);
                break;
            case 'rating':
                // 按評分排序
                filteredSpots.sort((a, b) => b.rating - a.rating);
                break;
            case 'newest':
                // 按最新排序
                filteredSpots.sort((a, b) => b.id - a.id);
                break;

        }
    }

    /**
     * 更新景點顯示
     */
    function updateSpotDisplay() {
        const grid = document.querySelector('.spot-index-popular__grid');
        if (!grid) return;
        grid.innerHTML = '';
        const input = document.getElementById('searchInput');
        const keyword = input && input.value.trim();
        let spotsToShow = filteredSpots;
        // 判斷是否有搜尋
        if (!keyword) {
            // 檢查目前有無地區篩選
            const activeRegionBtn = document.querySelector('.spot-index-region-item.active');
            let region = '';
            if (activeRegionBtn) {
                // 取得按鈕文字（排除 icon）
                const textNodes = Array.from(activeRegionBtn.childNodes).filter(node => node.nodeType === Node.TEXT_NODE);
                region = textNodes.map(node => node.textContent.trim()).join('').trim();
            }
            if (region && region !== '全部地區') {
                // 只顯示該地區前6筆
                spotsToShow = filteredSpots.filter(spot => spot.region === region).slice(0, 6);
            } else {
                // 全部地區只顯示前6筆
                spotsToShow = filteredSpots.slice(0, 6);
            }
        }
        if (spotsToShow.length === 0) {
            showEmptyState();
            return;
        } else {
            hideEmptyState && hideEmptyState();
        }
        spotsToShow.forEach(spot => {
            if (spot.element) grid.appendChild(spot.element);
        });
    }

    /**
     * 更新統計資訊顯示
     */
    function updateStatsDisplay() {
        const statsElement = document.querySelector('.spot-index-stats__text');
        if (statsElement) {
            const displayCount = Math.min(filteredSpots.length, 8);
            const totalCount = allSpots.length;
            const remainingCount = totalCount - displayCount;
            
            if (filteredSpots.length === 0) {
                statsElement.textContent = '沒有符合條件的景點';
            } else {
                statsElement.textContent = `已顯示 ${displayCount} 個景點，還有 ${remainingCount} 個等你探索`;
            }
        }
    }

    /**
     * 顯示空狀態
     */
    function showEmptyState() {
        const emptyState = document.querySelector('.spot-index-empty');
        const spotGrid = document.querySelector('.spot-index-popular__grid');
        const statsElement = document.querySelector('.spot-index-stats');
        
        if (emptyState && spotGrid && statsElement) {
            // 創建篩選專用的空狀態
            let filterEmptyState = document.querySelector('.spot-index-filter-empty');
            if (!filterEmptyState) {
                filterEmptyState = document.createElement('div');
                filterEmptyState.className = 'spot-index-empty spot-index-filter-empty';
                filterEmptyState.innerHTML = `
                    <div class="spot-index-empty__icon">
                        <span class="material-icons">search_off</span>
                    </div>
                    <h3 class="spot-index-empty__title">沒有符合條件的景點</h3>
                    <p class="spot-index-empty__desc">請嘗試調整篩選條件，或者重置所有篩選</p>
                    <button class="spot-index-btn spot-index-btn--primary" onclick="resetAllFilters()">
                        <span class="material-icons">refresh</span>
                        重置篩選
                    </button>
                `;
                spotGrid.parentNode.insertBefore(filterEmptyState, spotGrid);
            }
            
            spotGrid.style.display = 'none';
            statsElement.style.display = 'none';
            filterEmptyState.style.display = 'block';
        }
    }

    /**
     * 隱藏空狀態
     */
    function hideEmptyState() {
        const filterEmptyState = document.querySelector('.spot-index-filter-empty');
        const spotGrid = document.querySelector('.spot-index-popular__grid');
        const statsElement = document.querySelector('.spot-index-stats');
        
        if (filterEmptyState) {
            filterEmptyState.style.display = 'none';
        }
        if (spotGrid) {
            spotGrid.style.display = 'grid';
        }
        if (statsElement) {
            statsElement.style.display = 'block';
        }
    }

    /**
     * 更新篩選標籤顯示
     */
    function updateFilterTags() {
        const tagsContainer = document.getElementById('filterTagsContainer');
        const tagsList = document.getElementById('filterTagsList');
        
        if (!tagsContainer || !tagsList) return;
        
        // 清空現有標籤
        tagsList.innerHTML = '';
        
        // 如果沒有篩選條件，隱藏整個區域
        if (Object.keys(currentFilters).length === 0) {
            tagsContainer.style.display = 'none';
            return;
        }
        
        // 顯示篩選標籤區域
        tagsContainer.style.display = 'block';
        
        // 添加篩選標籤
        Object.keys(currentFilters).forEach(filterType => {
            const filter = currentFilters[filterType];
            const tag = document.createElement('span');
            tag.className = 'spot-index-filter-tag';
            tag.innerHTML = `
                <span class="spot-index-filter-tag__label">${getFilterTypeLabel(filterType)}</span>
                <span class="spot-index-filter-tag__value">${filter.displayText}</span>
                <button class="spot-index-filter-tag__remove" onclick="removeFilterTag('${filterType}')" title="移除此篩選條件">
                    <span class="material-icons">close</span>
                </button>
            `;
            tagsList.appendChild(tag);
        });
    }

    /**
     * 獲取篩選類型的顯示標籤
     */
    function getFilterTypeLabel(filterType) {
        const labels = {
            'rating': '評分',
            'ticket': '門票',
            'type': '類型',
            'category': '分類',
            'region': '地區',
            'sort': '排序'
        };
        return labels[filterType] || filterType;
    }

    /**
     * 重置特定篩選控制項
     */
    function resetFilterControl(filterType) {
        // 重置下拉選單
        const select = document.querySelector(`[data-filter-type="${filterType}"]`);
        if (select) {
            select.value = '';
        }
        
        // 重置分類按鈕
        if (filterType === 'category') {
            const categoryButtons = document.querySelectorAll('.spot-index-category-item');
            categoryButtons.forEach(btn => btn.classList.remove('active'));
            // 預設選中第一個按鈕（熱門）
            if (categoryButtons.length > 0) {
                categoryButtons[0].classList.add('active');
            }
        }
        
        // 重置地區按鈕
        if (filterType === 'region') {
            const regionButtons = document.querySelectorAll('.spot-index-region-item');
            regionButtons.forEach(btn => btn.classList.remove('active'));
            // 預設選中第一個按鈕（全部地區）
            if (regionButtons.length > 0) {
                regionButtons[0].classList.add('active');
            }
        }
        
        // 重置排序選擇
        if (filterType === 'sort') {
            const sortSelect = document.querySelector('.spot-index-sort-select');
            if (sortSelect) {
                sortSelect.value = 'popular';
            }
        }
    }

    /**
     * 移除單個篩選標籤
     */
    window.removeFilterTag = function(filterType) {
        removeFilter(filterType);
        applyFiltersInstantly();
    };

    /**
     * 重置所有篩選
     */
    window.resetAllFilters = function() {
        // 清空所有篩選條件
        currentFilters = {};
        
        // 重置所有控制項
        const filterSelects = document.querySelectorAll('.spot-index-filter-select');
        filterSelects.forEach(select => {
            select.value = '';
        });
        
        // 重置分類按鈕
        const categoryButtons = document.querySelectorAll('.spot-index-category-item');
        categoryButtons.forEach(btn => btn.classList.remove('active'));
        if (categoryButtons.length > 0) {
            categoryButtons[0].classList.add('active');
        }
        
        // 重置地區按鈕
        const regionButtons = document.querySelectorAll('.spot-index-region-item');
        regionButtons.forEach(btn => btn.classList.remove('active'));
        if (regionButtons.length > 0) {
            regionButtons[0].classList.add('active');
        }
        
        // 重置排序選擇
        const sortSelect = document.querySelector('.spot-index-sort-select');
        if (sortSelect) {
            sortSelect.value = 'popular';
        }
        
        // 顯示所有景點
        filteredSpots = [...allSpots];
        updateSpotDisplay();
        updateStatsDisplay();
        updateFilterTags();
        
        showToast('已重置所有篩選條件', 'success');
    };

    // === 新增：首頁搜尋功能 ===
    function initSearchBox() {
        const input = document.getElementById('searchInput');
        const btn = document.querySelector('.spot-index-search-btn');
        if (!input || !btn) return;
        // 按下 Enter 搜尋
        input.addEventListener('keydown', function(e) {
            if (e.key === 'Enter') {
                doSearch();
            }
        });
        // 點擊按鈕搜尋
        btn.addEventListener('click', doSearch);
    }
    function doSearch() {
        const input = document.getElementById('searchInput');
        if (!input) return;
        const keyword = input.value.trim();
        if (!keyword) {
            filteredSpots = [...allSpots];
        } else {
            filteredSpots = allSpots.filter(spot =>
                spot.name.includes(keyword) ||
                spot.location.includes(keyword) ||
                spot.description.includes(keyword)
            );
        }
        updateSpotDisplay();
        updateStatsDisplay && updateStatsDisplay();
    }
    // === end ===

    /**
     * 初始化滾動動畫
     */
    function initScrollAnimations() {
        if (prefersReducedMotion) return;
        
        const observerOptions = {
            threshold: 0.1,
            rootMargin: '0px 0px -50px 0px'
        };
        
        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.classList.add('animate-in');
                }
            });
        }, observerOptions);
        
        // 觀察所有需要動畫的元素
        const animateElements = document.querySelectorAll('.spot-index-hero, .spot-index-search, .spot-index-spot-card');
        animateElements.forEach(el => observer.observe(el));
    }

    /**
     * 初始化卡片懸停效果
     */
    function initCardHoverEffects() {
        const cards = document.querySelectorAll('.spot-index-spot-card');
        
        cards.forEach(card => {
            card.addEventListener('mouseenter', function() {
                if (!prefersReducedMotion) {
                    this.style.transform = 'translateY(-5px)';
                    this.style.boxShadow = '0 10px 25px rgba(0,0,0,0.15)';
                }
            });
            
            card.addEventListener('mouseleave', function() {
                if (!prefersReducedMotion) {
                    this.style.transform = 'translateY(0)';
                    this.style.boxShadow = '';
                }
            });
        });
    }

    /**
     * 初始化按鈕效果
     */
    function initButtonEffects() {
        const buttons = document.querySelectorAll('.spot-index-btn, .spot-index-category-item, .spot-index-region-item');
        
        buttons.forEach(button => {
            button.addEventListener('click', function(e) {
                // 創建漣漪效果
                if (!prefersReducedMotion) {
                    createRippleEffect(this, e);
                }
            });
        });
    }

    /**
     * 創建漣漪效果
     */
    function createRippleEffect(element, event) {
        const ripple = document.createElement('span');
        const rect = element.getBoundingClientRect();
        const size = Math.max(rect.width, rect.height);
        const x = event.clientX - rect.left - size / 2;
        const y = event.clientY - rect.top - size / 2;
        
        ripple.style.cssText = `
            position: absolute;
            border-radius: 50%;
            background: rgba(255, 255, 255, 0.6);
            transform: scale(0);
            animation: ripple 0.6s linear;
            left: ${x}px;
            top: ${y}px;
            width: ${size}px;
            height: ${size}px;
            pointer-events: none;
        `;
        
        element.style.position = 'relative';
        element.style.overflow = 'hidden';
        element.appendChild(ripple);
        
        setTimeout(() => {
            ripple.remove();
        }, 600);
    }

    /**
     * 初始化延遲載入
     */
    function initLazyLoading() {
        const images = document.querySelectorAll('img[loading="lazy"]');
        
        if ('IntersectionObserver' in window) {
            const imageObserver = new IntersectionObserver((entries) => {
                entries.forEach(entry => {
                    if (entry.isIntersecting) {
                        const img = entry.target;
                        img.src = img.dataset.src || img.src;
                        img.classList.remove('lazy');
                        imageObserver.unobserve(img);
                    }
                });
            });
            
            images.forEach(img => imageObserver.observe(img));
        }
    }

    /**
     * 初始化鍵盤導航
     */
    function initKeyboardNavigation() {
        // 處理Tab鍵導航
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Tab') {
                document.body.classList.add('keyboard-navigation');
            }
        });
        
        document.addEventListener('mousedown', function() {
            document.body.classList.remove('keyboard-navigation');
        });
    }

    /**
     * 初始化無障礙功能
     */
    function initAccessibility() {
        // 為動態添加的元素添加適當的ARIA標籤
        const filterSelects = document.querySelectorAll('.spot-index-filter-select');
        filterSelects.forEach(select => {
            if (!select.getAttribute('aria-label')) {
                const label = select.previousElementSibling;
                if (label) {
                    select.setAttribute('aria-label', label.textContent);
                }
            }
        });
    }

    /**
     * 淡入內容
     */
    function fadeInContent() {
        const elements = document.querySelectorAll('.spot-index-hero, .spot-index-search, .spot-index-popular');
        elements.forEach((el, index) => {
            setTimeout(() => {
                el.style.opacity = '1';
                el.style.transform = 'translateY(0)';
            }, index * 200);
        });
    }

    /**
     * 獲取提示圖標
     */
    function getToastIcon(type) {
        switch (type) {
            case 'success': return 'check_circle';
            case 'error': return 'error';
            case 'warning': return 'warning';
            default: return 'info';
        }
    }

    // 執行初始化
    init();

})(); 