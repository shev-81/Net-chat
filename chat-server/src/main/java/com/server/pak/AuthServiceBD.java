package com.server.pak;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuthServiceBD implements AuthService {
    private List<Users> listUser;
    private static Connection connection;
    private static Statement stmt;

    private class Users {
        private String name;
        private String login;
        private String pass;

        public Users(String name, String login, String pass) {
            this.name = name;
            this.login = login;
            this.pass = pass;
        }
    }
    AuthServiceBD() {
        listUser = new ArrayList<>();
        try {
            start();
            loadUsers();
        }catch (SQLException e) {
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        } finally {
            stop();
        }
    }
    public boolean registerNewUser(String nickName, String login, String pass){
        int result=0;
        start();
        try {
            // регистрируем нового пользователя в БД
            result = stmt.executeUpdate("INSERT INTO users (NickName, login, pass) VALUES ('"+nickName+"','"+login+"','"+pass+"');");
            // регистрируем нового пользователя в листе AuthServiceBD
            listUser.add(new Users(nickName,login,pass));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        stop();
        return result>0;
    }
    public void loadUsers() throws SQLException {
        try (ResultSet rs = stmt.executeQuery("SELECT * FROM users;")) {
            while (rs.next()) {
                listUser.add(new Users(
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
        stmt=connection.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }
    }
    @Override
    public String getNickByLoginPass(String login, String pass) {
        for (AuthServiceBD.Users user : listUser) {
            if (user.login.equals(login) && user.pass.equals(pass))
                return user.name;
        }
        return null;
    }
}
