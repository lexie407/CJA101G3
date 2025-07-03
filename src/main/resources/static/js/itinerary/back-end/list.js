console.log('=== itinerary list.js 開始載入 ===');

// 設定全域變數（從Thymeleaf傳遞）
function initializeGlobalData() {
    // 這些變數會在HTML中的inline script設定
    if (typeof window.allItinerariesData !== 'undefined') {
        console.log('=== 全域資料詳細檢查 ===');
        console.log('從後端獲取的完整資料:', window.allItinerariesData);
        console.log('資料筆數:', window.allItinerariesData.length);
        console.log('資料類型:', typeof window.allItinerariesData);
        console.log('是否為陣列:', Array.isArray(window.allItinerariesData));
        
        if (window.allItinerariesData.length > 0) {
            console.log('第一筆資料:', window.allItinerariesData[0]);
            console.log('最後一筆資料:', window.allItinerariesData[window.allItinerariesData.length - 1]);
        }
        
        // 檢查是否有重複的ID
        const ids = window.allItinerariesData.map(itinerary => itinerary.itnId);
        const uniqueIds = [...new Set(ids)];
        console.log('唯一ID數量:', uniqueIds.length, '總資料數量:', window.allItinerariesData.length);
        if (ids.length !== uniqueIds.length) {
            console.warn('發現重複的行程ID!');
        }
        console.log('===========================');
    } else {
        console.error('window.allItinerariesData 未定義!');
    }
}

// 批量操作函數（從HTML中的onclick移過來）
function batchAction(action) {
    const form = document.getElementById('batchForm');
    const checkedBoxes = document.querySelectorAll('.itinerary-checkbox:checked');
    
    if (checkedBoxes.length === 0) {
        alert('請至少選擇一個項目');
        return;
    }
    
    let confirmMessage = `確定要對 ${checkedBoxes.length} 個行程執行「批量${action === 'delete' ? '刪除' : (action === 'activate' ? '公開' : '私人')}」嗎？`;
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
        
        form.action = `/admin/itinerary/batch-${action}`;
        form.method = 'POST';
        form.submit();
    }
}

// 刪除確認函數（從HTML中的onsubmit移過來）
function confirmDelete(itineraryName) {
    return confirm(`確定要刪除這個行程「${itineraryName}」嗎？此操作無法復原！`);
}

// 暴露到全域供HTML使用
window.batchAction = batchAction;
window.confirmDelete = confirmDelete;

// 輔助函數
function getStatusText(status) {
    switch (status) {
        case 0: return '私人';
        case 1: return '公開';
        default: return '未知';
    }
}

function getStatusClass(status) {
    switch (status) {
        case 0: return 'status-badge status-private';
        case 1: return 'status-badge status-public';
        default: return 'status-badge';
    }
}

function formatDate(dateString) {
    if (!dateString) return '';
    // 處理 LocalDateTime 格式 (e.g., "2025-01-26T10:30:00")
    const date = new Date(dateString);
    return date.toISOString().split('T')[0]; // 只取日期部分 YYYY-MM-DD
}

