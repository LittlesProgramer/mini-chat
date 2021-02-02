package interruptible;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class InterClient2 {
    private static final InetSocketAddress adres = new InetSocketAddress("HP",12345);
    public static void main(String[] args) throws IOException {
        Socket client = new Socket();
        new Logowanie(client,adres);
    }
}
