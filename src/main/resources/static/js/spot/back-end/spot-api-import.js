/**
 * API匯入頁面 JavaScript - 符合 Material Design 3
 * 整合政府觀光資料開放平臺景點資料匯入功能
 */

// 當文檔加載完成時執行
$(document).ready(function() {
    // 初始化匯入按鈕
    initImportButton();
    
    // 初始化資料表格
    initDataTable();
});

// 初始化匯入按鈕
function initImportButton() {
    const importBtn = document.getElementById('importBtn');
    if (!importBtn) return;
    
    importBtn.addEventListener('click', function() {
        // 取得選擇的 API 類型
        const apiType = document.querySelector('input[name="apiType"]:checked')?.value;
        if (!apiType) {
            showToast('警告', '請選擇 API 類型', 'warning');
            return;
        }
        
        // 取得關鍵字
        const keyword = document.getElementById('keyword')?.value.trim();
        if (!keyword) {
            showToast('警告', '請輸入搜尋關鍵字', 'warning');
            return;
        }
        
        // 取得地區
        const district = document.getElementById('district')?.value;
        
        // 顯示載入中
        showLoading(true);
        
        // 根據 API 類型呼叫不同的函數
        if (apiType === 'government') {
            fetchGovernmentData(keyword, district);
        } else if (apiType === 'google') {
            fetchGooglePlacesData(keyword, district);
        }
    });
}

// 取得政府開放資料
function fetchGovernmentData(keyword, district) {
    // 構建請求參數
    const params = new URLSearchParams();
    params.append('keyword', keyword);
    if (district) params.append('district', district);
    
    // 發送請求
    fetch(`/admin/spot/api/government-data?${params.toString()}`)
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP 錯誤 ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            console.log('政府開放資料 API 回應:', data);
            
            if (data.success) {
                // 成功取得資料
                updateDataTable(data.data, 'government');
                showToast('成功', `已取得 ${data.data.length} 筆政府開放資料`, 'success');
            } else {
                // API 呼叫成功但未取得資料
                showToast('錯誤', data.error || '無法取得政府開放資料', 'error');
            }
        })
        .catch(error => {
            console.error('取得政府開放資料時發生錯誤:', error);
            showToast('錯誤', '發生錯誤: ' + error.message, 'error');
        })
        .finally(() => {
            // 隱藏載入中
            showLoading(false);
        });
}

// 取得 Google Places 資料
function fetchGooglePlacesData(keyword, district) {
    // 構建請求參數
    const params = new URLSearchParams();
    params.append('keyword', keyword);
    if (district) params.append('district', district);
    
    // 發送請求 - 使用新版 API 端點
    fetch(`/admin/spot/api/google-places-search?${params.toString()}`)
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP 錯誤 ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            console.log('Google Places API 回應:', data);
            
            if (data.success) {
                // 成功取得資料
                // 處理新版 API 的響應格式
                const processedData = data.data.map(item => {
                    return {
                        name: item.displayName?.text || item.name,
                        address: item.formattedAddress,
                        rating: item.rating,
                        phone: item.phoneNumber,
                        website: item.websiteUri || item.website,
                        placeId: item.id || item.placeId,
                        latitude: item.location?.latitude,
                        longitude: item.location?.longitude,
                        photoReferences: item.photoReferences,
                        openingHours: item.openingHours
                    };
                });
                
                updateDataTable(processedData, 'google');
                showToast('成功', `已取得 ${processedData.length} 筆 Google Places 資料`, 'success');
            } else {
                // API 呼叫成功但未取得資料
                showToast('錯誤', data.error || '無法取得 Google Places 資料', 'error');
            }
        })
        .catch(error => {
            console.error('取得 Google Places 資料時發生錯誤:', error);
            showToast('錯誤', '發生錯誤: ' + error.message, 'error');
        })
        .finally(() => {
            // 隱藏載入中
            showLoading(false);
        });
}

