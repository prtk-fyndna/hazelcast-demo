package com.fyndna.hazelcastdemo.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TransactionResponse {

  private String transactionId;
  private String result;
}
