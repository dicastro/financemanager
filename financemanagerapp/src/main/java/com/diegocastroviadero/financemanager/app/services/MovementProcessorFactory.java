package com.diegocastroviadero.financemanager.app.services;

import com.diegocastroviadero.financemanager.app.model.AccountPurpose;
import com.diegocastroviadero.financemanager.app.model.Bank;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class MovementProcessorFactory {
    private final List<MovementProcessor> movementProcessors;

    public MovementProcessor getMovementProcessor(final Bank bank, final AccountPurpose accountPurpose) {
        return movementProcessors.stream()
                .filter(mp -> mp.applies(bank, accountPurpose))
                .findFirst()
                .get(); // GenericMovementProcessor always applies and it is the last element of movementProcessors list, so always one MovementProcessor will be found
    }
}
