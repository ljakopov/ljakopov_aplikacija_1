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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.PathParam;
import javax.ws.rs.POST;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
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
@Path("/meteoRESTs")
public class MeteoRESTResourceContainer {

    @Context
    private UriInfo context;

    Connection c;

    /**
     * Creates a new instance of MeteoRESTResourceContainer
     */
    public MeteoRESTResourceContainer() {
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
     * Retrieves representation of an instance of
     * org.foi.nwtis.ljakopov.rest.serveri.MeteoRESTResourceContainer
     *
     * @return an instance of java.lang.String
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getJson() {
        long pocetak = System.currentTimeMillis();
        Connection connection = BazaPodataka.konekcijaNaBazu(c);

        String ispisSveIzTablice = "SELECT * from uredaji";

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
            Logger.getLogger(MeteoRESTResourceContainer.class.getName()).log(Level.SEVERE, null, ex);
        }

        /**
         * kreiranje JSON formata
         */
        JsonArrayBuilder jab = Json.createArrayBuilder();
        for (Uredjaj uredjaj : uredjaji) {
            JsonObjectBuilder job = Json.createObjectBuilder();
            job.add("uid", uredjaj.getId());
            job.add("naziv", uredjaj.getNaziv());
            job.add("lat", uredjaj.getGeoloc().getLatitude());
            job.add("lon", uredjaj.getGeoloc().getLongitude());

            jab.add(job);
        }
        long kraj = System.currentTimeMillis();
        Dnevnik.upisiUDnevnik(connection, "", (int) (kraj - pocetak), "getJson()-meteo", "REST-web", "localhost", "/ljakopov_aplikacija_1/webresources/meteoRESTs");

        return jab.build().toString();
    }

    /**
     * POST method for creating an instance of MeteoRESTResource
     *
     * @param content representation for the new resource
     * @return an HTTP response with content of the created resource
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("text/plain")
    public String postJson(String content) {
        long pocetak = System.currentTimeMillis();
        JsonReader reader = Json.createReader(new StringReader(content));
        JsonObject jo = reader.readObject();

        String id = jo.getString("id");
        String naziv = jo.getString("naziv");
        String adresa = jo.getString("adresa");

        System.out.println("Naziv: " + naziv);
        System.out.println("lat: " + adresa);
        Connection connection = BazaPodataka.konekcijaNaBazu(c);

        if (provjeriId(id) == false) {
            /**
             * dobivanje geo lokacije za poslanu adresu
             */
            GMKlijent gmk = new GMKlijent();
            Lokacija l = gmk.getGeoLocation(adresa);

            try {
                String sqlUnesiUredjaj = "INSERT INTO uredaji (id, naziv, latitude, longitude, status, vrijeme_promjene, vrijeme_kreiranja) VALUES(?, ?, ?, ?, 0, default, default)";
                PreparedStatement unesiUredjaj = connection.prepareStatement(sqlUnesiUredjaj);
                unesiUredjaj.setString(1, id);
                unesiUredjaj.setString(2, naziv);
                unesiUredjaj.setString(3, l.getLatitude());
                unesiUredjaj.setString(4, l.getLongitude());
                unesiUredjaj.executeUpdate();
            } catch (SQLException ex) {
                Logger.getLogger(MeteoRESTResourceContainer.class.getName()).log(Level.SEVERE, null, ex);
            }
            long kraj = System.currentTimeMillis();
            Dnevnik.upisiUDnevnik(connection, "", (int) (kraj - pocetak), "postJson()-meteo", "REST-web", "localhost", "/ljakopov_aplikacija_1/webresources/meteoREST");
            return "1";
        } else {
            long kraj = System.currentTimeMillis();
            Dnevnik.upisiUDnevnik(connection, "", (int) (kraj - pocetak), "postJson()-meteo-krivo", "REST-web", "localhost", "/ljakopov_aplikacija_1/webresources/meteoREST");
            return "0";
        }
    }

    /**
     * Sub-resource locator method for {id}
     */
    @Path("{id}")
    public MeteoRESTResource getMeteoRESTResource(@PathParam("id") String id) {
        return MeteoRESTResource.getInstance(id);
    }
}
