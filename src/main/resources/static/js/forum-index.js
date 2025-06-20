$(function () {
    let main_el = document.querySelector("main");
    const forumPath = "/forum"; // 這是論壇的基礎路徑

    fetch("/articles?sortBy=artCreTime&order=desc") // 取得最新文章
        .then(response => {
            if (!response.ok) {
                // 開發除錯用：輸出詳細錯誤
                console.error(`Fetch error: ${response.status} ${response.statusText}`);
                throw new Error("載入文章失敗");
            }
            return response.json();
        })
        .then(data => {
            data.forEach(article => {
                let card_el = createArticleCard(article);
                main_el.append(card_el);
            });
        })
        .catch(error => {
            console.error("文章載入錯誤：", error); // 開發除錯用
            main_el.html(`<p>${error.message}</p>`);
        });

    // 點擊整張卡片跳轉到文章詳情頁(施工中)
    // main_el.addEventListener("click", event => {
    //     const card = event.target.closest(".article-card");
    //     if (card) {
    //         const artId = card.dataset.artid;
    //         console.log(artId);
    //         if (artId) {
    //             window.location.href = `${forumPath}/article?artId=${artId}`;
    //         }
    //     }
    // })

});

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
    const artCon = function () {
        // 如果文章內容超過50個字，則截斷並加上省略號
        if (article.artCon.length > 50) {
            return article.artCon.substring(0, 50) + "...";
        } else {
            return article.artCon;
        }
    };
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
            <p class="article-preview">${artCon()}</p>
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