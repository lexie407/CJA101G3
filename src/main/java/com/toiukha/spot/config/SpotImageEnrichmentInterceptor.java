package com.toiukha.spot.config;

import com.toiukha.spot.model.SpotVO;
import com.toiukha.spot.service.SpotEnrichmentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import org.springframework.data.domain.Page;

@Component
public class SpotImageEnrichmentInterceptor implements HandlerInterceptor {

    @Autowired
    private SpotEnrichmentService spotEnrichmentService;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        if (modelAndView != null) {
            System.out.println("[攔截器觸發] URL=" + request.getRequestURI());
            Object listObj = null;
            if (modelAndView.getModel().containsKey("spots")) {
                listObj = modelAndView.getModel().get("spots");
            } else if (modelAndView.getModel().containsKey("spotList")) {
                listObj = modelAndView.getModel().get("spotList");
            } else if (modelAndView.getModel().containsKey("spotPage")) {
                Object pageObj = modelAndView.getModel().get("spotPage");
                if (pageObj instanceof Page<?>) {
                    listObj = ((Page<?>) pageObj).getContent();
                }
            }
            if (listObj instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof SpotVO) {
                @SuppressWarnings("unchecked")
                List<SpotVO> spotList = (List<SpotVO>) list;
                System.out.println("[補圖執行] spotList.size=" + spotList.size());
                for (int i = 0; i < Math.min(2, spotList.size()); i++) {
                    SpotVO spot = spotList.get(i);
                    System.out.println("  [spot] id=" + spot.getSpotId() + ", firstPictureUrl=" + spot.getFirstPictureUrl() + ", googlePlaceId=" + spot.getGooglePlaceId());
                }
                spotEnrichmentService.enrichSpotPictureUrlIfNeeded(spotList);
            }
        }
    }
} 