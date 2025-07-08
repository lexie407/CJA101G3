/**
 * 景點列表頁 JavaScript
 * @version 1.0
 * @description 提供景點列表頁的交互效果和用戶體驗增強
 */

// 全域變數和函數
let currentView = 'grid';
let currentSort = 'name';
let favorites = JSON.parse(localStorage.getItem('spotFavorites') || '[]');
let bookmarks = JSON.parse(localStorage.getItem('spotBookmarks') || '[]');
let currentSortOrder = 'asc'; // 預設升序

// 將這些函數暴露到全局作用域
window.handleFilter = function() {
    const regionSelect = document.getElementById('regionFilter');
    const selectedRegion = regionSelect.value;
    
    // 獲取當前的搜尋關鍵字
    const searchKeyword = document.getElementById('searchKeyword').value;
    
    // 構建URL
    let url = '/spot/user/list?';
    const params = new URLSearchParams();
    
    if (selectedRegion) {
        params.append('region', selectedRegion);
    }
    
    if (searchKeyword) {
        params.append('keyword', searchKeyword);
    }
    
    // 重定向到新的URL
    window.location.href = url + params.toString();
};

window.handleSearch = function(event) {
    if (event.key === 'Enter') {
        window.performSearch();
    }
};

window.performSearch = function() {
    const searchKeyword = document.getElementById('searchKeyword').value;
    const regionSelect = document.getElementById('regionFilter');
    const selectedRegion = regionSelect.value;
    
    // 構建URL
    let url = '/spot/user/list?';
    const params = new URLSearchParams();
    
    if (searchKeyword) {
        params.append('keyword', searchKeyword);
    }
    
    if (selectedRegion) {
        params.append('region', selectedRegion);
    }
    
    // 重定向到新的URL
    window.location.href = url + params.toString();
};

