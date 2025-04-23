package com.huynhduc.application.utils;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Utiliti {
    public static String getClientIPFromRequest(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");

        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getRemoteAddr();
        }

        // Nếu có nhiều IP trong X-Forwarded-For, lấy IP đầu tiên
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0];
        }

        return ipAddress;
    }
    public static String formatVND(String vnp_Amount) {
        long amount = Long.parseLong(vnp_Amount) / 100; // VNPAY trả về x100
        return String.format("%,d VNĐ", amount);
    }

    public static String formatVnpDate(String vnp_PayDate) {
        try {
            DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            LocalDateTime dateTime = LocalDateTime.parse(vnp_PayDate, inputFormat);
            return outputFormat.format(dateTime);
        } catch (Exception e) {
            return "Không xác định";
        }
    }
}
