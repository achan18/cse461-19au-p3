package p3;

import p3.HttpRequestParser;

import java.io.*;
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
        while (true) {
            try {
                Socket socket = server.accept();
                System.out.println("proxy to browser: " + socket);
                HttpRequestParser parser = new HttpRequestParser(socket.getInputStream());
                System.out.println(parser.getFirstLine());

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
                client = new Socket(host, port);
                System.out.println("proxy to server: " + client);

                if (!parser.getMethod().equalsIgnoreCase("CONNECT")) {
                    // NON CONNECT PROTOCOLS
                    parser.setHeader("Connection", "close");
                    //                System.out.println("host = " + host);
                    //                System.out.println("port = " + port);

                    //                System.out.println("client socket = " + client.toString());
                    //                System.out.println();

                    StringBuilder request = new StringBuilder(parser.getFirstLine() + "\r\n");
                    Map<String, String> headers = parser.getHeaders();
                    headers.forEach((k, v) -> {
                        request.append(k + ": " + v + "\r\n");
                    });
                    request.append("\r\n");

                    OutputStream outToServer = client.getOutputStream();
                    DataOutputStream out = new DataOutputStream(outToServer);
                    out.write(request.toString().getBytes());


                    DataInputStream in = new DataInputStream(new BufferedInputStream(client.getInputStream()));
                    DataOutputStream outToBrowser = new DataOutputStream(socket.getOutputStream());
                    in.transferTo(outToBrowser);
                } else {
                    // TUNNEL CONNECT

                    // 1. Send response to browser
                    StringBuilder responseToBrowser = new StringBuilder();
                    String statusCode = (client.isConnected()) ? "200 OK" : "502 Bad Gateway";
                    responseToBrowser.append(parser.getVersion() + " " + statusCode);
                    responseToBrowser.append("\r\n\r\n");

                    DataOutputStream outToBrowser = new DataOutputStream(socket.getOutputStream());
                    outToBrowser.writeBytes(responseToBrowser.toString());
                    System.out.println(responseToBrowser.toString());

                    // 2. Open a bit tunnel
                    if (client.isConnected()) {
                        ConnectTunnel thread = new ConnectTunnel(client, socket, "Server", "Browser");
                        ConnectTunnel thread2 = new ConnectTunnel(socket, client, "Browser", "Server");
                        thread.start();
                        thread2.start();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}