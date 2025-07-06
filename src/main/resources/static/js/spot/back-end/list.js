console.log('=== list.js 開始載入 ===');

// 設定全域變數（從Thymeleaf傳遞）
function initializeGlobalData() {
    // 這些變數會在HTML中的inline script設定
    if (typeof window.allSpotsData !== 'undefined') {
        console.log('=== 全域資料詳細檢查 ===');
        console.log('從後端獲取的完整資料:', window.allSpotsData);
        console.log('資料筆數:', window.allSpotsData.length);
        console.log('資料類型:', typeof window.allSpotsData);
        console.log('是否為陣列:', Array.isArray(window.allSpotsData));
        
        if (window.allSpotsData.length > 0) {
            console.log('第一筆資料:', window.allSpotsData[0]);
            console.log('最後一筆資料:', window.allSpotsData[window.allSpotsData.length - 1]);
        }
        
        // 檢查是否有重複的ID
        const ids = window.allSpotsData.map(spot => spot.spotId);
        const uniqueIds = [...new Set(ids)];
        console.log('唯一ID數量:', uniqueIds.length, '總資料數量:', window.allSpotsData.length);
        if (ids.length !== uniqueIds.length) {
            console.warn('發現重複的景點ID!');
        }
        console.log('===========================');
    } else {
        console.error('window.allSpotsData 未定義!');
    }
    
    if (typeof window.allRegions !== 'undefined') {
        console.log('從後端獲取的所有地區:', window.allRegions);
        console.log('地區數量:', window.allRegions.length);
    } else {
        console.error('window.allRegions 未定義!');
    }
}

// 批量操作函數（從HTML中的onclick移過來）
function batchAction(action) {
    const form = document.getElementById('batchForm');
    const checkedBoxes = document.querySelectorAll('.spot-checkbox:checked');
    
    if (checkedBoxes.length === 0) {
        alert('請至少選擇一個項目');
        return;
    }
    
    let confirmMessage = `確定要對 ${checkedBoxes.length} 個景點執行「批量${action === 'delete' ? '刪除' : (action === 'activate' ? '上架' : '下架')}」嗎？`;
    if (action === 'delete') {
        confirmMessage += '\n此操作無法復原！';
    }
    
    if (confirm(confirmMessage)) {
        // 添加 CSRF token
        const csrfToken = document.querySelector('meta[name="_csrf"]');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]');
        
        if (csrfToken && csrfHeader) {
            const input = document.createElement('input');
            input.type = 'hidden';
            input.name = '_csrf';
            input.value = csrfToken.content;
            form.appendChild(input);
        }
        
        form.action = `/admin/spot/batch-${action}`;
        form.method = 'POST';
        form.submit();
    }
}

// 刪除確認函數（從HTML中的onsubmit移過來）
function confirmDelete(spotName) {
    return confirm(`確定要刪除這個景點「${spotName}」嗎？此操作無法復原！`);
}

// 圖片錯誤處理函數（從HTML中的onerror移過來）
function handleImageError(img) {
    console.log('圖片載入失敗:', img.src);
    img.classList.add('hidden');
    const placeholder = img.parentNode.querySelector('.spot-image-placeholder');
    if (placeholder) {
        placeholder.classList.remove('spot-image-placeholder-none');
        placeholder.classList.add('spot-image-placeholder-flex');
    }
}

// 圖片載入成功處理
function handleImageLoad(img) {
    console.log('圖片載入成功:', img.src);
    img.classList.remove('hidden');
    const placeholder = img.parentNode.querySelector('.spot-image-placeholder');
    if (placeholder) {
        placeholder.classList.remove('spot-image-placeholder-flex');
        placeholder.classList.add('spot-image-placeholder-none');
    }
}

// 暴露到全域供HTML使用
window.batchAction = batchAction;
window.confirmDelete = confirmDelete;
window.handleImageError = handleImageError;
window.handleImageLoad = handleImageLoad;

// 輔助函數
function getStatusText(status) {
    switch (status) {
        case 0: return '待審核';
        case 1: return '上架';
        case 2: return '退回';
        case 3: return '下架';
        default: return '未知';
    }
}

function getStatusClass(status) {
    switch (status) {
        case 0: return 'status-badge status-pending';
        case 1: return 'status-badge status-active';
        case 2: return 'status-badge status-rejected';
        case 3: return 'status-badge status-inactive';
        default: return 'status-badge';
    }
}

function formatDate(dateString) {
    if (!dateString) return '';
    // 處理 LocalDateTime 格式 (e.g., "2025-01-26T10:30:00")
    const date = new Date(dateString);
    return date.toISOString().split('T')[0]; // 只取日期部分 YYYY-MM-DD
}

function generateActionButtons(spotId) {
    // 取得 CSRF token
    const csrfToken = document.querySelector('meta[name="_csrf"]');
    const csrfParam = document.querySelector('meta[name="_csrf_header"]');
    
    let csrfInput = '';
    if (csrfToken && csrfParam) {
        csrfInput = `<input type="hidden" name="_csrf" value="${csrfToken.content}"/>`;
    }
    
    return `
        <div class="action-buttons">
            <a href="/admin/spot/detail/${spotId}" class="action-btn btn-view" title="查看">👁️</a>
            <a href="/admin/spot/edit/${spotId}" class="action-btn btn-edit" title="編輯">✏️</a>
            <form action="/admin/spot/delete/${spotId}" method="post" style="display: inline;" onsubmit="return confirm('確定要刪除這個景點嗎？此操作無法復原！')">
                ${csrfInput}
                <button type="submit" class="action-btn btn-delete" title="刪除">🗑️</button>
            </form>
        </div>
    `;
}

function truncateText(text, maxLength) {
    if (!text) return '';
    if (text.length <= maxLength) return text;
    return text.substring(0, maxLength) + '...';
}

function extractRegionFromLocation(location) {
    if (!location) return '';
    
    // 清理地址文字（移除多餘空格）
    const cleanLocation = location.replace(/\s+/g, '');
    
    // 台灣縣市列表，按照長度排序（避免匹配衝突，如台北市vs台北）
    const regions = [
        '台北市', '新北市', '桃園市', '台中市', '台南市', '高雄市',
        '基隆市', '新竹市', '嘉義市',
        '新竹縣', '苗栗縣', '彰化縣', '南投縣', '雲林縣', '嘉義縣', 
        '屏東縣', '宜蘭縣', '花蓮縣', '台東縣', '澎湖縣', '金門縣', '連江縣'
    ];
    
    // 先檢查是否以任何縣市開頭
    for (const region of regions) {
        if (cleanLocation.startsWith(region)) {
            return region;
        }
    }
    
    // 如果沒有找到，可能是地址格式問題，嘗試其他匹配方式
    for (const region of regions) {
        if (cleanLocation.includes(region)) {
            return region;
        }
    }
    
    return '其他';
}

// 全域變數
let isAscending = false; // 預設為降序（最新的在前）
let currentSortField = 'spotId'; // 預設排序欄位
let allSpotData = []; // 儲存所有景點資料
let filteredData = []; // 儲存篩選後的資料
let currentPage = 0;
let pageSize = 10;
let currentFilters = {
    status: null,
    region: null,
    keyword: null
};

// 檢查 DOM 是否已載入
if (document.readyState === 'loading') {
    console.log('DOM 正在載入中...');
} else {
    console.log('DOM 已經載入完成');
}

