package com.amin.ssm.config;

import com.amin.ssm.domain.PaymentEvent;
import com.amin.ssm.domain.PaymentState;
import com.amin.ssm.services.PaymentServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import java.util.EnumSet;
import java.util.Random;

@Slf4j
@Configuration
@EnableStateMachineFactory
public class StateMachineConfig extends StateMachineConfigurerAdapter<PaymentState, PaymentEvent> {
    @Override
    public void configure(StateMachineStateConfigurer<PaymentState, PaymentEvent> states) throws Exception {
        states.withStates().initial(PaymentState.NEW).states(EnumSet.allOf(PaymentState.class))
                .end(PaymentState.AUTH).end(PaymentState.PRE_AUTH_ERR).end(PaymentState.AUTH_ERR);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<PaymentState, PaymentEvent> transitions) throws Exception {
        transitions.withExternal().source(PaymentState.NEW).target(PaymentState.NEW)
                .event(PaymentEvent.PRE_AUTHORIZE).action(preAuthAction()).guard(paymentIdGuard())
                .and().withExternal().source(PaymentState.NEW)
                .target(PaymentState.PRE_AUTH).event(PaymentEvent.PRE_AUTH_APPROVED)
                .and().withExternal().source(PaymentState.NEW)
                .target(PaymentState.PRE_AUTH_ERR).event(PaymentEvent.PRE_AUTH_DECLINED)
                .and().withExternal().source(PaymentState.PRE_AUTH).target(PaymentState.PRE_AUTH)
                .event(PaymentEvent.AUTHORIZE).action(authorizeAction()).guard(paymentIdGuard())
                .and().withExternal().source(PaymentState.PRE_AUTH)
                .target(PaymentState.AUTH).event(PaymentEvent.AUTH_APPROVED)
                .and().withExternal().source(PaymentState.PRE_AUTH)
                .target(PaymentState.AUTH_ERR).event(PaymentEvent.AUTH_DECLINED);
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<PaymentState, PaymentEvent> config) throws Exception {
        StateMachineListenerAdapter<PaymentState, PaymentEvent> adapter = new StateMachineListenerAdapter<>() {
            @Override
            public void stateChanged(State<PaymentState, PaymentEvent> from, State<PaymentState, PaymentEvent> to) {
                log.info(String.format("stateChanged(from: %s, to: %s)", from, to));
            }
        };
        config.withConfiguration().listener(adapter);
    }

    private Action<PaymentState, PaymentEvent> preAuthAction() {
        return stateContext -> {
            System.out.println("PreAuth was called!!!");
            if (new Random().nextInt(10) < 7) {
                System.out.println("Approved!");
                sendEvent(stateContext, PaymentEvent.PRE_AUTH_APPROVED);
            } else {
                System.out.println("Declined! No Credit!!!!!");
                sendEvent(stateContext, PaymentEvent.PRE_AUTH_DECLINED);
            }
        };
    }

    private Action<PaymentState, PaymentEvent> authorizeAction() {
        return stateContext -> {
            System.out.println("Auth was called!!!");
            if (new Random().nextInt(10) < 3) {
                System.out.println("Approved!");
                sendEvent(stateContext, PaymentEvent.AUTH_APPROVED);
            } else {
                System.out.println("Declined! No Credit!!!!!");
                sendEvent(stateContext, PaymentEvent.AUTH_DECLINED);
            }
        };
    }

    private Guard<PaymentState, PaymentEvent> paymentIdGuard() {
        return stateContext -> stateContext.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER) != null;
    }

    private void sendEvent(StateContext<PaymentState, PaymentEvent> stateContext, PaymentEvent paymentEvent) {
        stateContext.getStateMachine().sendEvent(MessageBuilder.withPayload(paymentEvent)
                .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER,
                        stateContext.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER)).build());
    }
}
