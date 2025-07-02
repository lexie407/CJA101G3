// ui.js
export function showAlert(type, message) {
    const alertHtml = `
        <div class="alert alert-${type} alert-dismissible fade show position-fixed" 
             style="top: 20px; right: 20px; z-index: 9999; min-width: 300px;" role="alert">
            <i class="fas fa-${type === 'success' ? 'check-circle' : 'exclamation-triangle'} me-2"></i>
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    `;
    $('.alert').remove();
    $('body').append(alertHtml);
    setTimeout(function() {
        $('.alert').fadeOut(function() {
            $(this).remove();
        });
    }, 3000);
}

export function showError(message) {
    showAlert('danger', message);
}

export function shareSpot(spotName) {
    if (navigator.share) {
        navigator.share({
            title: spotName,
            text: `來看看這個景點：${spotName}`,
            url: window.location.href
        });
    } else {
        navigator.clipboard.writeText(window.location.href).then(function() {
            alert('景點連結已複製到剪貼簿！');
        });
    }
}

export function addToItinerary() {
    alert('加入行程功能開發中，將整合行程管理系統...');
}

export function joinGroupActivity() {
    alert('揪團功能開發中，將整合揪團系統...');
}

export function reportSpot() {
    if (confirm('確定要檢舉此景點嗎？')) {
        alert('檢舉功能開發中，將整合檢舉系統...');
    }
} 