package ru.itmo.marimiari.ui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import ru.itmo.marimiari.service.UserService;
import ru.itmo.marimiari.user.User;

import java.util.Optional;

public class LoginDialog {
    private final UserService userService;
    private User loggedInUser = null;

    public LoginDialog(UserService userService) {
        this.userService = userService;
    }

    public boolean showAndWait(){
        Stage stage = new Stage();
        stage.setTitle("Welcome");

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
            Optional<User> userOpt = userService.login(login, password);
            if (userOpt.isPresent()) {
                loggedInUser = userOpt.get();
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
            if (userService.register(login, password)) {
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

    public User getLoggedInUser(){
        return loggedInUser; //возвращает логин вошедшего пользователя
    }

    private void showAlert(String msg){
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.showAndWait();
    }
}
