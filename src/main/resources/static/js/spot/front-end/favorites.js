/**
 * 我的收藏頁面 JavaScript
 * @description 處理收藏景點的移除、分享、動畫效果和用戶互動
 * @version 2.0
 * 
 * 功能包含：
 * - 收藏移除功能
 * - 分享功能
 * - 提示訊息顯示
 * - 卡片動畫效果
 * - 鍵盤快捷鍵
 * - 無障礙功能增強
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
            this.setupKeyboardShortcuts();
            this.setupAccessibility();
            this.initializeAnimations();
            console.log('我的收藏頁面已初始化');
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
            const shareData = {
                title: `${spotName} - 島遊Kha`,
                text: `來看看這個很棒的景點：${spotName}`,
                url: `${window.location.origin}/spot/detail/${spotId}`
            };

            try {
                if (navigator.share && navigator.canShare && navigator.canShare(shareData)) {
                    // 使用原生分享 API
                    await navigator.share(shareData);
                    this.showToast('分享成功', 'success');
                } else {
                    // 備用方案：複製到剪貼簿
                    await this.copyToClipboard(shareData.url);
                    this.showToast(this.config.shareSuccessMessage, 'success');
                }
            } catch (error) {
                if (error.name !== 'AbortError') {
                    console.error('分享失敗:', error);
                    // 嘗試複製到剪貼簿作為備用
                    try {
                        await this.copyToClipboard(shareData.url);
                        this.showToast(this.config.shareSuccessMessage, 'success');
                    } catch (clipboardError) {
                        console.error('複製到剪貼簿失敗:', clipboardError);
                        this.showToast(this.config.shareErrorMessage, 'error');
                    }
                }
            }
        },

        /**
         * 複製到剪貼簿
         */
        async copyToClipboard(text) {
            if (navigator.clipboard && window.isSecureContext) {
                return navigator.clipboard.writeText(text);
            } else {
                // 備用方案
                const textArea = document.createElement('textarea');
                textArea.value = text;
                textArea.style.position = 'fixed';
                textArea.style.left = '-999999px';
                textArea.style.top = '-999999px';
                document.body.appendChild(textArea);
                textArea.focus();
                textArea.select();
                
                try {
                    document.execCommand('copy');
                    textArea.remove();
                } catch (error) {
                    textArea.remove();
                    throw error;
                }
            }
        },

        /**
         * 重置移除按鈕
         */
        resetRemoveButton(button, originalContent) {
            button.disabled = false;
            button.innerHTML = originalContent || '<span class="material-icons">close</span>';
        },

        /**
         * 移除卡片動畫
         */
        async removeCardWithAnimation(card) {
            return new Promise((resolve) => {
                if (prefersReducedMotion) {
                    card.remove();
                    resolve();
                    return;
                }

                card.style.transition = 'all 0.3s ease-out';
                card.style.transform = 'translateX(-100%)';
                card.style.opacity = '0';
                
                setTimeout(() => {
                    card.remove();
                    resolve();
                }, 300);
            });
        },

        /**
         * 顯示空狀態
         */
        showEmptyState() {
            const container = document.querySelector('.container');
            if (!container) return;

            // 移除現有的收藏列表
            const favoritesContent = container.querySelector('.spot-favorites-content');
            if (favoritesContent) {
                favoritesContent.remove();
            }

            // 檢查是否已經有空狀態
            if (container.querySelector('.spot-favorites-empty')) {
                return;
            }

            // 創建空狀態 HTML
            const emptyStateHtml = `
                <section class="spot-favorites-empty" role="status" aria-live="polite">
                    <div class="spot-favorites-empty__icon">
                        <span class="material-icons" aria-hidden="true">heart_broken</span>
                    </div>
                    <h2 class="spot-favorites-empty__title">還沒有收藏任何景點</h2>
                    <p class="spot-favorites-empty__description">
                        快去探索精彩的台灣景點，加入您的收藏清單吧！
                    </p>
                    <div class="spot-favorites-empty__actions">
                        <a href="/spot/list" class="spot-favorites-empty__action-btn spot-favorites-empty__action-btn--primary">
                            <span class="material-icons" aria-hidden="true">search</span>
                            探索景點
                        </a>
                        <a href="/spot/" class="spot-favorites-empty__action-btn spot-favorites-empty__action-btn--secondary">
                            <span class="material-icons" aria-hidden="true">home</span>
                            回到首頁
                        </a>
                    </div>
                </section>
            `;

            container.insertAdjacentHTML('beforeend', emptyStateHtml);
        },

        /**
         * 顯示 Toast 通知
         */
        showToast(message, type = 'info') {
            // 清除現有的 Toast
            if (this.state.toastTimeout) {
                clearTimeout(this.state.toastTimeout);
            }

            const existingToast = document.querySelector('.spot-favorites-toast');
            if (existingToast) {
                existingToast.remove();
            }

            // 創建 Toast
            const toast = document.createElement('div');
            toast.className = `spot-favorites-toast spot-favorites-toast--${type}`;
            toast.innerHTML = `
                <div class="spot-favorites-toast__icon">
                    <span class="material-icons">${this.getToastIcon(type)}</span>
                </div>
                <div class="spot-favorites-toast__message">${message}</div>
                <button class="spot-favorites-toast__close" aria-label="關閉通知">
                    <span class="material-icons">close</span>
                </button>
            `;

            // 添加樣式
            toast.style.cssText = `
                position: fixed;
                top: 20px;
                right: 20px;
                background: var(--md-sys-color-surface);
                color: var(--md-sys-color-on-surface);
                padding: 1rem 1.5rem;
                border-radius: 12px;
                box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
                z-index: 1000;
                display: flex;
                align-items: center;
                gap: 0.75rem;
                max-width: 400px;
                transform: translateX(100%);
                transition: transform 0.3s ease-out;
                border-left: 4px solid ${this.getToastColor(type)};
            `;

            document.body.appendChild(toast);

            // 動畫顯示
            requestAnimationFrame(() => {
                toast.style.transform = 'translateX(0)';
            });

            // 綁定關閉事件
            toast.querySelector('.spot-favorites-toast__close').addEventListener('click', () => {
                this.hideToast(toast);
            });

            // 自動隱藏
            this.state.toastTimeout = setTimeout(() => {
                this.hideToast(toast);
            }, this.config.alertAutoHideDelay);
        },

        /**
         * 隱藏 Toast
         */
        hideToast(toast) {
            if (!toast || !toast.parentElement) return;
            
            toast.style.transform = 'translateX(100%)';
            setTimeout(() => {
                if (toast.parentElement) {
                    toast.remove();
                }
            }, 300);
        },

        /**
         * 獲取 Toast 圖標
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
         * 獲取 Toast 顏色
         */
        getToastColor(type) {
            const colors = {
                success: 'var(--md-sys-color-primary)',
                error: 'var(--md-sys-color-error)',
                warning: 'var(--md-sys-color-secondary)',
                info: 'var(--md-sys-color-outline)'
            };
            return colors[type] || colors.info;
        },

        /**
         * 隱藏提示訊息
         */
        hideAlert(alert) {
            if (!alert) return;
            
            if (prefersReducedMotion) {
                alert.style.display = 'none';
            } else {
                alert.style.transition = 'all 0.3s ease-out';
                alert.style.transform = 'translateY(-20px)';
                alert.style.opacity = '0';
                
                setTimeout(() => {
                    alert.style.display = 'none';
                }, 300);
            }
        },

        /**
         * 添加漣漪效果
         */
        addRippleEffect(element, event) {
            if (prefersReducedMotion) return;

            const rect = element.getBoundingClientRect();
            const size = Math.max(rect.width, rect.height);
            const x = event.clientX - rect.left - size / 2;
            const y = event.clientY - rect.top - size / 2;

            const ripple = document.createElement('span');
            ripple.style.cssText = `
                position: absolute;
                width: ${size}px;
                height: ${size}px;
                left: ${x}px;
                top: ${y}px;
                background: rgba(255, 255, 255, 0.3);
                border-radius: 50%;
                transform: scale(0);
                animation: ripple 0.6s linear;
                pointer-events: none;
            `;

            element.style.position = 'relative';
            element.style.overflow = 'hidden';
            element.appendChild(ripple);

            // 添加動畫樣式
            if (!document.querySelector('#ripple-keyframes')) {
                const style = document.createElement('style');
                style.id = 'ripple-keyframes';
                style.textContent = `
                    @keyframes ripple {
                        to {
                            transform: scale(2);
                            opacity: 0;
                        }
                    }
                `;
                document.head.appendChild(style);
            }

            setTimeout(() => {
                ripple.remove();
            }, 600);
        },

        /**
         * 初始化動畫
         */
        initializeAnimations() {
            if (prefersReducedMotion) return;

            // 為頁面元素添加進入動畫
            const header = document.querySelector('.spot-favorites-header');
            if (header) {
                header.style.opacity = '0';
                header.style.transform = 'translateY(-20px)';
                setTimeout(() => {
                    header.style.transition = 'all 0.6s ease-out';
                    header.style.opacity = '1';
                    header.style.transform = 'translateY(0)';
                }, 100);
            }
        },

        /**
         * 設定鍵盤快捷鍵
         */
        setupKeyboardShortcuts() {
            document.addEventListener('keydown', (e) => {
                // Ctrl + Shift + R: 重新載入
                if (e.ctrlKey && e.shiftKey && e.key === 'R') {
                    e.preventDefault();
                    location.reload();
                }

                // Ctrl + H: 回到首頁
                if (e.ctrlKey && e.key === 'h') {
                    e.preventDefault();
                    window.location.href = '/spot/';
                }

                // Ctrl + L: 前往景點列表
                if (e.ctrlKey && e.key === 'l') {
                    e.preventDefault();
                    window.location.href = '/spot/list';
                }

                // Escape: 關閉提示訊息
                if (e.key === 'Escape') {
                    const alerts = document.querySelectorAll('.spot-favorites-alert');
                    alerts.forEach(alert => this.hideAlert(alert));
                    
                    const toasts = document.querySelectorAll('.spot-favorites-toast');
                    toasts.forEach(toast => this.hideToast(toast));
                }
            });
        },

        /**
         * 設定無障礙功能
         */
        setupAccessibility() {
            // 為卡片添加鍵盤導覽
            document.addEventListener('keydown', (e) => {
                const focusedCard = document.activeElement;
                if (!focusedCard.classList.contains('spot-favorites-card')) return;

                if (e.key === 'Enter' || e.key === ' ') {
                    e.preventDefault();
                    const viewBtn = focusedCard.querySelector('.spot-favorites-card__view-btn');
                    if (viewBtn) {
                        viewBtn.click();
                    }
                }

                if (e.key === 'Delete' || e.key === 'Backspace') {
                    e.preventDefault();
                    const removeBtn = focusedCard.querySelector('.spot-favorites-card__remove-btn');
                    if (removeBtn) {
                        removeBtn.click();
                    }
                }
            });

            // 動態更新 ARIA 標籤
            this.updateAriaLabels();
        },

        /**
         * 更新 ARIA 標籤
         */
        updateAriaLabels() {
            const cards = document.querySelectorAll('.spot-favorites-card');
            cards.forEach((card, index) => {
                card.setAttribute('aria-label', `收藏景點 ${index + 1}，共 ${cards.length} 個`);
            });
        },

        /**
         * 處理視窗大小變化
         */
        handleResize() {
            // 更新卡片布局
            this.updateCardLayout();
        },

        /**
         * 更新卡片布局
         */
        updateCardLayout() {
            const grid = document.querySelector('.spot-favorites-grid');
            if (!grid) return;

            const cards = grid.querySelectorAll('.spot-favorites-card');
            const containerWidth = grid.offsetWidth;
            const cardMinWidth = 350;
            const gap = 24;
            const columns = Math.floor((containerWidth + gap) / (cardMinWidth + gap));
            
            if (columns !== this.state.currentColumns) {
                this.state.currentColumns = columns;
                // 可以在這裡添加布局變化的處理邏輯
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

    /**
     * DOM 載入完成後初始化
     */
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', () => {
            SpotFavorites.init();
        });
    } else {
        SpotFavorites.init();
    }

    // 導出模組供其他腳本使用
    window.SpotFavorites = SpotFavorites;

})(); 