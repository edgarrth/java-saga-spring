package com.example.payments.infrastructure.adapters.out.messaging;

import com.example.payments.application.saga.choreography.PaymentChoreographySaga;
import com.example.payments.domain.events.*;
import com.fasterxml.jackson.databind.JsonNode;import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;import org.springframework.stereotype.Component;

@Component
public class ChoreographyEventConsumer {
 private final ObjectMapper mapper; private final PaymentChoreographySaga saga;
 public ChoreographyEventConsumer(ObjectMapper mapper, PaymentChoreographySaga saga){this.mapper=mapper;this.saga=saga;}
 @KafkaListener(topics = "${app.topics.payment-events}")
 public void onMessage(String payload) throws Exception {
   JsonNode node = mapper.readTree(payload); String type = node.get("eventType").asText();
   switch(type){
     case "PaymentCreatedEvent" -> saga.on(mapper.readValue(payload, PaymentCreatedEvent.class));
     case "FundsReservedEvent" -> saga.on(mapper.readValue(payload, FundsReservedEvent.class));
     case "FraudApprovedEvent" -> saga.on(mapper.readValue(payload, FraudApprovedEvent.class));
     case "FraudRejectedEvent" -> saga.on(mapper.readValue(payload, FraudRejectedEvent.class));
     case "SettlementCapturedEvent" -> saga.on(mapper.readValue(payload, SettlementCapturedEvent.class));
     case "PaymentCompletedEvent" -> saga.on(mapper.readValue(payload, PaymentCompletedEvent.class));
     default -> { }
   }
 }
}
