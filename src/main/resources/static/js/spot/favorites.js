/**
 * 景點收藏頁面 JavaScript
 * @description 處理收藏景點的移除、分享、動畫效果和用戶互動
 * @version 2.1
 */

(function() {
    'use strict';

    // 檢查動畫偏好
    const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

    /**
     * 主要模組物件
     */
    const SpotFavorites = {
        // 配置選項
        config: {
            animationDuration: prefersReducedMotion ? 0 : 300,
            alertAutoHideDelay: 5000,
            confirmRemoveText: '確定要取消收藏這個景點嗎？',
            apiEndpoint: '/api/spot/favorites',
            removeSuccessMessage: '已成功取消收藏',
            removeErrorMessage: '取消收藏失敗，請稍後再試',
            shareSuccessMessage: '連結已複製到剪貼簿',
            shareErrorMessage: '分享功能暫時無法使用'
        },

        // 狀態管理
        state: {
            isRemoving: false,
            favoriteCount: 0,
            toastTimeout: null
        },

        /**
         * 初始化
         */
        init() {
            this.bindEvents();
            this.initializeCards();
            this.updateFavoriteCount();
            this.setupRealtimeSync();
            console.log('景點收藏頁面已初始化');
        },

        /**
         * 綁定事件
         */
        bindEvents() {
            // 收藏按鈕事件委託
            document.addEventListener('click', (e) => {
                if (e.target.closest('.spot-index-spot-card__favorite, .itinerary-list-card__favorite')) {
                    e.preventDefault();
                    e.stopPropagation();
                    const button = e.target.closest('.spot-index-spot-card__favorite, .itinerary-list-card__favorite');
                    const spotId = button.dataset.spotId;
                    if (spotId) {
                        this.toggleFavorite(spotId, button);
                    }
                }
            });

            // 提示訊息關閉按鈕
            document.addEventListener('click', (e) => {
                if (e.target.closest('.spot-favorites-alert__close')) {
                    e.preventDefault();
                    const alert = e.target.closest('.spot-favorites-alert');
                    if (alert) {
                        this.hideAlert(alert);
                    }
                }
            });
        },

        /**
         * 初始化卡片
         */
        initializeCards() {
            const cards = document.querySelectorAll('.spot-favorites-card, .itinerary-list-card');
            cards.forEach((card, index) => {
                // 設定動畫延遲
                if (!prefersReducedMotion) {
                    card.style.animationDelay = `${index * 0.1}s`;
                    card.classList.add('spot-favorites-card--fade-in');
                }
                
                // 設定無障礙屬性
                card.setAttribute('tabindex', '0');
                card.setAttribute('role', 'article');
            });
        },

        /**
         * 更新收藏數量
         */
        updateFavoriteCount() {
            const cards = document.querySelectorAll('.spot-favorites-card, .itinerary-list-card');
            this.state.favoriteCount = cards.length;
            
            const countElement = document.querySelector('.spot-favorites-count strong, #favorites-count');
            if (countElement) {
                countElement.textContent = this.state.favoriteCount;
            }
        },

        /**
         * 設定即時同步功能
         */
        setupRealtimeSync() {
            // 監聽 localStorage 變更事件（跨分頁同步）
            window.addEventListener('storage', (e) => {
                if (e.key === 'spotFavoriteChange') {
                    this.handleFavoriteChangeEvent(e.newValue);
                }
            });

            // 監聽自定義事件（同一頁面內的同步）
            document.addEventListener('spotFavoriteChanged', (e) => {
                this.handleFavoriteChangeEvent(JSON.stringify(e.detail));
            });

            // 監聽頁面可見性變化，當頁面重新變為可見時檢查更新
            document.addEventListener('visibilitychange', () => {
                if (!document.hidden) {
                    this.checkForUpdates();
                }
            });

            console.log('即時同步功能已設定');
        },

        /**
         * 處理收藏變更事件
         */
        handleFavoriteChangeEvent(dataString) {
            if (!dataString) return;

            try {
                const data = JSON.parse(dataString);
                const { spotId, isFavorited, timestamp, source } = data;

                if (!spotId) return;

                // 檢查事件是否太舊（超過30秒）
                const now = new Date().getTime();
                if (timestamp && (now - timestamp) > 30000) {
                    return;
                }

                console.log('收到收藏變更事件:', data);

                // 如果事件來源是當前頁面，且我們在收藏頁面，則不需要額外處理
                if (source === 'current-page' && window.location.pathname.includes('/spot/favorites')) {
                    console.log('忽略當前頁面在收藏頁面的自身事件');
                    return;
                }

                // 根據當前頁面類型處理同步
                if (window.location.pathname.includes('/spot/favorites')) {
                    this.handleFavoritePageSync(spotId, isFavorited);
                } else {
                    // 如果在其他頁面（如首頁），更新按鈕狀態
                    this.handleOtherPageSync(spotId, isFavorited);
                }
            } catch (error) {
                console.error('處理收藏變更事件時出錯:', error);
            }
        },

        /**
         * 處理收藏頁面的同步
         */
        handleFavoritePageSync(spotId, isFavorited) {
            // 如果在收藏頁面
            if (window.location.pathname.includes('/spot/favorites')) {
                const button = document.querySelector(`.itinerary-list-card__favorite[data-spot-id="${spotId}"]`);
                
                if (button) {
                    if (!isFavorited) {
                        // 如果取消收藏，移除卡片
                        const card = button.closest('.itinerary-list-item');
                        if (card) {
                            this.removeCardWithAnimation(card);
                            this.updateFavoriteCount();
                            
                            // 檢查是否還有收藏
                            setTimeout(() => {
                                if (this.state.favoriteCount === 0) {
                                    this.showEmptyState();
                                }
                            }, this.config.animationDuration);
                            
                            this.showToast('景點已從收藏中移除', 'info');
                        }
                    } else {
                        // 如果是加入收藏，更新按鈕狀態
                        this.updateFavoriteButtonState(button, true);
                        this.showToast('收藏狀態已更新', 'success');
                    }
                } else if (isFavorited) {
                    // 如果是新增的收藏且當前頁面沒有這個景點，重新載入頁面
                    this.showToast('新增了一個收藏景點，重新整理頁面以查看', 'success');
                    setTimeout(() => {
                        window.location.reload();
                    }, 1500);
                }
            } else {
                // 如果不在收藏頁面，只更新按鈕狀態
                const button = document.querySelector(`.spot-index-spot-card__favorite[data-spot-id="${spotId}"], .itinerary-list-card__favorite[data-spot-id="${spotId}"]`);
                if (button) {
                    this.updateFavoriteButtonState(button, isFavorited);
                }
            }
        },

        /**
         * 處理其他頁面的同步
         */
        handleOtherPageSync(spotId, isFavorited) {
            // 更新首頁或其他頁面的收藏按鈕狀態
            const buttons = document.querySelectorAll(`[data-spot-id="${spotId}"]`);
            
            buttons.forEach(button => {
                if (button.classList.contains('spot-index-spot-card__favorite') || 
                    button.classList.contains('itinerary-list-card__favorite')) {
                    this.updateFavoriteButtonState(button, isFavorited);
                    console.log(`已同步景點 ${spotId} 的收藏狀態: ${isFavorited ? '已收藏' : '未收藏'}`);
                }
            });
            
            // 顯示同步提示（可選）
            if (buttons.length > 0) {
                const message = isFavorited ? '景點已加入收藏' : '景點已取消收藏';
                // 可以選擇性地顯示提示，避免過多提示干擾使用者
                console.log(message);
            }
        },

        /**
         * 檢查更新
         */
        async checkForUpdates() {
            // 當頁面重新變為可見時，檢查是否有收藏狀態變更
            if (window.location.pathname.includes('/spot/favorites')) {
                // 在收藏頁面，可以重新載入以確保資料最新
                const lastCheck = localStorage.getItem('lastFavoriteCheck');
                const now = new Date().getTime();
                
                if (!lastCheck || (now - parseInt(lastCheck)) > 60000) { // 1分鐘檢查一次
                    localStorage.setItem('lastFavoriteCheck', now.toString());
                    
                    // 可以在這裡添加檢查邏輯，或者簡單地重新載入
                    console.log('檢查收藏頁面更新');
                }
            }
        },

        /**
         * 切換收藏狀態
         */
        async toggleFavorite(spotId, button) {
            if (!spotId || !button) return;
            
            // 防止重複點擊
            if (button.disabled || button.classList.contains('loading')) return;
            
            // 設置按鈕為載入狀態
            button.classList.add('loading');
            const originalContent = button.innerHTML;
            button.innerHTML = '<span class="material-icons loading-spinner">sync</span>';
            button.disabled = true;
            
            try {
                const response = await fetch(`${this.config.apiEndpoint}/${spotId}/toggle`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'X-Requested-With': 'XMLHttpRequest'
                    }
                });

                if (!response.ok) {
                    if (response.status === 401) {
                        // 未登入處理
                        this.showLoginDialog(button, originalContent);
                        throw new Error('請先登入');
                    }
                    throw new Error('操作失敗');
                }

                const result = await response.json();

                if (result.success) {
                    // 更新按鈕狀態
                    this.updateFavoriteButtonState(button, result.data.isFavorited);
                    
                    // 顯示提示訊息
                    this.showToast(result.data.message || (result.data.isFavorited ? '已加入收藏' : '已取消收藏'), 
                                  result.data.isFavorited ? 'success' : 'info');
                    
                    // 通知其他頁面
                    localStorage.setItem('spotFavoriteChange', JSON.stringify({
                        spotId: spotId,
                        isFavorited: result.data.isFavorited,
                        timestamp: new Date().getTime()
                    }));
                    
                    // 觸發同一頁面內的即時同步事件
                    const syncEvent = new CustomEvent('spotFavoriteChanged', {
                        detail: {
                            spotId: spotId,
                            isFavorited: result.data.isFavorited,
                            timestamp: new Date().getTime(),
                            source: 'current-page'
                        }
                    });
                    document.dispatchEvent(syncEvent);
                    
                    // 如果在收藏頁面且取消了收藏，則移除卡片
                    if (!result.data.isFavorited && window.location.pathname.includes('/spot/favorites')) {
                        const card = button.closest('.itinerary-list-item');
                        if (card) {
                            this.removeCardWithAnimation(card);
                            this.updateFavoriteCount();
                            
                            // 檢查是否還有收藏
                            if (this.state.favoriteCount === 0) {
                                this.showEmptyState();
                            }
                        }
                    }
                } else {
                    // 操作失敗
                    this.showToast(result.message || '操作失敗', 'error');
                    this.resetButton(button, originalContent);
                }
            } catch (error) {
                console.error('收藏操作失敗:', error);
                if (error.message !== '請先登入') {
                    this.showToast('操作失敗，請稍後再試', 'error');
                    this.resetButton(button, originalContent);
                }
            }
        },

        /**
         * 更新收藏按鈕狀態
         */
        updateFavoriteButtonState(button, isFavorited) {
            if (!button) return;
            
            button.classList.remove('loading');
            button.disabled = false;
            
            if (isFavorited) {
                button.innerHTML = '<span class="material-icons">favorite</span>';
                button.classList.add('favorited');
                button.title = '取消收藏';
                button.setAttribute('aria-label', '取消收藏此景點');
            } else {
                button.innerHTML = '<span class="material-icons">favorite_border</span>';
                button.classList.remove('favorited');
                button.title = '加入收藏';
                button.setAttribute('aria-label', '收藏此景點');
            }
        },

        /**
         * 重置按鈕狀態
         */
        resetButton(button, originalContent) {
            if (!button) return;
            
            button.classList.remove('loading');
            button.disabled = false;
            button.innerHTML = originalContent;
        },

        /**
         * 顯示登入對話框
         */
        showLoginDialog(button, originalContent) {
            // 檢查是否已存在對話框
            let dialog = document.querySelector('.login-dialog');
            
            if (!dialog) {
                // 創建對話框
                dialog = document.createElement('div');
                dialog.className = 'login-dialog';
                dialog.innerHTML = `
                    <div class="login-dialog__content">
                        <div class="login-dialog__header">
                            <h3 class="login-dialog__title">需要登入</h3>
                            <button class="login-dialog__close" aria-label="關閉">
                                <span class="material-icons">close</span>
                            </button>
                        </div>
                        <div class="login-dialog__body">
                            <p>請先登入會員，才能收藏景點。</p>
                        </div>
                        <div class="login-dialog__footer">
                            <button class="login-dialog__button login-dialog__button--secondary" data-action="cancel">稍後再說</button>
                            <button class="login-dialog__button login-dialog__button--primary" data-action="login">前往登入</button>
                        </div>
                    </div>
                `;
                
                document.body.appendChild(dialog);
                
                // 綁定關閉按鈕事件
                const closeButton = dialog.querySelector('.login-dialog__close');
                closeButton.addEventListener('click', () => this.closeLoginDialog(button, originalContent));
                
                // 綁定取消按鈕事件
                const cancelButton = dialog.querySelector('[data-action="cancel"]');
                cancelButton.addEventListener('click', () => this.closeLoginDialog(button, originalContent));
                
                // 綁定登入按鈕事件
                const loginButton = dialog.querySelector('[data-action="login"]');
                loginButton.addEventListener('click', () => {
                    // 保存當前頁面URL到localStorage，以便登入後重定向回來
                    localStorage.setItem('loginRedirectUrl', window.location.href);
                    // 跳轉到登入頁面，並帶上redirect參數
                    window.location.href = '/members/login?redirect=' + encodeURIComponent(window.location.href);
                });
            }
            
            // 顯示對話框
            setTimeout(() => {
                dialog.classList.add('login-dialog--show');
            }, 10);
        },

        /**
         * 關閉登入對話框
         */
        closeLoginDialog(button, originalContent) {
            const dialog = document.querySelector('.login-dialog');
            if (dialog) {
                dialog.classList.remove('login-dialog--show');
                setTimeout(() => {
                    dialog.remove();
                }, 300);
                
                // 恢復按鈕狀態
                if (button) {
                    this.resetButton(button, originalContent);
                }
            }
        },

        /**
         * 移除卡片動畫
         */
        async removeCardWithAnimation(card) {
            if (!prefersReducedMotion) {
                card.style.transition = 'opacity 0.3s ease, transform 0.3s ease';
                card.style.transform = 'scale(0.8)';
                card.style.opacity = '0';
                await new Promise(resolve => setTimeout(resolve, this.config.animationDuration));
            }
            card.remove();
        },

        /**
         * 顯示空狀態
         */
        showEmptyState() {
            const grid = document.querySelector('#favorites-grid, .itinerary-list-grid');
            const empty = document.querySelector('#favorites-empty, .itinerary-list-empty');
            
            if (grid && empty) {
                grid.style.display = 'none';
                empty.style.display = 'block';
            }
        },

        /**
         * 顯示提示訊息
         */
        showToast(message, type = 'info') {
            // 清除現有的提示
            if (this.state.toastTimeout) {
                clearTimeout(this.state.toastTimeout);
            }

            // 移除現有的提示
            const existingToast = document.querySelector('.spot-favorites-toast');
            if (existingToast) {
                existingToast.remove();
            }

            // 創建新的提示
            const toast = document.createElement('div');
            toast.className = `spot-favorites-toast spot-favorites-toast--${type}`;
            toast.innerHTML = `
                <div class="spot-favorites-toast__icon">
                    <span class="material-icons">${this.getToastIcon(type)}</span>
                </div>
                <div class="spot-favorites-toast__content">
                    <span>${message}</span>
                </div>
                <button class="spot-favorites-toast__close" aria-label="關閉提示">
                    <span class="material-icons">close</span>
                </button>
            `;

            // 添加樣式
            toast.style.cssText = `
                position: fixed;
                top: 20px;
                right: 20px;
                background: ${this.getToastColor(type)};
                color: white;
                padding: 1rem 1.5rem;
                border-radius: 8px;
                box-shadow: 0 4px 12px rgba(0,0,0,0.15);
                display: flex;
                align-items: center;
                gap: 0.75rem;
                z-index: 1000;
                max-width: 400px;
                animation: spot-favorites-toast-slide-in 0.3s ease-out;
            `;

            // 添加動畫樣式
            const style = document.createElement('style');
            style.textContent = `
                @keyframes spot-favorites-toast-slide-in {
                    from { transform: translateX(100%); opacity: 0; }
                    to { transform: translateX(0); opacity: 1; }
                }
                @keyframes spot-favorites-toast-slide-out {
                    from { transform: translateX(0); opacity: 1; }
                    to { transform: translateX(100%); opacity: 0; }
                }
            `;
            document.head.appendChild(style);

            // 關閉按鈕事件
            const closeBtn = toast.querySelector('.spot-favorites-toast__close');
            closeBtn.addEventListener('click', () => this.hideToast(toast));

            // 添加到頁面
            document.body.appendChild(toast);

            // 自動隱藏
            this.state.toastTimeout = setTimeout(() => {
                this.hideToast(toast);
            }, this.config.alertAutoHideDelay);
        },

        /**
         * 隱藏提示訊息
         */
        hideToast(toast) {
            if (toast && toast.parentNode) {
                toast.style.animation = 'spot-favorites-toast-slide-out 0.3s ease-in';
                setTimeout(() => {
                    if (toast.parentNode) {
                        toast.parentNode.removeChild(toast);
                    }
                }, 300);
            }
        },

        /**
         * 隱藏警告訊息
         */
        hideAlert(alert) {
            alert.style.opacity = '0';
            setTimeout(() => {
                if (alert.parentNode) {
                    alert.parentNode.removeChild(alert);
                }
            }, 300);
        },

        /**
         * 取得提示圖標
         */
        getToastIcon(type) {
            const icons = {
                success: 'check_circle',
                error: 'error',
                warning: 'warning',
                info: 'info'
            };
            return icons[type] || icons.info;
        },

        /**
         * 取得提示顏色
         */
        getToastColor(type) {
            const colors = {
                success: '#4caf50',
                error: '#f44336',
                warning: '#ff9800',
                info: '#2196f3'
            };
            return colors[type] || colors.info;
        }
    };

    // 當 DOM 載入完成後初始化
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', () => SpotFavorites.init());
    } else {
        SpotFavorites.init();
    }

    // 暴露到全域
    window.SpotFavorites = SpotFavorites;

})(); 