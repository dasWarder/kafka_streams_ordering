package com.example.kafka_streams.config;

import com.example.kafka_streams.map.OrderMapper;
import com.example.kafka_streams.model.Order;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Slf4j
@Configuration
@RequiredArgsConstructor
@Profile(value = "medium")
public class MediumKafkaConfig {

    private final OrderMapper orderMapper;

    private final ObjectMapper objectMapper;


    @Bean
    public KStream<String, String> kStream(StreamsBuilder builder) {

        KStream<String, String> stream = builder.stream("ordering", Consumed.with(Serdes.String(), Serdes.String()));

        KStream<String, String> orders = stream.mapValues(this::getOrderFromString)
                .filter((k, v) -> v.getType().equals("book"))
                .filter((k, v) -> v.getPrice() < 1000)
                .mapValues(orderMapper::objectToString);
        orders.to("basket", Produced.with(Serdes.String(), Serdes.String()));

    KStream<String, String> stock =
        stream
            .mapValues(this::getOrderFromString)
            .mapValues(orderMapper::orderToStockInfo)
            .mapValues(orderMapper::objectToString);
        stock.to("stock", Produced.with(Serdes.String(), Serdes.String()));

        return orders;
    }


    private Order getOrderFromString(String orderString) {

        Order order = null;

        try {
            order = objectMapper.readValue(orderString, Order.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return order;
    }


}
