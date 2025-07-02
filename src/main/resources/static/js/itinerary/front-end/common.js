/**
 * 行程模組共用 JavaScript 工具
 * 提供通用的功能和工具函數
 */

/**
 * Toast 提示系統
 */
class Toast {
    constructor() {
        this.container = null;
        this.init();
    }
    
    init() {
        // 建立 toast 容器
        this.container = document.createElement('div');
        this.container.className = 'toast-container';
        this.container.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            z-index: 9999;
            pointer-events: none;
        `;
        document.body.appendChild(this.container);
    }
    
    show(message, type = 'info', duration = 3000) {
        const toast = document.createElement('div');
        toast.className = `toast toast--${type}`;
        toast.innerHTML = `
            <div class="toast__content">
                <span class="material-icons">${this.getIcon(type)}</span>
                <span>${message}</span>
            </div>
        `;
        
        // 添加到容器
        this.container.appendChild(toast);
        
        // 顯示動畫
        setTimeout(() => {
            toast.classList.add('toast--show');
        }, 100);
        
        // 自動隱藏
        setTimeout(() => {
            toast.classList.remove('toast--show');
            setTimeout(() => {
                if (toast.parentNode) {
                    toast.parentNode.removeChild(toast);
                }
            }, 300);
        }, duration);
    }
    
    getIcon(type) {
        switch (type) {
            case 'success': return 'check_circle';
            case 'error': return 'error';
            case 'warning': return 'warning';
            default: return 'info';
        }
    }
}

// 全域 toast 實例
window.toast = new Toast();

/**
 * 表單驗證工具
 */
class FormValidator {
    constructor(form) {
        this.form = form;
        this.errors = [];
        this.init();
    }
    
    init() {
        this.bindEvents();
    }
    
    bindEvents() {
        const inputs = this.form.querySelectorAll('input, textarea, select');
        inputs.forEach(input => {
            input.addEventListener('blur', () => this.validateField(input));
            input.addEventListener('input', () => this.clearFieldError(input));
        });
    }
    
    validateField(field) {
        const value = field.value.trim();
        const fieldName = field.name;
        
        // 清除之前的錯誤
        this.clearFieldError(field);
        
        // 驗證規則
        const rules = this.getValidationRules(fieldName);
        for (const rule of rules) {
            if (!rule.test(value)) {
                this.showFieldError(field, rule.message);
                return false;
            }
        }
        
        return true;
    }
    
    getValidationRules(fieldName) {
        const rules = {
            'itnName': [
                {
                    test: (value) => value.length > 0,
                    message: '請輸入行程名稱'
                },
                {
                    test: (value) => value.length <= 100,
                    message: '行程名稱不能超過100字'
                }
            ],
            'itnDesc': [
                {
                    test: (value) => value.length <= 500,
                    message: '行程描述不能超過500字'
                }
            ]
        };
        
        return rules[fieldName] || [];
    }
    
    showFieldError(field, message) {
        field.classList.add('error');
        
        const errorElement = document.createElement('div');
        errorElement.className = 'field-error';
        errorElement.textContent = message;
        
        field.parentNode.appendChild(errorElement);
    }
    
    clearFieldError(field) {
        field.classList.remove('error');
        
        const errorElement = field.parentNode.querySelector('.field-error');
        if (errorElement) {
            errorElement.remove();
        }
    }
    
    validate() {
        this.errors = [];
        const inputs = this.form.querySelectorAll('input, textarea, select');
        
        inputs.forEach(input => {
            if (!this.validateField(input)) {
                this.errors.push(input.name);
            }
        });
        
        return this.errors.length === 0;
    }
    
    getErrors() {
        return this.errors;
    }
}

/**
 * API 請求工具
 */
class ApiClient {
    constructor(baseURL = '') {
        this.baseURL = baseURL;
    }
    
    async request(url, options = {}) {
        const fullURL = this.baseURL + url;
        
        const defaultOptions = {
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            }
        };
        
        const finalOptions = { ...defaultOptions, ...options };
        
        try {
            const response = await fetch(fullURL, finalOptions);
            const data = await response.json();
            
            if (!response.ok) {
                throw new Error(data.message || '請求失敗');
            }
            
            return data;
        } catch (error) {
            console.error('API 請求失敗:', error);
            throw error;
        }
    }
    
    async get(url, params = {}) {
        const queryString = new URLSearchParams(params).toString();
        const fullURL = queryString ? `${url}?${queryString}` : url;
        
        return this.request(fullURL, { method: 'GET' });
    }
    
    async post(url, data = {}) {
        return this.request(url, {
            method: 'POST',
            body: JSON.stringify(data)
        });
    }
    
    async put(url, data = {}) {
        return this.request(url, {
            method: 'PUT',
            body: JSON.stringify(data)
        });
    }
    
    async delete(url) {
        return this.request(url, { method: 'DELETE' });
    }
}

// 全域 API 客戶端
window.apiClient = new ApiClient('/api');

/**
 * 日期格式化工具
 */
class DateFormatter {
    static format(date, format = 'YYYY-MM-DD') {
        if (!date) return '';
        
        const d = new Date(date);
        if (isNaN(d.getTime())) return '';
        
        const year = d.getFullYear();
        const month = String(d.getMonth() + 1).padStart(2, '0');
        const day = String(d.getDate()).padStart(2, '0');
        const hours = String(d.getHours()).padStart(2, '0');
        const minutes = String(d.getMinutes()).padStart(2, '0');
        const seconds = String(d.getSeconds()).padStart(2, '0');
        
        return format
            .replace('YYYY', year)
            .replace('MM', month)
            .replace('DD', day)
            .replace('HH', hours)
            .replace('mm', minutes)
            .replace('ss', seconds);
    }
    
    static formatRelative(date) {
        if (!date) return '';
        
        const now = new Date();
        const target = new Date(date);
        const diff = now - target;
        
        const minutes = Math.floor(diff / (1000 * 60));
        const hours = Math.floor(diff / (1000 * 60 * 60));
        const days = Math.floor(diff / (1000 * 60 * 60 * 24));
        
        if (minutes < 1) return '剛剛';
        if (minutes < 60) return `${minutes}分鐘前`;
        if (hours < 24) return `${hours}小時前`;
        if (days < 7) return `${days}天前`;
        
        return this.format(date, 'YYYY-MM-DD');
    }
    
    static formatChinese(date) {
        if (!date) return '';
        
        const d = new Date(date);
        if (isNaN(d.getTime())) return '';
        
        return d.toLocaleDateString('zh-TW');
    }
}

/**
 * 字串工具
 */
class StringUtils {
    static truncate(str, length = 100, suffix = '...') {
        if (!str || str.length <= length) return str;
        return str.substring(0, length) + suffix;
    }
    
    static escapeHtml(str) {
        if (!str) return '';
        
        const div = document.createElement('div');
        div.textContent = str;
        return div.innerHTML;
    }
    
    static unescapeHtml(str) {
        if (!str) return '';
        
        const div = document.createElement('div');
        div.innerHTML = str;
        return div.textContent;
    }
}

/**
 * 本地儲存工具
 */
class Storage {
    static set(key, value) {
        try {
            localStorage.setItem(key, JSON.stringify(value));
        } catch (error) {
            console.error('儲存到 localStorage 失敗:', error);
        }
    }
    
    static get(key, defaultValue = null) {
        try {
            const item = localStorage.getItem(key);
            return item ? JSON.parse(item) : defaultValue;
        } catch (error) {
            console.error('從 localStorage 讀取失敗:', error);
            return defaultValue;
        }
    }
    
    static remove(key) {
        try {
            localStorage.removeItem(key);
        } catch (error) {
            console.error('從 localStorage 刪除失敗:', error);
        }
    }
    
    static clear() {
        try {
            localStorage.clear();
        } catch (error) {
            console.error('清空 localStorage 失敗:', error);
        }
    }
}

/**
 * 工具函數
 */
class Utils {
    /**
     * 防抖函數
     */
    static debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    }
    
    /**
     * 節流函數
     */
    static throttle(func, limit) {
        let inThrottle;
        return function() {
            const args = arguments;
            const context = this;
            if (!inThrottle) {
                func.apply(context, args);
                inThrottle = true;
                setTimeout(() => inThrottle = false, limit);
            }
        };
    }
    
    /**
     * 複製到剪貼簿
     */
    static async copyToClipboard(text) {
        try {
            await navigator.clipboard.writeText(text);
            return true;
        } catch (error) {
            console.error('複製到剪貼簿失敗:', error);
            return false;
        }
    }
    
    /**
     * 檢查是否為行動裝置
     */
    static isMobile() {
        return /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);
    }
    
    /**
     * 檢查是否為觸控裝置
     */
    static isTouchDevice() {
        return 'ontouchstart' in window || navigator.maxTouchPoints > 0;
    }
    
    /**
     * 生成唯一 ID
     */
    static generateId() {
        return Date.now().toString(36) + Math.random().toString(36).substr(2);
    }
    
    /**
     * 格式化檔案大小
     */
    static formatFileSize(bytes) {
        if (bytes === 0) return '0 Bytes';
        
        const k = 1024;
        const sizes = ['Bytes', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    }
}

// 全域工具函數
window.FormValidator = FormValidator;
window.DateFormatter = DateFormatter;
window.StringUtils = StringUtils;
window.Storage = Storage;
window.Utils = Utils;

// 簡化的全域函數
window.showToast = (message, type = 'info') => window.toast.show(message, type);
window.formatDate = (date) => DateFormatter.formatChinese(date);
window.formatRelativeDate = (date) => DateFormatter.formatRelative(date);
window.truncateText = (text, length) => StringUtils.truncate(text, length);
window.copyToClipboard = (text) => Utils.copyToClipboard(text);
window.isMobile = () => Utils.isMobile();
window.generateId = () => Utils.generateId(); 