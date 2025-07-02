package com.toiukha.articlereport.controller;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.toiukha.articlereport.model.ArticlereportService;
import com.toiukha.comments.model.CommentsVO;
import com.toiukha.articlereport.model.ArticlereportVO;
import com.toiukha.forum.article.dto.ArticleDTO;
import com.toiukha.forum.article.entity.Article;
import com.toiukha.forum.article.model.ArticleServiceImpl;
import com.toiukha.members.model.MembersVO;
import com.toiukha.notification.model.NotificationService;
import com.toiukha.notification.model.NotificationVO;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/Articlereport")
public class ArticlereportController {

	@Autowired
	ArticlereportService articlereportService;
	@Autowired
	ArticleServiceImpl articleServiceImpl;
	@Autowired
	NotificationService notificationService;
	
	//查看檢舉
		@GetMapping("/memberReportList")
		public String memberReportList(HttpServletRequest req, ModelMap model) {
			MembersVO membersVO = (MembersVO)req.getSession().getAttribute("member");
			List<ArticlereportVO> list = articlereportService.getMemList(membersVO.getMemId());
			model.addAttribute("list", list);
			model.addAttribute("currentPage", "account");
			return "front-end/articlereport/memberArticleReportList";
		}
		
		//看詳情的頁面
		@PostMapping("/memberReportListDetsil")
		public String memberReportListDetsil(
				@RequestParam("artRepId") Integer artRepId, 
				ModelMap model) {
			ArticlereportVO articlereportVO = articlereportService.getOne(artRepId);
			model.addAttribute("articlereportVO", articlereportVO);
			model.addAttribute("currentPage", "account");
			return "front-end/articlereport/memberArticleReportDetail";
		}
		
		//========== 後台 ==========//
		//查看所有檢舉
		@GetMapping("/allReportList")
		public String allReportList(ModelMap model) {
			List<ArticlereportVO> list = articlereportService.getAll();
			model.addAttribute("list", list);
			model.addAttribute("currentPage", "report");
			model.addAttribute("currentPage2", "articlereport");
			return "back-end/articlereport/allArticleReportList";
		}
		
		//編輯案件頁面
		@PostMapping("/editArticleReportList")
		public String editArticleReportList(
				@RequestParam("artRepId") Integer artRepId,
				ModelMap model) {
			ArticlereportVO articlereportVO = articlereportService.getOne(artRepId);
			Article article = articleServiceImpl.getArticleById(articlereportVO.getArtId());
			
			model.addAttribute("articlereportVO", articlereportVO);
			model.addAttribute("article", article);
			model.addAttribute("currentPage", "report");
			model.addAttribute("currentPage2", "articlereport");
			return "back-end/articlereport/editArticleReport";
		}
		
		//案件成立處理
		@PostMapping("/established")
		public String established(
				@RequestParam("artId") Integer artId,
				@RequestParam("remark") String remark, 
				@RequestParam("artRepId") Integer artRepId,
				RedirectAttributes redirectAttributes) {
			//修改文章狀態
			Article article = articleServiceImpl.getArticleById(artId);
			article.setArtSta((byte)2);
			articleServiceImpl.update(article);
			
			//修改檢舉狀態
			ArticlereportVO existingArticlereportVO = articlereportService.getOne(artRepId);
			existingArticlereportVO.setRemark(remark);
			existingArticlereportVO.setRepSta((byte)1);
			existingArticlereportVO.setRevTime(getNowTime());
			articlereportService.changeSta(existingArticlereportVO);
			
			//發通知
			NotificationVO notificationVO = new NotificationVO(
					"[系統]文章檢舉成立通知", 
					"你好，你所提出"+artRepId+"號文章檢舉案件通過，詳情可至會員中心查看。",
					articlereportService.getOne(artRepId).getMemId(),
					getNowTime());
			notificationService.addOneNoti(notificationVO);
			
			Integer memId2 = articleServiceImpl.getArticleById(artId).getArtHol();
			NotificationVO notificationVO2 = new NotificationVO(
					"[系統]文章下架通知", 
					"你好，你有一則文章因違反平台規範遭下架處理，特此通知。",
					memId2,
					getNowTime());
			notificationService.addOneNoti(notificationVO2);
			
			redirectAttributes.addFlashAttribute("successMsg", "檢舉案件處理已完成!");
			
			return "redirect:/Articlereport/allReportList";
		}
		
		//案件不成立處理
		@PostMapping("/unEstablished")
		public String unEstablished(
				@RequestParam("remark") String remark, 
				@RequestParam("artRepId") Integer artRepId,
				RedirectAttributes redirectAttributes) {
			//修改檢舉狀態
			ArticlereportVO existingArticlereportVO = articlereportService.getOne(artRepId);
			existingArticlereportVO.setRemark(remark);
			existingArticlereportVO.setRepSta((byte)2);
			existingArticlereportVO.setRevTime(getNowTime());
			articlereportService.changeSta(existingArticlereportVO);
			
			//發通知
			NotificationVO notificationVO = new NotificationVO(
					"[系統]留言檢舉不成立通知", 
					"你好，你所提出"+artRepId+"號留言檢舉案件不通過，詳情可至會員中心查看。",
					articlereportService.getOne(artRepId).getMemId(),
					getNowTime());
			notificationService.addOneNoti(notificationVO);
			
			redirectAttributes.addFlashAttribute("successMsg", "檢舉案件處理已完成!");
			
			return "redirect:/Articlereport/allReportList";
		}
		
		//現在時間
//		public Timestamp getNowTime() {
//			Date date = new Date();
//			return new Timestamp(date.getTime());
//		}
		
		 public Timestamp getNowTime() {
		        return Timestamp.from(Instant.now());
		    }
	
}
