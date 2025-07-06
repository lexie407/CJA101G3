/**
 * 行程卡片標準資料格式範例
 * 提供各種資料格式的範例和說明
 */

// 標準資料格式說明
const ITN_TRIP_DATA_SCHEMA = {
    // 必要欄位
    id: "string|number - 唯一識別碼（可選，系統會自動生成）",
    title: "string - 行程標題",
    date: "string|Date - 行程日期",
    duration: "string - 行程時長（如：8小時）",
    groupSize: "string - 團體人數（如：4-8人）",
    price: "number - 價格（數字，不含符號）",
    rating: "number - 評分（可選，預設5.0）",
    itinerary: "array - 行程安排陣列",
    
    // 可選欄位
    theme: {
        gradient: "string - 卡片頭部漸變色（可選）",
        primaryColor: "string - 主要顏色（可選）"
    },
    buttons: {
        register: "string - 報名按鈕文字（可選，預設：立即報名）",
        detail: "string - 詳情按鈕文字（可選，預設：查看詳情）"
    }
};

// 行程項目格式說明
const ITN_ITINERARY_ITEM_SCHEMA = {
    time: "string - 時間（如：09:00）",
    duration: "string - 持續時間（如：2.5小時）",
    name: "string - 地點名稱",
    location: "string - 地點位置",
    category: "string - 分類（文化景點、美食、自然景觀、購物、娛樂）"
};

// 範例資料 1：基本格式
const ITN_SAMPLE_TRIP_DATA_BASIC = [
    {
        id: "itn001",
        title: "台北文化美食一日遊",
        date: "2025年7月15日",
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
            },
            {
                time: "12:00",
                duration: "1.5小時",
                name: "鼎泰豐午餐",
                location: "信義區",
                category: "美食"
            }
        ]
    }
];

// 範例資料 2：完整格式（含主題色彩）
const ITN_SAMPLE_TRIP_DATA_FULL = [
    {
        id: "itn001",
        title: "台北文化美食一日遊",
        date: "2025-07-15",
        duration: "8小時",
        groupSize: "4-8人",
        price: 1200,
        rating: 4.8,
        theme: {
            gradient: "linear-gradient(135deg, #4285f4 0%, #9c27b0 100%)",
            primaryColor: "#2196f3"
        },
        buttons: {
            register: "立即報名",
            detail: "查看詳情"
        },
        itinerary: [
            {
                time: "09:00",
                duration: "2.5小時",
                name: "國立故宮博物院",
                location: "士林區",
                category: "文化景點"
            },
            {
                time: "12:00",
                duration: "1.5小時",
                name: "鼎泰豐午餐",
                location: "信義區",
                category: "美食"
            },
            {
                time: "14:00",
                duration: "3小時",
                name: "台北101觀景台",
                location: "信義區",
                category: "文化景點"
            }
        ]
    },
    {
        id: "itn002",
        title: "九份山城懷舊之旅",
        date: "2025-07-22",
        duration: "6小時",
        groupSize: "6-10人",
        price: 880,
        rating: 4.6,
        theme: {
            gradient: "linear-gradient(135deg, #ff6b35 0%, #f7931e 100%)",
            primaryColor: "#ff6b35"
        },
        itinerary: [
            {
                time: "10:00",
                duration: "3小時",
                name: "九份老街",
                location: "新北市",
                category: "文化景點"
            },
            {
                time: "14:00",
                duration: "2小時",
                name: "阿妹茶樓",
                location: "九份",
                category: "美食"
            }
        ]
    }
];

