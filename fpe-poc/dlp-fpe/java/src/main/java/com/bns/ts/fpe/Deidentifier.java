package com.bns.ts.fpe;

import com.google.cloud.dlp.v2.DlpServiceClient;
import com.google.common.io.BaseEncoding;
import com.google.privacy.dlp.v2.ContentItem;
import com.google.privacy.dlp.v2.CryptoDeterministicConfig;
import com.google.privacy.dlp.v2.CryptoKey;
import com.google.privacy.dlp.v2.CryptoReplaceFfxFpeConfig;
import com.google.privacy.dlp.v2.CryptoReplaceFfxFpeConfig.FfxCommonNativeAlphabet;
import com.google.privacy.dlp.v2.DeidentifyConfig;
import com.google.privacy.dlp.v2.DeidentifyContentRequest;
import com.google.privacy.dlp.v2.DeidentifyContentResponse;
import com.google.privacy.dlp.v2.FieldId;
import com.google.privacy.dlp.v2.FieldTransformation;
import com.google.privacy.dlp.v2.InfoType;
import com.google.privacy.dlp.v2.InfoTypeTransformations;
import com.google.privacy.dlp.v2.InspectConfig;
import com.google.privacy.dlp.v2.KmsWrappedCryptoKey;
import com.google.privacy.dlp.v2.LocationName;
import com.google.privacy.dlp.v2.PrimitiveTransformation;
import com.google.privacy.dlp.v2.RecordTransformations;
import com.google.privacy.dlp.v2.Table;
import com.google.protobuf.ByteString;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
public class Deidentifier {
  private final String projectId;
  public Deidentifier(String projectId) {
    this.projectId = projectId;
  }

  public DeidentifyContentResponse deIdentifyTableWithFpe(Table tableToDeIdentify, List<FieldId> fieldsToDeidentify, CryptoKey cryptoKey)
      throws IOException {
      // Initialize client that will be used to send requests. This client only needs to be created
      // once, and can be reused for multiple requests. After completing all of your requests, call
      // the "close" method on the client to safely clean up any remaining background resources.
      try (DlpServiceClient dlp = DlpServiceClient.create()) {
        // Specify what content you want the service to de-identify.
        ContentItem contentItem = ContentItem.newBuilder().setTable(tableToDeIdentify).build();

        // Specify how the content should be encrypted.
        CryptoReplaceFfxFpeConfig cryptoReplaceFfxFpeConfig =
            CryptoReplaceFfxFpeConfig.newBuilder()
                .setCryptoKey(cryptoKey)
                // Set of characters in the input text. For more info, see
                // https://cloud.google.com/dlp/docs/reference/rest/v2/organizations.deidentifyTemplates#DeidentifyTemplate.FfxCommonNativeAlphabet
                .setCommonAlphabet(FfxCommonNativeAlphabet.NUMERIC)
                .build();
        PrimitiveTransformation primitiveTransformation =
            PrimitiveTransformation.newBuilder()
                .setCryptoReplaceFfxFpeConfig(cryptoReplaceFfxFpeConfig)
                .build();

        // Associate the encryption with the specified field.
        FieldTransformation fieldTransformation =
            FieldTransformation.newBuilder()
                .setPrimitiveTransformation(primitiveTransformation)
                .addAllFields(fieldsToDeidentify)
                .build();
        RecordTransformations transformations =
            RecordTransformations.newBuilder().addFieldTransformations(fieldTransformation).build();

        DeidentifyConfig deidentifyConfig =
            DeidentifyConfig.newBuilder().setRecordTransformations(transformations).build();

        // Combine configurations into a request for the service.
        DeidentifyContentRequest request =
            DeidentifyContentRequest.newBuilder()
                .setParent(LocationName.of(projectId, "global").toString())
                .setItem(contentItem)
                .setDeidentifyConfig(deidentifyConfig)
                .build();

        // Send the request and receive response from the service.
        return dlp.deidentifyContent(request);
      }
  }

