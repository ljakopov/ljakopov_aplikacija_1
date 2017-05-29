/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.ljakopov.pomoc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.foi.nwtis.ljakopov.konfiguracije.Konfiguracija;
import org.foi.nwtis.ljakopov.ws.serveri.MeteoServiceWS;

/**
 *
 * @author ljakopov
 */
public class BazaPodataka {
    
    public static Konfiguracija konf;
    
    public static Connection konekcijaNaBazu(Connection c) {
        String db_driver = konf.dajPostavku("driver.database.mysql");
        String db_Username = konf.dajPostavku("user.username");
        String db_Password = konf.dajPostavku("user.password");
        String db_Host = konf.dajPostavku("server.database");
        String db_name = konf.dajPostavku("user.database");

        try {
            Class.forName(db_driver);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MeteoServiceWS.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            if (c == null) {
                System.out.println("PONOVNA KONEKCIJA");
                c = DriverManager.getConnection(db_Host + db_name,
                        db_Username,
                        db_Password);
            }
        } catch (SQLException ex) {
            Logger.getLogger(MeteoServiceWS.class.getName()).log(Level.SEVERE, null, ex);
        }
        return c;
    }
    
}