document.addEventListener('DOMContentLoaded', function() {
    console.log('=== DOMContentLoaded 事件觸發 ===');
    
    // 初始化全域變數
    initializeGlobalData();
    
    // 檢查重要元素是否存在
    const sortBtn = document.getElementById('sortBtn');
    const selectAll = document.getElementById('selectAll');
    const batchBtn = document.getElementById('batchBtn');
    const searchInput = document.getElementById('searchInput');
    
    console.log('檢查 DOM 元素:');
    console.log('- sortBtn:', sortBtn);
    console.log('- selectAll:', selectAll);
    console.log('- batchBtn:', batchBtn);
    console.log('- searchInput:', searchInput);
    
    // 檢查 filter dropdowns
    const filterDropdowns = document.querySelectorAll('.filter-dropdown');
    console.log('- filterDropdowns 數量:', filterDropdowns.length);
    
    // 檢查 checkboxes
    const checkboxes = document.querySelectorAll('tbody input[type="checkbox"]');
    console.log('- tbody checkboxes 數量:', checkboxes.length);
    
    // 初始化資料
    initializeData();
    
    // 初始化篩選按鈕狀態
    initializeFilterStates();

    // 綁定分頁按鈕事件
    bindPaginationEvents();
    
    // 綁定圖片事件
    bindImageEvents();
    
    // 綁定批量操作按鈕事件
    bindBatchActions();
    
    // 綁定表單提交事件
    bindFormSubmissions();

    // 排序功能 - 改為純前端操作
    if (sortBtn) {
        sortBtn.addEventListener('click', function() {
            console.log('排序按鈕被點擊!');
            
            // 添加點擊動畫
            this.classList.add('sorting');
            setTimeout(() => {
                this.classList.remove('sorting');
            }, 600);
            
            isAscending = !isAscending;
            const sortIcon = this.querySelector('.sort-icon');
            
            // 更新圖標和文字
            if (isAscending) {
                sortIcon.textContent = '↑';
                this.title = '目前：升序排列（點擊切換為降序）';
            } else {
                sortIcon.textContent = '↓';
                this.title = '目前：降序排列（點擊切換為升序）';
            }
            
            // 執行前端排序
            applySorting();
            currentPage = 0; // 重置到第一頁
            
            // 清除所有勾選狀態
            clearAllSelections();
            
            renderTable();
        });
    }
    
    // 搜尋功能 - 改為即時搜尋
    if (searchInput) {
        let searchTimeout;
        searchInput.addEventListener('input', function() {
            clearTimeout(searchTimeout);
            searchTimeout = setTimeout(() => {
                currentFilters.keyword = this.value.trim() || null;
                console.log('搜尋關鍵字:', currentFilters.keyword);
                applyFiltersAndSort();
                currentPage = 0;
                
                // 清除所有勾選狀態
                clearAllSelections();
                
                // 如果目前是顯示所有模式，重新調整 pageSize
                const pageSizeSelect = document.getElementById('pageSizeSelect');
                if (pageSizeSelect && pageSizeSelect.value === '-1') {
                    pageSize = filteredData.length;
                }
                
                renderTable();
            }, 300); // 300ms 防抖動
        });
    }
    
    // 下拉選單控制
    filterDropdowns.forEach(dropdown => {
        const btn = dropdown.querySelector('.filter-btn');
        const menu = dropdown.querySelector('.dropdown-menu');
        const radioButtons = dropdown.querySelectorAll('input[type="radio"]');
        
        btn.addEventListener('click', function(e) {
            e.stopPropagation();
            filterDropdowns.forEach(otherDropdown => {
                if (otherDropdown !== dropdown) {
                    otherDropdown.querySelector('.dropdown-menu').classList.remove('show');
                    otherDropdown.querySelector('.filter-btn').classList.remove('active');
                }
            });
            menu.classList.toggle('show');
            btn.classList.toggle('active');
        });
        
        // 監聽 radio 按鈕變更 - 改為純前端操作
        radioButtons.forEach(radio => {
            radio.addEventListener('change', function() {
                console.log('篩選條件改變:', this.name, '=', this.value);
                updateButtonState(dropdown);
                
                // 更新篩選條件
                const filterType = dropdown.dataset.filter;
                if (filterType === 'status') {
                    currentFilters.status = this.value === 'all' ? null : parseInt(this.value);
                } else if (filterType === 'region') {
                    currentFilters.region = this.value === 'all' ? null : this.value;
                }
                
                console.log('目前篩選條件:', currentFilters);
                
                // 立即套用篩選和排序
                applyFiltersAndSort();
                currentPage = 0; // 重置到第一頁
                
                // 清除所有勾選狀態
                clearAllSelections();
                
                // 如果目前是顯示所有模式，重新調整 pageSize
                const pageSizeSelect = document.getElementById('pageSizeSelect');
                if (pageSizeSelect && pageSizeSelect.value === '-1') {
                    pageSize = filteredData.length;
                }
                
                renderTable();
                
                setTimeout(() => {
                    menu.classList.remove('show');
                    btn.classList.remove('active');
                }, 150);
            });
        });
        
        // 點擊選項時觸發 radio
        dropdown.querySelectorAll('.option-item').forEach(item => {
            item.addEventListener('click', function() {
                const radio = item.querySelector('input[type="radio"]');
                if (radio && !radio.checked) {
                    radio.checked = true;
                    radio.dispatchEvent(new Event('change'));
                }
            });
        });
    });
    
    // 分頁大小切換 - 改為純前端操作
    const pageSizeSelect = document.getElementById('pageSizeSelect');
    if (pageSizeSelect) {
        pageSizeSelect.addEventListener('change', () => {
            const selectedValue = parseInt(pageSizeSelect.value);
            if (selectedValue === -1) {
                // 顯示所有：設定 pageSize 為資料總數
                pageSize = filteredData.length > 0 ? filteredData.length : allSpotData.length;
                console.log('切換為顯示所有，pageSize設為:', pageSize);
            } else {
                pageSize = selectedValue;
                console.log('分頁大小改變為:', pageSize);
            }
            currentPage = 0; // 重置到第一頁
            
            // 清除所有勾選狀態
            clearAllSelections();
            
            renderTable();
        });
    }
    
    // 清除篩選按鈕 - 改為純前端操作
    const clearBtn = document.querySelector('.clear-btn');
    if (clearBtn) {
        clearBtn.addEventListener('click', function() {
            console.log('清除所有篩選');
            clearAllFilters();
        });
    }
    
    // 點擊外部關閉下拉選單
    document.addEventListener('click', function() {
        filterDropdowns.forEach(dropdown => {
            dropdown.querySelector('.dropdown-menu').classList.remove('show');
            dropdown.querySelector('.filter-btn').classList.remove('active');
        });
        // 同時關閉批量操作選單
        const batchMenu = document.getElementById('batchMenu');
        if (batchMenu) {
            batchMenu.classList.remove('show');
        }
    });
    
    // 防止下拉選單內部點擊關閉
    document.querySelectorAll('.dropdown-menu').forEach(menu => {
        menu.addEventListener('click', function(e) {
            e.stopPropagation();
        });
    });
    
    // 全選/取消全選
    if (selectAll) {
        selectAll.addEventListener('change', function() {
            const checkboxes = document.querySelectorAll('tbody input[type="checkbox"]');
            const isChecking = this.checked;
            
            checkboxes.forEach((checkbox, index) => {
                // 為每個 checkbox 添加漸進式延遲動畫
                setTimeout(() => {
                    checkbox.checked = isChecking;
                    const row = checkbox.closest('tr');
                    
                    if (isChecking) {
                        row.style.animationDelay = `${index * 50}ms`;
                        row.classList.add('selected');
                    } else {
                        row.style.animationDelay = `${index * 30}ms`;
                        row.classList.remove('selected');
                    }
                }, index * (isChecking ? 50 : 30));
            });
            
            // 延遲更新選中資訊，等動畫完成
            setTimeout(() => {
                updateSelectedInfo();
            }, checkboxes.length * (isChecking ? 50 : 30) + 100);
        });
    }
    
    // 圖片載入錯誤處理已移到 bindImageEvents() 函數
    
    // 初始化篩選按鈕狀態
    filterDropdowns.forEach(dropdown => {
        updateButtonState(dropdown);
    });
});

