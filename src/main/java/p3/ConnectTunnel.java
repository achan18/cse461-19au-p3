package p3;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

public class ConnectTunnel extends Thread {
    Socket sender;
    Socket receiver;
    String senderName;
    String receiverName;

    public ConnectTunnel(Socket sender, Socket receiver, String senderName, String receiverName) {
        this.sender = sender;
        this.receiver = receiver;
        this.senderName = senderName;
        this.receiverName = receiverName;
    }

    public void run() {
        try {
            DataInputStream fromSender = new DataInputStream(new BufferedInputStream(sender.getInputStream()));
            DataOutputStream outToReceiver = new DataOutputStream(new BufferedOutputStream(receiver.getOutputStream()));

            while (true) {
                System.out.println(senderName + " sent:");
                byte[] browserMessage = fromSender.readAllBytes();
                System.out.println(Arrays.toString(browserMessage));
                outToReceiver.write(browserMessage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
