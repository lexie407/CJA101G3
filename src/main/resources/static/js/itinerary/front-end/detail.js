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
    // 登入按鈕
    const loginBtn = document.querySelector('.logout-btn');
    if (loginBtn) {
        loginBtn.addEventListener('click', function() {
            window.location.href = '/members/login';
        });
    }

    // 收藏按鈕 (頂部操作按鈕)
    const favoriteBtn = document.querySelector('.itinerary-action-btn--favorite');
    if (favoriteBtn) {
        favoriteBtn.addEventListener('click', function(e) {
            e.preventDefault();
            toggleFavorite();
        });
    }
    
    // 底部收藏按鈕
    const bottomFavoriteBtn = document.querySelector('#favoriteBtn');
    if (bottomFavoriteBtn) {
        bottomFavoriteBtn.addEventListener('click', function(e) {
            e.preventDefault();
            toggleFavorite();
        });
    }
    
    // 複製按鈕
    const copyBtn = document.querySelector('#copyBtn');
    if (copyBtn) {
        copyBtn.addEventListener('click', function(e) {
            e.preventDefault();
            copyItinerary();
        });
    }
    
    // 編輯按鈕
    const editBtn = document.querySelector('#editBtn');
    if (editBtn) {
        editBtn.addEventListener('click', function(e) {
            e.preventDefault();
            editItinerary();
        });
    }
    
    // 建立揪團按鈕
    const createActivityBtn = document.querySelector('#createActivityBtn');
    if (createActivityBtn) {
        createActivityBtn.addEventListener('click', function(e) {
            e.preventDefault();
            createActivity();
        });
    }
    
    // 景點按鈕
    document.addEventListener('click', function(e) {
        const button = e.target.closest('.itinerary-spot-btn');
        if (button) {
            e.preventDefault();
            const spotId = button.getAttribute('data-spot-id');
            
            if (button.title.includes('詳情')) {
                viewSpotDetail(spotId);
            } else if (button.title.includes('地圖')) {
                viewSpotOnMap(spotId);
            }
        }
    });

    // 登入對話框關閉按鈕
    const loginModalContainer = document.getElementById('loginModalContainer');
    if (loginModalContainer) {
        // 關閉按鈕
        const closeButtons = loginModalContainer.querySelectorAll('.modal-close');
        closeButtons.forEach(button => {
            button.addEventListener('click', () => closeLoginModal(loginModalContainer));
        });

        // 背景點擊關閉
        const backdrop = loginModalContainer.querySelector('.modal-backdrop');
        if (backdrop) {
            backdrop.addEventListener('click', () => closeLoginModal(loginModalContainer));
        }

        // ESC 鍵關閉
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape' && loginModalContainer.style.display === 'block') {
                closeLoginModal(loginModalContainer);
            }
        });
    }
}

/**
 * 載入行程資料
 */
function loadItineraryData() {
    if (!itineraryId) return;
    
    // 修正 API 路徑
    fetch(`/itinerary/api/${itineraryId}`)
        .then(response => {
            if (response.status === 404) {
                throw new Error('找不到此行程');
            }
            if (!response.ok) {
                throw new Error('載入失敗');
            }
            return response.json();
        })
        .then(data => {
            if (data.success) {
                updatePageData(data);
            } else {
                showToast(data.message || '載入失敗', 'error');
            }
        })
        .catch(error => {
            console.error('載入行程資料失敗:', error);
            showToast(error.message || '網路錯誤，請稍後再試', 'error');
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
    
    const favoriteBtn = document.querySelector('.itinerary-action-btn--favorite');
    const bottomFavoriteBtn = document.querySelector('#favoriteBtn');
    if (favoriteBtn) favoriteBtn.disabled = true;
    if (bottomFavoriteBtn) bottomFavoriteBtn.disabled = true;
    
    // 呼叫收藏API
    fetch(`/itinerary/api/${itineraryId}/favorite`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
        }
    })
    .then(response => {
        if (response.status === 401) {
            // 未登入，顯示登入提示
            showLoginRequiredModal('favorite');
            throw new Error('請先登入');
        }
        if (!response.ok) {
            throw new Error('伺服器回應錯誤');
        }
        return response.json();
    })
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
        if (error.message !== '請先登入') {
            showToast('網路錯誤，請稍後再試', 'error');
        }
    })
    .finally(() => {
        if (favoriteBtn) favoriteBtn.disabled = false;
        if (bottomFavoriteBtn) bottomFavoriteBtn.disabled = false;
    });
}