// 使用 API 獲取全部景點資料
async function initializeData() {
    console.log('=== 開始初始化資料 ===');
    
    try {
        // 1. 先嘗試從API獲取全部資料
        console.log('呼叫API獲取全部景點資料...');
        const response = await fetch('/admin/spot/api/all');
        
        if (response.ok) {
            const apiResult = await response.json();
            console.log('API 回應狀態:', apiResult.success);
            
            if (apiResult.success && apiResult.data) {
                allSpotData = apiResult.data;
                console.log('✅ 成功從API獲取資料，共', allSpotData.length, '筆景點');
                
                // 提取所有地區資訊 - 優先使用資料庫的region欄位
                const allRegions = [...new Set(allSpotData
                    .map(spot => {
                        // 先嘗試使用資料庫的region欄位，如果沒有再從地址提取
                        let region = spot.region;
                        if (!region || region.trim() === '') {
                            region = extractRegionFromLocation(spot.spotLoc);
                        }
                        // 清理地區名稱（移除多餘空格）
                        return region ? region.replace(/\s+/g, '') : null;
                    })
                    .filter(region => region && region !== '' && region !== '其他')
                )].sort();
                
                console.log('從API資料提取到的地區:', allRegions);
                updateRegionOptions(allRegions);
                
                // 初始化篩選和顯示
                applyFiltersAndSort();
                renderTable();
                updateStatsInfo();
                console.log('✅ API資料初始化完成');
                return;
            }
        }
        
        console.log('⚠️ API獲取失敗，回退到頁面資料...');
        
    } catch (error) {
        console.error('❌ API呼叫出錯:', error);
        console.log('回退到頁面資料...');
    }
    
    // 2. API失敗時回退到原方法
    if (typeof window.allSpotsData !== 'undefined' && window.allSpotsData.length > 0) {
        console.log('使用頁面傳遞的資料，共', window.allSpotsData.length, '筆');
        allSpotData = window.allSpotsData;
        
        if (typeof window.allRegions !== 'undefined') {
            updateRegionOptions(window.allRegions);
        }
    } else {
        console.log('⚠️ 頁面資料也不可用，從表格提取資料');
        extractDataFromTable();
    }
    
    // 顯示資料詳情
    if (allSpotData.length > 0) {
        console.log('=== 所有景點地區資訊 ===');
        allSpotData.forEach((spot, index) => {
            const region = extractRegionFromLocation(spot.spotLoc || '');
            console.log(`${index + 1}. 景點: ${spot.spotName} (ID: ${spot.spotId})`);
            console.log(`   位置: "${spot.spotLoc}"`);
            console.log(`   地區: "${region}"`);
            console.log(`   狀態: ${spot.spotStatus} (${spot.spotStatus === 1 ? '上架' : '下架'})`);
            console.log('   ---');
        });
        
        const regionCounts = {};
        allSpotData.forEach(spot => {
            // 優先使用資料庫的region欄位，如果沒有再從地址提取
            let region = spot.region;
            if (!region || region.trim() === '') {
                region = extractRegionFromLocation(spot.spotLoc || '');
            }
            // 清理地區名稱（移除多餘空格）
            const cleanRegion = region ? region.replace(/\s+/g, '') : '';
            const regionKey = cleanRegion || '未知';
            regionCounts[regionKey] = (regionCounts[regionKey] || 0) + 1;
        });
        console.log('地區分布統計:', regionCounts);
        
        // 提取地區選項並更新下拉選單
        const allRegions = Object.keys(regionCounts).filter(region => region !== '未知').sort();
        updateRegionOptions(allRegions);
    }
    
    console.log('初始化資料完成，共', allSpotData.length, '筆景點');
    
    // 執行初始篩選和排序
    applyFiltersAndSort();
    renderTable();
    updateStatsInfo();
}

// 從表格DOM提取資料（備用方案）
function extractDataFromTable() {
    console.log('從表格DOM提取景點資料...');
    const tbody = document.querySelector('table tbody');
    if (!tbody) {
        console.error('找不到表格 tbody 元素');
        return;
    }
    
    const rows = tbody.querySelectorAll('tr');
    allSpotData = Array.from(rows).map(row => extractSpotDataFromRow(row)).filter(Boolean);
    
    console.log('從表格提取到', allSpotData.length, '筆資料');
}

// 從表格行提取景點資料
function extractSpotDataFromRow(row) {
    try {
        const cells = row.querySelectorAll('td');
        if (cells.length < 7) return null;
        
        const checkbox = cells[0].querySelector('input[type="checkbox"]');
        const spotId = parseInt(checkbox ? checkbox.value : 0);
        
        const idElement = cells[1].querySelector('.spot-id');
        const displayId = idElement ? idElement.textContent.replace('#', '') : spotId;
        
        const nameElement = cells[2].querySelector('.spot-name');
        const descElement = cells[2].querySelector('.spot-desc');
        const imageElement = cells[2].querySelector('.spot-image');
        
        const locationElement = cells[3].querySelector('.spot-location');
        const statusElement = cells[4].querySelector('.status-badge');
        const dateElement = cells[5].querySelector('.spot-date');
        
        // 提取狀態值
        let status = 0;
        if (statusElement) {
            if (statusElement.classList.contains('status-active')) status = 1;
            else if (statusElement.classList.contains('status-inactive')) status = 0;
            else if (statusElement.classList.contains('status-rejected')) status = 2;
        }
        
        return {
            spotId: spotId,
            displayId: displayId,
            name: nameElement ? nameElement.textContent.trim() : '',
            description: descElement ? descElement.textContent.trim() : '',
            location: locationElement ? locationElement.textContent.trim() : '',
            status: status,
            statusText: statusElement ? statusElement.textContent.trim() : '',
            statusClass: statusElement ? statusElement.className : '',
            date: dateElement ? dateElement.textContent.trim() : '',
            imageUrl: imageElement ? imageElement.src : null,
            hasImage: !!imageElement && imageElement.style.display !== 'none',
            actions: cells[6].innerHTML, // 保存操作按鈕的 HTML
            originalRow: row.cloneNode(true), // 保存原始行
            // 直接從 data-region 屬性讀取地區資訊
            region: row.dataset.region || ''
        };
    } catch (error) {
        console.error('提取景點資料時發生錯誤:', error);
        return null;
    }
}

