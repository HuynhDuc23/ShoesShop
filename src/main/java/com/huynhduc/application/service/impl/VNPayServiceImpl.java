package com.huynhduc.application.service.impl;

import com.huynhduc.application.service.VNPayService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class VNPayServiceImpl implements VNPayService {
    private final String vnp_Version = "2.1.0";
    private final String vnp_Command = "pay";
    @Value("${vnpay.tmnCode}")
    private String vnp_TmnCode;
    @Value("${vnpay.hashSecret}")
    private String vnp_HashSecret;
    @Value(("${vnpay.payUrl}"))
    private String vnp_PayUrl;
    @Value("${vnpay.returnUrl}")
    private String vnp_ReturnUrl;

    public VNPayServiceImpl() {
    }

    public VNPayServiceImpl(String vnp_TmnCode, String vnp_ReturnUrl, String vnp_PayUrl, String vnp_HashSecret) {
        this.vnp_TmnCode = vnp_TmnCode;
        this.vnp_ReturnUrl = vnp_ReturnUrl;
        this.vnp_PayUrl = vnp_PayUrl;
        this.vnp_HashSecret = vnp_HashSecret;
    }

    public  String buildPaymentUrl(String orderCode, BigDecimal amount , String ipAdd) throws UnsupportedEncodingException {
        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", vnp_TmnCode);
        vnpParams.put("vnp_Amount", amount.multiply(new BigDecimal(100)).toString()); // VNPAY yêu cầu số tiền là VND * 100
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", orderCode); // Mã đơn hàng
        vnpParams.put("vnp_OrderInfo", "Order Shoes"); // Thông tin mô tả
        vnpParams.put("vnp_OrderType", "payment"); // Mã danh mục hàng hóa
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_IpAddr", ipAdd); // Địa chỉ IP của người dùng
        vnpParams.put("vnp_ReturnUrl", vnp_ReturnUrl); // URL callback
        vnpParams.put("vnp_CreateDate", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        // Tính toán thời gian hết hạn (thêm 15 phút vào thời gian tạo)
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 15); // Thêm 15 phút
        String vnpExpireDate = new SimpleDateFormat("yyyyMMddHHmmss").format(calendar.getTime());
        vnpParams.put("vnp_ExpireDate", vnpExpireDate);

        // Tạo chuỗi dữ liệu cần hash
        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();

        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) vnpParams.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                // Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = hmacSHA512(vnp_HashSecret, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = vnp_PayUrl + "?" + queryUrl;

        return paymentUrl;
    }

    public String hmacSHA512(final String key, final String data) {
        try {

            if (key == null || data == null) {
                throw new NullPointerException();
            }
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes();
            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();

        } catch (Exception ex) {
            return "";
        }
    }

    @Override
    public boolean verifySignature(Map<String, String> params, String vnp_SecureHash) {
        // Loại bỏ tham số vnp_SecureHash khỏi danh sách tham số
        params.remove("vnp_SecureHash");

        // Sắp xếp các tham số theo thứ tự tăng dần của tên tham số
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        // Xây dựng chuỗi dữ liệu cần tính toán chữ ký
        StringBuilder hashData = new StringBuilder();
        for (String name : fieldNames) {
            String value = params.get(name);
            if (value != null && !value.isEmpty()) {
                hashData.append(name).append("=").append(value).append("&");
            }
        }
        // Loại bỏ dấu "&" cuối cùng
        if (hashData.length() > 0) {
            hashData.setLength(hashData.length() - 1);
        }

        // Tính toán chữ ký HMAC-SHA512
        String calculatedHash = hmacSHA512(vnp_HashSecret, hashData.toString());

        // So sánh chữ ký tính toán với chữ ký nhận được
        return calculatedHash.equals(vnp_SecureHash);
    }

}
