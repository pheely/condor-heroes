package com.bns.ts.fpe;

import com.google.common.io.BaseEncoding;
import com.google.privacy.dlp.v2.CryptoKey;
import com.google.privacy.dlp.v2.DeidentifyContentResponse;
import com.google.privacy.dlp.v2.FieldId;
import com.google.privacy.dlp.v2.KmsWrappedCryptoKey;
import com.google.privacy.dlp.v2.Table;
import com.google.privacy.dlp.v2.Table.Row;
import com.google.privacy.dlp.v2.Value;
import com.google.protobuf.ByteString;
import java.util.ArrayList;
import java.util.List;


public class App {

  public static void main(String[] args) throws Exception {
    String projectId = "ibcwe-event-layer-f3ccf6d9";
    String kmsCryptoKeyName = "projects/ibcwe-event-layer-f3ccf6d9/locations/global/keyRings/fpe-poc-keyring/cryptoKeys/fpe-poc-key";
    String wrappedAesKey = "CiQA+yi9TI2anWIJ9WGrZe4zeee9FQgFunT1YXafzLcWGgWQZj8SSQCfIIegX+iV9gc7NArQqZAM7GJ5CQMJjTv3r7PyvbgRfORvw2vNOi2pdcWg6BlOih7u8qfleQQR+3ulFad1+fpz99sS/vXIcgc=";

    Deidentifier deidentifier = new Deidentifier(projectId);
    Reidentifier reidentifier = new Reidentifier(projectId);

    CryptoKey cryptoKey = buildWrappedDEK(kmsCryptoKeyName, wrappedAesKey);

    deidentifyTableWithFPE(deidentifier, reidentifier, cryptoKey);
    tokenizeWithDeterministicEncryption(deidentifier, cryptoKey);
    deidentifyTableWithDeterministc(deidentifier, cryptoKey);
  }

  public static void deidentifyTableWithFPE(Deidentifier deidentifier, Reidentifier reidentifier, CryptoKey cryptoKey) {
    Table tableToDeIdentify =
        Table.newBuilder()
            .addHeaders(FieldId.newBuilder().setName("Employee ID").build())
            .addHeaders(FieldId.newBuilder().setName("Date").build())
            .addHeaders(FieldId.newBuilder().setName("Compensation").build())
            .addRows(
                Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("11111").build())
                    .addValues(Value.newBuilder().setStringValue("2015").build())
                    .addValues(Value.newBuilder().setStringValue("$10").build())
                    .build())
            .addRows(
                Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("22222").build())
                    .addValues(Value.newBuilder().setStringValue("2016").build())
                    .addValues(Value.newBuilder().setStringValue("$20").build())
                    .build())
            .addRows(
                Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("33333").build())
                    .addValues(Value.newBuilder().setStringValue("2016").build())
                    .addValues(Value.newBuilder().setStringValue("$15").build())
                    .build())
            .build();

    System.out.println("Table before fpe: \n" + tableToDeIdentify);

    // Fields to be de-identified
    List<FieldId> fields = new ArrayList<>();
    fields.add(FieldId.newBuilder().setName("Employee ID").build());

    try {
      DeidentifyContentResponse res =
          deidentifier.deIdentifyTableWithFpe(tableToDeIdentify, fields, cryptoKey);
      System.out.println("Table after fpe: \n" + res.getItem().getTable());

      Table reidenitifiedTable =
          reidentifier.reidentifyTableWithFpe(res.getItem().getTable(),
              fields, cryptoKey);
      System.out.println("Table after reidentification: \n" + reidenitifiedTable);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  public static void deidentifyTableWithDeterministc(Deidentifier deidentifier,
      CryptoKey cryptoKey) {
    Table tableToDeIdentify =
        Table.newBuilder()
            .addHeaders(FieldId.newBuilder().setName("Employee ID").build())
            .addHeaders(FieldId.newBuilder().setName("Date").build())
            .addHeaders(FieldId.newBuilder().setName("Compensation").build())
            .addRows(
                Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("11111").build())
                    .addValues(Value.newBuilder().setStringValue("2015").build())
                    .addValues(Value.newBuilder().setStringValue("$10").build())
                    .build())
            .addRows(
                Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("22222").build())
                    .addValues(Value.newBuilder().setStringValue("2016").build())
                    .addValues(Value.newBuilder().setStringValue("$20").build())
                    .build())
            .addRows(
                Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("33333").build())
                    .addValues(Value.newBuilder().setStringValue("2016").build())
                    .addValues(Value.newBuilder().setStringValue("$15").build())
                    .build())
            .build();

    System.out.println("Table before fpe: \n" + tableToDeIdentify);

    // Fields to be de-identified
    List<FieldId> fields = new ArrayList<>();
    fields.add(FieldId.newBuilder().setName("Employee ID").build());

    try {
      DeidentifyContentResponse res =
          deidentifier.deIdentifyTableWithDeterministic(tableToDeIdentify, fields,
              cryptoKey);
      System.out.println("Table after fpe: \n" + res.getItem().getTable());

    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  public static void tokenizeWithDeterministicEncryption(Deidentifier deidentifier, CryptoKey cryptoKey) {
    String plaintext = "plain text: plj";
    String token;
    try {
      token = deidentifier.deIdentifyWithDeterministicEncryption(plaintext, cryptoKey);
      System.out.println("Plaintext: " + plaintext + " token: " + token);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  private static final CryptoKey buildWrappedDEK(String kmsKeyName,
      String wrappedAesKey) {
    // Specify an encrypted AES-256 key and the name of the Cloud KMS key that encrypted it
    KmsWrappedCryptoKey kmsWrappedCryptoKey =
        KmsWrappedCryptoKey.newBuilder()
            .setWrappedKey(ByteString.copyFrom(BaseEncoding.base64().decode(wrappedAesKey)))
            .setCryptoKeyName(kmsKeyName)
            .build();
    return CryptoKey.newBuilder().setKmsWrapped(kmsWrappedCryptoKey).build();
  }
}