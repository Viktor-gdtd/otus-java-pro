package otus.study.cashmachine.bank.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import otus.study.cashmachine.bank.dao.AccountDao;
import otus.study.cashmachine.bank.data.Account;
import otus.study.cashmachine.bank.service.impl.AccountServiceImpl;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {
    @Mock
    AccountDao accountDao;

    @InjectMocks
    AccountServiceImpl accountServiceImpl;

    @Test
    void createAccountMock() {
        // test account creation with mock and ArgumentMatcher
        BigDecimal amount = new BigDecimal(1000);
        Account expectedAccount = new Account(0, amount);

        when(accountDao.saveAccount(eq(expectedAccount))).thenReturn(expectedAccount);

        assertEquals(expectedAccount, accountServiceImpl.createAccount(amount));
    }

    @Test
    void createAccountCaptor() {
        // test account creation with ArgumentCaptor
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        accountServiceImpl.createAccount(new BigDecimal(1000));

        verify(accountDao).saveAccount(captor.capture());
    }

    @Test
    void addSum() {
        BigDecimal amount = new BigDecimal(0);
        Account account = new Account(0, amount);

        when(accountDao.getAccount(0L)).thenReturn(account);

        BigDecimal amountForPut = new BigDecimal(5000);

        assertEquals(amount.add(amountForPut), accountServiceImpl.putMoney(0L, amountForPut));
    }

    @Test
    void getSum() {
        BigDecimal amount = new BigDecimal(5000);
        Account account = new Account(0, amount);

        when(accountDao.getAccount(0L)).thenReturn(account);

        BigDecimal amountForGet = new BigDecimal(1000);
        assertEquals(amount.subtract(amountForGet), accountServiceImpl.getMoney(0L, amountForGet));
    }

    @Test
    void getSumShouldThrowException() {
        BigDecimal amount = new BigDecimal(1000);
        Account account = new Account(0, amount);

        when(accountDao.getAccount(0L)).thenReturn(account);

        BigDecimal amountForGet = new BigDecimal(5000);

        assertThrows(IllegalArgumentException.class,
                ()  -> accountServiceImpl.getMoney(0L, amountForGet) );
    }

    @Test
    void getAccount() {
        BigDecimal amount = new BigDecimal(1000);
        Account expectedAccount = new Account(0, amount);

        when(accountDao.getAccount(0L)).thenReturn(expectedAccount);

        assertEquals(expectedAccount, accountServiceImpl.getAccount(0L));
    }

    @Test
    void checkBalance() {
        BigDecimal expectedAmount = new BigDecimal(1000);
        Account account = new Account(0, expectedAmount);

        when(accountDao.getAccount(0L)).thenReturn(account);

        assertEquals(expectedAmount, accountServiceImpl.checkBalance(0L));
        ;
    }
}
