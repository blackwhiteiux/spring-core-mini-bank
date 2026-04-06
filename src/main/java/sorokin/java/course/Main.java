package sorokin.java.course;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import sorokin.java.course.console.OperationsConsoleListener;

public class Main {
    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext("sorokin.java.course")) {
            OperationsConsoleListener consoleListener = context.getBean(OperationsConsoleListener.class);
            consoleListener.runBank();
        }
    }
}

