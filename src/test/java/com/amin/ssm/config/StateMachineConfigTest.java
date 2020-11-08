package com.amin.ssm.config;

import com.amin.ssm.domain.PaymentEvent;
import com.amin.ssm.domain.PaymentState;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;

import java.util.UUID;

@SpringBootTest
class StateMachineConfigTest {
    @Autowired
    StateMachineFactory<PaymentState, PaymentEvent> factory;

    @RepeatedTest(10)
    void testStates() {
        StateMachine<PaymentState, PaymentEvent> stateMachine = factory.getStateMachine(UUID.randomUUID());
        stateMachine.start();
        System.out.println(stateMachine.getState().toString());
        stateMachine.sendEvent(PaymentEvent.PRE_AUTHORIZE);
        System.out.println(stateMachine.getState().toString());
        stateMachine.sendEvent(PaymentEvent.AUTHORIZE);
        System.out.println(stateMachine.getState().toString());
        stateMachine.sendEvent(PaymentEvent.AUTH_DECLINED);
        System.out.println(stateMachine.getState().toString());
    }
}