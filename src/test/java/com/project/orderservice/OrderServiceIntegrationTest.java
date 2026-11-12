package com.project.orderservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.orderservice.dto.OrderCreatedResponse;
import com.project.orderservice.dto.OrderRequest;
import com.project.orderservice.repository.OrderRepository;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class OrderServiceIntegrationTest {

    @ServiceConnection
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:16-alpine")
    );

    @Container
    static KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.6.0"));

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.stream.kafka.binder.brokers", kafka::getBootstrapServers);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeAll
    static void createTopic() throws Exception {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        try (AdminClient admin = AdminClient.create(props)) {
            NewTopic topic = new NewTopic("orders-topic", 1, (short) 1);
            admin.createTopics(Collections.singleton(topic)).all().get();
            System.out.println("✅ Topic 'orders-topic' created successfully");
        }
    }

    @Test
    void whenPostOrder_ThenSaveInDatabaseAndSendBackToKafka() throws Exception {

        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        try (Consumer<String, String> consumer = new DefaultKafkaConsumerFactory<String, String>(consumerProps).createConsumer()) {
            consumer.subscribe(List.of("orders-topic"));
            consumer.poll(Duration.ofMillis(500));

            OrderRequest request = new OrderRequest("LAPTOP-001", 3);
            String jsonRequest = objectMapper.writeValueAsString(request);

            MvcResult result = mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isCreated())
                    .andReturn();

            String jsonResponse = result.getResponse().getContentAsString();
            OrderCreatedResponse response = objectMapper.readValue(jsonResponse, OrderCreatedResponse.class);

            Assertions.assertNotNull(response.id());
            Assertions.assertEquals("Order created successfully", response.message());
            Assertions.assertTrue(orderRepository.findById(
                    response.id()).isPresent(), "Order should be in DB");

            Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
                var records = consumer.poll(Duration.ofMillis(500));
                Assertions.assertFalse(records.isEmpty(), "No messages received in Kafka");
                ConsumerRecord<String, String> record = records.iterator().next();
                Assertions.assertTrue(record.value()
                        .contains("LAPTOP-001"), "Message should contain product ID");
            });
        }
    }
}
