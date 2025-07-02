// favorite.js
export function toggleFavorite(spotId, isFavorited, updateFavoriteButton, updateFavoriteCount, showAlert) {
    $.ajax({
        url: `/api/spot/favorites/${spotId}/toggle`,
        type: 'POST',
        success: function(response) {
            if (response.success) {
                isFavorited = !isFavorited;
                updateFavoriteButton(isFavorited);
                updateFavoriteCount();
                showAlert('success', response.message);
            } else {
                showAlert('danger', response.message || '操作失敗');
            }
        },
        error: function(xhr) {
            let message = '操作失敗';
            if (xhr.status === 401) {
                message = '請先登入';
            } else if (xhr.responseJSON && xhr.responseJSON.message) {
                message = xhr.responseJSON.message;
            }
            showAlert('danger', message);
        }
    });
}

export function updateFavoriteButton(isFavorited) {
    const btn = document.getElementById('favoriteBtn');
    const span = btn.querySelector('span');
    if (isFavorited) {
        btn.className = 'btn btn-danger me-3';
        span.textContent = '已收藏';
    } else {
        btn.className = 'btn btn-outline-light me-3';
        span.textContent = '加入收藏';
    }
}

export function updateFavoriteCount(spotId) {
    $.ajax({
        url: `/api/spot/favorites/${spotId}/count`,
        type: 'GET',
        success: function(response) {
            if (response.success) {
                document.getElementById('favoriteCount').textContent = response.data;
            }
        }
    });
} 