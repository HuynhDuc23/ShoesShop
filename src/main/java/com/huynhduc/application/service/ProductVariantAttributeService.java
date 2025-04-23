package com.huynhduc.application.service;

import org.springframework.stereotype.Service;

import java.util.List;


public interface ProductVariantAttributeService {
    public List<String> getAttributeValues(String productId);
}
