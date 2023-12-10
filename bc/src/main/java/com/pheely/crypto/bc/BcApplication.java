package com.pheely.crypto.bc;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.asn1.cms.SignerInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.OutputEncryptor;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.crypto.Cipher;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;

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
            // Verify unlimited strength jurisdiction policy enabled
            logger.info("Max key size for AES: " + Cipher.getMaxAllowedKeyLength("AES"));
            loadCertAndKeystore();

            // CMS/PKCS7 encryption
            byte[] encrypted = encryptData("1234-5678-9012-3456".getBytes(StandardCharsets.UTF_8),
					(X509Certificate) certificate);
            logger.info("ciphertext: " + Base64.getEncoder().encodeToString(encrypted));

            // CMS/PKCS7 decryption
            byte[] decrypted = decryptData(encrypted, privateKey);
            logger.info("plaintext: " + new String(decrypted, StandardCharsets.UTF_8));

            // CMS/PKCS7 Signature / verification
            byte[] signed = signData("here is a message".getBytes(StandardCharsets.UTF_8), (X509Certificate) certificate, privateKey);
            logger.info("signed data: " + Base64.getEncoder().encodeToString(signed));
            logger.info("Data verified: " + verifySignedData(signed));
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

    private byte[] signData(byte[] data, X509Certificate signingCert, PrivateKey signingKey)
            throws CertificateEncodingException, OperatorCreationException, CMSException, IOException {
        List<X509Certificate> certList = new ArrayList<>();
        certList.add(signingCert);
        Store certs = new JcaCertStore(certList);

        CMSTypedData cmsData = new CMSProcessableByteArray(data);

        CMSSignedDataGenerator cmsSignedDataGenerator = new CMSSignedDataGenerator();
        ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256withRSA").build(signingKey);
        cmsSignedDataGenerator.addSignerInfoGenerator(
                new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder().setProvider("BC").build())
                        .build(contentSigner, signingCert));
        cmsSignedDataGenerator.addCertificates(certs);

        CMSSignedData cmsSignedData = cmsSignedDataGenerator.generate(cmsData, true);
        return  cmsSignedData.getEncoded();
    }

    private boolean verifySignedData(byte[] signedData) throws IOException, CMSException, OperatorCreationException, CertificateException {
        X509Certificate signCert = null;
        ByteArrayInputStream inputStream = new ByteArrayInputStream(signedData);
        ASN1InputStream asn1InputStream = new ASN1InputStream(inputStream);
        CMSSignedData cmsSignedData = new CMSSignedData(ContentInfo.getInstance(asn1InputStream.readObject()));

        Store certs = cmsSignedData.getCertificates();
        SignerInformationStore signers = cmsSignedData.getSignerInfos();
        SignerInformation signer = signers.getSigners().iterator().next();
        Collection<X509CertificateHolder> certCollection = certs.getMatches(signer.getSID());
        X509CertificateHolder certificateHolder = certCollection.iterator().next();
        return signer.verify(new JcaSimpleSignerInfoVerifierBuilder().build(certificateHolder));
    }
}
