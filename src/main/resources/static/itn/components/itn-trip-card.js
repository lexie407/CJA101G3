/**
 * 行程卡片動態元件 v1.0.0 (Itn)
 * 可重複使用的行程卡片生成器
 * 
 * 使用方式：
 * const itnTripCards = new ItnTripCardComponent();
 * itnTripCards.render(containerSelector, data, options);
 * 
 * 或使用便利函數：
 * createItnTripCards('#container', data, options);
 */

class ItnTripCardComponent {
    constructor() {
        this.defaultOptions = {
            onRegisterClick: null,
            onDetailClick: null,
            onCardClick: null,
            enableCardClick: true,
            containerClass: 'itn-trip-cards-container',
            cardClass: 'itn-trip-card'
        };
        
        // 注入 CSS 樣式
        this.injectStyles();
    }

    /**
     * 渲染卡片
     * @param {string} containerSelector - 容器選擇器
     * @param {Array|Object} data - 行程資料（陣列或單個物件）
     * @param {Object} options - 配置選項
     */
    render(containerSelector, data, options = {}) {
        try {
            const container = document.querySelector(containerSelector);
            if (!container) {
                throw new Error(`找不到容器: ${containerSelector}`);
            }

            // 合併選項
            const config = { ...this.defaultOptions, ...options };
            
            // 確保資料是陣列格式
            const tripData = Array.isArray(data) ? data : [data];
            
            // 驗證資料格式
            this.validateData(tripData);
            
            // 清空容器並設置樣式
            container.innerHTML = '';
            container.className = config.containerClass;
            
            // 生成卡片
            tripData.forEach((trip, index) => {
                const cardElement = this.createCard(trip, index, config);
                container.appendChild(cardElement);
            });
            
            console.log(`✅ 成功渲染 ${tripData.length} 張行程卡片`);
            
        } catch (error) {
            console.error('❌ 渲染卡片時發生錯誤:', error);
            this.showError(containerSelector, error.message);
        }
    }

    /**
     * 從 API 載入資料並渲染
     * @param {string} containerSelector - 容器選擇器
     * @param {string} apiUrl - API 網址
     * @param {Object} options - 配置選項
     */
    async renderFromAPI(containerSelector, apiUrl, options = {}) {
        try {
            this.showLoading(containerSelector);
            
            const response = await fetch(apiUrl);
            if (!response.ok) {
                throw new Error(`API 請求失敗: ${response.status}`);
            }
            
            const data = await response.json();
            this.render(containerSelector, data, options);
            
        } catch (error) {
            console.error('❌ 從 API 載入資料時發生錯誤:', error);
            this.showError(containerSelector, `載入失敗: ${error.message}`);
        }
    }

    /**
     * 創建單張卡片
     * @param {Object} trip - 行程資料
     * @param {number} index - 卡片索引
     * @param {Object} config - 配置選項
     * @returns {HTMLElement} 卡片元素
     */
    createCard(trip, index, config) {
        const cardId = trip.id || `itn-trip-card-${index}`;
        
        const cardElement = document.createElement('div');
        cardElement.className = config.cardClass;
        cardElement.setAttribute('data-trip-id', cardId);
        cardElement.innerHTML = this.generateCardHTML(trip);
        
        // 綁定事件
        this.bindCardEvents(cardElement, trip, config);
        
        return cardElement;
    }

