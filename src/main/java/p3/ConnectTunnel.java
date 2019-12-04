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
            DataOutputStream outToReceiver = new DataOutputStream((receiver.getOutputStream()));

//            byte[] buf = new byte[2048];
//            while (true) {
//                int offset = 0;
//                int rlen = 1;
//                while (rlen != 0) {
//                    rlen = fromSender.read(buf, offset, 0);
////                    System.out.println("read len = " + rlen);
////                    System.out.println(senderName + " sent: " + Arrays.toString(buf));
//                    offset += rlen;
//                    outToReceiver.write(buf, offset, rlen);
//                }

                byte[] browserMessage = fromSender.readAllBytes();
                System.out.println(senderName + " sent: " + Arrays.toString(browserMessage));
                outToReceiver.write(browserMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