// 主要初始化函數
(function() {
    'use strict';

    // 檢查動畫偏好
    const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

    /**
     * 初始化所有功能
     */
    function init() {
        initScrollAnimations();
        initCardHoverEffects();
        initButtonEffects();
        initLazyLoading();
        initKeyboardNavigation();
        initAccessibility();
        initSearchForm();
        initViewControls();
        initSortControls();
        initFavoriteSystem();
        initAlertSystem();
        
        // 頁面載入完成後的效果
        document.addEventListener('DOMContentLoaded', function() {
            if (!prefersReducedMotion) {
                fadeInContent();
                animateCards();
            }
            loadFavoriteStates();
            updateStatistics();
        });
    }

    /**
     * 捲動動畫
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
                    entry.target.style.opacity = '1';
                    entry.target.style.transform = 'translateY(0)';
                }
            });
        }, observerOptions);

        // 觀察所有卡片
        document.querySelectorAll('.spot-list-item').forEach((item, index) => {
            item.style.opacity = '0';
            item.style.transform = 'translateY(20px)';
            item.style.transition = `opacity 0.6s ease ${index * 0.1}s, transform 0.6s ease ${index * 0.1}s`;
            observer.observe(item);
        });
    }

    /**
     * 卡片懸停效果
     */
    function initCardHoverEffects() {
        if (prefersReducedMotion) return;

        document.querySelectorAll('.spot-list-card').forEach(card => {
            card.addEventListener('mouseenter', function() {
                this.style.transform = 'translateY(-4px)';
                const img = this.querySelector('.spot-list-card__img');
                if (img) {
                    img.style.transform = 'scale(1.05)';
                }
            });

            card.addEventListener('mouseleave', function() {
                this.style.transform = 'translateY(0)';
                const img = this.querySelector('.spot-list-card__img');
                if (img) {
                    img.style.transform = 'scale(1)';
                }
            });
        });
    }

    /**
     * 按鈕效果
     */
    function initButtonEffects() {
        // 搜尋按鈕效果
        const searchBtn = document.querySelector('.spot-list-search-btn');
        if (searchBtn) {
            searchBtn.addEventListener('click', function(e) {
                if (!prefersReducedMotion) {
                    this.style.transform = 'scale(0.95)';
                    setTimeout(() => {
                        this.style.transform = 'scale(1)';
                    }, 150);
                }
            });
        }

        // 其他按鈕的點擊效果
        document.querySelectorAll('.spot-list-btn, .spot-list-add-btn, .spot-list-card__link').forEach(btn => {
            btn.addEventListener('click', function(e) {
                if (!prefersReducedMotion) {
                    this.style.transform = 'translateY(-1px)';
                    setTimeout(() => {
                        this.style.transform = 'translateY(0)';
                    }, 200);
                }
            });
        });
    }

    /**
     * 圖片懶加載
     */
    function initLazyLoading() {
        const imageObserver = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    const img = entry.target;
                    if (img.dataset.src) {
                        img.src = img.dataset.src;
                        img.removeAttribute('data-src');
                        imageObserver.unobserve(img);
                    }
                }
            });
        });

        document.querySelectorAll('img[data-src]').forEach(img => {
            imageObserver.observe(img);
        });
    }

    /**
     * 鍵盤導覽
     */
    function initKeyboardNavigation() {
        document.addEventListener('keydown', function(e) {
            // ESC 鍵關閉警告框
            if (e.key === 'Escape') {
                const alerts = document.querySelectorAll('.spot-list-alert');
                alerts.forEach(alert => {
                    closeAlert(alert);
                });
            }

            // Enter 鍵執行搜尋
            if (e.key === 'Enter' && e.target.matches('.spot-list-search-input')) {
                e.preventDefault();
                const form = e.target.closest('form');
                if (form) {
                    form.submit();
                }
            }
        });

        // 為卡片添加焦點效果
        document.querySelectorAll('.spot-list-card').forEach(card => {
            const link = card.querySelector('.spot-list-card__link');
            if (link) {
                link.addEventListener('focus', function() {
                    card.style.outline = '2px solid var(--md-sys-color-primary)';
                    card.style.outlineOffset = '2px';
                });

                link.addEventListener('blur', function() {
                    card.style.outline = 'none';
                });
            }
        });
    }

    /**
     * 無障礙設計
     */
    function initAccessibility() {
        // 為動態內容添加 ARIA 標籤
        const grid = document.getElementById('spotGrid');
        if (grid) {
            grid.setAttribute('role', 'grid');
            grid.setAttribute('aria-label', '景點列表');
        }

        // 為卡片添加 ARIA 標籤
        document.querySelectorAll('.spot-list-card').forEach((card, index) => {
            card.setAttribute('role', 'gridcell');
            card.setAttribute('aria-label', `景點 ${index + 1}`);
        });

        // 為篩選控制項添加 ARIA 標籤
        const sortSelect = document.getElementById('sortBy');
        if (sortSelect) {
            sortSelect.setAttribute('aria-label', '選擇排序方式');
        }

        // 高對比度支援
        if (window.matchMedia('(prefers-contrast: high)').matches) {
            document.body.classList.add('high-contrast');
        }
    }

    /**
     * 初始化搜尋表單
     */
    function initSearchForm() {
        const searchForm = document.querySelector('.spot-list-search-form');
        const searchInput = document.querySelector('#keyword');
        const regionSelect = document.querySelector('#region');

        if (searchForm) {
            searchForm.addEventListener('submit', function(e) {
                const keyword = searchInput.value.trim();
                const region = regionSelect.value;

                // 如果關鍵字和地區都是空的，顯示提示
                if (!keyword && !region) {
                    e.preventDefault();
                    showToast('請輸入搜尋關鍵字或選擇地區', 'warning');
                    return;
                }
            });
        }

        // 重置按鈕功能
        const resetBtn = document.querySelector('.spot-list-reset-btn');
        if (resetBtn) {
            resetBtn.addEventListener('click', function() {
                if (searchInput) searchInput.value = '';
                if (regionSelect) regionSelect.value = '';
                showToast('已重置搜尋條件', 'info');
            });
        }
    }

    /**
     * 顯示搜尋提示
     * @param {string} message - 提示訊息
     * @param {string} type - 提示類型 (searching/success/error/warning/info)
     */
    function showToast(message, type = 'info') {
        // 移除舊的提示
        const oldToast = document.querySelector('.spot-list-toast');
        if (oldToast) {
            oldToast.remove();
        }

        // 創建新的提示
        const toast = document.createElement('div');
        toast.className = `spot-list-toast spot-list-toast--${type}`;
        
        // 根據類型設置圖標
        let icon = 'info';
        switch (type) {
            case 'searching':
                icon = 'search';
                break;
            case 'success':
                icon = 'check_circle';
                break;
            case 'error':
                icon = 'error';
                break;
            case 'warning':
                icon = 'warning';
                break;
        }
        
        toast.innerHTML = `
            <span class="material-icons">${icon}</span>
            <span>${message}</span>
        `;

        // 添加到頁面
        document.body.appendChild(toast);

        // 自動隱藏（除了"正在搜尋"狀態）
        if (type !== 'searching') {
            setTimeout(() => {
                toast.classList.add('spot-list-toast--hide');
                setTimeout(() => toast.remove(), 300);
            }, 3000);
        }

        return toast;
    }

    /**
     * 視圖控制
     */
    function initViewControls() {
        const viewButtons = document.querySelectorAll('.spot-list-view-btn');
        const grid = document.getElementById('spotGrid');

        viewButtons.forEach(btn => {
            btn.addEventListener('click', function() {
                const view = this.dataset.view;
                switchView(view);
                
                // 更新按鈕狀態
                viewButtons.forEach(b => b.classList.remove('active'));
                this.classList.add('active');
            });
        });

        function switchView(view) {
            if (!grid) return;

            currentView = view;
            
            if (view === 'list') {
                grid.classList.add('list-view');
                grid.style.gridTemplateColumns = '1fr';
            } else {
                grid.classList.remove('list-view');
                grid.style.gridTemplateColumns = 'repeat(auto-fill, minmax(350px, 1fr))';
            }

            // 儲存用戶偏好
            localStorage.setItem('spotListView', view);
            
            showToast(`已切換到${view === 'grid' ? '網格' : '列表'}檢視`, 'success');
        }

        // 載入用戶偏好
        const savedView = localStorage.getItem('spotListView');
        if (savedView && savedView !== currentView) {
            const btn = document.querySelector(`[data-view="${savedView}"]`);
            if (btn) {
                btn.click();
            }
        }
    }

    /**
     * 排序控制
     */
    function initSortControls() {
        const sortSelect = document.getElementById('sortBy');
        
        if (sortSelect) {
            sortSelect.addEventListener('change', function() {
                const sortValue = this.value;
                sortSpots(sortValue);
            });
        }
    }

    /**
     * 收藏系統
     */
    function initFavoriteSystem() {
        // 收藏按鈕事件
        document.querySelectorAll('.spot-list-card__favorite').forEach(btn => {
            btn.addEventListener('click', function(e) {
                e.preventDefault();
                e.stopPropagation();
                toggleFavorite(this);
            });
        });

        // 書籤按鈕事件
        document.querySelectorAll('.spot-list-card__bookmark-btn').forEach(btn => {
            btn.addEventListener('click', function(e) {
                e.preventDefault();
                e.stopPropagation();
                const spotId = this.dataset.spotId;
                toggleBookmark(spotId, this);
            });
        });
        
        // 監聽跨頁面收藏狀態變更
        window.addEventListener('storage', function(e) {
            if (e.key === 'spotFavoriteChange') {
                const data = JSON.parse(e.newValue);
                const spotId = data.spotId;
                const isFavorited = data.isFavorited;
                
                // 更新對應景點的收藏狀態
                const favoriteBtn = document.querySelector(`.spot-list-card__favorite[data-spot-id="${spotId}"]`);
                if (favoriteBtn) {
                    const icon = favoriteBtn.querySelector('.material-icons');
                    
                    if (isFavorited) {
                        favoriteBtn.classList.add('active');
                        icon.textContent = 'favorite';
                        favoriteBtn.title = '取消收藏';
                        if (!favorites.includes(spotId)) {
                            favorites.push(spotId);
                        }
                    } else {
                        favoriteBtn.classList.remove('active');
                        icon.textContent = 'favorite_border';
                        favoriteBtn.title = '加入收藏';
                        favorites = favorites.filter(id => id !== spotId);
                    }
                    
                    // 更新本地儲存
                    localStorage.setItem('spotFavorites', JSON.stringify(favorites));
                }
            }
        });
    }

    /**
     * 警告系統
     */
    function initAlertSystem() {
        // 關閉按鈕事件
        document.querySelectorAll('.spot-list-alert__close').forEach(btn => {
            btn.addEventListener('click', function() {
                const alert = this.closest('.spot-list-alert');
                closeAlert(alert);
            });
        });

        // 自動關閉成功訊息
        setTimeout(() => {
            const successAlerts = document.querySelectorAll('.spot-list-alert--success');
            successAlerts.forEach(alert => {
                closeAlert(alert);
            });
        }, 5000);
    }

    /**
     * 頁面載入動畫
     */
    function fadeInContent() {
        const sections = document.querySelectorAll('.spot-list-header, .spot-list-search-section, .spot-list-toolbar');
        sections.forEach((section, index) => {
            section.style.opacity = '0';
            section.style.transform = 'translateY(20px)';
            setTimeout(() => {
                section.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
                section.style.opacity = '1';
                section.style.transform = 'translateY(0)';
            }, index * 200);
        });
    }

    /**
     * 卡片動畫
     */
    function animateCards() {
        const cards = document.querySelectorAll('.spot-list-item');
        cards.forEach((card, index) => {
            card.style.setProperty('--animation-order', index);
        });
    }

    /**
     * 載入收藏狀態
     */
    function loadFavoriteStates() {
        // 載入收藏狀態
        document.querySelectorAll('.spot-list-card__favorite').forEach(btn => {
            const spotId = btn.dataset.spotId;
            if (favorites.includes(spotId)) {
                btn.classList.add('active');
                btn.querySelector('.material-icons').textContent = 'favorite';
                btn.title = '取消收藏';
            }
        });

        // 載入書籤狀態
        document.querySelectorAll('.spot-list-card__bookmark-btn').forEach(btn => {
            const spotId = btn.dataset.spotId;
            if (bookmarks.includes(spotId)) {
                btn.classList.add('active');
                btn.querySelector('.material-icons').textContent = 'bookmark';
                btn.title = '取消收藏';
            }
        });
    }

    /**
     * 更新統計資訊
     */
    function updateStatistics() {
        const statsText = document.querySelector('.spot-list-stats__text');
        const cards = document.querySelectorAll('.spot-list-card');
        
        if (statsText && cards.length > 0) {
            const count = cards.length;
            statsText.innerHTML = `共 <strong>${count}</strong> 個景點`;
        }
    }

    // 全域函數（供 HTML 調用）
    window.toggleFavorite = function(element) {
        const spotId = element.dataset.spotId;
        
        // 檢查參數
        if (!element || !spotId) {
            console.error('收藏功能參數錯誤:', element, spotId);
            return;
        }
        
        // 檢查按鈕是否已被禁用，避免重複點擊
        if (element.disabled || element.classList.contains('loading')) {
            return;
        }
        
        const icon = element.querySelector('.material-icons');
        const isActive = element.classList.contains('active');
        const originalText = icon ? icon.textContent : 'favorite_border';

        // 禁用按鈕，防止重複點擊
        element.disabled = true;
        
        // 顯示載入狀態
        if (icon) {
            icon.textContent = 'sync';
        }
        element.classList.add('loading');
        
        // 設置超時處理，確保按鈕不會永久停在載入狀態
        const timeout = setTimeout(() => {
            restoreButtonState(element, originalText, isActive);
        }, 5000); // 5秒後自動恢復

        // 首先檢查當前收藏狀態，確保與後端同步
        fetch(`/api/spot/favorites/${spotId}/status`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            }
        })
        .then(response => {
            if (response.status === 401) {
                // 未登入，直接呼叫切換 API（會返回 401）
                return toggleFavoriteApi();
            }
            
            if (!response.ok) {
                throw new Error('檢查收藏狀態失敗');
            }
            
            return response.json().then(data => {
                // 確保按鈕狀態與後端一致
                const serverStatus = data.success && data.data;
                
                if (serverStatus !== isActive) {
                    console.log(`收藏狀態不同步，後端: ${serverStatus}, 前端: ${isActive}`);
                    // 如果不一致，更新按鈕狀態
                    if (serverStatus) {
                        element.classList.add('active');
                        if (icon) icon.textContent = 'favorite';
                        element.title = '取消收藏';
                        if (!favorites.includes(spotId)) {
                            favorites.push(spotId);
                        }
                    } else {
                        element.classList.remove('active');
                        if (icon) icon.textContent = 'favorite_border';
                        element.title = '加入收藏';
                        favorites = favorites.filter(id => id !== spotId);
                    }
                    
                    // 更新本地儲存
                    localStorage.setItem('spotFavorites', JSON.stringify(favorites));
                }
                
                // 繼續切換收藏狀態
                return toggleFavoriteApi();
            });
        })
        .catch(error => {
            console.error('檢查收藏狀態失敗:', error);
            // 出錯時直接嘗試切換收藏狀態
            return toggleFavoriteApi();
        });
        
        // 切換收藏狀態的 API 調用
        function toggleFavoriteApi() {
            return fetch(`/api/spot/favorites/${spotId}/toggle`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-Requested-With': 'XMLHttpRequest'
                }
            })
            .then(response => {
                // 清除超時處理
                clearTimeout(timeout);
                
                if (response.status === 401) {
                    // 未登入，顯示登入對話框
                    showLoginDialog('收藏功能需要登入', '請先登入會員後再收藏景點', element, originalText, isActive);
                    throw new Error('需要登入');
                }
                
                if (!response.ok) {
                    throw new Error('伺服器回應錯誤');
                }
                
                return response.json();
            })
            .then(data => {
                if (data.success) {
                    // 從響應數據中獲取收藏狀態
                    const newStatus = data.data.isFavorited;
                    const favoriteCount = data.data.favoriteCount;
                    
                    if (newStatus) {
                        // 加入收藏
                        element.classList.add('active');
                        if (icon) {
                            icon.textContent = 'favorite';
                        }
                        element.title = '取消收藏';
                        if (!favorites.includes(spotId)) {
                            favorites.push(spotId);
                        }
                        showToast(data.data.message || '已加入收藏', 'success');
                    } else {
                        // 取消收藏
                        element.classList.remove('active');
                        if (icon) {
                            icon.textContent = 'favorite_border';
                        }
                        element.title = '加入收藏';
                        favorites = favorites.filter(id => id !== spotId);
                        showToast(data.data.message || '已取消收藏', 'info');
                    }
                    
                    // 更新收藏數量顯示（如果頁面上有這個元素）
                    const countElement = document.querySelector(`.spot-list-card[data-spot-id="${spotId}"] .favorite-count`);
                    if (countElement) {
                        countElement.textContent = favoriteCount;
                    }
                    
                    // 儲存到本地儲存
                    localStorage.setItem('spotFavorites', JSON.stringify(favorites));
                    
                    // 通知其他頁面更新收藏狀態
                    localStorage.setItem('spotFavoriteChange', JSON.stringify({
                        spotId: spotId,
                        isFavorited: newStatus,
                        favoriteCount: favoriteCount,
                        timestamp: new Date().getTime()
                    }));
                    
                    // 動畫效果
                    if (!prefersReducedMotion) {
                        element.style.transform = 'scale(1.2)';
                        setTimeout(() => {
                            element.style.transform = 'scale(1)';
                        }, 200);
                    }
                } else {
                    // API 返回錯誤
                    showToast(data.message || '操作失敗，請稍後再試', 'error');
                    // 還原按鈕狀態
                    restoreButtonState(element, originalText, isActive);
                }
            })
            .catch(error => {
                // 清除超時處理（如果尚未清除）
                clearTimeout(timeout);
                
                console.error('收藏操作失敗:', error);
                if (error.message !== '需要登入') {
                    showToast('網路錯誤，請稍後再試', 'error');
                    // 還原按鈕狀態
                    restoreButtonState(element, originalText, isActive);
                }
            })
            .finally(() => {
                // 移除載入狀態
                element.classList.remove('loading');
                element.disabled = false;
            });
        }
    };
    
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

    window.toggleBookmark = function(element) {
        const spotId = element.dataset.spotId;
        const icon = element.querySelector('.material-icons');
        const isActive = element.classList.contains('active');

        if (isActive) {
            element.classList.remove('active');
            icon.textContent = 'bookmark_border';
            element.title = '收藏景點';
            bookmarks = bookmarks.filter(id => id !== spotId);
            showToast('已取消書籤', 'info');
        } else {
            element.classList.add('active');
            icon.textContent = 'bookmark';
            element.title = '取消收藏';
            bookmarks.push(spotId);
            showToast('已加入書籤', 'success');
        }

        localStorage.setItem('spotBookmarks', JSON.stringify(bookmarks));
    };

    window.sortSpots = function(sortBy) {
        const grid = document.getElementById('spotGrid');
        if (!grid) return;

        const cards = Array.from(grid.querySelectorAll('.spot-list-item'));
        
        cards.sort((a, b) => {
            let aValue, bValue;
            
            switch(sortBy) {
                case 'name':
                    aValue = a.querySelector('.spot-list-card__title').textContent;
                    bValue = b.querySelector('.spot-list-card__title').textContent;
                    break;
                case 'date':
                    aValue = a.querySelector('.spot-list-card__date span:last-child').textContent;
                    bValue = b.querySelector('.spot-list-card__date span:last-child').textContent;
                    break;
                case 'location':
                    aValue = a.querySelector('.spot-list-card__location span:last-child').textContent;
                    bValue = b.querySelector('.spot-list-card__location span:last-child').textContent;
                    break;
                case 'rating':
                    // 獲取評分，如果沒有評分則預設為 0
                    aValue = parseFloat(a.querySelector('.spot-list-card__rating span:last-child').textContent) || 0;
                    bValue = parseFloat(b.querySelector('.spot-list-card__rating span:last-child').textContent) || 0;
                    break;
                default:
                    return 0;
            }
            
            // 根據排序方向進行比較
            if (currentSortOrder === 'asc') {
                if (typeof aValue === 'string') {
                    return aValue.localeCompare(bValue, 'zh-TW');
                }
                return aValue - bValue;
            } else {
                if (typeof aValue === 'string') {
                    return bValue.localeCompare(aValue, 'zh-TW');
                }
                return bValue - aValue;
            }
        });
        
        // 重新插入排序後的元素
        cards.forEach(spot => grid.appendChild(spot));
        
        currentSort = sortBy;
        showSortNotification(sortBy, currentSortOrder);
        
        // 重新觸發動畫
        if (!prefersReducedMotion) {
            animateCards();
        }
    };

    // 重置搜尋表單
    window.resetSearchForm = function() {
        // 清空關鍵字輸入
        document.getElementById('keyword').value = '';
        
        // 重置地區選擇為"所有地區"
        document.getElementById('region').value = '';
        
        // 提交表單
        document.querySelector('.spot-list-search-form').submit();
    };

    window.loadMoreSpots = function() {
        const btn = document.querySelector('.spot-list-load-more-btn');
        if (btn) {
            btn.innerHTML = '<span class="material-icons">hourglass_empty</span>載入中...';
            btn.disabled = true;
            
            
        }
    };

    // 輔助函數
    function getSortName(sortBy) {
        const names = {
            'name': '名稱',
            'date': '時間',
            'location': '地區',
            'rating': '評分'
        };
        return names[sortBy] || '預設';
    }

    function validateSearchInput(value) {
        const input = document.getElementById('keyword');
        if (!input) return;

        if (value.length > 0 && value.length < 2) {
            input.style.borderColor = 'var(--md-sys-color-error)';
        } else {
            input.style.borderColor = 'var(--md-sys-color-outline-variant)';
        }
    }

    function showSearchSuggestions() {
        // 搜尋建議功能（待實作）
        console.log('顯示搜尋建議');
    }

    function updateRegionFilter(region) {
        console.log('更新地區篩選:', region);
    }

    function closeAlert(alert) {
        if (!alert) return;
        
        if (!prefersReducedMotion) {
            alert.style.transition = 'opacity 0.3s ease, transform 0.3s ease';
            alert.style.opacity = '0';
            alert.style.transform = 'translateY(-10px)';
            setTimeout(() => {
                alert.remove();
            }, 300);
        } else {
            alert.remove();
        }
    }

    function showLoginDialog(title, message, buttonElement, originalIconText, isActive) {
        // 檢查是否已存在對話框，避免重複顯示
        if (document.getElementById('login-dialog')) {
            return;
        }
        
        // 創建對話框元素
        const dialog = document.createElement('div');
        dialog.id = 'login-dialog';
        dialog.className = 'spot-list-dialog';
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
        content.className = 'spot-list-dialog__content';
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
        titleEl.className = 'spot-list-dialog__title';
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
        messageEl.className = 'spot-list-dialog__message';
        messageEl.style.cssText = `
            margin-bottom: 24px;
            color: var(--md-sys-color-on-surface-variant);
            font-size: 1rem;
        `;
        messageEl.textContent = message;
        
        // 按鈕容器
        const buttons = document.createElement('div');
        buttons.className = 'spot-list-dialog__buttons';
        buttons.style.cssText = `
            display: flex;
            justify-content: center;
            gap: 16px;
        `;
        
        // 取消按鈕
        const cancelButton = document.createElement('button');
        cancelButton.className = 'spot-list-dialog__button spot-list-dialog__button--cancel';
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
            // 關閉對話框
            closeDialog(dialog);
            
            // 恢復按鈕狀態
            if (buttonElement) {
                restoreButtonState(buttonElement, originalIconText, isActive);
            }
        });
        
        // 登入按鈕
        const loginButton = document.createElement('button');
        loginButton.className = 'spot-list-dialog__button spot-list-dialog__button--login';
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
            
            .spot-list-dialog__button:hover {
                filter: brightness(1.1);
            }
        `;
        document.head.appendChild(style);
        
        // 點擊背景關閉對話框
        dialog.addEventListener('click', (e) => {
            if (e.target === dialog) {
                closeDialog(dialog);
                
                // 恢復按鈕狀態
                if (buttonElement) {
                    restoreButtonState(buttonElement, originalIconText, isActive);
                }
            }
        });
        
        // ESC 鍵關閉對話框
        document.addEventListener('keydown', function escHandler(e) {
            if (e.key === 'Escape') {
                closeDialog(dialog);
                
                // 恢復按鈕狀態
                if (buttonElement) {
                    restoreButtonState(buttonElement, originalIconText, isActive);
                }
                
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

    // 當頁面載入完成時執行
    document.addEventListener('DOMContentLoaded', function() {
        // 排序相關變數
        let currentSortOrder = 'asc'; // 預設升序
        
        // 切換排序方向
        window.toggleSortOrder = function() {
            const sortOrderBtn = document.getElementById('sortOrder');
            currentSortOrder = currentSortOrder === 'asc' ? 'desc' : 'asc';
            
            // 更新按鈕樣式
            if (currentSortOrder === 'desc') {
                sortOrderBtn.classList.add('desc');
            } else {
                sortOrderBtn.classList.remove('desc');
            }
            
            // 重新排序
            handleSort();
        };
        
        // 處理排序
        window.handleSort = function() {
            const sortBy = document.getElementById('sortBy').value;
            const spotGrid = document.getElementById('spotGrid');
            const spots = Array.from(spotGrid.children);
            
            spots.sort((a, b) => {
                let aValue, bValue;
                
                switch(sortBy) {
                    case 'name':
                        aValue = a.querySelector('.spot-list-card__title').textContent;
                        bValue = b.querySelector('.spot-list-card__title').textContent;
                        break;
                    case 'date':
                        aValue = a.querySelector('.spot-list-card__date span:last-child').textContent;
                        bValue = b.querySelector('.spot-list-card__date span:last-child').textContent;
                        break;
                    case 'location':
                        aValue = a.querySelector('.spot-list-card__location span:last-child').textContent;
                        bValue = b.querySelector('.spot-list-card__location span:last-child').textContent;
                        break;
                    case 'rating':
                        // 獲取評分，如果沒有評分則預設為 0
                        aValue = parseFloat(a.querySelector('.spot-list-card__rating span:last-child').textContent) || 0;
                        bValue = parseFloat(b.querySelector('.spot-list-card__rating span:last-child').textContent) || 0;
                        break;
                    default:
                        return 0;
                }
                
                // 根據排序方向進行比較
                if (currentSortOrder === 'asc') {
                    if (typeof aValue === 'string') {
                        return aValue.localeCompare(bValue, 'zh-TW');
                    }
                    return aValue - bValue;
                } else {
                    if (typeof aValue === 'string') {
                        return bValue.localeCompare(aValue, 'zh-TW');
                    }
                    return bValue - aValue;
                }
            });
            
            // 重新插入排序後的元素
            spots.forEach(spot => spotGrid.appendChild(spot));
            
            // 顯示排序提示
            showSortNotification(sortBy, currentSortOrder);
        };
        
        // 顯示排序提示
        function showSortNotification(sortBy, order) {
            const sortTypeText = {
                'name': '名稱',
                'date': '時間',
                'location': '地區',
                'rating': '評分'
            }[sortBy];
            
            const orderText = order === 'asc' ? '升序' : '降序';
            
            // 移除舊的提示（如果存在）
            const oldNotification = document.querySelector('.spot-list-notification');
            if (oldNotification) {
                oldNotification.remove();
            }
            
            // 創建提示元素
            const notification = document.createElement('div');
            notification.className = 'spot-list-notification';
            notification.innerHTML = `
                <span class="material-icons">sort</span>
                <span>已按${sortTypeText}${orderText}排序</span>
            `;
            
            // 添加到頁面
            document.body.appendChild(notification);
            
            // 3秒後移除
            setTimeout(() => {
                notification.style.opacity = '0';
                setTimeout(() => notification.remove(), 300);
            }, 3000);
        }
        
        // 如果有搜尋結果提示訊息
        const msg = document.querySelector('.spot-list-msg');
        if (msg) {
            // 3秒後自動隱藏提示訊息
            setTimeout(() => {
                msg.style.opacity = '0';
                setTimeout(() => {
                    msg.style.display = 'none';
                }, 300);
            }, 3000);
        }
    });

    // 初始化
    init();
})(); 