package com.bns.ts.vault;

import java.io.IOException;

import org.springframework.context.annotation.Configuration;
import org.springframework.vault.authentication.ClientAuthentication;
import org.springframework.vault.authentication.GcpIamCredentialsAuthentication;
import org.springframework.vault.authentication.GcpIamCredentialsAuthenticationOptions;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.config.AbstractVaultConfiguration;

import com.google.auth.oauth2.GoogleCredentials;

@Configuration
public class AppIAMConfig extends AbstractVaultConfiguration {
    @Override
    public ClientAuthentication clientAuthentication() {
        try {
            GcpIamCredentialsAuthenticationOptions options = GcpIamCredentialsAuthenticationOptions.builder()
            .role("vault-iam-auth-role").credentials(GoogleCredentials.getApplicationDefault()).build();
        
            GcpIamCredentialsAuthentication authentication = new GcpIamCredentialsAuthentication(options, restOperations());

            return authentication;
        } catch (IOException ex) {
            return null;
        }
    }

    @Override
    public VaultEndpoint vaultEndpoint() {
        VaultEndpoint vaultEndpoint = new VaultEndpoint();

        vaultEndpoint.setHost("b53c-35-222-242-37.ngrok.io");
        vaultEndpoint.setPort(443);
        vaultEndpoint.setScheme("https");

        return vaultEndpoint;
    }

}
