// Review 頁面 JavaScript 功能
console.log('🔄 Review 頁面腳本開始載入...');

// 等待 DOM 完全載入
document.addEventListener('DOMContentLoaded', function() {
    console.log('=== Review 頁面 DOMContentLoaded 開始 ===');
    
    // 初始化勾選框功能
    initializeCheckboxes();
    
    // 初始化搜尋功能
    initializeSearch();
    
    // 初始化 Toast 功能
    initializeToast();
    
    // 初始化篩選功能（預設顯示待審核）
    setTimeout(() => {
        filterByStatus('0');
    }, 100);
    
    console.log('=== Review 頁面 DOMContentLoaded 完成 ===');
});

// 初始化勾選框功能
function initializeCheckboxes() {
    console.log('🔧 初始化勾選框功能...');
    
    const selectAll = document.getElementById('selectAll');
    const checkboxes = document.querySelectorAll('.admin-table .spot-checkbox');
    
    console.log('🔍 找到的元素:');
    console.log('- selectAll:', selectAll);
    console.log('- checkboxes 數量:', checkboxes.length);
    console.log('- checkboxes 清單:', checkboxes);
    
    if (selectAll && checkboxes.length > 0) {
        // 綁定全選框事件監聽器
        selectAll.addEventListener('change', function() {
            console.log('📋 全選框狀態變更:', this.checked);
            
            checkboxes.forEach((checkbox, index) => {
                setTimeout(() => {
                    checkbox.checked = this.checked;
                    const row = checkbox.closest('tr');
                    if (this.checked) {
                        row.classList.add('selected');
                    } else {
                        row.classList.remove('selected');
                    }
                    console.log(`勾選框 ${index + 1} 設為: ${this.checked}`);
                }, index * 30); // 漸進式動畫效果
            });
            
            // 延遲更新統計資訊（等所有 checkbox 動畫完成）
            setTimeout(() => {
                updateSelectedInfo();
            }, checkboxes.length * 30 + 100);
        });
        
        console.log('✅ 全選框事件監聽器已綁定');
    } else {
        console.error('❌ 找不到全選框或個別勾選框');
        console.error('selectAll 存在:', !!selectAll);
        console.error('checkboxes 數量:', checkboxes.length);
    }
    
    // 初始化個別勾選框事件監聽器
    checkboxes.forEach((checkbox, index) => {
        checkbox.addEventListener('change', function() {
            console.log(`單個勾選框 ${index + 1} 狀態變更:`, this.value, this.checked);
            
            const row = this.closest('tr');
            if (this.checked) {
                row.classList.add('selected');
            } else {
                row.classList.remove('selected');
            }
            
            updateMasterCheckbox();
            updateSelectedInfo();
        });
    });
    
    // 初始化主勾選框狀態
    updateMasterCheckbox();
    
    console.log('✅ 勾選框初始化完成，找到', checkboxes.length, '個勾選框');
    
    // 添加測試功能到 window 物件，方便在控制台調試
    window.testCheckboxes = function() {
        console.log('=== 手動測試勾選框功能 ===');
        const selectAll = document.getElementById('selectAll');
        const checkboxes = document.querySelectorAll('.admin-table .spot-checkbox');
        console.log('selectAll 元素:', selectAll);
        console.log('checkboxes 數量:', checkboxes.length);
        console.log('checkboxes 清單:', checkboxes);
        
        if (selectAll) {
            console.log('測試點擊全選框...');
            selectAll.click();
        }
        
        if (checkboxes.length > 0) {
            console.log('測試點擊第一個勾選框...');
            checkboxes[0].click();
        }
    };
    
    console.log('💡 可在控制台執行 testCheckboxes() 來測試勾選框功能');
}

// 更新主要勾選框狀態
function updateMasterCheckbox() {
    const masterCheckbox = document.querySelector('.admin-table #selectAll');
    const checkboxes = document.querySelectorAll('.admin-table .spot-checkbox');
    
    if (masterCheckbox && checkboxes.length > 0) {
        const checkedCount = Array.from(checkboxes).filter(cb => cb.checked).length;
        masterCheckbox.checked = checkedCount === checkboxes.length;
        masterCheckbox.indeterminate = checkedCount > 0 && checkedCount < checkboxes.length;
        
        console.log('🔄 主勾選框狀態更新:', {
            checkedCount: checkedCount,
            totalCount: checkboxes.length,
            masterChecked: masterCheckbox.checked,
            indeterminate: masterCheckbox.indeterminate
        });
    }
}

