package com.fyndna.hazelcastdemo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fyndna.hazelcastdemo.config.PubsubConfig;
import com.fyndna.hazelcastdemo.listener.TransactionResponseListener;
import com.fyndna.hazelcastdemo.model.TransactionRequest;
import com.fyndna.hazelcastdemo.model.TransactionResponse;
import com.fyndna.hazelcastdemo.register.RequestRegister;
import com.hazelcast.map.IMap;
import io.dapr.client.DaprClient;
import jakarta.servlet.AsyncContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static com.fyndna.hazelcastdemo.Constants.STATE_STORE_NAME;
import static com.fyndna.hazelcastdemo.Constants.TRANSACTIONS_MAP;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

  private final HazelcastService hazelcastService;
  private final RequestRegister requestRegister;
  private final ObjectMapper objectMapper;
  private final DaprClient daprClient;

  private final PubsubConfig pubsubConfig;

  /**
   * This method adds the http async context in {@link RequestRegister}, adds {@link
   * TransactionResponseListener} to hazelcast map and publishes the request to kafka
   *
   * @param asyncContext
   * @param transactionRequest
   */
  public void start(AsyncContext asyncContext, TransactionRequest transactionRequest) {
    log.info("request received for id {}", transactionRequest.getTransactionId());
    requestRegister.addContext(transactionRequest.getTransactionId(), asyncContext);

    IMap<String, TransactionResponse> mapTransactions =
        hazelcastService.getClient().getMap(TRANSACTIONS_MAP);
    mapTransactions.addEntryListener(
        new TransactionResponseListener(requestRegister, hazelcastService, objectMapper),
        transactionRequest.getTransactionId(),
        true);
    daprClient
        .publishEvent(pubsubConfig.getName(), pubsubConfig.getTopic(), transactionRequest)
        .block();
  }

  /**
   * This method completes the transaction by adding the response entry in hazelcast corresponding
   * to transaction request. The {@link TransactionResponseListener} is invoked in the instance which had originally received
   * the transaction and had added the listener for this transaction id
   *
   * @param transactionRequest
   */
  public void complete(TransactionRequest transactionRequest) {

    //    insertResponseUsingDapr(transactionRequest);

    insertResponse(transactionRequest);
  }

  private void insertResponse(TransactionRequest transactionRequest) {
    IMap<String, TransactionResponse> mapTransactions =
        hazelcastService.getClient().getMap(TRANSACTIONS_MAP);
    var response = new TransactionResponse(transactionRequest.getTransactionId(), "SUCCESS");
    mapTransactions.put(transactionRequest.getTransactionId(), response, 30, TimeUnit.SECONDS);
  }

  private void insertResponseUsingDapr(TransactionRequest transactionRequest) {
    daprClient
        .saveState(
            STATE_STORE_NAME,
            transactionRequest.getTransactionId(),
            new TransactionResponse(transactionRequest.getTransactionId(), "SUCCESS"))
        .block();
  }
}
