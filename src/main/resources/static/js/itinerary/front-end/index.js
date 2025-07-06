/**
 * 行程首頁 JavaScript
 */

// DOM 載入完成後初始化
document.addEventListener('DOMContentLoaded', function() {
    initializeItineraryHome();
});

/**
 * 初始化行程首頁
 */
function initializeItineraryHome() {
    // 綁定登入按鈕事件
    bindLoginButton();
    
    console.log('行程首頁初始化完成');
}

/**
 * 綁定登入按鈕事件
 */
function bindLoginButton() {
    const loginButton = document.querySelector('.logout-btn');
    if (loginButton) {
        loginButton.addEventListener('click', function(e) {
            e.preventDefault();
            // 保存當前頁面URL到localStorage，以便登入後重定向回來
            localStorage.setItem('loginRedirectUrl', window.location.href);
            // 跳轉到登入頁面
            window.location.href = '/members/login?redirect=' + encodeURIComponent(window.location.href);
        });
    }
} 