    /**
     * 生成卡片 HTML
     * @param {Object} trip - 行程資料
     * @returns {string} HTML 字符串
     */
    generateCardHTML(trip) {
        const headerStyle = trip.theme?.gradient 
            ? `background: ${trip.theme.gradient};`
            : 'background: linear-gradient(135deg, #4285f4 0%, #9c27b0 100%);';
            
        const markerColor = trip.theme?.primaryColor || '#2196f3';
        const buttonStyle = trip.theme?.primaryColor 
            ? `background: ${trip.theme.primaryColor};`
            : '';

        return `
            <div class="card-header" style="${headerStyle}">
                <div class="trip-title">${this.escapeHtml(trip.title)}</div>
                <div class="trip-date">
                    <span class="date-icon">📅</span>
                    ${this.formatDate(trip.date)}
                </div>
                <div class="rating">
                    <span class="star">⭐</span>
                    ${trip.rating || '5.0'}
                </div>
            </div>
            
            <div class="card-body">
                <div class="meta-info">
                    <div class="meta-left">
                        <div class="meta-item">
                            <span class="meta-icon">🕐</span>
                            ${trip.duration}
                        </div>
                        <div class="meta-item">
                            <span class="meta-icon">👥</span>
                            ${trip.groupSize}
                        </div>
                    </div>
                    <div class="price">NT$ ${trip.price.toLocaleString()}</div>
                </div>

                <div class="itinerary">
                    ${trip.itinerary.map(item => this.generateItineraryItem(item, markerColor)).join('')}
                </div>

                <div class="action-buttons">
                    <button class="btn btn-primary" style="${buttonStyle}" data-action="register">
                        ${trip.buttons?.register || '立即報名'}
                    </button>
                    <button class="btn btn-secondary" data-action="detail">
                        ${trip.buttons?.detail || '查看詳情'}
                    </button>
                </div>
            </div>
        `;
    }

    /**
     * 生成行程項目 HTML
     * @param {Object} item - 行程項目
     * @param {string} markerColor - 標記顏色
     * @returns {string} HTML 字符串
     */
    generateItineraryItem(item, markerColor) {
        const categoryClass = this.getCategoryClass(item.category);
        
        return `
            <div class="itinerary-item">
                <div class="time-marker" style="background: ${markerColor};"></div>
                <div class="itinerary-content">
                    <div class="itinerary-header">
                        <div class="time">${item.time}</div>
                        <div class="duration">${item.duration}</div>
                    </div>
                    <div class="location-name">${this.escapeHtml(item.name)}</div>
                    <div class="location-detail">
                        <div class="location-address">
                            <span class="location-icon">📍</span>
                            ${this.escapeHtml(item.location)}
                        </div>
                        <div class="category-tag ${categoryClass}">${item.category}</div>
                    </div>
                </div>
            </div>
        `;
    }

    /**
     * 綁定卡片事件
     * @param {HTMLElement} cardElement - 卡片元素
     * @param {Object} trip - 行程資料
     * @param {Object} config - 配置選項
     */
    bindCardEvents(cardElement, trip, config) {
        // 按鈕點擊事件
        const registerBtn = cardElement.querySelector('[data-action="register"]');
        const detailBtn = cardElement.querySelector('[data-action="detail"]');
        
        if (registerBtn) {
            registerBtn.addEventListener('click', (e) => {
                e.stopPropagation();
                if (config.onRegisterClick) {
                    config.onRegisterClick(trip, cardElement);
                } else {
                    console.log('報名按鈕被點擊:', trip);
                }
            });
        }
        
        if (detailBtn) {
            detailBtn.addEventListener('click', (e) => {
                e.stopPropagation();
                if (config.onDetailClick) {
                    config.onDetailClick(trip, cardElement);
                } else {
                    console.log('詳情按鈕被點擊:', trip);
                }
            });
        }
        
        // 卡片點擊事件
        if (config.enableCardClick) {
            cardElement.addEventListener('click', (e) => {
                if (!e.target.classList.contains('btn')) {
                    if (config.onCardClick) {
                        config.onCardClick(trip, cardElement);
                    } else {
                        console.log('卡片被點擊:', trip);
                    }
                }
            });
        }
    }

