package com.db.awmd.challenge.domain;

import java.math.BigDecimal;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class Account {

	@NotNull
	@NotEmpty
	private final String accountId;
	private final Lock lock = new ReentrantLock();
	private final String INSUFFICIENT_BALANCE = "INSUFFICIENT_BALANCE";
	private final String SUCCESS = "SUCCESS";
	private final String UNKNOWN = "UNKNOWN";

	@NotNull
	@Min(value = 0, message = "Initial balance must be positive.")
	private BigDecimal balance;

	public Account(String accountId) {
		this.accountId = accountId;
		this.balance = BigDecimal.ZERO;
	}

	@JsonCreator
	public Account(@JsonProperty("accountId") String accountId, @JsonProperty("balance") BigDecimal balance) {
		this.accountId = accountId;
		this.balance = balance;
	}

	public String depositAmount(Account toAccount, BigDecimal amount) {
		String status = UNKNOWN;
		if (this.lock.tryLock()) {
			try {
				if (toAccount.lock.tryLock()) {
					try {
						if (this.getBalance().compareTo(amount) < 0) {
							status = INSUFFICIENT_BALANCE;
							throw new IllegalArgumentException(INSUFFICIENT_BALANCE);
						}
						toAccount.setBalance(toAccount.getBalance().add(amount));
						this.setBalance(this.getBalance().subtract(amount));
						status = SUCCESS;
					} finally {
						toAccount.lock.unlock();
					}
				}

			} finally {
				this.lock.unlock();
			}
		}
		return status;
	}

}
