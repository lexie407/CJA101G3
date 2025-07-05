/**
 * 景點選擇器 - 用於後台行程建立時選擇景點
 */
document.addEventListener('DOMContentLoaded', function() {
    // 初始化變數
    const spotSearchInput = document.getElementById('spotSearchInput');
    const spotSearchBtn = document.getElementById('spotSearchBtn');
    const spotPoolList = document.getElementById('spotPoolList');
    const spotSelectedList = document.getElementById('spotSelectedList');
    const spotPoolCount = document.getElementById('spotPoolCount');
    const spotSelectedCount = document.getElementById('spotSelectedCount');
    const selectedSpotsContainer = document.getElementById('selectedSpotsContainer');
    const itineraryForm = document.getElementById('itineraryForm');
    
    // 存儲已選擇的景點
    let selectedSpots = [];
    
    // 綁定搜尋按鈕點擊事件
    spotSearchBtn.addEventListener('click', searchSpots);
    
    // 綁定搜尋輸入框的回車事件
    spotSearchInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            e.preventDefault();
            searchSpots();
        }
    });
    
    // 初始加載一些景點（預設顯示上架狀態的景點）
    loadInitialSpots();
    
    // 綁定表單提交事件，確保將選擇的景點ID添加到表單中
    if (itineraryForm) {
        itineraryForm.addEventListener('submit', function(e) {
            // 清空之前的隱藏輸入
            selectedSpotsContainer.innerHTML = '';
            
            // 如果有選擇景點，則添加到表單中
            if (selectedSpots.length > 0) {
                selectedSpots.forEach((spot, index) => {
                    const hiddenInput = document.createElement('input');
                    hiddenInput.type = 'hidden';
                    hiddenInput.name = `spotIds[${index}]`;
                    hiddenInput.value = spot.spotId;
                    selectedSpotsContainer.appendChild(hiddenInput);
                });
            }
        });
    }
    
    /**
     * 初始加載一些景點
     */
    function loadInitialSpots() {
        // 顯示加載中
        spotPoolList.innerHTML = '<div class="spot-empty"><div class="material-icons spot-empty-icon">hourglass_empty</div><div class="spot-empty-text">載入中...</div></div>';
        
        // 發送AJAX請求獲取上架狀態的景點
        fetch('/api/spot-selector/active?limit=10')
            .then(response => response.json())
            .then(data => {
                if (data && data.length > 0) {
                    renderSpotPool(data);
                } else {
                    spotPoolList.innerHTML = '<div class="spot-empty"><div class="material-icons spot-empty-icon">info</div><div class="spot-empty-text">沒有找到上架的景點</div></div>';
                }
            })
            .catch(error => {
                console.error('載入景點失敗:', error);
                spotPoolList.innerHTML = '<div class="spot-empty"><div class="material-icons spot-empty-icon">error</div><div class="spot-empty-text">載入景點失敗，請重試</div></div>';
            });
    }
    
    /**
     * 搜尋景點
     */
    function searchSpots() {
        const searchTerm = spotSearchInput.value.trim();
        
        if (!searchTerm) {
            // 如果搜尋詞為空，則加載初始景點
            loadInitialSpots();
            return;
        }
        
        // 顯示加載中
        spotPoolList.innerHTML = '<div class="spot-empty"><div class="material-icons spot-empty-icon">hourglass_empty</div><div class="spot-empty-text">搜尋中...</div></div>';
        
        // 發送AJAX請求搜尋景點
        fetch(`/api/spot-selector/search?keyword=${encodeURIComponent(searchTerm)}`)
            .then(response => response.json())
            .then(data => {
                if (data && data.length > 0) {
                    renderSpotPool(data);
                } else {
                    spotPoolList.innerHTML = '<div class="spot-empty"><div class="material-icons spot-empty-icon">search_off</div><div class="spot-empty-text">沒有找到符合的景點</div></div>';
                }
            })
            .catch(error => {
                console.error('搜尋景點失敗:', error);
                spotPoolList.innerHTML = '<div class="spot-empty"><div class="material-icons spot-empty-icon">error</div><div class="spot-empty-text">搜尋失敗，請重試</div></div>';
            });
    }
    
    /**
     * 渲染景點池
     */
    function renderSpotPool(spots) {
        // 過濾掉已選擇的景點
        const filteredSpots = spots.filter(spot => !selectedSpots.some(selected => selected.spotId === spot.spotId));
        
        // 更新計數
        spotPoolCount.textContent = filteredSpots.length;
        
        // 如果沒有景點，顯示空狀態
        if (filteredSpots.length === 0) {
            spotPoolList.innerHTML = '<div class="spot-empty"><div class="material-icons spot-empty-icon">search_off</div><div class="spot-empty-text">沒有找到符合的景點</div></div>';
            return;
        }
        
        // 清空列表
        spotPoolList.innerHTML = '';
        
        // 渲染景點卡片
        filteredSpots.forEach(spot => {
            const spotCard = createSpotCard(spot, false);
            spotPoolList.appendChild(spotCard);
        });
    }
    
    /**
     * 渲染已選擇的景點列表
     */
    function renderSelectedSpots() {
        // 更新計數
        spotSelectedCount.textContent = selectedSpots.length;
        
        // 如果沒有選擇景點，顯示空狀態
        if (selectedSpots.length === 0) {
            spotSelectedList.innerHTML = '<div class="spot-empty"><div class="material-icons spot-empty-icon">playlist_add</div><div class="spot-empty-text">尚未選擇任何景點</div></div>';
            return;
        }
        
        // 清空列表
        spotSelectedList.innerHTML = '';
        
        // 渲染已選擇的景點卡片
        selectedSpots.forEach((spot, index) => {
            const spotCard = createSpotCard(spot, true, index + 1);
            spotSelectedList.appendChild(spotCard);
        });
        
        // 初始化拖拽排序
        initDragAndDrop();
    }
    
    /**
     * 創建景點卡片
     */
    function createSpotCard(spot, isSelected, number = null) {
        const spotCard = document.createElement('div');
        spotCard.className = 'spot-card';
        spotCard.dataset.spotId = spot.spotId;
        
        const cardContent = document.createElement('div');
        cardContent.className = 'spot-card-content';
        
        // 如果是已選擇的景點，添加序號和拖拽手柄
        if (isSelected) {
            // 添加序號
            const spotNumber = document.createElement('span');
            spotNumber.className = 'spot-card-number';
            spotNumber.textContent = number;
            cardContent.appendChild(spotNumber);
            
            // 添加拖拽手柄
            const dragHandle = document.createElement('span');
            dragHandle.className = 'material-icons spot-drag-handle';
            dragHandle.textContent = 'drag_indicator';
            cardContent.appendChild(dragHandle);
        }
        
        // 景點圖標
        const spotIcon = document.createElement('div');
        spotIcon.className = 'spot-card-icon';
        const icon = document.createElement('span');
        icon.className = 'material-icons';
        icon.textContent = 'place';
        spotIcon.appendChild(icon);
        cardContent.appendChild(spotIcon);
        
        // 景點信息
        const spotInfo = document.createElement('div');
        spotInfo.className = 'spot-card-info';
        
        const spotName = document.createElement('div');
        spotName.className = 'spot-card-name';
        spotName.textContent = spot.spotName;
        spotInfo.appendChild(spotName);
        
        const spotLocation = document.createElement('div');
        spotLocation.className = 'spot-card-location';
        spotLocation.textContent = spot.spotLoc || '無地址信息';
        spotInfo.appendChild(spotLocation);
        
        cardContent.appendChild(spotInfo);
        
        // 操作按鈕
        const actionBtn = document.createElement('button');
        actionBtn.type = 'button';
        actionBtn.className = 'material-icons spot-card-action';
        
        if (isSelected) {
            // 移除按鈕
            actionBtn.textContent = 'remove_circle';
            actionBtn.title = '移除景點';
            actionBtn.onclick = function() {
                removeSpot(spot);
            };
        } else {
            // 添加按鈕
            actionBtn.textContent = 'add_circle';
            actionBtn.title = '添加景點';
            actionBtn.onclick = function() {
                addSpot(spot);
            };
        }
        
        cardContent.appendChild(actionBtn);
        spotCard.appendChild(cardContent);
        
        return spotCard;
    }
    
    /**
     * 添加景點到已選列表
     */
    function addSpot(spot) {
        // 檢查是否已經選擇
        if (selectedSpots.some(selected => selected.spotId === spot.spotId)) {
            return;
        }
        
        // 添加到已選列表
        selectedSpots.push(spot);
        
        // 重新渲染已選列表
        renderSelectedSpots();
        
        // 從景點池中移除該景點
        const spotCards = spotPoolList.querySelectorAll('.spot-card');
        spotCards.forEach(card => {
            if (parseInt(card.dataset.spotId) === spot.spotId) {
                card.remove();
            }
        });
        
        // 更新計數
        spotPoolCount.textContent = parseInt(spotPoolCount.textContent) - 1;
    }
    
    /**
     * 從已選列表中移除景點
     */
    function removeSpot(spot) {
        // 從已選列表中移除
        selectedSpots = selectedSpots.filter(selected => selected.spotId !== spot.spotId);
        
        // 重新渲染已選列表
        renderSelectedSpots();
        
        // 如果當前搜尋結果中有該景點，則添加回景點池
        const searchTerm = spotSearchInput.value.trim();
        if (searchTerm) {
            // 如果有搜尋詞，重新搜尋
            searchSpots();
        } else {
            // 否則重新加載初始景點
            loadInitialSpots();
        }
    }
    
    /**
     * 初始化拖拽排序功能
     */
    function initDragAndDrop() {
        const spotCards = spotSelectedList.querySelectorAll('.spot-card');
        
        spotCards.forEach(card => {
            card.setAttribute('draggable', true);
            
            card.addEventListener('dragstart', function() {
                this.classList.add('dragging');
            });
            
            card.addEventListener('dragend', function() {
                this.classList.remove('dragging');
                
                // 更新選擇的景點順序
                updateSpotOrder();
            });
        });
        
        spotSelectedList.addEventListener('dragover', function(e) {
            e.preventDefault();
            const draggingCard = document.querySelector('.dragging');
            if (!draggingCard) return;
            
            const afterElement = getDragAfterElement(this, e.clientY);
            if (afterElement) {
                this.insertBefore(draggingCard, afterElement);
            } else {
                this.appendChild(draggingCard);
            }
        });
    }
    
    /**
     * 獲取拖拽後的位置
     */
    function getDragAfterElement(container, y) {
        const cards = [...container.querySelectorAll('.spot-card:not(.dragging)')];
        
        return cards.reduce((closest, child) => {
            const box = child.getBoundingClientRect();
            const offset = y - box.top - box.height / 2;
            
            if (offset < 0 && offset > closest.offset) {
                return { offset: offset, element: child };
            } else {
                return closest;
            }
        }, { offset: Number.NEGATIVE_INFINITY }).element;
    }
    
    /**
     * 更新景點順序
     */
    function updateSpotOrder() {
        const spotCards = spotSelectedList.querySelectorAll('.spot-card');
        const newOrder = [];
        
        spotCards.forEach(card => {
            const spotId = parseInt(card.dataset.spotId);
            const spot = selectedSpots.find(s => s.spotId === spotId);
            if (spot) {
                newOrder.push(spot);
            }
        });
        
        // 更新選擇的景點順序
        selectedSpots = newOrder;
        
        // 重新渲染以更新序號
        renderSelectedSpots();
    }
}); 