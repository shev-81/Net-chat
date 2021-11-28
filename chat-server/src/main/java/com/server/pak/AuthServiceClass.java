package com.server.pak;

import java.util.ArrayList;
import java.util.List;

public class AuthServiceClass implements AuthService{
    private List<Users> listUser;
    private class Users{
        private String name;
        private String login;
        private String pass;
        public Users(String name, String login, String pass) {
            this.name = name;
            this.login = login;
            this.pass = pass;
        }
    }
    AuthServiceClass(){
        listUser = new ArrayList<>();
        listUser.add(new Users("Shev","shev","shev"));
        listUser.add(new Users("Anna","anna","anna"));
        listUser.add(new Users("Ulya","ulya","ulya"));
    }
    @Override
    public void start() {
        System.out.println("Start autorization");
    }
    @Override
    public void stop() {
        System.out.println("Stop autorization");
    }
    @Override
    public String getNickByLoginPass(String login, String pass) {
        for(Users user: listUser){
            if(user.login.equals(login) && user.pass.equals(pass)) return user.name;
        }
        return null;
    }
}
