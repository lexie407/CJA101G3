/**
 * ========================================
 * GroupActivity 模組 - 核心工具函數
 * ========================================
 */

window.GroupActivityCore = (function () {
  "use strict";

  // 狀態映射對照表
  const STATUS_MAP = {
    0: { text: "招募中", class: "recruiting" },
    1: { text: "成團", class: "full" },
    2: { text: "未成團", class: "ended" },
    3: { text: "已取消", class: "ended" },
    4: { text: "已凍結", class: "ended" },
    5: { text: "已結束", class: "ended" },
  };

  /**
   * 格式化日期為本地化字串
   * @param {string} dateString - ISO 日期字串
   * @returns {string} 格式化後的日期字串
   */
  function formatDate(dateString) {
    if (!dateString) return "未設定";

    const date = new Date(dateString);
    return date.toLocaleDateString("zh-TW", {
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
    });
  }

  /**
   * 取得活動狀態的文字和 CSS 類別
   * @param {number} status - 狀態代碼
   * @returns {Object} 包含 text 和 class 的物件
   */
  function getStatusText(status) {
    return STATUS_MAP[status] || { text: "未知", class: "ended" };
  }

  /**
   * 防抖函數
   * @param {Function} func - 要執行的函數
   * @param {number} wait - 等待時間（毫秒）
   * @returns {Function} 防抖後的函數
   */
  function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
      const later = () => {
        clearTimeout(timeout);
        func(...args);
      };
      clearTimeout(timeout);
      timeout = setTimeout(later, wait);
    };
  }

  /**
   * 節流函數
   * @param {Function} func - 要執行的函數
   * @param {number} limit - 時間限制（毫秒）
   * @returns {Function} 節流後的函數
   */
  function throttle(func, limit) {
    let inThrottle;
    return function (...args) {
      if (!inThrottle) {
        func.apply(this, args);
        inThrottle = true;
        setTimeout(() => (inThrottle = false), limit);
      }
    };
  }

  /**
   * 安全的 JSON 解析
   * @param {string} jsonString - JSON 字串
   * @param {*} defaultValue - 預設值
   * @returns {*} 解析結果或預設值
   */
  function safeJsonParse(jsonString, defaultValue = null) {
    try {
      return JSON.parse(jsonString);
    } catch (e) {
      console.error("JSON parse error:", e);
      return defaultValue;
    }
  }

  /**
   * 顯示載入狀態
   * @param {HTMLElement} element - 目標元素
   * @param {string} message - 載入訊息
   */
  function showLoading(element, message = "載入中...") {
    element.classList.add("act-loading");
    element.innerHTML = message;
  }

  /**
   * 隱藏載入狀態
   * @param {HTMLElement} element - 目標元素
   */
  function hideLoading(element) {
    element.classList.remove("act-loading");
  }

  /**
   * 顯示錯誤訊息
   * @param {HTMLElement} element - 目標元素
   * @param {string} message - 錯誤訊息
   * @param {string} details - 詳細訊息
   */
  function showError(element, message = "發生錯誤", details = "") {
    element.innerHTML = `
      <div class="act-error-container">
        <h3>${message}</h3>
        ${details ? `<p>${details}</p>` : ""}
      </div>
    `;
  }

  /**
   * 顯示空狀態
   * @param {HTMLElement} element - 目標元素
   * @param {string} title - 標題
   * @param {string} message - 訊息
   */
  function showEmptyState(element, title = "沒有資料", message = "") {
    element.innerHTML = `
      <div class="act-empty-state">
        <h3>${title}</h3>
        ${message ? `<p>${message}</p>` : ""}
      </div>
    `;
  }

  /**
   * 驗證電子郵件格式
   * @param {string} email - 電子郵件地址
   * @returns {boolean} 是否有效
   */
  function isValidEmail(email) {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(email);
  }

  /**
   * 驗證日期範圍
   * @param {string} startDate - 開始日期
   * @param {string} endDate - 結束日期
   * @returns {boolean} 是否有效
   */
  function isValidDateRange(startDate, endDate) {
    if (!startDate || !endDate) return true; // 允許空值
    return new Date(startDate) <= new Date(endDate);
  }

  // 公開的 API
  return {
    // 核心工具函數
    formatDate,
    getStatusText,
    debounce,
    throttle,
    safeJsonParse,

    // UI 輔助函數
    showLoading,
    hideLoading,
    showError,
    showEmptyState,

    // 驗證函數
    isValidEmail,
    isValidDateRange,

    // 常數
    STATUS_MAP,
  };
})();

