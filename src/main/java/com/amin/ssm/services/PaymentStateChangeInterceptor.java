package com.amin.ssm.services;

import com.amin.ssm.domain.Payment;
import com.amin.ssm.domain.PaymentEvent;
import com.amin.ssm.domain.PaymentState;
import com.amin.ssm.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PaymentStateChangeInterceptor extends StateMachineInterceptorAdapter<PaymentState, PaymentEvent> {
    private final PaymentRepository paymentRepository;

    @Override
    public void preStateChange(State<PaymentState, PaymentEvent> state, Message<PaymentEvent> message,
                               Transition<PaymentState, PaymentEvent> transition,
                               StateMachine<PaymentState, PaymentEvent> stateMachine) {
        Optional.ofNullable(message).flatMap(paymentEventMessage ->
                Optional.ofNullable(message.getHeaders().get(PaymentServiceImpl.PAYMENT_ID_HEADER, Long.class)))
                .ifPresent(paymentId -> {
                    Payment payment = paymentRepository.getOne(paymentId);
                    payment.setState(state.getId());
                    paymentRepository.save(payment);
                });
    }
}
