package com.example.bunker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTORequest {
    public ProductDTO productDTO1;
    public ProductDTO productDTO2;
}
