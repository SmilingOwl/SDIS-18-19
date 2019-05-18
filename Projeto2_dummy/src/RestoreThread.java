import java.net.*;
import java.io.*;
import java.util.*;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class RestoreThread implements Runnable {
    private String file_name;
    private int rep_degree;
    private Peer owner;

    public RestoreThread(String file_name, Peer owner) {
        this.file_name = file_name;
        this.owner = owner;
    }

    public void run() {
        
    }
}