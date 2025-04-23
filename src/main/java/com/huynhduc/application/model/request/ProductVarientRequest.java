package com.huynhduc.application.model.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductVarientRequest {
    private Double price;
    private Integer stockQuantity;
    private List<ProductAttributeRequest> attributes;
}
