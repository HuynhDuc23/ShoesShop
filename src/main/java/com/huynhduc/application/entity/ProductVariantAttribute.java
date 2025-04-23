package com.huynhduc.application.entity;

import lombok.*;

import javax.persistence.*;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "product_variant_attributes")
public class ProductVariantAttribute {
    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "variant_id")
    private ProductVariant productVariant;

    @ManyToOne
    @JoinColumn(name = "attribute_id")
    private ProductAttribute attribute;

    private String value; // Ví dụ: "Đỏ", "M", "Cotton"
}