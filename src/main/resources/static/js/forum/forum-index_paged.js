$(function () {
    let main_el = document.querySelector("#articleList");
    const contextPath = getFullContextPath(); // 獲取完整的上下文路徑

    const loadMoreBtn = document.querySelector("#loadMoreBtn");

    let currentPage = 0;
    const pageSize = 10;
    const sortBy = "artCreTime"; // 可改成 "artLike"
    const order = "DESC";        // 你自訂的 SortDirection：ASC / DESC

    // 初始載入第一頁
    loadArticles();

    // 點擊載入更多
    loadMoreBtn.addEventListener("click", loadArticles);

    function loadArticles() {
        // console.log("載入文章請求網址：", `${contextPath}/articles/paged?page=${currentPage}&size=${pageSize}&sortBy=${sortBy}&order=${order}`);

        fetch(`/articles/paged?page=${currentPage}&size=${pageSize}&sortBy=${sortBy}&order=${order}`)
            .then(response => {
                if (!response.ok) throw new Error("載入失敗");
                return response.json();
            })
            .then(result => {
                const data = result.articles;
                data.forEach(article => {
                    const card_el = createArticleCard(article);
                    main_el.append(card_el);
                });

                currentPage++;

                if (!result.hasNext) {
                    loadMoreBtn.style.display = "none";
                }
            })
            .catch(error => {
                console.error("文章載入錯誤：", error);
                loadMoreBtn.textContent = "載入失敗";
                loadMoreBtn.disabled = true;
            });
    }


    // 點擊整張卡片跳轉到單一文章瀏覽
    main_el.addEventListener("click", event => {
        const card = event.target.closest(".article-card");
        if (card) {
            const artId = card.dataset.artid;
            console.log(artId);
            if (artId) {
                window.location.href = `/forum/article?artId=${artId}`;
            }
        }
    });

});

// 取得 host + context path
function getFullContextPath() {

    const path = window.location.pathname;
    const firstSlash = path.indexOf("/", 1);
    const ctx = firstSlash === -1 ? "" : path.substring(0, firstSlash);
    return window.location.origin + ctx;
}

// 建立文章卡片
function createArticleCard(article) {
    // 文章類別對應
    const artCatMap = {
        1: "文章",
        2: "發問中",
        3: "已解決"
    }
    const artCat = artCatMap[article.artCat] || "文章*";  //若文章類別沒有被適當的歸類，顯示成文章*類別
    const artTitle = article.artTitle;

    const artPreview = article.artPreview || "";  // 從 DTO 直接拿純文字預覽

    // const artCon = function () {
    //     const cleanText = article.artCon.replace(/<img[^>]*>/gi, ""); // 移除 img 標籤
    //
    //     // 如果文章內容超過50個字，則截斷並加上省略號
    //     if (cleanText.length > 50) {
    //         return cleanText.substring(0, 50) + "...";
    //     } else {
    //         return cleanText;
    //     }
    // };
    const mamName = article.mamName;
    const artId = article.artId;
    const artLike = article.artLike;
    // 把日期轉換用台灣常用的日期
    const createDate = new Date(article.artCreTime).toLocaleDateString("zh-TW");

    const card = document.createElement("div");
    card.classList.add("article-card");
    card.dataset.artid = artId;  // 設定 data-artid 屬性，方便點擊事件使用

    card.innerHTML = `
        <div class="article-meta">
            <span class="category category-${article.artCat}">${artCat}</span>
            <strong>${artTitle}</strong>
            <p class="article-preview">${artPreview}</p>
            <div class="article-info">${mamName} · ${createDate}</div>
        </div>
        <div class="article-actions">
            <span class="article-like">
              <span class="material-icons">thumb_up</span> ${artLike}
            </span>
            <span>#${artId}</span>
        </div>
    `;

    return card;
}