    /**
     * 驗證資料格式
     * @param {Array} data - 行程資料陣列
     */
    validateData(data) {
        if (!Array.isArray(data) || data.length === 0) {
            throw new Error('資料必須是非空陣列');
        }
        
        data.forEach((trip, index) => {
            // 驗證必要欄位
            const required = ['title', 'date', 'duration', 'groupSize', 'price', 'itinerary'];
            const missing = required.filter(field => !trip[field]);
            
            if (missing.length > 0) {
                throw new Error(`第 ${index + 1} 筆資料缺少必要欄位: ${missing.join(', ')}`);
            }
            
            // 驗證資料類型
            if (typeof trip.title !== 'string' || trip.title.trim() === '') {
                throw new Error(`第 ${index + 1} 筆資料的標題不能為空`);
            }
            
            if (typeof trip.price !== 'number' || trip.price < 0) {
                throw new Error(`第 ${index + 1} 筆資料的價格必須是正數`);
            }
            
            if (!Array.isArray(trip.itinerary) || trip.itinerary.length === 0) {
                throw new Error(`第 ${index + 1} 筆資料的行程安排必須是非空陣列`);
            }
            
            // 驗證行程項目
            trip.itinerary.forEach((item, itemIndex) => {
                const itemRequired = ['time', 'duration', 'name', 'location', 'category'];
                const itemMissing = itemRequired.filter(field => !item[field]);
                
                if (itemMissing.length > 0) {
                    throw new Error(`第 ${index + 1} 筆資料的第 ${itemIndex + 1} 個行程項目缺少欄位: ${itemMissing.join(', ')}`);
                }
                
                // 驗證時間格式
                if (!this.isValidTimeFormat(item.time)) {
                    throw new Error(`第 ${index + 1} 筆資料的第 ${itemIndex + 1} 個行程項目時間格式錯誤: ${item.time}`);
                }
                
                // 驗證分類
                const validCategories = ['文化景點', '美食', '自然景觀', '購物', '娛樂'];
                if (!validCategories.includes(item.category)) {
                    throw new Error(`第 ${index + 1} 筆資料的第 ${itemIndex + 1} 個行程項目分類無效: ${item.category}`);
                }
            });
            
            // 驗證評分範圍
            if (trip.rating !== undefined) {
                if (typeof trip.rating !== 'number' || trip.rating < 0 || trip.rating > 5) {
                    throw new Error(`第 ${index + 1} 筆資料的評分必須在 0-5 之間`);
                }
            }
        });
    }

    /**
     * 驗證時間格式 (HH:MM)
     * @param {string} time - 時間字符串
     * @returns {boolean} 是否有效
     */
    isValidTimeFormat(time) {
        const timeRegex = /^([01]?[0-9]|2[0-3]):[0-5][0-9]$/;
        return timeRegex.test(time);
    }

    /**
     * 獲取分類樣式類別
     * @param {string} category - 分類名稱
     * @returns {string} CSS 類別
     */
    getCategoryClass(category) {
        const categoryMap = {
            '文化景點': 'category-culture',
            '美食': 'category-food',
            '自然景觀': 'category-nature',
            '購物': 'category-shopping',
            '娛樂': 'category-entertainment'
        };
        return categoryMap[category] || 'category-culture';
    }

    /**
     * 格式化日期
     * @param {string|Date} date - 日期
     * @returns {string} 格式化後的日期字符串
     */
    formatDate(date) {
        if (typeof date === 'string') return date;
        
        const d = new Date(date);
        return d.toLocaleDateString('zh-TW', {
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });
    }

    /**
     * HTML 轉義
     * @param {string} text - 原始文字
     * @returns {string} 轉義後的文字
     */
    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    /**
     * 顯示載入中狀態
     * @param {string} containerSelector - 容器選擇器
     */
    showLoading(containerSelector) {
        const container = document.querySelector(containerSelector);
        if (container) {
            container.innerHTML = `
                <div class="itn-trip-card-loading">
                    <div class="loading-spinner"></div>
                    <p>載入中...</p>
                </div>
            `;
        }
    }

