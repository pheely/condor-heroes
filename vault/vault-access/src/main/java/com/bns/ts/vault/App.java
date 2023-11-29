package com.bns.ts.vault;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.vault.authentication.TokenAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.Versioned;

@SpringBootApplication
public class App implements CommandLineRunner {

  Logger logger = LoggerFactory.getLogger(App.class);

  public static void main(String... args) {
    SpringApplication.run(App.class, args);
  }

  @Override
  public void run(String... args) throws Exception {
    VaultEndpoint vaultEndpoint = new VaultEndpoint();

    vaultEndpoint.setHost("127.0.0.1");
    vaultEndpoint.setPort(8200);
    vaultEndpoint.setScheme("http");

    VaultTemplate vaultTemplate = new VaultTemplate(
        vaultEndpoint,
        new TokenAuthentication("hvs.AxtQtlY5FF2AKUBguFkGOObn")
    );

    Map<String, String> data = new HashMap<>();
    data.put("password", "Hashi123");

    Versioned.Metadata createResponse =
        vaultTemplate.opsForVersionedKeyValue("secret")
            .put("my-secret-password", data);

    logger.info("Secret written successfully.");

    Versioned<Map<String, Object>> readResponse =
        vaultTemplate.opsForVersionedKeyValue("secret")
            .get("my-secret-password");

    String password = "";
    if (readResponse != null && readResponse.hasData()) {
      password = (String) readResponse.getData().get("password");
    }

    if (!password.equals("Hashi123")) {
      throw new Exception("Unexpected password");
    }

    logger.info("Access granted");

//    readFromUAT();
  }

  /*
  This call fails. It seems a BNS issued trusted store is required.

  Caused by: org.springframework.web.client.ResourceAccessException: I/O error on GET request for
  "https://lb.vault.nonprod.bns:8200/v1/secret/data/token-pipeline-service-perimeter/uat/token-pipeline-mcopy/serviceAccount":
  PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException:
  unable to find valid certification path to requested target; nested exception is javax.net.ssl.SSLHandshakeException:
  PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException:
  unable to find valid certification path to requested target
	at org.springframework.web.client.RestTemplate.doExecute(RestTemplate.java:785) ~[spring-web-5.3.4.jar:5.3.4]
   */
  private void readFromUAT() throws Exception {
    VaultEndpoint vaultEndpoint = new VaultEndpoint();

    vaultEndpoint.setHost("lb.vault.nonprod.bns");
    vaultEndpoint.setPort(8200);
    vaultEndpoint.setScheme("https");

    VaultTemplate vaultTemplate = new VaultTemplate(
        vaultEndpoint,
        new TokenAuthentication(
            "hvs.CAESIDj_ZDBApdo74WAckBUY9fumHT6hDUKQHAbBQlwyKmPAGiQKHGh2cy5obW04QklEa1FTWWhVNWptVldidHJPaVAQxYP6iAI")
    );

    Versioned<Map<String, Object>> readResponse =
        vaultTemplate.opsForVersionedKeyValue("secret")
            .get(
                "token-pipeline-service-perimeter/uat/token-pipeline-mcopy/serviceAccount");

    String password = "";
    if (readResponse != null && readResponse.hasData()) {
      password = (String) readResponse.getData().get("privateKey");
    }

    logger.info("Access granted: " + password);
  }
}