// 初始化資料表格
function initDataTable() {
    // 檢查表格是否存在
    const tableElement = document.getElementById('apiDataTable');
    if (!tableElement) return;
    
    // 初始化 DataTable
    window.dataTable = $('#apiDataTable').DataTable({
        columns: [
            { data: 'name', title: '景點名稱' },
            { data: 'address', title: '地址' },
            { data: 'rating', title: '評分', 
              render: function(data) {
                  return data ? data : '-';
              }
            },
            { data: 'phone', title: '電話', 
              render: function(data) {
                  return data ? data : '-';
              }
            },
            { data: 'website', title: '網站', 
              render: function(data) {
                  if (data) {
                      return `<a href="${data}" target="_blank" class="text-truncate d-inline-block" style="max-width: 200px;">${data}</a>`;
                  }
                  return '-';
              }
            },
            { data: null, title: '操作', 
              render: function(data) {
                  return `
                      <button class="btn btn-sm btn-primary view-btn" data-id="${data.id || ''}">
                          <i class="fas fa-eye"></i> 查看
                      </button>
                      <button class="btn btn-sm btn-success import-item-btn" data-id="${data.id || ''}">
                          <i class="fas fa-file-import"></i> 匯入
                      </button>
                  `;
              }
            }
        ],
        language: {
            "lengthMenu": "顯示 _MENU_ 筆資料",
            "zeroRecords": "沒有符合的資料",
            "info": "顯示第 _START_ 至 _END_ 筆資料，共 _TOTAL_ 筆",
            "infoEmpty": "顯示第 0 至 0 筆資料，共 0 筆",
            "infoFiltered": "(從 _MAX_ 筆資料中過濾)",
            "search": "搜尋:",
            "paginate": {
                "first": "第一頁",
                "last": "最後一頁",
                "next": "下一頁",
                "previous": "上一頁"
            }
        },
        responsive: true,
        pageLength: 10,
        dom: 'Bfrtip',
        buttons: [
            'copy', 'excel', 'pdf'
        ]
    });
    
    // 綁定查看按鈕事件
    $('#apiDataTable').on('click', '.view-btn', function() {
        const rowData = window.dataTable.row($(this).closest('tr')).data();
        showSpotDetails(rowData);
    });
    
    // 綁定匯入按鈕事件
    $('#apiDataTable').on('click', '.import-item-btn', function() {
        const rowData = window.dataTable.row($(this).closest('tr')).data();
        importSpot(rowData);
    });
}

// 更新資料表格
function updateDataTable(data, source) {
    // 檢查資料表格是否已初始化
    if (!window.dataTable) {
        console.error('資料表格尚未初始化');
        return;
    }
    
    // 清空表格
    window.dataTable.clear();
    
    // 添加資料來源標記
    data.forEach(item => {
        item.dataSource = source;
    });
    
    // 添加新資料
    window.dataTable.rows.add(data).draw();
    
    // 顯示表格容器
    document.getElementById('tableContainer').style.display = 'block';
}

// 顯示景點詳細資訊
function showSpotDetails(spotData) {
    console.log('顯示景點詳細資訊:', spotData);
    
    // 創建模態框內容
    let modalContent = `
        <div class="modal-header">
            <h5 class="modal-title">${spotData.name || '未命名景點'}</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div class="modal-body">
            <div class="row">
                <div class="col-md-6">
                    <h6>基本資訊</h6>
                    <table class="table table-sm">
                        <tr>
                            <th>景點名稱</th>
                            <td>${spotData.name || '-'}</td>
                        </tr>
                        <tr>
                            <th>地址</th>
                            <td>${spotData.address || '-'}</td>
                        </tr>
                        <tr>
                            <th>評分</th>
                            <td>${spotData.rating || '-'}</td>
                        </tr>
                        <tr>
                            <th>電話</th>
                            <td>${spotData.phone || '-'}</td>
                        </tr>
                        <tr>
                            <th>網站</th>
                            <td>${spotData.website ? `<a href="${spotData.website}" target="_blank">${spotData.website}</a>` : '-'}</td>
                        </tr>
                    </table>
                </div>
                <div class="col-md-6">
    `;
    
    // 添加照片（如果有的話）
    if (spotData.photoUrl) {
        modalContent += `
            <h6>照片預覽</h6>
            <img src="${spotData.photoUrl}" class="img-fluid rounded" alt="${spotData.name}">
        `;
    } else if (spotData.photoReferences && spotData.photoReferences.length > 0 && spotData.dataSource === 'google') {
        // 新版 API 的照片處理
        const photoRef = spotData.photoReferences[0];
        modalContent += `
            <h6>照片預覽</h6>
            <img src="/admin/spot/api/google-photo?reference=${encodeURIComponent(photoRef)}&maxWidth=400" class="img-fluid rounded" alt="${spotData.name}">
        `;
    } else if (spotData.dataSource === 'google') {
        modalContent += `
            <h6>照片預覽</h6>
            <p class="text-muted">無法顯示照片預覽，請匯入後查看</p>
        `;
    }
    
    // 添加營業時間（如果有的話）
    if (spotData.openingHours && spotData.openingHours.length > 0) {
        modalContent += `
            <h6 class="mt-3">營業時間</h6>
            <ul class="list-group">
        `;
        
        spotData.openingHours.forEach(hour => {
            modalContent += `<li class="list-group-item">${hour}</li>`;
        });
        
        modalContent += `</ul>`;
    }
    
    modalContent += `
                </div>
            </div>
        </div>
        <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">關閉</button>
            <button type="button" class="btn btn-success" onclick="importSpot(${JSON.stringify(spotData).replace(/"/g, '&quot;')})">
                <i class="fas fa-file-import"></i> 匯入此景點
            </button>
        </div>
    `;
    
    // 創建或更新模態框
    let modalElement = document.getElementById('spotDetailModal');
    if (!modalElement) {
        modalElement = document.createElement('div');
        modalElement.className = 'modal fade';
        modalElement.id = 'spotDetailModal';
        modalElement.setAttribute('tabindex', '-1');
        modalElement.setAttribute('aria-hidden', 'true');
        modalElement.innerHTML = `
            <div class="modal-dialog modal-lg">
                <div class="modal-content">
                    ${modalContent}
                </div>
            </div>
        `;
        document.body.appendChild(modalElement);
    } else {
        modalElement.querySelector('.modal-content').innerHTML = modalContent;
    }
    
    // 顯示模態框
    const modal = new bootstrap.Modal(modalElement);
    modal.show();
}

