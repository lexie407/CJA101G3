document.addEventListener('DOMContentLoaded', function (){

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

        // ----------- 圖片處理區塊 -----------
        const temp = document.createElement('div');
        temp.innerHTML = quill.root.innerHTML;

        const imgElements = temp.querySelectorAll('img');
        for (let img of imgElements) {
            const src = img.getAttribute('src');
            if (src && src.startsWith('data:')) {
                const timestamp = Date.now(); // 例如 1720041180277
                const blob = await fetch(src).then(res => res.blob());
                const formData = new FormData();
                formData.append('image', blob, `upload_${timestamp}.png`);

                const res = await fetch('/forum/artImage/upload', {
                    method: 'POST',
                    body: formData
                });

                const result = await res.json();
                img.setAttribute('src', `/forum/artImage/${result.id}`); // 替換 src
            }
        }
        const finalHTML = temp.innerHTML;
        // ----------- 圖片處理區塊結束 -----------

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

        // 內容檢查（清理後內容）
        if (!finalHTML || finalHTML === '<p><br></p>') {
            alert('文章內容為必填！');
            return;
        }

        // 文章類型
        // const artCat = document.getElementById('artCat').value;

        // 狀態固定為1
        document.getElementById('artSta').value = "1";
        document.getElementById('title').value = title;
        document.getElementById('artCon').value = finalHTML;

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