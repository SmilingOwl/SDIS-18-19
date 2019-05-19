/*
Run with: java -Djavax.net.ssl.keyStore=keystore.jks -Djavax.net.ssl.keyStorePassword=password EchoServer
*/

import java.io.IOException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.net.*;
import java.io.*;
import javax.net.ssl.*;
import javax.security.cert.X509Certificate;
import java.security.KeyStore;

public class EchoServer
{
  public static void main(String[] args)
  {
    try
    {
      SSLContext ctx;
      KeyManagerFactory kmf;
      KeyStore ks, ks2;
      char[] passphrase = "password".toCharArray();

      ctx = SSLContext.getInstance("TLS");
      kmf = KeyManagerFactory.getInstance("SunX509");
      TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
      ks = KeyStore.getInstance("JKS");
      ks2 = KeyStore.getInstance("JKS");

      ks.load(new FileInputStream("keystore.jks"), passphrase);
      ks2.load(new FileInputStream("truststore.ts"), passphrase);
      tmf.init(ks2);

      kmf.init(ks, passphrase);
      ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

      SSLServerSocketFactory sslserversocketfactory =
        (SSLServerSocketFactory)ctx.getServerSocketFactory();
      SSLServerSocket sslserversocket =
        (SSLServerSocket)sslserversocketfactory.createServerSocket(9999);
      sslserversocket.setNeedClientAuth(true);
      
      while (true) {
        SSLSocket sslsocket = (SSLSocket) sslserversocket.accept();
        ConnectionThread connection = new ConnectionThread(sslsocket);
        Thread thread = new Thread(connection); //in project 2, use ScheduledThreadPoolExecutor for this
        thread.start();
      }
    } catch (Exception exception)
    {
      exception.printStackTrace();
    }
  }
}

