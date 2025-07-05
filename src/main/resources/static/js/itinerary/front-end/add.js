/**
 * 建立行程頁面 JavaScript
 * 處理行程建立的互動功能
 */

// 全域變數
let selectedSpots = [];
let allSpots = [];

// DOM 載入完成後初始化
document.addEventListener('DOMContentLoaded', function() {
    initializeItineraryAdd();
});

/**
 * 初始化建立行程頁面
 */
function initializeItineraryAdd() {
    // 檢查會員登入狀態
    checkLoginStatus();
    
    // 載入景點資料
    loadSpots();
    
    // 綁定搜尋功能
    bindSearchEvents();
    
    // 綁定表單驗證
    bindFormValidation();
    
    // 綁定公開設定選項
    bindVisibilityOptions();
    
    // 初始化工具提示
    initializeTooltips();

    // 初始化字數統計
    initializeCharacterCounter();
    
    console.log('建立行程頁面初始化完成');
}

/**
 * 檢查會員登入狀態
 */
function checkLoginStatus() {
    fetch("/api/session/currentMember")
        .then(res => res.json())
        .then(data => {
            if (!data.success) {
                showToast('請先登入會員', 'warning');
                setTimeout(() => {
                    window.location.href = "/members/login?redirect=/itinerary/add";
                }, 1500);
            }
        })
        .catch(error => {
            console.error('檢查登入狀態失敗', error);
        });
}

/**
 * 載入景點資料
 */
function loadSpots() {
    // 顯示載入中狀態
    const grid = document.querySelector('.itinerary-spots-grid');
    if (grid) {
        grid.innerHTML = `
            <div class="itinerary-loading">
                <span class="material-icons">hourglass_empty</span>
                <p>載入景點中...</p>
            </div>
        `;
    }
    
    // 從後端 API 獲取景點資料
    fetch('/api/spots?limit=12&status=1')
        .then(response => {
            if (!response.ok) {
                console.log('API響應狀態碼:', response.status);
                return response.text().then(text => {
                    console.log('API錯誤響應:', text);
                    throw new Error(`景點資料載入失敗 (${response.status})`);
                });
            }
            return response.json();
        })
        .then(data => {
            console.log('成功載入景點數據:', data.length);
            
            // 如果沒有數據，顯示空狀態
            if (!data || data.length === 0) {
                if (grid) {
                    grid.innerHTML = `
                        <div class="itinerary-empty-state">
                            <span class="material-icons">info</span>
                            <p>目前沒有可用的景點</p>
                        </div>
                    `;
                }
                return;
            }
            
            // 處理獲取的景點資料
            allSpots = data.map(spot => ({
                id: spot.spotId,
                name: spot.spotName,
                location: spot.spotLoc || '未知位置',
                rating: spot.googleRating || 4.0,
                icon: getSpotIcon(spot.zone || '其他')
            }));
            
            // 更新景點網格
            updateSpotsGrid(allSpots);
        })
        .catch(error => {
            console.error('載入景點資料失敗:', error);
            
            // 嘗試測試API是否正常工作
            fetch('/api/test/ping')
                .then(response => response.json())
                .then(data => {
                    console.log('API測試結果:', data);
                    showToast('API服務正常，但景點數據載入失敗，請稍後重試', 'warning');
                })
                .catch(testError => {
                    console.error('API測試失敗:', testError);
                    showToast('API服務異常，請聯繫管理員', 'error');
                });
            
            // 顯示錯誤狀態
            if (grid) {
                grid.innerHTML = `
                    <div class="itinerary-error-state">
                        <span class="material-icons">error</span>
                        <p>載入景點失敗</p>
                        <p class="error-message">${error.message}</p>
                        <button onclick="loadSpots()" class="itinerary-btn itinerary-btn--secondary">
                            <span class="material-icons">refresh</span>
                            重試
                        </button>
                    </div>
                `;
            }
        });
}

/**
 * 根據景點類型獲取對應的圖標
 */
