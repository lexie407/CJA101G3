/**
 * 我的行程頁面 JavaScript
 * 提供搜尋、篩選、行程管理等功能
 */

// 全域變數
let currentItineraries = [];
let filteredItineraries = [];
let currentPage = 1;
const itemsPerPage = 12;

/**
 * 頁面初始化
 */
document.addEventListener('DOMContentLoaded', function() {
    initializeMyItineraries();
});

/**
 * 初始化我的行程
 */
function initializeMyItineraries() {
    // 綁定搜尋表單事件
    bindSearchForm();
    
    // 綁定篩選事件
    bindFilterEvents();
    
    // 綁定行程操作事件
    bindItineraryActions();
    
    // 綁定載入更多事件
    bindLoadMoreEvent();
    
    // 綁定確認對話框事件
    bindModalEvents();
    
    // 載入初始資料
    loadMyItineraries();
    
    console.log('我的行程頁面初始化完成');
}

/**
 * 綁定搜尋表單事件
 */
function bindSearchForm() {
    const searchInput = document.getElementById('searchInput');
    const searchClear = document.getElementById('searchClear');
    const searchBtn = document.getElementById('searchBtn');
    const resetBtn = document.getElementById('resetBtn');
    
    if (searchInput && searchBtn && resetBtn) {
        // 搜尋按鈕點擊事件
        searchBtn.addEventListener('click', function() {
            performSearch();
        });
        
        // 輸入框 Enter 鍵事件
        searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                performSearch();
            }
        });
        
        // 重置按鈕點擊事件
        resetBtn.addEventListener('click', function() {
            resetFilters();
        });
    }
}

/**
 * 綁定篩選事件
 */
function bindFilterEvents() {
    const statusFilter = document.getElementById('statusFilter');
    const sortFilter = document.getElementById('sortFilter');
    
    if (statusFilter) {
        statusFilter.addEventListener('change', function() {
            applyFilters();
        });
    }
    
    if (sortFilter) {
        sortFilter.addEventListener('change', function() {
            applyFilters();
        });
    }
}

/**
 * 綁定行程操作事件
 */
function bindItineraryActions() {
    document.addEventListener('click', function(e) {
        // 編輯按鈕
        if (e.target.closest('.edit-btn')) {
            e.preventDefault();
            const button = e.target.closest('.edit-btn');
            const itineraryId = button.dataset.id;
            editItinerary(itineraryId);
        }
        
        // 複製按鈕
        if (e.target.closest('.copy-btn')) {
            e.preventDefault();
            const button = e.target.closest('.copy-btn');
            const itineraryId = button.dataset.id;
            copyItinerary(itineraryId);
        }
        
        // 刪除按鈕
        if (e.target.closest('.delete-btn')) {
            e.preventDefault();
            const button = e.target.closest('.delete-btn');
            const itineraryId = button.dataset.id;
            const card = button.closest('.itinerary-card');
            const title = card.querySelector('.card-title').textContent;
            deleteItinerary(itineraryId, title);
        }
        
        // 公開狀態切換
        if (e.target.closest('.toggle-public-btn')) {
            e.preventDefault();
            const button = e.target.closest('.toggle-public-btn');
            const itineraryId = button.dataset.id;
            const isPublic = button.dataset.public === 'true';
            togglePublicStatus(itineraryId, isPublic, button);
        }
    });
}

/**
 * 綁定載入更多事件
 */
function bindLoadMoreEvent() {
    const loadMoreBtn = document.getElementById('loadMoreBtn');
    if (loadMoreBtn) {
        loadMoreBtn.addEventListener('click', function() {
            loadMoreItineraries();
        });
    }
}

/**
 * 綁定確認對話框事件
 */
function bindModalEvents() {
    const modal = document.getElementById('confirmModal');
    const closeBtn = document.getElementById('modalClose');
    const cancelBtn = document.getElementById('confirmCancel');
    const okBtn = document.getElementById('confirmOk');
    
    if (modal && closeBtn && cancelBtn && okBtn) {
        // 關閉按鈕
        closeBtn.addEventListener('click', function() {
            hideModal();
        });
        
        // 取消按鈕
        cancelBtn.addEventListener('click', function() {
            hideModal();
        });
        
        // 點擊背景關閉
        modal.addEventListener('click', function(e) {
            if (e.target === modal) {
                hideModal();
            }
        });
        
        // ESC 鍵關閉
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape' && modal.classList.contains('show')) {
                hideModal();
            }
        });
    }
}

