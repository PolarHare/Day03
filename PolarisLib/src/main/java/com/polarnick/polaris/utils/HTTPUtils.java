package com.polarnick.polaris.utils;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.StringWriter;

/**
 * Date: 21.09.13
 *
 * @author Nickolay Polyarniy aka PolarNick
 */
public class HTTPUtils {

    public static String getContent(String urlString, String encodingCharset) throws IOException {
        HttpGet getRequest = new HttpGet(urlString);
        HttpResponse httpResponse = new DefaultHttpClient().execute(getRequest);
        StringWriter stringWriter = new StringWriter();
        IOUtils.copy(httpResponse.getEntity().getContent(), stringWriter, encodingCharset);
        return stringWriter.toString();
    }

}
