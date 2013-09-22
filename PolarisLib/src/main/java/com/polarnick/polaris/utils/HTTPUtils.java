package com.polarnick.polaris.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Date: 21.09.13
 *
 * @author Nickolay Polyarniy aka PolarNick
 */
public class HTTPUtils {

    public static String getContent(String urlString) throws IOException {
        URL url = new URL(urlString);
        return readContent(url.openStream());
    }

    public static String readContent(InputStream stream) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(stream));
        StringBuilder result = new StringBuilder();
        String inputLine = in.readLine();
        while (inputLine != null) {
            result.append(inputLine);
            inputLine = in.readLine();
        }
        in.close();
        return result.toString();
    }

}
