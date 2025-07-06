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
            const itineraryId = button.dataset.id || button.getAttribute('data-id');
            if (itineraryId) {
                editItinerary(itineraryId);
            } else {
                // 如果無法從data-id獲取，嘗試從href獲取
                const href = button.getAttribute('href');
                if (href) {
                    window.location.href = href;
                }
            }
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
            deleteItinerary(itineraryId);
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
    // 檢查頁面上是否已經有行程數據
    const itineraryGrid = document.getElementById('itineraryGrid');
    const existingCards = itineraryGrid ? itineraryGrid.querySelectorAll('.itinerary-card') : [];
    
    // 如果已經有行程卡片，則從DOM中提取數據
    if (existingCards.length > 0) {
        console.log('從頁面提取行程數據');
        
        // 提取行程數據
        currentItineraries = Array.from(existingCards).map(card => {
            const id = card.querySelector('.action-btn.edit-btn').getAttribute('href').split('/').pop();
            const name = card.querySelector('.card-title').textContent;
            const description = card.querySelector('.card-description').textContent;
            const isPublic = card.querySelector('.status-badge').classList.contains('status-public');
            const createdAt = card.querySelector('.meta-item span:last-child').textContent;
            
            return {
                id: id,
                name: name,
                description: description,
                isPublic: isPublic,
                createdAt: createdAt
            };
        });
        
        // 更新過濾後的行程
        filteredItineraries = [...currentItineraries];
        
        // 更新統計
        updateStatisticsFromDOM();
        
        // 綁定事件
        bindItineraryActions();
        
        return;
    }
    
    // 如果沒有現有數據，則從API獲取
    showLoading();
    
    // 從 API 獲取數據
    fetch('/api/itinerary/my')
        .then(response => {
            if (!response.ok) {
                if (response.status === 401) {
                    // 未登入，重定向到登入頁面
                    window.location.href = '/members/login?redirect=/itinerary/my';
                    throw new Error('請先登入會員');
                }
                throw new Error(`API 錯誤 (${response.status})`);
            }
            return response.json();
        })
        .then(data => {
            if (!data.success) {
                throw new Error(data.message || '載入失敗');
            }
            
            console.log('成功載入我的行程數據:', data);
            
            // 更新全局數據
            currentItineraries = data.itineraries || [];
            filteredItineraries = [...currentItineraries];
            
            // 更新統計
            updateStatistics(data.stats);
            
            // 渲染行程列表
            renderItineraries();
            
            // 檢查空狀態
            checkEmptyState();
            
            // 隱藏載入中狀態
            hideLoading();
        })
        .catch(error => {
            console.error('載入我的行程失敗:', error);
            
            // 顯示錯誤狀態
            showError(error.message);
            
            // 隱藏載入中狀態
            hideLoading();
        });
}

/**
 * 顯示載入中狀態
 */
function showLoading() {
    const grid = document.getElementById('itineraryGrid');
    if (grid) {
        grid.innerHTML = `
            <div class="loading-state">
                <div class="loading-spinner"></div>
                <p>載入中，請稍候...</p>
            </div>
        `;
    }
}

/**
 * 隱藏載入中狀態
 */
function hideLoading() {
    // 載入完成後會被 renderItineraries 替換，不需要額外處理
}

/**
 * 顯示錯誤狀態
 */
