package com.db.awmd.challenge.domain;

import java.math.BigDecimal;

public class SenderInformation {
	private Account receiverAccount;
	public Account getReceiverAccount() {
		return receiverAccount;
	}
	public void setReceiverAccount(Account receiverAccount) {
		this.receiverAccount = receiverAccount;
	}
	public BigDecimal getReceiverAmount() {
		return receiverAmount;
	}
	public void setReceiverAmount(BigDecimal receiverAmount) {
		this.receiverAmount = receiverAmount;
	}
	private BigDecimal receiverAmount;
}
