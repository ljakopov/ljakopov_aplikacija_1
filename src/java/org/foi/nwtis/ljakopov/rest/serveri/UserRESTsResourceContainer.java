/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.ljakopov.rest.serveri;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.PathParam;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import org.foi.nwtis.ljakopov.pomoc.BazaPodataka;
import org.foi.nwtis.ljakopov.pomoc.Dnevnik;
import org.foi.nwtis.ljakopov.ws.serveri.MeteoServiceWS;

/**
 * REST Web Service
 *
 * @author ljakopov
 */
@Path("/userREST")
public class UserRESTsResourceContainer {

    @Context
    private UriInfo context;
    Connection c;

    /**
     * Creates a new instance of UserRESTsResourceContainer
     */
    public UserRESTsResourceContainer() {
    }

    private boolean provjeriKorisnickoImeILozinku(String korisnickoIme) {
        String provjeraKorisnika = "SELECT username FROM korisnik WHERE username = ?";
        Connection connection = BazaPodataka.konekcijaNaBazu(c);
        boolean provjera = false;

        PreparedStatement ps;
        try {
            ps = connection.prepareStatement(provjeraKorisnika);
            ps.setString(1, korisnickoIme);
            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                provjera = true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserRESTsResourceContainer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return provjera;
    }

    /**
     * Retrieves representation of an instance of
     * org.foi.nwtis.ljakopov.rest.serveri.UserRESTsResourceContainer
     *
     * @return an instance of java.lang.String
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getJson() {
        long pocetak = System.currentTimeMillis();
        Connection connection = BazaPodataka.konekcijaNaBazu(c);

        String ispisSveIzTablice = "SELECT * from korisnik";

        PreparedStatement ps;
        JsonArrayBuilder jab = Json.createArrayBuilder();
        JsonObjectBuilder job = Json.createObjectBuilder();
        try {
            ps = connection.prepareStatement(ispisSveIzTablice);
            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                job.add("id", resultSet.getInt(1));
                job.add("username", resultSet.getString(2));
                job.add("prezime", resultSet.getString(4));
                job.add("email", resultSet.getString(5));
                jab.add(job);
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserRESTsResourceContainer.class.getName()).log(Level.SEVERE, null, ex);
        }

        long kraj = System.currentTimeMillis();
        Dnevnik.upisiUDnevnik(connection, "", (int) (kraj - pocetak), "getJson()-user", "REST-web");
        return jab.build().toString();
    }

    /**
     * POST method for creating an instance of UserRESTResource
     *
     * @param content representation for the new resource
     * @return an HTTP response with content of the created resource
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String postJson(String content) {
        long pocetak = System.currentTimeMillis();
        JsonReader reader = Json.createReader(new StringReader(content));
        JsonObject jo = reader.readObject();

        String username = jo.getString("username");
        String pass = jo.getString("pass");
        String prezime = jo.getString("prezime");
        String email = jo.getString("email");

        System.out.println("username: " + username);
        System.out.println("pass: " + pass);
        System.out.println("prezime: " + prezime);
        System.out.println("email: " + email);
        Connection connection = BazaPodataka.konekcijaNaBazu(c);

        if (provjeriKorisnickoImeILozinku(username) == false) {
            String spremiUredjajUBazu = "INSERT INTO `korisnik`(`id`, `username`, `pass`, `prezime`, `email`) "
                    + "VALUES (default,?,?,?,?)";
            PreparedStatement ps;
            try {
                ps = connection.prepareStatement(spremiUredjajUBazu);
                ps.setString(1, username);
                ps.setString(2, pass);
                ps.setString(3, prezime);
                ps.setString(4, email);
                ps.executeUpdate();
            } catch (SQLException ex) {
                Logger.getLogger(MeteoServiceWS.class.getName()).log(Level.SEVERE, null, ex);
            }
            JsonObjectBuilder job = Json.createObjectBuilder();
            job.add("odgovor", "1");
            long kraj = System.currentTimeMillis();
            Dnevnik.upisiUDnevnik(connection, username, (int) (kraj - pocetak), "postJson()-user", "REST-web");
            return job.build().toString();
        } else {
            JsonObjectBuilder job = Json.createObjectBuilder();
            job.add("odgovor", "0");
            return job.build().toString();
        }
    }

    /**
     * Sub-resource locator method for {korisnickoIme}
     */
    @Path("{korisnickoIme}")
    public UserRESTResource getUserRESTResource(@PathParam("korisnickoIme") String korisnickoIme) {
        return UserRESTResource.getInstance(korisnickoIme);
    }
}
