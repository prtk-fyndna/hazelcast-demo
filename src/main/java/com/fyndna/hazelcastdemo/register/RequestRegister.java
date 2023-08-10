package com.fyndna.hazelcastdemo.register;

import jakarta.servlet.AsyncContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds the {@link AsyncContext} for the received transaction id in a hashmap.
 * This is required as the http request is completed in a different thread than the one that received the request.
 */
@Component
public class RequestRegister {

  private final Map<String, AsyncContext> register = new HashMap<>();

  public final void addContext(String transactionId, AsyncContext asyncContext) {
    register.put(transactionId, asyncContext);
  }

  public final AsyncContext getContext(String transactionId) {
    return register.get(transactionId);
  }

  public final void deleteContext(String transactionId) {
    register.remove(transactionId);
  }
}
