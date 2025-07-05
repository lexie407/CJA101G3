/**
 * 景點搜尋頁 JavaScript
 * @version 2.0
 * @description 提供景點搜尋頁的交互效果和功能增強
 * 依賴基礎層樣式和Material Design 3色彩系統
 */

(function() {
    'use strict';

    // 全域變數
    let isAdvancedSearchVisible = false;
    let searchHistory = [];
    const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

    /**
     * 初始化所有功能
     */
    function init() {
        initSearchFunctionality();
        initAdvancedSearch();
        initCardEffects();
        initViewToggle();
        initKeyboardShortcuts();
        initAccessibility();
        initSearchHistory();
        initToastSystem();
        
        // 頁面載入完成後的效果
        document.addEventListener('DOMContentLoaded', function() {
            focusSearchInput();
            if (!prefersReducedMotion) {
                animatePageElements();
            }
            loadSearchHistory();
        });
    }

    /**
     * 搜尋功能增強
     */
    function initSearchFunctionality() {
        const searchForm = document.querySelector('.spot-search-form');
        const searchInput = document.querySelector('.spot-search-box__input');
        
        if (searchForm && searchInput) {
            // 防止空白搜尋
            searchForm.addEventListener('submit', function(e) {
                const keyword = searchInput.value.trim();
                if (!keyword) {
                    e.preventDefault();
                    showToast('請輸入搜尋關鍵字', 'warning');
                    searchInput.focus();
                    return;
                }
                
                // 儲存搜尋歷史
                saveSearchHistory(keyword);
                showToast('正在搜尋...', 'info');
            });

            // 即時搜尋建議（防抖動）
            let searchTimeout;
            searchInput.addEventListener('input', function() {
                clearTimeout(searchTimeout);
                searchTimeout = setTimeout(() => {
                    showSearchSuggestions(this.value.trim());
                }, 300);
            });

            // Enter 鍵快速搜尋
            searchInput.addEventListener('keydown', function(e) {
                if (e.key === 'Enter') {
                    e.preventDefault();
                    searchForm.submit();
                }
            });
        }
    }

    /**
     * 進階搜尋功能
     */
    function initAdvancedSearch() {
        const toggleBtn = document.querySelector('.spot-search-advanced-toggle');
        const advancedPanel = document.querySelector('.spot-search-advanced');
        
        if (toggleBtn && advancedPanel) {
            toggleBtn.addEventListener('click', function() {
                toggleAdvancedSearch();
            });
        }
    }

    /**
     * 切換進階搜尋面板
     */
    window.toggleAdvancedSearch = function() {
        const toggleBtn = document.querySelector('.spot-search-advanced-toggle');
        const advancedPanel = document.querySelector('.spot-search-advanced');
        const toggleText = document.querySelector('.spot-search-advanced-toggle__text');
        
        if (!toggleBtn || !advancedPanel) return;
        
        isAdvancedSearchVisible = !isAdvancedSearchVisible;
        
        if (isAdvancedSearchVisible) {
            advancedPanel.classList.add('active');
            toggleBtn.classList.add('active');
            if (toggleText) toggleText.textContent = '收起進階搜尋';
            
            // 聚焦第一個選擇框
            const firstSelect = advancedPanel.querySelector('select');
            if (firstSelect) {
                setTimeout(() => firstSelect.focus(), 100);
            }
        } else {
            advancedPanel.classList.remove('active');
            toggleBtn.classList.remove('active');
            if (toggleText) toggleText.textContent = '進階搜尋';
        }
        
        // 無障礙屬性更新
        toggleBtn.setAttribute('aria-expanded', isAdvancedSearchVisible);
    };

    /**
     * 重置搜尋表單
     */
    window.resetSearchForm = function() {
        const form = document.querySelector('.spot-search-form');
        if (form) {
            // 重置所有表單欄位
            const inputs = form.querySelectorAll('input, select');
            inputs.forEach(input => {
                if (input.type === 'text') {
                    input.value = '';
                } else if (input.tagName === 'SELECT') {
                    input.selectedIndex = 0;
                }
            });
            
            // 聚焦搜尋框
            const searchInput = form.querySelector('.spot-search-box__input');
            if (searchInput) {
                searchInput.focus();
            }
            
            showToast('搜尋條件已重置', 'info');
        }
    };

    /**
     * 清除搜尋並返回初始狀態
     */
    window.clearSearch = function() {
        window.location.href = '/spot/search';
    };

    /**
     * 熱門標籤搜尋功能
     */
    window.searchKeyword = function(keyword) {
        const input = document.querySelector('.spot-search-box__input');
        if (input) {
            input.value = keyword.trim();
            
            // 添加視覺回饋
            const tag = event.target.closest('.spot-search-tag');
            if (tag && !prefersReducedMotion) {
                tag.style.transform = 'scale(0.95)';
                setTimeout(() => {
                    tag.style.transform = '';
                }, 150);
            }
            
            // 提交表單
            const form = document.querySelector('.spot-search-form');
            if (form) {
                form.submit();
            }
        }
    };

    /**
     * 卡片效果增強
     */
    function initCardEffects() {
        const cards = document.querySelectorAll('.spot-search-card');
        
        cards.forEach(function(card, index) {
            // 點擊卡片跳轉到詳情頁
            card.addEventListener('click', function(e) {
                // 避免重複觸發
                if (e.target.closest('.spot-search-card__favorite, .spot-search-card__share, .spot-search-card__link')) {
                    return;
                }
                
                const viewBtn = this.querySelector('.spot-search-card__link');
                if (viewBtn) {
                    // 添加點擊效果
                    if (!prefersReducedMotion) {
                        this.style.transform = 'scale(0.98)';
                        setTimeout(() => {
                            this.style.transform = '';
                            window.location.href = viewBtn.href;
                        }, 100);
                    } else {
                        window.location.href = viewBtn.href;
                    }
                }
            });

            // 鍵盤導航支援
            card.setAttribute('tabindex', '0');
            card.setAttribute('role', 'button');
            
            card.addEventListener('keydown', function(e) {
                if (e.key === 'Enter' || e.key === ' ') {
                    e.preventDefault();
                    this.click();
                }
            });

            // 載入動畫
            if (!prefersReducedMotion) {
                card.style.opacity = '0';
                card.style.transform = 'translateY(20px)';
                
                setTimeout(() => {
                    card.style.transition = 'opacity 0.3s ease, transform 0.3s ease';
                    card.style.opacity = '1';
                    card.style.transform = 'translateY(0)';
                }, index * 100);
            }
        });
    }

    /**
     * 收藏功能
     */
    window.toggleFavorite = function(button) {
        const spotId = button.dataset.spotId;
        const icon = button.querySelector('.material-icons');
        
        if (!spotId) return;
        
        // 切換視覺狀態
        const isFavorited = button.classList.contains('active');
        
        if (isFavorited) {
            button.classList.remove('active');
            icon.textContent = 'favorite_border';
            showToast('已取消收藏', 'info');
        } else {
            button.classList.add('active');
            icon.textContent = 'favorite';
            showToast('已加入收藏', 'success');
        }
        
        // 添加動畫效果
        if (!prefersReducedMotion) {
            button.style.transform = 'scale(1.2)';
            setTimeout(() => {
                button.style.transform = '';
            }, 200);
        }
        
        // TODO: 實際的收藏API調用
        // favoriteSpot(spotId, !isFavorited);
    };

    /**
     * 分享功能
     */
    window.shareSpot = function(spotId) {
        const spotCard = document.querySelector(`[data-spot-id="${spotId}"]`)?.closest('.spot-search-card');
        const spotName = spotCard?.querySelector('.spot-search-card__title')?.textContent || '景點';
        
        const shareData = {
            title: `${spotName} - 島遊Kha`,
            text: `來看看這個很棒的景點：${spotName}`,
            url: `${window.location.origin}/spot/detail/${spotId}`
        };
        
        if (navigator.share) {
            navigator.share(shareData).catch(err => {
                console.log('分享失敗:', err);
                copyToClipboard(shareData.url);
            });
        } else {
            copyToClipboard(shareData.url);
        }
    };

    /**
     * 複製到剪貼簿
     */
    function copyToClipboard(text) {
        if (navigator.clipboard) {
            navigator.clipboard.writeText(text).then(() => {
                showToast('連結已複製到剪貼簿', 'success');
            }).catch(() => {
                fallbackCopyToClipboard(text);
            });
        } else {
            fallbackCopyToClipboard(text);
        }
    }

    function fallbackCopyToClipboard(text) {
        const textArea = document.createElement('textarea');
        textArea.value = text;
        textArea.style.position = 'fixed';
        textArea.style.opacity = '0';
        document.body.appendChild(textArea);
        textArea.select();
        
        try {
            document.execCommand('copy');
            showToast('連結已複製到剪貼簿', 'success');
        } catch (err) {
            showToast('複製失敗，請手動複製連結', 'error');
        }
        
        document.body.removeChild(textArea);
    }

    /**
     * 視圖切換功能
     */
    function initViewToggle() {
        const toggleButtons = document.querySelectorAll('.spot-search-view-toggle');
        const resultsGrid = document.querySelector('.spot-search-results-grid');
        
        toggleButtons.forEach(button => {
            button.addEventListener('click', function() {
                const view = this.dataset.view;
                
                // 更新按鈕狀態
                toggleButtons.forEach(btn => btn.classList.remove('active'));
                this.classList.add('active');
                
                // 更新網格樣式
                if (resultsGrid) {
                    if (view === 'list') {
                        resultsGrid.style.gridTemplateColumns = '1fr';
                    } else {
                        resultsGrid.style.gridTemplateColumns = 'repeat(auto-fill, minmax(350px, 1fr))';
                    }
                }
                
                showToast(`已切換到${view === 'grid' ? '網格' : '列表'}檢視`, 'info');
            });
        });
    }

    /**
     * 鍵盤快捷鍵
     */
    function initKeyboardShortcuts() {
        document.addEventListener('keydown', function(e) {
            // Ctrl/Cmd + K: 聚焦搜尋框
            if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
                e.preventDefault();
                const searchInput = document.querySelector('.spot-search-box__input');
                if (searchInput) {
                    searchInput.focus();
                    searchInput.select();
                }
            }
            
            // Escape: 清除搜尋框或關閉進階搜尋
            if (e.key === 'Escape') {
                const searchInput = document.querySelector('.spot-search-box__input');
                if (searchInput && document.activeElement === searchInput) {
                    searchInput.value = '';
                } else if (isAdvancedSearchVisible) {
                    toggleAdvancedSearch();
                }
            }
            
            // Ctrl/Cmd + Enter: 查看所有景點
            if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
                e.preventDefault();
                window.location.href = '/spot/spotlist';
            }
            
            // Alt + A: 切換進階搜尋
            if (e.altKey && e.key === 'a') {
                e.preventDefault();
                toggleAdvancedSearch();
            }
        });
    }

    /**
     * 無障礙功能增強
     */
    function initAccessibility() {
        // 為卡片添加 ARIA 標籤
        const cards = document.querySelectorAll('.spot-search-card');
        cards.forEach(function(card, index) {
            const title = card.querySelector('.spot-search-card__title');
            const location = card.querySelector('.spot-search-card__location');
            
            if (title && location) {
                const ariaLabel = `景點: ${title.textContent}, 位置: ${location.textContent}`;
                card.setAttribute('aria-label', ariaLabel);
            }
            
            // 設置 aria-posinset 和 aria-setsize
            card.setAttribute('aria-posinset', index + 1);
            card.setAttribute('aria-setsize', cards.length);
        });

        // 搜尋結果數量公告
        const resultCount = document.querySelectorAll('.spot-search-card').length;
        if (resultCount > 0) {
            announceToScreenReader(`找到 ${resultCount} 個搜尋結果`);
        }

        // 為進階搜尋按鈕添加 ARIA 屬性
        const advancedToggle = document.querySelector('.spot-search-advanced-toggle');
        if (advancedToggle) {
            advancedToggle.setAttribute('aria-expanded', 'false');
            advancedToggle.setAttribute('aria-controls', 'advancedSearch');
        }
    }

    /**
     * 螢幕閱讀器公告
     */
    function announceToScreenReader(message) {
        const announcement = document.createElement('div');
        announcement.setAttribute('aria-live', 'polite');
        announcement.setAttribute('aria-atomic', 'true');
        announcement.style.position = 'absolute';
        announcement.style.left = '-10000px';
        announcement.style.width = '1px';
        announcement.style.height = '1px';
        announcement.style.overflow = 'hidden';
        
        document.body.appendChild(announcement);
        announcement.textContent = message;
        
        setTimeout(() => {
            document.body.removeChild(announcement);
        }, 1000);
    }

    /**
     * 搜尋歷史功能
     */
    function initSearchHistory() {
        loadSearchHistory();
    }

    function saveSearchHistory(keyword) {
        if (!keyword || keyword.length < 2) return;
        
        // 從 localStorage 載入現有歷史
        let history = JSON.parse(localStorage.getItem('spotSearchHistory') || '[]');
        
        // 移除重複項目
        history = history.filter(item => item.keyword !== keyword);
        
        // 添加新項目到開頭
        history.unshift({
            keyword: keyword,
            timestamp: new Date().toISOString(),
            count: (history.find(item => item.keyword === keyword)?.count || 0) + 1
        });
        
        // 限制歷史記錄數量
        history = history.slice(0, 20);
        
        // 儲存到 localStorage
        localStorage.setItem('spotSearchHistory', JSON.stringify(history));
        searchHistory = history;
    }

    function loadSearchHistory() {
        try {
            searchHistory = JSON.parse(localStorage.getItem('spotSearchHistory') || '[]');
            
            // 如果沒有搜尋且有歷史記錄，可以顯示搜尋建議
            if (searchHistory.length > 0 && !hasSearched()) {
                createSearchHistoryUI(searchHistory.slice(0, 5));
            }
        } catch (e) {
            console.error('載入搜尋歷史失敗:', e);
            searchHistory = [];
        }
    }

    function hasSearched() {
        return document.querySelector('.spot-search-results') !== null;
    }

    function createSearchHistoryUI(history) {
        if (history.length === 0) return;
        
        const popularSection = document.querySelector('.spot-search-popular');
        if (!popularSection) return;
        
        const historySection = document.createElement('div');
        historySection.className = 'spot-search-history';
        historySection.innerHTML = `
            <h3 class="spot-search-history__title">
                <span class="material-icons">history</span>
                最近搜尋
            </h3>
            <div class="spot-search-history__tags">
                ${history.map(item => `
                    <button class="spot-search-tag" onclick="searchKeyword('${item.keyword}')">
                        ${item.keyword}
                    </button>
                `).join('')}
            </div>
        `;
        
        // 插入到熱門搜尋之後
        popularSection.parentNode.insertBefore(historySection, popularSection.nextSibling);
    }

    /**
     * Toast 通知系統
     */
    function initToastSystem() {
        // 確保 toast 容器存在
        if (!document.getElementById('toastContainer')) {
            const container = document.createElement('div');
            container.id = 'toastContainer';
            container.className = 'spot-search-toast-container';
            container.setAttribute('aria-live', 'polite');
            document.body.appendChild(container);
        }
    }

    function showToast(message, type = 'info', duration = 3000) {
        const container = document.getElementById('toastContainer');
        if (!container) return;
        
        const toast = document.createElement('div');
        toast.className = `spot-search-toast spot-search-toast--${type}`;
        
        const icon = getToastIcon(type);
        toast.innerHTML = `
            <span class="material-icons">${icon}</span>
            <span>${message}</span>
        `;
        
        container.appendChild(toast);
        
        // 自動移除
        setTimeout(() => {
            if (toast.parentNode) {
                toast.style.opacity = '0';
                toast.style.transform = 'translateX(100%)';
                setTimeout(() => {
                    container.removeChild(toast);
                }, 300);
            }
        }, duration);
        
        // 點擊關閉
        toast.addEventListener('click', () => {
            if (toast.parentNode) {
                container.removeChild(toast);
            }
        });
    }

    function getToastIcon(type) {
        const icons = {
            success: 'check_circle',
            error: 'error',
            warning: 'warning',
            info: 'info'
        };
        return icons[type] || 'info';
    }

    /**
     * 搜尋建議功能
     */
    function showSearchSuggestions(keyword) {
        if (!keyword || keyword.length < 2) {
            hideSuggestions();
            return;
        }
        
        // TODO: 實作搜尋建議API調用
        // 目前顯示歷史記錄中的匹配項目
        const suggestions = searchHistory
            .filter(item => item.keyword.toLowerCase().includes(keyword.toLowerCase()))
            .slice(0, 5);
        
        if (suggestions.length > 0) {
            createSuggestionsUI(suggestions);
        }
    }

    function createSuggestionsUI(suggestions) {
        hideSuggestions(); // 先移除現有建議
        
        const searchBox = document.querySelector('.spot-search-box');
        if (!searchBox) return;
        
        const suggestionsContainer = document.createElement('div');
        suggestionsContainer.className = 'spot-search-suggestions-dropdown';
        suggestionsContainer.innerHTML = suggestions.map(item => `
            <button class="spot-search-suggestion-item" onclick="searchKeyword('${item.keyword}')">
                <span class="material-icons">history</span>
                <span>${item.keyword}</span>
            </button>
        `).join('');
        
        searchBox.appendChild(suggestionsContainer);
    }

    function hideSuggestions() {
        const existing = document.querySelector('.spot-search-suggestions-dropdown');
        if (existing) {
            existing.remove();
        }
    }

    /**
     * 頁面動畫效果
     */
    function animatePageElements() {
        // 英雄區塊動畫
        const hero = document.querySelector('.spot-search-hero');
        if (hero) {
            hero.style.opacity = '0';
            hero.style.transform = 'translateY(-20px)';
            setTimeout(() => {
                hero.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
                hero.style.opacity = '1';
                hero.style.transform = 'translateY(0)';
            }, 100);
        }
        
        // 搜尋框動畫
        const searchBox = document.querySelector('.spot-search-box');
        if (searchBox) {
            searchBox.style.opacity = '0';
            searchBox.style.transform = 'scale(0.95)';
            setTimeout(() => {
                searchBox.style.transition = 'opacity 0.4s ease, transform 0.4s ease';
                searchBox.style.opacity = '1';
                searchBox.style.transform = 'scale(1)';
            }, 300);
        }
    }

    /**
     * 聚焦搜尋輸入框
     */
    function focusSearchInput() {
        const searchInput = document.querySelector('.spot-search-box__input');
        if (searchInput && !searchInput.value) {
            // 延遲聚焦，避免與頁面載入衝突
            setTimeout(() => {
                searchInput.focus();
            }, 500);
        }
    }

    /**
     * 關閉提示訊息
     */
    function initAlertClosing() {
        const alerts = document.querySelectorAll('.spot-search-alert');
        alerts.forEach(alert => {
            const closeBtn = alert.querySelector('.spot-search-alert__close');
            if (closeBtn) {
                closeBtn.addEventListener('click', () => {
                    alert.style.opacity = '0';
                    alert.style.transform = 'translateY(-20px)';
                    setTimeout(() => {
                        if (alert.parentNode) {
                            alert.parentNode.removeChild(alert);
                        }
                    }, 300);
                });
            }
        });
    }

    // 初始化應用程式
    init();
    initAlertClosing();

    // 導出函數供全域使用
    window.SpotSearch = {
        searchKeyword,
        toggleAdvancedSearch,
        resetSearchForm,
        clearSearch,
        toggleFavorite,
        shareSpot,
        showToast
    };

})(); 