package com.sugarsnooper.filetransfer.Client;

import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;

public class GetFilesToBeTransferred extends AsyncTask<String, Void , String> {
    String output ;
    public interface AsynResponse {
        void processFinish(String out);
    }

    AsynResponse asynResponse = null;
    public GetFilesToBeTransferred(AsynResponse asynResponse) {
        this.asynResponse = asynResponse;

    }
    private String replace(String str) {
        String[] words = str.split(" ");
        StringBuilder sentence = new StringBuilder(words[0]);

        for (int i = 1; i < words.length; ++i) {
            sentence.append("%20");
            sentence.append(words[i]);
        }

        return sentence.toString();
    }
    @Override
    protected String doInBackground(String... strings) {
        String link = strings[0] + "/request";
        link = replace(link);
        try {
            URL url = new URL(link);
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet();
            request.setURI(new URI(link));
            HttpResponse response = client.execute(request);
            BufferedReader in = new BufferedReader(new
                    InputStreamReader(response.getEntity().getContent()));

            StringBuffer sb = new StringBuffer("");
            String line="";

            while ((line = in.readLine()) != null) {
                sb.append(line);
                break;
            }

            in.close();
            return sb.toString();
        }
        catch (Exception f){
            return "error";
        }
    }

    @Override
    protected void onPostExecute(String s) {
        asynResponse.processFinish(s);
    }
}