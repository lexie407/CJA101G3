/**
 * ========================================
 * GroupActivity 模組 - 表單處理與驗證
 * ========================================
 */

window.GroupActivityForm = (function () {
  "use strict";

  /**
   * 圖片壓縮功能
   * @param {File} file - 原始圖片檔案
   * @param {number} maxWidth - 最大寬度
   * @param {number} maxHeight - 最大高度
   * @param {number} quality - 壓縮品質 (0-1)
   * @returns {Promise<Blob>} 壓縮後的圖片 Blob
   */
  function compressImage(file, maxWidth = 800, maxHeight = 600, quality = 0.8) {
    return new Promise((resolve) => {
      const canvas = document.createElement("canvas");
      const ctx = canvas.getContext("2d");
      const img = new Image();

      img.onload = () => {
        // 計算壓縮比例
        let { width, height } = img;
        const ratio = Math.min(maxWidth / width, maxHeight / height);

        if (ratio < 1) {
          width *= ratio;
          height *= ratio;
        }

        canvas.width = width;
        canvas.height = height;

        // 繪製壓縮後的圖片
        ctx.drawImage(img, 0, 0, width, height);

        // 轉為 Blob
        canvas.toBlob(resolve, "image/jpeg", quality);
      };

      img.src = URL.createObjectURL(file);
    });
  }

  /**
   * 設置圖片預覽與壓縮功能
   * @param {string} inputId - 檔案輸入元素 ID
   * @param {string} previewId - 預覽容器 ID
   * @param {string} previewImgId - 預覽圖片 ID
   * @param {string} infoId - 壓縮資訊 ID
   */
  function setupImagePreview(inputId, previewId, previewImgId, infoId) {
    const fileInput = document.getElementById(inputId);
    const preview = document.getElementById(previewId);
    const previewImg = document.getElementById(previewImgId);
    const compressionInfo = document.getElementById(infoId);

    if (!fileInput) return;

    fileInput.addEventListener("change", async function (e) {
      const file = e.target.files[0];

      if (!file) {
        if (preview) preview.style.display = "none";
        return;
      }

      // 顯示原始檔案資訊
      const originalSize = (file.size / 1024 / 1024).toFixed(2);
      if (compressionInfo) {
        compressionInfo.textContent = `原始大小: ${originalSize}MB，壓縮中...`;
      }

      try {
        // 壓縮圖片
        const compressedFile = await compressImage(file);
        const compressedSize = (compressedFile.size / 1024 / 1024).toFixed(2);

        // 更新預覽
        if (previewImg) {
          previewImg.src = URL.createObjectURL(compressedFile);
        }
        if (compressionInfo) {
          compressionInfo.textContent = `原始: ${originalSize}MB → 壓縮後: ${compressedSize}MB`;
        }
        if (preview) {
          preview.style.display = "block";
        }

        // 將壓縮後的檔案替換原始檔案
        const dataTransfer = new DataTransfer();
        const compressedFileObj = new File([compressedFile], file.name, {
          type: "image/jpeg",
          lastModified: Date.now(),
        });
        dataTransfer.items.add(compressedFileObj);
        e.target.files = dataTransfer.files;
      } catch (error) {
        console.error("圖片壓縮失敗:", error);
        if (compressionInfo) {
          compressionInfo.textContent = "圖片壓縮失敗，將使用原始檔案";
        }
        if (previewImg) {
          previewImg.src = URL.createObjectURL(file);
        }
        if (preview) {
          preview.style.display = "block";
        }
      }
    });
  }

  /**
   * 表單驗證規則
   */
  const validationRules = {
    required: (value) => value && value.trim() !== "",
    email: (value) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value),
    minLength: (min) => (value) => value && value.length >= min,
    maxLength: (max) => (value) => !value || value.length <= max,
    number: (value) => !isNaN(value) && value !== "",
    positiveNumber: (value) => !isNaN(value) && parseFloat(value) > 0,
    dateRange: (startDate, endDate) =>
      !startDate || !endDate || new Date(startDate) <= new Date(endDate),
  };

  /**
   * 驗證單一欄位
   * @param {HTMLElement} field - 表單欄位
   * @param {Array} rules - 驗證規則
   * @returns {boolean} 是否通過驗證
   */
  function validateField(field, rules = []) {
    const value = field.value;
    let isValid = true;
    let errorMessage = "";

    for (const rule of rules) {
      if (typeof rule === "string") {
        // 預設規則
        if (rule === "required" && !validationRules.required(value)) {
          isValid = false;
          errorMessage = "此欄位為必填";
          break;
        }
      } else if (typeof rule === "object") {
        // 自訂規則
        const { type, message, param } = rule;
        const validator = validationRules[type];

        if (validator) {
          const checkResult = param
            ? validator(param)(value)
            : validator(value);
          if (!checkResult) {
            isValid = false;
            errorMessage = message || "格式不正確";
            break;
          }
        }
      }
    }

    // 顯示/隱藏錯誤訊息
    showFieldError(field, isValid ? "" : errorMessage);

    return isValid;
  }

  /**
   * 顯示欄位錯誤訊息
   * @param {HTMLElement} field - 表單欄位
   * @param {string} message - 錯誤訊息
   */
  function showFieldError(field, message) {
    // 移除舊的錯誤訊息
    const oldError = field.parentNode.querySelector(".act-field-error");
    if (oldError) {
      oldError.remove();
    }

    // 更新欄位樣式
    if (message) {
      field.classList.add("error");
      const errorDiv = document.createElement("div");
      errorDiv.className = "act-field-error act-error";
      errorDiv.textContent = message;
      field.parentNode.appendChild(errorDiv);
    } else {
      field.classList.remove("error");
    }
  }

  /**
   * 驗證整個表單
   * @param {HTMLFormElement} form - 表單元素
   * @param {Object} fieldRules - 欄位驗證規則
   * @returns {boolean} 是否通過驗證
   */
  function validateForm(form, fieldRules = {}) {
    let isValid = true;

    Object.keys(fieldRules).forEach((fieldName) => {
      const field = form.querySelector(`[name="${fieldName}"]`);
      if (field) {
        const fieldValid = validateField(field, fieldRules[fieldName]);
        if (!fieldValid) {
          isValid = false;
        }
      }
    });

    return isValid;
  }

  /**
   * 設置即時驗證
   * @param {HTMLFormElement} form - 表單元素
   * @param {Object} fieldRules - 欄位驗證規則
   */
  function setupLiveValidation(form, fieldRules = {}) {
    Object.keys(fieldRules).forEach((fieldName) => {
      const field = form.querySelector(`[name="${fieldName}"]`);
      if (field) {
        field.addEventListener("blur", () => {
          validateField(field, fieldRules[fieldName]);
        });
      }
    });
  }

  /**
   * 序列化表單資料為 FormData
   * @param {HTMLFormElement} form - 表單元素
   * @returns {FormData} 表單資料
   */
  function serializeForm(form) {
    return new FormData(form);
  }

  /**
   * 序列化表單資料為 JSON
   * @param {HTMLFormElement} form - 表單元素
   * @returns {Object} JSON 物件
   */
  function serializeFormToJson(form) {
    const formData = new FormData(form);
    const json = {};

    for (const [key, value] of formData.entries()) {
      // 處理複選框和多選
      if (json[key]) {
        if (Array.isArray(json[key])) {
          json[key].push(value);
        } else {
          json[key] = [json[key], value];
        }
      } else {
        json[key] = value;
      }
    }

    return json;
  }

  /**
   * 重置表單和錯誤狀態
   * @param {HTMLFormElement} form - 表單元素
   */
  function resetForm(form) {
    form.reset();

    // 清除所有錯誤狀態
    form
      .querySelectorAll(".error")
      .forEach((el) => el.classList.remove("error"));
    form.querySelectorAll(".act-field-error").forEach((el) => el.remove());

    // 隱藏圖片預覽
    form.querySelectorAll(".act-image-preview").forEach((preview) => {
      preview.style.display = "none";
    });
  }

  // 公開的 API
  return {
    compressImage,
    setupImagePreview,
    validateField,
    validateForm,
    setupLiveValidation,
    serializeForm,
    serializeFormToJson,
    resetForm,
    validationRules,
  };
})();
