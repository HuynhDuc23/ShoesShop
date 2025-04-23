package com.huynhduc.application.service.impl;
import com.huynhduc.application.service.MailService;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
public class MailServiceImpl implements MailService {
    private final JavaMailSender javaMailSender ;
    public MailServiceImpl(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Override
    public void sendMailSimple(String to, String subject, String text) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom(to);
            String content = """
                <html>
                  <body>
                    <h2>🎉<b>%s</b>!</h2>
                    <p>Chúng tôi đã nhận được đơn hàng <strong>#%s</strong>.</p>
                    <p>📦 Đơn hàng sẽ được xử lý trong vòng 24 giờ.</p>
                    <hr>
                    <p style='color:gray;font-size:12px'>Shop Shoes - Hệ thống bán giày uy  số 1 Việt Nam</p>
                  </body>
                </html>
                """.formatted(subject, text);

            helper.setText(content, true); // true = HTML
            javaMailSender.send(message);
        }catch (Exception e){
            System.out.println("❌ Gửi mail thất bại: " + e.getMessage());
        }
    }
    }
