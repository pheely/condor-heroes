package com.bns.ts.vault;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.vault.authentication.TokenAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponseSupport;
import org.springframework.vault.support.Versioned;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class App implements CommandLineRunner {
    @Autowired
    private AppConfig appConfig;
    Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String... args) {
        SpringApplication.run(App.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
//        VaultEndpoint vaultEndpoint = new VaultEndpoint();
//
//        vaultEndpoint.setHost("0468-99-229-154-201.ngrok.io");
//        vaultEndpoint.setPort(443);
//        vaultEndpoint.setScheme("https");

//        VaultTemplate vaultTemplate = new VaultTemplate(
//                vaultEndpoint,
//                new TokenAuthentication("hvs.CCOLpHinOR61cPg64nTRrG9y"));

	    try {
        VaultTemplate vaultTemplate = new VaultTemplate(appConfig.vaultEndpoint(), appConfig.clientAuthentication());
	/*
        Secrets secrets = new Secrets();
        secrets.userName = "hello";
        secrets.password = "world";

        // read() and write() only work on kv-v1 which does not support versions
        vaultTemplate.write("secret-v1/my-secrets", secrets);

        VaultResponseSupport<Secrets> response = vaultTemplate.read("secret-v1/my-secrets", Secrets.class);
        logger.info("read secret: " + response.getData().getPassword());
        vaultTemplate.delete("secret-v1/my-secrets");

        // use this for kv-v2 secrets
        Versioned.Metadata createResponse = vaultTemplate.opsForVersionedKeyValue("secret").put("top-secrets", secrets);
        Versioned.Version version = createResponse.getVersion();
        logger.info("Secret written successfully - version: " + version.getVersion());
        */

        Versioned<Map<String, Object>> readResponse = vaultTemplate.opsForVersionedKeyValue("secret").get("top-secret");

	String password = "";
	  if (readResponse != null && readResponse.hasData()) {
      password = (String) readResponse.getData().get("password");
    }
    logger.info("Read secret: "+ password);

    /*
        Map<String, Object> secrets2 = new HashMap<>();
        if (readResponse != null && readResponse.hasData()) {
            secrets2 = readResponse.getData();
        }
        logger.info("Read secrets: " + secrets2);
*/
//    vaultTemplate.opsForVersionedKeyValue("secret").delete("my-secrets", version);
//    vaultTemplate.opsForVersionedKeyValue("secret").destroy("my-secrets", version);
    }catch (Exception ex) {
      ex.printStackTrace();
    }
    }
}
