package com.huynhduc.application.service;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Map;

public interface VNPayService {
    public String  buildPaymentUrl(String orderCode, BigDecimal amount , String ipAdd) throws UnsupportedEncodingException;
    public String hmacSHA512(String key, String data);
    public boolean verifySignature(Map<String, String> params, String vnp_SecureHash);
}
