package p3.Proxy;

import p3.HttpParser.HttpParser;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

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
            System.out.println(socket.getInputStream());
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
                // TODO: check if http is missing
                URL url = new URL(parser.getURI());
                if (url.getPort() != -1) {
                    port = url.getPort();
                } else {
                    port = (url.getProtocol() == "https") ? 443 : 80;
                }
            }
            System.out.println("host = " + host);
            System.out.println("port = " + port);

            client = new Socket(host, port);
            System.out.println("client socket = " + client.toString());
            System.out.println();

            StringBuilder request = new StringBuilder(parser.getFirstLine() + "\r\n");
            Map<String, String> headers = parser.getHeaders();
            headers.forEach((k,v) -> {
                request.append(k + ": " + v + "\r\n");
            });
            request.append("\r\n");
            System.out.println(request.toString());

            OutputStream outToServer = client.getOutputStream();
            DataOutputStream out = new DataOutputStream(outToServer);
            out.write(request.toString().getBytes());

            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            StringBuilder test = new StringBuilder();
            Stream<String> res = in.lines();
            res.forEach(line -> {
                test.append(line + "\r\n");
            });

//            System.out.println("server sent:");
//            Stream<String> response = in.lines();
//            response.forEach(line -> {
//                System.out.println(line);
//            });

            DataOutputStream outToBrowser = new DataOutputStream(socket.getOutputStream());
            System.out.println(test.toString());
            outToBrowser.write(test.toString().getBytes());



            client.close();
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}