  public DeidentifyContentResponse deIdentifyTableWithDeterministic(Table tableToDeIdentify, List<FieldId> fieldsToDeidentify, CryptoKey cryptoKey)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (DlpServiceClient dlp = DlpServiceClient.create()) {
      // Specify what content you want the service to de-identify.
      ContentItem contentItem = ContentItem.newBuilder().setTable(tableToDeIdentify).build();

      // Specify how the content should be encrypted.
      CryptoDeterministicConfig cryptoDeterministicConfig =
          CryptoDeterministicConfig.newBuilder()
              .setCryptoKey(cryptoKey)
              .build();
      PrimitiveTransformation primitiveTransformation =
          PrimitiveTransformation.newBuilder()
              .setCryptoDeterministicConfig(cryptoDeterministicConfig)
              .build();

      // Associate the encryption with the specified field.
      FieldTransformation fieldTransformation =
          FieldTransformation.newBuilder()
              .setPrimitiveTransformation(primitiveTransformation)
              .addAllFields(fieldsToDeidentify)
              .build();
      RecordTransformations transformations =
          RecordTransformations.newBuilder().addFieldTransformations(fieldTransformation).build();

      DeidentifyConfig deidentifyConfig =
          DeidentifyConfig.newBuilder().setRecordTransformations(transformations).build();

      // Combine configurations into a request for the service.
      DeidentifyContentRequest request =
          DeidentifyContentRequest.newBuilder()
              .setParent(LocationName.of(projectId, "global").toString())
              .setItem(contentItem)
              .setDeidentifyConfig(deidentifyConfig)
              .build();

      // Send the request and receive response from the service.
      return dlp.deidentifyContent(request);
    }
  }

  // De-identifies sensitive data in a string using deterministic encryption. The encryption is
  // performed with a wrapped key.
  public String deIdentifyWithDeterministicEncryption(String textToDeIdentify, CryptoKey cryptoKey) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (DlpServiceClient dlp = DlpServiceClient.create()) {
      // Specify what content you want the service to de-identify.
      ContentItem contentItem = ContentItem.newBuilder()
          .setValue(textToDeIdentify)
          .build();

      // Specify the type of info the inspection will look for.
      // See https://cloud.google.com/dlp/docs/infotypes-reference for complete list of info types
      InfoType infoType = InfoType.newBuilder()
          .setName("US_SOCIAL_SECURITY_NUMBER")
          .build();

      InspectConfig inspectConfig = InspectConfig.newBuilder()
          .addAllInfoTypes(Collections.singletonList(infoType))
          .build();

      // Specify how the info from the inspection should be encrypted.
      InfoType surrogateInfoType = InfoType.newBuilder()
          .setName("SSN_TOKEN")
          .build();

      CryptoDeterministicConfig cryptoDeterministicConfig = CryptoDeterministicConfig.newBuilder()
          .setSurrogateInfoType(surrogateInfoType)
          .setCryptoKey(cryptoKey)
          .build();

      PrimitiveTransformation primitiveTransformation = PrimitiveTransformation.newBuilder()
          .setCryptoDeterministicConfig(cryptoDeterministicConfig)
          .build();

      InfoTypeTransformations.InfoTypeTransformation infoTypeTransformation =
          InfoTypeTransformations.InfoTypeTransformation.newBuilder()
              .setPrimitiveTransformation(primitiveTransformation)
              .build();

      InfoTypeTransformations transformations = InfoTypeTransformations.newBuilder()
          .addTransformations(infoTypeTransformation)
          .build();

      DeidentifyConfig deidentifyConfig = DeidentifyConfig.newBuilder()
          .setInfoTypeTransformations(transformations)
          .build();

      // Combine configurations into a request for the service.
      DeidentifyContentRequest request = DeidentifyContentRequest.newBuilder()
          .setParent(LocationName.of(projectId, "global").toString())
          .setItem(contentItem)
          .setInspectConfig(inspectConfig)
          .setDeidentifyConfig(deidentifyConfig)
          .build();

      // Send the request and receive response from the service.
      DeidentifyContentResponse response = dlp.deidentifyContent(request);

      // Print the results.
      System.out.println(
          "Text after de-identification: " + response.getItem().getValue());

      return response.getItem().getValue();
    }
  }
}
