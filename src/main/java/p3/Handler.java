package p3;

import java.io.*;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.Map;

public class Handler extends Thread {
    private Socket browser_to_proxy;
    private Socket proxy_to_server;
    private String host;
    private int port;

    public Handler(Socket browser_to_proxy) {
        this.browser_to_proxy = browser_to_proxy;
    }

    public void run() {
        try {
            HttpRequestParser parser = new HttpRequestParser(browser_to_proxy.getInputStream());
            System.out.println(parser.getFirstLine());

            setHostAndPort(parser);
            this.proxy_to_server = new Socket(host, port);

            if (!parser.getMethod().equalsIgnoreCase("CONNECT")) {
                // NON CONNECT PROTOCOLS
                parser.setHeader("Connection", "close");

                StringBuilder request = new StringBuilder(parser.getFirstLine() + "\r\n");
                Map<String, String> headers = parser.getHeaders();
                headers.forEach((k, v) -> {
                    request.append(k + ": " + v + "\r\n");
                });
                request.append("\r\n");

                OutputStream outToServer = proxy_to_server.getOutputStream();
                DataOutputStream out = new DataOutputStream(outToServer);
                out.write(request.toString().getBytes());

                DataInputStream in = new DataInputStream(new BufferedInputStream(proxy_to_server.getInputStream()));
                DataOutputStream outToBrowser = new DataOutputStream(browser_to_proxy.getOutputStream());
                in.transferTo(outToBrowser);
            } else {
                // TUNNEL CONNECT

                // 1. Send response to browser
                StringBuilder responseToBrowser = new StringBuilder();
                String statusCode = (proxy_to_server.isConnected()) ? "200 OK" : "502 Bad Gateway";
                responseToBrowser.append(parser.getVersion() + " " + statusCode);
                responseToBrowser.append("\r\n\r\n");

                DataOutputStream outToBrowser = new DataOutputStream(browser_to_proxy.getOutputStream());
                outToBrowser.write(responseToBrowser.toString().getBytes());

                // 2. Open a bit tunnel
                ConnectTunnel thread = new ConnectTunnel(proxy_to_server, browser_to_proxy, "Server", "Browser");
                ConnectTunnel thread2 = new ConnectTunnel(browser_to_proxy, proxy_to_server, "Browser", "Server");
                thread.start();
                thread2.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setHostAndPort(HttpRequestParser parser) throws MalformedURLException {
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
    }
}