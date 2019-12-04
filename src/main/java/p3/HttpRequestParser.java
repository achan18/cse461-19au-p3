package p3;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

public class HttpRequestParser {

    private String method;
    private String URI;
    private String version;
    private String firstLine;
    private Map<String, String> headers;

    public HttpRequestParser(InputStream is) {
        headers = new HashMap<String,String>();

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        Stream<String> stream = reader.lines();
        Iterator<String> it = stream.iterator();

        if (!it.hasNext()) {
            throw new IllegalArgumentException("request is null");
        }

        String firstLine = it.next();
        this.firstLine = firstLine;
        String[] vals = firstLine.split("\\s");
        this.method = vals[0];
        this.URI = vals[1];
        this.version = vals[2];

        // get all headers
        // TODO: make all header keys lower case
        String line;
        while ((line = it.next()).length() != 0) {
            if (line != null && line.length() > 0) {
                String[] args = line.split(": ");
                headers.put(args[0], args[1]);
            }
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