/**
 * 更新收藏按鈕狀態
 */
function updateFavoriteButton() {
    // 更新頂部收藏按鈕
    const favoriteBtn = document.querySelector('.itinerary-action-btn--favorite');
    if (favoriteBtn) {
        favoriteBtn.classList.toggle('active', isFavorited);
        favoriteBtn.title = isFavorited ? '取消收藏' : '加入收藏';
        const icon = favoriteBtn.querySelector('.material-icons');
        if (icon) {
            icon.textContent = isFavorited ? 'favorite' : 'favorite_border';
        }
    }
    
    // 更新底部收藏按鈕
    const bottomFavoriteBtn = document.querySelector('#favoriteBtn');
    if (bottomFavoriteBtn) {
        bottomFavoriteBtn.classList.toggle('active', isFavorited);
        bottomFavoriteBtn.title = isFavorited ? '取消收藏' : '加入收藏';
        const text = bottomFavoriteBtn.querySelector('span:not(.material-icons)');
        if (text) {
            text.textContent = isFavorited ? '取消收藏' : '加入收藏';
        }
        const icon = bottomFavoriteBtn.querySelector('.material-icons');
        if (icon) {
            icon.textContent = isFavorited ? 'favorite' : 'favorite_border';
        }
    }
}

/**
 * 複製行程
 */
function copyItinerary() {
    if (!itineraryId) return;
    
    // 先檢查登入狀態
    fetch(`/itinerary/api/${itineraryId}/check-duplicate`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
        }
    })
    .then(response => {
        if (response.status === 401) {
            // 未登入，顯示登入提示
            showLoginRequiredModal('copy');
            throw new Error('請先登入');
        }
        if (!response.ok) {
            throw new Error('伺服器回應錯誤');
        }
        return response.json();
    })
    .then(data => {
        if (data.success) {
            // 已登入，顯示複製行程對話框
            showCustomNameModal(
                '複製行程',
                '請為複製的行程輸入新名稱：',
                document.querySelector('.itinerary-detail-title').textContent + ' - 複製',
                processCopyItinerary
            );
        } else {
            showToast(data.message || '無法複製行程', 'error');
        }
    })
    .catch(error => {
        console.error('檢查登入狀態失敗:', error);
        if (error.message !== '請先登入') {
            showToast('網路錯誤，請稍後再試', 'error');
        }
    });
}

/**
 * 處理行程複製請求
 */
function processCopyItinerary(customName) {
    // 發送複製請求
    fetch(`/api/itinerary/${itineraryId}/copy`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            customName: customName
        })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showToast('行程複製成功！', 'success');
            // 可選：導向到新複製的行程
            if (data.newItineraryId) {
                setTimeout(() => {
                    window.location.href = `/itinerary/detail/${data.newItineraryId}`;
                }, 1500);
            } else {
                // 導向到我的行程列表
                setTimeout(() => {
                    window.location.href = '/itinerary/my';
                }, 1500);
            }
        } else {
            showToast(data.message || '行程複製失敗', 'error');
        }
    })
    .catch(error => {
        console.error('複製行程失敗:', error);
        showToast('網路錯誤，請稍後再試', 'error');
    });
}

/**
 * 編輯行程
 */
function editItinerary() {
    if (!itineraryId) {
        showToast('無法獲取行程ID', 'error');
        return;
    }
    
    // 直接導向到編輯頁面
    window.location.href = `/itinerary/edit/${itineraryId}`;
}

/**
 * 建立揪團
 */