// 套用篩選和排序
function applyFiltersAndSort() {
    // 先篩選
    filteredData = allSpotData.filter(spot => {
        // 狀態篩選
        if (currentFilters.status !== null) {
            const spotStatus = spot.spotStatus !== undefined ? spot.spotStatus : spot.status;
            if (spotStatus !== currentFilters.status) {
                return false;
            }
        }
        
        // 地區篩選
        if (currentFilters.region !== null) {
            // 優先使用資料庫的region欄位，如果沒有再從地址提取
            let spotRegion = spot.region;
            if (!spotRegion || spotRegion.trim() === '') {
                const spotLocation = spot.spotLoc || spot.location || '';
                spotRegion = extractRegionFromLocation(spotLocation);
            }
            
            // 清理地區名稱（移除多餘空格）並比對
            const cleanSpotRegion = spotRegion ? spotRegion.replace(/\s+/g, '') : '';
            const cleanFilterRegion = currentFilters.region.replace(/\s+/g, '');
            
            if (cleanSpotRegion !== cleanFilterRegion) {
                return false;
            }
        }
        
        // 關鍵字搜尋（名稱和位置）
        if (currentFilters.keyword) {
            const keyword = currentFilters.keyword.toLowerCase();
            const spotName = spot.spotName || spot.name || '';
            const spotLocation = spot.spotLoc || spot.location || '';
            const nameMatch = spotName.toLowerCase().includes(keyword);
            const locationMatch = spotLocation.toLowerCase().includes(keyword);
            if (!nameMatch && !locationMatch) {
                return false;
            }
        }
        
        return true;
    });
    
    // 再排序
    applySorting();
    
    console.log('篩選結果:', filteredData.length, '筆資料');
}

// 套用排序
function applySorting() {
    filteredData.sort((a, b) => {
        let valueA, valueB;
        
        switch (currentSortField) {
            case 'spotId':
                valueA = a.spotId;
                valueB = b.spotId;
                break;
            case 'name':
                valueA = a.spotName || a.name || '';
                valueB = b.spotName || b.name || '';
                break;
            case 'location':
                valueA = a.spotLoc || a.location || '';
                valueB = b.spotLoc || b.location || '';
                break;
            case 'status':
                valueA = a.spotStatus !== undefined ? a.spotStatus : a.status;
                valueB = b.spotStatus !== undefined ? b.spotStatus : b.status;
                break;
            case 'date':
                const dateA = a.spotCreateAt || a.date || '';
                const dateB = b.spotCreateAt || b.date || '';
                valueA = new Date(dateA);
                valueB = new Date(dateB);
                break;
            default:
                valueA = a.spotId;
                valueB = b.spotId;
        }
        
        if (typeof valueA === 'string') {
            valueA = valueA.toLowerCase();
            valueB = valueB.toLowerCase();
        }
        
        if (valueA < valueB) {
            return isAscending ? -1 : 1;
        } else if (valueA > valueB) {
            return isAscending ? 1 : -1;
        }
        return 0;
    });
}

// 渲染表格
function renderTable() {
    const tbody = document.querySelector('tbody');
    if (!tbody) return;
    
    // 計算分頁
    const startIndex = currentPage * pageSize;
    let endIndex = startIndex + pageSize;
    
    // 如果是顯示所有模式，確保 pageSize 足夠大
    const pageSizeSelect = document.getElementById('pageSizeSelect');
    if (pageSizeSelect && pageSizeSelect.value === '-1') {
        pageSize = filteredData.length;
        endIndex = filteredData.length;
    }
    
    const pageData = filteredData.slice(startIndex, endIndex);
    
    // 清空表格
    tbody.innerHTML = '';
    
    if (pageData.length === 0) {
        // 顯示空狀態
        tbody.innerHTML = `
            <tr class="empty-row">
                <td colspan="7" style="text-align: center; padding: 40px 0;">
                    <div style="color: #6b7280;">
                        <div style="font-size: var(--font-size-3xl); margin-bottom: 10px;">📭</div>
                        <div>目前沒有符合條件的景點資料</div>
                        <div style="font-size: var(--font-size-xs); margin-top: 5px;">請調整篩選條件或清除篩選</div>
                    </div>
                </td>
            </tr>
        `;
    } else {
        // 渲染資料行
        pageData.forEach(spot => {
            const newRow = createTableRow(spot);
            tbody.appendChild(newRow);
        });
        
        // 重新綁定事件
        bindTableEvents();
    }
    
    // 更新分頁資訊
    updatePaginationInfo();
    
    // 更新統計資訊
    updateStatsInfo();
    
    console.log(`渲染表格完成: 第 ${currentPage + 1} 頁，顯示 ${pageData.length} 筆資料`);
}

// 創建表格行
function createTableRow(spot) {
    const row = document.createElement('tr');
    
    // 取得正確的資料欄位值
    const spotId = spot.spotId;
    const spotName = spot.spotName || spot.name || '';
    const spotDesc = spot.spotDesc || spot.description || '';
    const spotLoc = spot.spotLoc || spot.location || '';
    const spotStatus = spot.spotStatus !== undefined ? spot.spotStatus : (spot.status || 0);
    const spotCreateAt = spot.spotCreateAt || spot.date || '';
    const firstPictureUrl = spot.firstPictureUrl || spot.imageUrl || null;
    
    // 提取地區資訊
    const spotRegion = spot.region || extractRegionFromLocation(spotLoc);
    
    // 設定地區資料屬性
    if (spotRegion) {
        row.dataset.region = spotRegion;
    }
    
    // 格式化狀態
    const statusText = getStatusText(spotStatus);
    const statusClass = getStatusClass(spotStatus);
    
    // 格式化日期
    const formattedDate = formatDate(spotCreateAt);
    
    // 判斷是否有圖片
    const hasImage = !!firstPictureUrl;
    
    row.innerHTML = `
        <td><input type="checkbox" name="spotIds" value="${spotId}" class="spot-checkbox"></td>
        <td><span class="spot-id">#${spotId}</span></td>
        <td>
            <div class="spot-info">
                ${hasImage ? 
                    `<img src="${firstPictureUrl}" alt="${spotName}" class="spot-image" onload="handleImageLoad(this)" onerror="handleImageError(this)">` : ''
                }
                <div class="spot-image-placeholder ${hasImage ? 'spot-image-placeholder-none' : 'spot-image-placeholder-flex'}">🏞️</div>
                <div class="spot-text-info">
                    <div class="spot-name">${spotName}</div>
                    <div class="spot-desc" title="${spotDesc}">${truncateText(spotDesc, 50)}</div>
                </div>
            </div>
        </td>
        <td><span class="spot-location">${spotLoc}</span></td>
        <td><span class="${statusClass}">${statusText}</span></td>
        <td><span class="spot-date">${formattedDate}</span></td>
        <td>${generateActionButtons(spotId)}</td>
    `;
    
    return row;
}

// 重新綁定表格事件
function bindTableEvents() {
    // 重新綁定 checkbox 事件
    document.querySelectorAll('tbody input[type="checkbox"]').forEach(checkbox => {
        checkbox.addEventListener('change', function() {
            const row = this.closest('tr');
            if (this.checked) {
                row.classList.add('selected');
            } else {
                row.classList.remove('selected');
            }
            updateSelectedInfo();
            
            // 更新全選 checkbox 狀態
            const totalCheckboxes = document.querySelectorAll('tbody input[type="checkbox"]').length;
            const checkedCheckboxes = document.querySelectorAll('tbody input[type="checkbox"]:checked').length;
            const selectAll = document.getElementById('selectAll');
            if (selectAll) {
                selectAll.checked = totalCheckboxes === checkedCheckboxes && totalCheckboxes > 0;
            }
        });
    });
}

