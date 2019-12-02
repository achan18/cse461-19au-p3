package p3.Proxy;

import p3.HttpParser.HttpParser;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;

public class Proxy {
    public int port;
    public ServerSocket server;
    public Socket client;

    public Proxy(int port) {
        this.port = port;
        try {
            server = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        // TODO: implement starting the proxy
        try {
            System.out.println("waiting for requests...");
            Socket socket = server.accept();
            HttpParser parser = new HttpParser(socket.getInputStream());
            System.out.println(parser.getFirstLine());
            parser.setHeader("Connection", "close");

            String host;
            int port;

            String header = parser.getHeader("Host");
            String[] vals = header.split(":");
            host = vals[0];
            if (vals.length == 2) {
                port = Integer.parseInt(vals[1]);
            } else {
                URL url = new URL(parser.getURI());
                if (url.getPort() != -1) {
                    port = url.getPort();
                } else {
                    port = (url.getProtocol() == "https") ? 443 : 80;
                }
            }

            client = new Socket(host, port);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            out.writeUTF(parser.getFirstLine() + "\\r\\n");
            Map<String, String> headers = parser.getHeaders();
            headers.forEach((k,v) -> {
                try {
                    out.writeUTF(k + ": " + v + "\\r\\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            out.writeUTF("\\r\\n");




        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}