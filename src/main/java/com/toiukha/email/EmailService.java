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
    public void sendVerificationEmail(String email, String link) {
        
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
            message.setText("您好，請點選以下連結完成註冊驗證（30分鐘內有效）：\n\n" + link);

            // 寄出郵件
            Transport.send(message);

            // 成功訊息
            System.out.println(" 驗證信已成功寄出至：" + email);
        } catch (MessagingException e) {
            // 失敗訊息
            System.out.println("驗證信寄送失敗！");
            e.printStackTrace(); // 印出詳細錯誤（用來除錯）
        }
    }

     // 從 Redis 驗證 token → 拿到 memId（會員ID）
    public Integer verifyToken(String token) {
        String memIdStr = redisTemplate.opsForValue().get("verify:" + token);
        if (memIdStr == null) return null;

        redisTemplate.delete("verify:" + token);
        return Integer.parseInt(memIdStr);
    }
    
    public void sendResetPasswordEmail(String email, String resetLink) {


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
        } catch (MessagingException e) {
            System.out.println(" 重設密碼信寄送失敗！");
            e.printStackTrace();
        }
    }
    
    
     // 驗證「忘記密碼」token 是否存在
    public Integer verifyResetToken(String token) {
        String memIdStr = redisTemplate.opsForValue().get("reset:" + token);
        return (memIdStr != null) ? Integer.parseInt(memIdStr) : null;
    }
    
    
    //寄送「商家重設密碼」信
    
    public void sendStoreResetPasswordEmail(String email, String resetLink) {


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
            message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(email)
            );
            message.setSubject("商家重設密碼通知");
            message.setText("您好，請點擊以下連結重設您的商家密碼（15 分鐘內有效）：\n\n" + resetLink);

            Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

     // 驗證「商家重設密碼」token
    public Integer verifyStoreResetToken(String token) {
        String storeIdStr = redisTemplate.opsForValue().get("store:reset:" + token);
        return (storeIdStr != null) ? Integer.valueOf(storeIdStr) : null;
    }
    
    
}