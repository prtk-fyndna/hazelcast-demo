package com.fyndna.hazelcastdemo;

import io.dapr.client.DaprClient;
import io.dapr.client.DaprClientBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class HazelcastDemoApplication {

	@Bean
	public DaprClient daprClient() {
		return new DaprClientBuilder().build();
	}
	public static void main(String[] args) {
		SpringApplication.run(HazelcastDemoApplication.class, args);
	}

}
