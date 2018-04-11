package com.db.awmd.challenge.domain;

import java.math.BigDecimal;

public class ReceiverInformation {
	private Account senderAccount;
	public Account getSenderAccount() {
		return senderAccount;
	}
	public void setSenderAccount(Account senderAccountId) {
		this.senderAccount = senderAccountId;
	}
	public BigDecimal getSenderAmount() {
		return senderAmount;
	}
	public void setSenderAmount(BigDecimal senderAmount) {
		this.senderAmount = senderAmount;
	}
	private BigDecimal senderAmount;
}