function generateActionButtons(itnId) {
    // 取得 CSRF token
    const csrfToken = document.querySelector('meta[name="_csrf"]');
    const csrfParam = document.querySelector('meta[name="_csrf_header"]');
    
    let csrfInput = '';
    if (csrfToken && csrfParam) {
        csrfInput = `<input type="hidden" name="_csrf" value="${csrfToken.content}"/>`;
    }
    
    return `
        <div class="action-buttons">
            <a href="/admin/itinerary/detail/${itnId}" class="action-btn btn-view" title="查看">👁️</a>
            <a href="/admin/itinerary/edit/${itnId}" class="action-btn btn-edit" title="編輯">✏️</a>
            <form action="/admin/itinerary/delete/${itnId}" method="post" style="display: inline;" onsubmit="return confirm('確定要刪除這個行程嗎？此操作無法復原！')">
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

// 全域變數
let isAscending = false; // 預設為降序（最新的在前）
let currentSortField = 'itnId'; // 預設排序欄位
let allItineraryData = []; // 儲存所有行程資料
let filteredData = []; // 儲存篩選後的資料
let currentPage = 0;
let pageSize = 10;
let currentFilters = {
    status: null,
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
    
    // 綁定批量操作按鈕事件
    bindBatchActions();
    
    // 綁定表單提交事件
    bindFormSubmissions();
    
    console.log('=== 行程列表頁面初始化完成 ===');
});

// 初始化資料
async function initializeData() {
    console.log('=== 開始初始化資料 ===');
    
    try {
        // 使用從後端傳遞的資料
        if (window.allItinerariesData && Array.isArray(window.allItinerariesData)) {
            allItineraryData = window.allItinerariesData;
            console.log('使用從後端傳遞的資料，筆數:', allItineraryData.length);
        } else {
            console.warn('從後端傳遞的資料不存在或格式錯誤，嘗試從表格提取資料');
            // 從現有表格提取資料作為備用方案
            allItineraryData = extractDataFromTable();
            console.log('從表格提取的資料筆數:', allItineraryData.length);
        }
        
        // 如果還是沒有資料，使用空陣列
        if (!allItineraryData || allItineraryData.length === 0) {
            console.warn('沒有找到任何行程資料');
            allItineraryData = [];
        }
        
        // 套用篩選和排序
        applyFiltersAndSort();
        
        // 渲染表格
        renderTable();
        
        console.log('=== 資料初始化完成 ===');
        
    } catch (error) {
        console.error('初始化資料時發生錯誤:', error);
        allItineraryData = [];
        filteredData = [];
        renderTable();
    }
}

// 從表格提取資料（備用方案）
function extractDataFromTable() {
    const rows = document.querySelectorAll('tbody tr:not(.empty-row)');
    const data = [];
    
    rows.forEach(row => {
        const extractedData = extractItineraryDataFromRow(row);
        if (extractedData) {
            data.push(extractedData);
        }
    });
    
    return data;
}

function extractItineraryDataFromRow(row) {
    try {
        const checkbox = row.querySelector('.itinerary-checkbox');
        const itnId = checkbox ? parseInt(checkbox.value) : null;
        
        const nameEl = row.querySelector('.itinerary-name');
        const descEl = row.querySelector('.itinerary-desc');
        const spotCountEl = row.querySelector('.itinerary-days');
        const statusEl = row.querySelector('.status-badge');
        const dateEl = row.querySelector('.itinerary-date');
        
        if (!itnId) return null;
        
        // 提取狀態
        let isPublic = null;
        if (statusEl) {
            if (statusEl.classList.contains('status-public')) {
                isPublic = 1;
            } else if (statusEl.classList.contains('status-private')) {
                isPublic = 0;
            }
        }
        
        return {
            itnId: itnId,
            itnName: nameEl ? nameEl.textContent.trim() : '',
            itnDesc: descEl ? descEl.textContent.trim() : '',
            spotCount: spotCountEl ? parseInt(spotCountEl.textContent.replace(/[^\d]/g, '')) || 0 : 0,
            isPublic: isPublic,
            itnCreateDat: dateEl ? dateEl.textContent.trim() : '',
            publicStatusText: statusEl ? statusEl.textContent.trim() : ''
        };
    } catch (error) {
        console.error('提取行程資料時發生錯誤:', error);
        return null;
    }
}

// 套用篩選和排序
function applyFiltersAndSort() {
    console.log('套用篩選和排序...');
    
    // 從原始資料開始篩選
    filteredData = [...allItineraryData];
    
    // 關鍵字篩選
    if (currentFilters.keyword) {
        const keyword = currentFilters.keyword.toLowerCase();
        filteredData = filteredData.filter(itinerary => {
            return (itinerary.itnName && itinerary.itnName.toLowerCase().includes(keyword)) ||
                   (itinerary.itnDesc && itinerary.itnDesc.toLowerCase().includes(keyword));
        });
    }
    
    // 狀態篩選
    if (currentFilters.status !== null && currentFilters.status !== 'all') {
        const statusValue = parseInt(currentFilters.status);
        filteredData = filteredData.filter(itinerary => {
            return itinerary.isPublic === statusValue;
        });
    }
    
    // 排序
    applySorting();
    
    // 重設分頁
    currentPage = 0;
    
    console.log('篩選後資料筆數:', filteredData.length);
}

// 套用排序
function applySorting() {
    filteredData.sort((a, b) => {
        let aValue, bValue;
        
        switch (currentSortField) {
            case 'itnId':
                aValue = a.itnId || 0;
                bValue = b.itnId || 0;
                break;
            case 'itnName':
                aValue = (a.itnName || '').toLowerCase();
                bValue = (b.itnName || '').toLowerCase();
                break;
            case 'spotCount':
                aValue = a.spotCount || 0;
                bValue = b.spotCount || 0;
                break;
            case 'isPublic':
                aValue = a.isPublic || 0;
                bValue = b.isPublic || 0;
                break;
            case 'itnCreateDat':
                aValue = new Date(a.itnCreateDat || '1970-01-01');
                bValue = new Date(b.itnCreateDat || '1970-01-01');
                break;
            default:
                aValue = a.itnId || 0;
                bValue = b.itnId || 0;
        }
        
        if (aValue < bValue) return isAscending ? -1 : 1;
        if (aValue > bValue) return isAscending ? 1 : -1;
        return 0;
    });
}

// 渲染表格
function renderTable() {
    console.log('渲染表格...');
    
    const tbody = document.querySelector('tbody');
    if (!tbody) {
        console.error('找不到 tbody 元素');
        return;
    }
    
    // 清空現有內容
    tbody.innerHTML = '';
    
    // 如果沒有資料，顯示空狀態
    if (filteredData.length === 0) {
        tbody.innerHTML = `
            <tr class="empty-row">
                <td colspan="7">
                    <div class="empty-state-container">
                        <div class="icon">📭</div>
                        <div class="title">目前沒有符合條件的行程資料</div>
                        <div class="subtitle">請調整篩選條件或清除篩選</div>
                    </div>
                </td>
            </tr>
        `;
        updatePaginationInfo();
        updateStatsInfo();
        return;
    }
    
    // 計算分頁
    const startIndex = currentPage * pageSize;
    const endIndex = Math.min(startIndex + pageSize, filteredData.length);
    const pageData = filteredData.slice(startIndex, endIndex);
    
    // 渲染資料列
    pageData.forEach(itinerary => {
        const row = createTableRow(itinerary);
        tbody.appendChild(row);
    });
    
    // 重新綁定事件
    bindTableEvents();
    
    // 更新分頁和統計資訊
    updatePaginationInfo();
    updateStatsInfo();
}

// 建立表格列
function createTableRow(itinerary) {
    const row = document.createElement('tr');
    
    const statusClass = getStatusClass(itinerary.isPublic);
    const statusText = getStatusText(itinerary.isPublic);
    const formattedDate = formatDate(itinerary.itnCreateDat);
    
    row.innerHTML = `
        <td><input type="checkbox" name="itineraryIds" value="${itinerary.itnId}" class="itinerary-checkbox"></td>
        <td><span class="itinerary-id">#${itinerary.itnId}</span></td>
        <td>
            <div class="itinerary-info">
                <div class="itinerary-text-info">
                    <div class="itinerary-name">${itinerary.itnName || ''}</div>
                    <div class="itinerary-desc">${truncateText(itinerary.itnDesc || '', 50)}</div>
                </div>
            </div>
        </td>
        <td><span class="itinerary-days">${itinerary.spotCount || 0} 個</span></td>
        <td><span class="${statusClass}">${statusText}</span></td>
        <td><span class="itinerary-date">${formattedDate}</span></td>
        <td>
            ${generateActionButtons(itinerary.itnId)}
        </td>
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
    const totalPages = Math.ceil(filteredData.length / pageSize);
    const startRecord = filteredData.length > 0 ? currentPage * pageSize + 1 : 0;
    const endRecord = Math.min((currentPage + 1) * pageSize, filteredData.length);
    
    // 更新分頁文字資訊
    const paginationInfo = document.getElementById('paginationInfo');
    if (paginationInfo) {
        if (filteredData.length > 0) {
            paginationInfo.innerHTML = `
                共 ${totalPages} 頁，目前在第 ${currentPage + 1} 頁
                (顯示第 ${startRecord}-${endRecord} 筆，共 ${filteredData.length} 筆)
            `;
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
        public: filteredData.filter(i => i.isPublic === 1).length,
        private: filteredData.filter(i => i.isPublic === 0).length
    };
    
    // 更新統計卡片（如果存在）
    const statCards = document.querySelectorAll('.stat-card');
    statCards.forEach(card => {
        const label = card.querySelector('.stat-label')?.textContent;
        const valueEl = card.querySelector('.stat-value');
        if (valueEl && label) {
            if (label.includes('總計')) valueEl.textContent = statusCounts.total;
            else if (label.includes('公開')) valueEl.textContent = statusCounts.public;
            else if (label.includes('私人')) valueEl.textContent = statusCounts.private;
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
            batchMenu.classList.remove('show');
        }
    }
}

// 清除所有篩選 - 改為純前端操作
function clearAllFilters() {
    console.log('清除所有篩選');
    
    // 重設篩選條件
    currentFilters = {
        status: null,
        keyword: null
    };
    
    // 重設狀態篩選
    const statusAll = document.getElementById('status-all');
    if (statusAll) {
        statusAll.checked = true;
    }
    
    // 清空搜尋框
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.value = '';
    }
    
    // 重設排序
    isAscending = false;
    currentSortField = 'itnId';
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
            button.addEventListener('click', function(e) {
                e.preventDefault();
                e.stopPropagation();
                goToPage(0);
            });
        }
    }
    
    // 綁定上一頁按鈕
    const prevPageBtn = document.getElementById('prevPageBtn');
    if (prevPageBtn) {
        const button = prevPageBtn.querySelector('button');
        if (button) {
            button.addEventListener('click', function(e) {
                e.preventDefault();
                e.stopPropagation();
                goToPrevPage();
            });
        }
    }
    
    // 綁定下一頁按鈕
    const nextPageBtn = document.getElementById('nextPageBtn');
    if (nextPageBtn) {
        const button = nextPageBtn.querySelector('button');
        if (button) {
            button.addEventListener('click', function(e) {
                e.preventDefault();
                e.stopPropagation();
                goToNextPage();
            });
        }
    }
    
    // 綁定最後一頁按鈕
    const lastPageBtn = document.getElementById('lastPageBtn');
    if (lastPageBtn) {
        const button = lastPageBtn.querySelector('button');
        if (button) {
            button.addEventListener('click', function(e) {
                e.preventDefault();
                e.stopPropagation();
                goToLastPage();
            });
        }
    }
}

