package com.huynhduc.application.controller.shop;

import com.huynhduc.application.constant.Contant;
import com.huynhduc.application.entity.Order;
import com.huynhduc.application.service.OrderService;
import com.huynhduc.application.service.VNPayService;
import com.huynhduc.application.utils.Utiliti;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Map;

import static com.huynhduc.application.utils.Utiliti.formatVND;
import static com.huynhduc.application.utils.Utiliti.formatVnpDate;

@Controller
public class VnPayController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private VNPayService vnPayService ;
    @GetMapping("/api/payment/vnpay-url/{orderId}")
    public ResponseEntity<?> getVNPayPaymentUrl(@PathVariable Long orderId , HttpServletRequest http) throws UnsupportedEncodingException {
        Order order = orderService.findById(orderId);
        if (order == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Không tìm thấy đơn hàng"));
        }
        String ip = Utiliti.getClientIPFromRequest(http);

        String paymentUrl = vnPayService.buildPaymentUrl(order.getId()+"", BigDecimal.valueOf(order.getTotalPrice()),ip);
        System.out.println(paymentUrl);
        return ResponseEntity.ok(Map.of("paymentUrl", paymentUrl));
    }
    @GetMapping("/vnpay/return")
    public String handleReturnUrl(@RequestParam Map<String, String> params, Model model) {
        String vnp_ResponseCode = params.get("vnp_ResponseCode");
        String vnp_TxnRef = params.get("vnp_TxnRef");
        String vnp_Amount = params.get("vnp_Amount");
        String vnp_PayDate = params.get("vnp_PayDate");
        String vnp_CardType = params.get("vnp_CardType");

        // Gán đúng tên biến theo payment_result.html
        model.addAttribute("orderId", vnp_TxnRef);
        model.addAttribute("totalAmount", formatVND(vnp_Amount));
        model.addAttribute("paymentDate", formatVnpDate(vnp_PayDate));
        model.addAttribute("paymentMethod", vnp_CardType);

        if ("00".equals(vnp_ResponseCode)) {
            boolean isUpdated = orderService.updateOrderStatus(vnp_TxnRef, Contant.COMPLETED_STATUS == 3 ? "3" : "1");

            if (isUpdated) {
                model.addAttribute("message", "Thanh toán thành công");
                model.addAttribute("success", true);
            } else {
                model.addAttribute("message", "Lỗi cập nhật đơn hàng");
                model.addAttribute("success", false);
            }
        } else {
            model.addAttribute("message", "Thanh toán thất bại");
            model.addAttribute("success", false);
        }
        return "shop/payment_result"; // Trả về view payment_result.html
    }
}