    /**
     * 顯示錯誤訊息
     * @param {string} containerSelector - 容器選擇器
     * @param {string} message - 錯誤訊息
     */
    showError(containerSelector, message) {
        const container = document.querySelector(containerSelector);
        if (container) {
            container.innerHTML = `
                <div class="itn-trip-card-error">
                    <div class="error-icon">⚠️</div>
                    <p>載入失敗</p>
                    <small>${this.escapeHtml(message)}</small>
                </div>
            `;
        }
    }

    /**
     * 更新特定卡片
     * @param {string} tripId - 行程ID
     * @param {Object} newData - 新的行程資料
     * @param {Object} options - 配置選項
     */
    updateCard(tripId, newData, options = {}) {
        try {
            const cardElement = document.querySelector(`[data-trip-id="${tripId}"]`);
            if (!cardElement) {
                throw new Error(`找不到行程卡片: ${tripId}`);
            }
            
            const config = { ...this.defaultOptions, ...options };
            const updatedCard = this.createCard(newData, 0, config);
            
            cardElement.parentNode.replaceChild(updatedCard, cardElement);
            console.log(`✅ 成功更新行程卡片: ${tripId}`);
            
        } catch (error) {
            console.error('❌ 更新卡片時發生錯誤:', error);
        }
    }

    /**
     * 重新渲染所有卡片
     * @param {string} containerSelector - 容器選擇器
     * @param {Array} data - 行程資料
     * @param {Object} options - 配置選項
     */
    reRender(containerSelector, data, options = {}) {
        this.render(containerSelector, data, options);
    }

    /**
     * 獲取所有卡片資料
     * @param {string} containerSelector - 容器選擇器
     * @returns {Array} 卡片資料陣列
     */
    getCardsData(containerSelector) {
        const container = document.querySelector(containerSelector);
        if (!container) return [];
        
        const cards = container.querySelectorAll('.itn-trip-card');
        return Array.from(cards).map(card => {
            return {
                id: card.getAttribute('data-trip-id'),
                element: card
            };
        });
    }

    /**
     * 篩選卡片
     * @param {string} containerSelector - 容器選擇器
     * @param {Function} filterFunction - 篩選函數
     */
    filterCards(containerSelector, filterFunction) {
        const container = document.querySelector(containerSelector);
        if (!container) return;
        
        const cards = container.querySelectorAll('.itn-trip-card');
        cards.forEach(card => {
            const tripId = card.getAttribute('data-trip-id');
            const shouldShow = filterFunction(tripId, card);
            card.style.display = shouldShow ? 'block' : 'none';
        });
    }

    /**
     * 排序卡片
     * @param {string} containerSelector - 容器選擇器
     * @param {string} sortBy - 排序依據 ('price', 'rating', 'date')
     * @param {string} order - 排序順序 ('asc', 'desc')
     */
    sortCards(containerSelector, sortBy = 'price', order = 'asc') {
        const container = document.querySelector(containerSelector);
        if (!container) return;
        
        const cards = Array.from(container.querySelectorAll('.itn-trip-card'));
        
        cards.sort((a, b) => {
            let aValue, bValue;
            
            switch (sortBy) {
                case 'price':
                    aValue = parseFloat(a.querySelector('.price').textContent.replace(/[^\d]/g, ''));
                    bValue = parseFloat(b.querySelector('.price').textContent.replace(/[^\d]/g, ''));
                    break;
                case 'rating':
                    aValue = parseFloat(a.querySelector('.rating').textContent);
                    bValue = parseFloat(b.querySelector('.rating').textContent);
                    break;
                case 'date':
                    aValue = new Date(a.querySelector('.trip-date').textContent);
                    bValue = new Date(b.querySelector('.trip-date').textContent);
                    break;
                default:
                    return 0;
            }
            
            if (order === 'asc') {
                return aValue > bValue ? 1 : -1;
            } else {
                return aValue < bValue ? 1 : -1;
            }
        });
        
        // 重新排列卡片
        cards.forEach(card => container.appendChild(card));
    }

