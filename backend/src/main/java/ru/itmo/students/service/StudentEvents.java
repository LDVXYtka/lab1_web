package ru.itmo.students.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class StudentEvents {

    private static final Logger log = LoggerFactory.getLogger(StudentEvents.class);

    private final String url;
    private final String username;
    private final String password;
    private final List<SseEmitter> clients = new CopyOnWriteArrayList<>();
    private volatile boolean running = true;
    private volatile Connection connection;
    private Thread listener;

    public StudentEvents(@Value("${spring.datasource.url}") String url,
                         @Value("${spring.datasource.username}") String username,
                         @Value("${spring.datasource.password}") String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    @PostConstruct
    public void start() {
        listener = new Thread(this::listen, "student-events");
        listener.setDaemon(true);
        listener.start();
    }

    @PreDestroy
    public void stop() {
        running = false;
        listener.interrupt();
        Connection current = connection;
        if (current != null) {
            try {
                current.close();
            } catch (SQLException e) {
                log.warn("Не удалось закрыть соединение уведомлений", e);
            }
        }
        for (SseEmitter client : clients) client.complete();
        clients.clear();
        try {
            listener.join(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public SseEmitter subscribe() {
        SseEmitter client = new SseEmitter(30_000L);
        client.onCompletion(() -> clients.remove(client));
        client.onTimeout(() -> {
            clients.remove(client);
            client.complete();
        });
        client.onError(error -> clients.remove(client));
        clients.add(client);
        send(client, "ready", "");
        return client;
    }

    private void listen() {
        while (running) {
            try (Connection current = DriverManager.getConnection(url, username, password)) {
                connection = current;
                if (!running) return;
                current.setAutoCommit(true);
                try (Statement statement = current.createStatement()) {
                    statement.execute("LISTEN student_changes");
                }
                PGConnection postgres = current.unwrap(PGConnection.class);
                broadcast("ready", "");
                while (running) {
                    PGNotification[] notifications = postgres.getNotifications(1000);
                    if (notifications == null) continue;
                    for (PGNotification notification : notifications) {
                        broadcast("students-changed", notification.getParameter());
                    }
                }
            } catch (SQLException e) {
                if (running) log.warn("Не удалось получить уведомления PostgreSQL", e);
            } finally {
                connection = null;
            }
            if (!running) break;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void broadcast(String event, String data) {
        for (SseEmitter client : clients) send(client, event, data);
    }

    private void send(SseEmitter client, String event, String data) {
        try {
            client.send(SseEmitter.event().name(event).data(data));
        } catch (IOException | IllegalStateException e) {
            clients.remove(client);
        }
    }
}
