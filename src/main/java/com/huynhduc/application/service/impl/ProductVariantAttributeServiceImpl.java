package com.huynhduc.application.service.impl;

import com.huynhduc.application.repository.ProductVariantAttributeRepository;
import com.huynhduc.application.service.ProductVariantAttributeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductVariantAttributeServiceImpl implements ProductVariantAttributeService {
    @Autowired
    private ProductVariantAttributeRepository productVariantAttributeRepository ;
    @Override
    public List<String> getAttributeValues(String productId) {
       return this.productVariantAttributeRepository.findValuesByProductIdAndAttributeName(productId);
    }
}
