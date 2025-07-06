# 🎯 Itn 行程卡片元件

一個功能完整、可重複使用的行程卡片動態元件，專為揪團模組設計。

## 🚀 快速開始

## 測試網址:http://localhost:8080/itn/test/index.html

### 1. 引入檔案

```html
<!-- 引入主要元件檔案 -->
<script src="/static/itn/components/itn-trip-card.js"></script>

<!-- 引入自動載入器（可選） -->
<script src="/static/itn/components/itn-trip-card-loader.js"></script>
```

### 2. 基本使用

```javascript
// 方式一：直接使用靜態資料
const tripData = [
    {
        id: "itn001",
        title: "台北文化美食一日遊",
        date: "2025-07-15",
        duration: "8小時",
        groupSize: "4-8人",
        price: 1200,
        rating: 4.8,
        itinerary: [
            {
                time: "09:00",
                duration: "2.5小時",
                name: "國立故宮博物院",
                location: "士林區",
                category: "文化景點"
            }
        ]
    }
];

createItnTripCards('#container', tripData);
```

### 3. 自動載入行程資料

```javascript
// 方式二：自動從行程 API 載入資料
loadItineraryCards('#container', {
    limit: 10,
    cardOptions: {
        onRegisterClick: (tripId, tripData) => {
            // 處理報名邏輯
            window.location.href = `/groupactivity/register/${tripId}`;
        },
        onDetailClick: (tripId, tripData) => {
            // 跳轉到詳情頁面
            window.location.href = `/groupactivity/detail/${tripId}`;
        }
    }
});
```

### 4. 搜尋和篩選

```javascript
// 搜尋行程
searchItineraryCards('#container', '台北', { limit: 5 });

// 篩選公開行程
filterItineraryCards('#container', true, { limit: 10 });
```

## ✨ 功能特色

- 🚀 **純 JavaScript** - 無框架依賴，輕量高效
- 📱 **響應式設計** - 自動適應不同螢幕尺寸
- 🎨 **可自訂主題** - 支援自訂色彩和樣式
- 🔧 **靈活配置** - 多種資料來源和回調函數
- 🛡️ **完整驗證** - 嚴格的資料格式驗證
- 📊 **豐富功能** - 排序、篩選、更新等操作
- 🔄 **自動載入** - 支援從 API 自動抓取行程資料

## 📦 檔案結構

```
itn/
├── components/
│   ├── itn-trip-card.js          # 主要元件檔案
│   ├── itn-trip-card-loader.js   # 自動載入器
│   └── itn-trip-card-data.js     # 資料格式範例
├── demo/
│   ├── itn-trip-card-demo.html   # 使用範例
│   ├── itn-integration-example.html # 整合範例
│   └── groupactivity-integration.html # 揪團整合範例
├── test/
│   ├── test-itn-trip-card.html   # 測試頁面
│   └── quick-test-itn.js         # 快速測試
└── README.md                     # 說明文件
```

## 📋 資料格式

### 必要欄位

```javascript
{
    id: "string|number",           // 唯一識別碼（可選）
    title: "string",              // 行程標題
    date: "string|Date",          // 行程日期
    duration: "string",           // 行程時長（如：8小時）
    groupSize: "string",          // 團體人數（如：4-8人）
    price: number,                // 價格（數字）
    rating: number,               // 評分（可選，0-5）
    itinerary: [                  // 行程安排陣列
        {
            time: "HH:MM",        // 時間（如：09:00）
            duration: "string",   // 持續時間（如：2.5小時）
            name: "string",       // 地點名稱
            location: "string",   // 地點位置
            category: "string"    // 分類
        }
    ]
}
```

### 可選欄位

```javascript
{
    theme: {
        gradient: "string",       // 卡片頭部漸變色
        primaryColor: "string"    // 主要顏色
    },
    buttons: {
        register: "string",       // 報名按鈕文字
        detail: "string"          // 詳情按鈕文字
    }
}
```

### 支援的分類

- `文化景點` - 博物館、古蹟等
- `美食` - 餐廳、小吃等
- `自然景觀` - 公園、山景等
- `購物` - 商場、市集等
- `娛樂` - 遊樂園、電影院等

## 🔧 API 參考

### 主要方法

#### `render(containerSelector, data, options)`
渲染行程卡片

#### `renderFromAPI(containerSelector, apiUrl, options)`
從 API 載入資料並渲染

#### `updateCard(tripId, newData, options)`
更新特定卡片

#### `reRender(containerSelector, data, options)`
重新渲染所有卡片

#### `sortCards(containerSelector, sortBy, order)`
排序卡片
- `sortBy`: 'price', 'rating', 'date'
- `order`: 'asc', 'desc'

#### `filterCards(containerSelector, filterFunction)`
篩選卡片

#### `destroy(containerSelector)`
銷毀組件

### 便利函數

```javascript
// 基本渲染
createItnTripCards(containerSelector, data, options)

// API 載入
loadItnTripCardsFromAPI(containerSelector, apiUrl, options)

// 更新卡片
updateItnTripCard(tripId, newData, options)

// 排序卡片
sortItnTripCards(containerSelector, sortBy, order)

// 篩選卡片
filterItineraryCards(containerSelector, filterFunction)

// 銷毀組件
destroyItnTripCards(containerSelector)
```

### 實例管理

```javascript
// 創建實例
createItnTripCardInstance(instanceId, containerSelector, data, options)

// 獲取實例
getItnTripCardInstance(instanceId)

// 銷毀實例
destroyItnTripCardInstance(instanceId)
```

## 🎨 自訂樣式

元件會自動注入 CSS 樣式，您也可以透過以下方式自訂：

```css
/* 自訂容器樣式 */
.itn-trip-cards-container {
    /* 您的樣式 */
}

/* 自訂卡片樣式 */
.itn-trip-card {
    /* 您的樣式 */
}

/* 自訂分類標籤 */
.category-culture {
    background: #e8f5e8;
    color: #2e7d32;
}
```

## 🔍 錯誤處理

元件包含完整的錯誤處理機制：

- 資料格式驗證
- API 請求錯誤處理
- 容器不存在檢查
- 詳細的錯誤訊息

## 📱 響應式設計

元件自動適應不同螢幕尺寸：

- 桌面版：網格佈局，多列顯示
- 平板版：調整間距和字體大小
- 手機版：單列顯示，優化觸控體驗

## 🧪 測試

使用提供的測試頁面進行功能測試：

```html
<!-- 開啟測試頁面 -->
test/test-itn-trip-card.html
```

## 📄 授權

本元件為專案內部使用，請勿外流。

## 🤝 貢獻

如有問題或建議，請聯繫開發團隊。 