function getSpotIcon(spotType) {
    const iconMap = {
        '自然風景': 'landscape',
        '自然景觀': 'landscape',
        '風景區': 'landscape',
        '國家公園': 'landscape',
        '歷史古蹟': 'account_balance',
        '古蹟': 'account_balance',
        '文化': 'account_balance',
        '博物館': 'museum',
        '展覽館': 'museum',
        '美術館': 'museum',
        '公園': 'park',
        '休閒': 'park',
        '宗教場所': 'temple_buddhist',
        '寺廟': 'temple_buddhist',
        '教堂': 'temple_buddhist',
        '購物': 'shopping_bag',
        '商圈': 'shopping_bag',
        '市場': 'shopping_bag',
        '美食': 'restaurant',
        '餐廳': 'restaurant',
        '小吃': 'restaurant',
        '夜市': 'nightlife',
        '海灘': 'beach_access',
        '海水浴場': 'beach_access',
        '溫泉': 'hot_tub',
        '遊樂園': 'attractions',
        '觀光工廠': 'attractions',
        '其他': 'place'
    };
    
    return iconMap[spotType] || 'place';
}

/**
 * 初始化字數統計
 */
function initializeCharacterCounter() {
    // 行程名稱字數統計
    const nameInput = document.getElementById('itnName');
    if (nameInput) {
        const nameCounter = createCharacterCounter(nameInput.parentNode, 2, 50);
        nameInput.addEventListener('input', function() {
            updateCharacterCount(this, nameCounter, 2, 50);
        });
        // 初始化字數
        updateCharacterCount(nameInput, nameCounter, 2, 50);
    }
    
    // 行程描述字數統計
    const descInput = document.getElementById('itnDesc');
    if (descInput) {
        const descCounter = createCharacterCounter(descInput.parentNode, 10, 500);
        descInput.addEventListener('input', function() {
            updateCharacterCount(this, descCounter, 10, 500);
        });
        // 初始化字數
        updateCharacterCount(descInput, descCounter, 10, 500);
    }
}

/**
 * 創建字數統計元素
 */
function createCharacterCounter(parentElement, minLength, maxLength) {
    const counter = document.createElement('div');
    counter.className = 'character-counter';
    counter.style.fontSize = '0.8rem';
    counter.style.color = '#666';
    counter.style.textAlign = 'right';
    counter.style.marginTop = '0.3rem';
    counter.style.transition = 'color 0.3s';
    counter.innerHTML = `<span class="current">0</span>/${maxLength}`;
    
    parentElement.appendChild(counter);
    return counter;
}

/**
 * 更新字數統計
 */
