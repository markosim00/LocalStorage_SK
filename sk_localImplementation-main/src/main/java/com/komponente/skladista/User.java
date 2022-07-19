package com.komponente.skladista;

public class User extends Korisnik{

    public User(String username, String password, Korisnici tip) {
        super(username, password, tip);
    }

    public User(String username, String password){
        super(username,password);
    }

    public User(){
        super();
    }
}
