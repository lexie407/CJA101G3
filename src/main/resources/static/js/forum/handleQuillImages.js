async function handleQuillImages(quill) {
    const temp = document.createElement('div');
    temp.innerHTML = quill.root.innerHTML;

    const imgElements = temp.querySelectorAll('img');

    for (let img of imgElements) {
        const src = img.getAttribute('src');
        if (src && src.startsWith('data:')) {
            const timestamp = Date.now();
            const blob = await fetch(src).then(res => res.blob());
            const formData = new FormData();
            formData.append('image', blob, `upload_${timestamp}.png`);

            const res = await fetch('/forum/artImage/upload', {
                method: 'POST',
                body: formData
            });

            if (!res.ok) {
                const errText = await res.text();
                throw new Error(`圖片上傳失敗：${res.status} - ${errText}`);
            }

            const result = await res.json();
            img.setAttribute('src', `/forum/artImage/${result.id}`);
        }
    }

    return temp.innerHTML;
}