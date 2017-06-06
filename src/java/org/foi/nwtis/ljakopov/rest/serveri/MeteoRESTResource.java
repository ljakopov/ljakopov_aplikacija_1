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
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.DELETE;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.foi.nwtis.ljakopov.pomoc.BazaPodataka;
import org.foi.nwtis.ljakopov.pomoc.Dnevnik;
import org.foi.nwtis.ljakopov.rest.klijenti.GMKlijent;
import org.foi.nwtis.ljakopov.web.podaci.Lokacija;
import org.foi.nwtis.ljakopov.web.podaci.Uredjaj;

/**
 * REST Web Service
 *
 * @author ljakopov
 */
public class MeteoRESTResource {

    @Context
    private UriInfo context;

    private String id;
    Connection c;

    /**
     * Creates a new instance of MeteoRESTResource
     */
    private MeteoRESTResource(String id) {
        this.id = id;
    }

    private boolean provjeriId(String id) {
        String provjeraUredjaja = "SELECT * FROM uredaji WHERE id = ?";
        Connection connection = BazaPodataka.konekcijaNaBazu(c);
        boolean provjera = false;

        PreparedStatement ps;
        try {
            ps = connection.prepareStatement(provjeraUredjaja);
            ps.setString(1, id);
            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                provjera = true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(MeteoRESTResourceContainer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return provjera;
    }

    /**
     * Get instance of the MeteoRESTResource
     */
    public static MeteoRESTResource getInstance(String id) {
        // The user may use some kind of persistence mechanism
        // to store and restore instances of MeteoRESTResource class.
        return new MeteoRESTResource(id);
    }

    /**
     * Retrieves representation of an instance of
     * org.foi.nwtis.ljakopov.rest.serveri.MeteoRESTResource
     *
     * @return an instance of java.lang.String
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getJson() {
        long pocetak = System.currentTimeMillis();
        Connection connection = BazaPodataka.konekcijaNaBazu(c);
        String ispisSveIzTablice = "SELECT * from uredaji WHERE ID=" + this.id;

        /**
         * ispis podataka za lokaciju za određeni ID iz baze podataka
         */
        PreparedStatement ps;
        ArrayList<Uredjaj> uredjaji = new ArrayList<>();
        try {
            ps = connection.prepareStatement(ispisSveIzTablice);
            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                Lokacija loc = new Lokacija(resultSet.getString(3), resultSet.getString(4));
                uredjaji.add(new Uredjaj(resultSet.getInt(1), resultSet.getString(2), loc));
            }
        } catch (SQLException ex) {
            Logger.getLogger(MeteoRESTResource.class.getName()).log(Level.SEVERE, null, ex);
        }

        /**
         * dohvaćanje trenutačnih podatka vremena na temelju lat i lot na i
         * njuhovo spremanje u JSON format, ako ne postoji ID vraća se String
         * "Nepostojeći ID"
         */
        for (Uredjaj uredjaj : uredjaji) {
            if (uredjaj.getId() == Integer.parseInt(this.id)) {
                JsonArrayBuilder jab = Json.createArrayBuilder();
                JsonObjectBuilder job = Json.createObjectBuilder();
                job.add("uid", uredjaj.getId());
                job.add("naziv", uredjaj.getNaziv());
                job.add("lat", uredjaj.getGeoloc().getLatitude());
                job.add("lon", uredjaj.getGeoloc().getLongitude());
                jab.add(job);
                long kraj = System.currentTimeMillis();
                Dnevnik.upisiUDnevnik(connection, "", (int) (kraj - pocetak), "getJson()-meteo", "REST-web", "", "");
                return jab.build().toString();
            }
        }
        return "Nepostojeci ID";
    }

    /**
     * PUT method for updating or creating an instance of MeteoRESTResource
     *
     * @param content representation for the resource
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public String putJson(String content) {
        long pocetak = System.currentTimeMillis();
        JsonReader reader = Json.createReader(new StringReader(content));
        JsonObject jo = reader.readObject();

        String naziv = jo.getString("naziv");
        String adresa = jo.getString("adresa");

        Connection connection = BazaPodataka.konekcijaNaBazu(c);
        if (provjeriId(id) == true) {
            GMKlijent gmk = new GMKlijent();
            Lokacija l = gmk.getGeoLocation(adresa);
            try {
                String sqlAzurirajUredjaj = "UPDATE uredaji set naziv = ?, latitude = ?, longitude = ?, vrijeme_promjene = NOW() WHERE id = ?";
                PreparedStatement unesiUredjaj = connection.prepareStatement(sqlAzurirajUredjaj);
                unesiUredjaj.setString(1, naziv);
                unesiUredjaj.setString(2, l.getLatitude());
                unesiUredjaj.setString(3, l.getLongitude());
                unesiUredjaj.setString(4, id);
                unesiUredjaj.executeUpdate();
            } catch (SQLException ex) {
                Logger.getLogger(MeteoRESTResourceContainer.class.getName()).log(Level.SEVERE, null, ex);
            }
            JsonObjectBuilder job = Json.createObjectBuilder();
            job.add("odgovor", "1");
            long kraj = System.currentTimeMillis();
            Dnevnik.upisiUDnevnik(connection, "", (int) (kraj - pocetak), "putJson()-meteo", "REST-web", "", "");
            return job.build().toString();
        } else {
            JsonObjectBuilder job = Json.createObjectBuilder();
            job.add("odgovor", "0");
            return job.build().toString();
        }
    }

    /**
     * DELETE method for resource MeteoRESTResource
     */
    @DELETE
    public void delete() {
    }
}
