package com.toiukha.commentsReport.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.toiukha.comments.model.CommentsService;
import com.toiukha.comments.model.CommentsVO;
import com.toiukha.comments.model.CommentsService;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
public class ReadImg {

	@Autowired
	private CommentsService commentsService;
	
	@GetMapping(value = "/commentsReport/read.do", produces = MediaType.IMAGE_GIF_VALUE)
	public ResponseEntity<byte[]> readImg(
			@RequestParam("commId")Integer commId){
		
		CommentsVO commentsVO = commentsService.getOne(commId);
		
		return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_GIF)
                .body(commentsVO.getCommImg());
		
	}
	
}
