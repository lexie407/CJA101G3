package com.toiukha.comments.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.toiukha.comments.model.CommentsService;
import com.toiukha.comments.model.CommentsVO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/commentsAPI")
public class CommentsAPIController {

	@Autowired
	CommentsService commentsService;
	
	//========== 前台後台通用 ==========// 
	//查文章的留言
	@PostMapping("/getComments")
	public List<CommentsVO> getComments(
			@RequestParam("commArt")Integer commArt) {
		return commentsService.getArtComm(commArt);
	}
	
	@GetMapping("test")
	public String getTest() {
		return "test";
	}
	
	@PostMapping("/addComments")
	public String addComments(
	        @RequestParam("commCat") Byte commCat,
	        @RequestParam("commHol") Integer commHol,
	        @RequestParam("commArt") Integer commArt,
	        @RequestParam("commCon") String commCon,
	        @RequestParam("commImg") MultipartFile commImgFile) {

	    try {
	        byte[] commImg = commImgFile.getBytes();

	        // 手動建立 CommentsVO 物件
	        CommentsVO commentsVO = new CommentsVO(commCat, commHol, commArt, commCon, commImg);

	        commentsService.editOne(commentsVO);
	        return "留言新增成功";

	    } catch (IOException e) {
	        e.printStackTrace();
	        return "上傳圖片失敗：" + e.getMessage();
	    }
	}
	
	//修改留言
	@PostMapping("/updateComments")
	public void updateComments(
			@RequestParam("commImg")MultipartFile part,
			@RequestParam("commCon") String commCon,
			@RequestParam("commId") Integer commId) {
		byte[] commImg = null;
		try {
			commImg = part.getBytes();
		} catch (IOException e) {
			e.printStackTrace();
		}
		commentsService.changeComm(commId, commCon, commImg);
	}
	
	//最佳解
	@PostMapping("/bestAnswer")
	public void bestAnswer(
			@RequestParam("commId") Integer commId) {
		commentsService.changeSta(commId, (byte)3);
	}
	
	//刪除留言
		@PostMapping("/deleteComments")
		public void deleteComments(
				@RequestParam("commId") Integer commId) {
			commentsService.changeSta(commId, (byte)2);
		}
		
	
	
}
