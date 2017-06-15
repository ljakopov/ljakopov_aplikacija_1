/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.ljakopov.web.dretve;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.foi.nwtis.dkermek.ws.serveri.StatusKorisnika;
import org.foi.nwtis.dkermek.ws.serveri.StatusUredjaja;
import org.foi.nwtis.ljakopov.konfiguracije.Konfiguracija;
import org.foi.nwtis.ljakopov.pomoc.BazaPodataka;
import org.foi.nwtis.ljakopov.pomoc.Dnevnik;
import org.foi.nwtis.ljakopov.ws.klijenti.IoT_MasterWSKlijent;
import org.foi.nwtis.ljakopov.ws.serveri.MeteoServiceWS;

/**
 *
 * @author ljakopov
 */
public class ObradaZahtjeva extends Thread {

    Socket socket;
    Konfiguracija konf;
    Connection c;
    public static boolean pause = false;
    public boolean registriranaGrupa;
    public boolean aktivnaGrupa;
    long pocetak, kraj;

    public ObradaZahtjeva(Socket socket, Konfiguracija konf) {
        this.socket = socket;
        this.konf = konf;
    }

    private void procitajZahtjev(String zahtjev, OutputStream os, Connection connection) throws IOException {
        String serverSocket = "^USER ([^\\s]+); PASSWD ([^\\s]+); (PAUSE;|STOP;|START;|STATUS;)";
        String IoT_Master = "^USER ([^\\s]+); PASSWD ([^\\s]+); IoT_Master (WORK;|STOP;|START;|STATUS;|WAIT;|LOAD;|CLEAR;|LIST;)";
        String IoT = "^USER ([^\\s]+); PASSWD ([^\\s]+); IoT ([0-9]{1,6}) (ADD \"([^\\s]+)\" \"([^\\s]+)\";|WORK;|STATUS;|WAIT;|REMOVE;)";

        Pattern pattern = Pattern.compile(serverSocket);
        Matcher m = pattern.matcher(zahtjev);
        boolean status = m.matches();

        Pattern patternIoT_Master = Pattern.compile(IoT_Master);
        Matcher mIoT_Master = patternIoT_Master.matcher(zahtjev);
        boolean statusIoT_Master = mIoT_Master.matches();

        Pattern patternIoT = Pattern.compile(IoT);
        Matcher mIoT = patternIoT.matcher(zahtjev);
        boolean statusIoT = mIoT.matches();

        //HttpServletRequest httpServletRequest = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String urlAdresa = ""; //= httpServletRequest.getRequestURI();

        if (status) {
            if (provjeriKorisnickoImeILozinku(m.group(1), m.group(2), connection)) {
                pocetak = 0;
                kraj = 0;
                pocetak = System.currentTimeMillis();
                switch (m.group(3)) {
                    case "PAUSE;":
                        if (pause == false) {
                            pause = true;
                            os.write("OK 10; Server se nalazi u PAUSE".getBytes());
                        } else {
                            os.write("ERROR 10; Server se  nalazi u stanju PAUSE".getBytes());
                        }
                        kraj = System.currentTimeMillis();
                        System.out.println("IPADRESSA: " + socket.getRemoteSocketAddress().toString());
                        Dnevnik.upisiUDnevnik(connection, m.group(1), (int) (kraj - pocetak), "serverSocket - PAUSE", "serverSocket", socket.getRemoteSocketAddress().toString(), urlAdresa);
                        break;
                    case "START;":
                        if (pause == true) {
                            pause = false;
                            os.write("OK 10; Server se nalazi u START(server je bio u PAUSE)".getBytes());
                        } else {
                            os.write("ERROR 11; Server se ne nalazi u stanju PAUSE".getBytes());
                        }
                        kraj = System.currentTimeMillis();
                        Dnevnik.upisiUDnevnik(connection, m.group(1), (int) (kraj - pocetak), "serverSocket - START", "serverSocket", socket.getRemoteSocketAddress().toString(), urlAdresa);
                        break;
                    case "STOP;":
                        if (ServerSustava.prekid_obrade == true) {
                            os.write("ERR 12; Server se nalazi u stanju prekidanja".getBytes());
                        } else {
                            ServerSustava.prekid_obrade = true;
                            os.write("OK 10; Server se nije lanazio u stanju prekidanja".getBytes());
                        }
                        kraj = System.currentTimeMillis();
                        Dnevnik.upisiUDnevnik(connection, m.group(1), (int) (kraj - pocetak), "serverSocket - STOP", "serverSocket", socket.getRemoteSocketAddress().toString(), urlAdresa);
                        break;
                    case "STATUS;":
                        if (pause == true) {
                            os.write("OK 13; Server ne preuzima podatke".getBytes());
                        } else if (pause == false) {
                            os.write("OK 14; Server preuzima podatke".getBytes());
                        }
                        if (ServerSustava.prekid_obrade == true) {
                            os.write("OK 15; Server ne radi".getBytes());
                        }
                        kraj = System.currentTimeMillis();
                        Dnevnik.upisiUDnevnik(connection, m.group(1), (int) (kraj - pocetak), "serverSocket - STATUS", "serverSocket", socket.getRemoteSocketAddress().toString(), urlAdresa);
                        break;
                }
            } else {
                os.write("ERR 10;".getBytes());
            }
        } else if (statusIoT_Master) {
            if (provjeriKorisnickoImeILozinku(mIoT_Master.group(1), mIoT_Master.group(2), connection)) {
                pocetak = 0;
                kraj = 0;
                pocetak = System.currentTimeMillis();
                switch (mIoT_Master.group(3)) {
                    case "START;":
                        registriranaGrupa = IoT_MasterWSKlijent.registrirajGrupuIoT(mIoT_Master.group(1), mIoT_Master.group(2));
                        if (registriranaGrupa) {
                            os.write("OK 10;".getBytes());
                        } else {
                            os.write("ERR 20;".getBytes());
                        }
                        kraj = System.currentTimeMillis();
                        Dnevnik.upisiUDnevnik(connection, mIoT_Master.group(1), (int) (kraj - pocetak), "IoT_Master - START", "serverSocket", socket.getRemoteSocketAddress().toString(), urlAdresa);
                        break;
                    case "STOP;":
                        registriranaGrupa = IoT_MasterWSKlijent.deregistrirajGrupuIoT(mIoT_Master.group(1), mIoT_Master.group(2));
                        if (registriranaGrupa) {
                            os.write("OK 10;".getBytes());
                        } else {
                            os.write("ERR 21;".getBytes());
                        }
                        kraj = System.currentTimeMillis();
                        Dnevnik.upisiUDnevnik(connection, mIoT_Master.group(1), (int) (kraj - pocetak), "IoT_Master - STOP", "serverSocket", socket.getRemoteSocketAddress().toString(), urlAdresa);
                        break;
                    case "WORK;":
                        aktivnaGrupa = IoT_MasterWSKlijent.aktivirajGrupuIoT(mIoT_Master.group(1), mIoT_Master.group(2));
                        if (aktivnaGrupa) {
                            os.write("OK 10;".getBytes());
                        } else {
                            os.write("ERR 22;".getBytes());
                        }
                        kraj = System.currentTimeMillis();
                        Dnevnik.upisiUDnevnik(connection, mIoT_Master.group(1), (int) (kraj - pocetak), "IoT_Master - WORK", "serverSocket", socket.getRemoteSocketAddress().toString(), urlAdresa);
                        break;
                    case "WAIT;":
                        System.out.println("OVO JE WAIT");
                        aktivnaGrupa = IoT_MasterWSKlijent.blokirajGrupuIoT(mIoT_Master.group(1), mIoT_Master.group(2));
                        if (aktivnaGrupa) {
                            os.write("OK 10;".getBytes());
                        } else {
                            os.write("ERR 23;".getBytes());
                        }
                        kraj = System.currentTimeMillis();
                        Dnevnik.upisiUDnevnik(connection, mIoT_Master.group(1), (int) (kraj - pocetak), "IoT_Master - WAIT", "serverSocket", socket.getRemoteSocketAddress().toString(), urlAdresa);
                        break;
                    case "LOAD;":
                        IoT_MasterWSKlijent.ucitajSveUredjajeGrupe(mIoT_Master.group(1), mIoT_Master.group(2));
                        os.write("OK 10;".getBytes());
                        Dnevnik.upisiUDnevnik(connection, mIoT_Master.group(1), (int) (kraj - pocetak), "IoT_Master - LOAD", "serverSocket", socket.getRemoteSocketAddress().toString(), urlAdresa);
                        break;
                    case "CLEAR;":
                        IoT_MasterWSKlijent.obrisiSveUredjajeGrupe(mIoT_Master.group(1), mIoT_Master.group(2));
                        os.write("OK 10;".getBytes());
                        kraj = System.currentTimeMillis();
                        Dnevnik.upisiUDnevnik(connection, mIoT_Master.group(1), (int) (kraj - pocetak), "IoT_Master - CLEAR", "serverSocket", socket.getRemoteSocketAddress().toString(), urlAdresa);
                        break;
                    case "STATUS;":
                        StatusKorisnika statusGrupeString = IoT_MasterWSKlijent.dajStatusGrupeIoT(mIoT_Master.group(1), mIoT_Master.group(2));
                        if ("AKTIVAN".equals(statusGrupeString.toString())) {
                            System.out.println("OK 25;");
                        } else {
                            System.out.println("OK 24;");
                        }
                        kraj = System.currentTimeMillis();
                        Dnevnik.upisiUDnevnik(connection, mIoT_Master.group(1), (int) (kraj - pocetak), "IoT_Master - START", "serverSocket", socket.getRemoteSocketAddress().toString(), urlAdresa);
                        break;
                    case "LIST;":
                        //TODO sredi ispis svih LIST kod primitvnog poslužitelja
                        kraj = System.currentTimeMillis();
                        Dnevnik.upisiUDnevnik(connection, mIoT_Master.group(1), (int) (kraj - pocetak), "IoT_Master - LIST", "serverSocket", socket.getRemoteSocketAddress().toString(), urlAdresa);
                        break;
                }
            } else {
                os.write("ERR 10;".getBytes());
            }
        } else if (statusIoT) {
            if (provjeriKorisnickoImeILozinku(mIoT.group(1), mIoT.group(2), connection)) {
                pocetak = 0;
                kraj = 0;
                pocetak = System.currentTimeMillis();
                switch (mIoT.group(4)) {
                    case "ADD;":
                        //TODO do kraja dovrsi ADD u server socketu
                        kraj = System.currentTimeMillis();
                        Dnevnik.upisiUDnevnik(connection, mIoT.group(1), (int) (kraj - pocetak), "IoT - ADD", "serverSocket", socket.getRemoteSocketAddress().toString(), urlAdresa);
                        break;
                    case "WORK;":
                        System.out.println("IPADRESSA: " + socket.getRemoteSocketAddress().toString());
                        //System.out.println("URL: " + socket.);
                        boolean aktivanUredjaj = IoT_MasterWSKlijent.aktivirajUredjajGrupe(mIoT.group(1), mIoT.group(2), Integer.parseInt(mIoT.group(3)));
                        if (aktivanUredjaj) {
                            os.write("OK 10;".getBytes());
                        } else {
                            os.write("ERR 31;".getBytes());
                        }
                        kraj = System.currentTimeMillis();
                        Dnevnik.upisiUDnevnik(connection, mIoT.group(1), (int) (kraj - pocetak), "IoT - WORK", "serverSocket", socket.getRemoteSocketAddress().toString(), urlAdresa);
                        break;
                    case "WAIT;":
                        boolean blokiranUredjaj = IoT_MasterWSKlijent.blokirajUredjajGrupe(mIoT.group(1), mIoT.group(2), Integer.parseInt(mIoT.group(3)));
                        if (blokiranUredjaj) {
                            os.write("OK 10;".getBytes());
                        } else {
                            os.write("ERR 32;".getBytes());
                        }
                        kraj = System.currentTimeMillis();
                        Dnevnik.upisiUDnevnik(connection, mIoT.group(1), (int) (kraj - pocetak), "IoT - WAIT", "serverSocket", socket.getRemoteSocketAddress().toString(), urlAdresa);
                        break;
                    case "REMOVE;":
                        boolean obrisaniUredjaj = IoT_MasterWSKlijent.obrisiUredjajGrupe(mIoT.group(1), mIoT.group(2), Integer.parseInt(mIoT.group(3)));
                        if (obrisaniUredjaj) {
                            os.write("OK 10;".getBytes());
                        } else {
                            os.write("ERR 33;".getBytes());
                        }
                        kraj = System.currentTimeMillis();
                        Dnevnik.upisiUDnevnik(connection, mIoT.group(1), (int) (kraj - pocetak), "IoT - REMOVE", "serverSocket", socket.getRemoteSocketAddress().toString(), urlAdresa);
                        break;
                    case "STATUS;":
                        StatusUredjaja statusUredjaja = IoT_MasterWSKlijent.dajStatusUredjajaGrupe(mIoT.group(1), mIoT.group(2), Integer.parseInt(mIoT.group(3)));
                        if ("AKTIVAN".equals(statusUredjaja.toString())) {
                            os.write("OK 35;".getBytes());
                        } else {
                            os.write("OK 34;".getBytes());
                        }
                        kraj = System.currentTimeMillis();
                        Dnevnik.upisiUDnevnik(connection, mIoT.group(1), (int) (kraj - pocetak), "IoT - STATUS", "serverSocket", socket.getRemoteSocketAddress().toString(), urlAdresa);
                        break;
                }
            } else {
                os.write("ERR 10;".getBytes());
            }
        }
        os.flush();
    }