// 初始化篩選按鈕狀態
function initializeFilterStates() {
    console.log('初始化篩選按鈕狀態');
    
    // 綁定排序按鈕
    const sortBtn = document.getElementById('sortBtn');
    if (sortBtn) {
        sortBtn.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            console.log('排序按鈕被點擊');
            isAscending = !isAscending;
            
            const sortIcon = sortBtn.querySelector('.sort-icon');
            if (sortIcon) {
                sortIcon.textContent = isAscending ? '↑' : '↓';
                sortBtn.title = isAscending ? '目前：升序排列（點擊切換為降序）' : '目前：降序排列（點擊切換為升序）';
            }
            
            applyFiltersAndSort();
            renderTable();
        });
        
        // 設定初始狀態
        const sortIcon = sortBtn.querySelector('.sort-icon');
        if (sortIcon) {
            sortIcon.textContent = isAscending ? '↑' : '↓';
            sortBtn.title = isAscending ? '目前：升序排列（點擊切換為降序）' : '目前：降序排列（點擊切換為升序）';
        }
    }
    
    // 綁定全選checkbox
    const selectAll = document.getElementById('selectAll');
    if (selectAll) {
        selectAll.addEventListener('change', function() {
            console.log('全選checkbox被點擊:', this.checked);
            const checkboxes = document.querySelectorAll('tbody input[type="checkbox"]');
            checkboxes.forEach(checkbox => {
                checkbox.checked = this.checked;
                const row = checkbox.closest('tr');
                if (this.checked) {
                    row.classList.add('selected');
                } else {
                    row.classList.remove('selected');
                }
            });
            updateSelectedInfo();
        });
    }
    
    // 綁定搜尋框
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        let searchTimeout;
        searchInput.addEventListener('input', function() {
            clearTimeout(searchTimeout);
            searchTimeout = setTimeout(() => {
                console.log('搜尋關鍵字:', this.value);
                currentFilters.keyword = this.value.trim() || null;
                applyFiltersAndSort();
                renderTable();
            }, 300);
        });
    }
    
    // 綁定篩選下拉選單
    document.querySelectorAll('.filter-dropdown').forEach(dropdown => {
        const btn = dropdown.querySelector('.filter-btn');
        const menu = dropdown.querySelector('.dropdown-menu');
        
        if (btn && menu) {
            btn.addEventListener('click', function(e) {
                e.stopPropagation();
                
                // 關閉其他下拉選單
                document.querySelectorAll('.filter-dropdown .dropdown-menu').forEach(otherMenu => {
                    if (otherMenu !== menu) {
                        otherMenu.classList.remove('show');
                    }
                });
                
                menu.classList.toggle('show');
            });
            
            // 綁定選項變更事件
            dropdown.querySelectorAll('input[type="radio"]').forEach(radio => {
                radio.addEventListener('change', function() {
                    const filterType = dropdown.dataset.filter;
                    
                    if (filterType === 'status') {
                        currentFilters.status = this.value === 'all' ? null : this.value;
                        console.log('狀態篩選變更:', currentFilters.status);
                    }
                    
                    updateButtonState(dropdown);
                    applyFiltersAndSort();
                    renderTable();
                    
                    // 關閉下拉選單
                    menu.classList.remove('show');
                });
            });
        }
    });
    
    // 綁定每頁顯示數量選擇器
    const pageSizeSelect = document.getElementById('pageSizeSelect');
    if (pageSizeSelect) {
        pageSizeSelect.addEventListener('change', function() {
            pageSize = parseInt(this.value);
            currentPage = 0; // 重設到第一頁
            console.log('每頁顯示數量變更:', pageSize);
            renderTable();
        });
    }
    
    // 綁定清除按鈕
    const clearBtn = document.querySelector('.clear-btn');
    if (clearBtn) {
        clearBtn.addEventListener('click', function() {
            clearAllFilters();
        });
    }
    
    // 點擊外部關閉下拉選單
    document.addEventListener('click', function(e) {
        if (!e.target.closest('.filter-dropdown')) {
            document.querySelectorAll('.dropdown-menu').forEach(menu => {
                menu.classList.remove('show');
            });
        }
    });
}