function updateCharacterCount(input, counterElement, minLength, maxLength) {
    const currentLength = input.value.length;
    const currentSpan = counterElement.querySelector('.current');
    
    currentSpan.textContent = currentLength;
    
    // 更新顏色
    if (currentLength === 0) {
        counterElement.style.color = '#f44336'; // 紅色：空值
    } else if (currentLength < minLength) {
        counterElement.style.color = '#ff9800'; // 橙色：低於最小值
    } else if (currentLength > maxLength * 0.9) {
        counterElement.style.color = '#ff9800'; // 橙色：接近最大值
    } else {
        counterElement.style.color = '#4caf50'; // 綠色：正常範圍
    }
    
    // 超出最大值
    if (currentLength > maxLength) {
        counterElement.style.color = '#f44336'; // 紅色：超出最大值
        input.style.borderColor = '#f44336';
    } else {
        input.style.borderColor = '';
    }
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
    
    grid.innerHTML = spots.map(spot => {
        // 解析評分
        const rating = parseFloat(spot.rating) || 0;
        const ratingValue = Math.min(5, Math.max(0, rating)); // 確保評分在0-5之間
        let starsHtml = '';
        
        // 生成星級評分HTML
        const fullStars = Math.floor(ratingValue);
        const hasHalfStar = ratingValue % 1 >= 0.3 && ratingValue % 1 < 0.8;
        const emptyStars = 5 - fullStars - (hasHalfStar ? 1 : 0);
        
        // 添加實心星星
        for (let i = 0; i < fullStars; i++) {
            starsHtml += '<span class="material-icons">star</span>';
        }
        
        // 添加半星（如果需要）
        if (hasHalfStar) {
            starsHtml += '<span class="material-icons">star_half</span>';
        }
        
        // 添加空心星星
        for (let i = 0; i < emptyStars; i++) {
            starsHtml += '<span class="material-icons">star_outline</span>';
        }
        
        // 處理名稱和地址中的特殊字符，避免JavaScript錯誤
        const safeName = spot.name.replace(/'/g, "\\'").replace(/"/g, "&quot;");
        const safeLocation = spot.location.replace(/'/g, "\\'").replace(/"/g, "&quot;");
        
        return `
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
                    ${starsHtml}
                    <span>${ratingValue.toFixed(1)}</span>
                </div>
            </div>
            <button type="button" class="itinerary-spot-card__add" onclick="addSpotToItinerary(${spot.id}, '${safeName}', '${safeLocation}')">
                <span class="material-icons">add</span>
            </button>
        </div>
        `;
    }).join('');
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
    
    // 即時驗證行程名稱
    const nameInput = document.getElementById('itnName');
    if (nameInput) {
        nameInput.addEventListener('blur', validateName);
        nameInput.addEventListener('input', function() {
            clearValidationError(this);
            // 即時檢查特殊字符
            checkSpecialCharacters(this);
        });
    }
    
    // 即時驗證行程描述
    const descInput = document.getElementById('itnDesc');
    if (descInput) {
        descInput.addEventListener('blur', validateDesc);
        descInput.addEventListener('input', function() {
            clearValidationError(this);
        });
    }
}

/**
 * 檢查特殊字符
 */
function checkSpecialCharacters(input) {
    const value = input.value;
    const specialChars = /[<>{}[\]|\\'"]/g;
    
    if (specialChars.test(value)) {
        const invalidChars = value.match(specialChars).join(' ');
        showValidationError(input, `行程名稱不能包含特殊字符: ${invalidChars}`);
        return false;
    }
    
    return true;
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
    
    // 驗證行程描述
    if (!validateDesc()) {
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
        showValidationError(nameInput, '行程名稱不能為空');
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
    
    // 檢查特殊字符
    if (!checkSpecialCharacters(nameInput)) {
        return false;
    }
    
    clearValidationError(nameInput);
    return true;
}

/**
 * 驗證行程描述
 */
function validateDesc() {
    const descInput = document.getElementById('itnDesc');
    if (!descInput) return true;
    
    const desc = descInput.value.trim();
    
    if (!desc) {
        showValidationError(descInput, '行程描述不能為空');
        return false;
    }
    
    if (desc.length < 10) {
        showValidationError(descInput, '行程描述至少需要10個字元');
        return false;
    }
    
    if (desc.length > 500) {
        showValidationError(descInput, '行程描述不能超過500個字元');
        return false;
    }
    
    clearValidationError(descInput);
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
    
    // 重置字數統計
    const nameInput = document.getElementById('itnName');
    const descInput = document.getElementById('itnDesc');
    const nameCounter = nameInput?.parentNode.querySelector('.character-counter');
    const descCounter = descInput?.parentNode.querySelector('.character-counter');
    
    if (nameInput && nameCounter) {
        updateCharacterCount(nameInput, nameCounter, 2, 50);
    }
    
    if (descInput && descCounter) {
        updateCharacterCount(descInput, descCounter, 10, 500);
    }
    
    showToast('表單已重置', 'info');
}

// 全域函數，供 HTML 直接呼叫
window.addSpotToItinerary = addSpotToItinerary;
window.removeSpotFromItinerary = removeSpotFromItinerary;
window.resetForm = resetForm; 