// 更新選中資訊
function updateSelectedInfo() {
    const selectedCount = document.querySelectorAll('.admin-table .spot-checkbox:checked').length;
    console.log('📊 選中項目數量:', selectedCount);
    
    // 這裡可以添加視覺反饋，比如顯示選中數量
    if (selectedCount > 0) {
        console.log('✅ 已選中', selectedCount, '個項目');
    }
}

// 初始化搜尋功能
function initializeSearch() {
    const searchInput = document.getElementById('searchInput');
    const clearBtn = document.querySelector('.clear-search');
    
    if (searchInput) {
        // 搜尋輸入事件
        searchInput.addEventListener('input', function() {
            const value = this.value.trim();
            
            // 顯示/隱藏清除按鈕
            if (clearBtn) {
                if (value) {
                    clearBtn.classList.add('show');
                } else {
                    clearBtn.classList.remove('show');
                }
            }
            
            // 執行搜尋
            filterTable();
        });
        
        // 清除搜尋按鈕事件
        if (clearBtn) {
            clearBtn.addEventListener('click', function() {
                searchInput.value = '';
                this.classList.remove('show');
                filterTable();
                searchInput.focus();
            });
        }
    }
}

// 表格篩選功能
function filterTable() {
    const input = document.getElementById('searchInput');
    if (!input) return;
    
    const searchValue = input.value.toLowerCase();
    const rows = document.querySelectorAll('.admin-table tbody tr[data-status]');
    
    rows.forEach(row => {
        // 檢查 row.children 是否存在
        if (row.children.length < 4) return;
        
        const name = row.children[2]?.textContent.toLowerCase() || '';
        const location = row.children[3]?.textContent.toLowerCase() || '';
        
        // 檢查是否符合搜尋條件
        const matchesSearch = !searchValue || name.includes(searchValue) || location.includes(searchValue);
        
        // 檢查是否符合狀態篩選
        const rowStatus = row.getAttribute('data-status');
        const matchesStatus = currentFilter === 'all' || rowStatus == currentFilter;
        
        // 只有同時符合搜尋和狀態篩選的行才顯示
        const shouldShow = matchesSearch && matchesStatus;
        
        if (shouldShow) {
            row.classList.remove('hidden');
            row.style.display = '';
        } else {
            row.classList.add('hidden');
            row.style.display = 'none';
        }
    });
}

// 批次操作功能
function batchOperation(operation) {
    const checkedBoxes = document.querySelectorAll('.spot-checkbox:checked');
    const ids = Array.from(checkedBoxes).map(cb => cb.value);
    
    if (ids.length === 0) {
        showCustomToast('請先勾選要操作的景點', 'red');
        return;
    }
    
    let operationText = '';
    let endpoint = '';
    
    switch(operation) {
        case 'approve':
            operationText = '通過';
            endpoint = '/admin/spot/api/batch-approve';
            break;
        case 'reject':
            operationText = '拒絕';
            endpoint = '/admin/spot/api/batch-reject';
            break;
        case 'pending':
            operationText = '移至待審';
            endpoint = '/admin/spot/api/batch-pending';
            break;
        default:
            showCustomToast('未知的操作類型', 'red');
            return;
    }
    
    if (!confirm(`確定要批次${operationText}選取的 ${ids.length} 個景點嗎？`)) {
        return;
    }
    
    // 移除 CSRF Token 依賴
    // const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    // const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
    
    fetch(endpoint, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(ids)
    })
    .then(response => {
        console.log(`批次${operationText} HTTP 狀態碼:`, response.status);
        if (!response.ok) {
            throw new Error('HTTP錯誤: ' + response.status);
        }
        return response.json();
    })
    .then(result => {
        console.log(`批次${operationText} API回傳`, result);
        showCustomToast(result.message || `批次${operationText}完成`, 'green');
        setTimeout(() => location.reload(), 2500);
    })
    .catch(error => {
        console.error(`批次${operationText}錯誤:`, error);
        showCustomToast(`批次${operationText}失敗: ` + error.message, 'red');
    });
    
    // 關閉下拉選單
    closeBatchDropdown();
}

