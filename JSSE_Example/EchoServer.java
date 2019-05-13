/*
Run with: java -Djavax.net.ssl.keyStore=keystore.jks -Djavax.net.ssl.keyStorePassword=password EchoServer
*/

import java.io.IOException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

public class EchoServer
{
  public static void main(String[] args)
  {
    try
    {
      SSLServerSocketFactory sslserversocketfactory =
        (SSLServerSocketFactory)SSLServerSocketFactory.getDefault();
      SSLServerSocket sslserversocket =
        (SSLServerSocket)sslserversocketfactory.createServerSocket(9999);
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

