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
import com.toiukha.forum.article.model.ArticleServiceImpl;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/commentsAPI")
public class CommentsAPIController {

	@Autowired
	CommentsService commentsService;
	@Autowired
	ArticleServiceImpl articleServiceImpl;
	
	//========== 前台後台通用 ==========// 
	//查文章的留言
	@PostMapping("/getComments")
	public List<CommentsVO> getComments(
			@RequestParam("commArt")Integer commArt) {
		return commentsService.getArtComm(commArt);
	}
	
	//查留言
	@PostMapping("/getOneComment")
	public CommentsVO getOneComment(
			@RequestParam("commHol")Integer commHol) {
		return commentsService.getOne(commHol);
	}
	
	@PostMapping("/addComments")
	public CommentsVO addComments(
	        @RequestParam("commHol") Integer commHol,
	        @RequestParam("commArt") Integer commArt,
	        @RequestParam("commCon") String commCon,
	        @RequestParam("commImg") MultipartFile commImgFile) {

	    try {
	        byte[] commImg = commImgFile.getBytes();
	        
	        Byte commCat = articleServiceImpl.getArticleById(commArt).getArtCat();

	        // 手動建立 CommentsVO 物件
	        CommentsVO commentsVO = new CommentsVO(commCat, commHol, commArt, commCon, commImg);

	        CommentsVO newcommentsVO = commentsService.editOne(commentsVO);
	        return commentsService.getOne(newcommentsVO.getCommId());
	        
	    } catch (IOException e) {
	        e.printStackTrace();
	        return null;
	    }
	}
	
	//修改留言
	@PostMapping("/updateComments")
	public CommentsVO updateComments(
			@RequestParam(value = "commImg", required = false)MultipartFile part,
			@RequestParam("commCon") String commCon,
			@RequestParam("commId") Integer commId) {
		byte[] commImg = null;
		if (part != null && !part.isEmpty()) {
			try {
				commImg = part.getBytes();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}

		commentsService.changeComm(commId, commCon, commImg);
		
		return commentsService.getOne(commId);
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
