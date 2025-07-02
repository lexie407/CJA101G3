/**
 * 行程詳情頁面 JavaScript
 * 處理行程詳情的互動功能
 */

// 全域變數
let itineraryId = null;
let isFavorited = false;

// DOM 載入完成後初始化
document.addEventListener('DOMContentLoaded', function() {
    initializeItineraryDetail();
});

/**
 * 初始化行程詳情
 */
function initializeItineraryDetail() {
    // 取得行程 ID
    itineraryId = getItineraryIdFromUrl();
    
    // 綁定按鈕事件
    bindButtonEvents();
    
    // 初始化工具提示
    initializeTooltips();
    
    // 載入行程資料
    loadItineraryData();
    
    console.log('行程詳情頁面初始化完成');
}

/**
 * 從 URL 取得行程 ID
 */
function getItineraryIdFromUrl() {
    const pathParts = window.location.pathname.split('/');
    return pathParts[pathParts.length - 1];
}

/**
 * 綁定按鈕事件
 */
function bindButtonEvents() {
    // 收藏按鈕 (頂部操作按鈕)
    const favoriteBtn = document.querySelector('.itinerary-action-btn--favorite');
    if (favoriteBtn) {
        favoriteBtn.addEventListener('click', function(e) {
            e.preventDefault();
            toggleFavorite();
        });
    }
    
    // 底部收藏按鈕
    const bottomFavoriteBtn = document.querySelector('.itinerary-primary-btn');
    if (bottomFavoriteBtn) {
        bottomFavoriteBtn.addEventListener('click', function(e) {
            e.preventDefault();
            toggleFavorite();
        });
    }
    
    // 複製按鈕
    const copyBtns = document.querySelectorAll('.itinerary-secondary-btn');
    copyBtns.forEach(btn => {
        if (btn.textContent.includes('複製')) {
            btn.addEventListener('click', function(e) {
                e.preventDefault();
            copyItinerary();
        });
    }
    });
    
    // 分享按鈕
    const shareBtns = document.querySelectorAll('.itinerary-action-btn--share, .itinerary-secondary-btn');
    shareBtns.forEach(btn => {
        if (btn.textContent.includes('分享') || btn.querySelector('.material-icons')?.textContent === 'share') {
            btn.addEventListener('click', function(e) {
            e.preventDefault();
            shareItinerary();
        });
    }
    });
    
    // 景點按鈕
    document.addEventListener('click', function(e) {
        if (e.target.closest('.itinerary-spot-btn')) {
            e.preventDefault();
            const button = e.target.closest('.itinerary-spot-btn');
            const action = button.getAttribute('data-action');
            const spotId = button.getAttribute('data-spot-id');
            
            if (action === 'view') {
                viewSpotDetail(spotId);
            } else if (action === 'map') {
                viewSpotOnMap(spotId);
            }
        }
    });
}

/**
 * 載入行程資料
 */
function loadItineraryData() {
    if (!itineraryId) return;
    
    // 模擬 API 呼叫
    fetch(`/api/itinerary/${itineraryId}`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                updatePageData(data.data);
            } else {
                showToast(data.message || '載入失敗', 'error');
            }
        })
        .catch(error => {
            console.error('載入行程資料失敗:', error);
            showToast('網路錯誤，請稍後再試', 'error');
        });
}

/**
 * 更新頁面資料
 */
function updatePageData(itinerary) {
    // 更新頁面標題
    document.title = `${itinerary.itnName} - 行程詳情 - 島遊Kha`;
    
    // 更新英雄區塊
    updateHeroSection(itinerary);
    
    // 更新行程描述
    updateDescription(itinerary);
    
    // 更新景點列表
    updateSpotsList(itinerary.spots);
    
    // 更新行程資訊
    updateItineraryInfo(itinerary);
    
    // 更新相關行程
    updateRelatedItineraries(itinerary.related);
}

/**
 * 更新英雄區塊
 */
