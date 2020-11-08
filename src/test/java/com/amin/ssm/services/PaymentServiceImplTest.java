package com.amin.ssm.services;

import com.amin.ssm.domain.Payment;
import com.amin.ssm.domain.PaymentState;
import com.amin.ssm.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@SpringBootTest
class PaymentServiceImplTest {
    private Payment payment;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @BeforeEach
    void setUp() {
        payment = Payment.builder().amount(new BigDecimal("12.99")).build();
    }

    @Transactional
    @RepeatedTest(10)
    void process() {
        Payment savedPayment = paymentService.newPayment(payment);
        System.out.println("Should be NEW");
        System.out.println("savedPayment = " + savedPayment);
        paymentService.preAuth(savedPayment.getId());
        Payment preAuthedPayment = paymentRepository.getOne(savedPayment.getId());
        System.out.println("Should be PRE_AUTH or PRE_AUTH_ERR");
        System.out.println("preAuthedPayment = " + preAuthedPayment);
        if (preAuthedPayment.getState() == PaymentState.PRE_AUTH) {
            paymentService.authorize(preAuthedPayment.getId());
            Payment authorizedPayment = paymentRepository.getOne(preAuthedPayment.getId());
            System.out.println("Should be AUTH or AUTH_ERR");
            System.out.println("authorizedPayment = " + authorizedPayment);
        }
    }
}
