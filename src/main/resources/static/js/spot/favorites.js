/**
 * 景點收藏頁面 JavaScript
 * @description 處理收藏景點的移除、分享、動畫效果和用戶互動
 * @version 2.1
 * 
 * 功能包含：
 * - 收藏移除功能
 * - 分享功能
 * - 提示訊息顯示
 * - 卡片動畫效果
 * - 鍵盤快捷鍵
 * - 無障礙功能增強
 * - 配合會員模組佈局
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
            shareErrorMessage: '分享功能暫時無法使用',
            // 配合會員模組的佈局配置
            memberLayout: {
                sidebarWidth: 200,
                breakpoint: 768
            }
        },

        // 狀態管理
        state: {
            isRemoving: false,
            favoriteCount: 0,
            toastTimeout: null,
            isMemberLayout: false
        },

        /**
         * 初始化
         */
        init() {
            this.detectLayout();
            this.bindEvents();
            this.initializeCards();
            this.updateFavoriteCount();
            this.setupKeyboardShortcuts();
            this.setupAccessibility();
            this.initializeAnimations();
            this.setupMemberLayoutIntegration();
            console.log('景點收藏頁面已初始化');
        },

        /**
         * 檢測佈局類型
         */
        detectLayout() {
            // 檢查是否在會員模組的佈局中
            const memberNav = document.querySelector('.left-nav');
            const memberContent = document.querySelector('.content-area-main');
            this.state.isMemberLayout = !!(memberNav && memberContent);
            
            if (this.state.isMemberLayout) {
                console.log('檢測到會員模組佈局');
            }
        },

        /**
         * 設定會員模組佈局整合
         */
        setupMemberLayoutIntegration() {
            if (!this.state.isMemberLayout) return;

            // 監聽會員模組的側邊欄切換事件
            this.handleMemberSidebarToggle();
            
            // 調整容器寬度
            this.adjustContainerForMemberLayout();
        },

        /**
         * 處理會員模組側邊欄切換
         */
        handleMemberSidebarToggle() {
            // 監聽視窗大小變化，處理側邊欄響應式切換
            window.addEventListener('resize', this.debounce(() => {
                this.adjustContainerForMemberLayout();
            }, 250));
        },

        /**
         * 調整容器以配合會員模組佈局
         */
        adjustContainerForMemberLayout() {
            const container = document.querySelector('.spot-favorites-container');
            if (!container) return;

            const isDesktop = window.innerWidth >= this.config.memberLayout.breakpoint;
            const sidebarVisible = isDesktop; // 在桌面版側邊欄總是可見

            if (sidebarVisible) {
                container.style.marginLeft = '0';
                container.style.width = `calc(100% - ${this.config.memberLayout.sidebarWidth}px)`;
            } else {
                container.style.marginLeft = '0';
                container.style.width = '100%';
            }
        },

        /**
         * 綁定事件
         */
        bindEvents() {
            // 移除收藏按鈕事件委託
            document.addEventListener('click', (e) => {
                if (e.target.closest('.spot-favorites-card__remove-btn')) {
                    e.preventDefault();
                    e.stopPropagation();
                    const button = e.target.closest('.spot-favorites-card__remove-btn');
                    const spotId = button.dataset.spotId;
                    if (spotId) {
                        this.handleRemoveFavorite(spotId, button);
                    }
                }
            });

            // 分享按鈕事件委託
            document.addEventListener('click', (e) => {
                if (e.target.closest('.spot-favorites-card__share-btn')) {
                    e.preventDefault();
                    e.stopPropagation();
                    const button = e.target.closest('.spot-favorites-card__share-btn');
                    const spotId = button.dataset.spotId;
                    const spotName = button.dataset.spotName;
                    if (spotId && spotName) {
                        this.handleShareSpot(spotId, spotName);
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

            // 查看詳情按鈕增強
            document.addEventListener('click', (e) => {
                if (e.target.closest('.spot-favorites-card__view-btn')) {
                    const button = e.target.closest('.spot-favorites-card__view-btn');
                    this.addRippleEffect(button, e);
                }
            });

            // 監聽視窗大小變化
            window.addEventListener('resize', this.debounce(() => {
                this.handleResize();
            }, 250));
        },

        /**
         * 初始化卡片
         */
        initializeCards() {
            const cards = document.querySelectorAll('.spot-favorites-card');
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
            const cards = document.querySelectorAll('.spot-favorites-card');
            this.state.favoriteCount = cards.length;
            
            const countElement = document.querySelector('.spot-favorites-count strong');
            if (countElement) {
                countElement.textContent = this.state.favoriteCount;
            }

            // 如果是在會員模組中，可能需要更新會員中心的收藏數量顯示
            this.updateMemberCenterFavoriteCount();
        },

        /**
         * 更新會員中心的收藏數量顯示
         */
        updateMemberCenterFavoriteCount() {
            if (!this.state.isMemberLayout) return;

            // 尋找會員中心側邊欄中的收藏數量顯示
            const memberFavoritesLink = document.querySelector('.left-nav a[href*="favorites"]');
            if (memberFavoritesLink) {
                // 移除舊的數量標籤
                const oldBadge = memberFavoritesLink.querySelector('.favorite-count-badge');
                if (oldBadge) {
                    oldBadge.remove();
                }

                // 如果有收藏，添加數量標籤
                if (this.state.favoriteCount > 0) {
                    const badge = document.createElement('span');
                    badge.className = 'favorite-count-badge';
                    badge.textContent = this.state.favoriteCount;
                    badge.style.cssText = `
                        background: var(--md-sys-color-primary);
                        color: var(--md-sys-color-on-primary);
                        border-radius: 50%;
                        width: 20px;
                        height: 20px;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        font-size: 0.75rem;
                        font-weight: bold;
                        margin-left: auto;
                    `;
                    memberFavoritesLink.appendChild(badge);
                }
            }
        },

        /**
         * 處理移除收藏
         */
        async handleRemoveFavorite(spotId, button) {
            if (this.state.isRemoving) return;

            // 確認對話框
            if (!confirm(this.config.confirmRemoveText)) {
                return;
            }

            this.state.isRemoving = true;
            const card = button.closest('.spot-favorites-card');
            const originalContent = button.innerHTML;
            
            // 添加載入狀態
            button.disabled = true;
            button.innerHTML = '<span class="material-icons">hourglass_empty</span>';
            
            try {
                const response = await fetch(`${this.config.apiEndpoint}/${spotId}`, {
                    method: 'DELETE',
                    headers: {
                        'Content-Type': 'application/json',
                        'X-Requested-With': 'XMLHttpRequest'
                    }
                });

                const result = await response.json();

                if (response.ok && result.success) {
                    // 成功移除
                    this.showToast(result.message || this.config.removeSuccessMessage, 'success');
                    await this.removeCardWithAnimation(card);
                    this.updateFavoriteCount();
                    
                    // 檢查是否還有收藏
                    if (this.state.favoriteCount === 0) {
                        this.showEmptyState();
                    }
                } else {
                    // 移除失敗
                    this.showToast(result.message || this.config.removeErrorMessage, 'error');
                    this.resetRemoveButton(button, originalContent);
                }
            } catch (error) {
                console.error('移除收藏時發生錯誤:', error);
                this.showToast(this.config.removeErrorMessage, 'error');
                this.resetRemoveButton(button, originalContent);
            } finally {
                this.state.isRemoving = false;
            }
        },

        /**
         * 處理分享景點
         */
        async handleShareSpot(spotId, spotName) {
            const shareUrl = `${window.location.origin}/spot/detail/${spotId}`;
            const shareText = `推薦景點：${spotName}`;
            
            try {
                // 嘗試使用 Web Share API
                if (navigator.share) {
                    await navigator.share({
                        title: spotName,
                        text: shareText,
                        url: shareUrl
                    });
                    this.showToast('分享成功', 'success');
                } else {
                    // 回退到複製連結
                    await this.copyToClipboard(shareUrl);
                    this.showToast(this.config.shareSuccessMessage, 'success');
                }
            } catch (error) {
                console.error('分享失敗:', error);
                this.showToast(this.config.shareErrorMessage, 'error');
            }
        },

        /**
         * 複製文字到剪貼簿
         */
        async copyToClipboard(text) {
            try {
                if (navigator.clipboard) {
                    await navigator.clipboard.writeText(text);
                } else {
                    // 回退方法
                    const textArea = document.createElement('textarea');
                    textArea.value = text;
                    textArea.style.position = 'fixed';
                    textArea.style.left = '-999999px';
                    textArea.style.top = '-999999px';
                    document.body.appendChild(textArea);
                    textArea.focus();
                    textArea.select();
                    document.execCommand('copy');
                    document.body.removeChild(textArea);
                }
            } catch (error) {
                throw new Error('複製失敗');
            }
        },

        /**
         * 重置移除按鈕
         */
        resetRemoveButton(button, originalContent) {
            button.disabled = false;
            button.innerHTML = originalContent;
        },

        /**
         * 移除卡片動畫
         */
        async removeCardWithAnimation(card) {
            if (!prefersReducedMotion) {
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
            const container = document.querySelector('.spot-favorites-container');
            if (container) {
                const emptyState = document.querySelector('.spot-favorites-empty');
                if (emptyState) {
                    emptyState.style.display = 'block';
                }
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

            // 添加樣式 - 配合會員模組佈局調整位置
            const toastPosition = this.state.isMemberLayout ? 
                `top: 20px; right: ${this.config.memberLayout.sidebarWidth + 20}px;` : 
                'top: 20px; right: 20px;';

            toast.style.cssText = `
                position: fixed;
                ${toastPosition}
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
        },

        /**
         * 隱藏警告訊息
         */
        hideAlert(alert) {
            alert.style.animation = 'spot-favorites-alert-slide-out 0.3s ease-in';
            setTimeout(() => {
                if (alert.parentNode) {
                    alert.parentNode.removeChild(alert);
                }
            }, 300);
        },

        /**
         * 添加漣漪效果
         */
        addRippleEffect(element, event) {
            if (prefersReducedMotion) return;

            const ripple = document.createElement('span');
            const rect = element.getBoundingClientRect();
            const size = Math.max(rect.width, rect.height);
            const x = event.clientX - rect.left - size / 2;
            const y = event.clientY - rect.top - size / 2;

            ripple.style.cssText = `
                position: absolute;
                width: ${size}px;
                height: ${size}px;
                left: ${x}px;
                top: ${y}px;
                background: rgba(255, 255, 255, 0.3);
                border-radius: 50%;
                transform: scale(0);
                animation: spot-favorites-ripple 0.6s ease-out;
                pointer-events: none;
            `;

            element.style.position = 'relative';
            element.style.overflow = 'hidden';
            element.appendChild(ripple);

            setTimeout(() => {
                if (ripple.parentNode) {
                    ripple.parentNode.removeChild(ripple);
                }
            }, 600);
        },

        /**
         * 初始化動畫
         */
        initializeAnimations() {
            if (prefersReducedMotion) return;

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
                @keyframes spot-favorites-alert-slide-out {
                    from { transform: translateY(0); opacity: 1; }
                    to { transform: translateY(-10px); opacity: 0; }
                }
                @keyframes spot-favorites-ripple {
                    to { transform: scale(4); opacity: 0; }
                }
            `;
            document.head.appendChild(style);
        },

        /**
         * 設定鍵盤快捷鍵
         */
        setupKeyboardShortcuts() {
            document.addEventListener('keydown', (e) => {
                // ESC 鍵關閉提示
                if (e.key === 'Escape') {
                    const toast = document.querySelector('.spot-favorites-toast');
                    if (toast) {
                        this.hideToast(toast);
                    }
                }

                // Enter 鍵在卡片上時查看詳情
                if (e.key === 'Enter' && e.target.classList.contains('spot-favorites-card')) {
                    const viewBtn = e.target.querySelector('.spot-favorites-card__view-btn');
                    if (viewBtn) {
                        viewBtn.click();
                    }
                }
            });
        },

        /**
         * 設定無障礙功能
         */
        setupAccessibility() {
            // 為卡片添加鍵盤導航
            const cards = document.querySelectorAll('.spot-favorites-card');
            cards.forEach(card => {
                card.addEventListener('keydown', (e) => {
                    if (e.key === 'Enter' || e.key === ' ') {
                        e.preventDefault();
                        const viewBtn = card.querySelector('.spot-favorites-card__view-btn');
                        if (viewBtn) {
                            viewBtn.click();
                        }
                    }
                });
            });

            // 更新 ARIA 標籤
            this.updateAriaLabels();
        },

        /**
         * 更新 ARIA 標籤
         */
        updateAriaLabels() {
            const cards = document.querySelectorAll('.spot-favorites-card');
            cards.forEach((card, index) => {
                const title = card.querySelector('.spot-favorites-card__title');
                if (title) {
                    card.setAttribute('aria-label', `收藏的景點：${title.textContent}，第 ${index + 1} 個`);
                }
            });
        },

        /**
         * 處理視窗大小變化
         */
        handleResize() {
            this.updateCardLayout();
            this.adjustContainerForMemberLayout();
        },

        /**
         * 更新卡片佈局
         */
        updateCardLayout() {
            const grid = document.querySelector('.spot-favorites-grid');
            if (grid) {
                const cards = grid.querySelectorAll('.spot-favorites-card');
                cards.forEach(card => {
                    // 重新計算卡片高度
                    const imageContainer = card.querySelector('.spot-favorites-card__image-container');
                    const content = card.querySelector('.spot-favorites-card__content');
                    if (imageContainer && content) {
                        const imageHeight = imageContainer.offsetHeight;
                        content.style.height = `calc(100% - ${imageHeight}px)`;
                    }
                });
            }
        },

        /**
         * 防抖函數
         */
        debounce(func, wait) {
            let timeout;
            return function executedFunction(...args) {
                const later = () => {
                    clearTimeout(timeout);
                    func(...args);
                };
                clearTimeout(timeout);
                timeout = setTimeout(later, wait);
            };
        }
    };

    // 當 DOM 載入完成後初始化
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', () => SpotFavorites.init());
    } else {
        SpotFavorites.init();
    }

    // 暴露到全域（可選）
    window.SpotFavorites = SpotFavorites;

})(); 