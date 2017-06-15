/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.ljakopov.web.slusaci;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.foi.nwtis.ljakopov.konfiguracije.Konfiguracija;
import org.foi.nwtis.ljakopov.konfiguracije.KonfiguracijaApstraktna;
import org.foi.nwtis.ljakopov.konfiguracije.NeispravnaKonfiguracija;
import org.foi.nwtis.ljakopov.konfiguracije.NemaKonfiguracije;
import org.foi.nwtis.ljakopov.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.ljakopov.pomoc.BazaPodataka;
import org.foi.nwtis.ljakopov.web.dretve.PreuzmiMeteoPodatke;
import org.foi.nwtis.ljakopov.web.dretve.ServerSustava;
import org.foi.nwtis.ljakopov.ws.serveri.MeteoServiceWS;

/**
 * Web application lifecycle listener.
 *
 * @author ljakopov
 */
public class SlusacAplikacije implements ServletContextListener {

    private ServletContext context = null;
    private PreuzmiMeteoPodatke pmp = null;
    private ServerSustava ss = null;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        context = sce.getServletContext();
        String datoteka = context.getRealPath("/WEB-INF")
                + File.separator
                + context.getInitParameter("konfiguracija");

        BP_Konfiguracija bp_konf = new BP_Konfiguracija(datoteka);
        context.setAttribute("BP_Konfig", bp_konf);
        System.out.println("Učitana konfiguacija");

        Konfiguracija konf = null;
        try {
            konf = KonfiguracijaApstraktna.preuzmiKonfiguraciju(datoteka);
            MeteoServiceWS.konf = konf;
            BazaPodataka.konf = konf;
            context.setAttribute("Mail_Konfig", konf);
        } catch (NemaKonfiguracije | NeispravnaKonfiguracija ex) {
            Logger.getLogger(SlusacAplikacije.class.getName()).log(Level.SEVERE, null, ex);
        }
        pokreniServer();
        //pokreniPreuzimanjePodataka();
    }

    private void pokreniServer() {
        ss = new ServerSustava();
        ss.setSc(context);
        ss.start();
    }

    private void pokreniPreuzimanjePodataka() {
        pmp = new PreuzmiMeteoPodatke();
        pmp.setSc(context);
        pmp.start();

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (pmp != null && pmp.isAlive()) {
            pmp.interrupt();
        }
        zaustaviServerSocket();
    }

    private void zaustaviServerSocket() {
        if (ss != null && ss.isAlive()) {
            ss.interrupt();
        }
    }

    public ServletContext getContext() {
        return context;
    }
}