function updateHeroSection(itinerary) {
    const title = document.querySelector('.itinerary-detail-hero__title');
    if (title) {
        title.textContent = itinerary.itnName;
    }
    
    const author = document.querySelector('.itinerary-detail-hero__author');
    if (author) {
        author.innerHTML = `<span class="material-icons">person</span><span>${itinerary.creator || '匿名'}</span>`;
    }
    
    const date = document.querySelector('.itinerary-detail-hero__date');
    if (date) {
        date.innerHTML = `<span class="material-icons">schedule</span><span>${formatDate(itinerary.itnCreatedAt)}</span>`;
    }
    
    const status = document.querySelector('.itinerary-detail-hero__status-badge');
    if (status) {
        status.className = `itinerary-detail-hero__status-badge itinerary-detail-hero__status-badge--${itinerary.isPublic ? 'public' : 'private'}`;
        status.innerHTML = `<span class="material-icons">${itinerary.isPublic ? 'public' : 'lock'}</span>${itinerary.isPublic ? '公開' : '私人'}`;
    }
    
    // 更新收藏狀態
    isFavorited = itinerary.isFavorited || false;
    updateFavoriteButton();
}

/**
 * 更新行程描述
 */
function updateDescription(itinerary) {
    const descElement = document.querySelector('.itinerary-detail-desc');
    if (descElement) {
        descElement.textContent = itinerary.itnDesc || '暫無描述';
    }
}

/**
 * 更新景點列表
 */
function updateSpotsList(spots) {
    const spotsContainer = document.querySelector('.itinerary-detail-spots');
    if (!spotsContainer || !spots) return;
    
    const spotsList = spotsContainer.querySelector('.itinerary-spot-item');
    if (!spotsList) return;
    
    // 清空現有內容
    spotsList.innerHTML = '';
    
    // 添加景點
    spots.forEach((spot, index) => {
        const spotElement = createSpotElement(spot, index + 1);
        spotsList.appendChild(spotElement);
    });
    
    // 更新標題中的景點數量
    const sectionTitle = spotsContainer.querySelector('.itinerary-detail-section-title');
    if (sectionTitle) {
        sectionTitle.innerHTML = `<span class="material-icons">place</span>行程景點 (${spots.length}個景點)`;
    }
}

/**
 * 建立景點元素
 */
function createSpotElement(spot, number) {
    const div = document.createElement('div');
    div.className = 'itinerary-spot-item';
    div.innerHTML = `
        <div class="itinerary-spot-number">${number}</div>
        <div class="itinerary-spot-content">
            <div class="itinerary-spot-header">
                <h3 class="itinerary-spot-name">${spot.spotName}</h3>
                <div class="itinerary-spot-actions">
                    <button class="itinerary-spot-btn" 
                            data-action="view" 
                            data-spot-id="${spot.spotId}" 
                            title="查看景點詳情">
                        <span class="material-icons">visibility</span>
                    </button>
                    <button class="itinerary-spot-btn" 
                            data-action="map" 
                            data-spot-id="${spot.spotId}" 
                            title="在地圖上查看">
                        <span class="material-icons">map</span>
                    </button>
                </div>
            </div>
            <p class="itinerary-spot-location">
                <span class="material-icons">place</span>
                ${spot.spotLoc}
            </p>
        </div>
    `;
    
    return div;
}

/**
 * 更新行程資訊
 */
function updateItineraryInfo(itinerary) {
    const infoContainer = document.querySelector('.itinerary-info-grid');
    if (!infoContainer) return;
    
    // 清空現有內容
    infoContainer.innerHTML = '';
    
    // 添加資訊項目
    const infoItems = [
        {
            icon: 'schedule',
            title: '建立時間',
            value: formatDate(itinerary.itnCreatedAt)
        },
        {
            icon: 'person',
            title: '建立者',
            value: itinerary.creator || '匿名'
        },
        {
            icon: 'visibility',
            title: '公開狀態',
            value: itinerary.isPublic ? '公開' : '私人'
        },
        {
            icon: 'favorite',
            title: '收藏數',
            value: itinerary.favoriteCount || 0
        }
    ];
    
    infoItems.forEach(item => {
        const itemElement = createInfoItem(item);
        infoContainer.appendChild(itemElement);
    });
}

/**
 * 建立資訊項目元素
 */
function createInfoItem(item) {
    const div = document.createElement('div');
    div.className = 'itinerary-info-item';
    div.innerHTML = `
        <span class="material-icons">${item.icon}</span>
        <div class="itinerary-info-content">
            <h4>${item.title}</h4>
            <p>${item.value}</p>
        </div>
    `;
    
    return div;
}

