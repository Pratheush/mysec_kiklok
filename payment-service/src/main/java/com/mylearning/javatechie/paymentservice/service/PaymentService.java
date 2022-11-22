package com.mylearning.javatechie.paymentservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylearning.javatechie.paymentservice.entity.Payment;
import com.mylearning.javatechie.paymentservice.repository.PaymentRepository;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;

@Service
public class PaymentService {

    private static final String UNSTABLE_DO_PAYMENT="unstableDoPayment";

    @Autowired
    private Environment environment;

    @Autowired
    private PaymentRepository repository;

    Logger logger= LoggerFactory.getLogger(PaymentService.class);

    @RateLimiter(name = UNSTABLE_DO_PAYMENT,fallbackMethod = "fallbackDoPayment")
    public Payment doPayment(Payment payment) throws JsonProcessingException {
        payment.setPaymentStatus(paymentProcessing());
        payment.setTransactionId(UUID.randomUUID().toString());
        payment.setPort(environment.getProperty("local.server.port"));

        // we are going to view log in json view format so we are using ObjectMapper().writeValueAsString(payment)
        logger.info("Payment-Service Request : {}",new ObjectMapper().writeValueAsString(payment));

        return repository.save(payment);
    }

    public Payment fallbackDoPayment(Exception e){
        Payment payment=new Payment();
        payment.setTransactionId("Payment Processing Failed - "+UUID.randomUUID().toString());
        payment.setPort(null);
        payment.setPaymentStatus("Payment failure");
        return payment;
    }

    public String paymentProcessing(){
        //api should be 3rd party payment gateway (paypal,paytm...)
        return new Random().nextBoolean()?"success":"false";
    }


    public Payment findPaymentHistoryByOrderId(int orderId) throws JsonProcessingException {
        Payment payment=repository.findByOrderId(orderId);

        // we are going to view log in json view format so we are using ObjectMapper().writeValueAsString(payment)
        logger.info("paymentService findPaymentHistoryByOrderId : {}",new ObjectMapper().writeValueAsString(payment));
        return payment ;
    }


}


