package com.teambind.profileserver.config;

import static org.mockito.Mockito.mock;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * 테스트 환경을 위한 설정
 *
 * <p>Kafka와 같은 외부 의존성을 Mock으로 대체하여 테스트 환경에서 Spring Context가 정상적으로 로드되도록 함
 */
@TestConfiguration
public class TestConfig {

  /** KafkaTemplate Mock Bean 제공 EventPublisher가 의존하는 KafkaTemplate을 Mock으로 대체 */
  @Bean
  @Primary
  public KafkaTemplate<String, Object> kafkaTemplate() {
    return mock(KafkaTemplate.class);
  }
}
