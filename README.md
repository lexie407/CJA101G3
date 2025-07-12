# CJA101G3 島遊Kha

## 專案簡介
在規劃台灣旅遊時，複雜的行程規劃常常令人感到困擾，獨自旅行時也可能面臨難以找到旅伴的窘境，旅途中的心得與攻略缺乏合適的分享平台。因此，
我們打造一個專屬於台灣旅遊愛好者的交流平台，匯集多元的行程資訊，提供旅人編輯行程、發起揪團旅遊、採購旅遊票卷，盡情分享心得與攻略的空間。

## 使用技術
<!-- 後端技術 -->
![Java](https://img.shields.io/badge/-Java-007396?logo=java&logoColor=white&style=flat-square)
![Spring Boot](https://img.shields.io/badge/-Spring_Boot-6DB33F?logo=spring-boot&logoColor=white&style=flat-square)
![Spring MVC](https://img.shields.io/badge/-Spring_MVC-6DB33F?logo=spring&logoColor=white&style=flat-square)
![JPA](https://img.shields.io/badge/-JPA-59666C?logo=hibernate&logoColor=white&style=flat-square)
![Hibernate](https://img.shields.io/badge/-Hibernate-59666C?logo=hibernate&logoColor=white&style=flat-square)
![Maven](https://img.shields.io/badge/-Maven-C71A36?logo=apachemaven&logoColor=white&style=flat-square)

<!-- 資料管理 -->
![MySQL](https://img.shields.io/badge/-MySQL-4479A1?logo=mysql&logoColor=white&style=flat-square)
![Redis](https://img.shields.io/badge/-Redis-DC382D?logo=redis&logoColor=white&style=flat-square)

<!-- 前端整合 -->
![Thymeleaf](https://img.shields.io/badge/-Thymeleaf-005F0F?logo=thymeleaf&logoColor=white&style=flat-square)
![jQuery](https://img.shields.io/badge/-jQuery-0769AD?logo=jquery&logoColor=white&style=flat-square)
![AJAX](https://img.shields.io/badge/-AJAX-0081CB?logo=jquery&logoColor=white&style=flat-square)
![Fetch](https://img.shields.io/badge/-Fetch-000000?logo=javascript&logoColor=white&style=flat-square)
![Bootstrap](https://img.shields.io/badge/-Bootstrap-7952B3?logo=bootstrap&logoColor=white&style=flat-square)
![DataTables](https://img.shields.io/badge/-DataTables-1E90FF?logo=databricks&logoColor=white&style=flat-square)


## 開發協作流程(每天務必執行)
1. 開發前同步主線  
    Pull… + 選 master + Rebase  
    即使沒寫程式，也要先 rebase !!!
2. 切換到自己分支開始開發  
    Team → Switch To 自己的分支
3. 若只是進度中  
    選擇 commit  
    不需 push、不開 PR
4. 若功能完成  
    commit + push  
    再開 PR（選 master 為目標）
5. 組長負責 Review  
    Approve 允許  
    Request Changes 退回
    *免費版：只能用留言留下意見
6. 管理員 負責 Merge PR
7. 若 PR 無法合併（conflict）  
    PR 發起人需再次 rebase main  
    解決衝突後重新 push

## 其他注意事項
#Java package 命名建議

- 使用 `com.toiukha.<功能>`，例如：
    - `com.toiukha.groupactivity`
    - `com.toiukha.member`
    - `com.toiukha.itinerary`

#檔案與資料夾結構建議

```
src
└── main
    ├── java
    │   └── com
    │       └── toiukha  
    │           ├── groupactivity      ← 自己建立資料夾，放自己的模組檔案
    │           │   ├── controller
    │           │   ├── service
    │           │   ├── model
    │           │   └── dao
    │           ├── member
    │           └── itinerary
    └── resources
        ├── static                  ← 放全部的css, js, images...(注意命名與分類)
        ├── templates               ← 放 Thymeleaf 的 HTML 檔案 (依照前後台>自己建立資料夾)
        │   ├── back-end
        │   │   ├── groupactivity
        │   │   ├── member
        │   │   └── itinerary
        │   └── front-end
        │       ├── groupactivity
        │       ├── member
        │       └── itinerary
        ├── application.properties           ← 資料庫設置，上線時統一合併，先不要動
        ├── application-local.example        ← 範本檔，供大家複製修改(不要直接改)
        └── application-local.properties     ← 自己新增，設定自己的連線池&帳密(不會push)


```

#資料庫連線設定說明

請每位組員於 `src/main/resources` 中建立 `application-local.properties`，設定以下個人連線資訊：

```
spring.datasource.url=jdbc:mysql://localhost:3306/g3_<暱稱>?serverTimezone=Asia/Taipei
spring.datasource.username=你的帳號
spring.datasource.password=你的密碼

```

📌 此檔案不應被 commit，已在 `.gitignore` 中排除，保障安全。 📌 範例檔 `application-local.example` 由組長提供，clone 時可直接複製改名為 `application-local.properties`


#thymeleaf template操作說明  
1/ src > main > resources > templates > thymeleaf_template.html 複製到各自的front 或 back使用  
2/ thymeleaf_template.html中 line17 line22請放入自己的內容  
3/ 自己的controller各方法內請加上「model.addAttribute("currentPage", "???");」???是放所屬頁面屬性的值，這屬性是為了讓左側導覽列知道目前的頁面，讓其標籤套用目前頁面的CSS  
    各頁面英文屬性值如下  
    首頁: home  
    行程: travel  
    揪團: groups  
    商店: store  
    討論區: forum  
    會員中心: account  
    成為廠商: bePartner  
