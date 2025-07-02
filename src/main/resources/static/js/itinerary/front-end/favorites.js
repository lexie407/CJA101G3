/**
 * 我的收藏頁面 JavaScript
 * 處理我的收藏的互動功能
 */

// 全域變數
let currentPage = 1;
let currentSize = 10;
let isLoading = false;

// DOM 載入完成後初始化
document.addEventListener('DOMContentLoaded', function() {
    initializeMyFavorites();
});

/**
 * 初始化我的收藏
 */
function initializeMyFavorites() {
    // 綁定搜尋表單事件
    bindSearchForm();
    
    // 綁定篩選事件
    bindFilterEvents();
    
    // 綁定收藏操作事件
    bindFavoriteActions();
    
    // 初始化工具提示
    initializeTooltips();
    
    console.log('我的收藏頁面初始化完成');
}

/**
 * 綁定搜尋表單事件
 */
function bindSearchForm() {
    const searchForm = document.querySelector('.my-favorites-search-form');
    if (searchForm) {
        searchForm.addEventListener('submit', function(e) {
            e.preventDefault();
            performSearch();
        });
    }
    
    // 即時搜尋（可選）
    const searchInput = document.getElementById('keyword');
    if (searchInput) {
        let searchTimeout;
        searchInput.addEventListener('input', function() {
            clearTimeout(searchTimeout);
            searchTimeout = setTimeout(() => {
                performSearch();
            }, 500);
        });
    }
}

/**
 * 執行搜尋
 */
function performSearch() {
    const keyword = document.getElementById('keyword')?.value || '';
    const isPublic = document.getElementById('isPublic')?.value;
    
    // 更新 URL 參數
    const url = new URL(window.location);
    if (keyword) url.searchParams.set('keyword', keyword);
    if (isPublic) url.searchParams.set('isPublic', isPublic);
    url.searchParams.delete('page'); // 重置頁碼
    
    // 重新載入頁面或使用 AJAX
    window.location.href = url.toString();
}

/**
 * 綁定篩選事件
 */
function bindFilterEvents() {
    const filterSelects = document.querySelectorAll('.my-favorites-search-select');
    filterSelects.forEach(select => {
        select.addEventListener('change', function() {
            performSearch();
        });
    });
}

/**
 * 綁定收藏操作事件
 */
function bindFavoriteActions() {
    document.addEventListener('click', function(e) {
        // 取消收藏按鈕
        if (e.target.closest('.my-favorites-card__btn--unfavorite')) {
            e.preventDefault();
            const button = e.target.closest('.my-favorites-card__btn--unfavorite');
            const itineraryId = button.dataset.itineraryId;
            const itineraryName = button.dataset.itineraryName;
            unfavoriteItinerary(itineraryId, itineraryName, button);
        }
        
        // 複製按鈕
        if (e.target.closest('.my-favorites-card__btn--copy')) {
            e.preventDefault();
            const button = e.target.closest('.my-favorites-card__btn--copy');
            const itineraryId = button.dataset.itineraryId;
            copyItinerary(itineraryId);
        }
    });
}

/**
 * 取消收藏行程
 */
function unfavoriteItinerary(itineraryId, itineraryName, button) {
    if (!itineraryId) return;
    
    // 確認對話框
    const confirmed = confirm(`確定要取消收藏行程「${itineraryName}」嗎？`);
    if (!confirmed) return;
    
    // 顯示載入狀態
    button.disabled = true;
    const originalText = button.innerHTML;
    button.innerHTML = '<span class="material-icons">hourglass_empty</span>取消中...';
    
    // 模擬 API 呼叫
    fetch(`/api/itinerary/${itineraryId}/favorite`, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showToast('已取消收藏', 'success');
            // 移除行程卡片
            const card = button.closest('.my-favorites-card');
            if (card) {
                card.style.opacity = '0';
                setTimeout(() => {
                    card.remove();
                    // 檢查是否還有收藏
                    checkEmptyState();
                }, 300);
            }
        } else {
            showToast(data.message || '取消收藏失敗', 'error');
        }
    })
    .catch(error => {
        console.error('取消收藏失敗:', error);
        showToast('網路錯誤，請稍後再試', 'error');
    })
    .finally(() => {
        button.disabled = false;
        button.innerHTML = originalText;
    });
}

/**
 * 複製行程
 */
function copyItinerary(itineraryId) {
    if (!itineraryId) return;
    
    const copyBtn = document.querySelector(`[data-itinerary-id="${itineraryId}"].my-favorites-card__btn--copy`);
    if (!copyBtn) return;
    
    // 視覺回饋
    copyBtn.disabled = true;
    const originalText = copyBtn.innerHTML;
    copyBtn.innerHTML = '<span class="material-icons">hourglass_empty</span>複製中...';
    
    // 模擬 API 呼叫
    fetch(`/api/itinerary/${itineraryId}/copy`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showToast('行程複製成功！', 'success');
            // 跳轉到新行程的編輯頁面
            setTimeout(() => {
                window.location.href = `/itinerary/edit/${data.data.itnId}`;
            }, 1500);
        } else {
            showToast(data.message || '複製失敗', 'error');
        }
    })
    .catch(error => {
        console.error('複製行程失敗:', error);
        showToast('網路錯誤，請稍後再試', 'error');
    })
    .finally(() => {
        copyBtn.disabled = false;
        copyBtn.innerHTML = originalText;
    });
}

/**
 * 檢查空狀態
 */
function checkEmptyState() {
    const cards = document.querySelectorAll('.my-favorites-card');
    const emptyState = document.querySelector('.my-favorites-empty');
    
    if (cards.length === 0 && emptyState) {
        emptyState.style.display = 'block';
    }
}

