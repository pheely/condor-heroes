package com.pheely.crypto.bc;

import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.bouncycastle.cms.jcajce.JceKeyTransEnvelopedRecipient;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipient;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OutputEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.crypto.Cipher;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Collection;

@SpringBootApplication
public class BcApplication implements CommandLineRunner {
    static {
        Security.setProperty("crypto.policy", "unlimited");
        Security.addProvider(new BouncyCastleProvider());
    }

    Logger logger = LoggerFactory.getLogger(BcApplication.class);
    private Certificate certificate;
    private PrivateKey privateKey;

    public static void main(String[] args) {
        SpringApplication.run(BcApplication.class, args);
    }

    @Override
    public void run(String... args) {
        try {
            logger.info("Max key size for AES: " + Cipher.getMaxAllowedKeyLength("AES"));
            loadCertAndKeystore();

            byte[] encrypted = encryptData("1234-5678-9012-3456".getBytes(StandardCharsets.UTF_8),
					(X509Certificate) certificate);
            logger.info(Base64.getEncoder().encodeToString(encrypted));

            byte[] decrypted = decryptData(encrypted, privateKey);
            logger.info(new String(decrypted, StandardCharsets.UTF_8));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loadCertAndKeystore() throws Exception {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509", "BC");

        certificate = certificateFactory.generateCertificate(new FileInputStream("dev.crt"));

        char[] keyStorePassword = "changeit".toCharArray();
        char[] keyPassword = "changeit".toCharArray();

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(new FileInputStream("pheely-dev.p12"), keyStorePassword);
        privateKey = (PrivateKey) keyStore.getKey("pheely-dev", keyPassword);

        logger.info("Cert and privateKey loaded successfully");
    }

    private byte[] encryptData(byte[] data, X509Certificate encryptionCert)
			throws CertificateEncodingException, CMSException, IOException {
        byte[] ciphertext = null;
        if (null != data && null != encryptionCert) {
            CMSEnvelopedDataGenerator cmsEnvelopedDataGenerator = new CMSEnvelopedDataGenerator();
            JceKeyTransRecipientInfoGenerator jceKey = new JceKeyTransRecipientInfoGenerator(encryptionCert);
            cmsEnvelopedDataGenerator.addRecipientInfoGenerator(jceKey);
            CMSTypedData msg = new CMSProcessableByteArray(data);
            OutputEncryptor encryptor =
                    new JceCMSContentEncryptorBuilder(CMSAlgorithm.AES128_GCM).setProvider("BC").build();
            CMSEnvelopedData cmsEnvelopedData = cmsEnvelopedDataGenerator.generate(msg, encryptor);
            ciphertext = cmsEnvelopedData.getEncoded();
        }
        return ciphertext;
    }

    private byte[] decryptData(byte[] data, PrivateKey decryptionKey) throws CMSException {
        byte[] plaintext = null;

        if (null != data && null != decryptionKey) {
            CMSEnvelopedData cmsEnvelopedData = new CMSEnvelopedData(data);

            Collection<RecipientInformation> recipients = cmsEnvelopedData.getRecipientInfos().getRecipients();
            KeyTransRecipientInformation recipientInformation =
                    (KeyTransRecipientInformation) recipients.iterator().next();
            JceKeyTransRecipient recipient = new JceKeyTransEnvelopedRecipient(decryptionKey);
            plaintext = recipientInformation.getContent(recipient);
        }
        return plaintext;
    }
}
