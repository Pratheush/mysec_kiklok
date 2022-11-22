package com.mylearning.javatechie.orderservice.service;

import com.mylearning.javatechie.orderservice.common.Payment;
import com.mylearning.javatechie.orderservice.common.TransactionRequest;
import com.mylearning.javatechie.orderservice.common.TransactionResponse;
import com.mylearning.javatechie.orderservice.entity.Order;
import com.mylearning.javatechie.orderservice.repository.OrderRepository;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@RefreshScope
public class OrderService {

    private static final String UNSTABLE_SAVE_ORDER_SERVICE="unstable-save-order";
    Logger logger= LoggerFactory.getLogger(OrderService.class);
    @Autowired
    private OrderRepository repository;
    @Autowired
    @Lazy
    private RestTemplate template;

    @Value("${microservice.payment-service.endpoints.endpoint.uri}")
    //@Value("http:'//localhost:9181/payment/doPayment/")
    //@Value("http:'//payment-service/payment/doPayment/")
    private String ENDPOINT_URL;

    @CircuitBreaker(name=UNSTABLE_SAVE_ORDER_SERVICE,fallbackMethod = "fallbackSaveOrder")
    @Bulkhead(name=UNSTABLE_SAVE_ORDER_SERVICE)
    public TransactionResponse saveOrder(TransactionRequest request) throws JsonProcessingException {
        String response = "";
        Order order = request.getOrder();
        Payment payment = request.getPayment();
        payment.setOrderId(order.getId());
        payment.setAmount(order.getPrice());

        // we are going to view log in json view format so we are using ObjectMapper().writeValueAsString(request)
        logger.info("Order-Service Request : "+new ObjectMapper().writeValueAsString(request));

        //rest call
        Payment paymentResponse = template.postForObject(ENDPOINT_URL, payment, Payment.class);

        response = paymentResponse.getPaymentStatus().equals("success") ? "payment processing successful and order placed" : "there is a failure in payment api , order added to cart";

        // we are going to view log in json view format so we are using ObjectMapper().writeValueAsString(response)
        logger.info("Order Service getting Response from Payment-Service : "+new ObjectMapper().writeValueAsString(response));
        repository.save(order);
        return new TransactionResponse(order, paymentResponse.getAmount(), paymentResponse.getTransactionId(), response,paymentResponse.getPort());
    }

    public TransactionResponse fallbackSaveOrder(Exception e){
        return new TransactionResponse(null,0,null,e.getMessage(),"0");

    }
}