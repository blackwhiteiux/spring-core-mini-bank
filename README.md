# MiniBank (Spring Core + Hibernate + PostgreSQL)

Консольное учебное банковское приложение на Java + Spring Core с хранением данных в PostgreSQL через Hibernate.

## Что умеет
- создавать пользователей;
- показывать всех пользователей и их счета;
- создавать дополнительные счета;
- пополнять и снимать деньги;
- переводить между счетами (с комиссией для разных пользователей);
- закрывать счет с переносом остатка;
- завершать работу по команде `EXIT`.

## Технологии
- Java 21
- Spring Core (`spring-context`)
- Hibernate ORM (JPA)
- PostgreSQL
- Конфигурация через `@Configuration`, `@PropertySource`, `@Component`
- Транзакционная обвязка с поддержкой nested-вызовов

## Архитектура
- `User`, `Account` — JPA-сущности (аннотации `@Entity`, `@Id`, `@GeneratedValue`, `@ManyToOne`, `@OneToMany`).
- `UserService`, `AccountService` — бизнес-логика и работа с БД через Hibernate.
- `TransactionHelper` — управление транзакциями (открытие/закрытие сессии, commit/rollback, поддержка nested-вызовов).
- `HibernateConfiguration` — настройка подключения к PostgreSQL и Hibernate.
- `OperationCommand` + `ConsoleOperationType` — обработка команд (Command pattern).
- `OperationsConsoleListener` — главный цикл приложения.
- `ConsoleInput` — единая точка чтения/валидации консольного ввода.

## Команды
| Команда | Описание |
|---------|----------|
| `USER_CREATE` | Создать пользователя (автоматически создается счет с начальным балансом) |
| `SHOW_ALL_USERS` | Показать всех пользователей и их счета |
| `ACCOUNT_CREATE` | Создать дополнительный счет |
| `ACCOUNT_DEPOSIT` | Пополнить счет |
| `ACCOUNT_WITHDRAW` | Снять деньги |
| `ACCOUNT_TRANSFER` | Перевести между счетами |
| `ACCOUNT_CLOSE` | Закрыть счет (средства переводятся на другой счет) |
| `EXIT` | Выход |

## Настройки

Файл: `src/main/resources/application.properties`

```properties
account.default-amount=1000
account.transfer-commission=0.01
```

## Настройка базы данных

Создайте базу данных:

```sql
CREATE DATABASE mini_bank;
Проверьте параметры подключения в HibernateConfiguration.java:

Параметр	Значение
Хост	localhost
Порт	5433 (или ваш)
База	mini_bank
Пользователь	postgres
Пароль	root
```
## Запуск
1. Собрать проект:
```bash
mvn clean package
```
2. Запустить:
```bash
mvn exec:java -Dexec.mainClass="sorokin.java.course.Main"
```

Если `exec-maven-plugin` не настроен, можно запускать из IDE через класс `Main`.

## Пример работы
Enter command: USER_CREATE
Enter login: john
User created successfully! ID: 1

Enter command: ACCOUNT_DEPOSIT
Enter account ID: 1
Enter amount: 500
Deposit successful! New balance: 1500

Enter command: ACCOUNT_TRANSFER
Enter source account ID: 1
Enter target account ID: 2
Enter amount: 300
Transfer successful!

Enter command: EXIT

## Проверка в БД

```sql
SELECT * FROM users;
SELECT * FROM accounts;
```

## Структура проекта
src/main/java/sorokin/java/course/
├── Main.java
├── TransactionHelper.java          # Управление транзакциями
├── ConsoleInput.java               # Ввод с консоли
├── ConsoleOperationType.java       # Enum команд
├── OperationCommand.java           # Интерфейс команды
├── OperationsConsoleListener.java  # Главный цикл
├── config/
│   ├── HibernateConfiguration.java # Настройки Hibernate + PostgreSQL
│   └── ApplicationConfiguration.java
├── user/
│   ├── User.java                   # JPA-сущность
│   ├── UserService.java
│   └── commands/
└── account/
    ├── Account.java                # JPA-сущность
    ├── AccountService.java
    ├── AccountProperties.java
    └── commands/