// 匯入景點
function importSpot(spotData) {
    console.log('匯入景點:', spotData);
    
    // 確認是否要匯入
    if (!confirm(`確定要匯入景點「${spotData.name || '未命名景點'}」嗎？`)) {
        return;
    }
    
    // 顯示載入中
    showLoading(true);
    
    // 發送請求
    fetch('/admin/spot/api/import', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            name: spotData.name,
            address: spotData.address,
            phone: spotData.phone,
            website: spotData.website,
            rating: spotData.rating,
            placeId: spotData.placeId,
            latitude: spotData.latitude,
            longitude: spotData.longitude,
            dataSource: spotData.dataSource,
            photoReferences: spotData.photoReferences
        })
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`HTTP 錯誤 ${response.status}`);
        }
        return response.json();
    })
    .then(data => {
        console.log('匯入景點回應:', data);
        
        if (data.success) {
            // 成功匯入
            showToast('成功', '已成功匯入景點', 'success');
            
            // 如果有返回的景點 ID，提供連結
            if (data.spotId) {
                setTimeout(() => {
                    if (confirm('景點已成功匯入，是否前往編輯頁面？')) {
                        window.location.href = `/admin/spot/edit/${data.spotId}`;
                    }
                }, 1000);
            }
        } else {
            // API 呼叫成功但匯入失敗
            showToast('錯誤', data.error || '匯入景點失敗', 'error');
        }
    })
    .catch(error => {
        console.error('匯入景點時發生錯誤:', error);
        showToast('錯誤', '發生錯誤: ' + error.message, 'error');
    })
    .finally(() => {
        // 隱藏載入中
        showLoading(false);
    });
}

// 顯示載入中
function showLoading(show) {
    const loadingElement = document.getElementById('loading');
    if (!loadingElement) return;
    
    loadingElement.style.display = show ? 'flex' : 'none';
}

// 顯示 Toast 通知
function showToast(title, message, type = 'info') {
    // 檢查是否有 Toast 容器，如果沒有則創建
    let toastContainer = document.querySelector('.toast-container');
    if (!toastContainer) {
        toastContainer = document.createElement('div');
        toastContainer.className = 'toast-container position-fixed bottom-0 end-0 p-3';
        document.body.appendChild(toastContainer);
    }
    
    // 創建 Toast 元素
    const toastId = 'toast-' + Date.now();
    const toast = document.createElement('div');
    toast.className = `toast ${type === 'error' ? 'bg-danger text-white' : ''}`;
    toast.id = toastId;
    toast.setAttribute('role', 'alert');
    toast.setAttribute('aria-live', 'assertive');
    toast.setAttribute('aria-atomic', 'true');
    
    // Toast 內容
    toast.innerHTML = `
        <div class="toast-header">
            <strong class="me-auto">${title}</strong>
            <button type="button" class="btn-close" data-bs-dismiss="toast" aria-label="Close"></button>
        </div>
        <div class="toast-body">
            ${message}
        </div>
    `;
    
    // 添加到容器
    toastContainer.appendChild(toast);
    
    // 初始化並顯示 Toast
    const bsToast = new bootstrap.Toast(toast);
    bsToast.show();
    
    // 自動移除
    toast.addEventListener('hidden.bs.toast', function() {
                    toast.remove();
    });
}

// 添加CSS動畫
const style = document.createElement('style');
style.textContent = `
    @keyframes slideInRight {
        from {
            transform: translateX(100%);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }
    
    @keyframes slideOutRight {
        from {
            transform: translateX(0);
            opacity: 1;
        }
        to {
            transform: translateX(100%);
            opacity: 0;
        }
    }
    
    .toast .material-icons {
        font-size: 18px !important;
    }
`;
document.head.appendChild(style); 