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
    
    // 從後端 API 獲取景點資料 - 修正API路徑
    fetch('/api/spot/selector/public/list')
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
        .then(responseData => {
            console.log('API響應數據:', responseData);
            
            // 檢查API響應格式
            if (!responseData.success) {
                throw new Error(responseData.message || '景點資料載入失敗');
            }
            
            const data = responseData.data || [];
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
            
            // 如果是編輯模式，載入已選景點
            loadInitialSelectedSpots();
            
            // 更新景點網格
            updateSpotsGrid(allSpots);
        })
        .catch(error => {
            console.error('載入景點資料失敗:', error);
            
            // 嘗試使用備用API
            console.log('嘗試使用備用API...');
            fetch('/api/spot/public/list')
                .then(response => response.json())
                .then(responseData => {
                    if (responseData.success) {
                        const data = responseData.data || [];
                        console.log('成功從備用API載入景點數據:', data.length);
                        
                        // 處理獲取的景點資料
                        allSpots = data.map(spot => ({
                            id: spot.spotId,
                            name: spot.spotName,
                            location: spot.spotLoc || '未知位置',
                            rating: spot.googleRating || 4.0,
                            icon: getSpotIcon(spot.zone || '其他')
                        }));
                        
                        // 如果是編輯模式，載入已選景點
                        loadInitialSelectedSpots();
                        
                        // 更新景點網格
                        updateSpotsGrid(allSpots);
                    } else {
                        throw new Error('備用API也失敗');
                    }
                })
                .catch(backupError => {
                    console.error('備用API也失敗:', backupError);
                    
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
        });
}

/**
 * 載入編輯模式下的已選景點
 */
function loadInitialSelectedSpots() {
    const initialSpotsDiv = document.getElementById('initial-selected-spots');
    
    if (initialSpotsDiv && initialSpotsDiv.dataset.spotIds) {
        const initialIds = initialSpotsDiv.dataset.spotIds.split(',').map(id => parseInt(id, 10));
        
        if (initialIds.length > 0) {
            console.log('📝 編輯模式：發現已選景點IDs:', initialIds);
            
            // 從可選池中找出已選景點
            initialIds.forEach(id => {
                const spot = allSpots.find(s => s.id === id);
                if (spot) {
                    selectedSpots.push(spot);
                    console.log('✅ 已添加已選景點:', spot.name);
                } else {
                    console.warn('⚠️ 找不到景點ID:', id);
                }
            });
            
            // 更新已選景點列表
            updateSelectedSpotsList();
            updateSelectedCount();
        }
    }
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
 * @param {Array} spots 景點數據數組
 */
function updateSpotsGrid(spots) {
    const grid = document.querySelector('.itinerary-spots-grid');
    if (!grid) return;
    
    // 清空網格
    grid.innerHTML = '';
    
    // 如果沒有景點，顯示空狀態
    if (!spots || spots.length === 0) {
        grid.innerHTML = `
            <div class="itinerary-empty-state">
                <span class="material-icons">info</span>
                <p>沒有找到符合條件的景點</p>
            </div>
        `;
        return;
    }
    
    // 生成景點卡片
    spots.forEach(spot => {
        // 檢查該景點是否已被選擇
        const isSelected = selectedSpots.some(s => s.id === spot.id);
        if (isSelected) return; // 如果已選擇，跳過不顯示
        
        // 創建景點卡片
        const spotCard = document.createElement('div');
        spotCard.className = 'itinerary-spot-card';
        spotCard.dataset.spotId = spot.id;
        
        // 生成星級評分
        const ratingStars = generateRatingStars(spot.rating);
        
        // 設置卡片內容
        spotCard.innerHTML = `
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
                    ${ratingStars}
                    <span>${spot.rating ? spot.rating.toFixed(1) : 'N/A'}</span>
                </div>
            </div>
            <button type="button" class="itinerary-spot-card__add">
                <span class="material-icons">add</span>
            </button>
        `;
        
        // 添加點擊事件
        const addButton = spotCard.querySelector('.itinerary-spot-card__add');
        if (addButton) {
            addButton.addEventListener('click', () => {
                addSpotToItinerary(spot.id, spot.name, spot.location);
            });
        }
        
        // 添加到網格
        grid.appendChild(spotCard);
    });
    
    // 如果沒有可顯示的景點（全部都已選擇）
    if (grid.children.length === 0) {
        grid.innerHTML = `
            <div class="itinerary-empty-state">
                <span class="material-icons">check_circle</span>
                <p>所有景點都已選擇</p>
                <button onclick="loadSpots()" class="itinerary-btn itinerary-btn--secondary">
                    <span class="material-icons">refresh</span>
                    重新載入景點
                </button>
            </div>
        `;
    }
}

/**
 * 生成星級評分顯示
 * @param {number} rating 評分值
 * @returns {string} 星級HTML
 */
function generateRatingStars(rating) {
    if (!rating) return '<span class="material-icons">star_outline</span>'.repeat(5);
    
    const fullStars = Math.floor(rating);
    const halfStar = rating % 1 >= 0.5 ? 1 : 0;
    const emptyStars = 5 - fullStars - halfStar;
    
    let starsHTML = '';
    
    // 添加實心星星
    for (let i = 0; i < fullStars; i++) {
        starsHTML += '<span class="material-icons">star</span>';
    }
    
    // 添加半星
    if (halfStar) {
        starsHTML += '<span class="material-icons">star_half</span>';
    }
    
    // 添加空心星星
    for (let i = 0; i < emptyStars; i++) {
        starsHTML += '<span class="material-icons">star_outline</span>';
    }
    
    return starsHTML;
}

/**
 * 添加景點到行程
 * @param {number} spotId 景點ID
 * @param {string} spotName 景點名稱
 * @param {string} spotLocation 景點位置
 */
function addSpotToItinerary(spotId, spotName, spotLocation) {
    console.log(`添加景點: ID=${spotId}, 名稱=${spotName}, 位置=${spotLocation}`);
    
    // 檢查是否已達到最大景點數量限制
    const MAX_SPOTS = 10;
    if (selectedSpots.length >= MAX_SPOTS) {
        showToast(`最多只能選擇 ${MAX_SPOTS} 個景點`, 'warning');
        return;
    }
    
    // 檢查是否已經添加過該景點
    if (selectedSpots.some(spot => spot.id === spotId)) {
        showToast(`景點「${spotName}」已經添加過了`, 'info');
        return;
    }
    
    // 獲取景點圖標
    const spotData = allSpots.find(spot => spot.id === spotId);
    const icon = spotData ? spotData.icon : 'place';
    
    // 添加到已選景點列表
    selectedSpots.push({
        id: spotId,
        name: spotName,
        location: spotLocation,
        icon: icon
    });
    
    // 更新已選景點顯示
    updateSelectedSpotsList();
    
    // 更新景點計數
    updateSelectedCount();
    
    // 更新景點網格，移除已選景點
    updateSpotsGrid(allSpots);
    
    // 顯示成功提示
    showToast(`成功添加「${spotName}」到行程`, 'success');
    
    // 添加隱藏輸入欄位到表單
    addSpotToForm(spotId);
}

/**
 * 添加景點ID到表單
 * @param {number} spotId 景點ID
 */
function addSpotToForm(spotId) {
    const form = document.querySelector('.itinerary-add-form');
    if (!form) return;
    
    // 檢查是否已存在該景點的輸入欄位
    const existingInput = form.querySelector(`input[name="spotIds"][value="${spotId}"]`);
    if (existingInput) return;
    
    // 創建隱藏輸入欄位
    const input = document.createElement('input');
    input.type = 'hidden';
    input.name = 'spotIds';
    input.value = spotId;
    
    // 添加到表單
    form.appendChild(input);
    
    console.log(`已添加景點ID ${spotId} 到表單`);
}

/**
 * 從行程中移除景點
 * @param {number} spotId 景點ID
 */
function removeSpotFromItinerary(spotId) {
    // 從已選列表中移除
    selectedSpots = selectedSpots.filter(spot => spot.id !== spotId);
    
    // 更新已選景點顯示
    updateSelectedSpotsList();
    
    // 更新景點計數
    updateSelectedCount();
    
    // 更新景點網格，顯示被移除的景點
    updateSpotsGrid(allSpots);
    
    // 從表單中移除該景點的輸入欄位
    removeSpotFromForm(spotId);
    
    // 顯示提示
    showToast('已從行程中移除景點', 'info');
}

/**
 * 從表單中移除景點ID
 * @param {number} spotId 景點ID
 */
function removeSpotFromForm(spotId) {
    const form = document.querySelector('.itinerary-add-form');
    if (!form) return;
    
    // 查找並移除該景點的輸入欄位
    const input = form.querySelector(`input[name="spotIds"][value="${spotId}"]`);
    if (input) {
        input.remove();
        console.log(`已從表單移除景點ID ${spotId}`);
    }
}

/**
 * 更新已選景點列表顯示
 */
function updateSelectedSpotsList() {
    const container = document.querySelector('.itinerary-selected-spots');
    if (!container) return;
    
    // 清空容器
    container.innerHTML = '';
    
    // 如果沒有已選景點，顯示提示
    if (selectedSpots.length === 0) {
        container.innerHTML = `
            <div class="itinerary-empty-selection">
                <span class="material-icons">info</span>
                <p>尚未選擇任何景點</p>
                <p class="hint">從上方列表選擇景點添加到行程</p>
            </div>
        `;
        return;
    }
    
    // 創建已選景點列表
    const spotsList = document.createElement('div');
    spotsList.className = 'itinerary-selected-spots-list';
    
    // 添加已選景點
    selectedSpots.forEach((spot, index) => {
        const spotItem = document.createElement('div');
        spotItem.className = 'itinerary-selected-spot-item';
        spotItem.dataset.spotId = spot.id;
        
        // 設置內容
        spotItem.innerHTML = `
            <div class="itinerary-selected-spot-item__order">${index + 1}</div>
            <div class="itinerary-selected-spot-item__icon">
                <span class="material-icons">${spot.icon || 'place'}</span>
            </div>
            <div class="itinerary-selected-spot-item__content">
                <h5 class="itinerary-selected-spot-item__name">${spot.name}</h5>
                <p class="itinerary-selected-spot-item__location">
                    <span class="material-icons">location_on</span>
                    ${spot.location}
                </p>
            </div>
            <button type="button" class="itinerary-selected-spot-item__remove">
                <span class="material-icons">close</span>
            </button>
        `;
        
        // 添加移除按鈕事件
        const removeBtn = spotItem.querySelector('.itinerary-selected-spot-item__remove');
        if (removeBtn) {
            removeBtn.addEventListener('click', () => {
                removeSpotFromItinerary(spot.id);
            });
        }
        
        // 添加到列表
        spotsList.appendChild(spotItem);
    });
    
    // 添加到容器
    container.appendChild(spotsList);
}

/**
 * 更新已選景點數量
 */
function updateSelectedCount() {
    const countElement = document.getElementById('selectedCount');
    if (!countElement) return;
    
    // 更新數量
    countElement.textContent = selectedSpots.length;
    
    // 更新樣式
    if (selectedSpots.length > 0) {
        countElement.classList.add('has-spots');
    } else {
        countElement.classList.remove('has-spots');
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
        showValidationError(descInput, '行程描述至少需要5個字元');
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