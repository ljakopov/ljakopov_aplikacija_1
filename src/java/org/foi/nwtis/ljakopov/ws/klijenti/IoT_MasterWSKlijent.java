/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.ljakopov.ws.klijenti;

import org.foi.nwtis.dkermek.ws.serveri.StatusKorisnika;
import org.foi.nwtis.dkermek.ws.serveri.StatusUredjaja;

/**
 *
 * @author ljakopov
 */
public class IoT_MasterWSKlijent {

    public static Boolean registrirajGrupuIoT(java.lang.String korisnickoIme, java.lang.String korisnickaLozinka) {
        org.foi.nwtis.dkermek.ws.serveri.IoTMaster_Service service = new org.foi.nwtis.dkermek.ws.serveri.IoTMaster_Service();
        org.foi.nwtis.dkermek.ws.serveri.IoTMaster port = service.getIoTMasterPort();
        return port.registrirajGrupuIoT(korisnickoIme, korisnickaLozinka);
    }

    public static Boolean deregistrirajGrupuIoT(java.lang.String korisnickoIme, java.lang.String korisnickaLozinka) {
        org.foi.nwtis.dkermek.ws.serveri.IoTMaster_Service service = new org.foi.nwtis.dkermek.ws.serveri.IoTMaster_Service();
        org.foi.nwtis.dkermek.ws.serveri.IoTMaster port = service.getIoTMasterPort();
        return port.deregistrirajGrupuIoT(korisnickoIme, korisnickaLozinka);
    }

    public static Boolean aktivirajGrupuIoT(java.lang.String korisnickoIme, java.lang.String korisnickaLozinka) {
        org.foi.nwtis.dkermek.ws.serveri.IoTMaster_Service service = new org.foi.nwtis.dkermek.ws.serveri.IoTMaster_Service();
        org.foi.nwtis.dkermek.ws.serveri.IoTMaster port = service.getIoTMasterPort();
        return port.aktivirajGrupuIoT(korisnickoIme, korisnickaLozinka);
    }

    public static Boolean blokirajGrupuIoT(java.lang.String korisnickoIme, java.lang.String korisnickaLozinka) {
        org.foi.nwtis.dkermek.ws.serveri.IoTMaster_Service service = new org.foi.nwtis.dkermek.ws.serveri.IoTMaster_Service();
        org.foi.nwtis.dkermek.ws.serveri.IoTMaster port = service.getIoTMasterPort();
        return port.blokirajGrupuIoT(korisnickoIme, korisnickaLozinka);
    }

    public static boolean ucitajSveUredjajeGrupe(java.lang.String korisnickoIme, java.lang.String korisnickaLozinka) {
        org.foi.nwtis.dkermek.ws.serveri.IoTMaster_Service service = new org.foi.nwtis.dkermek.ws.serveri.IoTMaster_Service();
        org.foi.nwtis.dkermek.ws.serveri.IoTMaster port = service.getIoTMasterPort();
        return port.ucitajSveUredjajeGrupe(korisnickoIme, korisnickaLozinka);
    }

    public static Boolean obrisiSveUredjajeGrupe(java.lang.String korisnickoIme, java.lang.String korisnickaLozinka) {
        org.foi.nwtis.dkermek.ws.serveri.IoTMaster_Service service = new org.foi.nwtis.dkermek.ws.serveri.IoTMaster_Service();
        org.foi.nwtis.dkermek.ws.serveri.IoTMaster port = service.getIoTMasterPort();
        return port.obrisiSveUredjajeGrupe(korisnickoIme, korisnickaLozinka);
    }

    public static StatusKorisnika dajStatusGrupeIoT(java.lang.String korisnickoIme, java.lang.String korisnickaLozinka) {
        org.foi.nwtis.dkermek.ws.serveri.IoTMaster_Service service = new org.foi.nwtis.dkermek.ws.serveri.IoTMaster_Service();
        org.foi.nwtis.dkermek.ws.serveri.IoTMaster port = service.getIoTMasterPort();
        return port.dajStatusGrupeIoT(korisnickoIme, korisnickaLozinka);
    }

    public static Boolean dodajNoviUredjajGrupi(java.lang.String korisnickoIme, java.lang.String korisnickaLozinka, int idUredjaj, java.lang.String nazivUredjaj, java.lang.String adresaUredjaj) {
        org.foi.nwtis.dkermek.ws.serveri.IoTMaster_Service service = new org.foi.nwtis.dkermek.ws.serveri.IoTMaster_Service();
        org.foi.nwtis.dkermek.ws.serveri.IoTMaster port = service.getIoTMasterPort();
        return port.dodajNoviUredjajGrupi(korisnickoIme, korisnickaLozinka, idUredjaj, nazivUredjaj, adresaUredjaj);
    }

    public static boolean aktivirajUredjajGrupe(java.lang.String korisnickoIme, java.lang.String korisnickaLozinka, int idUredjaj) {
        org.foi.nwtis.dkermek.ws.serveri.IoTMaster_Service service = new org.foi.nwtis.dkermek.ws.serveri.IoTMaster_Service();
        org.foi.nwtis.dkermek.ws.serveri.IoTMaster port = service.getIoTMasterPort();
        return port.aktivirajUredjajGrupe(korisnickoIme, korisnickaLozinka, idUredjaj);
    }

    public static boolean blokirajUredjajGrupe(java.lang.String korisnickoIme, java.lang.String korisnickaLozinka, int idUredjaj) {
        org.foi.nwtis.dkermek.ws.serveri.IoTMaster_Service service = new org.foi.nwtis.dkermek.ws.serveri.IoTMaster_Service();
        org.foi.nwtis.dkermek.ws.serveri.IoTMaster port = service.getIoTMasterPort();
        return port.blokirajUredjajGrupe(korisnickoIme, korisnickaLozinka, idUredjaj);
    }

    public static boolean obrisiUredjajGrupe(java.lang.String korisnickoIme, java.lang.String korisnickaLozinka, int idUredjaj) {
        org.foi.nwtis.dkermek.ws.serveri.IoTMaster_Service service = new org.foi.nwtis.dkermek.ws.serveri.IoTMaster_Service();
        org.foi.nwtis.dkermek.ws.serveri.IoTMaster port = service.getIoTMasterPort();
        return port.obrisiUredjajGrupe(korisnickoIme, korisnickaLozinka, idUredjaj);
    }

    public static StatusUredjaja dajStatusUredjajaGrupe(java.lang.String korisnickoIme, java.lang.String korisnickaLozinka, int idUredjaj) {
        org.foi.nwtis.dkermek.ws.serveri.IoTMaster_Service service = new org.foi.nwtis.dkermek.ws.serveri.IoTMaster_Service();
        org.foi.nwtis.dkermek.ws.serveri.IoTMaster port = service.getIoTMasterPort();
        return port.dajStatusUredjajaGrupe(korisnickoIme, korisnickaLozinka, idUredjaj);
    }
    
    

}
