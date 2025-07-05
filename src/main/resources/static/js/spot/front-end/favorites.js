/**
 * 景點收藏頁面 JavaScript
 * 處理景點收藏的互動功能
 */

// 全域變數
let currentPage = 1;
let currentSize = 10;
let isLoading = false;

// DOM 載入完成後初始化
document.addEventListener('DOMContentLoaded', function() {
    // 綁定收藏按鈕事件
    bindFavoriteButtons();
    
    // 更新收藏數量
    updateFavoritesCount();
});

/**
 * 綁定收藏按鈕事件
 */
function bindFavoriteButtons() {
    const favoriteButtons = document.querySelectorAll('.itinerary-list-card__favorite');
    favoriteButtons.forEach(button => {
        button.addEventListener('click', function(event) {
            event.preventDefault();
            const spotId = this.getAttribute('data-spot-id');
            toggleFavorite(this, spotId, true);
        });
    });
}

/**
 * 切換收藏狀態
 * @param {HTMLElement} button - 收藏按鈕元素
 * @param {number} spotId - 景點ID
 * @param {boolean} isInFavoritesPage - 是否在收藏頁面
 */
function toggleFavorite(button, spotId, isInFavoritesPage = false) {
    if (!spotId) return;
    
    // 防止重複點擊
    if (button.classList.contains('loading')) return;
    
    // 設置按鈕為載入狀態
    button.classList.add('loading');
    const icon = button.querySelector('.material-icons');
    const originalIcon = icon.textContent;
    icon.textContent = 'sync';
    
    fetch(`/api/spot/favorites/${spotId}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }
    })
    .then(response => {
        if (!response.ok) {
            if (response.status === 401) {
                // 未登入處理
                showLoginDialog();
                throw new Error('請先登入');
            }
            throw new Error('操作失敗');
        }
        return response.json();
    })
    .then(data => {
        // 移除載入狀態
        button.classList.remove('loading');
        icon.textContent = originalIcon;
        
        if (data.success) {
            if (isInFavoritesPage) {
                // 在收藏頁面，移除卡片
                const card = button.closest('.itinerary-list-item');
                if (card) {
                    card.style.transition = 'opacity 0.3s ease';
                    card.style.opacity = '0';
                    
                    setTimeout(() => {
                        card.remove();
                        updateFavoritesCount();
                        
                        // 檢查是否還有收藏
                        checkEmptyState();
                    }, 300);
                }
            } else {
                // 在其他頁面，切換按鈕狀態
                button.classList.toggle('favorited');
                if (button.classList.contains('favorited')) {
                    icon.textContent = 'favorite';
                    button.title = '取消收藏';
                    showToast('已加入收藏', 'success');
                } else {
                    icon.textContent = 'favorite_border';
                    button.title = '加入收藏';
                    showToast('已取消收藏', 'info');
                }
            }
            
            // 通知其他頁面
            localStorage.setItem('spotFavoriteChange', JSON.stringify({
                spotId: spotId,
                isFavorited: !isInFavoritesPage && button.classList.contains('favorited'),
                timestamp: new Date().getTime()
            }));
        } else {
            showToast(data.message || '操作失敗', 'error');
        }
    })
    .catch(error => {
        console.error('收藏操作失敗:', error);
        // 移除載入狀態
        button.classList.remove('loading');
        icon.textContent = originalIcon;
        
        if (error.message !== '請先登入') {
            showToast('操作失敗，請稍後再試', 'error');
        }
    });
}

/**
 * 更新收藏數量
 */
function updateFavoritesCount() {
    const favoritesGrid = document.getElementById('favorites-grid');
    const favoritesCount = document.getElementById('favorites-count');
    
    if (favoritesGrid && favoritesCount) {
        const count = favoritesGrid.querySelectorAll('.itinerary-list-item').length;
        favoritesCount.textContent = count;
    }
}

/**
 * 檢查是否顯示空狀態
 */
function checkEmptyState() {
    const favoritesGrid = document.getElementById('favorites-grid');
    const emptyState = document.getElementById('favorites-empty');
    
    if (favoritesGrid && emptyState) {
        const hasItems = favoritesGrid.querySelectorAll('.itinerary-list-item').length > 0;
        
        if (!hasItems) {
            favoritesGrid.style.display = 'none';
            emptyState.style.display = 'block';
        } else {
            favoritesGrid.style.display = 'grid';
            emptyState.style.display = 'none';
        }
    }
}

/**
 * 顯示登入對話框
 */
function showLoginDialog() {
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
        closeButton.addEventListener('click', closeLoginDialog);
        
        // 綁定取消按鈕事件
        const cancelButton = dialog.querySelector('[data-action="cancel"]');
        cancelButton.addEventListener('click', closeLoginDialog);
        
        // 綁定登入按鈕事件
        const loginButton = dialog.querySelector('[data-action="login"]');
        loginButton.addEventListener('click', () => {
            window.location.href = '/members/login';
        });
    }
    
    // 顯示對話框
    setTimeout(() => {
        dialog.classList.add('login-dialog--show');
    }, 10);
}

/**
 * 關閉登入對話框
 */
function closeLoginDialog() {
    const dialog = document.querySelector('.login-dialog');
    if (dialog) {
        dialog.classList.remove('login-dialog--show');
        setTimeout(() => {
            dialog.remove();
        }, 300);
    }
}

/**
 * 顯示提示訊息
 * @param {string} message - 提示訊息
 * @param {string} type - 提示類型 (success, error, info, warning)
 */
function showToast(message, type = 'info') {
    // 檢查是否已存在提示
    let toast = document.querySelector('.toast');
    
    if (toast) {
        // 移除現有提示
        toast.remove();
    }
    
    // 創建新提示
    toast = document.createElement('div');
    toast.className = `toast toast--${type}`;
    toast.innerHTML = `
        <div class="toast__content">
            <span class="material-icons">${getToastIcon(type)}</span>
            <span>${message}</span>
        </div>
    `;
    
    document.body.appendChild(toast);
    
    // 顯示提示
    setTimeout(() => {
        toast.classList.add('toast--show');
    }, 10);
    
    // 自動隱藏提示
    setTimeout(() => {
        toast.classList.remove('toast--show');
        setTimeout(() => {
            toast.remove();
        }, 300);
    }, 3000);
}

/**
 * 獲取提示圖標
 * @param {string} type - 提示類型
 * @returns {string} 圖標名稱
 */
function getToastIcon(type) {
    switch (type) {
        case 'success':
            return 'check_circle';
        case 'error':
            return 'error';
        case 'warning':
            return 'warning';
        case 'info':
        default:
            return 'info';
    }
}

// 監聽收藏狀態變更事件
window.addEventListener('storage', function(e) {
    if (e.key === 'spotFavoriteChange') {
        const data = JSON.parse(e.newValue);
        const button = document.querySelector(`.itinerary-list-card__favorite[data-spot-id="${data.spotId}"]`);
        
        if (button) {
            const card = button.closest('.itinerary-list-item');
            if (card && !data.isFavorited) {
                // 在收藏頁面，如果取消收藏，移除卡片
                card.style.transition = 'opacity 0.3s ease';
                card.style.opacity = '0';
                
                setTimeout(() => {
                    card.remove();
                    updateFavoritesCount();
                    checkEmptyState();
                }, 300);
            }
        }
    }
}); 