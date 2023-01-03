package com.pic.velib.service.properties;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Component
public class PropertiesImpl implements Properties {
    private JSONObject properties;

    private Map<key, String> props = null;

    private enum key {Database_URL , Database_USER, Database_PASSWORD, Recaptcha_SECRET, Facebook_CLIENT_ID, Facebook_CLIENT_SECRET}

    public PropertiesImpl()
    {
        props = new HashMap<key, String>();
        props.put(key.Database_URL, "database_url");
        props.put(key.Database_USER, "database_user");
        props.put(key.Database_PASSWORD, "database_password");
        props.put(key.Recaptcha_SECRET, "recaptcha_secret");
        props.put(key.Facebook_CLIENT_ID, "facebook_client_id");
        props.put(key.Facebook_CLIENT_SECRET, "facebook_client_secret");



        String urlPropertiesFile = System.getProperty("user.dir") + "/properties.json";

        System.out.println(urlPropertiesFile);

        try {
            File f = new File(urlPropertiesFile);
            if (f.exists()) {
                InputStream is = new FileInputStream(urlPropertiesFile);

                StringBuilder resultStringBuilder = new StringBuilder();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line;
                while ((line = br.readLine()) != null) {
                    resultStringBuilder.append(line);
                }

                String jsonTxt = resultStringBuilder.toString();

                properties = new JSONObject(jsonTxt);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getString(String prop)
    {
        try {
            return properties.getString(prop);
        }catch(Exception e) {
            return null;
        }
    }

    @Override
    public String getDatabaseURL() {
        return getString(props.get(key.Database_URL));
    }

    @Override
    public String getDatabaseUser() {
        return getString(props.get(key.Database_USER));
    }

    @Override
    public String getDatabasePassword() {
        return getString(props.get(key.Database_PASSWORD));
    }

    @Override
    public String getRecaptchaSecret() {
        return getString(props.get(key.Recaptcha_SECRET));
    }

    @Override
    public String getFacebookClientID() {
        return getString(props.get(key.Facebook_CLIENT_ID));
    }

    @Override
    public String getFacebookClientSecret() {
        return getString(props.get(key.Facebook_CLIENT_SECRET));
    }
}