/**
 * 更新相關行程
 */
function updateRelatedItineraries(related) {
    const relatedContainer = document.querySelector('.itinerary-related-grid');
    if (!relatedContainer || !related) return;
    
    // 清空現有內容
    relatedContainer.innerHTML = '';
    
    // 添加相關行程
    related.forEach(itinerary => {
        const itineraryElement = createRelatedItineraryElement(itinerary);
        relatedContainer.appendChild(itineraryElement);
    });
}

/**
 * 建立相關行程元素
 */
function createRelatedItineraryElement(itinerary) {
    const div = document.createElement('div');
    div.className = 'itinerary-related-card';
    div.innerHTML = `
        <h3 class="itinerary-related-card__title">${itinerary.itnName}</h3>
        <div class="itinerary-related-card__author">
            <span class="material-icons">person</span>
            <span>${itinerary.creator || '匿名'}</span>
        </div>
    `;
    
    // 添加點擊事件
    div.addEventListener('click', function() {
        window.location.href = `/itinerary/detail/${itinerary.itnId}`;
    });
    
    return div;
}

/**
 * 切換收藏狀態
 */
function toggleFavorite() {
    if (!itineraryId) return;
    
    const favoriteBtn = document.querySelector('.itinerary-detail-hero__btn--favorite');
    if (!favoriteBtn) return;
    
    // 視覺回饋
    favoriteBtn.disabled = true;
    
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
            isFavorited = !isFavorited;
            updateFavoriteButton();
            showToast(isFavorited ? '已加入收藏' : '已取消收藏', 'success');
        } else {
            showToast(data.message || '操作失敗', 'error');
        }
    })
    .catch(error => {
        console.error('收藏操作失敗:', error);
        showToast('網路錯誤，請稍後再試', 'error');
    })
    .finally(() => {
        favoriteBtn.disabled = false;
    });
}

/**
 * 更新收藏按鈕狀態
 */
function updateFavoriteButton() {
    const favoriteBtn = document.querySelector('.itinerary-detail-hero__btn--favorite');
    if (!favoriteBtn) return;
    
    const icon = favoriteBtn.querySelector('.material-icons');
    if (isFavorited) {
        icon.textContent = 'favorite';
        favoriteBtn.title = '取消收藏';
    } else {
        icon.textContent = 'favorite_border';
        favoriteBtn.title = '加入收藏';
    }
}

/**
 * 複製行程
 */
function copyItinerary() {
    if (!itineraryId) return;
    
    const copyBtn = document.querySelector('.itinerary-detail-hero__btn--copy');
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
 * 分享行程
 */
function shareItinerary() {
    const shareData = {
        title: document.title,
        text: '看看這個精彩的旅遊行程！',
        url: window.location.href
    };
    
    if (navigator.share) {
        navigator.share(shareData)
            .then(() => {
                showToast('分享成功！', 'success');
            })
            .catch(error => {
                console.error('分享失敗:', error);
                copyToClipboard(window.location.href);
            });
    } else {
        copyToClipboard(window.location.href);
    }
}

/**
 * 複製到剪貼簿
 */
function copyToClipboard(text) {
    navigator.clipboard.writeText(text)
        .then(() => {
            showToast('連結已複製到剪貼簿', 'success');
        })
        .catch(error => {
            console.error('複製失敗:', error);
            showToast('複製失敗，請手動複製連結', 'error');
        });
}

/**
 * 查看景點詳情
 */
function viewSpotDetail(spotId) {
    window.open(`/spot/detail/${spotId}`, '_blank');
}

/**
 * 在地圖上查看景點
 */
function viewSpotOnMap(spotId) {
    window.open(`/spot/map?spot=${spotId}`, '_blank');
}

/**
 * 初始化工具提示
 */
function initializeTooltips() {
    // 為按鈕添加工具提示
    const buttons = document.querySelectorAll('.itinerary-detail-hero__btn, .itinerary-spot-btn');
    buttons.forEach(button => {
        if (button.title) {
            // 工具提示已經設定
        }
    });
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
window.toggleFavorite = toggleFavorite;
window.copyItinerary = copyItinerary;
window.shareItinerary = shareItinerary;
window.viewSpotDetail = viewSpotDetail;
window.viewSpotOnMap = viewSpotOnMap; 