package com.fyndna.hazelcastdemo.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fyndna.hazelcastdemo.model.TransactionResponse;
import com.fyndna.hazelcastdemo.register.RequestRegister;
import com.fyndna.hazelcastdemo.service.HazelcastService;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.map.listener.EntryAddedListener;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.ServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import static com.fyndna.hazelcastdemo.Constants.TRANSACTIONS_MAP;

/**
 * Listens to the entry added event in hazelcast map. For each transaction entry added, the listener
 * will extract the http async context from {@link RequestRegister} and complete it with entry's value
 */
@Slf4j
public class TransactionResponseListener
    implements EntryAddedListener<String, TransactionResponse> {

  private final RequestRegister requestRegister;

  private final HazelcastService hazelcastService;
  private final ObjectMapper objectMapper;

  public TransactionResponseListener(
      RequestRegister requestRegister,
      HazelcastService hazelcastService,
      ObjectMapper objectMapper) {
    this.hazelcastService = hazelcastService;
    this.requestRegister = requestRegister;
    this.objectMapper = objectMapper;
  }

  @Override
  public void entryAdded(EntryEvent<String, TransactionResponse> event) {
    log.info("entry added {}", event);
    AsyncContext asyncContext = requestRegister.getContext(event.getKey());
    try {

      sendResponse(event.getKey(), asyncContext, event.getValue());

    } finally {
      requestRegister.deleteContext(event.getKey());
      hazelcastService.getClient().getMap(TRANSACTIONS_MAP).remove(event.getKey());
    }
  }

  private void sendResponse(
      String transactionId, AsyncContext asyncContext, TransactionResponse transactionResponse) {
    if (asyncContext != null && asyncContext.getResponse() != null) {
      ServletResponse asyncContextResponse = asyncContext.getResponse();
      ServletOutputStream os = null;
      try {
        os = asyncContextResponse.getOutputStream();
        os.print(objectMapper.writeValueAsString(transactionResponse));
      } catch (IOException e) {
        log.error(
            "Error writing to async context response for transaction id {}", transactionId, e);
      } finally {
        closeOutputStream(transactionId, os);
        completeAsyncContext(transactionId, asyncContext);
      }
    } else {
      log.error("async context is null for {}", transactionId);
    }
  }

  private void completeAsyncContext(String transactionId, AsyncContext context) {
    try {
      context.complete();
    } catch (IllegalStateException e) {
      log.error(
          "Request already timed out for transaction id {}, before marking it as complete using asynchronous context",
          transactionId,
          e);
    } catch (Exception e) {
      log.error(
          "Exception occurred in non blocking IO thread for transaction id {}, while marking request as complete using asynchronous context",
          transactionId,
          e);
    }
  }

  private void closeOutputStream(String transactionId, ServletOutputStream os) {
    if (os != null) {
      try {
        os.close();
      } catch (IOException e) {
        log.error("Error closing ServletOutputStream for transaction id {}", transactionId, e);
      }
    }
  }
}
