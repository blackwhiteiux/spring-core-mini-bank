package sorokin.java.course.user;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Component;
import sorokin.java.course.transaction.TransactionHelper;
import sorokin.java.course.account.Account;
import sorokin.java.course.account.AccountProperties;

import java.util.*;

@Component
public class UserService {

    private final SessionFactory sessionFactory;
    private final TransactionHelper transactionHelper;
    private final AccountProperties accountProperties;

    public UserService(
                       SessionFactory sessionFactory,
                       TransactionHelper transactionHelper,
                       AccountProperties accountProperties
    ) {
        this.sessionFactory = sessionFactory;
        this.transactionHelper = transactionHelper;
        this.accountProperties = accountProperties;
    }

    public User createUser(String login) {
        String normalizedLogin = validateLogin(login);

        return transactionHelper.executeInTransaction(session -> {
            if (isLoginTakenInSession(session, normalizedLogin)) {
                throw new IllegalArgumentException("User already exists with login=%s".formatted(normalizedLogin));
            }

            var user = new User(normalizedLogin, new ArrayList<>());
            session.persist(user);

            var defaultAccount = new Account(user, accountProperties.getDefaultAmount());
            session.persist(defaultAccount);
            user.getAccountList().add(defaultAccount);

            return user;
        });
    }

    public Optional<User> findUserById(Integer id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("user id must be > 0");
        }

        try (Session session = sessionFactory.openSession()){
            String hql = "SELECT u FROM User u LEFT JOIN FETCH u.accountList WHERE u.id = :id";
            User user = session.createQuery(hql, User.class)
                    .setParameter("id", id)
                    .uniqueResult();
            return Optional.ofNullable(user);
        }
    }

    public List<User> findAll() {
        try (Session session = sessionFactory.openSession()){
            return session
                    .createQuery("SELECT u FROM User u LEFT JOIN FETCH u.accountList", User.class)
                    .list();
        }
    }

    private String validateLogin(String login) {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("login must not be blank");
        }
        return login.trim();
    }

    private boolean isLoginTakenInSession(Session session, String login) {
        String sql = "SELECT COUNT(u) FROM User u WHERE u.login = :login";

        var query = session.createQuery(sql, Long.class);
        query.setParameter("login", login);
        return query.getSingleResult() > 0;
    }
}