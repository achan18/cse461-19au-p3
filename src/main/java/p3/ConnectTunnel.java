package p3;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

public class ConnectTunnel extends Thread {
    Socket proxyToServer;
    Socket proxyToClient;

    public ConnectTunnel(Socket proxyToServer, Socket proxyToClient) {
        this.proxyToClient = proxyToClient;
        this.proxyToServer = proxyToServer;
    }

    public void run() {
        try {
            DataInputStream fromServer = new DataInputStream(new BufferedInputStream(proxyToServer.getInputStream()));
            DataOutputStream outToServer = new DataOutputStream(new BufferedOutputStream(proxyToServer.getOutputStream()));

            DataInputStream fromBrowser = new DataInputStream(new BufferedInputStream(proxyToClient.getInputStream()));
            DataOutputStream outToBrowser = new DataOutputStream(new BufferedOutputStream(proxyToClient.getOutputStream()));
            while (true) {
                System.out.println("server sent:");
                byte[] serverMessage = fromServer.readAllBytes();
                System.out.println(Arrays.toString(serverMessage));
                outToBrowser.write(serverMessage);

                System.out.println("browser sent:");
                byte[] browserMessage = fromBrowser.readAllBytes();
                System.out.println(Arrays.toString(browserMessage));
                outToServer.write(fromBrowser.readAllBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