/**
 * 載入我的行程
 */
function loadMyItineraries() {
    // 模擬載入資料
    currentItineraries = generateMockData();
    filteredItineraries = [...currentItineraries];
    
    // 更新統計
    updateStatistics();
    
    // 渲染行程列表
    renderItineraries();
    
    // 檢查空狀態
    checkEmptyState();
}

/**
 * 執行搜尋
 */
function performSearch() {
    const searchInput = document.getElementById('searchInput');
    const keyword = searchInput.value.trim().toLowerCase();
    
    if (keyword === '') {
        filteredItineraries = [...currentItineraries];
    } else {
        filteredItineraries = currentItineraries.filter(itinerary => 
            itinerary.name.toLowerCase().includes(keyword) ||
            itinerary.description.toLowerCase().includes(keyword)
        );
    }
    
    // 重置頁碼
    currentPage = 1;
    
    // 應用其他篩選
    applyFilters(false);
    
    // 顯示搜尋結果提示
    if (keyword !== '') {
        showToast(`找到 ${filteredItineraries.length} 個符合「${keyword}」的行程`, 'success');
    }
}

/**
 * 重置篩選
 */
function resetFilters() {
    // 清空搜尋框
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.value = '';
    }
    
    // 重置篩選器
    const statusFilter = document.getElementById('statusFilter');
    const sortFilter = document.getElementById('sortFilter');
    
    if (statusFilter) {
        statusFilter.value = '';
    }
    
    if (sortFilter) {
        sortFilter.value = 'newest';
    }
    
    // 重置資料
    filteredItineraries = [...currentItineraries];
    currentPage = 1;
    
    // 重新渲染
    renderItineraries();
    checkEmptyState();
    
    showToast('篩選條件已重置', 'success');
}

/**
 * 應用篩選
 */
function applyFilters(resetPage = true) {
    const statusFilter = document.getElementById('statusFilter');
    const sortFilter = document.getElementById('sortFilter');
    
    let filtered = [...filteredItineraries];
    
    // 狀態篩選
    if (statusFilter && statusFilter.value) {
        const isPublic = statusFilter.value === 'public';
        filtered = filtered.filter(itinerary => itinerary.isPublic === isPublic);
    }
    
    // 排序
    if (sortFilter && sortFilter.value) {
        const sortBy = sortFilter.value;
        filtered.sort((a, b) => {
            switch (sortBy) {
                case 'newest':
                    return new Date(b.createdAt) - new Date(a.createdAt);
                case 'oldest':
                    return new Date(a.createdAt) - new Date(b.createdAt);
                case 'name':
                    return a.name.localeCompare(b.name);
                case 'spots':
                    return b.spotCount - a.spotCount;
                default:
                    return 0;
            }
        });
    }
    
    filteredItineraries = filtered;
    
    if (resetPage) {
        currentPage = 1;
    }
    
    renderItineraries();
    checkEmptyState();
}

/**
 * 編輯行程
 */
function editItinerary(itineraryId) {
    if (!itineraryId) return;
    
    window.location.href = `/itinerary/edit/${itineraryId}`;
}

/**
 * 複製行程
 */
function copyItinerary(itineraryId) {
    if (!itineraryId) return;
    
    const copyBtn = document.querySelector(`[data-id="${itineraryId}"].copy-btn`);
    if (!copyBtn) return;
    
    // 視覺回饋
    copyBtn.disabled = true;
    const originalHtml = copyBtn.innerHTML;
    copyBtn.innerHTML = '<span class="material-icons">hourglass_empty</span>';
    
    // 模擬 API 呼叫
    setTimeout(() => {
        const itinerary = currentItineraries.find(item => item.id == itineraryId);
        if (itinerary) {
            // 建立複製的行程
            const newItinerary = {
                ...itinerary,
                id: Date.now(),
                name: `${itinerary.name} (副本)`,
                createdAt: new Date().toISOString(),
                isPublic: false // 複製的行程預設為私人
            };
            
            // 添加到列表
            currentItineraries.unshift(newItinerary);
            filteredItineraries = [...currentItineraries];
            
            // 更新顯示
            updateStatistics();
            renderItineraries();
            
            showToast('行程複製成功！', 'success');
        } else {
            showToast('複製失敗，請稍後再試', 'error');
        }
        
        copyBtn.disabled = false;
        copyBtn.innerHTML = originalHtml;
    }, 1000);
}

