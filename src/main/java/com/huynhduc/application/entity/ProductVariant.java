package com.huynhduc.application.entity;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "product_variants")
public class ProductVariant {
    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private Double price;
    private int stockQuantity;

}
