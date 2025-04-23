package com.huynhduc.application.entity;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Table(name = "product_attributes")
public class ProductAttribute {
    @Id
    private String id;

    private String name; // Ví dụ: "color", "size", "material"

}
