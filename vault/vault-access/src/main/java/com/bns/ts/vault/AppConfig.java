package com.bns.ts.vault;

import org.springframework.context.annotation.Configuration;
import org.springframework.vault.authentication.ClientAuthentication;
import org.springframework.vault.authentication.GcpComputeAuthentication;
import org.springframework.vault.authentication.GcpComputeAuthenticationOptions;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.config.AbstractVaultConfiguration;

@Configuration
public class AppConfig extends AbstractVaultConfiguration {
    @Override
    public ClientAuthentication clientAuthentication() {
        GcpComputeAuthenticationOptions options = GcpComputeAuthenticationOptions.builder().role("vault-gce-auth-role").build();
        GcpComputeAuthentication authentication = new GcpComputeAuthentication(options, restOperations());
        return authentication;
    }

    @Override
    public VaultEndpoint vaultEndpoint() {
        VaultEndpoint vaultEndpoint = new VaultEndpoint();

        vaultEndpoint.setHost("9039-35-222-242-37.ngrok.io");
        vaultEndpoint.setPort(443);
        vaultEndpoint.setScheme("https");

        return vaultEndpoint;
    }

}
