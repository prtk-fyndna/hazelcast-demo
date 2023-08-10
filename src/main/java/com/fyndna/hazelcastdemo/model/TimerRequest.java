package com.fyndna.hazelcastdemo.model;

import lombok.Data;

@Data
public class TimerRequest {

    private String id;
    private String value;
    private long timeout;
}
