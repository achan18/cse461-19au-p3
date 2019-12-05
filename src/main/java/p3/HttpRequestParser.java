package p3;

import java.io.*;
import java.util.*;
import java.util.stream.Stream;

public class HttpRequestParser {

    private String method;
    private String URI;
    private String version;
    private String firstLine;
    private Map<String, String> headers;

    public HttpRequestParser(InputStream is) {
        headers = new HashMap<String,String>();

        DataInputStream reader = new DataInputStream(new BufferedInputStream(is));
        try {
            String s = reader.readUTF();
            System.out.println(s);
        } catch (IOException e) {
            e.printStackTrace();
        }

//        String firstLine = temp.get(0);
//        this.firstLine = firstLine;
//        String[] vals = firstLine.split("\\s");
//        this.method = vals[0];
//        this.URI = vals[1];
//        this.version = vals[2];
//
//        // get all headers
//        // TODO: make all header keys lower case
//        String line;
//        for (int i = 1; i < temp.size(); i++) {
//            line = temp.get(i);
//            if (line != null && line.length() > 0) {
//                String[] args = line.split(": ");
//                headers.put(args[0], args[1]);
//            }
//        }
//        System.out.println(headers);
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