// 更新分頁資訊
function updatePaginationInfo() {
    const pageSizeSelect = document.getElementById('pageSizeSelect');
    const isShowAll = pageSizeSelect && pageSizeSelect.value === '-1';
    
    let totalPages, startRecord, endRecord;
    
    if (isShowAll) {
        // 顯示所有模式
        totalPages = 1;
        startRecord = filteredData.length > 0 ? 1 : 0;
        endRecord = filteredData.length;
    } else {
        // 正常分頁模式
        totalPages = Math.ceil(filteredData.length / pageSize);
        startRecord = filteredData.length > 0 ? currentPage * pageSize + 1 : 0;
        endRecord = Math.min((currentPage + 1) * pageSize, filteredData.length);
    }
    
    // 更新分頁文字資訊
    const paginationInfo = document.getElementById('paginationInfo');
    if (paginationInfo) {
        if (filteredData.length > 0) {
            if (isShowAll) {
                paginationInfo.innerHTML = `顯示所有資料 (共 ${filteredData.length} 筆)`;
            } else {
                paginationInfo.innerHTML = `
                    共 ${totalPages} 頁，目前在第 ${currentPage + 1} 頁
                    (顯示第 ${startRecord}-${endRecord} 筆，共 ${filteredData.length} 筆)
                `;
            }
        } else {
            paginationInfo.innerHTML = '沒有資料';
        }
    }
    
    // 更新表格標題的數量標籤
    const countBadge = document.querySelector('.count-badge');
    if (countBadge) {
        countBadge.textContent = `共 ${filteredData.length} 筆`;
    }
    
    // 更新分頁按鈕狀態
    updatePaginationButtons();
}

// 更新統計資訊
function updateStatsInfo() {
    // 計算各狀態的數量
    const statusCounts = {
        total: filteredData.length,
        active: filteredData.filter(s => {
            const status = s.spotStatus !== undefined ? s.spotStatus : s.status;
            return status === 1;
        }).length,
        inactive: filteredData.filter(s => {
            const status = s.spotStatus !== undefined ? s.spotStatus : s.status;
            return status === 0;
        }).length,
        rejected: filteredData.filter(s => {
            const status = s.spotStatus !== undefined ? s.spotStatus : s.status;
            return status === 2;
        }).length
    };
    
    // 更新統計卡片（如果存在）
    const statCards = document.querySelectorAll('.stat-card');
    statCards.forEach(card => {
        const label = card.querySelector('.stat-label')?.textContent;
        const valueEl = card.querySelector('.stat-value');
        if (valueEl && label) {
            if (label.includes('總計')) valueEl.textContent = statusCounts.total;
            else if (label.includes('上架')) valueEl.textContent = statusCounts.active;
            else if (label.includes('下架')) valueEl.textContent = statusCounts.inactive;
            else if (label.includes('退回')) valueEl.textContent = statusCounts.rejected;
        }
    });
}

// 更新篩選按鈕狀態
function updateButtonState(dropdown) {
    const btn = dropdown.querySelector('.filter-btn');
    const selectedRadio = dropdown.querySelector('input[type="radio"]:checked');
    const span = btn.querySelector('span');
    
    if (selectedRadio && selectedRadio.value !== 'all') {
        btn.classList.add('has-value');
        const label = dropdown.querySelector(`label[for="${selectedRadio.id}"]`).textContent;
        span.textContent = label;
    } else {
        btn.classList.remove('has-value');
        const filterType = dropdown.dataset.filter;
        if (filterType === 'status') span.textContent = '狀態篩選';
        else if (filterType === 'region') span.textContent = '地區篩選';
    }
}

// 更新選中資訊
function updateSelectedInfo() {
    const selectedCount = document.querySelectorAll('tbody input[type="checkbox"]:checked').length;
    const selectedInfo = document.getElementById('selectedInfo');
    const batchBtn = document.getElementById('batchBtn');
    const selectedCountEl = document.getElementById('selectedCount');
    
    if (selectedCount > 0) {
        selectedInfo.classList.remove('hidden');
        selectedInfo.classList.add('display-inline');
        selectedCountEl.textContent = selectedCount;
    } else {
        selectedInfo.classList.add('hidden');
        selectedInfo.classList.remove('display-inline');
    }
    
    // 修改為只要有選擇就顯示批量操作（方便測試）
    if (selectedCount >= 1) {
        batchBtn.classList.remove('hidden');
        console.log('顯示批量操作按鈕，已選擇', selectedCount, '個項目');
    } else {
        batchBtn.classList.add('hidden');
        // 隱藏批量操作選單
        const batchMenu = document.getElementById('batchMenu');
        if (batchMenu) {
            batchMenu.style.display = 'none';
        }
    }
}

// 清除所有篩選 - 改為純前端操作
// 清除所有勾選狀態
function clearAllSelections() {
    console.log('清除所有勾選狀態');
    
    // 清除全選 checkbox
    const selectAll = document.getElementById('selectAll');
    if (selectAll) {
        selectAll.checked = false;
    }
    
    // 清除所有行的勾選狀態
    const checkboxes = document.querySelectorAll('tbody input[type="checkbox"]');
    checkboxes.forEach(checkbox => {
        checkbox.checked = false;
        const row = checkbox.closest('tr');
        if (row) {
            row.classList.remove('selected');
        }
    });
    
    // 更新選中資訊
    updateSelectedInfo();
    
    console.log('所有勾選狀態已清除');
}

function clearAllFilters() {
    console.log('清除所有篩選');
    
    // 重設篩選條件
    currentFilters = {
        status: null,
        region: null,
        keyword: null
    };
    
    // 重設狀態篩選
    const statusAll = document.getElementById('status-all');
    if (statusAll) {
        statusAll.checked = true;
    }
    
    // 重設地區篩選
    const regionAll = document.querySelector('input[name="region"][value="all"]');
    if (regionAll) {
        regionAll.checked = true;
    }
    
    // 清空搜尋框
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.value = '';
    }
    
    // 重設排序
    isAscending = false;
    currentSortField = 'spotId';
    const sortBtn = document.getElementById('sortBtn');
    if (sortBtn) {
        const sortIcon = sortBtn.querySelector('.sort-icon');
        if (sortIcon) {
            sortIcon.textContent = '↓';
            sortBtn.title = '目前：降序排列（點擊切換為升序）';
        }
    }
    
    // 重設分頁
    currentPage = 0;
    
    // 更新按鈕狀態
    document.querySelectorAll('.filter-dropdown').forEach(dropdown => {
        updateButtonState(dropdown);
    });
    
    // 重新套用篩選和排序
    applyFiltersAndSort();
    
    // 清除所有勾選狀態
    clearAllSelections();
    
    // 如果目前是顯示所有模式，重新調整 pageSize
    const pageSizeSelect = document.getElementById('pageSizeSelect');
    if (pageSizeSelect && pageSizeSelect.value === '-1') {
        pageSize = filteredData.length;
    }
    
    renderTable();
    
    console.log('篩選已清除');
}

