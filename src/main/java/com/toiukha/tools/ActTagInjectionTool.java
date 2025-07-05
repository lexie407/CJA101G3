package com.toiukha.tools;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.*;

/**
 * 活動標籤注入工具
 * 用途：批量寫入活動標籤資料到 Redis
 * 功能：支援從 JSON 檔案讀取或使用預設假資料批量寫入 Redis
 * 使用方式：直接在 IDE 中執行此 Java 程式
 * 
 * 注意事項：
 * 1. 需要先啟動 Redis 服務
 * 2. 需要先啟動 Spring Boot 應用程式（因為需要 RedisTemplate）
 * 3. 此工具會清空現有的 groupactivity 標籤資料並重新寫入
 */
public class ActTagInjectionTool {
    
    // 資料庫連線設定
    private static final String DB_URL = "jdbc:mysql://localhost:3306/toiukha?serverTimezone=Asia/Taipei";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "123456";
    
    // Redis key 前綴
    private static final String REDIS_KEY_PREFIX = "groupactivity:";
    
    // 活動類型標籤
    private static final String[] ACT_TYPE_TAGS = {
        "美食", "文化", "自然", "運動", "購物", "娛樂", "教育", "社交"
    };
    
    // 地區標籤（大區）
    private static final String[] ACT_CITY_TAGS = {
        "台北", "新北", "桃園", "台中", "台南", "高雄", "基隆", "新竹", "嘉義", "宜蘭", "花蓮", "台東", "澎湖", "金門", "連江"
    };
    
    // 預設假資料 - 每個活動的標籤配置
    private static final Map<Integer, List<String>> DEFAULT_ACT_TAGS = new HashMap<>();
    
    static {
        // 活動 1: 美食 + 台北
        DEFAULT_ACT_TAGS.put(1, Arrays.asList("美食", "台北"));
        
        // 活動 2: 文化 + 台中
        DEFAULT_ACT_TAGS.put(2, Arrays.asList("文化", "台中"));
        
        // 活動 3: 自然 + 花蓮
        DEFAULT_ACT_TAGS.put(3, Arrays.asList("自然", "花蓮"));
        
        // 活動 4: 運動 + 新北
        DEFAULT_ACT_TAGS.put(4, Arrays.asList("運動", "新北"));
        
        // 活動 5: 購物 + 台北
        DEFAULT_ACT_TAGS.put(5, Arrays.asList("購物", "台北"));
        
        // 活動 6: 娛樂 + 高雄
        DEFAULT_ACT_TAGS.put(6, Arrays.asList("娛樂", "高雄"));
        
        // 活動 7: 教育 + 台南
        DEFAULT_ACT_TAGS.put(7, Arrays.asList("教育", "台南"));
        
        // 活動 8: 社交 + 桃園
        DEFAULT_ACT_TAGS.put(8, Arrays.asList("社交", "桃園"));
        
        // 活動 9: 美食 + 文化 + 台北
        DEFAULT_ACT_TAGS.put(9, Arrays.asList("美食", "文化", "台北"));
        
        // 活動 10: 自然 + 運動 + 宜蘭
        DEFAULT_ACT_TAGS.put(10, Arrays.asList("自然", "運動", "宜蘭"));
    }

    public static void main(String[] args) {
        System.out.println("=== 活動標籤注入工具 ===");
        System.out.println("此工具將批量寫入活動標籤資料到 Redis");
        System.out.println("Redis Key 前綴: " + REDIS_KEY_PREFIX);
        System.out.println("========================================");
        
        try {
            // 檢查資料庫連線
            if (!checkDatabaseConnection()) {
                System.out.println("❌ 資料庫連線失敗，請檢查資料庫設定");
                return;
            }
            
            // 取得所有活動 ID
            List<Integer> actIds = getAllActIds();
            if (actIds.isEmpty()) {
                System.out.println("❌ 資料庫中沒有找到任何活動");
                return;
            }
            
            System.out.println("找到 " + actIds.size() + " 個活動");
            
            // 選擇資料來源
            Scanner scanner = new Scanner(System.in);
            System.out.println("\n請選擇資料來源：");
            System.out.println("1. 使用預設假資料");
            System.out.println("2. 從 JSON 檔案讀取");
            System.out.print("請輸入選擇 (1 或 2): ");
            
            int choice = scanner.nextInt();
            
            Map<Integer, List<String>> actTags = new HashMap<>();
            
            switch (choice) {
                case 1:
                    actTags = useDefaultData(actIds);
                    break;
                case 2:
                    actTags = loadFromJsonFile(actIds);
                    break;
                default:
                    System.out.println("❌ 無效的選擇");
                    return;
            }
            
            if (actTags.isEmpty()) {
                System.out.println("❌ 沒有有效的標籤資料");
                return;
            }
            
            // 確認執行
            System.out.println("\n準備寫入以下標籤資料：");
            for (Map.Entry<Integer, List<String>> entry : actTags.entrySet()) {
                System.out.println("活動 " + entry.getKey() + ": " + String.join(", ", entry.getValue()));
            }
            
            System.out.print("\n確認執行？(y/n): ");
            String confirm = scanner.next();
            
            if (!"y".equalsIgnoreCase(confirm)) {
                System.out.println("❌ 操作已取消");
                return;
            }
            
            // 執行寫入
            writeTagsToRedis(actTags);
            
        } catch (Exception e) {
            System.out.println("❌ 程式執行錯誤：" + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 檢查資料庫連線
     */
    private static boolean checkDatabaseConnection() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            System.out.println("✅ 資料庫連線成功");
            return true;
        } catch (SQLException e) {
            System.out.println("❌ 資料庫連線失敗：" + e.getMessage());
            return false;
        }
    }
    
    /**
     * 取得所有活動 ID
     */
    private static List<Integer> getAllActIds() throws SQLException {
        List<Integer> actIds = new ArrayList<>();
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement("SELECT ACTID FROM groupactivity ORDER BY ACTID");
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                actIds.add(rs.getInt("ACTID"));
            }
        }
        
