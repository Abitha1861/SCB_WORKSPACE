package com.hdsoft.common;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.security.KeyStore;

public class LdapSslUtil {

    public static SSLSocketFactory getSocketFactory(String certPath) throws Exception {
        // Load the certificate
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        try (FileInputStream fis = new FileInputStream(certPath)) {
            trustStore.load(fis, null); // No password required for a certificate
        }

        // Initialize TrustManagerFactory with the loaded certificate
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);

        // Create an SSL context with the custom TrustManager
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagerFactory.getTrustManagers(), new java.security.SecureRandom());

        return sslContext.getSocketFactory();
    }
}