// 綁定分頁按鈕事件
function bindPaginationEvents() {
    // 綁定第一頁按鈕
    const firstPageBtn = document.getElementById('firstPageBtn');
    if (firstPageBtn) {
        const button = firstPageBtn.querySelector('button');
        if (button) {
            button.addEventListener('click', () => goToPage(0));
        }
    }
    
    // 綁定上一頁按鈕
    const prevPageBtn = document.getElementById('prevPageBtn');
    if (prevPageBtn) {
        const button = prevPageBtn.querySelector('button');
        if (button) {
            button.addEventListener('click', goToPrevPage);
        }
    }
    
    // 綁定下一頁按鈕
    const nextPageBtn = document.getElementById('nextPageBtn');
    if (nextPageBtn) {
        const button = nextPageBtn.querySelector('button');
        if (button) {
            button.addEventListener('click', goToNextPage);
        }
    }
    
    // 綁定最後一頁按鈕
    const lastPageBtn = document.getElementById('lastPageBtn');
    if (lastPageBtn) {
        const button = lastPageBtn.querySelector('button');
        if (button) {
            button.addEventListener('click', goToLastPage);
        }
    }
    
    console.log('分頁按鈕事件綁定完成');
}

// 初始化篩選按鈕狀態
function initializeFilterStates() {
    console.log('初始化篩選按鈕狀態');
    
    // 從 URL 參數讀取初始狀態（保持與後端的一致性）
    const url = new URL(window.location.href);
    const urlStatus = url.searchParams.get('status');
    const urlRegion = url.searchParams.get('region');
    const urlKeyword = url.searchParams.get('keyword');
    const urlSort = url.searchParams.get('sort') || 'spotId';
    const urlDirection = url.searchParams.get('direction') || 'desc';
    const urlSize = url.searchParams.get('size') || '10';
    const urlPage = url.searchParams.get('page') || '0';
    
    // 設定初始篩選條件
    currentFilters.status = urlStatus ? parseInt(urlStatus) : null;
    currentFilters.region = urlRegion || null;
    currentFilters.keyword = urlKeyword || null;
    currentSortField = urlSort;
    isAscending = urlDirection === 'asc';
    pageSize = parseInt(urlSize);
    currentPage = parseInt(urlPage);
    
    // 設定搜尋框值
    const searchInput = document.getElementById('searchInput');
    if (searchInput && currentFilters.keyword) {
        searchInput.value = currentFilters.keyword;
    }
    
    // 設定分頁大小選擇器
    const pageSizeSelect = document.getElementById('pageSizeSelect');
    if (pageSizeSelect) {
        pageSizeSelect.value = pageSize.toString();
    }
    
    // 初始化所有篩選下拉選單的狀態
    document.querySelectorAll('.filter-dropdown').forEach(dropdown => {
        updateButtonState(dropdown);
    });
    
    // 初始化排序按鈕狀態
    const sortBtn = document.getElementById('sortBtn');
    if (sortBtn) {
        const sortIcon = sortBtn.querySelector('.sort-icon');
        if (sortIcon) {
            if (isAscending) {
                sortIcon.textContent = '↑';
                sortBtn.title = '目前：升序排列（點擊切換為降序）';
            } else {
                sortIcon.textContent = '↓';
                sortBtn.title = '目前：降序排列（點擊切換為升序）';
            }
        }
    }
}

// 分頁函數
function goToPage(page) {
    const totalPages = Math.ceil(filteredData.length / pageSize);
    if (page >= 0 && page < totalPages) {
        currentPage = page;
        
        // 清除所有勾選狀態
        clearAllSelections();
        
        renderTable();
        console.log(`跳轉到第 ${page + 1} 頁`);
    }
}

function goToPrevPage() {
    if (currentPage > 0) {
        goToPage(currentPage - 1);
    }
}

function goToNextPage() {
    const totalPages = Math.ceil(filteredData.length / pageSize);
    if (currentPage < totalPages - 1) {
        goToPage(currentPage + 1);
    }
}

function goToLastPage() {
    const totalPages = Math.ceil(filteredData.length / pageSize);
    goToPage(totalPages - 1);
}

// 更新分頁按鈕狀態和頁碼
function updatePaginationButtons() {
    const pageSizeSelect = document.getElementById('pageSizeSelect');
    const isShowAll = pageSizeSelect && pageSizeSelect.value === '-1';
    const totalPages = isShowAll ? 1 : Math.ceil(filteredData.length / pageSize);
    const paginationContainer = document.getElementById('paginationContainer');
    
    if (!paginationContainer) return;
    
    // 總是顯示分頁容器
    paginationContainer.classList.remove('hidden');
    
    // 獲取分頁按鈕容器
    const paginationNav = paginationContainer.querySelector('nav');
    
    // 在「顯示所有」模式下隱藏分頁控制按鈕
    if (isShowAll || filteredData.length === 0) {
        if (paginationNav) paginationNav.classList.add('hidden');
    } else {
        // 正常分頁模式且有資料時顯示分頁按鈕
        if (paginationNav) paginationNav.classList.remove('hidden');
    }
    
    // 更新按鈕狀態
    const firstBtn = document.getElementById('firstPageBtn');
    const prevBtn = document.getElementById('prevPageBtn');
    const nextBtn = document.getElementById('nextPageBtn');
    const lastBtn = document.getElementById('lastPageBtn');
    const pageNumbers = document.getElementById('pageNumbers');
    
    // 禁用/上架按鈕
    if (firstBtn && prevBtn) {
        if (currentPage === 0) {
            firstBtn.classList.add('disabled');
            prevBtn.classList.add('disabled');
        } else {
            firstBtn.classList.remove('disabled');
            prevBtn.classList.remove('disabled');
        }
    }
    
    if (nextBtn && lastBtn) {
        if (currentPage >= totalPages - 1) {
            nextBtn.classList.add('disabled');
            lastBtn.classList.add('disabled');
        } else {
            nextBtn.classList.remove('disabled');
            lastBtn.classList.remove('disabled');
        }
    }
    
    // 生成頁碼按鈕
    if (pageNumbers) {
        pageNumbers.innerHTML = '';
        
        // 計算顯示的頁碼範圍（最多顯示 5 個頁碼）
        const maxVisiblePages = 5;
        let startPage = Math.max(0, currentPage - Math.floor(maxVisiblePages / 2));
        let endPage = Math.min(totalPages - 1, startPage + maxVisiblePages - 1);
        
        // 調整起始頁面，確保總是顯示 maxVisiblePages 個頁碼（如果可能）
        if (endPage - startPage + 1 < maxVisiblePages) {
            startPage = Math.max(0, endPage - maxVisiblePages + 1);
        }
        
        for (let i = startPage; i <= endPage; i++) {
            const pageItem = document.createElement('li');
            pageItem.className = 'page-item' + (i === currentPage ? ' active' : '');
            
            const pageButton = document.createElement('button');
            pageButton.className = 'page-link';
            pageButton.textContent = i + 1;
            pageButton.onclick = () => goToPage(i);
            
            pageItem.appendChild(pageButton);
            pageNumbers.appendChild(pageItem);
        }
    }
}