/**
 * 初始化工具提示
 */
function initializeTooltips() {
    // 為按鈕添加工具提示
    const buttons = document.querySelectorAll('.my-favorites-card__btn');
    buttons.forEach(button => {
        if (button.title) {
            // 工具提示已經設定
        }
    });
}

/**
 * 載入更多收藏（分頁功能）
 */
function loadMoreFavorites() {
    if (isLoading) return;
    
    isLoading = true;
    currentPage++;
    
    // 顯示載入狀態
    const loadMoreBtn = document.querySelector('.my-favorites-load-more-btn');
    if (loadMoreBtn) {
        loadMoreBtn.disabled = true;
        loadMoreBtn.innerHTML = '<span class="material-icons">hourglass_empty</span>載入中...';
    }
    
    // 模擬 API 呼叫
    fetch(`/api/itinerary/favorites?page=${currentPage}&size=${currentSize}`)
        .then(response => response.json())
        .then(data => {
            if (data.success && data.data.length > 0) {
                appendFavorites(data.data);
            } else {
                // 沒有更多資料
                if (loadMoreBtn) {
                    loadMoreBtn.style.display = 'none';
                }
            }
        })
        .catch(error => {
            console.error('載入更多收藏失敗:', error);
            showToast('載入失敗，請稍後再試', 'error');
            currentPage--; // 恢復頁碼
        })
        .finally(() => {
            isLoading = false;
            if (loadMoreBtn) {
                loadMoreBtn.disabled = false;
                loadMoreBtn.innerHTML = '<span class="material-icons">expand_more</span>載入更多';
            }
        });
}

/**
 * 添加收藏到列表
 */
function appendFavorites(favorites) {
    const grid = document.getElementById('myFavoritesGrid');
    if (!grid) return;
    
    favorites.forEach(favorite => {
        const favoriteElement = createFavoriteElement(favorite);
        grid.appendChild(favoriteElement);
    });
    
    // 重新綁定事件
    bindFavoriteActions();
    initializeTooltips();
}

/**
 * 建立收藏元素
 */
function createFavoriteElement(favorite) {
    const div = document.createElement('div');
    div.className = 'my-favorites-card';
    div.setAttribute('data-itinerary-id', favorite.itnId);
    div.innerHTML = `
        <div class="my-favorites-card__header">
            <span class="my-favorites-card__status-badge my-favorites-card__status-badge--${favorite.isPublic ? 'public' : 'private'}">
                <span class="material-icons">${favorite.isPublic ? 'public' : 'lock'}</span>
                ${favorite.isPublic ? '公開' : '私人'}
            </span>
            <div class="my-favorites-card__actions">
                <button class="my-favorites-card__btn my-favorites-card__btn--copy" 
                        data-itinerary-id="${favorite.itnId}"
                        title="複製行程">
                    <span class="material-icons">content_copy</span>
                </button>
                <button class="my-favorites-card__btn my-favorites-card__btn--unfavorite" 
                        data-itinerary-id="${favorite.itnId}"
                        data-itinerary-name="${favorite.itnName}"
                        title="取消收藏">
                    <span class="material-icons">favorite</span>
                </button>
            </div>
        </div>
        <div class="my-favorites-card__content">
            <h3 class="my-favorites-card__title">${favorite.itnName}</h3>
            <p class="my-favorites-card__description">${favorite.itnDesc || '暫無描述'}</p>
            <div class="my-favorites-card__meta">
                <div class="my-favorites-card__author">
                    <span class="material-icons">person</span>
                    <span>${favorite.creator || '匿名'}</span>
                </div>
                <div class="my-favorites-card__date">
                    <span class="material-icons">schedule</span>
                    <span>${formatDate(favorite.itnCreatedAt)}</span>
                </div>
                <div class="my-favorites-card__spots">
                    <span class="material-icons">place</span>
                    <span>${favorite.spotCount || 0}個景點</span>
                </div>
            </div>
            <div class="my-favorites-card__actions-bottom">
                <a href="/itinerary/detail/${favorite.itnId}" class="my-favorites-card__link">
                    <span class="material-icons">visibility</span>
                    查看行程
                </a>
            </div>
        </div>
    `;
    
    return div;
}

/**
 * 格式化日期
 */
function formatDate(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('zh-TW');
}

/**
 * 重置搜尋
 */
function resetSearch() {
    const searchForm = document.querySelector('.my-favorites-search-form');
    if (searchForm) {
        searchForm.reset();
        performSearch();
    }
}

/**
 * 顯示提示訊息
 */
function showToast(message, type = 'info') {
    // 建立 toast 元素
    const toast = document.createElement('div');
    toast.className = `toast toast--${type}`;
    toast.innerHTML = `
        <div class="toast__content">
            <span class="material-icons">${getToastIcon(type)}</span>
            <span>${message}</span>
        </div>
    `;
    
    // 添加到頁面
    document.body.appendChild(toast);
    
    // 顯示動畫
    setTimeout(() => {
        toast.classList.add('toast--show');
    }, 100);
    
    // 自動隱藏
    setTimeout(() => {
        toast.classList.remove('toast--show');
        setTimeout(() => {
            document.body.removeChild(toast);
        }, 300);
    }, 3000);
}

/**
 * 取得 toast 圖示
 */
function getToastIcon(type) {
    switch (type) {
        case 'success': return 'check_circle';
        case 'error': return 'error';
        case 'warning': return 'warning';
        default: return 'info';
    }
}

// 全域函數，供 HTML 直接呼叫
window.unfavoriteItinerary = unfavoriteItinerary;
window.copyItinerary = copyItinerary;
window.loadMoreFavorites = loadMoreFavorites;
window.resetSearch = resetSearch; 