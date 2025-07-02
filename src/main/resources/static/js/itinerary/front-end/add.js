/**
 * 建立行程頁面 JavaScript
 * 處理行程建立的互動功能
 */

// 全域變數
let selectedSpots = [];
let allSpots = [
    { id: 1, name: '台北101', location: '台北市信義區', rating: 4.5, icon: 'landscape' },
    { id: 2, name: '故宮博物院', location: '台北市士林區', rating: 4.7, icon: 'museum' },
    { id: 3, name: '淡水老街', location: '新北市淡水區', rating: 4.3, icon: 'water' },
    { id: 4, name: '龍山寺', location: '台北市萬華區', rating: 4.4, icon: 'temple_buddhist' },
    { id: 5, name: '九份老街', location: '新北市瑞芳區', rating: 4.6, icon: 'landscape' },
    { id: 6, name: '西門町', location: '台北市萬華區', rating: 4.2, icon: 'shopping_bag' },
    { id: 7, name: '陽明山國家公園', location: '台北市北投區', rating: 4.5, icon: 'park' },
    { id: 8, name: '中正紀念堂', location: '台北市中正區', rating: 4.3, icon: 'account_balance' }
];

// DOM 載入完成後初始化
document.addEventListener('DOMContentLoaded', function() {
    initializeItineraryAdd();
});

/**
 * 初始化建立行程頁面
 */
function initializeItineraryAdd() {
    // 綁定搜尋功能
    bindSearchEvents();
    
    // 綁定表單驗證
    bindFormValidation();
    
    // 綁定公開設定選項
    bindVisibilityOptions();
    
    // 初始化工具提示
    initializeTooltips();
    
    console.log('建立行程頁面初始化完成');
}

/**
 * 綁定搜尋事件
 */
function bindSearchEvents() {
    const searchInput = document.getElementById('spotSearch');
    const searchBtn = document.querySelector('.itinerary-search-btn');
    
    if (searchInput && searchBtn) {
        // 搜尋按鈕點擊事件
        searchBtn.addEventListener('click', performSearch);
        
        // 輸入框回車事件
        searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                performSearch();
            }
        });
        
        // 即時搜尋（可選）
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
    const searchInput = document.getElementById('spotSearch');
    const keyword = searchInput?.value.toLowerCase().trim() || '';
    
    // 篩選景點
    let filteredSpots = allSpots;
    if (keyword) {
        filteredSpots = allSpots.filter(spot => 
            spot.name.toLowerCase().includes(keyword) ||
            spot.location.toLowerCase().includes(keyword)
        );
    }
    
    // 更新景點網格
    updateSpotsGrid(filteredSpots);
    
    // 顯示搜尋結果提示
    if (keyword && filteredSpots.length === 0) {
        showToast('找不到符合條件的景點', 'info');
    } else if (keyword) {
        showToast(`找到 ${filteredSpots.length} 個景點`, 'success');
    }
}

/**
 * 更新景點網格
 */
function updateSpotsGrid(spots) {
    const grid = document.querySelector('.itinerary-spots-grid');
    if (!grid) return;
    
    grid.innerHTML = spots.map(spot => `
        <div class="itinerary-spot-card" data-spot-id="${spot.id}">
            <div class="itinerary-spot-card__image">
                <span class="material-icons">${spot.icon}</span>
            </div>
            <div class="itinerary-spot-card__content">
                <h4 class="itinerary-spot-card__name">${spot.name}</h4>
                <p class="itinerary-spot-card__location">
                    <span class="material-icons">location_on</span>
                    ${spot.location}
                </p>
                <div class="itinerary-spot-card__rating">
                    <span class="material-icons">star</span>
                    <span>${spot.rating}</span>
                </div>
            </div>
            <button type="button" class="itinerary-spot-card__add" onclick="addSpotToItinerary(${spot.id}, '${spot.name}', '${spot.location}')">
                <span class="material-icons">add</span>
            </button>
        </div>
    `).join('');
}

/**
 * 添加景點到行程
 */
