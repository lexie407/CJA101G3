/**
 * 景點列表頁 JavaScript
 * @version 1.0
 * @description 提供景點列表頁的交互效果和用戶體驗增強
 */

(function() {
    'use strict';

    // 檢查動畫偏好
    const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

    // 全域變數
    let currentView = 'grid';
    let currentSort = 'name';
    let favorites = JSON.parse(localStorage.getItem('spotFavorites') || '[]');
    let bookmarks = JSON.parse(localStorage.getItem('spotBookmarks') || '[]');

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
     * 搜尋表單功能
     */
    function initSearchForm() {
        const searchInput = document.getElementById('keyword');
        const regionSelect = document.getElementById('region');
        const searchForm = document.querySelector('.spot-list-search-form');

        // 搜尋輸入框即時提示
        if (searchInput) {
            let searchTimeout;
            searchInput.addEventListener('input', function() {
                clearTimeout(searchTimeout);
                searchTimeout = setTimeout(() => {
                    validateSearchInput(this.value);
                }, 300);
            });

            // 搜尋建議功能
            searchInput.addEventListener('focus', function() {
                showSearchSuggestions();
            });
        }

        // 地區選擇變更
        if (regionSelect) {
            regionSelect.addEventListener('change', function() {
                updateRegionFilter(this.value);
                // 自動提交表單進行篩選
                if (this.value !== '') {
                    showToast('正在篩選地區...', 'info');
                    setTimeout(() => {
                        searchForm.submit();
                    }, 300);
                }
            });
        }

        // 表單提交
        if (searchForm) {
            searchForm.addEventListener('submit', function(e) {
                const keyword = searchInput?.value.trim();
                const region = regionSelect?.value;
                
                // 驗證搜尋條件
                if (keyword && keyword.length < 2) {
                    e.preventDefault();
                    showToast('搜尋關鍵字至少需要2個字元', 'warning');
                    return;
                }
                
                // 如果沒有任何搜尋條件，提示用戶
                if (!keyword && !region) {
                    e.preventDefault();
                    showToast('請輸入搜尋關鍵字或選擇地區', 'warning');
                    return;
                }
                
                showToast('正在搜尋景點...', 'info');
            });
        }
        
        // 搜尋按鈕點擊事件
        const searchBtn = document.querySelector('.spot-list-search-btn');
        if (searchBtn) {
            searchBtn.addEventListener('click', function(e) {
                e.preventDefault();
                
                const keyword = searchInput?.value.trim();
                const region = regionSelect?.value;
                
                // 驗證搜尋條件
                if (keyword && keyword.length < 2) {
                    showToast('搜尋關鍵字至少需要2個字元', 'warning');
                    searchInput?.focus();
                    return;
                }
                
                // 如果沒有任何搜尋條件，提示用戶
                if (!keyword && !region) {
                    showToast('請輸入搜尋關鍵字或選擇地區', 'warning');
                    searchInput?.focus();
                    return;
                }
                
                // 提交表單
                showToast('正在搜尋景點...', 'info');
                if (searchForm) {
                    searchForm.submit();
                }
            });
        }
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
                const spotId = this.dataset.spotId;
                toggleFavorite(spotId, this);
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
        const icon = element.querySelector('.material-icons');
        const isActive = element.classList.contains('active');

        if (isActive) {
            // 取消收藏
            element.classList.remove('active');
            icon.textContent = 'favorite_border';
            element.title = '加入收藏';
            favorites = favorites.filter(id => id !== spotId);
            showToast('已取消收藏', 'info');
        } else {
            // 加入收藏
            element.classList.add('active');
            icon.textContent = 'favorite';
            element.title = '取消收藏';
            favorites.push(spotId);
            showToast('已加入收藏', 'success');
        }

        // 儲存到本地儲存
        localStorage.setItem('spotFavorites', JSON.stringify(favorites));

        // 動畫效果
        if (!prefersReducedMotion) {
            element.style.transform = 'scale(1.2)';
            setTimeout(() => {
                element.style.transform = 'scale(1)';
            }, 200);
        }
    };

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
            const aCard = a.querySelector('.spot-list-card');
            const bCard = b.querySelector('.spot-list-card');
            
            switch (sortBy) {
                case 'name':
                    const aName = aCard.querySelector('.spot-list-card__title').textContent;
                    const bName = bCard.querySelector('.spot-list-card__title').textContent;
                    return aName.localeCompare(bName, 'zh-TW');
                    
                case 'date':
                    const aDate = aCard.querySelector('.spot-list-card__date span:last-child').textContent;
                    const bDate = bCard.querySelector('.spot-list-card__date span:last-child').textContent;
                    return new Date(bDate) - new Date(aDate);
                    
                case 'location':
                    const aLoc = aCard.querySelector('.spot-list-card__location span:last-child').textContent;
                    const bLoc = bCard.querySelector('.spot-list-card__location span:last-child').textContent;
                    return aLoc.localeCompare(bLoc, 'zh-TW');
                    
                case 'popular':
                    const aRating = parseFloat(aCard.querySelector('.spot-list-card__rating span:last-child').textContent);
                    const bRating = parseFloat(bCard.querySelector('.spot-list-card__rating span:last-child').textContent);
                    return bRating - aRating;
                    
                default:
                    return 0;
            }
        });

        // 重新排列 DOM
        cards.forEach(card => grid.appendChild(card));
        
        currentSort = sortBy;
        showToast(`已按${getSortName(sortBy)}排序`, 'success');
        
        // 重新觸發動畫
        if (!prefersReducedMotion) {
            animateCards();
        }
    };

    window.resetSearchForm = function() {
        const form = document.querySelector('.spot-list-search-form');
        const keywordInput = document.getElementById('keyword');
        const regionSelect = document.getElementById('region');
        
        if (form) {
            // 清除表單內容
            form.reset();
            
            // 確保輸入框清空
            if (keywordInput) {
                keywordInput.value = '';
                keywordInput.style.borderColor = 'var(--md-sys-color-outline-variant)';
            }
            
            if (regionSelect) {
                regionSelect.value = '';
            }
            
            showToast('已重置搜尋條件', 'info');
            
            // 重新導向到無參數的列表頁面
            setTimeout(() => {
                window.location.href = '/spot/spotlist';
            }, 500);
        }
    };

    window.loadMoreSpots = function() {
        const btn = document.querySelector('.spot-list-load-more-btn');
        if (btn) {
            btn.innerHTML = '<span class="material-icons">hourglass_empty</span>載入中...';
            btn.disabled = true;
            
            // 模擬載入
            setTimeout(() => {
                showToast('載入更多景點功能開發中', 'info');
                btn.innerHTML = '<span class="material-icons">expand_more</span>載入更多景點';
                btn.disabled = false;
            }, 1000);
        }
    };

    // 輔助函數
    function getSortName(sortBy) {
        const names = {
            'name': '名稱',
            'date': '時間',
            'location': '地區',
            'popular': '熱門度'
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

    function showToast(message, type = 'info') {
        // 創建 Toast 通知
        const toast = document.createElement('div');
        toast.className = `spot-list-toast spot-list-toast--${type}`;
        toast.innerHTML = `
            <span class="material-icons">${getToastIcon(type)}</span>
            <span>${message}</span>
        `;
        
        // 添加樣式
        Object.assign(toast.style, {
            position: 'fixed',
            top: '20px',
            right: '20px',
            background: getToastColor(type),
            color: 'var(--md-sys-color-on-primary)',
            padding: '12px 20px',
            borderRadius: '8px',
            display: 'flex',
            alignItems: 'center',
            gap: '8px',
            boxShadow: '0 4px 12px rgba(0, 0, 0, 0.15)',
            zIndex: '10000',
            transform: 'translateX(100%)',
            transition: 'transform 0.3s ease'
        });
        
        document.body.appendChild(toast);
        
        // 動畫進入
        setTimeout(() => {
            toast.style.transform = 'translateX(0)';
        }, 100);
        
        // 自動移除
        setTimeout(() => {
            toast.style.transform = 'translateX(100%)';
            setTimeout(() => {
                toast.remove();
            }, 300);
        }, 3000);
    }

    function getToastIcon(type) {
        const icons = {
            'success': 'check_circle',
            'error': 'error',
            'warning': 'warning',
            'info': 'info'
        };
        return icons[type] || 'info';
    }

    function getToastColor(type) {
        const colors = {
            'success': 'var(--md-sys-color-primary)',
            'error': 'var(--md-sys-color-error)',
            'warning': 'var(--md-sys-color-secondary)',
            'info': 'var(--md-sys-color-surface-container)'
        };
        return colors[type] || 'var(--md-sys-color-surface-container)';
    }

    // 初始化
    init();
})(); 