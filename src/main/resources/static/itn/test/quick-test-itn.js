// 🧪 Itn 行程卡片元件快速測試腳本
// 
// 📍 執行位置：
// 1. 主要測試：在 `test-itn-trip-card.html` 頁面開啟後，按 F12 開啟開發者工具
//    網址：http://localhost:8080/itn/test/test-itn-trip-card.html
// 
// 2. 整合測試：在 `itn-integration-example.html` 頁面開啟後，按 F12 開啟開發者工具
//    網址：http://localhost:8080/itn/demo/itn-integration-example.html
// 
// 3. 本地伺服器：確保已啟動 Spring Boot 應用程式或使用其他本地伺服器
// 
// 💡 使用方式：
// 1. 開啟上述任一頁面
// 2. 按 F12 開啟瀏覽器開發者工具
// 3. 切換到 Console 分頁
// 4. 複製此腳本內容並貼上執行
// 
// 在瀏覽器控制台執行此腳本來快速驗證元件功能

console.log('🚀 開始 Itn 行程卡片元件快速測試...');

// 測試 1: 檢查元件是否載入
console.log('\n📋 測試 1: 元件載入檢查');
console.log('ItnTripCardComponent:', typeof ItnTripCardComponent);
console.log('createItnTripCards:', typeof createItnTripCards);
console.log('getItnTripSampleData:', typeof getItnTripSampleData);
console.log('validateItnTripData:', typeof validateItnTripData);

// 測試 2: 資料格式驗證
console.log('\n📋 測試 2: 資料格式驗證');
const basicData = getItnTripSampleData('BASIC');
const validation = validateItnTripData(basicData);
console.log('基本資料驗證結果:', validation);

// 測試 3: 創建元件實例
console.log('\n📋 測試 3: 元件實例創建');
try {
    const component = new ItnTripCardComponent();
    console.log('✅ 元件實例創建成功');
    console.log('元件方法:', Object.getOwnPropertyNames(Object.getPrototypeOf(component)));
} catch (error) {
    console.error('❌ 元件實例創建失敗:', error);
}

// 測試 4: 測試容器檢查
console.log('\n📋 測試 4: 容器檢查');
const testContainer = document.getElementById('testContainer');
if (!testContainer) {
    console.log('⚠️  測試容器不存在，創建臨時容器...');
    const tempContainer = document.createElement('div');
    tempContainer.id = 'tempTestContainer';
    tempContainer.style.position = 'fixed';
    tempContainer.style.top = '10px';
    tempContainer.style.right = '10px';
    tempContainer.style.width = '300px';
    tempContainer.style.height = '400px';
    tempContainer.style.backgroundColor = '#f0f0f0';
    tempContainer.style.border = '1px solid #ccc';
    tempContainer.style.zIndex = '9999';
    tempContainer.style.overflow = 'auto';
    document.body.appendChild(tempContainer);
    
    // 測試 5: 實際渲染測試
    console.log('\n📋 測試 5: 實際渲染測試');
    try {
        createItnTripCards('#tempTestContainer', basicData, {
            onRegisterClick: function(trip) {
                console.log('🎯 報名按鈕點擊:', trip.title);
            },
            onDetailClick: function(trip) {
                console.log('🔍 詳情按鈕點擊:', trip.title);
            },
            onCardClick: function(trip) {
                console.log('📱 卡片點擊:', trip.title);
            }
        });
        console.log('✅ 卡片渲染成功');
        
        // 檢查渲染的卡片數量
        const cards = document.querySelectorAll('.itn-trip-card');
        console.log('渲染的卡片數量:', cards.length);
        
    } catch (error) {
        console.error('❌ 卡片渲染失敗:', error);
    }
} else {
    console.log('✅ 測試容器存在');
}

// 測試 6: 效能測試
console.log('\n📋 測試 6: 效能測試');
const startTime = performance.now();
const fullData = getItnTripSampleData('FULL');
const endTime = performance.now();
console.log('資料載入時間:', (endTime - startTime).toFixed(2), 'ms');