/**
 * 刪除行程
 */
function deleteItinerary(itineraryId, itineraryName) {
    if (!itineraryId) return;
    
    showConfirmModal(
        `確定要刪除行程「${itineraryName}」嗎？`,
        '此操作無法復原，請謹慎考慮。',
        () => {
            performDeleteItinerary(itineraryId);
        }
    );
}

/**
 * 執行刪除行程
 */
function performDeleteItinerary(itineraryId) {
    const deleteBtn = document.querySelector(`[data-id="${itineraryId}"].delete-btn`);
    
    if (deleteBtn) {
        deleteBtn.disabled = true;
        const originalHtml = deleteBtn.innerHTML;
        deleteBtn.innerHTML = '<span class="material-icons">hourglass_empty</span>';
        
        // 模擬 API 呼叫
        setTimeout(() => {
            // 從資料中移除
            currentItineraries = currentItineraries.filter(item => item.id != itineraryId);
            filteredItineraries = filteredItineraries.filter(item => item.id != itineraryId);
            
            // 移除 DOM 元素
            const card = deleteBtn.closest('.itinerary-card');
            if (card) {
                card.style.opacity = '0';
                card.style.transform = 'translateY(-20px)';
                setTimeout(() => {
                    card.remove();
                    checkEmptyState();
                }, 300);
            }
            
            // 更新統計
            updateStatistics();
            
            showToast('行程刪除成功', 'success');
        }, 1000);
    }
}

/**
 * 切換公開狀態
 */
function togglePublicStatus(itineraryId, currentPublic, button) {
    if (!itineraryId || !button) return;
    
    const newPublic = !currentPublic;
    const action = newPublic ? '設為公開' : '設為私人';
    
    // 視覺回饋
    button.disabled = true;
    const originalHtml = button.innerHTML;
    button.innerHTML = '<span class="material-icons">hourglass_empty</span>處理中...';
    
    // 模擬 API 呼叫
    setTimeout(() => {
        // 更新資料
        const itinerary = currentItineraries.find(item => item.id == itineraryId);
        if (itinerary) {
            itinerary.isPublic = newPublic;
            
            // 更新按鈕
            button.dataset.public = newPublic;
            if (newPublic) {
                button.innerHTML = '<span class="material-icons">lock</span>設為私人';
                button.className = 'itn-capsule-outline-btn toggle-public-btn';
            } else {
                button.innerHTML = '<span class="material-icons">public</span>設為公開';
                button.className = 'itn-capsule-outline-btn toggle-public-btn';
            }
            
            // 更新狀態徽章
            const card = button.closest('.itinerary-card');
            const statusBadge = card.querySelector('.status-badge');
            if (statusBadge) {
                if (newPublic) {
                    statusBadge.className = 'status-badge status-public';
                    statusBadge.innerHTML = '<span class="material-icons">public</span>公開';
                } else {
                    statusBadge.className = 'status-badge status-private';
                    statusBadge.innerHTML = '<span class="material-icons">lock</span>私人';
                }
            }
            
            // 更新統計
            updateStatistics();
            
            showToast(`${action}成功`, 'success');
        } else {
            showToast(`${action}失敗`, 'error');
        }
        
        button.disabled = false;
    }, 800);
}

/**
 * 載入更多行程
 */
function loadMoreItineraries() {
    const loadMoreBtn = document.getElementById('loadMoreBtn');
    if (!loadMoreBtn) return;
    
    // 模擬載入
    loadMoreBtn.disabled = true;
    loadMoreBtn.innerHTML = '<span class="material-icons">hourglass_empty</span>載入中...';
    
    setTimeout(() => {
        currentPage++;
        renderItineraries(false);
        
        loadMoreBtn.disabled = false;
        loadMoreBtn.innerHTML = '<span class="material-icons">expand_more</span>載入更多行程';
        
        // 檢查是否還有更多資料
        const totalPages = Math.ceil(filteredItineraries.length / itemsPerPage);
        if (currentPage >= totalPages) {
            loadMoreBtn.style.display = 'none';
        }
    }, 1000);
}

/**
 * 渲染行程列表
 */
