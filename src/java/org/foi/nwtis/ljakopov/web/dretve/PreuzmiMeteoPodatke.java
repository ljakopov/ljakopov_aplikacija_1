/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.ljakopov.web.dretve;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import org.foi.nwtis.ljakopov.konfiguracije.Konfiguracija;
import org.foi.nwtis.ljakopov.rest.klijenti.GMKlijent;
import org.foi.nwtis.ljakopov.rest.klijenti.OWMKlijent;
import org.foi.nwtis.ljakopov.web.podaci.MeteoPodaci;

/**
 *
 * @author ljakopov
 */
public class PreuzmiMeteoPodatke extends Thread {

    public static boolean prekid_obrade = false;
    private ServletContext sc = null;
    Connection c = null;

    @Override
    public void interrupt() {
        prekid_obrade = true;
        super.interrupt();
    }

    private void dbDriver(String db_driver) {
        try {
            Class.forName(db_driver);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(PreuzmiMeteoPodatke.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * spajanje na bazu podataka prema podacima iz konfiguracije
     */
    private Connection spojiSeNaBazu(String db_driver, String db_Host, String db_name, String db_Username, String db_Password) {
        dbDriver(db_driver);
        try {
            if (c == null) {
                System.out.println("PONOVNA KONEKCIJA");
                c = DriverManager.getConnection(db_Host + db_name,
                        db_Username,
                        db_Password);
            }
        } catch (SQLException ex) {
            Logger.getLogger(PreuzmiMeteoPodatke.class.getName()).log(Level.SEVERE, null, ex);
        }
        return c;
    }

    private ResultSet grupirajUredjaje() {
        String ispisSveIzTablice = "SELECT latitude, longitude FROM uredaji GROUP BY latitude, longitude";
        ResultSet resultSet = null;

        PreparedStatement ps;
        try {
            ps = c.prepareStatement(ispisSveIzTablice);
            resultSet = ps.executeQuery();
        } catch (SQLException ex) {
            Logger.getLogger(PreuzmiMeteoPodatke.class.getName()).log(Level.SEVERE, null, ex);
        }
        return resultSet;
    }

    private ResultSet dajId(float lat, float lon) {
        String ispisSveIzTablice = "select id from uredaji where (latitude between ? AND ?) and (longitude between ? AND ?)";
        ResultSet resultSet = null;

        PreparedStatement ps;
        try {
            ps = c.prepareStatement(ispisSveIzTablice);
            ps.setFloat(1, (float) (lat - 0.0001));
            ps.setFloat(2, (float) (lat + 0.0001));
            ps.setFloat(3, (float) (lon - 0.0001));
            ps.setFloat(4, (float) (lon + 0.0001));
            resultSet = ps.executeQuery();
        } catch (SQLException ex) {
            Logger.getLogger(PreuzmiMeteoPodatke.class.getName()).log(Level.SEVERE, null, ex);
        }
        return resultSet;
    }

    @Override
    public void run() {
        Konfiguracija konf = (Konfiguracija) sc.getAttribute("Mail_Konfig");
        String db_Username = konf.dajPostavku("user.username");
        String db_Password = konf.dajPostavku("user.password");
        String db_Host = konf.dajPostavku("server.database");
        String db_name = konf.dajPostavku("user.database");
        String db_driver = konf.dajPostavku("driver.database.mysql");
        String apikey = konf.dajPostavku("apikey");
        int trajanjeCiklusa = Integer.parseInt(konf.dajPostavku("intervalDretveZaMeteoPodatke"));

        while (!prekid_obrade) {
            long pocetak = System.currentTimeMillis();
            int brojac = 0;
            if (!ObradaZahtjeva.pause == true) {
                c = spojiSeNaBazu(db_driver, db_Host, db_name, db_Username, db_Password);

                OWMKlijent owmk = new OWMKlijent(apikey);

                /**
                 * Dohvaćanje svih uređaja iz baze podataa uz pomoć metode
                 * sviUredjaji() i nakon toga njihovo spremanje u bazu podataka
                 */
                String upisiUTablicuMeteo = "INSERT INTO meteo (id, adresaStanice, latitude, longitude, vrijeme, vrijemeOpis, temp, tempMin, tempMax, vlaga, tlak, vjetar,"
                        + "vjetarSmjer, preuzeto) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,default)";
                PreparedStatement upisiInsert;
                try {
                    ResultSet resultSet = grupirajUredjaje();
                    while (resultSet.next()) {
                        System.out.println("PROBA: " + resultSet.getString(1) + " " + resultSet.getString(2));
                        MeteoPodaci mp = owmk.getRealTimeWeather(resultSet.getString(1), resultSet.getString(2));
                        GMKlijent gMKlijent = new GMKlijent();
                        String adresa = gMKlijent.getGeoLocationFromLatLot(resultSet.getString(1), resultSet.getString(2));
                        ResultSet resultSetUredjajiId = dajId(Float.parseFloat(resultSet.getString(1)), Float.parseFloat(resultSet.getString(2)));
                        while (resultSetUredjajiId.next()) {
                            brojac++;
                            upisiInsert = c.prepareStatement(upisiUTablicuMeteo);
                            upisiInsert.setInt(1, resultSetUredjajiId.getInt(1));
                            upisiInsert.setString(2, adresa);
                            upisiInsert.setString(3, resultSet.getString(1));
                            upisiInsert.setString(4, resultSet.getString(2));
                            upisiInsert.setString(5, "");
                            upisiInsert.setString(6, "");
                            upisiInsert.setFloat(7, mp.getTemperatureValue());
                            upisiInsert.setFloat(8, mp.getTemperatureMin());
                            upisiInsert.setFloat(9, mp.getTemperatureMax());
                            upisiInsert.setFloat(10, mp.getHumidityValue());
                            upisiInsert.setFloat(11, mp.getPressureValue());
                            upisiInsert.setFloat(12, mp.getWindSpeedValue());
                            upisiInsert.setFloat(13, mp.getWindDirectionValue());
                            upisiInsert.executeUpdate();

                            System.out.println("OVO JE ID: " + resultSet.getString(1));
                            System.out.println("OVO JE Naziv: " + resultSet.getString(2));
                            System.out.println("BROJAC: " + brojac);

                        }
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(PreuzmiMeteoPodatke.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                ObradaZahtjeva.pause = false;
                System.out.println("POSTAJE FALSE");
            }
            long kraj = System.currentTimeMillis();

            try {
                sleep((trajanjeCiklusa * 1000) - (kraj - pocetak));
            } catch (InterruptedException ex) {
                Logger.getLogger(PreuzmiMeteoPodatke.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public synchronized void start() {
        super.start(); //To change body of generated methods, choose Tools | Templates.
    }

    public void setSc(ServletContext sc) {
        this.sc = sc;
    }
}
