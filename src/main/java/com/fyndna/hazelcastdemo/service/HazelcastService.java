package com.fyndna.hazelcastdemo.service;

import com.fyndna.hazelcastdemo.config.HazelcastConfig;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.stereotype.Service;

@Service
public class HazelcastService {

  private final HazelcastInstance client;

  public HazelcastService(HazelcastConfig hazelcastConfig) {
    ClientConfig clientConfig = new ClientConfig();
    clientConfig.setClusterName(hazelcastConfig.getClusterName());
    clientConfig.getNetworkConfig().addAddress(hazelcastConfig.getAddress());

    client = HazelcastClient.newHazelcastClient(clientConfig);
  }

  public HazelcastInstance getClient() {
    return client;
  }
}