// ===========================================
// 全域函數定義（供 HTML 中 onclick 事件使用）
// ===========================================

/**
 * 查看活動詳情
 * @param {number} actId - 活動 ID
 */
window.viewAct = function (actId) {
  window.location.href = `/act/member/view/${actId}`;
};

/**
 * 編輯活動
 * @param {number} actId - 活動 ID
 */
window.editAct = function (actId) {
  window.location.href = `/act/member/edit/${actId}`;
};

/**
 * 刪除活動（設為已取消狀態）
 * @param {number} actId - 活動 ID
 * @param {number} hostId - 團主 ID
 */
window.deleteAct = function (actId, hostId) {
  if (confirm("確定要刪除這個活動嗎？此操作無法復原。")) {
    fetch(`/api/act/${actId}/status/3?operatorId=${hostId}&admin=false`, {
      method: "PUT",
    })
        .then((response) => {
          if (response.ok) {
            location.reload();
          } else {
            alert("刪除失敗，請稍後再試");
          }
        })
        .catch((error) => {
          console.error("刪除失敗:", error);
          alert("刪除失敗，請稍後再試");
        });
  }
};

/**
 * 會員刪除活動（僅限未公開活動）
 * @param {number} actId - 活動 ID
 */
window.memberDeleteAct = function (actId) {
  if (confirm("確定要刪除這個活動嗎？此操作無法復原。")) {
    GroupActivityAPI.memberDeleteActivity(actId)
        .then((response) => {
          if (response.success) {
            alert("活動刪除成功");
            location.reload();
          } else {
            alert(response.error || "刪除失敗，請稍後再試");
          }
        })
        .catch((error) => {
          console.error("刪除失敗:", error);
          alert("刪除失敗，請稍後再試");
        });
  }
};

/**
 * 參加活動
 * @param {number} actId - 活動 ID
 */
window.joinActivity = function (actId) {
  // 從頁面取得 currentMemId，如果沒有則提示登入
  const currentMemId = window.currentMemId;
  if (!currentMemId) {
    alert("請先登入");
    // 將當前頁面URL作為查詢參數傳遞，以便登入後返回
    const currentUrl = encodeURIComponent(window.location.href);
    window.location.href = `/members/login?redirect=${currentUrl}`;
    return;
  }

  GroupActivityAPI.joinActivity(actId, currentMemId)
      .then((response) => {
        if (response.success) {
          alert("報名成功！");
          location.reload();
        } else {
          alert(response.error || "報名失敗，請稍後再試");
        }
      })
      .catch((error) => {
        console.error("報名失敗:", error);
        alert("報名失敗，請稍後再試");
      });
};

/**
 * 取消參加活動
 * @param {number} actId - 活動 ID
 */
window.cancelParticipation = function (actId) {
  if (!confirm("確定要取消報名嗎？")) {
    return;
  }

  // 從頁面取得 currentMemId，如果沒有則提示登入
  const currentMemId = window.currentMemId;
  if (!currentMemId) {
    alert("請先登入");
    // 將當前頁面URL作為查詢參數傳遞，以便登入後返回
    const currentUrl = encodeURIComponent(window.location.href);
    window.location.href = `/members/login?redirect=${currentUrl}`;
    return;
  }

  GroupActivityAPI.leaveActivity(actId, currentMemId)
      .then((response) => {
        if (response.success) {
          alert("取消報名成功");
          location.reload();
        } else {
          alert(response.error || "取消報名失敗，請稍後再試");
        }
      })
      .catch((error) => {
        console.error("取消報名失敗:", error);
        alert("取消報名失敗，請稍後再試");
      });
};