        return actIds;
    }
    
    /**
     * 使用預設假資料
     */
    private static Map<Integer, List<String>> useDefaultData(List<Integer> actIds) {
        Map<Integer, List<String>> result = new HashMap<>();
        
        for (Integer actId : actIds) {
            if (DEFAULT_ACT_TAGS.containsKey(actId)) {
                result.put(actId, DEFAULT_ACT_TAGS.get(actId));
            } else {
                // 為沒有預設標籤的活動隨機分配標籤
                List<String> randomTags = generateRandomTags();
                result.put(actId, randomTags);
            }
        }
        
        return result;
    }
    
    /**
     * 從 JSON 檔案讀取標籤資料
     */
    private static Map<Integer, List<String>> loadFromJsonFile(List<Integer> actIds) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("請輸入 JSON 檔案路徑 (例如: act_tags.json): ");
        String filePath = scanner.nextLine();
        
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("❌ 檔案不存在：" + filePath);
            return new HashMap<>();
        }
        
        try {
            Gson gson = new Gson();
            String jsonContent = new String(java.nio.file.Files.readAllBytes(file.toPath()));
            
            // 解析 JSON 格式：{"1": ["美食", "台北"], "2": ["文化", "台中"]}
            TypeToken<Map<String, List<String>>> token = new TypeToken<Map<String, List<String>>>() {};
            Map<String, List<String>> jsonData = gson.fromJson(jsonContent, token.getType());
            
            Map<Integer, List<String>> result = new HashMap<>();
            
            for (Integer actId : actIds) {
                String actIdStr = String.valueOf(actId);
                if (jsonData.containsKey(actIdStr)) {
                    result.put(actId, jsonData.get(actIdStr));
                } else {
                    // 為沒有 JSON 資料的活動隨機分配標籤
                    List<String> randomTags = generateRandomTags();
                    result.put(actId, randomTags);
                }
            }
            
            System.out.println("✅ 成功從 JSON 檔案讀取 " + jsonData.size() + " 筆標籤資料");
            return result;
            
        } catch (Exception e) {
            System.out.println("❌ 讀取 JSON 檔案失敗：" + e.getMessage());
            return new HashMap<>();
        }
    }
    
    /**
     * 生成隨機標籤
     */
    private static List<String> generateRandomTags() {
        List<String> tags = new ArrayList<>();
        Random random = new Random();
        
        // 隨機選擇 1-3 個活動類型標籤
        int typeCount = random.nextInt(3) + 1;
        List<String> availableTypes = new ArrayList<>(Arrays.asList(ACT_TYPE_TAGS));
        Collections.shuffle(availableTypes);
        
        for (int i = 0; i < typeCount && i < availableTypes.size(); i++) {
            tags.add(availableTypes.get(i));
        }
        
        // 隨機選擇 1 個地區標籤
        String randomCity = ACT_CITY_TAGS[random.nextInt(ACT_CITY_TAGS.length)];
        tags.add(randomCity);
        
        return tags;
    }
    
    /**
     * 寫入標籤到 Redis
     * 注意：此方法需要 Spring Boot 應用程式正在運行
     */
    private static void writeTagsToRedis(Map<Integer, List<String>> actTags) {
        System.out.println("\n⚠️  注意：此步驟需要 Spring Boot 應用程式正在運行");
        System.out.println("請確保應用程式已啟動並且 Redis 服務可用");
        System.out.println("然後在 ActApiController 中呼叫批量寫入方法");
        
        // 這裡我們輸出 JSON 格式，方便複製到 ActApiController 中使用
        System.out.println("\n=== 標籤資料 (JSON 格式) ===");
        Gson gson = new Gson();
        String jsonData = gson.toJson(actTags);
        System.out.println(jsonData);
        
        System.out.println("\n=== 使用方式 ===");
        System.out.println("1. 複製上面的 JSON 資料");
        System.out.println("2. 在 ActApiController 中新增測試端點");
        System.out.println("3. 使用 ActTagService 的批量寫入方法");
        System.out.println("4. 或者直接使用 RedisTemplate 寫入");
        
        // 生成 Redis 命令範例
        System.out.println("\n=== Redis 命令範例 ===");
        for (Map.Entry<Integer, List<String>> entry : actTags.entrySet()) {
            String key = REDIS_KEY_PREFIX + "act:" + entry.getKey() + ":tags";
            String value = String.join(",", entry.getValue());
            System.out.println("SET " + key + " \"" + value + "\"");
        }
        
        System.out.println("\n✅ 標籤資料準備完成！");
        System.out.println("請在 ActApiController 中實作批量寫入邏輯");
    }
    
    /**
     * 生成 JSON 檔案範例
     */
    public static void generateJsonExample() {
        Map<Integer, List<String>> exampleData = new HashMap<>();
        exampleData.put(1, Arrays.asList("美食", "台北"));
        exampleData.put(2, Arrays.asList("文化", "台中"));
        exampleData.put(3, Arrays.asList("自然", "花蓮"));
        exampleData.put(4, Arrays.asList("運動", "新北"));
        exampleData.put(5, Arrays.asList("購物", "台北"));
        
        Gson gson = new Gson();
        String json = gson.toJson(exampleData);
        
        try (FileWriter writer = new FileWriter("act_tags_example.json")) {
            writer.write(json);
            System.out.println("✅ JSON 範例檔案已生成：act_tags_example.json");
        } catch (IOException e) {
            System.out.println("❌ 生成 JSON 範例檔案失敗：" + e.getMessage());
        }
    }
} 