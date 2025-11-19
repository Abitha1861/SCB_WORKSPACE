package com.hdsoft.common;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import javax.net.ssl.SSLSocketFactory;

import org.springframework.beans.factory.annotation.Value;

public class CustomSSLSocketFactory extends SSLSocketFactory {

	@Value("${spring.ldap.certificate}")
	private static String ldapcertificate;
	
    private static SSLSocketFactory socketFactory;

    static {
        try {
            System.out.println("ldap cert: "+ldapcertificate);
            socketFactory = LdapSslUtil.getSocketFactory(ldapcertificate);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize SSL context", e);
        }
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return socketFactory.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return socketFactory.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        return socketFactory.createSocket(s, host, port, autoClose);
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        return socketFactory.createSocket(host, port);
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
        return socketFactory.createSocket(host, port, localHost, localPort);
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return socketFactory.createSocket(host, port);
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return socketFactory.createSocket(address, port, localAddress, localPort);
    }
}