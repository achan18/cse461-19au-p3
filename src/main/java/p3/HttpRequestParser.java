package p3;

import java.io.*;
import java.util.*;

public class HttpRequestParser {

    private String method;
    private String URI;
    private String version;
    private String firstLine;
    private String body;
    private Map<String, String> headers;

    public HttpRequestParser(InputStream is) {
        headers = new HashMap<String,String>();

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        List<String> temp = new ArrayList<String>();
        while (true) {
            String line = "";
            try {
                line = reader.readLine();
                if (line == null || line.equals("")) {
                    break;
                }
                temp.add(line);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (temp.size() > 0) {
            this.firstLine = temp.get(0);
            String[] vals = firstLine.split("\\s");
            this.method = vals[0];
            this.URI = vals[1];
            this.version = vals[2];
        }

        for (int i = 1; i < temp.size(); i++) {
            String line = temp.get(i);
            String[] args = line.split(": ");
            headers.put(args[0], args[1]);
        }

        if (headers.containsKey("Content-Length") || headers.containsKey("Transfer-Encoding")) {
            System.out.println("has body");
            try {
                reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("read one line");
            StringBuilder body = new StringBuilder();
            String line = "";
            while (true) {
                try {
                    line = reader.readLine();
                    if (line == null || line.equals("")) {
                        break;
                    }
                    System.out.println(line);
                    body.append(line);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("body = " + body.toString());
        }

    }

    public void setHeader(String header, String val) {
        if (headers.containsKey(header)) {
            headers.put(header, val);
        }
    }

    public String getMethod() {
        return this.method;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getHeader(String header) {
        return headers.getOrDefault(header, null);
    }

    public String getVersion() {
        return this.version;
    }

    public String getURI() {
        return URI;
    }

    public String getFirstLine() {
        return firstLine;
    }
}
