package com.toiukha.groupactivity.model;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 活動標籤Enum
 * 僅存入redis使用
 */
public enum ActTag {
    // 活動類型（8個分類）
    ARTS("藝文", "type"),
    ECOLOGY("生態", "type"),
    OUTDOOR("戶外", "type"), 
    SPORTS("運動", "type"),
    FOOD("美食", "type"),
    FAMILY("親子", "type"),
    SENIOR("銀髮", "type"),
    PET("寵物", "type"),
    
    // 縣市分類
    TAIPEI("台北", "city"),
    NEW_TAIPEI("新北", "city"),
    TAOYUAN("桃園", "city"),
    TAICHUNG("台中", "city"),
    TAINAN("台南", "city"),
    KAOHSIUNG("高雄", "city"),
    KEELUNG("基隆", "city"),
    HSINCHU("新竹", "city"),
    MIAOLI("苗栗", "city"),
    CHANGHUA("彰化", "city"),
    NANTOU("南投", "city"),
    YUNLIN("雲林", "city"),
    CHIAYI("嘉義", "city"),
    PINGTUNG("屏東", "city"),
    YILAN("宜蘭", "city"),
    HUALIEN("花蓮", "city"),
    TAITUNG("台東", "city"),
    ISLANDS("離島", "city");

    private final String displayName;
    private final String category; // "type" 或 "city"

    ActTag(String displayName, String category) {
        this.displayName = displayName;
        this.category = category;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCategory() {
        return category;
    }

    //獲取所有類型標籤
    public static List<ActTag> getTypesTags() {
        return Arrays.stream(values())
            .filter(tag -> "type".equals(tag.category))
            .collect(Collectors.toList());
    }

    //獲取所有縣市標籤
    public static List<ActTag> getCityTags() {
        return Arrays.stream(values())
            .filter(tag -> "city".equals(tag.category))
            .collect(Collectors.toList());
    }
} 