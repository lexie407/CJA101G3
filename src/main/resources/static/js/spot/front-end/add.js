/**
 * 新增景點頁面 JavaScript - 簡化版
 * 專注於表單驗證功能
 */

(function() {
    'use strict';

    console.log('add.js 開始載入');

    /**
     * 主要模組物件
     */
    const SpotAdd = {
        // DOM 元素快取
        elements: {},

        /**
         * 初始化
         */
        init() {
            console.log('SpotAdd.init() 開始執行');
            
            this.cacheElements();
            this.bindEvents();
            this.setupAddressAutoComplete();
            
            console.log('SpotAdd 初始化完成');
        },

        /**
         * 快取 DOM 元素
         */
        cacheElements() {
            console.log('開始快取 DOM 元素');
            
            this.elements = {
                form: document.getElementById('spotAddForm'),
                spotName: document.getElementById('spotName'),
                spotLoc: document.getElementById('spotLoc'),
                spotDesc: document.getElementById('spotDesc'),
                region: document.getElementById('region'),
                tel: document.getElementById('tel'),
                website: document.getElementById('website'),
                descCharCount: document.getElementById('descCharCount'),
                spotImg: document.getElementById('spotImg'),
                uploadArea: document.getElementById('uploadArea'),
                imagePreview: document.getElementById('imagePreview'),
                spotNameError: document.getElementById('spotNameError'),
                spotLocError: document.getElementById('spotLocError'),
                spotDescError: document.getElementById('spotDescError'),
                regionError: document.getElementById('regionError'),
                telError: document.getElementById('telError'),
                websiteError: document.getElementById('websiteError'),
                spotImgError: document.getElementById('spotImgError')
            };

            // 除錯：檢查元素是否正確載入
            console.log('DOM 元素快取狀態：');
            console.log('表單:', this.elements.form ? '✓' : '✗');
            console.log('景點名稱:', this.elements.spotName ? '✓' : '✗');
            console.log('景點地址:', this.elements.spotLoc ? '✓' : '✗');
            console.log('景點描述:', this.elements.spotDesc ? '✓' : '✗');
            console.log('地區選擇:', this.elements.region ? '✓' : '✗');
            console.log('聯絡電話:', this.elements.tel ? '✓' : '✗');
            console.log('官方網站:', this.elements.website ? '✓' : '✗');
            console.log('字數統計:', this.elements.descCharCount ? '✓' : '✗');
            console.log('地址錯誤元素:', this.elements.spotLocError ? '✓' : '✗');
            console.log('地區錯誤元素:', this.elements.regionError ? '✓' : '✗');
        },

        /**
         * 綁定事件
         */
        bindEvents() {
            console.log('開始綁定事件');
            
            // 表單提交事件
            if (this.elements.form) {
                this.elements.form.addEventListener('submit', (e) => {
                    console.log('表單提交事件觸發');
                    this.handleFormSubmit(e);
                });
            }

            // 景點名稱即時驗證
            if (this.elements.spotName) {
                this.elements.spotName.addEventListener('input', () => {
                    console.log('景點名稱輸入事件觸發');
                    this.validateSpotName();
                });
                
                this.elements.spotName.addEventListener('blur', () => {
                    console.log('景點名稱失焦事件觸發');
                    this.validateSpotName();
                });
            }

            // 地址欄位即時驗證
            if (this.elements.spotLoc) {
                this.elements.spotLoc.addEventListener('input', () => {
                    console.log('地址欄位輸入事件觸發');
                    this.validateSpotLoc();
                });
                
                this.elements.spotLoc.addEventListener('blur', () => {
                    console.log('地址欄位失焦事件觸發');
                    this.validateSpotLoc();
                });
            }

            // 景點描述即時驗證和字數統計
            if (this.elements.spotDesc) {
                this.elements.spotDesc.addEventListener('input', () => {
                    console.log('景點描述輸入事件觸發');
                    this.updateCharacterCount();
                    this.validateSpotDesc();
                });
                
                this.elements.spotDesc.addEventListener('blur', () => {
                    console.log('景點描述失焦事件觸發');
                    this.validateSpotDesc();
                });
                
                // 初始化字數統計
                this.updateCharacterCount();
            }

            // 地區選擇驗證（必填）
            if (this.elements.region) {
                this.elements.region.addEventListener('change', () => {
                    console.log('地區選擇變更事件觸發');
                    this.validateRegion();
                });
                
                this.elements.region.addEventListener('blur', () => {
                    console.log('地區選擇失焦事件觸發');
                    this.validateRegion();
                });
            }

            // 聯絡電話驗證（選填）
            if (this.elements.tel) {
                this.elements.tel.addEventListener('input', () => {
                    console.log('聯絡電話輸入事件觸發');
                    this.validateTel();
                });
                
                this.elements.tel.addEventListener('blur', () => {
                    console.log('聯絡電話失焦事件觸發');
                    this.validateTel();
                });
            }

            // 官方網站驗證（選填）
            if (this.elements.website) {
                this.elements.website.addEventListener('input', () => {
                    console.log('官方網站輸入事件觸發');
                    this.validateWebsite();
                });
                
                this.elements.website.addEventListener('blur', () => {
                    console.log('官方網站失焦事件觸發');
                    this.validateWebsite();
                });
            }

            // 圖片上傳事件
            this.setupImageUpload();

            console.log('事件綁定完成');
        },

        /**
         * 設置地址自動完成功能
         * 當輸入地址時自動提取地區並設置到地區下拉選單
         */
        setupAddressAutoComplete() {
            console.log('設置地址自動完成功能');
            
            if (!this.elements.spotLoc || !this.elements.region) {
                console.log('地址或地區元素未找到，跳過地址自動完成設置');
                return;
            }
            
            // 監聽地址輸入框的blur事件
            this.elements.spotLoc.addEventListener('blur', () => {
                const address = this.elements.spotLoc.value.trim();
                if (!address) return;
                
                console.log('地址輸入完成，嘗試自動提取地區:', address);
                
                // 提取地區
                const region = this.extractRegionFromAddress(address);
                if (region) {
                    // 設置地區下拉選單
                    this.elements.region.value = region;
                    
                    // 觸發change事件，確保驗證邏輯正確執行
                    const event = new Event('change', { bubbles: true });
                    this.elements.region.dispatchEvent(event);
                    
                    console.log('自動設置地區成功:', region);
                } else {
                    console.log('無法從地址中提取地區');
                }
            });
        },

        /**
         * 從地址中提取地區（縣市）
         * @param {string} address 地址
         * @return {string|null} 提取的地區，如果無法提取則返回null
         */
        extractRegionFromAddress(address) {
            if (!address) return null;
            
            // 台灣縣市列表
            const regions = [
                '台北市', '臺北市', '新北市', '桃園市', '台中市', '臺中市', '台南市', '臺南市', '高雄市',
                '基隆市', '新竹市', '嘉義市', '新竹縣', '苗栗縣', '彰化縣', '南投縣', '雲林縣',
                '嘉義縣', '屏東縣', '宜蘭縣', '花蓮縣', '台東縣', '臺東縣', '澎湖縣', '金門縣', '連江縣'
            ];
            
            // 尋找地址中的縣市
            for (const region of regions) {
                if (address.includes(region)) {
                    // 處理台/臺的差異
                    if (region === '臺北市') return '台北市';
                    if (region === '臺中市') return '台中市';
                    if (region === '臺南市') return '台南市';
                    if (region === '臺東縣') return '台東縣';
                    return region;
                }
            }
            
            return null;
        },

        /**
         * 設置圖片上傳功能
         */
        setupImageUpload() {
            console.log('設置圖片上傳功能');
            
            if (!this.elements.spotImg || !this.elements.uploadArea || !this.elements.imagePreview) {
                console.log('圖片上傳元素未找到，跳過圖片上傳設置');
                return;
            }

            // 點擊上傳區域觸發檔案選擇
            this.elements.uploadArea.addEventListener('click', () => {
                this.elements.spotImg.click();
            });

            // 檔案選擇事件
            this.elements.spotImg.addEventListener('change', (e) => {
                this.handleFileSelect(e.target.files);
            });

            // 拖拽功能
            this.elements.uploadArea.addEventListener('dragover', (e) => {
                e.preventDefault();
                this.elements.uploadArea.classList.add('dragover');
            });

            this.elements.uploadArea.addEventListener('dragleave', (e) => {
                e.preventDefault();
                this.elements.uploadArea.classList.remove('dragover');
            });

            this.elements.uploadArea.addEventListener('drop', (e) => {
                e.preventDefault();
                this.elements.uploadArea.classList.remove('dragover');
                this.handleFileSelect(e.dataTransfer.files);
            });
        },

        /**
         * 處理檔案選擇
         */
        handleFileSelect(files) {
            console.log('處理檔案選擇，檔案數量:', files.length);
            
            if (files.length === 0) return;

            // 清空之前的預覽
            this.elements.imagePreview.innerHTML = '';
            this.clearError('spotImg');

            let validFiles = [];
            let hasError = false;

            for (let i = 0; i < files.length; i++) {
                const file = files[i];
                
                // 檢查檔案類型
                if (!file.type.match(/^image\/(jpeg|jpg|png)$/)) {
                    this.showError('spotImg', '只支援 JPG、PNG 格式的圖片');
                    hasError = true;
                    continue;
                }

                // 檢查檔案大小 (2MB = 2 * 1024 * 1024 bytes)
                if (file.size > 2 * 1024 * 1024) {
                    this.showError('spotImg', '圖片大小不能超過2MB');
                    hasError = true;
                    continue;
                }

                validFiles.push(file);
            }

            if (hasError && validFiles.length === 0) {
                return;
            }

            // 顯示預覽
            validFiles.forEach((file, index) => {
                this.createImagePreview(file, index);
            });

            console.log('有效檔案數量:', validFiles.length);
        },

        /**
         * 創建圖片預覽
         */
        createImagePreview(file, index) {
            const reader = new FileReader();
            reader.onload = (e) => {
                const previewItem = document.createElement('div');
                previewItem.className = 'spot-add-image-item';
                previewItem.innerHTML = `
                    <img src="${e.target.result}" alt="預覽圖片" class="spot-add-preview-img">
                    <div class="spot-add-image-info">
                        <div class="spot-add-image-name">${file.name}</div>
                        <div class="spot-add-image-size">${this.formatFileSize(file.size)}</div>
                    </div>
                    <button type="button" class="spot-add-remove-btn" onclick="this.parentElement.remove()">
                        <span class="material-icons">close</span>
                    </button>
                `;
                this.elements.imagePreview.appendChild(previewItem);
            };
            reader.readAsDataURL(file);
        },

        /**
         * 格式化檔案大小
         */
        formatFileSize(bytes) {
            if (bytes === 0) return '0 Bytes';
            const k = 1024;
            const sizes = ['Bytes', 'KB', 'MB', 'GB'];
            const i = Math.floor(Math.log(bytes) / Math.log(k));
            return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
        },

        /**
         * 驗證景點名稱
         */
        validateSpotName() {
            console.log('開始驗證景點名稱');
            
            const field = this.elements.spotName;
            const value = field.value.trim();
            let isValid = true;
            let errorMessage = '';

            console.log(`景點名稱值: "${value}"`);

            // 必填驗證
            if (!value) {
                isValid = false;
                errorMessage = '景點名稱為必填欄位';
                console.log('驗證失敗: 景點名稱為空');
            }
            // 長度驗證
            else if (value.length < 2 || value.length > 50) {
                isValid = false;
                errorMessage = '景點名稱字數必須在2-50字之間';
                console.log(`驗證失敗: 長度不符合要求 (${value.length}字)`);
            }
            // 特殊字符檢查
            else if (/[<>{}[\]|\\\"']/.test(value)) {
                isValid = false;
                errorMessage = '景點名稱不能包含特殊字符 < > { } [ ] | \\ " \'';
                console.log('驗證失敗: 包含特殊字符');
            }

            console.log(`景點名稱驗證結果: ${isValid ? '通過' : '失敗'}`);
            if (!isValid) {
                console.log(`錯誤訊息: ${errorMessage}`);
            }

            // 更新UI
            if (!isValid) {
                this.showError('spotName', errorMessage);
            } else {
                this.clearError('spotName');
            }

            return isValid;
        },

        /**
         * 驗證地址欄位
         */
        validateSpotLoc() {
            console.log('開始驗證地址欄位');
            
            const field = this.elements.spotLoc;
            const value = field.value.trim();
            let isValid = true;
            let errorMessage = '';

            console.log(`地址欄位值: "${value}"`);

            // 必填驗證
            if (!value) {
                isValid = false;
                errorMessage = '地址為必填欄位';
                console.log('驗證失敗: 地址為空');
            }
            // 長度驗證
            else if (value.length < 5 || value.length > 100) {
                isValid = false;
                errorMessage = '地址字數必須在5-100字之間';
                console.log(`驗證失敗: 長度不符合要求 (${value.length}字)`);
            }
            // 特殊字符檢查
            else if (/[<>{}[\]|\\\"']/.test(value)) {
                isValid = false;
                errorMessage = '景點地址不能包含特殊字符 < > { } [ ] | \\ " \'';
                console.log('驗證失敗: 包含特殊字符');
            }
            // 檢查是否包含中文
            else if (!/[\u4e00-\u9fa5]/.test(value)) {
                isValid = false;
                errorMessage = '地址必須包含中文';
                console.log('驗證失敗: 不包含中文');
            }
            // 檢查是否包含縣市及鄉鎮市區
            else if (!/[縣市].*?[鄉鎮市區]/.test(value)) {
                isValid = false;
                errorMessage = '請輸入完整地址（包含縣市及鄉鎮市區）';
                console.log('驗證失敗: 不包含縣市及鄉鎮市區');
            }

            console.log(`地址驗證結果: ${isValid ? '通過' : '失敗'}`);
            if (!isValid) {
                console.log(`錯誤訊息: ${errorMessage}`);
            }

            // 更新UI
            if (!isValid) {
                this.showError('spotLoc', errorMessage);
            } else {
                this.clearError('spotLoc');
            }

            return isValid;
        },

        /**
         * 驗證景點描述
         */
        validateSpotDesc() {
            console.log('開始驗證景點描述');
            
            const field = this.elements.spotDesc;
            const value = field.value.trim();
            let isValid = true;
            let errorMessage = '';

            console.log(`景點描述值: "${value}" (${value.length}字)`);

            // 必填驗證
            if (!value) {
                isValid = false;
                errorMessage = '景點描述為必填欄位';
                console.log('驗證失敗: 景點描述為空');
            }
            // 長度驗證
            else if (value.length < 5) {
                isValid = false;
                errorMessage = '景點描述至少需要5個字';
                console.log(`驗證失敗: 長度不足 (${value.length}字)`);
            }
            else if (value.length > 500) {
                isValid = false;
                errorMessage = '景點描述不能超過500個字';
                console.log(`驗證失敗: 長度超過限制 (${value.length}字)`);
            }

            console.log(`景點描述驗證結果: ${isValid ? '通過' : '失敗'}`);
            if (!isValid) {
                console.log(`錯誤訊息: ${errorMessage}`);
            }

            // 更新UI
            if (!isValid) {
                this.showError('spotDesc', errorMessage);
            } else {
                this.clearError('spotDesc');
            }

            return isValid;
        },

        /**
         * 驗證地區選擇（必填）
         */
        validateRegion() {
            console.log('開始驗證地區選擇');
            
            const field = this.elements.region;
            const value = field.value.trim();
            let isValid = true;
            let errorMessage = '';

            console.log(`地區選擇值: "${value}"`);

            // 必填驗證
            if (!value) {
                isValid = false;
                errorMessage = '請選擇景點所在地區';
                console.log('驗證失敗: 地區未選擇');
            }

            console.log(`地區選擇驗證結果: ${isValid ? '通過' : '失敗'}`);
            if (!isValid) {
                console.log(`錯誤訊息: ${errorMessage}`);
            }

            // 更新UI
            if (!isValid) {
                this.showError('region', errorMessage);
            } else {
                this.clearError('region');
            }

            return isValid;
        },

        /**
         * 更新字數統計
         */
        updateCharacterCount() {
            if (this.elements.spotDesc && this.elements.descCharCount) {
                const currentLength = this.elements.spotDesc.value.length;
                this.elements.descCharCount.textContent = `${currentLength}/500`;
                
                // 根據字數改變顏色
                if (currentLength > 500) {
                    this.elements.descCharCount.style.color = '#d32f2f';
                } else if (currentLength > 450) {
                    this.elements.descCharCount.style.color = '#ff9800';
                } else {
                    this.elements.descCharCount.style.color = '#666';
                }
                
                console.log(`字數統計更新: ${currentLength}/500`);
            }
        },

        /**
         * 驗證聯絡電話（選填）
         */
        validateTel() {
            console.log('開始驗證聯絡電話');
            
            const field = this.elements.tel;
            const value = field.value.trim();
            let isValid = true;
            let errorMessage = '';

            console.log(`聯絡電話值: "${value}"`);

            // 選填欄位，如果為空則跳過驗證
            if (!value) {
                console.log('聯絡電話為空，跳過驗證');
                this.clearError('tel');
                return true;
            }

            // 簡單格式檢查：只檢查是否為數字、空格、連字號、括號
            const telPattern = /^[\d\-\s()（）+]+$/;
            if (!telPattern.test(value)) {
                isValid = false;
                errorMessage = '電話格式不正確，請只使用數字、連字號、空格和括號';
                console.log('驗證失敗: 電話格式不正確');
            }
            // 長度檢查
            else if (value.length < 8 || value.length > 20) {
                    isValid = false;
                errorMessage = '電話號碼長度應在8-20字元之間';
                console.log(`驗證失敗: 電話長度不符合要求 (${value.length}字)`);
            }

            console.log(`聯絡電話驗證結果: ${isValid ? '通過' : '失敗'}`);
            if (!isValid) {
                console.log(`錯誤訊息: ${errorMessage}`);
            }

            // 更新UI
            if (!isValid) {
                this.showError('tel', errorMessage);
            } else {
                this.clearError('tel');
            }

            return isValid;
        },

        /**
         * 驗證官方網站（選填）
         */
        validateWebsite() {
            console.log('開始驗證官方網站');

            const field = this.elements.website;
            const value = field.value.trim();
            let isValid = true;
            let errorMessage = '';

            console.log(`官方網站值: "${value}"`);

            // 選填欄位，如果為空則跳過驗證
            if (!value) {
                console.log('官方網站為空，跳過驗證');
                this.clearError('website');
                return true;
            }

            // 檢查是否包含 http:// 或 https://
            if (!value.startsWith('http://') && !value.startsWith('https://')) {
                isValid = false;
                errorMessage = '網站URL必須包含http://或https://';
                console.log('驗證失敗: 缺少協議前綴');
            }
            // 簡單的URL格式檢查
            else {
                try {
                    new URL(value);
                } catch (e) {
                    isValid = false;
                    errorMessage = '請輸入有效的網站URL';
                    console.log('驗證失敗: URL格式無效');
                }
            }

            console.log(`官方網站驗證結果: ${isValid ? '通過' : '失敗'}`);
            if (!isValid) {
                console.log(`錯誤訊息: ${errorMessage}`);
            }

            // 更新UI
            if (!isValid) {
                this.showError('website', errorMessage);
            } else {
                this.clearError('website');
            }

            return isValid;
        },

        /**
         * 顯示錯誤
         */
        showError(fieldName, message) {
            console.log(`顯示錯誤: ${fieldName} - ${message}`);
            
            const field = this.elements[fieldName];
            const errorElement = this.elements[fieldName + 'Error'];

            if (field) {
                field.classList.add('error');
                field.style.borderColor = '#d32f2f';
                field.style.backgroundColor = '#ffebee';
            }

            if (errorElement) {
                errorElement.textContent = message;
                errorElement.style.display = 'block';
                errorElement.style.color = '#d32f2f';
            }
        },

        /**
         * 清除錯誤
         */
        clearError(fieldName) {
            console.log(`清除錯誤: ${fieldName}`);
            
            const field = this.elements[fieldName];
            const errorElement = this.elements[fieldName + 'Error'];

            if (field) {
                field.classList.remove('error');
                field.style.borderColor = '';
                field.style.backgroundColor = '';
            }

            if (errorElement) {
                errorElement.textContent = '';
                errorElement.style.display = 'none';
            }
        },

        /**
         * 處理表單提交
         */
        handleFormSubmit(e) {
            console.log('處理表單提交');
            
            // 驗證所有必填欄位
            const nameValid = this.validateSpotName();
            const locValid = this.validateSpotLoc();
            const descValid = this.validateSpotDesc();
            const regionValid = this.validateRegion();
            
            // 驗證選填欄位
            const telValid = this.validateTel();
            const websiteValid = this.validateWebsite();
            
            const allValid = nameValid && locValid && descValid && regionValid && telValid && websiteValid;
            
            if (!allValid) {
                console.log('表單驗證失敗，阻止提交');
                e.preventDefault();
                alert('請修正表單中的錯誤後再提交');
                
                // 聚焦到第一個錯誤欄位
                if (!nameValid) {
                    this.elements.spotName.focus();
                } else if (!locValid) {
                    this.elements.spotLoc.focus();
                } else if (!descValid) {
                    this.elements.spotDesc.focus();
                } else if (!regionValid) {
                    this.elements.region.focus();
                } else if (!telValid) {
                    this.elements.tel.focus();
                } else if (!websiteValid) {
                    this.elements.website.focus();
                }
                
                return false;
            }
            
            console.log('表單驗證通過');
            // 允許表單提交
        }
    };

    // 將 SpotAdd 暴露到全域範圍
    window.SpotAdd = SpotAdd;

    console.log('add.js 載入完成，SpotAdd 已暴露到全域');

})();
