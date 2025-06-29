document.addEventListener('DOMContentLoaded', function (){

    // 畫面上的分類選擇功能
    // document.querySelectorAll('.chip').forEach(chip => {
    //     chip.addEventListener('click', function() {
    //         document.querySelectorAll('.chip').forEach(c => c.classList.remove('selected'));
    //         this.classList.add('selected');
    //     });
    // });

    // 分類選擇功能
    document.querySelectorAll('.category-chips .chip').forEach(chip => {
        chip.addEventListener('click', () => {
            // 移除所有 chip 的選取狀態
            document.querySelectorAll('.category-chips .chip').forEach(c => c.classList.remove('selected'));

            // 設定目前點到的 chip 為 selected
            chip.classList.add('selected');

            // 將 data-category 的值寫入 hidden input
            const selectedCategory = chip.getAttribute('data-category');
            document.getElementById('artCat').value = selectedCategory;
        });
    });



    // 表單送出
    document.getElementById('articleForm').addEventListener('submit', async function (e) {
        e.preventDefault(); // 一律先攔截

        // 文章內容檢查
        const content = quill.root.innerHTML.trim();
        if (!content) {
            alert('文章內容為必填！');
            return;
        }

        // 標題檢查
        const title = document.getElementById('title').value.trim();
        if (!title) {
            alert('標題為必填！');
            return;
        }

        // 會員編號檢查
        const artHol = document.getElementById('artHol').value.trim();
        if (!artHol) {
            alert('會員編號為必填！');
            return;
        }

        // 文章類型
        const artCat = document.getElementById('artCat').value;

        // 狀態固定為1
        document.getElementById('artSta').value = "1";

        // 填入標題
        document.getElementById('title').value = title;

        // 填入內容
        document.getElementById('artCon').value = content;

        // 送出表單
        e.target.submit();
    });
});

// 用來查看編輯器目前內容(測試用)
function getContent() {
    const quillhtml = quill.root.innerHTML;
    document.getElementById('output').textContent = quillhtml;
}

// 取消儲存
function cancelForm() {
    if (confirm('確定要取消嗎？現在寫的內容都會消失喔！')) {
        history.back();
    }
}