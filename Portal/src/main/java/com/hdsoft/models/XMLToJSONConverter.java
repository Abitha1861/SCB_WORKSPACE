package com.hdsoft.models;
import org.json.JSONObject;
import org.json.XML;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.google.gson.JsonObject;
import com.hdsoft.common.Common_Utils;

public class XMLToJSONConverter 
{
	
	public static void main(String args[])
	{
		XMLToJSONConverter x = new XMLToJSONConverter();
		
		String Message = "<ns3:notifyInternalAccountsTransactionRequest xmlns=\"http://www.sc.com/SCBML-1\" xmlns:ns2=\"http://schema.ebbs.scb/RTA/\" xmlns:ns3=\"http://www.sc.com/coreBanking/v1/TransactionAlert\"><header><messageDetails><messageVersion>1.0</messageVersion><messageType><typeName>CoreBanking:TransactionAlert</typeName><subType><subTypeName>notifyInternalAccountsTransaction</subTypeName></subType></messageType></messageDetails><originationDetails><messageSender><messageSender>eBBS</messageSender><senderDomain><domainName>CoreBanking</domainName><subDomainName subdomainNameScheme=\"\"/></senderDomain><countryCode>TZ</countryCode></messageSender><initiatedTimestamp>2024-08-16T13:51:19.643+03:00</initiatedTimestamp><trackingId>202310050358708</trackingId><correlationID>202310050358708</correlationID><encoding>utf-8</encoding><possibleDuplicate>false</possibleDuplicate></originationDetails><process><processName>notifyInternalAccountsTransaction</processName><eventType>notify</eventType></process><metadata><tag><key/></tag></metadata></header><notifyInternalAccountsTransactionReqPayload><ns2:notifyInternalAccountsTransactionReq><ns2:TransactionDetails><ns2:TransactionEntry><ns2:TransactionBranch>08700</ns2:TransactionBranch><ns2:ChannelID>0008</ns2:ChannelID><ns2:BatchEntryDate>2023-10-05T00:00:00.000+03:00</ns2:BatchEntryDate><ns2:BatchNo>8948815</ns2:BatchNo><ns2:SeqNo>0000001</ns2:SeqNo><ns2:Account><ns2:CurrencyCode>TZS</ns2:CurrencyCode><ns2:AccountNo>0120599006100</ns2:AccountNo><ns2:ShortName>XXX</ns2:ShortName><ns2:AcctCategory>N</ns2:AcctCategory></ns2:Account><ns2:AccountBranch>08700</ns2:AccountBranch><ns2:TrnCode><ns2:Code>NC1</ns2:Code><ns2:Desc>Nostro/Clrng Susp credit-Corporate(S2B) Channel</ns2:Desc></ns2:TrnCode><ns2:TrnTypecode>ROL</ns2:TrnTypecode><ns2:TrnAmount>1.00</ns2:TrnAmount><ns2:CreditDebitFlag>C</ns2:CreditDebitFlag><ns2:ValueDate>2024-08-16T00:00:00.000+03:00</ns2:ValueDate><ns2:ErrorAccountFlag>N</ns2:ErrorAccountFlag><ns2:Narrations><ns2:Narration1>TZ1OL2408160000P</ns2:Narration1><ns2:Narration2>ACTZTZTZXXX</ns2:Narration2><ns2:Narration3>0106021700600|TZ1OL2408160000P</ns2:Narration3><ns2:Narration4>9D0X1I1Z532Z3131</ns2:Narration4></ns2:Narrations><ns2:ExtendedNarrations/><ns2:ProductCode>205</ns2:ProductCode><ns2:SegmentCode>95</ns2:SegmentCode><ns2:MasterNo>990061</ns2:MasterNo><ns2:GLDeptID>7251</ns2:GLDeptID><ns2:CustomerRate>1.000000000</ns2:CustomerRate><ns2:LCYEquivalent>1.00</ns2:LCYEquivalent><ns2:SysGenFlag>N</ns2:SysGenFlag><ns2:ForcePostFlag>N</ns2:ForcePostFlag><ns2:AppInsFlag>N</ns2:AppInsFlag><ns2:FinancialTrnflag>Y</ns2:FinancialTrnflag><ns2:IntAcEntryFlag>Y</ns2:IntAcEntryFlag><ns2:AdvTreasuryFlag>N</ns2:AdvTreasuryFlag><ns2:MakerDetails><ns2:ID>SCPAY</ns2:ID><ns2:Branch>08700</ns2:Branch><ns2:IPAddress>1.1.1.0</ns2:IPAddress><ns2:Date>2024-08-16T00:00:00.000+03:00</ns2:Date><ns2:Time>13:51:19.000+03:00</ns2:Time></ns2:MakerDetails><ns2:CheckerDetails><ns2:ID>SCPAY</ns2:ID><ns2:Branch>08700</ns2:Branch><ns2:IPAddress>1.1.1.0</ns2:IPAddress><ns2:Date>2024-08-16T00:00:00.000+03:00</ns2:Date><ns2:Time>13:51:19.000+03:00</ns2:Time></ns2:CheckerDetails><ns2:ApproverDetails><ns2:ID>SCPAY</ns2:ID><ns2:Branch>08700</ns2:Branch><ns2:IPAddress>1.1.1.0</ns2:IPAddress><ns2:Date>2024-08-16T00:00:00.000+03:00</ns2:Date><ns2:Time>13:51:19.000+03:00</ns2:Time></ns2:ApproverDetails><ns2:Statusflag>57</ns2:Statusflag><ns2:SysEntryFlag>N</ns2:SysEntryFlag><ns2:Reversalflag>N</ns2:Reversalflag><ns2:TrnReference><ns2:CurrencyCode>TZS</ns2:CurrencyCode><ns2:AccountNo>0120599006100</ns2:AccountNo></ns2:TrnReference><ns2:ExternalSystemKey>e634f5ba-e886-47ae-932c-bace9e060131_SCP_MTN_C</ns2:ExternalSystemKey><ns2:ApprvSeqno>202310050358708</ns2:ApprvSeqno><ns2:BalanceDetails><ns2:Ledger>10010078794477949.37</ns2:Ledger><ns2:AvailableWithOutLimit>-129686041422.33</ns2:AvailableWithOutLimit><ns2:AvailableWithAccountLimit>-129686041422.33</ns2:AvailableWithAccountLimit><ns2:AvailableWithLimit>-129686041422.33</ns2:AvailableWithLimit><ns2:AvailableWithIntradayLimit>-129686041422.33</ns2:AvailableWithIntradayLimit><ns2:AvailableWithDAUELimit>-129686041422.33</ns2:AvailableWithDAUELimit><ns2:AvailableWithCODLimit>-129686041422.33</ns2:AvailableWithCODLimit><ns2:TotalUnclearedEffects>10010191742925889.26</ns2:TotalUnclearedEffects></ns2:BalanceDetails><ns2:TransactionLimit><ns2:AvailableLimit>0.00</ns2:AvailableLimit></ns2:TransactionLimit><ns2:RelDetails><ns2:RelationshipNo>999999999</ns2:RelationshipNo><ns2:PrefCardName>XXX</ns2:PrefCardName><ns2:RelContactDetails><ns2:contact><ns2:ContactTypeCode>TRS</ns2:ContactTypeCode><ns2:Contact>XXX</ns2:Contact></ns2:contact><ns2:contact><ns2:ContactTypeCode>FOF</ns2:ContactTypeCode><ns2:Contact>XXX</ns2:Contact></ns2:contact><ns2:contact><ns2:ContactTypeCode>SWF</ns2:ContactTypeCode><ns2:Contact>SCBLTZS0XXX</ns2:Contact></ns2:contact><ns2:contact><ns2:ContactTypeCode>S01</ns2:ContactTypeCode><ns2:Contact>SCBLTZS0XXX</ns2:Contact></ns2:contact><ns2:contact><ns2:ContactTypeCode>S02</ns2:ContactTypeCode><ns2:Contact>SCBLTZS0XXX</ns2:Contact></ns2:contact><ns2:contact><ns2:ContactTypeCode>S03</ns2:ContactTypeCode><ns2:Contact>SCBLTZS0XXX</ns2:Contact></ns2:contact><ns2:contact><ns2:ContactTypeCode>S04</ns2:ContactTypeCode><ns2:Contact>SCBLTZS0XXX</ns2:Contact></ns2:contact><ns2:contact><ns2:ContactTypeCode>S05</ns2:ContactTypeCode><ns2:Contact>SCBLTZS0XXX</ns2:Contact></ns2:contact><ns2:contact><ns2:ContactTypeCode>S06</ns2:ContactTypeCode><ns2:Contact>SCBLTZS0XXX</ns2:Contact></ns2:contact><ns2:contact><ns2:ContactTypeCode>S07</ns2:ContactTypeCode><ns2:Contact>SCBLTZS0XXX</ns2:Contact></ns2:contact><ns2:contact><ns2:ContactTypeCode>S08</ns2:ContactTypeCode><ns2:Contact>SCBLTZS0XXX</ns2:Contact></ns2:contact><ns2:contact><ns2:ContactTypeCode>S09</ns2:ContactTypeCode><ns2:Contact>SCBLTZS0XXX</ns2:Contact></ns2:contact><ns2:contact><ns2:ContactTypeCode>S10</ns2:ContactTypeCode><ns2:Contact>SCBLTZS0XXX</ns2:Contact></ns2:contact><ns2:contact><ns2:ContactTypeCode>S11</ns2:ContactTypeCode><ns2:Contact>SCBLTZS0XXX</ns2:Contact></ns2:contact><ns2:contact><ns2:ContactTypeCode>S12</ns2:ContactTypeCode><ns2:Contact>SCBLTZS0XXX</ns2:Contact></ns2:contact><ns2:contact><ns2:ContactTypeCode>S13</ns2:ContactTypeCode><ns2:Contact>SCBLTZS0XXX</ns2:Contact></ns2:contact><ns2:contact><ns2:ContactTypeCode>S14</ns2:ContactTypeCode><ns2:Contact>SCBLTZS0XXX</ns2:Contact></ns2:contact><ns2:contact><ns2:ContactTypeCode>S15</ns2:ContactTypeCode><ns2:Contact>SCBLTZS0XXX</ns2:Contact></ns2:contact><ns2:contact><ns2:ContactTypeCode>S16</ns2:ContactTypeCode><ns2:Contact>SCBLTZS0XXX</ns2:Contact></ns2:contact><ns2:contact><ns2:ContactTypeCode>S17</ns2:ContactTypeCode><ns2:Contact>SCBLTZS0XXX</ns2:Contact></ns2:contact><ns2:contact><ns2:ContactTypeCode>S18</ns2:ContactTypeCode><ns2:Contact>SCBLTZS0XXX</ns2:Contact></ns2:contact><ns2:contact><ns2:ContactTypeCode>S19</ns2:ContactTypeCode><ns2:Contact>SCBLTZS0XXX</ns2:Contact></ns2:contact><ns2:contact><ns2:ContactTypeCode>S20</ns2:ContactTypeCode><ns2:Contact>SCBLTZS0XXX</ns2:Contact></ns2:contact><ns2:contact><ns2:ContactTypeCode>S21</ns2:ContactTypeCode><ns2:Contact>SCBLTZS0XXX</ns2:Contact></ns2:contact><ns2:contact><ns2:ContactTypeCode>S22</ns2:ContactTypeCode><ns2:Contact>SCBLTZS0XXX</ns2:Contact></ns2:contact><ns2:contact><ns2:ContactTypeCode>S23</ns2:ContactTypeCode><ns2:Contact>SCBLTZS0XXX</ns2:Contact></ns2:contact><ns2:contact><ns2:ContactTypeCode>S24</ns2:ContactTypeCode><ns2:Contact>SCBLTZS0XXX</ns2:Contact></ns2:contact><ns2:contact><ns2:ContactTypeCode>S25</ns2:ContactTypeCode><ns2:Contact>SCBLTZS0XXX</ns2:Contact></ns2:contact><ns2:contact><ns2:ContactTypeCode>S26</ns2:ContactTypeCode><ns2:Contact>SCBLTZS0XXX</ns2:Contact></ns2:contact><ns2:contact><ns2:ContactTypeCode>S27</ns2:ContactTypeCode><ns2:Contact>SCBLTZS0XXX</ns2:Contact></ns2:contact></ns2:RelContactDetails></ns2:RelDetails><ns2:SwiftAddress>SCBLTZS0XXX</ns2:SwiftAddress><ns2:CurrencyNo>01</ns2:CurrencyNo><ns2:LinkedAccounts/><ns2:Paydtl/></ns2:TransactionEntry></ns2:TransactionDetails></ns2:notifyInternalAccountsTransactionReq></notifyInternalAccountsTransactionReqPayload></ns3:notifyInternalAccountsTransactionRequest>";
		
		x.ConvertToJson(Message);
	}
    public JsonObject ConvertToJson(String Message) 
    {
    	JsonObject obj = new JsonObject();
    	
        try 
        {
            // Sample XML string (Make sure this XML is well-formed)
            String xmlString = Message;
            
            xmlString = xmlString.replaceAll("\\s*xmlns[^>]*?>", ">");
            xmlString = xmlString.replaceAll("<[^>]*?xml[^>]*?>", "");
            xmlString = xmlString.replaceAll("</[^>]*?:", "</");
            xmlString = xmlString.replaceAll("<[^>]*?:","<");  
            
            // Convert XML String to Document
            Document document = convertStringToDocument(xmlString);

            // Remove Namespace Prefixes
            removeNamespacePrefixes(document.getDocumentElement());

            // Convert Document to String
            String xmlAsString = convertDocumentToString(document);

            // Convert XML String to JSON
            JSONObject json = XML.toJSONObject(xmlAsString);

            // Output JSON
           // System.out.println(json.toString(4)); // Pretty print JSON with indentation level 4
            
            String jsonString = json.toString();  
			
            obj = new Common_Utils().StringToJsonObject(jsonString);
            
            System.out.println(obj); 

        }
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        
        return obj;
    }

