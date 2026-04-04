package sorokin.java.course.operations.commands;

import org.springframework.stereotype.Component;
import sorokin.java.course.account.AccountService;
import sorokin.java.course.console.ConsoleInput;
import sorokin.java.course.operations.ConsoleOperationType;
import sorokin.java.course.operations.OperationCommand;
import sorokin.java.course.user.UserService;

@Component
public class AccountCloseCommand implements OperationCommand {

    private final AccountService accountService;
    private final UserService userService;
    private final ConsoleInput consoleInput;

    public AccountCloseCommand(AccountService accountService, UserService userService, ConsoleInput consoleInput) {
        this.accountService = accountService;
        this.userService = userService;
        this.consoleInput = consoleInput;
    }

    @Override
    public void execute() {
        int accountId = consoleInput.readPositiveInt("Enter account id to close:", "account id");
        var closedAccount = accountService.closeAccount(accountId);
        var user = userService.findUserById(closedAccount.getUser().getId());
        user.getAccountList().remove(closedAccount);
        System.out.println("Account " + accountId + " closed.");
    }

    @Override
    public ConsoleOperationType getOperationType() {
        return ConsoleOperationType.ACCOUNT_CLOSE;
    }
}
