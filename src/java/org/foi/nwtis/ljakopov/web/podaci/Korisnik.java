/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.ljakopov.web.podaci;

/**
 *
 * @author ljakopov
 */
public class Korisnik {
    
    public String prezime;
    public String id;
    public String username;
    public String email;

    public Korisnik(String prezime, String id, String username, String email) {
        this.prezime = prezime;
        this.id = id;
        this.username = username;
        this.email = email;
    } 

    public String getPrezime() {
        return prezime;
    }

    public void setPrezime(String prezime) {
        this.prezime = prezime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    
    
    
}
