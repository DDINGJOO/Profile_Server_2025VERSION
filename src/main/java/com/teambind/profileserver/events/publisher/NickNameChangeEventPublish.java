package com.teambind.profileserver.events.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teambind.profileserver.events.event.UserNickNameChangedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class NickNameChangeEventPublish extends  EventPublisher{
	
	public NickNameChangeEventPublish(KafkaTemplate<String, Object> kafkaTemplate, ObjectMapper objectMapper) {
		super(kafkaTemplate, objectMapper);
	}
	
	public void publish(UserNickNameChangedEvent message) {
		String topic = "user-nickname-changed";
		super.publish(topic, message);
	}
}
