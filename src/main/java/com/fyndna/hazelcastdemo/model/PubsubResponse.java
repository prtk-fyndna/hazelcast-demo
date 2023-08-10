package com.fyndna.hazelcastdemo.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class PubsubResponse {
    private PubsubProcessingStatus status;
}
