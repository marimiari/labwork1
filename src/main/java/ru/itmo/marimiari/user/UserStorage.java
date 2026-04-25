package ru.itmo.marimiari.user;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserStorage {
    private static final Path USER_FILE = Paths.get("users.json");
    private List<User> users = new ArrayList<>();

    public UserStorage(){
        load(); //загружает пользователей из файла
    }

    public void load(){
        if (!Files.exists(USER_FILE)) return; //преобразует json в user при наличии файла
        try (Reader reader = new FileReader(USER_FILE.toFile())) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<User>>(){}.getType();
            users = gson.fromJson(reader, type);
            if (users == null) users = new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save(){
        try (Writer writer = new FileWriter(USER_FILE.toFile())){
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(users, writer); //записывает текущий список пользователей в json с форматированием
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public boolean register(String login, String password) {
        if (findByLogin(login).isPresent()) return false; // проверяет, нет ли уже пользователя с таким логином
        users.add(new User(login, password)); //если нет-создает нового, добавляет в список и сохраняет в файл
        save();
        return true;
    }

    public Optional<User> login(String login, String password) {
        return findByLogin(login).filter(u -> u.checkPassword(password)); //ищет пользователя по логину, затем проверяет пароль
    }

    private Optional<User> findByLogin(String login) {
        return users.stream().filter(u -> u.getLogin().equals(login)).findFirst(); //внутренний метод, ищет пользователя по логину в списке
    }
}
