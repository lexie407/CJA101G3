/**
 * 景點選擇器 - 用於後台行程建立時選擇景點
 */
document.addEventListener('DOMContentLoaded', () => {
    const spotSelectorContainer = document.querySelector('.spot-selector-section');
    if (!spotSelectorContainer) {
        console.log('Spot selector not found on this page.');
        return;
    }

    // DOM Elements
    const searchInput = document.getElementById('spotSearchInput');
    const spotPoolList = document.getElementById('spotPoolList');
    const selectedList = document.getElementById('spotSelectedList');
    const selectedCountSpan = document.getElementById('spotSelectedCount');
    const validationMessage = document.querySelector('.spot-validation-message');
    const itineraryForm = document.getElementById('itineraryForm');
    const initialSpotsDiv = document.getElementById('initial-selected-spots');
    
    // State
    let availableSpots = [];
    let selectedSpots = [];
    let searchTimeout;
    const MAX_SPOTS = 10;

    const api = {
        fetchByIds: (ids) => fetch(`/api/spot/public/list-by-ids?spotIds=${ids.join(',')}`).then(res => res.json()),
        fetchAllPublic: () => fetch('/api/spot/public/list').then(res => res.json()),
        searchPublic: (keyword) => fetch(`/api/spot/public/search?keyword=${encodeURIComponent(keyword)}`).then(res => res.json()),
    };

    const render = {
        spotCard: (spot, isSelected) => {
            const card = document.createElement('div');
            card.className = 'spot-card';
            card.dataset.spotId = spot.spotId;
            card.innerHTML = `
                <div class="spot-card-content">
                    <div class="spot-card-icon">
                        <i class="material-icons">place</i>
                    </div>
                    <div class="spot-card-info">
                        <div class="spot-card-name">${spot.spotName}</div>
                        <div class="spot-card-location">${spot.spotLoc || '位置資訊未提供'}</div>
                    </div>
                    <button type="button" class="spot-card-action" data-action="${isSelected ? 'remove' : 'add'}">
                        <i class="material-icons">${isSelected ? 'remove' : 'add'}</i>
                    </button>
                </div>
            `;
            
            const actionBtn = card.querySelector('.spot-card-action');
            actionBtn.addEventListener('click', (e) => {
                e.stopPropagation();
                if (actionBtn.dataset.action === 'add') {
                    controller.selectSpot(spot);
                } else {
                    controller.deselectSpot(spot.spotId);
                }
            });
            
            return card;
        },
        selectedCard: (spot, index) => {
            const card = document.createElement('div');
            card.className = 'spot-card';
            card.dataset.spotId = spot.spotId;
            card.innerHTML = `
                <div class="spot-card-content">
                    <div class="spot-card-number">${index + 1}</div>
                    <div class="spot-card-icon">
                        <i class="material-icons">place</i>
                    </div>
                    <div class="spot-card-info">
                        <div class="spot-card-name">${spot.spotName}</div>
                        <div class="spot-card-location">${spot.spotLoc || '位置資訊未提供'}</div>
                    </div>
                    <button type="button" class="spot-card-action" data-action="remove">
                        <i class="material-icons">close</i>
                    </button>
                </div>
            `;
            
            const removeBtn = card.querySelector('.spot-card-action');
            removeBtn.addEventListener('click', (e) => {
                e.stopPropagation();
                controller.deselectSpot(spot.spotId);
            });
            
            return card;
        },
        updateAll: () => {
            // Clear empty messages
            const poolEmpty = spotPoolList.querySelector('.spot-empty');
            const selectedEmpty = selectedList.querySelector('.spot-empty');
            
            // Filter out selected spots from available spots for display
            const unselectedSpots = availableSpots.filter(spot => 
                !selectedSpots.some(s => s.spotId === spot.spotId)
            );
            
            // Render available spots (only unselected ones)
            spotPoolList.innerHTML = '';
            if (unselectedSpots.length === 0) {
                if (availableSpots.length === 0) {
                    spotPoolList.innerHTML = `
                        <div class="spot-empty">
                            <div class="material-icons spot-empty-icon">search</div>
                            <div class="spot-empty-text">請搜尋景點以顯示結果</div>
                        </div>
                    `;
                } else {
                    spotPoolList.innerHTML = `
                        <div class="spot-empty">
                            <div class="material-icons spot-empty-icon">check_circle</div>
                            <div class="spot-empty-text">所有景點都已選擇</div>
                            <div class="spot-empty-hint">可以搜尋更多景點或移除已選景點</div>
                        </div>
                    `;
                }
            } else {
                unselectedSpots.forEach(spot => {
                    const card = render.spotCard(spot, false);
                    spotPoolList.appendChild(card);
                });
            }
            
            // Update pool count (show unselected spots count)
            document.getElementById('spotPoolCount').textContent = unselectedSpots.length;
            
            // Render selected spots
            selectedList.innerHTML = '';
        if (selectedSpots.length === 0) {
                selectedList.innerHTML = `
                    <div class="spot-empty">
                <div class="material-icons spot-empty-icon">playlist_add</div>
                <div class="spot-empty-text">尚未選擇任何景點</div>
                <div class="spot-empty-hint">點擊左側景點的加號按鈕添加</div>
                    </div>
            `;
        } else {
            selectedSpots.forEach((spot, index) => {
                    const card = render.selectedCard(spot, index);
                    selectedList.appendChild(card);
                });
            }
            
            // Update selected count
            selectedCountSpan.textContent = selectedSpots.length;
            
            // Validate
            controller.validate();
        },
        filterAvailable: () => {
            const query = searchInput.value.toLowerCase();
            document.querySelectorAll('#spotPoolList .spot-card').forEach(card => {
                const name = card.querySelector('.spot-card-name').textContent.toLowerCase();
                const location = card.querySelector('.spot-card-location').textContent.toLowerCase();
                const matches = name.includes(query) || location.includes(query);
                card.style.display = matches ? '' : 'none';
            });
        },
        searchSpots: async (keyword) => {
            if (!keyword.trim()) {
                try {
                    const response = await api.fetchAllPublic();
                    console.log("載入所有景點 API 回應:", response);
                    
                    // 處理不同的API回應格式
                    let spots = [];
                    if (response.success && response.data) {
                        spots = response.data;
                    } else if (Array.isArray(response)) {
                        spots = response;
                    } else if (response.data && Array.isArray(response.data)) {
                        spots = response.data;
                    }
                    
                    if (spots.length > 0) {
                        availableSpots = spots;
                        render.updateAll();
                    }
                } catch (error) {
                    console.error('Error fetching all public spots:', error);
                }
                return;
            }
        
            try {
                const response = await api.searchPublic(keyword);
                console.log("搜尋 API 回應:", response);
                
                // 處理不同的API回應格式
                let spots = [];
                if (response.success && response.data) {
                    spots = response.data;
                } else if (Array.isArray(response)) {
                    spots = response;
                } else if (response.data && Array.isArray(response.data)) {
                    spots = response.data;
                }
                
                if (spots.length >= 0) { // 允許空結果
                    availableSpots = spots;
                    render.updateAll();
                } else {
                    spotPoolList.innerHTML = `
                        <div class="spot-empty">
                            <div class="material-icons spot-empty-icon">error</div>
                            <div class="spot-empty-text">搜尋失敗：${response.message || '請稍後再試'}</div>
                        </div>
                    `;
                }
            } catch (error) {
                console.error('搜尋錯誤:', error);
                spotPoolList.innerHTML = `
                    <div class="spot-empty">
                        <div class="material-icons spot-empty-icon">error</div>
                        <div class="spot-empty-text">載入失敗，請檢查網路連線</div>
                    </div>
                `;
            }
        }
    };

    const controller = {
        selectSpot: (spot) => {
            if (selectedSpots.length >= MAX_SPOTS) {
                alert(`最多只能選擇 ${MAX_SPOTS} 個景點`);
            return;
        }
            if (!selectedSpots.some(s => s.spotId === spot.spotId)) {
        selectedSpots.push(spot);
                render.updateAll();
            }
        },
        deselectSpot: (spotId) => {
            selectedSpots = selectedSpots.filter(s => s.spotId !== spotId);
            render.updateAll();
        },
        validate: () => {
            if (selectedSpots.length > MAX_SPOTS) {
                if (validationMessage) {
                    validationMessage.textContent = `超過 ${MAX_SPOTS} 個景點上限！`;
                    validationMessage.style.display = 'block';
                }
                return false;
            }
            if (validationMessage) {
                validationMessage.style.display = 'none';
            }
            return true;
        },
        prepareForSubmit: (event) => {
            console.log("表單提交前準備景點數據...");
            console.log("已選景點數量:", selectedSpots.length);
            
            // Use the dedicated container for hidden inputs
            const container = document.getElementById('selectedSpotsContainer');
            if (container) {
                // Clear existing hidden inputs
                container.innerHTML = '';
                
                // Add current selected spots as hidden inputs
                selectedSpots.forEach(spot => {
                    const input = document.createElement('input');
                    input.type = 'hidden';
                    input.name = 'spotIds';
                    input.value = spot.spotId;
                    container.appendChild(input);
                    console.log("添加景點ID:", spot.spotId, spot.spotName);
                });
                
                console.log("景點數據準備完成，表單中的景點數量:", container.querySelectorAll('input[name="spotIds"]').length);
        } else {
                // Fallback to old method if container not found
                itineraryForm.querySelectorAll('input[name="spotIds"]').forEach(input => input.remove());
                
                selectedSpots.forEach(spot => {
                    const input = document.createElement('input');
                    input.type = 'hidden';
                    input.name = 'spotIds';
                    input.value = spot.spotId;
                    itineraryForm.appendChild(input);
                    console.log("添加景點ID (舊方法):", spot.spotId, spot.spotName);
                });
                
                console.log("景點數據準備完成 (舊方法)，表單中的景點數量:", itineraryForm.querySelectorAll('input[name="spotIds"]').length);
            }
        }
    };

    const init = async () => {
        console.log("🚀 景點選擇器初始化開始...");
        
        // 載入初始景點數據
        try {
            // 載入所有公開景點作為可選池
            const response = await api.fetchAllPublic();
            console.log("API 回應:", response);
            
            // 處理不同的API回應格式
            let spots = [];
            if (response.success && response.data) {
                spots = response.data;
            } else if (Array.isArray(response)) {
                spots = response;
            } else if (response.data && Array.isArray(response.data)) {
                spots = response.data;
            }
            
            if (spots.length > 0) {
                availableSpots = spots;
                console.log("✅ 已載入", availableSpots.length, "個可選景點");
                
                // 如果是編輯頁面，載入已選景點
                if (initialSpotsDiv && initialSpotsDiv.dataset.spotIds) {
                    const initialIds = initialSpotsDiv.dataset.spotIds.split(',').map(id => parseInt(id, 10));
                    if (initialIds.length > 0) {
                        console.log("📝 編輯頁面：發現已選景點IDs:", initialIds);
                        
                        // 從可選池中找出已選景點
                        initialIds.forEach(id => {
                            const spot = availableSpots.find(s => s.spotId === id);
                            if (spot) {
                                selectedSpots.push(spot);
                                console.log("✅ 已添加已選景點:", spot.spotName);
                            } else {
                                console.warn("⚠️ 找不到景點ID:", id);
                            }
                        });
                    }
                }
                
                // 立即渲染所有景點
                render.updateAll();
                console.log("🎉 景點選擇器初始化完成");
        } else {
                console.error("❌ 無法載入景點數據，API回應:", response);
                spotPoolList.innerHTML = `
                    <div class="spot-empty">
                        <div class="material-icons spot-empty-icon">error</div>
                        <div class="spot-empty-text">無法載入景點數據</div>
                        <div class="spot-empty-hint">請檢查網路連線或聯絡管理員</div>
                        <button type="button" onclick="location.reload()" style="margin-top: 10px; padding: 8px 16px; background: #007bff; color: white; border: none; border-radius: 4px; cursor: pointer;">重新載入</button>
                    </div>
                `;
            }
        } catch (error) {
            console.error('❌ 初始化景點選擇器時發生錯誤:', error);
            spotPoolList.innerHTML = `
                <div class="spot-empty">
                    <div class="material-icons spot-empty-icon">error</div>
                    <div class="spot-empty-text">載入失敗</div>
                    <div class="spot-empty-hint">請重新整理頁面或聯絡管理員</div>
                    <button type="button" onclick="location.reload()" style="margin-top: 10px; padding: 8px 16px; background: #007bff; color: white; border: none; border-radius: 4px; cursor: pointer;">重新載入</button>
                </div>
            `;
        }
    };

    // Event Listeners
    searchInput.addEventListener('input', () => {
        clearTimeout(searchTimeout);
        searchTimeout = setTimeout(() => {
            render.filterAvailable();
        }, 300);
    });

    document.getElementById('spotSearchBtn').addEventListener('click', () => {
        const keyword = searchInput.value.trim();
        if (keyword) {
            render.searchSpots(keyword);
        } else {
            // 如果沒有關鍵字，載入所有景點
            render.searchSpots('');
        }
    });
    
    searchInput.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            const keyword = searchInput.value.trim();
            if (keyword) {
                render.searchSpots(keyword);
            } else {
                render.searchSpots('');
            }
        }
    });

    // 表單提交前處理景點數據
    if (itineraryForm) {
        itineraryForm.addEventListener('submit', function(event) {
            // 阻止表單默認提交
            event.preventDefault();
            
            // 準備景點數據
            controller.prepareForSubmit();
            
            // 檢查是否有選擇景點
            const spotInputs = document.querySelectorAll('input[name="spotIds"]');
            console.log("表單提交時的景點數量:", spotInputs.length);
            
            // 繼續提交表單
            this.submit();
        });
    }

    // Start
    init();
}); 