package sorokin.java.course.account;

import org.springframework.stereotype.Component;
import sorokin.java.course.transaction.TransactionHelper;
import sorokin.java.course.user.User;


@Component
public class AccountService {

    private final TransactionHelper transactionHelper;
    private final AccountProperties accountProperties;

    public AccountService(
            AccountProperties accountProperties,
            TransactionHelper transactionHelper
    ) {
        this.accountProperties = accountProperties;
        this.transactionHelper = transactionHelper;
    }

    public Account createAccount(User user) {
        if (user == null) {
            throw new IllegalArgumentException("user must not be null");
        }

        return transactionHelper.executeInTransaction(session -> {
            Account newAccount = new Account(user, accountProperties.getDefaultAmount());
            session.persist(newAccount);
            return newAccount;
        });
    }

    public void withdraw(Integer fromAccountId, Integer amount) {
        validatePositiveId(fromAccountId, "account id");
        validatePositiveAmount(amount);

        transactionHelper.executeInTransaction(session -> {
            Account account = session.get(Account.class, fromAccountId);
            if(account == null) {
                throw new IllegalArgumentException("No such account: id=%s".formatted(fromAccountId));
            }

            if (amount > account.getMoneyAmount()) {
                throw new IllegalArgumentException(
                        "insufficient funds on account id=%s, moneyAmount=%s, attempted withdraw=%s"
                                .formatted(account.getId(), account.getMoneyAmount(), amount)
                );
            }

            account.setMoneyAmount(account.getMoneyAmount() - amount);
        });
    }

    public void deposit(Integer toAccountId, Integer amount) {
        validatePositiveId(toAccountId, "account id");
        validatePositiveAmount(amount);

        transactionHelper.executeInTransaction(session -> {
            Account account = session.get(Account.class, toAccountId);
            if(account == null) {
                throw new IllegalArgumentException("No such account: id=%s".formatted(toAccountId));
            }

            account.setMoneyAmount(account.getMoneyAmount() + amount);
        });
    }

    public Account closeAccount(Integer accountId) {
        validatePositiveId(accountId, "account id");

        return transactionHelper.executeInTransaction(session -> {
            Account accountToClose = session.get(Account.class, accountId);
            if (accountToClose == null) {
                throw new IllegalArgumentException("No such account: id=%s".formatted(accountId));
            }

            var userId = accountToClose.getUser().getId();
            var userAccounts = session.createQuery("SELECT a FROM Account a WHERE a.user.id = :userId", Account.class)
                    .setParameter("userId", userId)
                    .list();

            if (userAccounts.size() == 1) {
                throw new IllegalStateException("Can't close the only one account");
            }

            session.remove(accountToClose);

            var accountToTransferMoney = userAccounts.stream()
                    .filter(it -> it.getId() != accountId)
                    .findFirst()
                    .orElseThrow();

            var newAmount = accountToTransferMoney.getMoneyAmount() + accountToClose.getMoneyAmount();
            accountToTransferMoney.setMoneyAmount(newAmount);
            return accountToClose;
        });
    }

    public void transfer(int fromAccountId, int toAccountId, int amount) {
        validatePositiveId(fromAccountId, "source account id");
        validatePositiveId(toAccountId, "target account id");
        validatePositiveAmount(amount);
        if (fromAccountId == toAccountId) {
            throw new IllegalArgumentException("source and target account id must be different");
        }

        transactionHelper.executeInTransaction(session -> {
            Account accountFrom = session.get(Account.class, fromAccountId);
            Account accountTo = session.get(Account.class, toAccountId);

            if(accountFrom == null) {
                throw new IllegalArgumentException("No such account: id=%s".formatted(fromAccountId));
            }

            if(accountTo == null) {
                throw new IllegalArgumentException("No such account: id=%s".formatted(toAccountId));
            }

            if (amount > accountFrom.getMoneyAmount()) {
                throw new IllegalArgumentException(
                        "insufficient funds on account id=%s, moneyAmount=%s, attempted transfer=%s"
                                .formatted(accountFrom.getId(), accountFrom.getMoneyAmount(), amount)
                );
            }
            accountFrom.setMoneyAmount(accountFrom.getMoneyAmount() - amount);

            int amountToTransfer = accountTo.getUser().getId() == accountFrom.getUser().getId()
                    ? amount
                    : (int) Math.round(amount * (1 - accountProperties.getTransferCommission()));
            accountTo.setMoneyAmount(accountTo.getMoneyAmount() + amountToTransfer);
        });
    }

    private void validatePositiveId(Integer id, String fieldName) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(fieldName + " must be > 0");
        }
    }

    private void validatePositiveAmount(Integer amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("amount must be > 0");
        }
    }
}