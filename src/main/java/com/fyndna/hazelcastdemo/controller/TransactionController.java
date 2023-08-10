package com.fyndna.hazelcastdemo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fyndna.hazelcastdemo.model.PubsubProcessingStatus;
import com.fyndna.hazelcastdemo.model.PubsubResponse;
import com.fyndna.hazelcastdemo.model.TransactionRequest;
import com.fyndna.hazelcastdemo.service.TransactionService;
import io.dapr.client.domain.CloudEvent;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/transaction")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

  private final TransactionService transactionService;
  private final ObjectMapper objectMapper;

  /**
   * Starts the transaction by publishing request to kafka and adding a listener in hazelcast map.
   * The response is sent back to caller once the message is consumed from kafka by {@link
   * TransactionController#complete(CloudEvent)} and processed.
   *
   * @param request
   * @param response
   * @throws IOException
   */
  @PostMapping("/start")
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

    final AsyncContext asyncContext = request.startAsync();
    try (ServletInputStream input = request.getInputStream()) {
      TransactionRequest transactionRequest =
          objectMapper.readValue(input, TransactionRequest.class);
      transactionService.start(asyncContext, transactionRequest);
    } catch (IOException e) {
      log.error("error reading request");
      throw new RuntimeException("unable to process request");
    }
  }

  /**
   * Completes the transaction by adding an entry in hazelcast map with transaction id as key and
   * response as value
   *
   * @param cloudEvent
   * @return
   */
  @PostMapping("/complete")
  public PubsubResponse complete(@RequestBody CloudEvent<TransactionRequest> cloudEvent) {
    try {
      transactionService.complete(cloudEvent.getData());
    } catch (Exception e) {
      log.error(
          "Unable to Complete Transaction {} due to", cloudEvent.getData().getTransactionId(), e);
      return new PubsubResponse(PubsubProcessingStatus.DROP);
    }
    return new PubsubResponse(PubsubProcessingStatus.SUCCESS);
  }
}
