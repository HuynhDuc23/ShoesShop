package com.huynhduc.application.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class ProductAttributeRequest {
    private Long attributeId;
    @NotBlank(message = "Giá trị không được trống!")
    private String value;
}
