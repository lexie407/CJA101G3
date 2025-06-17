package com.toiukha.email;

import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 產生驗證碼 → 存進 Redis → 寄信
     *
     * @param email 收信者信箱
     * @param memId 會員ID（存進 Redis 用來之後找會員）
     */
    public void sendVerificationEmail(String email, Integer memId) {
        // 產生唯一驗證碼
        String token = UUID.randomUUID().toString();

        // 存進 Redis，30 分鐘有效
        redisTemplate.opsForValue().set("verify:" + token, memId.toString(), 30, TimeUnit.MINUTES);

        // 組驗證連結
        String link = "http://localhost:8080/member/verify?token=" + token;

        try {
            // 設定 Gmail SMTP 屬性
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");

            // 建立 Session
            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication("ixlogic.wu3@gmail.com", "yziextispxbdvtbo");
                }
            });

            // 建立郵件訊息
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("ixlogic.wu3@gmail.com"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            message.setSubject("會員註冊驗證信");
            message.setText("您好，請點選以下連結完成註冊驗證：\n\n" + link);

            // 寄出郵件
            Transport.send(message);

            // 成功訊息
            System.out.println("✅ 驗證信已成功寄出至：" + email);
        } catch (MessagingException e) {
            // 失敗訊息
            System.out.println("❌ 驗證信寄送失敗！");
            e.printStackTrace(); // 印出詳細錯誤（用來除錯）
        }
    }

    /**
     * 從 Redis 驗證 token → 拿到 memId（會員ID）
     *
     * @param token 驗證連結中的 token
     * @return memId，如果驗證碼不存在則回傳 null
     */
    public Integer verifyToken(String token) {
        String memIdStr = redisTemplate.opsForValue().get("verify:" + token);
        if (memIdStr == null) return null;

        redisTemplate.delete("verify:" + token);
        return Integer.parseInt(memIdStr);
    }
    
    public void sendResetPasswordEmail(String email, Integer memId) {
        String token = UUID.randomUUID().toString();

        // Redis 存入 token 對應 memId，有效時間 15 分鐘
        redisTemplate.opsForValue().set("reset:" + token, memId.toString(), 15, TimeUnit.MINUTES);

        // 建立重設密碼連結
        String resetLink = "http://localhost:8080/members/reset-password?token=" + token;

        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");

            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication("ixlogic.wu3@gmail.com", "yziextispxbdvtbo");
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("ixlogic.wu3@gmail.com"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            message.setSubject("重設密碼連結");
            message.setText("您好，請點擊下方連結重設您的密碼（15分鐘內有效）：\n\n" + resetLink);

            Transport.send(message);
            System.out.println("✅ 重設密碼信已寄出至：" + email);
        } catch (MessagingException e) {
            System.out.println("❌ 重設密碼信寄送失敗！");
            e.printStackTrace();
        }
    }
    
    
}