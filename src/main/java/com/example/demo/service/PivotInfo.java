package com.example.demo.service;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PivotInfo {
    private List<String> legend;
    private List<PivotValue> value;
    private List<String> axis;
    @Data
    @Builder
    public static class PivotValue {
        private String value;
        private String aggregation;
    }
}