    private static Document convertStringToDocument(String xmlString) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true); // Ensure the factory is namespace-aware
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xmlString));
        return builder.parse(is);
    }

    private static void removeNamespacePrefixes(Node node) {
        // Remove namespace prefixes from element and its children
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) node;
            String localName = element.getLocalName();
            if (localName != null) {
                Node parent = element.getParentNode();
                if (parent != null) {
                    Document doc = element.getOwnerDocument();
                    // Create a new element without namespace
                    Element newElement = doc.createElement(localName);
                    
                    // Copy attributes
                    for (int i = 0; i < element.getAttributes().getLength(); i++) {
                        newElement.setAttribute(
                                element.getAttributes().item(i).getNodeName(),
                                element.getAttributes().item(i).getNodeValue()
                        );
                    }

                    // Copy child nodes
                    while (element.hasChildNodes()) {
                        Node child = element.getFirstChild();
                        element.removeChild(child);
                        newElement.appendChild(child);
                    }

                    // Replace old element with new element
                    parent.replaceChild(newElement, element);

                    // Recursively process child elements
                    for (int i = 0; i < newElement.getChildNodes().getLength(); i++) {
                        removeNamespacePrefixes(newElement.getChildNodes().item(i));
                    }
                }
            }
        }
    }

    private static String convertDocumentToString(Document doc) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        transformer.transform(source, result);
        return writer.toString();
    }
}