    /**
     * 銷毀組件
     * @param {string} containerSelector - 容器選擇器
     */
    destroy(containerSelector) {
        const container = document.querySelector(containerSelector);
        if (container) {
            container.innerHTML = '';
            console.log(`✅ 成功銷毀行程卡片組件: ${containerSelector}`);
        }
    }

    /**
     * 注入 CSS 樣式
     */
    injectStyles() {
        if (document.getElementById('itn-trip-card-styles')) return;
        
        const style = document.createElement('style');
        style.id = 'itn-trip-card-styles';
        style.textContent = `
            .itn-trip-cards-container {
                display: grid;
                grid-template-columns: repeat(auto-fit, minmax(320px, 400px));
                gap: 20px;
                max-width: 1200px;
                width: 100%;
                margin: 0 auto;
            }

            .itn-trip-card {
                background: white;
                border-radius: 16px;
                box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
                overflow: hidden;
                transition: all 0.3s ease;
                cursor: pointer;
            }

            .itn-trip-card:hover {
                transform: translateY(-2px);
                box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
            }

            .card-header {
                background: linear-gradient(135deg, #4285f4 0%, #9c27b0 100%);
                color: white;
                padding: 20px;
                position: relative;
            }

            .trip-title {
                font-size: 18px;
                font-weight: 600;
                margin-bottom: 8px;
                line-height: 1.3;
            }

            .trip-date {
                font-size: 14px;
                opacity: 0.9;
                display: flex;
                align-items: center;
            }

            .date-icon {
                margin-right: 6px;
            }

            .rating {
                position: absolute;
                top: 20px;
                right: 20px;
                display: flex;
                align-items: center;
                font-size: 14px;
                font-weight: 600;
            }

            .star {
                color: #ffd700;
                margin-right: 4px;
            }

            .card-body {
                padding: 20px;
            }

            .meta-info {
                display: flex;
                justify-content: space-between;
                align-items: center;
                margin-bottom: 20px;
                padding: 12px 0;
            }

            .meta-left {
                display: flex;
                align-items: center;
                gap: 16px;
            }

            .meta-item {
                display: flex;
                align-items: center;
                font-size: 14px;
                color: #666;
            }

            .meta-icon {
                margin-right: 4px;
                font-size: 14px;
            }

            .price {
                color: #00c853;
                font-weight: 700;
                font-size: 18px;
            }

            .itinerary {
                margin-bottom: 20px;
            }

            .itinerary-item {
                display: flex;
                margin-bottom: 16px;
                position: relative;
            }

            .itinerary-item:not(:last-child)::after {
                content: "";
                position: absolute;
                left: 8px;
                top: 28px;
                width: 2px;
                height: 24px;
                background: #e3f2fd;
            }

            .time-marker {
                width: 16px;
                height: 16px;
                border-radius: 50%;
                background: #2196f3;
                margin-right: 16px;
                margin-top: 4px;
                flex-shrink: 0;
            }

            .itinerary-content {
                flex: 1;
            }

            .itinerary-header {
                display: flex;
                justify-content: space-between;
                align-items: flex-start;
                margin-bottom: 4px;
            }

            .time {
                font-size: 14px;
                font-weight: 600;
                color: #333;
            }

            .duration {
                font-size: 12px;
                color: #999;
            }

            .location-name {
                font-size: 14px;
                color: #333;
                font-weight: 500;
                margin-bottom: 4px;
            }

            .location-detail {
                display: flex;
                justify-content: space-between;
                align-items: center;
            }

            .location-address {
                font-size: 12px;
                color: #666;
                display: flex;
                align-items: center;
            }

            .location-icon {
                margin-right: 4px;
                font-size: 10px;
            }

            .category-tag {
                font-size: 11px;
                padding: 3px 8px;
                border-radius: 12px;
                font-weight: 500;
            }

            .category-culture {
                background: #e8f5e8;
                color: #2e7d32;
            }

            .category-food {
                background: #fff3e0;
                color: #f57c00;
            }

            .category-nature {
                background: #e0f2f1;
                color: #00695c;
            }

            .category-shopping {
                background: #fce4ec;
                color: #ad1457;
            }

            .category-entertainment {
                background: #f3e5f5;
                color: #7b1fa2;
            }

            .action-buttons {
                display: flex;
                gap: 12px;
            }

            .btn {
                flex: 1;
                padding: 14px 20px;
                border-radius: 12px;
                font-size: 14px;
                font-weight: 600;
                border: none;
                cursor: pointer;
                transition: all 0.2s ease;
            }

            .btn-primary {
                background: #2196f3;
                color: white;
            }

            .btn-primary:hover {
                background: #1976d2;
                transform: translateY(-1px);
            }

            .btn-secondary {
                background: #f5f5f5;
                color: #666;
            }

            .btn-secondary:hover {
                background: #e0e0e0;
            }

            .itn-trip-card-loading,
            .itn-trip-card-error {
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: center;
                padding: 40px 20px;
                text-align: center;
                color: #666;
                grid-column: 1 / -1;
            }

            .loading-spinner {
                width: 40px;
                height: 40px;
                border: 4px solid #f3f3f3;
                border-top: 4px solid #2196f3;
                border-radius: 50%;
                animation: spin 1s linear infinite;
                margin-bottom: 16px;
            }

            .error-icon {
                font-size: 48px;
                margin-bottom: 16px;
            }

            @keyframes spin {
                0% { transform: rotate(0deg); }
                100% { transform: rotate(360deg); }
            }

            @media (max-width: 768px) {
                .itn-trip-cards-container {
                    grid-template-columns: 1fr;
                    padding: 10px;
                }
                
                .meta-info {
                    flex-direction: column;
                    align-items: flex-start;
                    gap: 12px;
                }
                
                .meta-left {
                    width: 100%;
                }
                
                .price {
                    align-self: flex-end;
                }
            }
        `;
        
        document.head.appendChild(style);
    }
}

