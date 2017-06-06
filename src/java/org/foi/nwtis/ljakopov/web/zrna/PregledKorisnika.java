/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.ljakopov.web.zrna;

import java.io.Serializable;
import javax.inject.Named;
import javax.faces.bean.SessionScoped;

/**
 *
 * @author Lovro
 */
@Named(value = "pregledKorisnika")
@SessionScoped
public class PregledKorisnika implements Serializable {

    private String napisi = "PKIKK=KK=K";

    /**
     * Creates a new instance of PregledKorisnika
     */
    public PregledKorisnika() {
        ispis();
    }

    public String getNapisi() {
        return napisi;
    }

    public void setNapisi(String napisi) {
        this.napisi = napisi;
    }

    public void ispis() {
        System.out.println(napisi);
    }

}