// 範例資料 3：多樣化分類
const ITN_SAMPLE_TRIP_DATA_CATEGORIES = [
    {
        id: "itn003",
        title: "台中自然生態之旅",
        date: "2025-08-05",
        duration: "10小時",
        groupSize: "8-12人",
        price: 1500,
        rating: 4.9,
        theme: {
            gradient: "linear-gradient(135deg, #4caf50 0%, #8bc34a 100%)",
            primaryColor: "#4caf50"
        },
        itinerary: [
            {
                time: "08:00",
                duration: "4小時",
                name: "合歡山日出",
                location: "南投縣",
                category: "自然景觀"
            },
            {
                time: "13:00",
                duration: "2小時",
                name: "清境農場",
                location: "南投縣",
                category: "自然景觀"
            },
            {
                time: "16:00",
                duration: "2小時",
                name: "埔里酒廠",
                location: "南投縣",
                category: "文化景點"
            }
        ]
    },
    {
        id: "itn004",
        title: "高雄購物美食行",
        date: "2025-08-12",
        duration: "7小時",
        groupSize: "4-6人",
        price: 950,
        rating: 4.7,
        theme: {
            gradient: "linear-gradient(135deg, #e91e63 0%, #f06292 100%)",
            primaryColor: "#e91e63"
        },
        itinerary: [
            {
                time: "10:00",
                duration: "3小時",
                name: "夢時代購物中心",
                location: "前鎮區",
                category: "購物"
            },
            {
                time: "14:00",
                duration: "2小時",
                name: "六合夜市",
                location: "新興區",
                category: "美食"
            },
            {
                time: "17:00",
                duration: "2小時",
                name: "義大世界",
                location: "大樹區",
                category: "娛樂"
            }
        ]
    }
];

// API 回應格式範例
const ITN_API_RESPONSE_EXAMPLE = {
    success: true,
    data: ITN_SAMPLE_TRIP_DATA_FULL,
    message: "資料載入成功",
    total: 2
};

// 錯誤處理範例
const ITN_ERROR_EXAMPLES = {
    // 缺少必要欄位
    MISSING_REQUIRED_FIELDS: {
        title: "台北一日遊",
        // 缺少 date, duration, groupSize, price, itinerary
    },
    
    // 錯誤的資料類型
    WRONG_DATA_TYPES: {
        title: "台北一日遊",
        date: "2025-07-15",
        duration: "8小時",
        groupSize: "4-8人",
        price: "1200", // 應該是數字
        itinerary: "錯誤格式" // 應該是陣列
    },
    
    // 空的行程安排
    EMPTY_ITINERARY: {
        title: "台北一日遊",
        date: "2025-07-15",
        duration: "8小時",
        groupSize: "4-8人",
        price: 1200,
        itinerary: [] // 不能為空
    }
};

// 匯出所有範例資料
window.ItnTripCardSamples = {
    SCHEMA: ITN_TRIP_DATA_SCHEMA,
    ITINERARY_SCHEMA: ITN_ITINERARY_ITEM_SCHEMA,
    BASIC: ITN_SAMPLE_TRIP_DATA_BASIC,
    FULL: ITN_SAMPLE_TRIP_DATA_FULL,
    CATEGORIES: ITN_SAMPLE_TRIP_DATA_CATEGORIES,
    API_RESPONSE: ITN_API_RESPONSE_EXAMPLE,
    ERRORS: ITN_ERROR_EXAMPLES
};

// 便利函數：獲取範例資料
window.getItnTripSampleData = function(type = 'FULL') {
    return window.ItnTripCardSamples[type] || window.ItnTripCardSamples.FULL;
};

// 便利函數：驗證資料格式
window.validateItnTripData = function(data) {
    const tripData = Array.isArray(data) ? data : [data];
    const errors = [];
    
    tripData.forEach((trip, index) => {
        const required = ['title', 'date', 'duration', 'groupSize', 'price', 'itinerary'];
        const missing = required.filter(field => !trip[field]);
        
        if (missing.length > 0) {
            errors.push(`第 ${index + 1} 筆資料缺少必要欄位: ${missing.join(', ')}`);
        }
        
        if (typeof trip.price !== 'number') {
            errors.push(`第 ${index + 1} 筆資料的價格必須是數字`);
        }
        
        if (!Array.isArray(trip.itinerary) || trip.itinerary.length === 0) {
            errors.push(`第 ${index + 1} 筆資料的行程安排必須是非空陣列`);
        }
    });
    
    return {
        valid: errors.length === 0,
        errors: errors
    };
}; 