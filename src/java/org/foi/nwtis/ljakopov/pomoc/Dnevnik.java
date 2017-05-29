/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.ljakopov.pomoc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.foi.nwtis.ljakopov.ws.serveri.MeteoServiceWS;

/**
 *
 * @author ljakopov
 */
public class Dnevnik {

    public static void upisiUDnevnik(Connection c, String korisnik, int trajanje, String metoda, String opis) {
        String spremiDnevnikUBazu = "INSERT INTO dnevnik(`id`, `korisnik`, `url`, `ipadresa`, `vrijeme`, `trajanje`, `status`, `metoda`, `opis`) VALUES "
                + "(default,?,?,?,?,?,?,?,?)";
        PreparedStatement ps;
        try {
            ps = c.prepareStatement(spremiDnevnikUBazu);
            ps.setString(1, korisnik);
            ps.setString(2, "");
            ps.setString(3, "");
            ps.setString(4, new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()));
            ps.setInt(5, trajanje);
            ps.setInt(6, 0);
            ps.setString(7, metoda);
            ps.setString(8, opis);
            ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(MeteoServiceWS.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