// 動態更新地區選項
function updateRegionOptions(regions) {
    console.log('更新地區選項:', regions);
    
    const regionDropdown = document.querySelector('input[name="region"]')?.closest('.filter-dropdown');
    if (!regionDropdown) {
        console.error('找不到地區篩選下拉選單');
        return;
    }
    
    const dropdownContent = regionDropdown.querySelector('.dropdown-content');
    if (!dropdownContent) {
        console.error('找不到地區下拉選單內容');
        return;
    }
    
    // 完全清空並重建選項
    dropdownContent.innerHTML = '';
    
    // 添加全部地區選項
    const allItem = document.createElement('div');
    allItem.className = 'option-item';
    allItem.innerHTML = `
        <input type="radio" name="region" value="all" id="region-all" checked>
        <label for="region-all">全部地區</label>
    `;
    dropdownContent.appendChild(allItem);
    
    // 添加動態地區選項
    regions.forEach((region, index) => {
        const optionItem = document.createElement('div');
        optionItem.className = 'option-item';
        
        const regionId = `region-${index}`;
        optionItem.innerHTML = `
            <input type="radio" name="region" value="${region}" id="${regionId}">
            <label for="${regionId}">${region}</label>
        `;
        
        dropdownContent.appendChild(optionItem);
    });
    
    // 重新綁定地區篩選事件
    const radioButtons = dropdownContent.querySelectorAll('input[type="radio"]');
    radioButtons.forEach(radio => {
        radio.addEventListener('change', function() {
            console.log('地區篩選改變:', this.value);
            
            // 更新篩選條件
            currentFilters.region = this.value === 'all' ? null : this.value;
            console.log('目前地區篩選條件:', currentFilters.region);
            
            // 更新按鈕狀態
            updateButtonState(regionDropdown);
            
            // 立即套用篩選和排序
            applyFiltersAndSort();
            currentPage = 0; // 重置到第一頁
            
            // 清除所有勾選狀態
            clearAllSelections();
            
            // 如果目前是顯示所有模式，重新調整 pageSize
            const pageSizeSelect = document.getElementById('pageSizeSelect');
            if (pageSizeSelect && pageSizeSelect.value === '-1') {
                pageSize = filteredData.length;
            }
            
            renderTable();
            
            // 隱藏下拉選單
            const menu = regionDropdown.querySelector('.dropdown-menu');
            const btn = regionDropdown.querySelector('.filter-btn');
            if (menu && btn) {
                setTimeout(() => {
                    menu.classList.remove('show');
                    btn.classList.remove('active');
                }, 150);
            }
        });
    });
    
    // 綁定點擊選項事件
    dropdownContent.querySelectorAll('.option-item').forEach(item => {
        item.addEventListener('click', function() {
            const radio = item.querySelector('input[type="radio"]');
            if (radio && !radio.checked) {
                radio.checked = true;
                radio.dispatchEvent(new Event('change'));
            }
        });
    });
    
    console.log(`地區選項更新完成，共 ${regions.length + 1} 個選項`);
}

// 綁定圖片事件
function bindImageEvents() {
    document.querySelectorAll('.spot-image').forEach(img => {
        img.addEventListener('error', function() {
            handleImageError(this);
        });
        
        img.addEventListener('load', function() {
            handleImageLoad(this);
        });
    });
}

// 綁定批量操作按鈕事件
function bindBatchActions() {
    console.log('=== 開始綁定批量操作事件 ===');
    
    // 延遲執行，確保 DOM 完全就緒
    setTimeout(() => {
        const batchBtn = document.getElementById('batchBtn');
        let batchMenu = document.getElementById('batchMenu');

        console.log('查找批量操作元素...');
        console.log('batchBtn:', batchBtn);
        console.log('batchMenu:', batchMenu);

        if (!batchBtn) {
            console.error('❌ 找不到批量操作按鈕 (id=batchBtn)');
            return;
        }
        
        // 如果找不到批量操作選單，就動態創建一個
        if (!batchMenu) {
            console.log('⚠️ 找不到批量操作選單，動態創建中...');
            batchMenu = document.createElement('div');
            batchMenu.id = 'batchMenu';
            batchMenu.className = 'temp-menu'; // 先用安全 class
            batchMenu.style.display = 'none';
            batchMenu.innerHTML = `
                <button type="button" class="dropdown-item" data-action="activate">
                    <span class="material-icons icon-success">check_circle</span> 批量上架
                </button>
                <button type="button" class="dropdown-item" data-action="deactivate">
                    <span class="material-icons icon-warning">cancel</span> 批量下架
                </button>
                <button type="button" class="dropdown-item danger" data-action="delete">
                    <span class="material-icons icon-danger">delete</span> 批量刪除
                </button>
            `;
            document.body.appendChild(batchMenu);
            setTimeout(() => {
                batchMenu.className = 'super-test-menu';
                console.log('✅ 批量操作選單 class 已切換為 super-test-menu (延遲切換)');
            }, 500);
            console.log('✅ 批量操作選單已動態創建');
        }

        console.log('✅ 找到批量操作元素:', {
            batchBtn: {
                id: batchBtn.id,
                className: batchBtn.className,
                hidden: batchBtn.classList.contains('hidden'),
                display: window.getComputedStyle(batchBtn).display
            },
            batchMenu: {
                id: batchMenu.id,
                className: batchMenu.className,
                display: window.getComputedStyle(batchMenu).display,
                parent: batchMenu.parentElement.tagName
            }
        });

        // 使用事件委託，綁定到 document 上
        document.addEventListener('click', function(e) {
            console.log('🔍 點擊事件觸發:', {
                target: e.target.tagName + (e.target.className ? '.' + e.target.className : ''),
                id: e.target.id,
                closestDropdownItem: e.target.closest('.dropdown-item') ? '找到' : '未找到',
                closestBatchMenu: e.target.closest('#batchMenu') ? '找到' : '未找到'
            });
            
            // 檢查是否點擊了批量操作按鈕
            if (e.target.id === 'batchBtn' || e.target.closest('#batchBtn')) {
                e.preventDefault();
                e.stopPropagation();
                
                console.log('🔘 批量操作按鈕被點擊！');
                
                const currentDisplay = batchMenu.style.display;
                console.log('選單當前 display:', currentDisplay);
                
                if (currentDisplay === 'block') {
                    batchMenu.style.display = 'none';
                    console.log('➡️ 隱藏選單');
                } else {
                    // 獲取按鈕位置
                    const btnElement = document.getElementById('batchBtn');
                    const rect = btnElement.getBoundingClientRect();
                    
                    // 計算選單位置，確保不會超出視窗邊界
                    const menuWidth = 180;
                    let leftPos = rect.left + rect.width - menuWidth; // 右對齊按鈕
                    
                    // 如果右對齊會超出視窗左邊界，則左對齊按鈕
                    if (leftPos < 10) {
                        leftPos = rect.left;
                    }
                    
                    // 如果左對齊會超出視窗右邊界，則向左調整
                    if (leftPos + menuWidth > window.innerWidth - 10) {
                        leftPos = window.innerWidth - menuWidth - 10;
                    }
                    
                    // 設置選單位置和顯示
                    batchMenu.style.cssText = `
                        display: block !important;
                        position: fixed !important;
                        left: ${leftPos}px !important;
                        top: ${rect.bottom + 8}px !important;
                        z-index: 99999 !important;
                        min-width: ${menuWidth}px !important;
                        padding: 8px 0 !important;
                        background: #ffffff !important;
                        border: 1px solid #e0e0e0 !important;
                        border-radius: 8px !important;
                        box-shadow: 0 8px 24px rgba(0,0,0,0.15) !important;
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif !important;
                    `;
                    
                    console.log('➡️ 顯示選單，位置:', {
                        buttonRect: rect,
                        menuLeft: leftPos,
                        menuTop: rect.bottom + 8,
                        menuWidth: menuWidth
                    });
                }
                return;
            }
            
            // 檢查是否點擊了選單項目 - 簡化邏輯
            const clickedItem = e.target.closest('.dropdown-item');
            if (clickedItem && clickedItem.closest('#batchMenu')) {
                e.preventDefault();
                e.stopPropagation();
                
                const action = clickedItem.dataset.action;
                console.log('📌 選單項目被點擊！', {
                    item: clickedItem,
                    action: action,
                    text: clickedItem.textContent.trim()
                });
                
                if (action) {
                    console.log('🚀 執行批量操作:', action);
                    batchAction(action);
                    batchMenu.style.display = 'none';
                } else {
                    console.log('❌ 沒有找到 action 屬性');
                }
                return;
            }
            
            // 點擊其他地方關閉選單
            if (batchMenu.style.display === 'block') {
                if (!e.target.closest('#batchMenu')) {
                    batchMenu.style.display = 'none';
                    console.log('➡️ 點擊外部，關閉選單');
                }
            }
        }, true); // 使用捕獲階段

        // 添加滾動事件監聽，滾動時隱藏選單
        window.addEventListener('scroll', function() {
            if (batchMenu && batchMenu.style.display === 'block') {
                batchMenu.style.display = 'none';
                console.log('➡️ 頁面滾動，自動關閉批量操作選單');
            }
        }, { passive: true });

        console.log('✅ 批量操作事件綁定完成（事件委託版）');
    }, 100);
}

