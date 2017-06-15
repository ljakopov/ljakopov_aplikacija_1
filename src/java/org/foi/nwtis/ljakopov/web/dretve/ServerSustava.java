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

    public static boolean prekid_obrade = false;
    private ServletContext sc = null;
    private ServerSocket serverSocket;

    private void otvaranjeServerSocketa() {
        Konfiguracija konf = (Konfiguracija) sc.getAttribute("Mail_Konfig");
        int port = Integer.parseInt(konf.dajPostavku("port"));
        try {
            System.out.println("ONA JE POREKNUTA");
            serverSocket = new ServerSocket(port);
        } catch (IOException ex) {
            System.out.println("PORT JE VEĆ OTVOREN");
            //Logger.getLogger(ServerSustava.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void interrupt() {
        prekid_obrade=true;
        try {
            System.out.println("OVO JE PREKID RADA S PORTOM");
            this.serverSocket.close();
            //super.interrupt(); //To change body of generated methods, choose Tools | Templates.
        } catch (IOException ex) {
            Logger.getLogger(ServerSustava.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        Konfiguracija konf = (Konfiguracija) sc.getAttribute("Mail_Konfig");
        //super.run(); //To change body of generated methods, choose Tools | Templates.
        otvaranjeServerSocketa();
        while (!prekid_obrade) {
            Socket socket = null;
            try {
                socket = serverSocket.accept();
            } catch (IOException ex) {
                System.out.println("PORT JE OTVOREN ipak");
                Logger.getLogger(ServerSustava.class.getName()).log(Level.SEVERE, null, ex);
            } 
            ObradaZahtjeva obradaZahtjeva = new ObradaZahtjeva(socket, konf);
            obradaZahtjeva.start();
        }
    }

    @Override
    public synchronized void start() {
        try {
            System.out.println("IDE POMALO");
            Thread.sleep(2000); // 2 s
        } catch (InterruptedException ex) {
            Logger.getLogger(ServerSustava.class.getName()).log(Level.SEVERE, null, ex);
        }
        super.start(); //To change body of generated methods, choose Tools | Templates.
    }

    public void setSc(ServletContext sc) {
        this.sc = sc;
    }
}
