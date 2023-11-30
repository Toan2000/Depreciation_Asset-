package com.example.depreciationService.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssetType {
    private Long typeId;
    private String typeName;
    private Double price;
    private Double valuePerMonth;
    private Double valuePrev;
    private Double valuePresent;
    private Map<String,Double> months;
}
