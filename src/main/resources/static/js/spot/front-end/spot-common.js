/**
 * 景點模組通用 JavaScript 函數庫
 * 包含所有景點模組頁面共用的功能
 */

(function() {
    'use strict';
    
    // 全域變數
    const API_ENDPOINT = '/api/spot/favorites';
    
    // 初始化函數
    function init() {
        // 綁定登入按鈕事件
        bindLoginButton();
        
        // 初始化收藏按鈕
        initFavoriteButtons();
        
        console.log('景點模組通用腳本初始化完成');
    }
    
    // 綁定登入按鈕事件
    function bindLoginButton() {
        const loginButtons = document.querySelectorAll('.login-button, .nav-login-button');
        loginButtons.forEach(button => {
            if (button) {
                button.addEventListener('click', function(e) {
                    e.preventDefault();
                    // 保存當前頁面URL到localStorage，以便登入後重定向回來
                    localStorage.setItem('loginRedirectUrl', window.location.href);
                    // 跳轉到登入頁面，並帶上redirect參數
                    window.location.href = '/members/login?redirect=' + encodeURIComponent(window.location.href);
                });
            }
        });
    }
    
    // 初始化收藏按鈕
    function initFavoriteButtons() {
        // 同時支援 .favorite-button 和 .spot-index-spot-card__favorite 兩種類名
        const favoriteButtons = document.querySelectorAll('.favorite-button, .spot-index-spot-card__favorite');
        favoriteButtons.forEach(button => {
            if (button) {
                button.addEventListener('click', function(e) {
                    e.preventDefault();
                    // 支援兩種獲取spotId的方式
                    const spotId = this.dataset.spotId || this.getAttribute('data-spot-id');
                    if (spotId) {
                        toggleFavorite(spotId, this);
                    }
                });
            }
        });
    }
    
    // 切換收藏狀態
    async function toggleFavorite(spotId, button, providedOriginalHTML) {
        if (!spotId || !button) return;
        
        // 顯示載入狀態
        const originalHTML = providedOriginalHTML || button.innerHTML;
        button.innerHTML = '<span class="material-icons loading-spinner">sync</span>';
        button.disabled = true;
        
        try {
            const response = await fetch(`${API_ENDPOINT}/${spotId}/toggle`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            });
            
            if (response.ok) {
                const result = await response.json();
                
                if (result.success) {
                    // 更新按鈕狀態
                    updateFavoriteButtonState(button, result.data.isFavorited);
                    
                    // 顯示提示訊息
                    showToast(result.data.message, 'success');
                    
                    // 通知其他頁面更新收藏狀態
                    localStorage.setItem('spotFavoriteChange', JSON.stringify({
                        spotId: spotId,
                        isFavorited: result.data.isFavorited,
                        favoriteCount: result.data.favoriteCount,
                        timestamp: new Date().getTime()
                    }));
                } else {
                    // API 返回錯誤
                    showToast(result.message || '操作失敗，請稍後再試', 'error');
                    
                    // 重置按鈕狀態
                    button.innerHTML = originalHTML;
                    button.disabled = false;
                }
            } else if (response.status === 401) {
                // 未登入，顯示登入對話框
                showLoginDialog(button, originalHTML);
            } else {
                const error = await response.json();
                showToast(error.message || '操作失敗', 'error');
                
                // 重置按鈕狀態
                button.innerHTML = originalHTML;
                button.disabled = false;
            }
        } catch (error) {
            console.error('收藏操作失敗:', error);
            showToast('網路錯誤，請稍後再試', 'error');
            
            // 重置按鈕狀態
            button.innerHTML = originalHTML;
            button.disabled = false;
        }
    }
    
    // 更新收藏按鈕狀態
    function updateFavoriteButtonState(button, isFavorited) {
        if (!button) return;
        
        if (isFavorited) {
            button.innerHTML = '<span class="material-icons">favorite</span>';
            button.classList.add('favorited');
            button.setAttribute('title', '取消收藏');
            button.setAttribute('aria-label', '取消收藏此景點');
        } else {
            button.innerHTML = '<span class="material-icons">favorite_border</span>';
            button.classList.remove('favorited');
            button.setAttribute('title', '加入收藏');
            button.setAttribute('aria-label', '收藏此景點');
        }
        
        button.disabled = false;
    }
    
    // 顯示登入對話框
    function showLoginDialog(button, providedOriginalHTML) {
        // 檢查是否已經有登入對話框
        const existingDialog = document.querySelector('.login-dialog');
        if (existingDialog) {
            existingDialog.remove();
        }
        
        // 保存按鈕原始狀態，優先使用提供的原始HTML
        const originalHTML = providedOriginalHTML || button.innerHTML;
        
        // 創建對話框
        const dialog = document.createElement('div');
        dialog.className = 'login-dialog';
        dialog.id = 'spotCommonLoginDialog';
        dialog.innerHTML = `
            <div class="login-dialog__content">
                <div class="login-dialog__header">
                    <h3 class="login-dialog__title">請先登入</h3>
                    <button class="login-dialog__close" aria-label="關閉">
                        <span class="material-icons">close</span>
                    </button>
                </div>
                <div class="login-dialog__body">
                    <p>您需要先登入才能收藏景點。</p>
                    <p>是否前往登入頁面？</p>
                </div>
                <div class="login-dialog__footer">
                    <button class="login-dialog__button login-dialog__button--secondary" data-action="cancel">稍後再說</button>
                    <button class="login-dialog__button login-dialog__button--primary" data-action="login">前往登入</button>
                </div>
            </div>
        `;
        
        // 添加到頁面
        document.body.appendChild(dialog);
        
        // 確保DOM更新後再添加動畫效果
        requestAnimationFrame(() => {
            dialog.classList.add('login-dialog--show');
        });
        
        // 綁定事件
        const closeButton = dialog.querySelector('.login-dialog__close');
        const cancelButton = dialog.querySelector('[data-action="cancel"]');
        const loginButton = dialog.querySelector('[data-action="login"]');
        const dialogContent = dialog.querySelector('.login-dialog__content');
        
        // 點擊背景關閉對話框
        dialog.addEventListener('click', function(e) {
            if (e.target === dialog) {
                closeDialog();
            }
        });
        
        // 防止點擊內容區域時關閉對話框
        dialogContent.addEventListener('click', function(e) {
            e.stopPropagation();
        });
        
        // 關閉對話框函數
        function closeDialog() {
            // 防止重複關閉
            if (!dialog.classList.contains('login-dialog--show')) return;
            
            dialog.classList.remove('login-dialog--show');
            setTimeout(() => {
                // 確保對話框仍然存在於DOM中
                if (document.body.contains(dialog)) {
                    dialog.remove();
                }
            }, 300);
            
            // 恢復按鈕狀態
            restoreButtonState(button, originalHTML);
        }
        
        // 恢復按鈕狀態
        function restoreButtonState(button, originalHTML) {
            if (button) {
                button.innerHTML = originalHTML;
                button.disabled = false;
                
                // 觸發自定義事件，通知其他組件登入對話框已取消
                const event = new CustomEvent('loginDialogCancelled', {
                    detail: {
                        button: button,
                        originalHTML: originalHTML
                    }
                });
                document.dispatchEvent(event);
                console.log('spot-common.js: 觸發loginDialogCancelled事件', button);
            }
        }
        
        // 綁定關閉事件 (使用一次性事件監聽器，防止重複綁定)
        closeButton.addEventListener('click', closeDialog, { once: true });
        cancelButton.addEventListener('click', closeDialog, { once: true });
        
        // 綁定登入事件
        loginButton.addEventListener('click', () => {
            // 保存當前頁面URL到localStorage，以便登入後重定向回來
            localStorage.setItem('loginRedirectUrl', window.location.href);
            // 跳轉到登入頁面，並帶上redirect參數
            window.location.href = '/members/login?redirect=' + encodeURIComponent(window.location.href);
        }, { once: true });
    }
    
    // 顯示提示訊息 - 已停用，不再顯示任何提示
    function showToast(message, type = 'info') {
        // 提示訊息功能已停用
        console.log('提示訊息(已停用顯示):', message, type);
        return null;
    }
    
    // 關閉提示訊息 - 已停用
    function closeToast(toast) {
        // 提示訊息功能已停用
        return;
    }
    
    // 獲取 toast 圖示
    function getToastIcon(type) {
        switch (type) {
            case 'success': return 'check_circle';
            case 'error': return 'error';
            case 'warning': return 'warning';
            default: return 'info';
        }
    }
    
    // 檢查收藏狀態
    async function checkFavoriteStatus(spotId) {
        if (!spotId) return false;
        
        try {
            const response = await fetch(`${API_ENDPOINT}/${spotId}/status`);
            
            if (response.ok) {
                const result = await response.json();
                return result.success ? result.data : false;
            }
            
            return false;
        } catch (error) {
            console.error('檢查收藏狀態失敗:', error);
            return false;
        }
    }
    
    // 批量檢查收藏狀態
    async function checkBatchFavoriteStatus(spotIds) {
        if (!spotIds || !spotIds.length) return {};
        
        try {
            const response = await fetch(`${API_ENDPOINT}/status/batch?spotIds=${spotIds.join(',')}`);
            
            if (response.ok) {
                const result = await response.json();
                return result.success ? result.data : {};
            }
            
            return {};
        } catch (error) {
            console.error('批量檢查收藏狀態失敗:', error);
            return {};
        }
    }
    
    // 全局錯誤處理函數，確保按鈕恢復正常狀態
    function ensureButtonNormalState(button) {
        if (!button) return;
        
        // 檢查按鈕是否處於載入狀態
        const isLoading = button.querySelector('.loading-spinner');
        if (isLoading) {
            // 恢復為空心愛心
            button.innerHTML = '<span class="material-icons">favorite_border</span>';
            button.classList.remove('favorited');
            button.setAttribute('title', '加入收藏');
            button.setAttribute('aria-label', '收藏此景點');
            button.disabled = false;
            console.log('強制恢復按鈕狀態', button);
        }
    }
    
    // 公開API
    window.SpotCommon = {
        toggleFavorite,
        updateFavoriteButtonState,
        showLoginDialog,
        showToast,
        checkFavoriteStatus,
        checkBatchFavoriteStatus,
        ensureButtonNormalState,
        closeToast
    };
    
    // 頁面載入完成後初始化
    document.addEventListener('DOMContentLoaded', init);
})();