package com.db.awmd.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.repository.AccountsRepositoryInMemory;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.service.NotificationService;

import junit.framework.TestCase;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountsServiceTest {

	@Autowired
	private AccountsService accountsService;
	
	@InjectMocks
    @Autowired
    private AccountsRepositoryInMemory accountsRepositoryInMemory;
	
	@Mock
	NotificationService notificationService;
	
	@Before
    public void initMocks(){
        MockitoAnnotations.initMocks(this);
    }

	@Test
	public void addAccount() throws Exception {
		Account account = new Account("Id-123");
		account.setBalance(new BigDecimal(1000));
		this.accountsService.createAccount(account);

		assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
	}

	@Test
	public void addAccount_failsOnDuplicateId() throws Exception {
		String uniqueId = "Id-" + System.currentTimeMillis();
		Account account = new Account(uniqueId);
		this.accountsService.createAccount(account);

		try {
			this.accountsService.createAccount(account);
			fail("Should have failed when adding duplicate account");
		} catch (DuplicateAccountIdException ex) {
			assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
		}

	}

	@Test
	public void accountTransferSuccess() {

		Account fromAccount = new Account("Id-456");
		fromAccount.setBalance(new BigDecimal(1000));
		this.accountsService.createAccount(fromAccount);

		Account toAccount = new Account("Id-789");
		toAccount.setBalance(new BigDecimal(500));
		this.accountsService.createAccount(toAccount);

		this.accountsService.accountTransfer(fromAccount, toAccount, BigDecimal.valueOf(50));
		verify(notificationService, times(1)).notifyAboutTransfer(fromAccount, "You have received 50amount in your account from Id-456");
		assertThat(this.accountsService.getAccount("Id-456").getBalance()).isEqualTo(BigDecimal.valueOf(950));
		assertThat(this.accountsService.getAccount("Id-789").getBalance()).isEqualTo(BigDecimal.valueOf(550));
	}

	@Test
	public void accountTransferFailure() {
		Account fromAccount = new Account("Id-1104");
		fromAccount.setBalance(new BigDecimal(500));
		this.accountsService.createAccount(fromAccount);

		Account toAccount = new Account("Id-1105");
		toAccount.setBalance(new BigDecimal(500));
		this.accountsService.createAccount(toAccount);

		this.accountsService.accountTransfer(fromAccount, toAccount, BigDecimal.valueOf(1000));
		verify(notificationService, times(1)).notifyAboutTransfer(fromAccount, "Transaction of 1000fromId-1104failed due to insufficient balance");
		assertThat(this.accountsService.getAccount("Id-1104").getBalance()).isEqualTo(BigDecimal.valueOf(500));
		assertThat(this.accountsService.getAccount("Id-1105").getBalance()).isEqualTo(BigDecimal.valueOf(500));
	}
}
