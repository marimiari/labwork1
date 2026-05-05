package ru.itmo.marimiari.user;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class User {
    private long id;
    private String login;
    private String passwordHash;

    public User() {
    } //нужно для библиотеки gson

    public User(String login, String password) { //принимает логин и пароль
        this.login = login;
        this.passwordHash = hashPassword(password); //хеширует пароль и сохраняет хеш
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256"); //берет пароль и превращает в байты, прогоняет через SHA-256, делает уникальную строку длиной 256 бит
            byte[] hash = md.digest(password.getBytes()); //превращает строку пароля в массив байт, вычисляет SHA-256 хеш от этих байтов
            // в результате 32 байта (бинарные данные)
            return Base64.getEncoder().encodeToString(hash); //берет стандартный кодировщик, превращает 32 байта в строку из символов
            //чтобы хранить бинарный хеш в текстовом файле json без проблем с кодировкой
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing algorithm not found", e);
        }
    }

    public boolean checkPassword(String password) {
        return passwordHash.equals(hashPassword(password)); //проверяет, совпадает ли хеш переданного пароля с сохраненным хешем
    }
}
