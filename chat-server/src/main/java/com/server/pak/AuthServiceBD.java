package com.server.pak;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuthServiceBD implements AuthService {

    private static final Logger LOGGER = LogManager.getLogger(AuthServiceBD.class);
    private List<User> listUser;
    private static Connection connection;
    private static Statement stmt;

    private class User {
        private String name;
        private String login;
        private String pass;

        public User(String name, String login, String pass) {
            this.name = name;
            this.login = login;
            this.pass = pass;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    AuthServiceBD() {
        listUser = new ArrayList<>();
        try {
            start();
            loadUsers();
            LOGGER.info("Загрузили пользователей из БД AuthServiceBD");
        } catch (SQLException e) {
            LOGGER.throwing(Level.ERROR, e);
        } catch (Exception e) {
            LOGGER.throwing(Level.FATAL, e);
        }
    }

    public boolean registerNewUser(String nickName, String login, String pass) {
        int result = 0;
        try {
            // регистрируем нового пользователя в БД
            result = stmt.executeUpdate("INSERT INTO users (NickName, login, pass) VALUES ('" + nickName + "','" + login + "','" + pass + "');");
            // регистрируем нового пользователя в листе AuthServiceBD
            listUser.add(new User(nickName, login, pass));
        } catch (SQLException e) {
            LOGGER.throwing(Level.ERROR, e);
        }
        return result > 0;
    }

    public boolean updateNickName(String newName, String oldName) {
        int result = 0;
        try {
            result = stmt.executeUpdate("UPDATE users SET NickName = '" + newName + "' WHERE NickName = '" + oldName + "';");
            // если операция в бд свершилась то меняем в памяти сервера
            if(result > 0){
                for (int i = 0; i < listUser.size(); i++) {
                    if(listUser.get(i).getName().equals(oldName)){
                        listUser.get(i).setName(newName);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Ошибка в смене имени пользователя");
        }
        return result > 0;
    }

    public void loadUsers() throws SQLException {
        try (ResultSet rs = stmt.executeQuery("SELECT * FROM users;")) {
            while (rs.next()) {
                listUser.add(new User(
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4)
                ));
            }
        }
    }

    @Override
    public void start() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:userschat.db");
            stmt = connection.createStatement();
        } catch (SQLException e) {
            LOGGER.throwing(Level.ERROR, e);
            throw new RuntimeException("Не возможно подключиться к БД.");
        }
    }

    @Override
    public void stop() {
        try {
            if (stmt != null)
                stmt.close();
            if (connection != null)
                connection.close();
        } catch (SQLException e) {
            LOGGER.throwing(Level.ERROR, e);
        }
    }

    @Override
    public String getNickByLoginPass(String login, String pass) {
        for (User user : listUser) {
            if (user.login.equals(login) && user.pass.equals(pass))
                return user.name;
        }
        return null;
    }

    public static Statement getStmt() {
        return stmt;
    }
}
