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
import javax.annotation.Resource;
import javax.faces.context.FacesContext;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.DELETE;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import org.foi.nwtis.ljakopov.pomoc.BazaPodataka;
import org.foi.nwtis.ljakopov.pomoc.Dnevnik;

/**
 * REST Web Service
 *
 * @author User
 */
public class UserRESTResource {

    @Resource
    WebServiceContext wsContext;

    private String korisnickoIme;
    private Connection c;

    /**
     * Creates a new instance of UserRESTResource
     */
    private UserRESTResource(String korisnickoIme) {
        this.korisnickoIme = korisnickoIme;
    }

    private boolean provjeriKorisnickoIme(String korisnickoIme) {
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
            Logger.getLogger(UserRESTResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        return provjera;
    }

    /**
     * Get instance of the UserRESTResource
     */
    public static UserRESTResource getInstance(String korisnickoIme) {
        // The user may use some kind of persistence mechanism
        // to store and restore instances of UserRESTResource class.
        return new UserRESTResource(korisnickoIme);
    }

    /**
     * Retrieves representation of an instance of
     * org.foi.nwtis.ljakopov.rest.serveri.UserRESTResource
     *
     * @return an instance of java.lang.String
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getJson() {
        long pocetak = System.currentTimeMillis();
        Connection connection = BazaPodataka.konekcijaNaBazu(c);

        String ispisSveIzTablice = "SELECT * from korisnik WHERE username=?";

        PreparedStatement ps;
        JsonArrayBuilder jab = Json.createArrayBuilder();
        JsonObjectBuilder job = Json.createObjectBuilder();
        try {
            ps = connection.prepareStatement(ispisSveIzTablice);
            ps.setString(1, korisnickoIme);
            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                job.add("id", resultSet.getInt(1));
                job.add("username", resultSet.getString(2));
                job.add("pass", resultSet.getString(3));
                job.add("prezime", resultSet.getString(4));
                job.add("email", resultSet.getString(5));
                jab.add(job);
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserRESTsResourceContainer.class.getName()).log(Level.SEVERE, null, ex);
        }
        long kraj = System.currentTimeMillis();
        Dnevnik.upisiUDnevnik(connection, korisnickoIme, (int) (kraj - pocetak), "getJson()-userID", "REST-web", "", "");

        return jab.build().toString();
    }

    /**
     * PUT method for updating or creating an instance of UserRESTResource
     *
     * @param content representation for the resource
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public String putJson(String content) {
        long pocetak = System.currentTimeMillis();
        JsonReader reader = Json.createReader(new StringReader(content));
        JsonObject jo = reader.readObject();
        Connection connection = BazaPodataka.konekcijaNaBazu(c);

        String pass = jo.getString("pass");
        String prezime = jo.getString("prezime");
        String email = jo.getString("email");

        System.out.println("pass: " + pass);
        System.out.println("prezime: " + prezime);
        System.out.println("email: " + email);
        if (provjeriKorisnickoIme(korisnickoIme) == true) {
            String azurirajKorisnik = "UPDATE korisnik SET pass = ?, prezime = ?, email = ? WHERE username = ?";
            PreparedStatement ps;
            try {
                ps = connection.prepareStatement(azurirajKorisnik);
                ps.setString(1, pass);
                ps.setString(2, prezime);
                ps.setString(3, email);
                ps.setString(4, korisnickoIme);
                ps.executeUpdate();
            } catch (SQLException ex) {
                Logger.getLogger(UserRESTResource.class.getName()).log(Level.SEVERE, null, ex);
            }
            JsonObjectBuilder job = Json.createObjectBuilder();
            job.add("odgovor", "1");
            long kraj = System.currentTimeMillis();
            Dnevnik.upisiUDnevnik(connection, korisnickoIme, (int) (kraj - pocetak), "putJson()-user", "REST-web", "", "");
            return job.build().toString();
        } else {
            JsonObjectBuilder job = Json.createObjectBuilder();
            job.add("odgovor", "0");
            return job.build().toString();
        }
    }

    /**
     * DELETE method for resource UserRESTResource
     */
    @DELETE
    public void delete() {
    }
}
