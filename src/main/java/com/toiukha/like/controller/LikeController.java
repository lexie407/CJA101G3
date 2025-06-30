package com.toiukha.like.controller;

import java.sql.Timestamp;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.toiukha.like.model.LikeService;
import com.toiukha.like.model.LikeVO;

@RestController
@RequestMapping("/likeAPI")
public class LikeController {

	@Autowired
	private LikeService likeService;
	
	@PostMapping("/dolike")
	public Integer dolike(
	        @ModelAttribute LikeVO likeVO) {

	    Integer Id = null; // 初始化為 null
	    Byte sta = null;   // 初始化為 null
	    LikeVO existingLike = null; // 用於儲存查詢結果

	    try {
	        if (likeVO.getParDocId() == null) {
	            existingLike = likeService.getOneLike(likeVO.getDocId(), likeVO.getMemId());
	        } else {
	            existingLike = likeService.getOneLike(likeVO.getParDocId(), likeVO.getDocId(), likeVO.getMemId());
	        }

	        // 檢查 existingLike 是否為 null，避免 NullPointerException
	        if (existingLike != null) {
	            Id = existingLike.getLikeId();
	            sta = existingLike.getLikeSta();
	        }

	        // 如果找到了資料且狀態為0，則更新為1；否則更新為0或新增
	        if (Id != null && sta != null && sta.equals(Byte.valueOf((byte) 0))) {
	            likeVO.setLikeSta(Byte.valueOf((byte) 1));
	            likeVO.setLikeId(Id);
	            likeVO.setLikeTime(getNowTime());
	            likeService.eidtOne(likeVO);
	        } else if (Id != null && sta != null && sta.equals(Byte.valueOf((byte) 1))) {
	            // 如果Id不為空且狀態為1，表示要取消讚
	            likeVO.setLikeSta(Byte.valueOf((byte) 0));
	            likeVO.setLikeId(Id);
	            likeVO.setLikeTime(getNowTime());
	            likeService.eidtOne(likeVO);
	        } else {
	            // 如果 Id 為 null 或 sta 不為 0 也不為 1，則當作是第一次點讚或資料有誤，直接新增
	            // 這裡可以根據業務邏輯決定是新增還是其他處理
	            likeVO.setDocType(Byte.valueOf((byte) 0)); // 假設 docType 預設為 0
	            likeVO.setLikeSta(Byte.valueOf((byte) 1)); // 預設為點讚
	            likeVO.setLikeTime(getNowTime()); // 設定時間
	            likeService.eidtOne(likeVO);
	        }
	    } catch (Exception e) { // 捕獲更廣泛的例外，以便除錯
	        // 當發生其他例外時，例如資料庫連接問題等，可以進行日誌記錄
	        System.err.println("發生錯誤: " + e.getMessage());
	        // 這裡可以選擇是否要新增資料，如果前面邏輯已經處理過，這裡可能不需要再次新增
	        // likeVO.setDocType(Byte.valueOf((byte)0));
	        // likeVO.setLikeSta(Byte.valueOf((byte)1));
	        // likeService.eidtOne(likeVO);
	    }
	    
	    if (likeVO.getParDocId() == null) {
            return likeService.getLikeNum(likeVO.getDocId());
        } else {
        	return likeService.getLikeNum(likeVO.getParDocId(), likeVO.getDocId());
        }
	    
	}
	
	//取得文章按讚數
	@PostMapping("/artLikeNum")
	public Integer artLikeNum(
			@RequestParam("artId") Integer parDocId) {
		return likeService.getLikeNum(parDocId);
	}
	
	//取得留言按讚數
	@PostMapping("/commLikeNum")
	public Integer commLikeNum(
			@RequestParam("artId") Integer parDocId,
			@RequestParam("commId") Integer docId) {
		return likeService.getLikeNum(parDocId, docId);
	}
	
	//取得留言讚資料
	@PostMapping("/getCommLike")
	public Boolean getCommLike(
			@RequestParam("artId") Integer parDocId,
			@RequestParam("commId") Integer docId,
			@RequestParam("memId") Integer memId) {
		LikeVO likeVO = likeService.getOneLike(parDocId, docId, memId);
		if(likeVO != null && likeVO.getLikeSta() == 1) {
			return true;
		}else {
			return false;
		}
	}
	
	//取得文章讚資料
	@PostMapping("/getArticleLike")
	public Boolean getArticleLike(
			@RequestParam("artId") Integer docId,
			@RequestParam("memId") Integer memId) {
		LikeVO likeVO = likeService.getOneLike(docId, memId);
		if(likeVO != null && likeVO.getLikeSta() == 1) {
			return true;
		}else {
			return false;
		}
	}
	
	Timestamp getNowTime() {
		Date date = new Date();
		Timestamp now = new Timestamp(date.getTime());
		return now;
	}
}
