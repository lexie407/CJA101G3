package com.toiukha.report.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.toiukha.members.model.MembersVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

// @RestController 是一個組合註解，表示這個類別是個控制器，
// 並且所有方法的返回值都會被自動轉換成 JSON 格式，直接寫入 HTTP 回應中。
@RestController
// @RequestMapping("/api") 表示這個控制器處理的所有請求路徑，都會以 "/api" 作為前綴。
// 例如，下面的 "/chat" 的完整路徑就是 "/api/chat"。
@RequestMapping("/api")
// @RequiredArgsConstructor 是 Lombok 的一個註解，會自動為所有 final 的欄位生成建構子。
@RequiredArgsConstructor
public class ConversationController {

    // @Value("${dify.api.key}") 會從設定檔 (如 application.properties) 中讀取名為 "dify.api.key" 的值，
    // 並將其注入到 difyApiKey 這個變數中。這是存放敏感金鑰的安全做法。
    @Value("${dify.api.key}")
    private String difyApiKey;

    // RestTemplate 是 Spring 提供的一個工具，用來方便地發送 HTTP 請求到其他服務。
    private final RestTemplate restTemplate = new RestTemplate();
    // ObjectMapper 是 Jackson 函式庫的核心，用來在 Java 物件和 JSON 字串之間進行轉換。
    private final ObjectMapper objectMapper = new ObjectMapper();

    // @PostMapping("/chat") 表示這個方法專門處理對 "/api/chat" 路徑的 HTTP POST 請求。
    // @RequestBody 表示方法參數 requestBody 的值來自於請求的 body，Spring 會自動將 JSON 轉為 Map。
    // HttpServletRequest 讓我們可以存取原始的 HTTP 請求資訊，例如 session。
    public SseEmitter proxyToDify(@RequestBody Map<String, Object> requestBody, HttpServletRequest httpRequest) {
        // SseEmitter (Server-Sent Events Emitter) 是實現伺服器推送技術的關鍵。
        // 它會建立一個長連接，讓伺服器可以隨時向瀏覽器發送資料。
        SseEmitter emitter = new SseEmitter();

        // Dify AI 服務的 API 端點網址。
        String difyUrl = "https://api.dify.ai/v1/chat-messages";

        // === 準備要發送給 Dify AI 的資料 (Payload) ===
        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> inputs = new HashMap<>();
        
        // 從 session 中獲取當前登入的會員資訊，並放入 inputs 中。
        // 這樣 Dify 就可以根據不同使用者提供個人化的回覆。
        inputs.put("memId", ((MembersVO)httpRequest.getSession().getAttribute("member")).getMemId());
        inputs.put("memName", ((MembersVO)httpRequest.getSession().getAttribute("member")).getMemName());
        
        // 將前端傳來的查詢問題 (query) 放入 payload。
        payload.put("query", requestBody.get("query"));
        payload.put("inputs", inputs);
        // 這是關鍵！告訴 Dify 我們需要「串流」模式的回應。
        payload.put("response_mode", "streaming");
        // 將前端傳來的對話 ID (conversation_id) 放入 payload，以保持對話的連續性。
        payload.put("conversation_id", requestBody.get("conversation_id"));
        // 將使用者的 session ID 作為 Dify 的 user 識別符。
        payload.put("user", httpRequest.getSession().getId());
        
        // 使用 RestTemplate 執行請求。這是一個非同步操作，不會阻塞主執行緒。
        restTemplate.execute(difyUrl, HttpMethod.POST, 
            // 這個 Lambda 函式用來「準備請求」。它會在請求發送前被呼叫。
            (ClientHttpRequest req) -> {
                HttpHeaders headers = req.getHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON); // 設定請求內容為 JSON
                headers.setBearerAuth(difyApiKey); // 在 Authorization 標頭中加入我們的 API Key
                headers.setAccept(MediaType.parseMediaTypes("text/event-stream")); // 告訴 Dify 我們期望接收串流事件

                // 將 payload 這個 Map 物件轉換成 JSON 字串，並寫入請求的 body 中。
                objectMapper.writeValue(req.getBody(), payload);
            }, 
            // 這個 Lambda 函式用來「處理回應」。當 Dify 伺服器回傳資料時，它會被呼叫。
            (ClientHttpResponse res) -> {
                // 使用 try-with-resources 確保資源會被自動關閉。
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(res.getBody()))) {
                    String line;
                    // 一行一行地讀取 Dify 回傳的串流資料。
                    while ((line = reader.readLine()) != null) {
                        // 每讀到一行，就立刻透過 emitter 將這行文字「推送」給前端。
                        // 這就是串流的核心：收到一點，就發送一點。
                        emitter.send(line);
                    }
                    // 當所有資料都讀取完畢 (readLine() 返回 null)，表示串流結束。
                    // 我們呼叫 complete() 來正常關閉與瀏覽器的連接。
                    emitter.complete();
                } catch (Exception e) {
                    // 如果在處理過程中發生任何錯誤（例如網路中斷），
                    // 我們就以錯誤狀態關閉連接，並將異常資訊傳遞過去。
                    emitter.completeWithError(e);
                }
                // execute 方法要求這裡返回一個值，但我們在串流中已經處理完所有事，所以返回 null 即可。
                return null;
            });

        // 最後，將 emitter 物件返回。Spring MVC 會處理好後續的非同步連接。
        return emitter;
    }
}