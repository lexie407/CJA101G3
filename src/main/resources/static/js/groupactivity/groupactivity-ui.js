/**
 * ========================================
 * GroupActivity 模組 - UI 組件生成器
 * ========================================
 */

window.GroupActivityUI = (function () {
  "use strict";

  // 確保 GroupActivityCore 已載入
  if (typeof GroupActivityCore === "undefined") {
    console.error("GroupActivityCore is required but not loaded");
    return {};
  }

  const { formatDate, getStatusText } = GroupActivityCore;

  /**
   * 創建活動卡片 HTML
   * @param {Object} activity - 活動資料
   * @param {Object} options - 選項配置
   * @returns {string} HTML 字串
   */
  function createActCard(activity, options = {}) {
    const {
      showActions = true,
      showHostActions = false,
      showImage = true,
      template = "default",
      customActions = [],
    } = options;

    const status = getStatusText(activity.recruitStatus);

    // 圖片 HTML
    let imageHtml = "";
    if (showImage) {
      imageHtml = `<img src="/api/act/image/${activity.actId}" alt="活動圖片" class="act-card-image">`;
    }

    // 基本資訊 - 根據模板類型調整
    let infoHtml = "";
    if (template === "myJoin") {
      // 我跟的團：顯示團主資訊
      infoHtml = `
        <div class="act-card-info">
          <div class="act-info-item">
            <span class="act-info-label">人數</span>
            <span class="act-info-value">${activity.signupCnt || 0}/${
        activity.maxCap || 0
      }</span>
          </div>
          <div class="act-info-item">
            <span class="act-info-label">開始日期</span>
            <span class="act-info-value">${formatDate(activity.actStart)}</span>
          </div>
          <div class="act-info-item">
            <span class="act-info-label">團主</span>
            <span class="act-info-value">會員 ${activity.hostId}</span>
          </div>
        </div>
      `;
    } else {
      // 預設：基本資訊
      infoHtml = `
        <div class="act-card-info">
          <div class="act-info-item">
            <span class="act-info-label">人數</span>
            <span class="act-info-value">${activity.signupCnt || 0}/${
        activity.maxCap || 0
      }</span>
          </div>
          <div class="act-info-item">
            <span class="act-info-label">開始日期</span>
            <span class="act-info-value">${formatDate(activity.actStart)}</span>
          </div>
        </div>
      `;
    }

    // 狀態標籤
    const statusHtml = `<span class="act-status ${status.class}">${status.text}</span>`;

    // 操作按鈕
    let actionsHtml = "";
    if (showActions) {
      let actions = [];

      if (template === "myJoin") {
        // 我跟的團：icon 按鈕設計
        actions.push(
          `<button class="btn btn-icon-zoom" title="查看詳情" onclick="window.location.href='/act/member/view/${activity.actId}'"><i class="fas fa-eye"></i></button>`
        );
        actions.push(
          `<button class="btn btn-icon-zoom secondary group-home-btn" title="前往揪團主頁" onclick="window.location.href='/act/group/${activity.actId}/home'"><i class="fas fa-users" data-default-icon="fas fa-users" data-hover-icon="fas fa-arrow-right"></i></button>`
        );
        actions.push(
          `<button class="btn btn-icon-zoom error" title="取消報名" onclick="cancelParticipation(${activity.actId})"><i class="fas fa-trash"></i></button>`
        );
      } else if (showHostActions) {
        // 我揪的團：icon 按鈕設計
        actions.push(
          `<button class="btn btn-icon-zoom" title="查看詳情" onclick="viewAct(${activity.actId})"><i class="fas fa-eye"></i></button>`
        );
        actions.push(
          `<button class="btn btn-icon-zoom secondary group-home-btn" title="前往揪團主頁" onclick="window.location.href='/act/group/${activity.actId}/home'"><i class="fas fa-users" data-default-icon="fas fa-users" data-hover-icon="fas fa-arrow-right"></i></button>`
        );
        actions.push(
          `<button class="btn btn-icon-zoom tertiary" title="編輯" onclick="editAct(${activity.actId})"><i class="fas fa-pen"></i></button>`
        );
        if (activity.isPublic != 1) {
          actions.push(
            `<button class="btn btn-icon-zoom error" title="刪除" onclick="memberDeleteAct(${activity.actId})"><i class="fas fa-trash"></i></button>`
          );
        }
      } else {
        // 搜尋頁面或其他：永遠顯示檢視詳情
        actions.push(
          `<button onclick="viewAct(${activity.actId})" class="act-btn act-btn-primary">檢視詳情</button>`
        );
        if (
          activity.isCurrentUserParticipant === true &&
          activity.hostId != window.currentMemId
        ) {
          actions.push(
            `<button onclick="cancelParticipation(${activity.actId})" class="act-btn act-btn-danger">取消報名</button>`
          );
        } else if (
          activity.isCurrentUserParticipant === false &&
          activity.recruitStatus === 0
        ) {
          actions.push(
            `<button onclick="joinActivity(${activity.actId})" class="act-btn act-btn-primary">報名參加</button>`
          );
        }
      }

      // 自訂操作
      actions.push(...customActions);

      // 安全檢查：確保"我跟的團"不會顯示刪除或編輯按鈕
      if (template === "myJoin") {
        actions = actions.filter(
          (action) =>
            !action.includes('onclick="memberDeleteAct') &&
            !action.includes('onclick="editAct') &&
            !action.includes('onclick="deleteAct')
        );
      }

      if (actions.length > 0) {
        actionsHtml = `<div class="act-card-actions btn-group">${actions.join("")}</div>`;
      }
    }

    return `
      <div class="act-card">
        ${imageHtml}
        <h3 class="act-card-title">${activity.actName || "未命名活動"}</h3>
        <p class="act-card-desc">${activity.actDesc || "無描述"}</p>
        ${infoHtml}
        ${statusHtml}
        ${actionsHtml}
      </div>
    `;
  }

  /**
   * 創建分頁按鈕
   * @param {Object} pageInfo - 分頁資訊
   * @param {Function} onLoadMore - 載入更多的回調函數
   * @returns {string} HTML 字串
   */
  function createPaginationButton(pageInfo, onLoadMore) {
    if (pageInfo.last) return "";

    return `
      <button 
        id="loadMoreBtn" 
        class="act-btn act-btn-primary" 
        style="margin-top: 20px;"
        onclick="(${onLoadMore.toString()})()"
      >
        載入更多
      </button>
    `;
  }

  /**
   * 創建成員列表項目
   * @param {Object} member - 成員資料
   * @param {boolean} isHost - 是否為團主
   * @param {Array} actions - 操作按鈕
   * @returns {string} HTML 字串
   */
  function createMemberItem(member, isHost = false, actions = []) {
    const hostBadge = isHost ? '<span class="act-host-badge">團主</span>' : "";
    const actionsHtml = actions.length > 0 ? actions.join("") : "";

    return `
      <div class="act-member-item">
        <div>
          <span>${member.name || member.membName || "未知成員"}</span>
          ${hostBadge}
        </div>
        <div>${actionsHtml}</div>
      </div>
    `;
  }

  /**
   * 創建聊天訊息
   * @param {Object} message - 訊息資料
   * @returns {string} HTML 字串
   */
  function createChatMessage(message) {
    const timestamp = new Date(
      message.timestamp || Date.now()
    ).toLocaleTimeString("zh-TW");
    return `
      <div class="act-chat-message">
        <strong>${message.sender || "匿名"}:</strong>
        ${message.content || ""}
        <span class="timestamp">${timestamp}</span>
      </div>
    `;
  }

  /**
   * 創建模態框
   * @param {string} title - 標題
   * @param {string} content - 內容
   * @param {Array} buttons - 按鈕配置
   * @returns {HTMLElement} 模態框元素
   */
  function createModal(title, content, buttons = []) {
    const modal = document.createElement("div");
    modal.className = "act-modal";
    modal.style.display = "flex";

    const buttonsHtml = buttons
      .map(
        (btn) =>
          `<button class="act-btn ${btn.class || "act-btn-primary"}" onclick="${
            btn.onclick || ""
          }">${btn.text}</button>`
      )
      .join("");

    modal.innerHTML = `
      <div class="act-modal-content">
        <button class="act-modal-close" onclick="this.closest('.act-modal').remove()">&times;</button>
        <h3>${title}</h3>
        <div>${content}</div>
        <div style="margin-top: 20px; text-align: right;">
          ${buttonsHtml}
        </div>
      </div>
    `;

    return modal;
  }

  /**
   * 創建表單欄位
   * @param {Object} field - 欄位配置
   * @returns {string} HTML 字串
   */
  function createFormField(field) {
    const {
      type = "text",
      name,
      label,
      value = "",
      placeholder = "",
      required = false,
      options = [], // for select
    } = field;

    let inputHtml = "";

    switch (type) {
      case "select":
        const optionsHtml = options
          .map(
            (opt) =>
              `<option value="${opt.value}" ${
                opt.value == value ? "selected" : ""
              }>${opt.text}</option>`
          )
          .join("");
        inputHtml = `<select name="${name}" class="act-search-input" ${
          required ? "required" : ""
        }>${optionsHtml}</select>`;
        break;
      case "textarea":
        inputHtml = `<textarea name="${name}" class="act-search-input" placeholder="${placeholder}" ${
          required ? "required" : ""
        }>${value}</textarea>`;
        break;
      default:
        inputHtml = `<input type="${type}" name="${name}" class="act-search-input" value="${value}" placeholder="${placeholder}" ${
          required ? "required" : ""
        }>`;
    }

    return `
      <div class="act-form-group">
        <label class="act-search-label">${label}${required ? " *" : ""}</label>
        ${inputHtml}
      </div>
    `;
  }

  /**
   * 創建搜尋結果容器
   * @param {Array} items - 項目陣列
   * @param {Function} itemRenderer - 項目渲染函數
   * @param {Object} emptyState - 空狀態配置
   * @returns {string} HTML 字串
   */
  function createResultsContainer(items, itemRenderer, emptyState = {}) {
    if (!items || items.length === 0) {
      return `
        <div class="act-empty-state">
          <h3>${emptyState.title || "沒有找到結果"}</h3>
          <p>${emptyState.message || "請嘗試調整搜尋條件"}</p>
        </div>
      `;
    }

    const itemsHtml = items.map(itemRenderer).join("");
    return `<div class="act-card-container">${itemsHtml}</div>`;
  }

  // 公開的 API
  return {
    createActCard,
    createPaginationButton,
    createMemberItem,
    createChatMessage,
    createModal,
    createFormField,
    createResultsContainer,
  };
})();