function addSpotToItinerary(spotId, spotName, spotLocation) {
    // 檢查是否已經選擇過
    if (selectedSpots.find(spot => spot.id === spotId)) {
        showToast('此景點已經在行程中！', 'warning');
        return;
    }
    
    // 添加到已選列表
    selectedSpots.push({
        id: spotId,
        name: spotName,
        location: spotLocation
    });
    
    // 更新UI
    updateSelectedSpotsList();
    updateSelectedCount();
    
    // 視覺反饋
    const spotCard = document.querySelector(`[data-spot-id="${spotId}"]`);
    if (spotCard) {
        const addBtn = spotCard.querySelector('.itinerary-spot-card__add');
        if (addBtn) {
            addBtn.style.background = '#4caf50';
            addBtn.style.borderColor = '#4caf50';
            addBtn.style.color = 'white';
            addBtn.querySelector('.material-icons').textContent = 'check';
            
            setTimeout(() => {
                addBtn.style.background = '';
                addBtn.style.borderColor = '';
                addBtn.style.color = '';
                addBtn.querySelector('.material-icons').textContent = 'add';
            }, 1000);
        }
    }
    
    showToast(`已添加 ${spotName}`, 'success');
}

/**
 * 從行程中移除景點
 */
function removeSpotFromItinerary(spotId) {
    const spot = selectedSpots.find(s => s.id === spotId);
    if (!spot) return;
    
    selectedSpots = selectedSpots.filter(s => s.id !== spotId);
    updateSelectedSpotsList();
    updateSelectedCount();
    
    showToast(`已移除 ${spot.name}`, 'info');
}

/**
 * 更新已選景點列表
 */
function updateSelectedSpotsList() {
    const container = document.getElementById('selectedSpotsList');
    if (!container) return;
    
    if (selectedSpots.length === 0) {
        container.innerHTML = `
            <div class="itinerary-empty-state">
                <span class="material-icons">info</span>
                <p>尚未選擇任何景點</p>
                <small>從上方推薦景點中選擇，或使用搜尋功能尋找更多景點</small>
            </div>
        `;
        return;
    }
    
    container.innerHTML = selectedSpots.map((spot, index) => `
        <div class="itinerary-selected-item">
            <div class="itinerary-selected-number">${index + 1}</div>
            <div class="itinerary-selected-content">
                <h4 class="itinerary-selected-name">${spot.name}</h4>
                <p class="itinerary-selected-location">${spot.location}</p>
            </div>
            <button type="button" class="itinerary-selected-remove" onclick="removeSpotFromItinerary(${spot.id})" title="移除景點">
                <span class="material-icons">close</span>
            </button>
            <input type="hidden" name="spotIds" value="${spot.id}">
        </div>
    `).join('');
}

/**
 * 更新已選景點數量
 */
function updateSelectedCount() {
    const countElement = document.getElementById('selectedCount');
    if (countElement) {
        countElement.textContent = `(${selectedSpots.length})`;
    }
}

/**
 * 綁定表單驗證
 */
function bindFormValidation() {
    const form = document.querySelector('.itinerary-add-form');
    if (!form) return;
    
    form.addEventListener('submit', function(e) {
        e.preventDefault();
        
        if (validateForm()) {
            submitForm();
        }
    });
    
    // 即時驗證
    const nameInput = document.getElementById('itnName');
    if (nameInput) {
        nameInput.addEventListener('blur', validateName);
        nameInput.addEventListener('input', clearValidationError);
    }
}

/**
 * 驗證表單
 */
function validateForm() {
    let isValid = true;
    
    // 驗證行程名稱
    if (!validateName()) {
        isValid = false;
    }
    
    // 驗證景點選擇
    if (selectedSpots.length === 0) {
        showToast('請至少選擇一個景點', 'warning');
        isValid = false;
    }
    
    return isValid;
}

/**
 * 驗證行程名稱
 */
