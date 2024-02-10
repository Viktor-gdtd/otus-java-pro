package otus.study.cashmachine.machine.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import otus.study.cashmachine.bank.service.CardService;
import otus.study.cashmachine.machine.data.CashMachine;
import otus.study.cashmachine.machine.data.MoneyBox;
import otus.study.cashmachine.machine.service.impl.CashMachineServiceImpl;


import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CashMachineServiceTest {
    @Spy
    private CardService cardService;

    @Spy
    private MoneyBoxService moneyBoxService;

    @InjectMocks
    private CashMachineServiceImpl cashMachineService;


    private CashMachine cashMachine = new CashMachine(new MoneyBox());

    @Test
    void getMoney() {
        // create get money test using spy as mock
        List<Integer> expectedNotes = List.of(1, 0, 0, 0);

        when(moneyBoxService.getMoney(any(), anyInt())).thenReturn(expectedNotes);
        assertEquals(expectedNotes, cashMachineService.getMoney(cashMachine, "1111", "0000", new BigDecimal(1000)));
    }

    @Test
    void putMoney() {
       BigDecimal amount = new BigDecimal(1000);

       when(cardService.putMoney("1111", "0000", amount)).thenReturn(amount);
       assertEquals(amount, cashMachineService.putMoney(cashMachine, "1111", "0000", List.of(0, 1, 0, 0)));
    }

    @Test
    void checkBalance() {
        BigDecimal expectedAmount = new BigDecimal(1000);

        when(cardService.getBalance("1111", "0000")).thenReturn(expectedAmount);
        assertEquals(expectedAmount, cashMachineService.checkBalance(cashMachine, "1111", "0000"));

    }

    @Test
    void changePin() {
        // change pin test using spy as implementation and ArgumentCaptor and thenReturn
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        when(cardService.cnangePin(captor.capture(), captor.capture(), captor.capture())).thenReturn(true);
        assertTrue(cashMachineService.changePin("1111", "0000", "0001"));
        verify(cardService).cnangePin(captor.capture(), captor.capture(), captor.capture());
    }

    @Test
    void changePinWithAnswer() {
        // change pin test using spy as implementation and mock an thenAnswer
        when(cardService.cnangePin(any(), any(), any()))
                .thenAnswer(in ->
                        "1111".equals(in.getArgument(0))
                                && "0000".equals(in.getArgument(1))
                                && "0001".equals(in.getArgument(2))
                );
        assertTrue(cashMachineService.changePin("1111", "0000", "0001"));
    }
}