function renderItineraries(clearFirst = true) {
    const grid = document.getElementById('itineraryGrid');
    if (!grid) return;
    
    if (clearFirst) {
        grid.innerHTML = '';
        currentPage = 1;
    }
    
    const startIndex = (currentPage - 1) * itemsPerPage;
    const endIndex = startIndex + itemsPerPage;
    const itemsToShow = filteredItineraries.slice(startIndex, endIndex);
    
    itemsToShow.forEach(itinerary => {
        const card = createItineraryCard(itinerary);
        grid.appendChild(card);
    });
    
    // 更新載入更多按鈕顯示
    const loadMoreBtn = document.getElementById('loadMoreBtn');
    if (loadMoreBtn) {
        const totalPages = Math.ceil(filteredItineraries.length / itemsPerPage);
        loadMoreBtn.style.display = currentPage >= totalPages ? 'none' : 'block';
    }
}

/**
 * 建立行程卡片
 */
function createItineraryCard(itinerary) {
    const article = document.createElement('article');
    article.className = 'itinerary-card';
    
    const statusClass = itinerary.isPublic ? 'status-public' : 'status-private';
    const statusIcon = itinerary.isPublic ? 'public' : 'lock';
    const statusText = itinerary.isPublic ? '公開' : '私人';
    
    const toggleIcon = itinerary.isPublic ? 'lock' : 'public';
    const toggleText = itinerary.isPublic ? '設為私人' : '設為公開';
    
    article.innerHTML = `
        <div class="card-header">
            <div class="card-status">
                <span class="status-badge ${statusClass}">
                    <span class="material-icons">${statusIcon}</span>
                    ${statusText}
                </span>
            </div>
            <div class="card-actions">
                <button class="action-btn edit-btn" title="編輯行程" data-id="${itinerary.id}">
                    <span class="material-icons">edit</span>
                </button>
                <button class="action-btn copy-btn" title="複製行程" data-id="${itinerary.id}">
                    <span class="material-icons">content_copy</span>
                </button>
                <button class="action-btn delete-btn" title="刪除行程" data-id="${itinerary.id}">
                    <span class="material-icons">delete</span>
                </button>
            </div>
        </div>
        <div class="card-content">
            <h3 class="card-title">${itinerary.name}</h3>
            <p class="card-description">${itinerary.description}</p>
            <div class="card-meta">
                <div class="meta-item">
                    <span class="material-icons">schedule</span>
                    <span>${formatDate(itinerary.createdAt)}</span>
                </div>
                <div class="meta-item">
                    <span class="material-icons">place</span>
                    <span>${itinerary.spotCount}個景點</span>
                </div>
                <div class="meta-item">
                    <span class="material-icons">visibility</span>
                    <span>${itinerary.viewCount}次瀏覽</span>
                </div>
            </div>
        </div>
        <div class="card-footer">
            <a href="/itinerary/detail/${itinerary.id}" class="itn-capsule-btn">
                <span class="material-icons">visibility</span>
                查看行程
            </a>
            <button class="itn-capsule-outline-btn toggle-public-btn" data-id="${itinerary.id}" data-public="${itinerary.isPublic}">
                <span class="material-icons">${toggleIcon}</span>
                ${toggleText}
            </button>
        </div>
    `;
    
    return article;
}

/**
 * 更新統計資訊
 */
function updateStatistics() {
    const totalCount = document.getElementById('totalCount');
    const publicCount = document.getElementById('publicCount');
    const privateCount = document.getElementById('privateCount');
    const totalSpots = document.getElementById('totalSpots');
    
    const total = currentItineraries.length;
    const publicItineraries = currentItineraries.filter(item => item.isPublic).length;
    const privateItineraries = total - publicItineraries;
    const spots = currentItineraries.reduce((sum, item) => sum + item.spotCount, 0);
    
    if (totalCount) totalCount.textContent = total;
    if (publicCount) publicCount.textContent = publicItineraries;
    if (privateCount) privateCount.textContent = privateItineraries;
    if (totalSpots) totalSpots.textContent = spots;
}

/**
 * 檢查空狀態
 */
function checkEmptyState() {
    const grid = document.getElementById('itineraryGrid');
    const emptyState = document.getElementById('emptyState');
    
    if (!grid || !emptyState) return;
    
    const hasItineraries = filteredItineraries.length > 0;
    
    grid.style.display = hasItineraries ? 'grid' : 'none';
    emptyState.style.display = hasItineraries ? 'none' : 'block';
    
    // 更新載入更多按鈕
    const loadMoreSection = document.querySelector('.load-more-section');
    if (loadMoreSection) {
        loadMoreSection.style.display = hasItineraries ? 'block' : 'none';
    }
}

