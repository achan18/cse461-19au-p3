package p3;

import java.io.*;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class Handler extends Thread {
    private static final Date date = new Date();
    private static final DateFormat dateFormat = new SimpleDateFormat("dd MMM HH:mm:ss");

    private Socket browser_to_proxy;
    private Socket proxy_to_server;
    private String host;
    private int port;

    public Handler(Socket browser_to_proxy) {
        this.browser_to_proxy = browser_to_proxy;
    }

    public void run() {
        try {
            InputStream fromBrowser = browser_to_proxy.getInputStream();
            HttpRequestParser parser = new HttpRequestParser(fromBrowser);
            String firstLine = parser.getFirstLine();
            if (firstLine == null) {
                return;
            }

            // print request
            System.out.println(dateFormat.format(date) + " - >>> " + firstLine);

            this.host = getHost(parser);
            this.port = getPort(parser);

            if (!parser.getMethod().equalsIgnoreCase("CONNECT")) {
                // NON CONNECT PROTOCOLS
                this.proxy_to_server = new Socket(host, port);
                parser.setHeader("Connection", "close");

                StringBuilder request = new StringBuilder(parser.getFirstLine() + "\r\n");
                Map<String, String> headers = parser.getHeaders();
                headers.forEach((k, v) -> {
                    request.append(k + ": " + v + "\r\n");
                });
                if (parser.getBody() != null) {
                    request.append(parser.getBody());
                }
                request.append("\r\n\r\n");

                OutputStream outToServer = proxy_to_server.getOutputStream();
                DataOutputStream out = new DataOutputStream(outToServer);
                out.write(request.toString().getBytes());

//                DataInputStream in = new DataInputStream(new BufferedInputStream(proxy_to_server.getInputStream()));
//                DataOutputStream outToBrowser = new DataOutputStream(browser_to_proxy.getOutputStream());
//                in.transferTo(outToBrowser);

                ConnectTunnel fromServer = new ConnectTunnel(proxy_to_server, browser_to_proxy, "server", "browser");
                ConnectTunnel fromBrowser2 = new ConnectTunnel(browser_to_proxy, proxy_to_server, "browser", "server");
                fromServer.start();
                fromBrowser2.start();
            } else {
                // TUNNEL CONNECT

                // 1. Send response
                this.proxy_to_server = new Socket(host, port);
                DataOutputStream outToBrowser = new DataOutputStream(browser_to_proxy.getOutputStream());

                if (!proxy_to_server.isConnected()) {
                    String responseToBrowser = parser.getVersion() + " 502 Bad Gateway\r\n\r\n";
                    outToBrowser.write(responseToBrowser.getBytes());
                    return;
                }

                String responseToBrowser = "HTTP/1.0 200 OK\r\n\r\n";
                outToBrowser.write(responseToBrowser.getBytes());

                // 2. Open a bit tunnel
                ConnectTunnel thread1 = new ConnectTunnel(proxy_to_server, browser_to_proxy, "Server", "Browser");
                ConnectTunnel thread2 = new ConnectTunnel(browser_to_proxy, proxy_to_server, "Browser", "Server");
                thread1.start();
                thread2.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getHost(HttpRequestParser parser) {
        String header = parser.getHeader("host");
        String[] vals = header.split(":");
        return vals[0];
    }

    private int getPort(HttpRequestParser parser) throws MalformedURLException {
        String[] vals = parser.getHeader("host").split(":");
        if (vals.length == 2) {
            return Integer.parseInt(vals[1]);
        }
        try {
            URL url = new URL(parser.getURI());
            return (url.getProtocol().equals("https")) ? 443 : 80;
        } catch (MalformedURLException e) {
            return 80;
        }
    }
}