// 測試 7: 錯誤處理測試
console.log('\n📋 測試 7: 錯誤處理測試');
const invalidData = [
    {
        title: '無效資料',
        // 缺少必要欄位
    }
];
const errorValidation = validateItnTripData(invalidData);
console.log('錯誤資料驗證結果:', errorValidation);

// 測試 8: 瀏覽器相容性檢查
console.log('\n📋 測試 8: 瀏覽器相容性檢查');
console.log('User Agent:', navigator.userAgent);
console.log('支援 ES6:', typeof Promise !== 'undefined');
console.log('支援 fetch:', typeof fetch !== 'undefined');
console.log('支援 CSS Grid:', CSS.supports('display', 'grid'));

// 測試 9: 記憶體檢查
console.log('\n📋 測試 9: 記憶體檢查');
if (performance.memory) {
    console.log('記憶體使用:', {
        used: Math.round(performance.memory.usedJSHeapSize / 1024 / 1024) + 'MB',
        total: Math.round(performance.memory.totalJSHeapSize / 1024 / 1024) + 'MB',
        limit: Math.round(performance.memory.jsHeapSizeLimit / 1024 / 1024) + 'MB'
    });
} else {
    console.log('⚠️  無法獲取記憶體資訊');
}

// 測試結果總結
console.log('\n🎯 測試結果總結');
console.log('='.repeat(50));

const testResults = {
    '元件載入': typeof ItnTripCardComponent !== 'undefined',
    '便利函數': typeof createItnTripCards !== 'undefined',
    '資料獲取': typeof getItnTripSampleData !== 'undefined',
    '資料驗證': typeof validateItnTripData !== 'undefined',
    '資料格式': validation.valid,
    '錯誤處理': !errorValidation.valid && errorValidation.errors.length > 0
};

let passedTests = 0;
let totalTests = Object.keys(testResults).length;

Object.entries(testResults).forEach(([test, result]) => {
    const status = result ? '✅' : '❌';
    console.log(`${status} ${test}: ${result ? '通過' : '失敗'}`);
    if (result) passedTests++;
});

console.log('='.repeat(50));
console.log(`總測試數: ${totalTests}`);
console.log(`通過測試: ${passedTests}`);
console.log(`失敗測試: ${totalTests - passedTests}`);
console.log(`成功率: ${Math.round((passedTests / totalTests) * 100)}%`);

if (passedTests === totalTests) {
    console.log('🎉 所有測試通過！元件可以安全使用。');
} else {
    console.log('⚠️  部分測試失敗，請檢查相關功能。');
}

// 清理臨時容器
setTimeout(() => {
    const tempContainer = document.getElementById('tempTestContainer');
    if (tempContainer) {
        console.log('\n🧹 清理臨時測試容器...');
        tempContainer.remove();
    }
}, 10000); // 10秒後自動清理

console.log('\n📝 測試完成！請查看上方結果。');
console.log('💡 提示：臨時測試容器將在 10 秒後自動移除。');

// 📋 測試環境檢查
console.log('\n🔍 測試環境檢查');
console.log('當前頁面:', window.location.href);
console.log('頁面標題:', document.title);

// 檢查是否在正確的測試頁面
const isTestPage = window.location.href.includes('test-itn-trip-card.html') || 
                   window.location.href.includes('itn-integration-example.html') ||
                   window.location.href.includes('itn-trip-card-demo.html');
console.log('是否在測試頁面:', isTestPage ? '✅ 是' : '❌ 否');

if (!isTestPage) {
    console.warn('⚠️  建議在測試頁面執行此腳本以獲得最佳測試效果');
    console.warn('   測試頁面：itn/test/test-itn-trip-card.html 或 itn/demo/itn-integration-example.html 或 itn/demo/itn-trip-card-demo.html');
} 