/**
 * 顯示確認對話框
 */
function showConfirmModal(title, message, onConfirm) {
    const modal = document.getElementById('confirmModal');
    const titleElement = document.querySelector('.modal-title');
    const messageElement = document.getElementById('confirmMessage');
    const okBtn = document.getElementById('confirmOk');
    
    if (modal && titleElement && messageElement && okBtn) {
        titleElement.textContent = title;
        messageElement.textContent = message;
        
        // 移除舊的事件監聽器
        const newOkBtn = okBtn.cloneNode(true);
        okBtn.parentNode.replaceChild(newOkBtn, okBtn);
        
        // 添加新的事件監聽器
        newOkBtn.addEventListener('click', function() {
            hideModal();
            if (onConfirm) onConfirm();
        });
        
        modal.classList.add('show');
    }
}

/**
 * 隱藏確認對話框
 */
function hideModal() {
    const modal = document.getElementById('confirmModal');
    if (modal) {
        modal.classList.remove('show');
    }
}

/**
 * 顯示 Toast 通知
 */
function showToast(message, type = 'info') {
    const toast = document.getElementById('toast');
    if (!toast) return;
    
    toast.textContent = message;
    toast.className = `toast ${type}`;
    
    // 顯示
    setTimeout(() => {
        toast.classList.add('show');
    }, 100);
    
    // 隱藏
    setTimeout(() => {
        toast.classList.remove('show');
    }, 3000);
}

/**
 * 格式化日期
 */
function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('zh-TW', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
    });
}

/**
 * 生成模擬資料
 */
function generateMockData() {
    return [
        {
            id: 1,
            name: '台北一日遊',
            description: '探索台北市區的經典景點，包含台北101、故宮博物院、士林夜市等熱門景點，適合初次造訪台北的旅客。',
            isPublic: true,
            spotCount: 8,
            viewCount: 156,
            createdAt: '2024-01-15T10:00:00Z'
        },
        {
            id: 2,
            name: '花蓮太魯閣三日遊',
            description: '深度探索太魯閣國家公園的壯麗峽谷風光，包含砂卡礑步道、燕子口、九曲洞等必訪景點。',
            isPublic: true,
            spotCount: 12,
            viewCount: 89,
            createdAt: '2024-01-12T14:30:00Z'
        },
        {
            id: 3,
            name: '私人度假計畫',
            description: '個人專屬的放鬆行程，遠離城市喧囂，享受寧靜的溫泉度假時光。',
            isPublic: false,
            spotCount: 5,
            viewCount: 0,
            createdAt: '2024-01-10T09:15:00Z'
        },
        {
            id: 4,
            name: '南投清境農場',
            description: '高山牧場風光與原住民文化體驗，包含清境農場、合歡山、廬山溫泉等景點。',
            isPublic: true,
            spotCount: 6,
            viewCount: 234,
            createdAt: '2024-01-08T16:45:00Z'
        },
        {
            id: 5,
            name: '墾丁海洋假期',
            description: '南台灣熱帶風情體驗，包含墾丁國家公園、鵝鑾鼻燈塔、南灣海灘等經典景點。',
            isPublic: false,
            spotCount: 7,
            viewCount: 0,
            createdAt: '2024-01-05T11:20:00Z'
        },
        {
            id: 6,
            name: '阿里山日出之旅',
            description: '體驗阿里山壯麗日出與神木群，包含阿里山森林遊樂區、奮起湖老街、茶園風光。',
            isPublic: true,
            spotCount: 9,
            viewCount: 312,
            createdAt: '2024-01-03T07:00:00Z'
        },
        {
            id: 7,
            name: '宜蘭親子樂園',
            description: '適合全家大小的宜蘭親子旅遊，包含傳統藝術中心、幾米公園、羅東夜市等景點。',
            isPublic: true,
            spotCount: 10,
            viewCount: 178,
            createdAt: '2024-01-01T13:30:00Z'
        },
        {
            id: 8,
            name: '私密溫泉之旅',
            description: '精選私房溫泉景點，享受寧靜的泡湯體驗，遠離人群的喧囂。',
            isPublic: false,
            spotCount: 4,
            viewCount: 0,
            createdAt: '2023-12-28T15:45:00Z'
        }
    ];
} 