// 分頁相關函數
function goToPage(page) {
    const totalPages = Math.ceil(filteredData.length / pageSize);
    if (page >= 0 && page < totalPages) {
        currentPage = page;
        renderTable();
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
    if (totalPages > 0) {
        goToPage(totalPages - 1);
    }
}

function updatePaginationButtons() {
    const totalPages = Math.ceil(filteredData.length / pageSize);
    
    // 更新按鈕狀態
    const firstPageBtn = document.getElementById('firstPageBtn');
    const prevPageBtn = document.getElementById('prevPageBtn');
    const nextPageBtn = document.getElementById('nextPageBtn');
    const lastPageBtn = document.getElementById('lastPageBtn');
    
    if (firstPageBtn) {
        const button = firstPageBtn.querySelector('button');
        if (button) {
            button.disabled = currentPage === 0;
            firstPageBtn.classList.toggle('disabled', currentPage === 0);
        }
    }
    
    if (prevPageBtn) {
        const button = prevPageBtn.querySelector('button');
        if (button) {
            button.disabled = currentPage === 0;
            prevPageBtn.classList.toggle('disabled', currentPage === 0);
        }
    }
    
    if (nextPageBtn) {
        const button = nextPageBtn.querySelector('button');
        if (button) {
            button.disabled = currentPage >= totalPages - 1;
            nextPageBtn.classList.toggle('disabled', currentPage >= totalPages - 1);
        }
    }
    
    if (lastPageBtn) {
        const button = lastPageBtn.querySelector('button');
        if (button) {
            button.disabled = currentPage >= totalPages - 1;
            lastPageBtn.classList.toggle('disabled', currentPage >= totalPages - 1);
        }
    }
    
    // 更新頁碼按鈕
    const pageNumbers = document.getElementById('pageNumbers');
    if (pageNumbers && totalPages > 0) {
        pageNumbers.innerHTML = '';
        
    const startPage = Math.max(0, currentPage - 2);
    const endPage = Math.min(totalPages - 1, currentPage + 2);
    
    for (let i = startPage; i <= endPage; i++) {
        const pageItem = document.createElement('li');
        pageItem.className = `page-item ${i === currentPage ? 'active' : ''}`;
                
            const pageLink = document.createElement('button');
            pageLink.type = 'button';
            pageLink.className = 'page-link';
            pageLink.textContent = i + 1;
            pageLink.addEventListener('click', function(e) {
                e.preventDefault();
                e.stopPropagation();
                goToPage(i);
            });
            
            pageItem.appendChild(pageLink);
        pageNumbers.appendChild(pageItem);
        }
    }
}

// 綁定批量操作按鈕事件
function bindBatchActions() {
    const batchBtn = document.getElementById('batchBtn');
    const batchMenu = document.getElementById('batchMenu');
    
    if (batchBtn && batchMenu) {
        // 綁定批量按鈕點擊事件
        batchBtn.addEventListener('click', function(e) {
            e.stopPropagation();
            console.log('批量操作按鈕被點擊');
            
            // 關閉其他下拉選單
            document.querySelectorAll('.dropdown-menu').forEach(menu => {
                if (menu !== batchMenu) {
                    menu.classList.remove('show');
                }
            });
            
            // 切換批量操作選單
            batchMenu.classList.toggle('show');
            console.log('批量選單狀態:', batchMenu.classList.contains('show') ? '顯示' : '隱藏');
        });
        
        // 綁定批量操作選單項目點擊事件
        batchMenu.querySelectorAll('.dropdown-item').forEach(item => {
            item.addEventListener('click', function(e) {
                e.stopPropagation();
                const action = this.getAttribute('data-action');
                if (action) {
                    console.log('執行批量操作:', action);
                    batchAction(action);
                    // 關閉選單
                    batchMenu.classList.remove('show');
                }
            });
        });
        
        // 點擊外部關閉選單
        document.addEventListener('click', function(e) {
            if (!e.target.closest('.batch-actions')) {
                batchMenu.classList.remove('show');
            }
        });
    } else {
        console.warn('批量操作按鈕或選單未找到:', { batchBtn, batchMenu });
    }
}

// 綁定表單提交事件
function bindFormSubmissions() {
    // 綁定刪除表單的提交事件
    document.addEventListener('submit', function(e) {
        if (e.target.matches('form[action*="/admin/itinerary/delete/"]')) {
            const itineraryName = e.target.closest('tr')?.querySelector('.itinerary-name')?.textContent || '此行程';
            if (!confirm(`確定要刪除「${itineraryName}」嗎？此操作無法復原！`)) {
                e.preventDefault();
            }
        }
    });
} 