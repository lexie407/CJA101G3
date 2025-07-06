/**
 * 行程卡片自動載入器
 * 自動從行程 API 抓取資料並渲染行程卡片
 * 
 * 使用方式：
 * ItnTripCardLoader.loadCards('#container', options);
 */

class ItnTripCardLoader {
    constructor() {
        this.baseUrl = '/api/itinerary/cards';
        this.defaultOptions = {
            limit: 10,
            keyword: null,
            isPublic: true,
            autoRefresh: false,
            refreshInterval: 30000, // 30秒
            onLoadStart: null,
            onLoadSuccess: null,
            onLoadError: null,
            cardOptions: {
                onRegisterClick: null,
                onDetailClick: null,
                onCardClick: null,
                enableCardClick: true
            }
        };
    }

    /**
     * 載入行程卡片
     * @param {string} containerSelector - 容器選擇器
     * @param {Object} options - 配置選項
     */
    async loadCards(containerSelector, options = {}) {
        const config = { ...this.defaultOptions, ...options };
        
        try {
            // 觸發載入開始回調
            if (config.onLoadStart) {
                config.onLoadStart();
            }

            // 顯示載入狀態
            this.showLoading(containerSelector);

            // 構建 API URL
            const url = this.buildApiUrl(config);

            // 發送請求
            const response = await fetch(url);
            
            if (!response.ok) {
                throw new Error(`API 請求失敗: ${response.status}`);
            }

            const result = await response.json();

            if (!result.success) {
                throw new Error(result.message || '載入失敗');
            }

            // 渲染卡片
            if (window.createItnTripCards) {
                window.createItnTripCards(containerSelector, result.data, config.cardOptions);
            } else {
                throw new Error('行程卡片元件未載入');
            }

            // 觸發載入成功回調
            if (config.onLoadSuccess) {
                config.onLoadSuccess(result.data, result.total);
            }

            console.log(`✅ 成功載入 ${result.total} 個行程卡片`);

            // 如果啟用自動刷新
            if (config.autoRefresh) {
                this.startAutoRefresh(containerSelector, config);
            }

        } catch (error) {
            console.error('❌ 載入行程卡片時發生錯誤:', error);
            
            // 顯示錯誤狀態
            this.showError(containerSelector, error.message);
            
            // 觸發載入錯誤回調
            if (config.onLoadError) {
                config.onLoadError(error);
            }
        }
    }

    /**
     * 構建 API URL
     * @param {Object} config - 配置選項
     * @returns {string} API URL
     */
    buildApiUrl(config) {
        const params = new URLSearchParams();
        
        if (config.limit) {
            params.append('limit', config.limit);
        }
        
        if (config.keyword) {
            params.append('keyword', config.keyword);
        }
        
        if (config.isPublic !== null) {
            params.append('isPublic', config.isPublic);
        }

        const queryString = params.toString();
        return queryString ? `${this.baseUrl}?${queryString}` : this.baseUrl;
    }

    /**
     * 搜尋行程
     * @param {string} containerSelector - 容器選擇器
     * @param {string} keyword - 搜尋關鍵字
     * @param {Object} options - 配置選項
     */
    async searchCards(containerSelector, keyword, options = {}) {
        const searchOptions = { ...options, keyword };
        await this.loadCards(containerSelector, searchOptions);
    }

    /**
     * 篩選公開/私人行程
     * @param {string} containerSelector - 容器選擇器
     * @param {boolean} isPublic - 是否公開
     * @param {Object} options - 配置選項
     */
    async filterByPublicStatus(containerSelector, isPublic, options = {}) {
        const filterOptions = { ...options, isPublic };
        await this.loadCards(containerSelector, filterOptions);
    }

    /**
     * 重新載入
     * @param {string} containerSelector - 容器選擇器
     * @param {Object} options - 配置選項
     */
    async reload(containerSelector, options = {}) {
        await this.loadCards(containerSelector, options);
    }

    /**
     * 開始自動刷新
     * @param {string} containerSelector - 容器選擇器
     * @param {Object} config - 配置選項
     */
    startAutoRefresh(containerSelector, config) {
        // 清除現有的定時器
        if (this.refreshTimer) {
            clearInterval(this.refreshTimer);
        }

        // 設定新的定時器
        this.refreshTimer = setInterval(() => {
            this.loadCards(containerSelector, config);
        }, config.refreshInterval);

        console.log(`🔄 已啟用自動刷新，間隔: ${config.refreshInterval}ms`);
    }

    /**
     * 停止自動刷新
     */
    stopAutoRefresh() {
        if (this.refreshTimer) {
            clearInterval(this.refreshTimer);
            this.refreshTimer = null;
            console.log('⏹️ 已停止自動刷新');
        }
    }

    /**
     * 顯示載入狀態
     * @param {string} containerSelector - 容器選擇器
     */
    showLoading(containerSelector) {
        const container = document.querySelector(containerSelector);
        if (container) {
            container.innerHTML = `
                <div class="itn-trip-card-loading">
                    <div class="loading-spinner"></div>
                    <p>載入行程資料中...</p>
                </div>
            `;
        }
    }

    /**
     * 顯示錯誤狀態
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
                    <button onclick="ItnTripCardLoader.reload('${containerSelector}')" 
                            style="margin-top: 10px; padding: 8px 16px; background: #2196f3; color: white; border: none; border-radius: 4px; cursor: pointer;">
                        重新載入
                    </button>
                </div>
            `;
        }
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
     * 獲取統計資訊
     * @returns {Promise<Object>} 統計資訊
     */
    async getStats() {
        try {
            const response = await fetch('/api/itinerary/stats');
            if (response.ok) {
                return await response.json();
            }
        } catch (error) {
            console.error('取得統計資訊失敗:', error);
        }
        return null;
    }
}

// 全域實例
window.ItnTripCardLoader = new ItnTripCardLoader();

// 便利函數
window.loadItineraryCards = function(containerSelector, options) {
    return window.ItnTripCardLoader.loadCards(containerSelector, options);
};

window.searchItineraryCards = function(containerSelector, keyword, options) {
    return window.ItnTripCardLoader.searchCards(containerSelector, keyword, options);
};

window.filterItineraryCards = function(containerSelector, isPublic, options) {
    return window.ItnTripCardLoader.filterByPublicStatus(containerSelector, isPublic, options);
};

window.reloadItineraryCards = function(containerSelector, options) {
    return window.ItnTripCardLoader.reload(containerSelector, options);
}; 