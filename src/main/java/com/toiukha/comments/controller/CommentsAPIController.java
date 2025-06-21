package com.toiukha.comments.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.toiukha.comments.model.CommentsService;
import com.toiukha.comments.model.CommentsVO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/commentsAPI")
public class CommentsAPIController {

	@Autowired
	CommentsService commentsService;
	
	
	//查文章的留言
	@PostMapping("/getComments")
	public List<CommentsVO> getComments(
			@RequestParam("commArt")Integer commArt) {
		return commentsService.getArtComm(commArt);
	}
	
	//新增留言
	@PostMapping("/addComments")
	public void addComments(
			@ModelAttribute CommentsVO commentsVO) {
		commentsService.editOne(commentsVO);
	}
	
	//新增留言
	@PostMapping("/updateComments")
	public void updateComments(
			@ModelAttribute CommentsVO commentsVO) {
		commentsService.editOne(commentsVO);
	}
	
}