function showError(message) {
    const grid = document.getElementById('itineraryGrid');
    if (grid) {
        grid.innerHTML = `
            <div class="error-state">
                <span class="material-icons">error</span>
                <p>載入失敗</p>
                <p class="error-message">${message}</p>
                <button onclick="loadMyItineraries()" class="itn-capsule-btn">
                    <span class="material-icons">refresh</span>
                    重試
                </button>
            </div>
        `;
    }
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
            (itinerary.name && itinerary.name.toLowerCase().includes(keyword)) ||
            (itinerary.description && itinerary.description.toLowerCase().includes(keyword))
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
 * @param {string} itineraryId - 行程ID
 */
function deleteItinerary(itineraryId) {
    if (!itineraryId) return;
    
    // 找到行程名稱
    const card = document.querySelector(`.itinerary-card [data-id="${itineraryId}"]`).closest('.itinerary-card');
    const title = card ? card.querySelector('.card-title').textContent : '此行程';
    
    // 確認對話框
    showConfirmModal(
        '刪除行程', 
        `確定要刪除「${title}」嗎？此操作無法復原。`,
        function() {
            // 使用者確認後執行
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
        // 禁用按鈕並顯示載入中
        deleteBtn.disabled = true;
        const originalHtml = deleteBtn.innerHTML;
        deleteBtn.innerHTML = '<span class="material-icons">hourglass_empty</span>';
        
        // 發送API請求
        fetch(`/itinerary/delete/${itineraryId}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            credentials: 'same-origin'
        })
        .then(response => {
            if (!response.ok) {
                throw new Error(`API 錯誤 (${response.status})`);
            }
            return response.json();
        })
        .then(data => {
            if (data.success) {
                // 顯示成功訊息
                showToast(data.message || '行程刪除成功', 'success');
                
                // 移除 DOM 元素
                const card = deleteBtn.closest('.itinerary-card');
                if (card) {
                    card.style.opacity = '0';
                    card.style.transform = 'translateY(-20px)';
                    setTimeout(() => {
                        card.remove();
                        
                        // 檢查是否需要顯示空狀態
                        const remainingCards = document.querySelectorAll('.itinerary-card');
                        if (remainingCards.length === 0) {
                            // 重新載入頁面以顯示空狀態
                            window.location.reload();
                        }
                    }, 300);
                }
            } else {
                // 顯示錯誤訊息
                showToast(data.message || '刪除失敗', 'error');
                
                // 恢復按鈕狀態
                deleteBtn.disabled = false;
                deleteBtn.innerHTML = originalHtml;
            }
        })
        .catch(error => {
            console.error('刪除行程失敗:', error);
            showToast('操作失敗，請稍後再試', 'error');
            
            // 恢復按鈕狀態
            deleteBtn.disabled = false;
            deleteBtn.innerHTML = originalHtml;
        });
    }
}

/**
 * 切換行程公開/私人狀態
 * @param {string} itineraryId - 行程ID
 * @param {boolean} currentPublic - 當前是否公開
 * @param {HTMLElement} button - 按鈕元素
 */
function togglePublicStatus(itineraryId, currentPublic, button) {
    // 確認對話框
    const newStatus = !currentPublic;
    const confirmMessage = newStatus 
        ? '確定要將此行程設為公開嗎？公開後所有人都能查看此行程。' 
        : '確定要將此行程設為私人嗎？設為私人後只有您能查看此行程。';
    
    showConfirmModal(
        '切換行程狀態', 
        confirmMessage,
        function() {
            // 使用者確認後執行
            performTogglePublic(itineraryId, newStatus, button);
        }
    );
}

/**
 * 執行切換公開狀態
 * @param {string} itineraryId - 行程ID
 * @param {boolean} makePublic - 是否設為公開
 * @param {HTMLElement} button - 按鈕元素
 */
function performTogglePublic(itineraryId, makePublic, button) {
    // 顯示載入中
    button.disabled = true;
    const originalText = button.innerHTML;
    const loadingText = makePublic ? '正在設為公開...' : '正在設為私人...';
    button.innerHTML = `<span class="material-icons">hourglass_empty</span> ${loadingText}`;
    
    // 發送API請求
    fetch(`/itinerary/toggle-visibility/${itineraryId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        credentials: 'same-origin'
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`API 錯誤 (${response.status})`);
        }
        return response.json();
    })
    .then(data => {
        if (data.success) {
            // 顯示成功訊息
            showToast(data.message, 'success');
            
            // 重新載入頁面以更新UI
            setTimeout(() => {
                window.location.reload();
            }, 1000);
        } else {
            // 顯示錯誤訊息
            showToast(data.message || '操作失敗', 'error');
            
            // 恢復按鈕狀態
            button.disabled = false;
            button.innerHTML = originalText;
        }
    })
    .catch(error => {
        console.error('切換公開狀態失敗:', error);
        showToast('操作失敗，請稍後再試', 'error');
        
        // 恢復按鈕狀態
        button.disabled = false;
        button.innerHTML = originalText;
    });
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
    
    // 計算當前頁的數據
    const startIndex = 0;
    const endIndex = Math.min(currentPage * itemsPerPage, filteredItineraries.length);
    const currentPageData = filteredItineraries.slice(startIndex, endIndex);
    
    // 是否清空現有內容
    if (clearFirst) {
        grid.innerHTML = '';
    }
    
    // 如果沒有數據，顯示空狀態
    if (currentPageData.length === 0) {
        grid.innerHTML = `
            <div class="empty-state">
                <span class="material-icons">info</span>
                <p>尚未建立任何行程</p>
                <a href="/itinerary/add" class="itn-capsule-btn">
                    <span class="material-icons">add</span>
                    建立新行程
                </a>
            </div>
        `;
        return;
    }
    
    // 生成行程卡片 HTML
    let cardsHtml = '';
    currentPageData.forEach(itinerary => {
        cardsHtml += createItineraryCard(itinerary);
    });
    
    // 添加到網格
    grid.innerHTML = cardsHtml;
    
    // 如果有更多數據，顯示載入更多按鈕
    const loadMoreContainer = document.getElementById('loadMoreContainer');
    if (loadMoreContainer) {
        if (endIndex < filteredItineraries.length) {
            loadMoreContainer.style.display = 'flex';
            
            // 更新剩餘數量
            const remainingCount = filteredItineraries.length - endIndex;
            const loadMoreText = document.getElementById('loadMoreText');
            if (loadMoreText) {
                loadMoreText.textContent = `載入更多 (${remainingCount})`;
            }
        } else {
            loadMoreContainer.style.display = 'none';
        }
    }
}

/**
 * 創建行程卡片
 * @param {Object} itinerary - 行程數據
 * @returns {string} 行程卡片 HTML
 */
function createItineraryCard(itinerary) {
    const isPublic = itinerary.isPublic === true;
    const statusBadge = isPublic
        ? `<span class="status-badge status-public"><span class="material-icons">public</span>公開</span>`
        : `<span class="status-badge status-private"><span class="material-icons">lock</span>私人</span>`;
    
    const createdDate = itinerary.createdAt ? formatDate(itinerary.createdAt) : '';
    
    return `
        <article class="itinerary-card">
            <div class="card-header">
                <div class="card-status">
                    ${statusBadge}
                </div>
                <div class="card-actions">
                    <a href="/itinerary/edit/${itinerary.id}" class="action-btn edit-btn" title="編輯行程">
                        <span class="material-icons">edit</span>
                    </a>
                    <button class="action-btn delete-btn" title="刪除行程" data-id="${itinerary.id}" onclick="deleteItinerary('${itinerary.id}')">
                        <span class="material-icons">delete</span>
                    </button>
                </div>
            </div>
            <div class="card-content">
                <h3 class="card-title">${itinerary.name || '未命名行程'}</h3>
                <p class="card-description">${itinerary.description ? (itinerary.description.length > 100 ? itinerary.description.substring(0, 100) + '...' : itinerary.description) : '暫無描述'}</p>
                <div class="card-meta">
                    <div class="meta-item">
                        <span class="material-icons">schedule</span>
                        <span>${createdDate}</span>
                    </div>
                </div>
            </div>
            <div class="card-footer">
                <a href="/itinerary/detail/${itinerary.id}" class="itn-capsule-btn">
                    <span class="material-icons">visibility</span>
                    查看行程
                </a>
                ${isPublic ? `
                    <button class="itn-capsule-outline-btn toggle-public-btn" 
                            data-id="${itinerary.id}" 
                            data-public="true"
                            onclick="togglePublicStatus('${itinerary.id}', true, this)">
                        <span class="material-icons">lock</span>
                        設為私人
                    </button>
                ` : `
                    <button class="itn-capsule-outline-btn toggle-public-btn" 
                            data-id="${itinerary.id}" 
                            data-public="false"
                            onclick="togglePublicStatus('${itinerary.id}', false, this)">
                        <span class="material-icons">public</span>
                        設為公開
                    </button>
                `}
            </div>
        </article>
    `;
}

/**
 * 更新統計資訊
 */
function updateStatistics(stats = {}) {
    const totalCount = document.getElementById('totalCount');
    const publicCount = document.getElementById('publicCount');
    const privateCount = document.getElementById('privateCount');
    const totalSpots = document.getElementById('totalSpots');
    
    // 使用 API 返回的統計數據或計算當前數據
    const total = stats.total || currentItineraries.length;
    const publicItineraries = stats.public || currentItineraries.filter(item => item.isPublic === 1).length;
    const privateItineraries = stats.private || (total - publicItineraries);
    
    // 更新 DOM
    if (totalCount) totalCount.textContent = total;
    if (publicCount) publicCount.textContent = publicItineraries;
    if (privateCount) privateCount.textContent = privateItineraries;
    
    // 總景點數只在有明確數據時更新，否則保持後端傳遞的值
    if (totalSpots && stats.spots !== undefined) {
        totalSpots.textContent = stats.spots;
    }
}

/**
 * 從 DOM 中更新統計資訊
 */
function updateStatisticsFromDOM() {
    const totalCount = document.getElementById('totalCount');
    const publicCount = document.getElementById('publicCount');
    const privateCount = document.getElementById('privateCount');
    const totalSpots = document.getElementById('totalSpots');
    
    // 如果頁面上已有統計數據，則不需要再計算
    if (totalCount && publicCount && privateCount) {
        return;
    }
    
    // 否則，從當前行程數據計算
    const total = currentItineraries.length;
    const publicItineraries = currentItineraries.filter(item => item.isPublic).length;
    const privateItineraries = total - publicItineraries;
    
    // 更新 DOM（但不更新總景點數，保持後端傳遞的值）
    if (totalCount) totalCount.textContent = total;
    if (publicCount) publicCount.textContent = publicItineraries;
    if (privateCount) privateCount.textContent = privateItineraries;
    // 總景點數保持後端傳遞的值，不在前端計算
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