// 綁定表單提交事件
function bindFormSubmissions() {
    // 使用事件委託處理刪除表單提交
    document.addEventListener('submit', function(e) {
        if (e.target.matches('.action-form')) {
            const form = e.target;
            const spotName = form.dataset.spotName || '此景點';
            
            if (!confirmDelete(spotName)) {
                e.preventDefault();
                return false;
            }
        }
    });
    
    console.log('表單提交事件已綁定');
}

// API匯入相關函數
function initializeApiImport() {
    const modal = document.getElementById('apiImportModal');
    const openBtn = document.getElementById('apiImportBtn');
    const closeBtn = document.getElementById('closeModal');
    const importAllBtn = document.getElementById('importAllBtn');
    const cityButtons = document.querySelectorAll('.city-btn');
    const resultArea = document.getElementById('importResult');

    // 標記是否已完成匯入
    let importCompleted = false;

    // 打開模態視窗
    openBtn.addEventListener('click', () => {
        modal.style.display = 'block';
        importCompleted = false; // 重置匯入狀態
    });

    // 關閉模態視窗
    closeBtn.addEventListener('click', () => {
        modal.style.display = 'none';
        // 如果匯入已完成，關閉時重新整理頁面
        if (importCompleted) {
            window.location.reload();
        }
    });

    // 修改點擊模態視窗外部關閉的邏輯
    let mouseDownInModal = false;
    
    // 記錄滑鼠按下的位置是否在模態視窗內
    modal.addEventListener('mousedown', (e) => {
        // 檢查點擊位置是否在模態內容區域內
        const modalContent = modal.querySelector('.modal-content');
        if (modalContent.contains(e.target)) {
            mouseDownInModal = true;
        }
    });

    // 監聽滑鼠釋放事件
    window.addEventListener('mouseup', (e) => {
        // 只有當滑鼠按下和釋放都在模態視窗外時才關閉
        if (e.target === modal && !mouseDownInModal) {
            modal.style.display = 'none';
            // 如果匯入已完成，關閉時重新整理頁面
            if (importCompleted) {
                window.location.reload();
            }
        }
        mouseDownInModal = false;
    });

    // 全台匯入
    importAllBtn.addEventListener('click', async () => {
        const count = document.getElementById('import-count').value;
        if (count < 10 || count > 200) {
            alert('請輸入10-200之間的數字');
            return;
        }

        try {
            importAllBtn.disabled = true;
            importAllBtn.innerHTML = '<i class="material-icons">hourglass_empty</i> 匯入中...';
            
            const response = await fetch(`/admin/spot/api/import-spots?limit=${count}`, {
                method: 'POST'
            });
            
            const result = await response.json();
            showResult(result);
            
            // 標記匯入已完成
            if (result.success) {
                importCompleted = true;
            }
        } catch (error) {
            console.error('匯入失敗:', error);
            showResult({
                success: false,
                message: '匯入失敗，請稍後再試'
            });
        } finally {
            importAllBtn.disabled = false;
            importAllBtn.innerHTML = '<i class="material-icons">download</i> 開始匯入全台景點';
        }
    });

    // 依縣市匯入
    cityButtons.forEach(btn => {
        btn.addEventListener('click', async () => {
            const city = btn.dataset.city;
            const count = document.getElementById('city-count').value;
            
            if (count < 10 || count > 100) {
                alert('請輸入10-100之間的數字');
                return;
            }

            try {
                btn.disabled = true;
                const originalText = btn.innerHTML;
                btn.innerHTML = '<i class="material-icons">hourglass_empty</i>';
                
                const response = await fetch(`/admin/spot/api/import-spots-by-city?city=${city}&limit=${count}`, {
                    method: 'POST'
                });
                
                const result = await response.json();
                showResult(result);
                
                // 標記匯入已完成
                if (result.success) {
                    importCompleted = true;
                }
            } catch (error) {
                console.error('匯入失敗:', error);
                showResult({
                    success: false,
                    message: '匯入失敗，請稍後再試'
                });
            } finally {
                btn.disabled = false;
                btn.innerHTML = originalText;
            }
        });
    });
}

// 顯示匯入結果
function showResult(result) {
    const resultArea = document.getElementById('importResult');
    const resultContent = resultArea.querySelector('.result-content');
    
    resultArea.style.display = 'block';
    
    console.log('API 返回結果:', result);
    
    if (result.success) {
        // 確保 data 存在且包含 ImportResult 數據
        const data = result.data || {};
        
        resultContent.innerHTML = `
            <div class="success-message">
                <i class="material-icons">check_circle</i>
                <p>匯入成功！</p>
                <ul>
                    <li>✅ 成功匯入: ${data.successCount || 0} 筆</li>
                    <li>⏭️ 重複跳過: ${data.skippedCount || 0} 筆</li>
                    <li>❌ 匯入失敗: ${data.errorCount || 0} 筆</li>
                </ul>
                <p class="success-note">匯入完成，關閉視窗後將自動更新資料</p>
            </div>
        `;
        
        // 顯示 Toastify 通知
        Toastify({
            text: `✅ 匯入成功！共匯入 ${data.successCount || 0} 筆資料`,
            duration: 3000,
            close: true,
            gravity: "top",
            position: "center",
            style: {
                background: "#2ecc71"
            }
        }).showToast();
    } else {
        resultContent.innerHTML = `
            <div class="error-message">
                <i class="material-icons">error</i>
                <p>${result.message || '匯入失敗，請稍後再試'}</p>
            </div>
        `;
        
        // 顯示 Toastify 通知
        Toastify({
            text: `❌ ${result.message || '匯入失敗，請稍後再試'}`,
            duration: 3000,
            close: true,
            gravity: "top",
            position: "center",
            style: {
                background: "#e74c3c"
            }
        }).showToast();
    }
}

// 在 document ready 時初始化
document.addEventListener('DOMContentLoaded', () => {
    // ... 其他初始化代碼 ...
    
    // 初始化API匯入功能
    initializeApiImport();
});