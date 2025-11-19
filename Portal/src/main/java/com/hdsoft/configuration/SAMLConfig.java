package com.hdsoft.configuration;

/*
public class SAMLConfig {

    static {
        try {
            DefaultBootstrap.bootstrap();
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
    }

    public static void loadIDPMetadata(InputStream metadataStream) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            org.w3c.dom.Document document = factory.newDocumentBuilder().parse(metadataStream);
            MetadataProvider provider = new BasicParserPool().parse(document);
            EntityDescriptor entityDescriptor = (EntityDescriptor) provider.getEntityDescriptor();
            System.out.println("IDP Entity ID: " + entityDescriptor.getEntityID());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
*/