// 全域可用
window.ItnTripCardComponent = ItnTripCardComponent;

// 便利函數
window.createItnTripCards = function(containerSelector, data, options) {
    const component = new ItnTripCardComponent();
    component.render(containerSelector, data, options);
    return component;
};

window.loadItnTripCardsFromAPI = async function(containerSelector, apiUrl, options) {
    const component = new ItnTripCardComponent();
    await component.renderFromAPI(containerSelector, apiUrl, options);
    return component;
};

// 新增便利函數
window.updateItnTripCard = function(tripId, newData, options) {
    const component = new ItnTripCardComponent();
    component.updateCard(tripId, newData, options);
};

window.sortItnTripCards = function(containerSelector, sortBy, order) {
    const component = new ItnTripCardComponent();
    component.sortCards(containerSelector, sortBy, order);
};

window.filterItnTripCards = function(containerSelector, filterFunction) {
    const component = new ItnTripCardComponent();
    component.filterCards(containerSelector, filterFunction);
};

window.destroyItnTripCards = function(containerSelector) {
    const component = new ItnTripCardComponent();
    component.destroy(containerSelector);
};

// 全域實例管理
window.ItnTripCardInstances = new Map();

// 創建並管理實例
window.createItnTripCardInstance = function(instanceId, containerSelector, data, options) {
    const component = new ItnTripCardComponent();
    component.render(containerSelector, data, options);
    window.ItnTripCardInstances.set(instanceId, component);
    return component;
};

// 獲取實例
window.getItnTripCardInstance = function(instanceId) {
    return window.ItnTripCardInstances.get(instanceId);
};

// 銷毀實例
window.destroyItnTripCardInstance = function(instanceId) {
    const component = window.ItnTripCardInstances.get(instanceId);
    if (component) {
        component.destroy();
        window.ItnTripCardInstances.delete(instanceId);
    }
}; 