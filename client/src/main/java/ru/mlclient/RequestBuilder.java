package ru.mlclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;

/**
 *
 * @author scorpds
 */
public class RequestBuilder {

    private HttpURLConnection conn;

    public RequestBuilder() throws IOException {
        URL url = new URL("http://localhost:8080/blob");
        conn = (HttpURLConnection) url.openConnection();
    }

    public void sendRequest(RawScreenshot image, String elementType, String elementText) throws IOException {

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addTextBody("elementType", elementType, ContentType.TEXT_PLAIN);
        builder.addTextBody("elementCaption", elementText, ContentType.TEXT_PLAIN);
        builder.addBinaryBody("image", image.getBytes(), ContentType.MULTIPART_FORM_DATA, "1.png");
        HttpEntity multipart = builder.build();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-type", multipart.getContentType().getValue());
        conn.setDoOutput(true);

        OutputStream os = conn.getOutputStream();
        multipart.writeTo(os);
        os.flush();
        os.close();
        
        if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
            throw new RuntimeException("Request failed: HTTP error code: "
                    + conn.getResponseCode());
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(
                conn.getInputStream()));

        String output;
        System.out.println("Output from Server ... \n");
        while ((output = br.readLine()) != null) {
            System.out.println(output);
        }

        conn.disconnect();
    }
}