function validateName() {
    const nameInput = document.getElementById('itnName');
    if (!nameInput) return true;
    
    const name = nameInput.value.trim();
    
    if (!name) {
        showValidationError(nameInput, '請輸入行程名稱');
        return false;
    }
    
    if (name.length < 2) {
        showValidationError(nameInput, '行程名稱至少需要2個字元');
        return false;
    }
    
    if (name.length > 50) {
        showValidationError(nameInput, '行程名稱不能超過50個字元');
        return false;
    }
    
    clearValidationError(nameInput);
    return true;
}

/**
 * 顯示驗證錯誤
 */
function showValidationError(input, message) {
    // 移除現有錯誤
    clearValidationError(input);
    
    // 添加錯誤樣式
    input.style.borderColor = '#f44336';
    input.style.boxShadow = '0 0 0 3px rgba(244, 67, 54, 0.1)';
    
    // 創建錯誤訊息
    const errorDiv = document.createElement('div');
    errorDiv.className = 'validation-error';
    errorDiv.style.color = '#f44336';
    errorDiv.style.fontSize = '0.8rem';
    errorDiv.style.marginTop = '0.5rem';
    errorDiv.style.display = 'flex';
    errorDiv.style.alignItems = 'center';
    errorDiv.style.gap = '0.3rem';
    errorDiv.innerHTML = `
        <span class="material-icons" style="font-size: 1rem;">error</span>
        ${message}
    `;
    
    // 插入錯誤訊息
    input.parentNode.appendChild(errorDiv);
}

/**
 * 清除驗證錯誤
 */
function clearValidationError(input) {
    if (typeof input === 'object' && input.target) {
        input = input.target;
    }
    
    // 恢復樣式
    input.style.borderColor = '';
    input.style.boxShadow = '';
    
    // 移除錯誤訊息
    const errorDiv = input.parentNode.querySelector('.validation-error');
    if (errorDiv) {
        errorDiv.remove();
    }
}

/**
 * 綁定公開設定選項
 */
function bindVisibilityOptions() {
    const radioButtons = document.querySelectorAll('input[name="isPublic"]');
    radioButtons.forEach(radio => {
        radio.addEventListener('change', function() {
            // 更新選中狀態的視覺效果已由CSS處理
            const visibility = this.value === '1' ? '公開' : '私人';
            console.log(`行程設定為：${visibility}`);
        });
    });
}

/**
 * 初始化工具提示
 */
function initializeTooltips() {
    // 為添加按鈕設置工具提示
    document.addEventListener('mouseover', function(e) {
        if (e.target.closest('.itinerary-spot-card__add')) {
            e.target.closest('.itinerary-spot-card__add').title = '添加到行程';
        }
    });
}

/**
 * 提交表單
 */
function submitForm() {
    const form = document.querySelector('.itinerary-add-form');
    if (!form) return;
    
    // 顯示載入狀態
    const submitBtn = form.querySelector('.itinerary-btn--primary');
    if (submitBtn) {
        const originalText = submitBtn.innerHTML;
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<span class="material-icons">hourglass_empty</span>建立中...';
        
        // 模擬提交延遲
        setTimeout(() => {
            // 實際提交表單
            form.submit();
        }, 1000);
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
            if (document.body.contains(toast)) {
                document.body.removeChild(toast);
            }
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
 * 重置表單
 */
function resetForm() {
    // 清空已選景點
    selectedSpots = [];
    updateSelectedSpotsList();
    updateSelectedCount();
    
    // 重置表單
    const form = document.querySelector('.itinerary-add-form');
    if (form) {
        form.reset();
    }
    
    // 清除所有驗證錯誤
    document.querySelectorAll('.validation-error').forEach(error => error.remove());
    document.querySelectorAll('input, textarea').forEach(input => {
        input.style.borderColor = '';
        input.style.boxShadow = '';
    });
    
    // 重置搜尋
    const searchInput = document.getElementById('spotSearch');
    if (searchInput) {
        searchInput.value = '';
        updateSpotsGrid(allSpots);
    }
    
    showToast('表單已重置', 'info');
}

// 全域函數，供 HTML 直接呼叫
window.addSpotToItinerary = addSpotToItinerary;
window.removeSpotFromItinerary = removeSpotFromItinerary;
window.resetForm = resetForm; 