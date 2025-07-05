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
            toggleFavorite(button);
        }
    });
}

/**
 * 切換收藏狀態
 */
function toggleFavorite(button) {
    const itineraryId = button.dataset.itineraryId;
    if (!itineraryId) {
        console.error('行程 ID 不存在');
        return;
    }
    
    button.disabled = true; // 禁用按鈕，防止重複點擊
    const icon = button.querySelector('.material-icons');
    
    // 呼叫收藏API
    fetch(`/favItn`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
        },
        body: JSON.stringify({
            favItnId: itineraryId,
            memId: 10  // 暫時使用測試會員ID
        })
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('伺服器回應錯誤');
        }
        return response.json();
    })
    .then(data => {
        if (data.success) {
            // 完全根據後端回傳的真實狀態來更新UI
            if (data.isFavorited) {
                // 如果回傳是「已收藏」
                button.classList.add('favorited');
                icon.textContent = 'favorite';
                showToast('已加入收藏', 'success');
            } else {
                // 如果回傳是「未收藏」
                button.classList.remove('favorited');
                icon.textContent = 'favorite_border';
                showToast('已取消收藏', 'info');
            }
        } else {
            // 處理後端回傳的操作失敗訊息
            showToast(data.message || '操作失敗', 'error');
        }
    })
    .catch(error => {
        console.error('收藏操作失敗:', error);
        showToast('網路錯誤，請稍後再試', 'error');
    })
    .finally(() => {
        button.disabled = false; // 無論成功或失敗，最後都重新啟用按鈕
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
            fetch(`/api/itinerary/itnlist?page=${currentPage}&size=${currentSize}`)
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
 * 重置搜尋表單
 */
function resetSearchForm() {
    document.getElementById('keyword').value = '';
    document.getElementById('isPublic').value = '';
    document.querySelector('.itinerary-list-search-form').submit();
}

// 切換行程公開/私人狀態
function toggleVisibility(button, makePublic) {
    const itineraryId = button.getAttribute('data-itinerary-id');
    const loadingText = makePublic ? '正在設為公開...' : '正在設為私人...';
    
    // 禁用按鈕並顯示載入中
    button.disabled = true;
    const originalText = button.innerHTML;
    button.innerHTML = `<span class="material-icons">hourglass_empty</span> ${loadingText}`;
    
    // 發送AJAX請求
    fetch(`/itinerary/toggle-visibility/${itineraryId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        credentials: 'same-origin'
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            // 顯示成功訊息
            showToast(data.message);
            
            // 重新載入頁面以更新UI
            setTimeout(() => {
                window.location.reload();
            }, 1000);
        } else {
            // 顯示錯誤訊息
            showToast(data.message, 'error');
            
            // 恢復按鈕原始狀態
            button.innerHTML = originalText;
            button.disabled = false;
        }
    })
    .catch(error => {
        console.error('切換公開/私人狀態失敗:', error);
        showToast('操作失敗，請稍後再試', 'error');
        
        // 恢復按鈕原始狀態
        button.innerHTML = originalText;
        button.disabled = false;
    });
}

// 顯示提示訊息
function showToast(message, type = 'success') {
    // 檢查是否已存在toast元素，如果有則移除
    const existingToast = document.querySelector('.itinerary-toast');
    if (existingToast) {
        existingToast.remove();
    }
    
    // 創建toast元素
    const toast = document.createElement('div');
    toast.className = `itinerary-toast itinerary-toast--${type}`;
    toast.innerHTML = `
        <span class="material-icons">${type === 'success' ? 'check_circle' : 'error'}</span>
        <span>${message}</span>
    `;
    
    // 添加到頁面
    document.body.appendChild(toast);
    
    // 添加CSS樣式
    toast.style.position = 'fixed';
    toast.style.bottom = '20px';
    toast.style.right = '20px';
    toast.style.backgroundColor = type === 'success' ? '#4CAF50' : '#F44336';
    toast.style.color = 'white';
    toast.style.padding = '12px 20px';
    toast.style.borderRadius = '4px';
    toast.style.boxShadow = '0 2px 10px rgba(0,0,0,0.2)';
    toast.style.display = 'flex';
    toast.style.alignItems = 'center';
    toast.style.zIndex = '9999';
    toast.style.opacity = '0';
    toast.style.transition = 'opacity 0.3s ease-in-out';
    
    // 設置圖標樣式
    const icon = toast.querySelector('.material-icons');
    icon.style.marginRight = '8px';
    
    // 顯示toast
    setTimeout(() => {
        toast.style.opacity = '1';
    }, 10);
    
    // 3秒後隱藏toast
    setTimeout(() => {
        toast.style.opacity = '0';
        setTimeout(() => {
            toast.remove();
        }, 300);
    }, 3000);
}

// 全域函數，供 HTML 直接呼叫
window.loadMoreItineraries = loadMoreItineraries;
window.resetSearchForm = resetSearchForm;
window.toggleVisibility = toggleVisibility; 

// 監聽收藏狀態變更事件
window.addEventListener('storage', function(e) {
    if (e.key === 'favoriteChange') {
        const data = JSON.parse(e.newValue);
        updateFavoriteButton(data.itineraryId, data.isFavorited);
    }
});

function updateFavoriteButton(itineraryId, isFavorited) {
    const button = document.querySelector(`button[data-itinerary-id="${itineraryId}"]`);
    if (button) {
        button.classList.toggle('favorited', isFavorited);
        const icon = button.querySelector('.material-icons');
        icon.textContent = isFavorited ? 'favorite' : 'favorite_border';
        button.title = isFavorited ? '取消收藏' : '加入收藏';
    }
}

function toggleFavorite(button) {
    const itineraryId = button.dataset.itineraryId;
    if (!itineraryId) return;

    fetch(`/api/itinerary/${itineraryId}/favorite`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }
    })
    .then(res => res.json())
    .then(data => {
        if (data.success) {
            // 更新按鈕狀態
            button.classList.toggle('favorited');
            const icon = button.querySelector('.material-icons');
            if (data.isFavorited) {
                icon.textContent = 'favorite';
                button.title = '取消收藏';
            } else {
                icon.textContent = 'favorite_border';
                button.title = '加入收藏';
            }

            // 通知其他頁面
            localStorage.setItem('favoriteChange', JSON.stringify({
                itineraryId: itineraryId,
                isFavorited: data.isFavorited,
                timestamp: new Date().getTime()
            }));

            showToast(data.isFavorited ? '已加入收藏' : '已取消收藏', 'info');
        } else {
            showToast('操作失敗，請稍後再試', 'error');
        }
    })
    .catch(() => showToast('網路錯誤', 'error'));
} 