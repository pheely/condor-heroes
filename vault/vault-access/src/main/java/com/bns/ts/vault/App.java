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
  }

}