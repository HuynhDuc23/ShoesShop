package com.huynhduc.application.dlt;

import com.huynhduc.application.model.request.CreateOrderRequest;
import com.huynhduc.application.repository.UserRepository;
import com.huynhduc.application.service.MailService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class DeepLetterTopic {
    private final MailService mailService ;
    private final UserRepository userRepository ;
    public DeepLetterTopic(MailService mailService, UserRepository userRepository) {
        this.mailService = mailService;
        this.userRepository = userRepository;
    }
    @KafkaListener(topics = "create-order.DLT", groupId = "dlt-handler-group")
    public void handleDeadLetter(CreateOrderRequest request) {
        try {
            // Cố gắng xử lý lại message
            // Nếu không thành công, lưu vào DB hoặc thực hiện các hành động khác
            String email = userRepository.findById(request.getUserId()).get().getEmail();
            this.mailService.sendMailSimple(email, "Thông báo đơn hàng", "Đơn hàng của bạn không thành công , vui lòng thử lại sau");
        } catch (Exception e) {
            System.out.println("❌ Lưu message lỗi vào DB: {}" + request + e);
        }
    }
}
