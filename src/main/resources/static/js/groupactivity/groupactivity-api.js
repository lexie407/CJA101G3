/**
 * ========================================
 * GroupActivity 模組 - API 呼叫封裝
 * ========================================
 */

window.GroupActivityAPI = (function () {
  "use strict";

  // API 基礎路徑
  const API_BASE = "/api/act";

  /**
   * 基礎 HTTP 請求封裝
   * @param {string} url - 請求 URL
   * @param {Object} options - 請求選項
   * @returns {Promise} 請求結果
   */
  async function request(url, options = {}) {
    const defaultOptions = {
      credentials: "same-origin",
      headers: {
        "X-Requested-With": "XMLHttpRequest",
      },
    };

    const config = {
      ...defaultOptions,
      ...options,
      headers: {
        ...defaultOptions.headers,
        ...options.headers,
      },
    };

    try {
      const response = await fetch(url, config);

      // 處理認證錯誤
      if (response.status === 401) {
        window.location.href = "/members/login";
        return;
      }

      // 處理權限錯誤
      if (response.status === 403) {
        const data = await response.json();
        if (data.redirectTo) {
          window.location.href = data.redirectTo;
        } else {
          throw new Error("您沒有權限執行此操作");
        }
        return;
      }

      if (!response.ok) {
        // 嘗試獲取詳細錯誤資訊
        let errorData = null;
        try {
          errorData = await response.json();
          console.error("詳細錯誤資訊:", errorData);
        } catch (parseError) {
          console.error("無法解析錯誤回應:", parseError);
        }

        // 創建包含完整錯誤資訊的錯誤對象
        const error = new Error(
          `HTTP ${response.status}: ${response.statusText}`
        );
        error.statusCode = response.status;
        error.errorData = errorData;
        throw error;
      }

      return await response.json();
    } catch (error) {
      console.error("API request failed:", error);
      throw error;
    }
  }

  /**
   * 搜尋活動
   * @param {Object} params - 搜尋參數
   * @returns {Promise} 搜尋結果
   */
  function searchActivities(params = {}) {
    const searchParams = new URLSearchParams();

    // 處理搜尋參數
    Object.keys(params).forEach((key) => {
      const value = params[key];
      if (value !== null && value !== undefined && value !== "") {
        searchParams.append(key, value);
      }
    });

    return request(`${API_BASE}/search?${searchParams.toString()}`);
  }

  /**
   * 取得我的活動列表
   * @param {number} hostId - 團主 ID
   * @returns {Promise} 活動列表
   */
  function getMyActivities(hostId) {
    return request(`${API_BASE}/my/${hostId}`);
  }

  /**
   * 取得我參加的活動列表
   * @param {number} memberId - 成員 ID
   * @returns {Promise} 活動列表
   */
  function getJoinedActivities(memberId) {
    return request(`${API_BASE}/joined/${memberId}`);
  }

  /**
   * 取得單一活動詳情
   * @param {number} actId - 活動 ID
   * @returns {Promise} 活動詳情
   */
  // ---未使用---
  function getActivity(actId) {
    return request(`${API_BASE}/${actId}`);
  }

  /**
   * 新增活動
   * @param {FormData} formData - 活動資料
   * @returns {Promise} 新增結果
   */
  async function createActivity(formData) {
    try {
      console.log("=== API請求開始 ===");
      console.log("FormData內容:");
      for (let [key, value] of formData.entries()) {
        console.log(`${key}:`, value);
      }

      const response = await fetch("/api/act/add", {
        method: "POST",
        body: formData,
      });

      console.log("=== API回應狀態 ===");
      console.log("Response status:", response.status);
      console.log("Response ok:", response.ok);

      const result = await response.json();
      console.log("=== API回應內容 ===");
      console.log("完整回應:", result);
      console.log("回應類型:", typeof result);
      console.log("回應的keys:", Object.keys(result));

      if (!response.ok || !result.success) {
        console.log("=== 錯誤回應處理 ===");
        console.log("建立錯誤對象前 - result:", result);

        // 創建標準化錯誤對象
        const error = {
          statusCode: response.status,
          errorData: result,
          message: result.error || result.message || "請求失敗",
        };

        // 如果後端直接返回errors陣列，複製到error對象頂層
        if (result.errors && Array.isArray(result.errors)) {
          error.errors = result.errors;
          console.log("複製errors陣列到頂層:", error.errors);
        }

        console.log("最終錯誤對象:", error);
        throw error;
      }

      console.log("=== API請求成功 ===");
      return result;
    } catch (error) {
      console.error("=== API請求異常 ===");
      console.error("異常詳情:", error);

      // 如果是網絡錯誤或其他非HTTP錯誤
      if (!error.statusCode) {
        console.log("網絡錯誤，建立預設錯誤對象");
        throw {
          statusCode: 0,
          message: "網絡連接失敗，請檢查網絡連接",
        };
      }

      throw error;
    }
  }

  /**
   * 更新活動
   * @param {FormData} formData - 活動資料
   * @returns {Promise} 更新結果
   */
  function updateActivity(formData) {
    return request(`${API_BASE}/update`, {
      method: "PUT",
      body: formData,
    });
  }

  /**
   * 更新活動狀態
   * @param {number} actId - 活動 ID
   * @param {number} status - 新狀態
   * @param {number} operatorId - 操作者 ID
   * @param {boolean} isAdmin - 是否為管理員
   * @returns {Promise} 更新結果
   */
  // ---未使用---
  function updateActivityStatus(actId, status, operatorId, isAdmin = false) {
    return request(
      `${API_BASE}/${actId}/status/${status}?operatorId=${operatorId}&admin=${isAdmin}`,
      {
        method: "PUT",
      }
    );
  }

  /**
   * 刪除活動（設為已取消狀態）
   * @param {number} actId - 活動 ID
   * @param {number} operatorId - 操作者 ID
   * @param {boolean} isAdmin - 是否為管理員
   * @returns {Promise} 刪除結果
   */
  // ---未使用---
  function deleteActivity(actId, operatorId, isAdmin = false) {
    return updateActivityStatus(actId, 3, operatorId, isAdmin);
  }

  /**
   * 取得活動成員列表
   * @param {number} actId - 活動 ID
   * @returns {Promise} 成員列表
   */
  // ---未使用---
  function getActivityMembers(actId) {
    return request(`${API_BASE}/${actId}/members`);
  }

  /**
   * 參加活動
   * @param {number} actId - 活動 ID
   * @param {number} memberId - 成員 ID
   * @returns {Promise} 參加結果
   */
  // ---未使用---
  function joinActivity(actId, memberId) {
    return request(`/api/participate/${actId}/signup/${memberId}`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
    });
  }

  /**
   * 退出活動
   * @param {number} actId - 活動 ID
   * @param {number} memberId - 成員 ID
   * @returns {Promise} 退出結果
   */
  // ---未使用---
  function leaveActivity(actId, memberId) {
    return request(`/api/participate/${actId}/signup/${memberId}`, {
      method: "DELETE",
      headers: {
        "Content-Type": "application/json",
      },
    });
  }

  /**
   * 會員刪除活動（僅限未公開活動）
   * @param {number} actId - 活動 ID
   * @returns {Promise} 刪除結果
   */
  // ---未使用---
  function memberDeleteActivity(actId) {
    return request(`${API_BASE}/member/delete/${actId}`, {
      method: "DELETE",
    });
  }

  /**
   * 建構搜尋 URL
   * @param {Object} params - 搜尋參數
   * @param {number} page - 頁碼
   * @param {number} size - 每頁筆數
   * @returns {string} 完整的搜尋 URL
   */
  function buildSearchUrl(params, page = 0, size = 9) {
    const searchParams = new URLSearchParams();

    // 基本搜尋參數
    Object.keys(params).forEach((key) => {
      const value = params[key];
      if (value !== null && value !== undefined && value !== "") {
        if (key === "actStart" && value) {
          // 日期參數特殊處理
          const startDateTime = new Date(value + "T00:00:00");
          searchParams.append(key, startDateTime.toISOString());
        } else {
          searchParams.append(key, value);
        }
      }
    });

    // 分頁參數
    searchParams.append("page", page);
    searchParams.append("size", size);

    return `${API_BASE}/search?${searchParams.toString()}`;
  }

  /**
   * 通用的分頁載入處理
   * @param {string} url - API URL
   * @param {HTMLElement} container - 結果容器
   * @param {Function} itemRenderer - 項目渲染函數
   * @param {boolean} isNewSearch - 是否為新搜尋
   * @returns {Promise} 載入結果
   */
  async function loadPaginatedResults(
    url,
    container,
    itemRenderer,
    isNewSearch = false
  ) {
    try {
      // 顯示載入狀態
      if (isNewSearch) {
        container.innerHTML = '<div class="act-loading">搜尋中...</div>';
      }

      const response = await request(url);
      const content = response.content || [];
      const isLastPage = response.last;

      // 清除載入狀態
      if (isNewSearch) {
        container.innerHTML = "";
      } else {
        // 移除舊的載入更多按鈕
        const oldBtn = container.querySelector("#loadMoreBtn");
        if (oldBtn) oldBtn.remove();
      }

      if (content.length > 0) {
        // 取得或創建卡片容器
        let cardContainer = container.querySelector(".act-card-container");
        if (isNewSearch || !cardContainer) {
          cardContainer = document.createElement("div");
          cardContainer.className = "act-card-container";
          container.appendChild(cardContainer);
        }

        // 渲染項目
        content.forEach((item) => {
          const itemHtml = itemRenderer(item);
          cardContainer.insertAdjacentHTML("beforeend", itemHtml);
        });

        return {
          hasMore: !isLastPage,
          itemCount: content.length,
        };
      } else if (isNewSearch) {
        container.innerHTML = `
          <div class="act-empty-state">
            <h3>找不到符合條件的活動</h3>
            <p>請嘗試修改您的搜尋條件</p>
          </div>
        `;
      }

      return {
        hasMore: false,
        itemCount: 0,
      };
    } catch (error) {
      console.error("載入失敗:", error);
      container.innerHTML = `
        <div class="act-error-container">
          <h3>載入失敗</h3>
          <p>發生網路錯誤，請稍後再試</p>
        </div>
      `;
      throw error;
    }
  }

  // 公開的 API
  return {
    // 基礎方法
    request,

    // 活動相關
    searchActivities,
    getMyActivities,
    getJoinedActivities,
    getActivity,
    createActivity,
    updateActivity,
    updateActivityStatus,
    deleteActivity,
    memberDeleteActivity,

    // 成員相關
    getActivityMembers,
    joinActivity,
    leaveActivity,

    // 工具方法
    buildSearchUrl,
    loadPaginatedResults,
  };
})();