    private boolean provjeriKorisnickoImeILozinku(String korisnickoIme, String lozinka, Connection connection) {
        String provjeraKorisnika = "SELECT username,pass FROM korisnik WHERE username = ? and pass = ?";
        boolean provjera = false;

        PreparedStatement ps;
        try {
            ps = connection.prepareStatement(provjeraKorisnika);
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

    @Override
    public void interrupt() {
        super.interrupt(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void run() {
        super.run(); //To change body of generated methods, choose Tools | Templates.

        InputStream is = null;
        OutputStream os = null;
        try {
            System.out.println("KLASA JE: " + this.getClass().getName());
            is = socket.getInputStream();
            os = socket.getOutputStream();

            StringBuffer sb = new StringBuffer();

            while (true) {
                int znak = is.read();
                if (znak == -1) {
                    break;
                }
                sb.append((char) znak);
            }
            System.out.println("Primljena naredba: " + sb);
            Connection connection = BazaPodataka.konekcijaNaBazu(c);
            procitajZahtjev(sb.toString(), os, connection);
            socket.close();
            try {
                connection.close();
            } catch (SQLException ex) {
                Logger.getLogger(ObradaZahtjeva.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (IOException ex) {
            Logger.getLogger(ObradaZahtjeva.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (os != null) {
                    os.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(ObradaZahtjeva.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public synchronized void start() {
        super.start(); //To change body of generated methods, choose Tools | Templates.
    }

}
