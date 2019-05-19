/*
Run with: java -Djavax.net.ssl.trustStore=truststore.ts -Djavax.net.ssl.trustStorePassword=password EchoClient
*/

import java.io.InputStream;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.net.*;
import java.io.*;
import javax.net.ssl.*;
import javax.security.cert.X509Certificate;
import java.security.KeyStore;

public class EchoClient
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

      SSLSocketFactory sslsocketfactory = (SSLSocketFactory)SSLSocketFactory.getDefault();
      SSLSocket sslsocket = (SSLSocket)sslsocketfactory.createSocket("localhost", 9999);
      InputStream inputstream = System.in;
      InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
      BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
      OutputStream outputstream = sslsocket.getOutputStream();
      OutputStreamWriter outputstreamwriter = new OutputStreamWriter(outputstream);
      BufferedWriter bufferedwriter = new BufferedWriter(outputstreamwriter);
      String string = null;
      while ((string = bufferedreader.readLine()) != null)
      {
        bufferedwriter.write(string + '\n');
        bufferedwriter.flush();
      }
    }
    catch (Exception exception)
    {
      exception.printStackTrace();
    }
  }
}
