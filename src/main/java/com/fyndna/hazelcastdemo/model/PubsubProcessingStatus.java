package com.fyndna.hazelcastdemo.model;

public enum PubsubProcessingStatus {
    /** Message is processed successfully */
    SUCCESS,
    /** Message to be retried by Dapr */
    RETRY,
    /** Warning is logged and message is dropped */
    DROP
}
