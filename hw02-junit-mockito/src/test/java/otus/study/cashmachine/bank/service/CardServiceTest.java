package otus.study.cashmachine.bank.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import otus.study.cashmachine.TestUtil;
import otus.study.cashmachine.bank.dao.CardsDao;
import otus.study.cashmachine.bank.data.Card;
import otus.study.cashmachine.bank.service.impl.CardServiceImpl;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CardServiceTest {
    @Mock
    AccountService accountService;

    @Mock
    CardsDao cardsDao;

    @InjectMocks
    CardServiceImpl cardService;

    @Test
    void testCreateCard() {
        when(cardsDao.createCard("5555", 1L, "0123")).thenReturn(
                new Card(1L, "5555", 1L, "0123"));

        Card newCard = cardService.createCard("5555", 1L, "0123");
        assertNotEquals(0, newCard.getId());
        assertEquals("5555", newCard.getNumber());
        assertEquals(1L, newCard.getAccountId());
        assertEquals("0123", newCard.getPinCode());
    }

    @Test
    void checkBalance() {
        Card card = new Card(1L, "1234", 1L, TestUtil.getHash("0000"));

        when(cardsDao.getCardByNumber(anyString())).thenReturn(card);
        when(accountService.checkBalance(1L)).thenReturn(new BigDecimal(1000));

        BigDecimal sum = cardService.getBalance("1234", "0000");
        assertEquals(0, sum.compareTo(new BigDecimal(1000)));
    }

    @Test
    void checkBalanceShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> cardService.getBalance("1111", "0000"));

    }

    @Test
    void getMoney() {
        ArgumentCaptor<BigDecimal> amountCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);

        when(cardsDao.getCardByNumber("1111"))
                .thenReturn(new Card(1L, "1111", 100L, TestUtil.getHash("0000")));

        when(accountService.getMoney(idCaptor.capture(), amountCaptor.capture()))
                .thenReturn(BigDecimal.TEN);

        cardService.getMoney("1111", "0000", BigDecimal.ONE);

        verify(accountService, only()).getMoney(anyLong(), any());
        assertEquals(BigDecimal.ONE, amountCaptor.getValue());
        assertEquals(100L, idCaptor.getValue().longValue());
    }

    @Test
    void getMoneyShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> cardService.getMoney("1111", "0000", new BigDecimal(1000)));

    }

    @Test
    void putMoney() {
        BigDecimal amountForPut = new BigDecimal(1000);

        when(cardsDao.getCardByNumber("1111"))
                .thenReturn(new Card(1L, "1111", 0L, TestUtil.getHash("0000")));
        when(accountService.putMoney(0L, amountForPut)).thenReturn(amountForPut);
        assertEquals(amountForPut, cardService.putMoney("1111", "0000", amountForPut));
    }

    @Test
    void putMoneyShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> cardService.putMoney("1111", "0000", new BigDecimal(1000)));
    }

    @Test
    void checkIncorrectPin() {
        Card card = new Card(1L, "1234", 1L, "0000");
        when(cardsDao.getCardByNumber(eq("1234"))).thenReturn(card);

        Exception thrown = assertThrows(IllegalArgumentException.class, () -> {
            cardService.getBalance("1234", "0012");
        });
        assertEquals(thrown.getMessage(), "Pincode is incorrect");
    }

    @Test
    void changePin() {
        Card card = new Card(1L, "1111", 0L, TestUtil.getHash("0000"));

        when(cardsDao.getCardByNumber("1111")).thenReturn(card);
        assertTrue(cardService.cnangePin("1111", "0000", "0001"));
    }

    @Test
    void changePinWithIncorrectOldPin() {
        Card card = new Card(1L, "1111", 0L, TestUtil.getHash("0000"));

        when(cardsDao.getCardByNumber("1111")).thenReturn(card);
        assertFalse(cardService.cnangePin("1111", "0002", "0001"));
    }

    @Test
    void changePinShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> cardService.cnangePin("1111", "0000", "0001"));
    }
}