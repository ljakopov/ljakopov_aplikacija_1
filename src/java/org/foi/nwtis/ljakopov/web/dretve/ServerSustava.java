/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.ljakopov.web.dretve;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import org.foi.nwtis.ljakopov.konfiguracije.Konfiguracija;

/**
 *
 * @author ljakopov
 */
public class ServerSustava extends Thread {

    private boolean prekid_obrade = false;
    private ServletContext sc = null;
    private ServerSocket serverSocket;

    private void otvaranjeServerSocketa() {
         Konfiguracija konf = (Konfiguracija) sc.getAttribute("Mail_Konfig");
        int port = Integer.parseInt(konf.dajPostavku("port"));
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException ex) {
            Logger.getLogger(ServerSustava.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void interrupt() {
        super.interrupt(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void run() {
         Konfiguracija konf = (Konfiguracija) sc.getAttribute("Mail_Konfig");
        //super.run(); //To change body of generated methods, choose Tools | Templates.
        otvaranjeServerSocketa();
        while (true) {
            Socket socket = null;
            try {
                socket = serverSocket.accept();
            } catch (IOException ex) {
                Logger.getLogger(ServerSustava.class.getName()).log(Level.SEVERE, null, ex);
            }
            ObradaZahtjeva obradaZahtjeva = new ObradaZahtjeva(socket, konf);
            obradaZahtjeva.start();
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
