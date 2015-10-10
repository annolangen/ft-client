package net.anno;

import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.fusiontables.Fusiontables;
import com.google.api.services.fusiontables.FusiontablesScopes;
import com.google.api.services.fusiontables.model.Sqlresponse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

/**
 * Hello world!
 */
public class FtClient {
  Fusiontables fusiontables;

  public FtClient(Fusiontables fusiontables) {
    this.fusiontables = fusiontables;
  }

  void run(Reader reader, PrintStream out) throws IOException {
    try (BufferedReader in = new BufferedReader(reader)) {
      for (String line; (line = in.readLine()) != null; ) {
        Fusiontables.Query.SqlGet sqlGet = fusiontables.query().sqlGet(line);
        Sqlresponse sqlresponse = sqlGet.execute();
        out.println(sqlresponse.getColumns());
        for (List<Object> list : sqlresponse.getRows()) {
          out.println(list);
        }
      }
    }
  }

  private static Fusiontables getFusiontables() throws IOException, GeneralSecurityException {
    File dataDirectory = new File(System.getProperty("user.home"), ".store/ft_client");
    if (!dataDirectory.exists()) {
      dataDirectory.mkdirs();
    }
    File secretsFile = new File(dataDirectory, "client_secrets.json");
    if (!secretsFile.exists()) {
      System.out.println(
          "Goto https://code.google.com/apis/console/?api=fusiontables and copy "
              + "resulting secret JSON file into "
              + secretsFile.getPath());
      System.exit(1);
    }
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    // load client secrets
    GoogleClientSecrets clientSecrets =
        GoogleClientSecrets.load(jsonFactory, new FileReader(secretsFile));
    HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    DataStoreFactory dataStoreFactory = new FileDataStoreFactory(dataDirectory);
    // set up authorization code flow
    GoogleAuthorizationCodeFlow flow =
        new GoogleAuthorizationCodeFlow.Builder(
                httpTransport,
                jsonFactory,
                clientSecrets,
                Collections.singleton(FusiontablesScopes.FUSIONTABLES))
            .setDataStoreFactory(dataStoreFactory)
            .build();
    HttpRequestInitializer httpRequestInitializer =
        new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    return new Fusiontables.Builder(httpTransport, jsonFactory, httpRequestInitializer)
        .setApplicationName("FtClient/1.0")
        .build();
  }

  public static void main(String[] args) throws Exception {
     new FtClient(getFusiontables()).run(new InputStreamReader(System.in), System.out);
  }
}
