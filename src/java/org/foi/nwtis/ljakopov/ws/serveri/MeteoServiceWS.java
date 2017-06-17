/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.ljakopov.ws.serveri;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import org.foi.nwtis.ljakopov.konfiguracije.Konfiguracija;
import org.foi.nwtis.ljakopov.rest.klijenti.GMKlijent;
import org.foi.nwtis.ljakopov.rest.klijenti.OWMKlijent;
import org.foi.nwtis.ljakopov.web.podaci.MeteoPodaci;

/**
 *
 * @author ljakopov
 */
@WebService(serviceName = "MeteoServiceWS")
public class MeteoServiceWS {

    public static Konfiguracija konf;
    Connection c;

    @Resource
    WebServiceContext wsContext;

    private Connection konekcijaNaBazu() {
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

    private boolean provjeriKorisnickoImeILozinku(String korisnickoIme, String lozinka) {
        String provjeraKorisnika = "SELECT username,pass FROM korisnik WHERE username = ? and pass = ?";
        Connection c = konekcijaNaBazu();
        boolean provjera = false;

        PreparedStatement ps;
        try {
            ps = c.prepareStatement(provjeraKorisnika);
            ps.setString(1, korisnickoIme);
            ps.setString(2, lozinka);
            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                provjera = true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(MeteoServiceWS.class.getName()).log(Level.SEVERE, null, ex);
        }
        return provjera;
    }

    private void upisiUDnevnik(String korisnik, int trajanje, String metoda, String opis) {
        MessageContext mc = wsContext.getMessageContext();
        HttpServletRequest req = (HttpServletRequest) mc.get(MessageContext.SERVLET_REQUEST);
        String ipAdresa = req.getRemoteAddr();
        String urlAdresa = req.getRequestURI();
        c = konekcijaNaBazu();
        String spremiDnevnikUBazu = "INSERT INTO dnevnik(`id`, `korisnik`, `url`, `ipadresa`, `vrijeme`, `trajanje`, `status`, `metoda`, `opis`) VALUES "
                + "(default,?,?,?,?,?,?,?,?)";
        PreparedStatement ps;
        try {
            ps = c.prepareStatement(spremiDnevnikUBazu);
            ps.setString(1, korisnik);
            ps.setString(2, urlAdresa);
            ps.setString(3, ipAdresa);
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

    /**
     * Web service operation
     */
    @WebMethod(operationName = "dajZadnjiMeteoPodatakZaUredjaj")
    public MeteoPodaci dajZadnjiMeteoPodatakZaUredjaj(@WebParam(name = "username") String username, @WebParam(name = "pass") String pass, @WebParam(name = "id") int id) {
        MeteoPodaci meteoPodaci = null;
        long pocetak = System.currentTimeMillis();
        if (provjeriKorisnickoImeILozinku(username, pass) == true) {

            String vracajZadnjiId = "SELECT * FROM METEO WHERE ID=? ORDER BY IDMETEO DESC LIMIT 1";

            PreparedStatement ps;
            try {
                ps = c.prepareStatement(vracajZadnjiId);
                ps.setInt(1, id);
                ResultSet resultSet = ps.executeQuery();
                while (resultSet.next()) {
                    meteoPodaci = new MeteoPodaci(new Date(), new Date(), resultSet.getFloat(8), resultSet.getFloat(9), resultSet.getFloat(10), "C", resultSet.getFloat(11), "%", resultSet.getFloat(12), "hPa", resultSet.getFloat(13), "", resultSet.getFloat(14), "", "", 1, "", "OK", 0.0f, "", "", 7, "", "", resultSet.getDate(15));
                }
            } catch (SQLException ex) {
                Logger.getLogger(MeteoServiceWS.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        long kraj = System.currentTimeMillis();
        upisiUDnevnik(username, (int) (kraj - pocetak), "dajZadnjiMeteoPodatakZaUredjaj()", "SOAP-web");
        return meteoPodaci;
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "dajPosljednihNMeteoPodatakaZaUredjaj")
    public java.util.List<MeteoPodaci> dajPosljednihNMeteoPodatakaZaUredjaj(@WebParam(name = "username") String username, @WebParam(name = "pass") String pass, @WebParam(name = "id") int id, @WebParam(name = "n") int n) {
        List<MeteoPodaci> listaMeteroloskiPodataka = new ArrayList<>();
        long pocetak = System.currentTimeMillis();
        if (provjeriKorisnickoImeILozinku(username, pass) == true) {

            String vracajZadnjeNPodatke = "SELECT * FROM METEO WHERE ID=? ORDER BY IDMETEO DESC LIMIT ?";
            PreparedStatement ps;
            try {
                ps = c.prepareStatement(vracajZadnjeNPodatke);
                ps.setInt(1, id);
                ps.setInt(2, n);
                ResultSet resultSet = ps.executeQuery();
                while (resultSet.next()) {
                    listaMeteroloskiPodataka.add(new MeteoPodaci(new Date(), new Date(), resultSet.getFloat(8), resultSet.getFloat(9), resultSet.getFloat(10), "C", resultSet.getFloat(11), "%", resultSet.getFloat(12), "hPa", resultSet.getFloat(13), "", resultSet.getFloat(14), "", "", 1, "", "OK", 0.0f, "", "", 7, "", "", resultSet.getDate(15)));
                }
            } catch (SQLException ex) {
                Logger.getLogger(MeteoServiceWS.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        long kraj = System.currentTimeMillis();
        upisiUDnevnik(username, (int) (kraj - pocetak), "dajPosljednihNMeteoPodatakaZaUredjaj()", "SOAP-web");
        return listaMeteroloskiPodataka;
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "dajMeteoPodatkeZaUredjajOdDoDatum")
    public java.util.List<MeteoPodaci> dajMeteoPodatkeZaUredjajOdDoDatum(@WebParam(name = "username") String username, @WebParam(name = "pass") String pass, @WebParam(name = "id") int id, @WebParam(name = "from") long from, @WebParam(name = "to") long to) {
        List<MeteoPodaci> listaMeteroloskiPodataka = new ArrayList<>();
        long pocetak = System.currentTimeMillis();
        if (provjeriKorisnickoImeILozinku(username, pass) == true) {
            Date pocetakTime = new Date(from * 1000);
            Date krajTime = new Date(to * 1000);
            Timestamp vrijeme_od = new Timestamp(pocetakTime.getTime());
            Timestamp vrijeme_do = new Timestamp(krajTime.getTime());

            String vracajZadnjeNPodatke = "SELECT * FROM METEO WHERE id=? and preuzeto BETWEEN ? and ?";

            PreparedStatement ps;
            try {
                ps = c.prepareStatement(vracajZadnjeNPodatke);
                ps.setInt(1, id);
                ps.setTimestamp(2, vrijeme_od);
                ps.setTimestamp(3, vrijeme_do);
                ResultSet resultSet = ps.executeQuery();
                while (resultSet.next()) {
                    listaMeteroloskiPodataka.add(new MeteoPodaci(new Date(), new Date(), resultSet.getFloat(8), resultSet.getFloat(9), resultSet.getFloat(10), "C", resultSet.getFloat(11), "%", resultSet.getFloat(12), "hPa", resultSet.getFloat(13), "", resultSet.getFloat(14), "", "", 1, "", "OK", 0.0f, "", "", 7, "", "", resultSet.getDate(15)));
                }
            } catch (SQLException ex) {
                Logger.getLogger(MeteoServiceWS.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        long kraj = System.currentTimeMillis();
        upisiUDnevnik(username, (int) (kraj - pocetak), "dajMeteoPodatkeZaUredjajOdDoDatum()", "SOAP-web");
        return listaMeteroloskiPodataka;
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "dajMeteoPodatkeZaUredjaj")
    public MeteoPodaci dajMeteoPodatkeZaUredjaj(@WebParam(name = "username") String username, @WebParam(name = "pass") String pass, @WebParam(name = "id") int id) {
        MeteoPodaci meteoPodaci = null;
        long pocetak = System.currentTimeMillis();
        if (provjeriKorisnickoImeILozinku(username, pass) == true) {
            String api = konf.dajPostavku("apikey");
            OWMKlijent owmk = new OWMKlijent(api);
            String vracajvazeceMeteroloskePodatkeZaUredjaj = "SELECT * FROM uredaji WHERE ID=?";

            PreparedStatement ps;
            try {
                ps = c.prepareStatement(vracajvazeceMeteroloskePodatkeZaUredjaj);
                ps.setInt(1, id);
                ResultSet resultSet = ps.executeQuery();
                while (resultSet.next()) {
                    meteoPodaci = owmk.getRealTimeWeather(resultSet.getString(3), resultSet.getString(4));
                }
            } catch (SQLException ex) {
                Logger.getLogger(MeteoServiceWS.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        long kraj = System.currentTimeMillis();
        upisiUDnevnik(username, (int) (kraj - pocetak), "dajMeteoPodatkeZaUredjaj()", "SOAP-web");
        return meteoPodaci;
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "dajAdresuNaTemeljuLatLon")
    public String dajAdresuNaTemeljuLatLon(@WebParam(name = "username") String username, @WebParam(name = "pass") String pass, @WebParam(name = "lat") String lat, @WebParam(name = "lon") String lon) {
        String mjesto = null;
        long pocetak = System.currentTimeMillis();
        if (provjeriKorisnickoImeILozinku(username, pass) == true) {
            GMKlijent gmk = new GMKlijent();
            mjesto = gmk.getGeoLocationFromLatLot(lat, lon);            
        }
        long kraj = System.currentTimeMillis();
        upisiUDnevnik(username, (int) (kraj - pocetak), "dajAdresuNaTemeljuLatLon()", "SOAP-web");
        return mjesto;
    }
}