function createActivity() {
    if (!itineraryId) {
        showToast('無法獲取行程ID', 'error');
        return;
    }
    
    // 確認是否已登入
    fetch('/api/members/check-login')
        .then(response => response.json())
        .then(data => {
            if (!data.isLoggedIn) {
                // 未登入，顯示提示並導向登入頁
                showLoginRequiredModal('activity');
                return;
            }
            
            // 已登入，顯示確認對話框
            showConfirmModal(
                '建立揪團',
                '確定要以此行程建立揪團活動嗎？系統會自動複製此行程作為揪團活動的行程。',
                '確定建立',
                () => {
                    // 導向到建立揪團頁面，並帶上行程ID
                    window.location.href = `/groupactivity/create?itnId=${itineraryId}`;
                }
            );
        })
        .catch(error => {
            console.error('檢查登入狀態失敗:', error);
            showToast('網路錯誤，請稍後再試', 'error');
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

/**
 * 顯示登入提示對話框
 */
function showLoginRequiredModal(action) {
    const modalContainer = document.getElementById('loginModalContainer');
    if (!modalContainer) return;

    // 保存當前頁面 URL 到 localStorage
    localStorage.setItem('redirectUrl', window.location.href);

    // 更新提示訊息
    const messageElements = modalContainer.querySelectorAll('.modal-message');
    if (messageElements && messageElements.length >= 2) {
        let actionText = '';
        switch (action) {
            case 'favorite':
                actionText = '收藏行程';
                break;
            case 'copy':
                actionText = '複製行程';
                break;
            case 'edit':
                actionText = '編輯行程';
                break;
            case 'activity':
                actionText = '建立揪團';
                break;
            default:
                actionText = '繼續操作';
        }
        messageElements[0].textContent = `您需要先登入才能${actionText}。`;
    }

    // 顯示對話框
    modalContainer.style.display = 'block';
    document.body.style.overflow = 'hidden';

    // 綁定關閉事件
    const closeButtons = modalContainer.querySelectorAll('.modal-close');
    closeButtons.forEach(button => {
        button.addEventListener('click', () => closeLoginModal(modalContainer));
    });

    // 綁定登入按鈕事件
    const loginBtn = modalContainer.querySelector('.modal-login-btn');
    if (loginBtn) {
        loginBtn.addEventListener('click', () => {
            window.location.href = '/members/login';
        });
    }

    // 點擊背景關閉
    const backdrop = modalContainer.querySelector('.modal-backdrop');
    if (backdrop) {
        backdrop.addEventListener('click', () => closeLoginModal(modalContainer));
    }

    // 按 ESC 關閉
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape' && modalContainer.style.display === 'block') {
            closeLoginModal(modalContainer);
        }
    });
}

/**
 * 關閉登入提示對話框
 */
function closeLoginModal(modal) {
    if (!modal) return;
    
    modal.style.display = 'none';
    document.body.style.overflow = '';
}

/**
 * 顯示確認對話框
 */
function showConfirmModal(title, message, confirmText, onConfirm) {
    // 創建模態對話框
    const modal = document.createElement('div');
    modal.className = 'itinerary-modal';
    modal.innerHTML = `
        <div class="itinerary-modal-content">
            <div class="itinerary-modal-header">
                <h3>${title}</h3>
                <button class="itinerary-modal-close">&times;</button>
            </div>
            <div class="itinerary-modal-body">
                <p>${message}</p>
            </div>
            <div class="itinerary-modal-footer">
                <button class="itinerary-modal-btn itinerary-modal-btn-secondary">取消</button>
                <button class="itinerary-modal-btn itinerary-modal-btn-primary">${confirmText}</button>
            </div>
        </div>
    `;
    
    // 添加到頁面
    document.body.appendChild(modal);
    
    // 顯示模態對話框
    setTimeout(() => {
        modal.classList.add('show');
        modal.querySelector('.itinerary-modal-content').style.opacity = '1';
        modal.querySelector('.itinerary-modal-content').style.transform = 'translateY(0)';
    }, 10);
    
    // 關閉按鈕事件
    const closeBtn = modal.querySelector('.itinerary-modal-close');
    closeBtn.addEventListener('click', () => closeModal(modal));
    
    // 取消按鈕事件
    const cancelBtn = modal.querySelector('.itinerary-modal-btn-secondary');
    cancelBtn.addEventListener('click', () => closeModal(modal));
    
    // 確認按鈕事件
    const confirmBtn = modal.querySelector('.itinerary-modal-btn-primary');
    confirmBtn.addEventListener('click', () => {
        closeModal(modal);
        if (typeof onConfirm === 'function') {
            onConfirm();
        }
    });
    
    // 點擊背景關閉
    modal.addEventListener('click', (e) => {
        if (e.target === modal) {
            closeModal(modal);
        }
    });
}

