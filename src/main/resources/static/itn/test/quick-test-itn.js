/**
 * 行程卡片元件快速測試腳本
 * 用於快速驗證元件功能
 */

class ItnQuickTest {
    constructor() {
        this.testResults = [];
        this.apiBaseUrl = '/api/itinerary/cards';
    }

    /**
     * 執行所有測試
     */
    async runAllTests() {
        console.log('🧪 開始執行行程卡片元件測試...');
        
        try {
            // 1. 測試 API 端點
            await this.testAPIEndpoints();
            
            // 2. 測試元件載入
            await this.testComponentLoading();
            
            // 3. 測試資料格式
            await this.testDataFormat();
            
            // 4. 測試互動功能
            await this.testInteractions();
            
            // 5. 顯示測試結果
            this.showTestResults();
            
        } catch (error) {
            console.error('❌ 測試過程中發生錯誤:', error);
        }
    }

    /**
     * 測試 API 端點
     */
    async testAPIEndpoints() {
        console.log('📡 測試 API 端點...');
        
        const tests = [
            {
                name: '基本卡片資料載入',
                url: this.apiBaseUrl + '?limit=3',
                expected: 'success'
            },
            {
                name: '搜尋功能測試',
                url: this.apiBaseUrl + '?keyword=台北&limit=2',
                expected: 'success'
            },
            {
                name: '篩選功能測試',
                url: this.apiBaseUrl + '?isPublic=true&limit=2',
                expected: 'success'
            }
        ];

        for (const test of tests) {
            try {
                const response = await fetch(test.url);
                const data = await response.json();
                
                const passed = response.ok && data.success;
                this.addTestResult(test.name, passed, data);
                
                console.log(`  ${passed ? '✅' : '❌'} ${test.name}`);
                
            } catch (error) {
                this.addTestResult(test.name, false, error.message);
                console.log(`  ❌ ${test.name}: ${error.message}`);
            }
        }
    }

    /**
     * 測試元件載入
     */
    async testComponentLoading() {
        console.log('🔧 測試元件載入...');
        
        const tests = [
            {
                name: 'ItnTripCardComponent 類別',
                test: () => typeof window.ItnTripCardComponent !== 'undefined',
                expected: true
            },
            {
                name: 'createItnTripCards 函數',
                test: () => typeof window.createItnTripCards === 'function',
                expected: true
            },
            {
                name: 'ItnTripCardLoader 類別',
                test: () => typeof window.ItnTripCardLoader !== 'undefined',
                expected: true
            },
            {
                name: 'loadItineraryCards 函數',
                test: () => typeof window.loadItineraryCards === 'function',
                expected: true
            }
        ];

        for (const test of tests) {
            const passed = test.test();
            this.addTestResult(test.name, passed, passed ? '載入成功' : '載入失敗');
            console.log(`  ${passed ? '✅' : '❌'} ${test.name}`);
        }
    }

    /**
     * 測試資料格式
     */
    async testDataFormat() {
        console.log('📋 測試資料格式...');
        
        try {
            const response = await fetch(this.apiBaseUrl + '?limit=1');
            const data = await response.json();
            
            if (!data.success || !data.data || data.data.length === 0) {
                this.addTestResult('資料格式驗證', false, 'API 返回資料格式錯誤');
                return;
            }
            
            const trip = data.data[0];
            const requiredFields = ['id', 'title', 'date', 'duration', 'groupSize', 'price', 'itinerary'];
            
            const missingFields = requiredFields.filter(field => !trip.hasOwnProperty(field));
            
            if (missingFields.length > 0) {
                this.addTestResult('必要欄位檢查', false, `缺少欄位: ${missingFields.join(', ')}`);
            } else {
                this.addTestResult('必要欄位檢查', true, '所有必要欄位都存在');
            }
            
            // 測試行程項目格式
            if (trip.itinerary && trip.itinerary.length > 0) {
                const item = trip.itinerary[0];
                const itemFields = ['time', 'duration', 'name', 'location', 'category'];
                const missingItemFields = itemFields.filter(field => !item.hasOwnProperty(field));
                
                if (missingItemFields.length > 0) {
                    this.addTestResult('行程項目格式', false, `缺少欄位: ${missingItemFields.join(', ')}`);
                } else {
                    this.addTestResult('行程項目格式', true, '行程項目格式正確');
                }
            }
            
        } catch (error) {
            this.addTestResult('資料格式驗證', false, error.message);
        }
    }

