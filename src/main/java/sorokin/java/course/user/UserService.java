package sorokin.java.course.user;

import org.springframework.stereotype.Component;
import sorokin.java.course.account.AccountService;
import sorokin.java.course.user.User;

import java.util.*;

@Component
public class UserService {

    private int idCounter;
    private final Map<Integer, User> userMap;
    private final Set<String> takenLogins;
    private final AccountService accountService;

    public UserService(AccountService accountService) {
        this.idCounter = 0;
        this.userMap = new HashMap<>();
        this.takenLogins = new HashSet<>();
        this.accountService = accountService;
    }

    public User createUser(String login) {
        String normalizedLogin = validateLogin(login);
        if (takenLogins.contains(normalizedLogin)) {
            throw new IllegalArgumentException("User already exists with login=%s".formatted(normalizedLogin));
        }

        idCounter++;
        var user = new User(idCounter, normalizedLogin, new ArrayList<>());
        var defaultAccount = accountService.createAccount(user);
        user.getAccountList().add(defaultAccount);

        userMap.put(idCounter, user);
        takenLogins.add(normalizedLogin);
        return user;
    }

    public User findUserById(Integer id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("user id must be > 0");
        }
        var user = userMap.get(id);
        if (user == null) {
            throw new IllegalArgumentException("No such user with id=%s".formatted(id));
        }
        return user;
    }

    public List<User> findAll() {
        return userMap.values().stream().toList();
    }

    private String validateLogin(String login) {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("login must not be blank");
        }
        return login.trim();
    }
}