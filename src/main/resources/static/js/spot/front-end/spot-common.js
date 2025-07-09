/**
 * 景點模組通用 JavaScript 函數庫
 * 包含所有景點模組頁面共用的功能
 */

(function() {
    'use strict';
    
    // 全域變數
    const API_ENDPOINT = '/api/spot/favorites';
    
    // SpotCommon 模組
    const SpotCommon = {
        // 初始化函數
        init() {
            // 綁定登入按鈕事件
            this.bindLoginButton();
            
            console.log('景點模組通用腳本初始化完成');
        },
        
        // 綁定登入按鈕事件
        bindLoginButton() {
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
        },
        
        // 檢查收藏狀態
        async checkFavoriteStatus(spotId) {
            if (!spotId) return false;
            
            try {
                const response = await fetch(`${API_ENDPOINT}/${spotId}/status`, {
                    method: 'GET',
                    headers: {
                        'Content-Type': 'application/json'
                    }
                });
                
                if (response.ok) {
                    const result = await response.json();
                    return result.data && result.data.isFavorited;
                }
                
                return false;
            } catch (error) {
                console.error('檢查收藏狀態失敗:', error);
                return false;
            }
        },
        
        // 批量檢查收藏狀態
        async checkBatchFavoriteStatus(spotIds) {
            if (!spotIds || !Array.isArray(spotIds) || spotIds.length === 0) {
                return {};
            }
            
            try {
                const response = await fetch(`${API_ENDPOINT}/status/batch`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ spotIds: spotIds })
                });
                
                if (response.ok) {
                    const result = await response.json();
                    return result.data || {};
                }
                
                return {};
            } catch (error) {
                console.error('批量檢查收藏狀態失敗:', error);
                return {};
            }
        },
        
        // 確保按鈕處於正常狀態
        ensureButtonNormalState(button) {
            if (!button) return;
            
            // 移除載入狀態
            button.classList.remove('loading');
            button.disabled = false;
            
            // 檢查是否有載入中的圖標
            const loadingIcon = button.querySelector('.loading-spinner');
            if (loadingIcon) {
                // 根據按鈕類別決定顯示的圖標
                if (button.classList.contains('favorited')) {
                    button.innerHTML = '<span class="material-icons">favorite</span>';
                } else {
                    button.innerHTML = '<span class="material-icons">favorite_border</span>';
                }
            }
        }
    };
    
    // 當 DOM 載入完成後初始化
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', () => SpotCommon.init());
    } else {
        SpotCommon.init();
    }
    
    // 暴露到全域
    window.SpotCommon = SpotCommon;
    
})();

/**
 * 初始化共用功能
 */
document.addEventListener('DOMContentLoaded', function() {
    // 綁定登入按鈕事件
    const loginBtn = document.querySelector('.logout-btn');
    if (loginBtn) {
        loginBtn.addEventListener('click', function() {
            window.location.href = '/members/login';
        });
    }
});