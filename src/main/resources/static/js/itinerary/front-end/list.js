/**
 * 行程列表頁面 JavaScript
 * 處理行程列表的互動功能
 */

// 全域變數
let currentPage = 1;
let currentSize = 10;
let isLoading = false;

// DOM 載入完成後初始化
document.addEventListener('DOMContentLoaded', function() {
    initializeItineraryList();
});

/**
 * 初始化行程列表
 */
function initializeItineraryList() {
    // 綁定搜尋表單事件
    bindSearchForm();
    
    // 綁定收藏按鈕事件
    bindFavoriteButtons();
    
    // 綁定篩選事件
    bindFilterEvents();
    
    // 初始化工具提示
    initializeTooltips();
    
    console.log('行程列表頁面初始化完成');
}

/**
 * 綁定搜尋表單事件
 */
function bindSearchForm() {
    const searchForm = document.querySelector('.itinerary-list-search-form');
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
 * 綁定收藏按鈕事件
 */
function bindFavoriteButtons() {
    document.addEventListener('click', function(e) {
        if (e.target.closest('.itinerary-list-card__favorite')) {
            e.preventDefault();
            const button = e.target.closest('.itinerary-list-card__favorite');
            const itineraryId = button.dataset.itineraryId;
            toggleFavorite(itineraryId, button);
        }
    });
}

/**
 * 切換收藏狀態
 */
function toggleFavorite(itineraryId, button) {
    if (!itineraryId) {
        console.error('行程 ID 不存在');
        return;
    }
    
    // 視覺回饋
    button.disabled = true;
    const icon = button.querySelector('.material-icons');
    const originalText = icon.textContent;
    
    // 切換圖示
    icon.textContent = icon.textContent === 'favorite_border' ? 'favorite' : 'favorite_border';
    
    // 模擬 API 呼叫
    fetch(`/api/itinerary/${itineraryId}/favorite`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            // 更新按鈕狀態
            if (icon.textContent === 'favorite') {
                button.classList.add('favorited');
                showToast('已加入收藏', 'success');
            } else {
                button.classList.remove('favorited');
                showToast('已取消收藏', 'info');
            }
        } else {
            // 恢復原始狀態
            icon.textContent = originalText;
            showToast(data.message || '操作失敗', 'error');
        }
    })
    .catch(error => {
        console.error('收藏操作失敗:', error);
        // 恢復原始狀態
        icon.textContent = originalText;
        showToast('網路錯誤，請稍後再試', 'error');
    })
    .finally(() => {
        button.disabled = false;
    });
}

/**
 * 綁定篩選事件
 */
function bindFilterEvents() {
    const filterSelects = document.querySelectorAll('.itinerary-list-search-select');
    filterSelects.forEach(select => {
        select.addEventListener('change', function() {
            performSearch();
        });
    });
}

/**
 * 初始化工具提示
 */
function initializeTooltips() {
    // 為收藏按鈕添加工具提示
    const favoriteButtons = document.querySelectorAll('.itinerary-list-card__favorite');
    favoriteButtons.forEach(button => {
        const icon = button.querySelector('.material-icons');
        if (icon.textContent === 'favorite_border') {
            button.title = '加入收藏';
        } else {
            button.title = '取消收藏';
        }
    });
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

/**
 * 載入更多行程（分頁功能）
 */
function loadMoreItineraries() {
    if (isLoading) return;
    
    isLoading = true;
    currentPage++;
    
    // 顯示載入狀態
    const loadMoreBtn = document.querySelector('.itinerary-list-load-more-btn');
    if (loadMoreBtn) {
        loadMoreBtn.disabled = true;
        loadMoreBtn.innerHTML = '<span class="material-icons">hourglass_empty</span>載入中...';
    }
    
    // 模擬 API 呼叫
    fetch(`/api/itinerary/list?page=${currentPage}&size=${currentSize}`)
        .then(response => response.json())
        .then(data => {
            if (data.success && data.data.length > 0) {
                appendItineraries(data.data);
            } else {
                // 沒有更多資料
                if (loadMoreBtn) {
                    loadMoreBtn.style.display = 'none';
                }
            }
        })
        .catch(error => {
            console.error('載入更多行程失敗:', error);
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
 * 添加行程到列表
 */
function appendItineraries(itineraries) {
    const grid = document.getElementById('itineraryGrid');
    if (!grid) return;
    
    itineraries.forEach(itinerary => {
        const itineraryElement = createItineraryElement(itinerary);
        grid.appendChild(itineraryElement);
    });
    
    // 重新綁定事件
    bindFavoriteButtons();
    initializeTooltips();
}

/**
 * 建立行程元素
 */
function createItineraryElement(itinerary) {
    const div = document.createElement('div');
    div.className = 'itinerary-list-item';
    div.innerHTML = `
        <article class="itinerary-list-card">
            <div class="itinerary-list-card__header">
                <button class="itinerary-list-card__favorite" 
                        data-itinerary-id="${itinerary.itnId}"
                        title="加入收藏">
                    <span class="material-icons">favorite_border</span>
                </button>
                <span class="itinerary-list-card__status itinerary-list-card__status--${itinerary.isPublic ? 'public' : 'private'}">
                    <span class="material-icons">${itinerary.isPublic ? 'public' : 'lock'}</span>
                    ${itinerary.isPublic ? '公開' : '私人'}
                </span>
            </div>
            <div class="itinerary-list-card__content">
                <h3 class="itinerary-list-card__title">${itinerary.itnName}</h3>
                <p class="itinerary-list-card__description">${itinerary.itnDesc || '暫無描述'}</p>
                <div class="itinerary-list-card__meta">
                    <div class="itinerary-list-card__author">
                        <span class="material-icons">person</span>
                        <span>${itinerary.creator || '匿名'}</span>
                    </div>
                    <div class="itinerary-list-card__date">
                        <span class="material-icons">schedule</span>
                        <span>${formatDate(itinerary.itnCreatedAt)}</span>
                    </div>
                </div>
                <div class="itinerary-list-card__actions">
                    <a href="/itinerary/detail/${itinerary.itnId}" class="itinerary-list-card__link">
                        <span class="material-icons">visibility</span>
                        查看行程
                    </a>
                </div>
            </div>
        </article>
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
function resetSearchForm() {
    // 清空表單
    const searchForm = document.querySelector('.itinerary-list-search-form');
    if (searchForm) {
        searchForm.reset();
    }
    
    // 清空 URL 參數並重新載入
    const url = new URL(window.location);
    url.search = '';
    window.location.href = url.toString();
}

// 全域函數，供 HTML 直接呼叫
window.toggleFavorite = toggleFavorite;
window.loadMoreItineraries = loadMoreItineraries;
window.resetSearchForm = resetSearchForm; 