/**
 * 顯示自定義名稱對話框
 */
function showCustomNameModal(title, message, defaultName, onConfirm) {
    // 創建模態對話框
    const modal = document.createElement('div');
    modal.className = 'itinerary-modal';
    modal.innerHTML = `
        <div class="itinerary-modal-content">
            <div class="itinerary-modal-header">
                <h3>${title}</h3>
                <button class="itinerary-modal-close">&times;</button>
            </div>
            <div class="itinerary-modal-body">
                <p>${message}</p>
                <div class="itinerary-modal-input-group">
                    <input type="text" class="itinerary-modal-input" id="customNameInput" value="${defaultName}" placeholder="請輸入行程名稱">
                </div>
            </div>
            <div class="itinerary-modal-footer">
                <button class="itinerary-modal-btn itinerary-modal-btn-secondary">取消</button>
                <button class="itinerary-modal-btn itinerary-modal-btn-primary">確定</button>
            </div>
        </div>
    `;
    
    // 添加到頁面
    document.body.appendChild(modal);
    
    // 獲取輸入框元素
    const input = modal.querySelector('#customNameInput');
    input.focus();
    input.select();
    
    // 顯示模態對話框
    setTimeout(() => {
        modal.classList.add('show');
        modal.querySelector('.itinerary-modal-content').style.opacity = '1';
        modal.querySelector('.itinerary-modal-content').style.transform = 'translateY(0)';
    }, 10);
    
    // 關閉按鈕事件
    const closeBtn = modal.querySelector('.itinerary-modal-close');
    closeBtn.addEventListener('click', () => closeModal(modal));
    
    // 取消按鈕事件
    const cancelBtn = modal.querySelector('.itinerary-modal-btn-secondary');
    cancelBtn.addEventListener('click', () => closeModal(modal));
    
    // 確認按鈕事件
    const confirmBtn = modal.querySelector('.itinerary-modal-btn-primary');
    confirmBtn.addEventListener('click', () => {
        const customName = input.value.trim();
        if (!customName) {
            input.classList.add('error');
            setTimeout(() => input.classList.remove('error'), 1000);
            return;
        }
        closeModal(modal);
        if (typeof onConfirm === 'function') {
            onConfirm(customName);
        }
    });
    
    // 輸入框回車事件
    input.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            confirmBtn.click();
        }
    });
    
    // 點擊背景關閉
    modal.addEventListener('click', (e) => {
        if (e.target === modal) {
            closeModal(modal);
        }
    });
}

/**
 * 關閉模態對話框
 */
function closeModal(modal) {
    // 淡出動畫
    modal.querySelector('.itinerary-modal-content').style.opacity = '0';
    modal.querySelector('.itinerary-modal-content').style.transform = 'translateY(20px)';
    modal.classList.remove('show');
    
    // 移除模態對話框
    setTimeout(() => {
        if (document.body.contains(modal)) {
            document.body.removeChild(modal);
        }
    }, 300);
}

// 全域函數，供 HTML 直接呼叫
window.toggleFavorite = toggleFavorite;
window.copyItinerary = copyItinerary;
window.shareItinerary = shareItinerary;
window.viewSpotDetail = viewSpotDetail;
window.viewSpotOnMap = viewSpotOnMap; 