// 相容性函數（保持原有的 batchApprove 函數以防 HTML 中有引用）
function batchApprove() {
    batchOperation('approve');
}

// 單個景點通過功能
function approveSpot(id) {
    if (!confirm('確定要通過此景點？')) return;
    
    console.log('開始審核通過，景點ID:', id);
    
    fetch('/admin/spot/api/approve/' + id, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        console.log('HTTP 狀態碼:', response.status);
        if (!response.ok) {
            throw new Error('HTTP錯誤: ' + response.status);
        }
        return response.json();
    })
    .then(result => {
        console.log('API回傳', result);
        showCustomToast(result.message || '通過成功', 'green');
        setTimeout(() => location.reload(), 2500);
    })
    .catch(error => {
        console.error('審核錯誤:', error);
        showCustomToast('審核失敗: ' + error.message, 'red');
    });
}

// Modal 功能
function showRejectModal(id) {
    const rejectSpotIdInput = document.getElementById('rejectSpotId');
    const modal = document.getElementById('rejectModal');
    
    if (rejectSpotIdInput) {
        rejectSpotIdInput.value = id;
    }
    
    if (modal) {
        modal.classList.add('show');
    }
}

function closeModal() {
    const modal = document.getElementById('rejectModal');
    if (modal) {
        modal.classList.remove('show');
    }
}

function submitReject() {
    const idInput = document.getElementById('rejectSpotId');
    const reasonSelect = document.getElementById('rejectReason');
    const remarkInput = document.getElementById('rejectRemark');
    
    if (!idInput || !reasonSelect) {
        showCustomToast('表單元素不存在', 'red');
        return;
    }
    
    const id = idInput.value;
    const reason = reasonSelect.value;
    const remark = remarkInput ? remarkInput.value : '';
    
    if (reason === '其他' && !remark.trim()) {
        showCustomToast('請填寫補充說明', 'red');
        return;
    }
    
    fetch('/admin/spot/api/reject/' + id, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ reason, remark })
    })
    .then(response => {
        console.log('退回 HTTP 狀態碼:', response.status);
        if (!response.ok) {
            throw new Error('HTTP錯誤: ' + response.status);
        }
        return response.json();
    })
    .then(result => {
        console.log('退回 API回傳', result);
        closeModal();
        showCustomToast(result.message || '退回完成', 'orange');
        setTimeout(() => location.reload(), 2500);
    })
    .catch(error => {
        console.error('退回錯誤:', error);
        showCustomToast('退回失敗: ' + error.message, 'red');
    });
}

// Toast 訊息功能
let toastContainer = null;