    /**
     * 測試互動功能
     */
    async testInteractions() {
        console.log('🖱️ 測試互動功能...');
        
        // 創建測試容器
        const testContainer = document.createElement('div');
        testContainer.id = 'test-container';
        testContainer.style.display = 'none';
        document.body.appendChild(testContainer);
        
        try {
            // 測試基本渲染
            if (typeof window.createItnTripCards === 'function') {
                const testData = [{
                    id: 'test001',
                    title: '測試行程',
                    date: '2025-01-01',
                    duration: '4小時',
                    groupSize: '2-4人',
                    price: 800,
                    rating: 4.5,
                    itinerary: [{
                        time: '09:00',
                        duration: '2小時',
                        name: '測試景點',
                        location: '測試地點',
                        category: '文化景點'
                    }]
                }];
                
                window.createItnTripCards('#test-container', testData);
                
                const cards = testContainer.querySelectorAll('.itn-trip-card');
                if (cards.length > 0) {
                    this.addTestResult('基本渲染功能', true, `成功渲染 ${cards.length} 張卡片`);
                } else {
                    this.addTestResult('基本渲染功能', false, '沒有渲染出卡片');
                }
            }
            
            // 測試自動載入
            if (typeof window.loadItineraryCards === 'function') {
                this.addTestResult('自動載入功能', true, '函數存在，可正常使用');
            } else {
                this.addTestResult('自動載入功能', false, '函數不存在');
            }
            
        } catch (error) {
            this.addTestResult('互動功能測試', false, error.message);
        } finally {
            // 清理測試容器
            document.body.removeChild(testContainer);
        }
    }

    /**
     * 添加測試結果
     */
    addTestResult(name, passed, details) {
        this.testResults.push({
            name,
            passed,
            details,
            timestamp: new Date().toLocaleTimeString()
        });
    }

    /**
     * 顯示測試結果
     */
    showTestResults() {
        console.log('\n📊 測試結果總結:');
        console.log('='.repeat(50));
        
        const passed = this.testResults.filter(r => r.passed).length;
        const total = this.testResults.length;
        
        console.log(`總測試數: ${total}`);
        console.log(`通過: ${passed}`);
        console.log(`失敗: ${total - passed}`);
        console.log(`成功率: ${((passed / total) * 100).toFixed(1)}%`);
        
        console.log('\n詳細結果:');
        this.testResults.forEach(result => {
            const icon = result.passed ? '✅' : '❌';
            console.log(`${icon} ${result.name}: ${result.details}`);
        });
        
        // 在頁面上顯示結果
        this.displayResultsOnPage();
    }

    /**
     * 在頁面上顯示結果
     */
    displayResultsOnPage() {
        const resultsDiv = document.createElement('div');
        resultsDiv.id = 'test-results';
        resultsDiv.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            background: white;
            border: 2px solid #333;
            border-radius: 8px;
            padding: 20px;
            max-width: 400px;
            max-height: 80vh;
            overflow-y: auto;
            z-index: 10000;
            font-family: monospace;
            font-size: 14px;
        `;
        
        const passed = this.testResults.filter(r => r.passed).length;
        const total = this.testResults.length;
        
        resultsDiv.innerHTML = `
            <h3>🧪 測試結果</h3>
            <p><strong>總測試數:</strong> ${total}</p>
            <p><strong>通過:</strong> ${passed}</p>
            <p><strong>失敗:</strong> ${total - passed}</p>
            <p><strong>成功率:</strong> ${((passed / total) * 100).toFixed(1)}%</p>
            <hr>
            <h4>詳細結果:</h4>
            ${this.testResults.map(result => `
                <div style="margin: 5px 0; padding: 5px; border-left: 3px solid ${result.passed ? '#4caf50' : '#f44336'};">
                    <strong>${result.passed ? '✅' : '❌'} ${result.name}</strong><br>
                    <small>${result.details}</small>
                </div>
            `).join('')}
            <button onclick="this.parentElement.remove()" style="margin-top: 10px; padding: 5px 10px;">關閉</button>
        `;
        
        document.body.appendChild(resultsDiv);
    }
}

// 全域測試函數
window.runItnTests = function() {
    const tester = new ItnQuickTest();
    return tester.runAllTests();
};

// 自動執行測試（如果頁面載入完成）
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
        console.log('🚀 頁面載入完成，可以執行測試');
        console.log('執行測試指令: runItnTests()');
    });
} else {
    console.log('🚀 頁面已載入，可以執行測試');
    console.log('執行測試指令: runItnTests()');
} 