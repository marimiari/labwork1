package ru.itmo.marimiari.ui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import ru.itmo.marimiari.user.UserStorage;

public class LoginDialog {
    private final UserStorage userStorage = new UserStorage();
    private String loggedInUser = null;

    public boolean showAndWait(){
        Stage stage = new Stage();
        stage.setTitle("Welcome | Login / Register");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        TextField loginField = new TextField();
        PasswordField passwordField = new PasswordField();

        grid.add(new Label("Login:"), 0, 0);
        grid.add(loginField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);

        Button loginButton = new Button("Login");
        Button registerButton = new Button("Register");

        grid.add(loginButton, 0, 2);
        grid.add(registerButton, 1, 2);

        loginButton.setOnAction(e -> {
            String login = loginField.getText().trim();
            String password = passwordField.getText();
            if (login.isEmpty() || password.isEmpty()) {
                showAlert("Please fill both fields");
                return;
            }
            if (userStorage.login(login, password).isPresent()) {
                loggedInUser = login;
                stage.close();
            } else {
                showAlert("Invalid login or password");
            }
        });

        registerButton.setOnAction(e-> {
            String login = loginField.getText().trim();
            String password = passwordField.getText();
            if (login.isEmpty() || password.isEmpty()){
                showAlert("Please fill both fields");
                return;
            }
            if (userStorage.register(login, password)){
                showAlert("Registration successful. Please login.");
            } else {
                showAlert("Login already exists");
            }
        });

        Scene scene = new Scene(grid,300,150);
        stage.setScene(scene);
        stage.showAndWait();

        return loggedInUser != null; //возвращает true, если успешно вошли
    }

    public String getLoggedInUser(){
        return loggedInUser; //возвращает логин вошедшего пользователя
    }

    private void showAlert(String msg){
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.showAndWait();
    }
}
