package com.db.awmd.challenge.repository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.ReceiverInformation;
import com.db.awmd.challenge.domain.SenderInformation;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.service.NotificationService;

@Repository
public class AccountsRepositoryInMemory implements AccountsRepository {

	private final Map<String, Account> accounts = new ConcurrentHashMap<>();

	@Autowired
	private NotificationService notificationService;

	private final String INSUFFICIENT_BALANCE = "INSUFFICIENT_BALANCE";
	private final String SUCCESS = "SUCCESS";

	@Override
	public void createAccount(Account account) throws DuplicateAccountIdException {
		Account previousAccount = accounts.putIfAbsent(account.getAccountId(), account);
		if (previousAccount != null) {
			throw new DuplicateAccountIdException("Account id " + account.getAccountId() + " already exists!");
		}
	}

	@Override
	public Account getAccount(String accountId) {
		return accounts.get(accountId);
	}

	@Override
	public void clearAccounts() {
		accounts.clear();
	}

	@Override
	public void accountTransfer(Account fromAccount, Account toAccount, BigDecimal amount) {
		String transferStatus;
		try {
			transferStatus = fromAccount.depositAmount(toAccount, amount);
		} catch (IllegalArgumentException iae) {
			transferStatus = iae.getMessage();
		}
		SenderInformation senderInformation = getSenderInformation(toAccount, amount);
		ReceiverInformation receiverInformation = getReceiverInformation(fromAccount, amount);
		sendReceiverNotification(receiverInformation, transferStatus);
		sendSenderNotification(senderInformation, transferStatus);

	}

	private String getReceiverSuccessMessage(ReceiverInformation receiverInformation) {
		return "You have received " + receiverInformation.getSenderAmount() + "amount in your account from "
				+ receiverInformation.getSenderAccount().getAccountId();
	}

	private String getReceiverFailureMessage(ReceiverInformation receiverInformation) {
		return "Transaction of " + receiverInformation.getSenderAmount() + "from"
				+ receiverInformation.getSenderAccount().getAccountId() + "failed due to insufficient balance";
	}

	private void sendReceiverNotification(ReceiverInformation receiverInformation, String status) {
		if (status.equals(SUCCESS)) {
			notificationService.notifyAboutTransfer(receiverInformation.getSenderAccount(),
					getReceiverSuccessMessage(receiverInformation));
		}
		if (status.equals(INSUFFICIENT_BALANCE)) {
			notificationService.notifyAboutTransfer(receiverInformation.getSenderAccount(),
					getReceiverFailureMessage(receiverInformation));
		}

	}

	private String getSenderSuccessMessage(SenderInformation senderInformation) {
		return "Your transaction of " + senderInformation.getReceiverAmount() + "amount has been deposited "
				+ senderInformation.getReceiverAccount().getAccountId();
	}

	private String getSenderFailureMessage(SenderInformation senderInformation) {
		return "Transaction of " + senderInformation.getReceiverAmount() + "from"
				+ senderInformation.getReceiverAccount().getAccountId() + "failed due to insufficient balance";
	}

	private void sendSenderNotification(SenderInformation senderInformation, String status) {
		if (status.equals(SUCCESS)) {
			notificationService.notifyAboutTransfer(senderInformation.getReceiverAccount(),
					getSenderSuccessMessage(senderInformation));
		}
		if (status.equals(INSUFFICIENT_BALANCE)) {
			notificationService.notifyAboutTransfer(senderInformation.getReceiverAccount(),
					getSenderFailureMessage(senderInformation));
		}

	}

	private SenderInformation getSenderInformation(Account receiverAccount, BigDecimal receiverAmount) {
		SenderInformation senderInformation = new SenderInformation();
		senderInformation.setReceiverAccount(receiverAccount);
		senderInformation.setReceiverAmount(receiverAmount);
		return senderInformation;

	}

	private ReceiverInformation getReceiverInformation(Account senderAccount, BigDecimal senderAmount) {
		ReceiverInformation receiverInformation = new ReceiverInformation();
		receiverInformation.setSenderAccount(senderAccount);
		receiverInformation.setSenderAmount(senderAmount);
		return receiverInformation;

	}

}