function initializeToast() {
    // 創建 toast 容器（如果不存在）
    if (!toastContainer) {
        toastContainer = document.createElement('div');
        toastContainer.id = 'toast-container';
        toastContainer.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            z-index: 9999;
            pointer-events: none;
        `;
        document.body.appendChild(toastContainer);
    }
}

function showCustomToast(message, type = 'green') {
    if (!toastContainer) {
        initializeToast();
    }
    
    const toast = document.createElement('div');
    toast.className = `custom-toast ${type}`;
    toast.textContent = message;
    toast.style.pointerEvents = 'auto';
    
    toastContainer.appendChild(toast);
    
    // 顯示動畫
    setTimeout(() => {
        toast.classList.add('show');
    }, 100);
    
    // 自動隱藏
    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => {
            if (toast.parentNode) {
                toast.parentNode.removeChild(toast);
            }
        }, 300);
    }, 3000);
}

// 下拉選單功能
function toggleBatchDropdown() {
    const dropdown = document.getElementById('batchDropdown');
    const toggle = document.querySelector('.batch-btn');
    
    if (dropdown) {
        dropdown.classList.toggle('show');
        if (toggle) {
            toggle.classList.toggle('active');
        }
    }
    
    // 關閉其他下拉選單
    const filterDropdown = document.getElementById('filterDropdown');
    if (filterDropdown && filterDropdown.classList.contains('show')) {
        closeFilterDropdown();
    }
}

function closeBatchDropdown() {
    const dropdown = document.getElementById('batchDropdown');
    const toggle = document.querySelector('.batch-btn');
    
    if (dropdown) {
        dropdown.classList.remove('show');
    }
    if (toggle) {
        toggle.classList.remove('active');
    }
}

function toggleFilterDropdown() {
    const dropdown = document.getElementById('filterDropdown');
    const toggle = document.querySelector('.filter-btn');
    
    if (dropdown) {
        dropdown.classList.toggle('show');
        if (toggle) {
            toggle.classList.toggle('active');
        }
    }
    
    // 關閉其他下拉選單
    const batchDropdown = document.getElementById('batchDropdown');
    if (batchDropdown && batchDropdown.classList.contains('show')) {
        closeBatchDropdown();
    }
}

function closeFilterDropdown() {
    const dropdown = document.getElementById('filterDropdown');
    const toggle = document.querySelector('.filter-btn');
    
    if (dropdown) {
        dropdown.classList.remove('show');
    }
    if (toggle) {
        toggle.classList.remove('active');
    }
}

// 篩選功能
let currentFilter = '0'; // 預設顯示待審核

function filterByStatus(status) {
    currentFilter = status;
    const rows = document.querySelectorAll('.admin-table tbody tr[data-status]');
    
    // 更新篩選按鈕的 active 狀態
    const filterItems = document.querySelectorAll('#filterDropdown .dropdown-item');
    filterItems.forEach(item => {
        item.classList.remove('active');
        if (item.getAttribute('data-status') == status) {
            item.classList.add('active');
        }
    });
    
    // 篩選表格行
    rows.forEach(row => {
        const rowStatus = row.getAttribute('data-status');
        if (status === 'all' || rowStatus == status) {
            row.classList.remove('hidden');
            row.style.display = '';
        } else {
            row.classList.add('hidden');
            row.style.display = 'none';
        }
    });
    
    // 重新應用搜尋篩選（如果有的話）
    const searchInput = document.getElementById('searchInput');
    if (searchInput && searchInput.value.trim()) {
        filterTable();
    }
    
    // 關閉下拉選單
    closeFilterDropdown();
    
    // 更新選擇狀態
    updateMasterCheckbox();
    
    console.log('篩選狀態:', status === 'all' ? '全部' : getStatusText(status));
}

function getStatusText(status) {
    switch(status) {
        case '0': return '待審核';
        case '1': return '上架';
        case '2': return '退回';
        case '3': return '下架';
        default: return '未知';
    }
}

// 點擊外部關閉下拉選單
document.addEventListener('click', function(event) {
    const batchDropdown = document.getElementById('batchDropdown');
    const filterDropdown = document.getElementById('filterDropdown');
    const batchBtn = document.querySelector('.batch-btn');
    const filterBtn = document.querySelector('.filter-btn');
    
    // 如果點擊的不是批次操作按鈕或下拉選單內容，則關閉批次下拉選單
    if (batchDropdown && batchBtn && 
        !batchBtn.contains(event.target) && 
        !batchDropdown.contains(event.target)) {
        closeBatchDropdown();
    }
    
    // 如果點擊的不是篩選按鈕或下拉選單內容，則關閉篩選下拉選單
    if (filterDropdown && filterBtn && 
        !filterBtn.contains(event.target) && 
        !filterDropdown.contains(event.target)) {
        closeFilterDropdown();
    }
});

// 將函數暴露到全域範圍，供 HTML 中的 onclick 事件使用
window.batchApprove = batchApprove;
window.batchOperation = batchOperation;
window.approveSpot = approveSpot;
window.showRejectModal = showRejectModal;
window.closeModal = closeModal;
window.submitReject = submitReject;
window.filterTable = filterTable;
window.toggleBatchDropdown = toggleBatchDropdown;
window.closeBatchDropdown = closeBatchDropdown;
window.toggleFilterDropdown = toggleFilterDropdown;
window.closeFilterDropdown = closeFilterDropdown;
window.filterByStatus = filterByStatus;
window.showCustomToast = showCustomToast; 