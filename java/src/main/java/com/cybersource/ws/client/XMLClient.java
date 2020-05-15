/*
* Copyright 2003-2014 CyberSource Corporation
*
* THE SOFTWARE AND THE DOCUMENTATION ARE PROVIDED ON AN "AS IS" AND "AS
* AVAILABLE" BASIS WITH NO WARRANTY.  YOU AGREE THAT YOUR USE OF THE SOFTWARE AND THE
* DOCUMENTATION IS AT YOUR SOLE RISK AND YOU ARE SOLELY RESPONSIBLE FOR ANY DAMAGE TO YOUR
* COMPUTER SYSTEM OR OTHER DEVICE OR LOSS OF DATA THAT RESULTS FROM SUCH USE. TO THE FULLEST
* EXTENT PERMISSIBLE UNDER APPLICABLE LAW, CYBERSOURCE AND ITS AFFILIATES EXPRESSLY DISCLAIM ALL
* WARRANTIES OF ANY KIND, EXPRESS OR IMPLIED, WITH RESPECT TO THE SOFTWARE AND THE
* DOCUMENTATION, INCLUDING ALL WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE,
* SATISFACTORY QUALITY, ACCURACY, TITLE AND NON-INFRINGEMENT, AND ANY WARRANTIES THAT MAY ARISE
* OUT OF COURSE OF PERFORMANCE, COURSE OF DEALING OR USAGE OF TRADE.  NEITHER CYBERSOURCE NOR
* ITS AFFILIATES WARRANT THAT THE FUNCTIONS OR INFORMATION CONTAINED IN THE SOFTWARE OR THE
* DOCUMENTATION WILL MEET ANY REQUIREMENTS OR NEEDS YOU MAY HAVE, OR THAT THE SOFTWARE OR
* DOCUMENTATION WILL OPERATE ERROR FREE, OR THAT THE SOFTWARE OR DOCUMENTATION IS COMPATIBLE
* WITH ANY PARTICULAR OPERATING SYSTEM.
*/

package com.cybersource.ws.client;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import static com.cybersource.ws.client.Utility.*;

/**
 * Containing runTransaction() methods that accept the requests in the
 * form of a Document object.
 */
public class XMLClient {
    private static final String SOAP_ENVELOPE =
            "<soap:Envelope xmlns:soap=\"" +
                    "http://schemas.xmlsoap.org/soap/envelope/" +
                    "\"><soap:Body></soap:Body></soap:Envelope>";

    private static final String ELEM_REQUEST_MESSAGE = "requestMessage";
    private static final String ELEM_REPLY_MESSAGE = "replyMessage";

    private static final String[] VERSION_FIELDS
            = {ELEM_CLIENT_LIBRARY,
            ELEM_CLIENT_LIBRARY_VERSION,
            ELEM_CLIENT_ENVIRONMENT};

    private static Document soapEnvelope;
    private static Exception initException = null;

    private static ConcurrentHashMap<String, MerchantConfig> mcObjects = new ConcurrentHashMap<String, MerchantConfig>();


    static {
        try {
            // load the SOAP envelope document.
            DocumentBuilder builder = Utility.newDocumentBuilder();
            StringReader sr = new StringReader(SOAP_ENVELOPE);
            soapEnvelope = builder.parse(new InputSource(sr));
            sr.close();
        } catch (ParserConfigurationException e) {
            initException = e;
        } catch (SAXException e) {
            initException = e;
        } catch (IOException e) {
            initException = e;
        }
    }

    /**
     * Runs a transaction.
     *
     * @param request request to send.
     * @param props   properties the client needs to run the transaction.
     *                See README for more information.
     * @throws FaultException  if a fault occurs.
     * @throws ClientException if any other exception occurs.
     */
    public static Document runTransaction(Document request, Properties props)
            throws FaultException, ClientException {
        return (runTransaction(
                request, props, null, true, true));
    }

    /**
     * Runs a transaction.
     *
     * @param request      request to send.
     * @param props        properties the client needs to run the transaction.
     *                     See README for more information.
     * @param _logger      Logger object to used for logging.
     * @param prepare      Flag as to whether or not the logger's
     *                     prepare() method should be called.
     * @param logTranStart Flag as to whether or not the logger's
     *                     logTransactionStart() method should be called.
     * @throws FaultException  if a fault occurs.
     * @throws ClientException if any other exception occurs.
     */
    @SuppressWarnings("unchecked")
	public static Document runTransaction(
            Document request, Properties props,
            Logger _logger, boolean prepare, boolean logTranStart)
            throws FaultException, ClientException {
        if (initException != null) {
            throw new ClientException(initException, false, null);
        }

        String nsURI;
        MerchantConfig mc;
        LoggerWrapper logger = null;
        Connection con = null;

        try {
            long requestSentTime = System.currentTimeMillis();
            // At this point, we do not know what namespace to use yet so
            // we locate the first merchantID element with any namespace
            // (actually, there should be just one.  Otherwise, there's
            // something wrong with their XML request).

            boolean isMerchantConfigCacheEnabled = Boolean.parseBoolean(props.getProperty("merchantConfigCacheEnabled", "false"));
            if(isMerchantConfigCacheEnabled) {
                mc = getInstanceMap(request, props);
            } else {
                mc = getMerchantConfigObject(request, props);
            }

            nsURI = mc.getEffectiveNamespaceURI();

            logger = new LoggerWrapper(_logger, prepare, logTranStart, mc);

            String isAuthService = checkIfAuthServiceFieldExist(request, nsURI);
            if (Boolean.valueOf(isAuthService) && mc.getUseHttpClientWithConnectionPool()){
                String mtiField = checkIfMTIFiledExist(request, nsURI);
                if(StringUtils.isBlank(mtiField)) {
                    throw new ClientException(HTTP_BAD_REQUEST, MTI_FIELD_ERR_MSG, false, logger);
                }
            }

            setVersionInformation(request, nsURI);

            DocumentBuilder builder = Utility.newDocumentBuilder();

            Document signedDoc
                    = soapWrapAndSign(request, mc, builder, logger);
            if(mc.isCustomHttpClassEnabled()){
				Class<Connection> customConnectionClass;
				try {
					customConnectionClass = (Class<Connection>) Class.forName(mc.getCustomHttpClass());
					Class[] constructor_Args = new Class[] {com.cybersource.ws.client.MerchantConfig.class, javax.xml.parsers.DocumentBuilder.class, com.cybersource.ws.client.LoggerWrapper.class};
					con=customConnectionClass.getDeclaredConstructor(constructor_Args).newInstance(mc, builder, logger);

				} catch (InstantiationException e) {
					logger.log(Logger.LT_INFO, "Failed to Instantiate the class "+e);
					throw new ClientException(e, false, null);
				} catch (IllegalAccessException e) {
					logger.log(Logger.LT_INFO, "Could not Access the method invoked "+e);
					throw new ClientException(e, false, null);
				} catch (ClassNotFoundException e) {
					logger.log(Logger.LT_INFO, "Could not load the custom HTTP class ");
					throw new ClientException(e, false, null);
				} catch (IllegalArgumentException e) {
					logger.log(Logger.LT_INFO, "Method invoked with Illegal Argument list  "+e);
					throw new ClientException(e, false, null);
				} catch (SecurityException e) {
					logger.log(Logger.LT_INFO, "Security Exception "+e);
					throw new ClientException(e, false, null);
				} catch (InvocationTargetException e) {
					logger.log(Logger.LT_INFO, "Exception occured while calling the method "+e);
					throw new ClientException(e, false, null);
				} catch (NoSuchMethodException e) {
					logger.log(Logger.LT_INFO, "Method not found ");
					throw new ClientException(e, false, null);
				}
            }
            else{
            	con = Connection.getInstance(mc, builder, logger);
            }
            Document wrappedReply = con.post(signedDoc, requestSentTime);

            Document doc = soapUnwrap(wrappedReply, mc, builder, logger);
            logger.log(Logger.LT_INFO, "Client, End of runTransaction Call   ", false);

            return doc;
        } catch (ParserConfigurationException e) {
            throw new ClientException(
                    e, con != null && con.isRequestSent(), logger);
        } catch (SignException e) {
            throw new ClientException(
                    e, con != null && con.isRequestSent(), logger);
        } catch (ConfigException e) {
            throw new ClientException(
                    e, con != null && con.isRequestSent(), logger);
        } catch (SignEncryptException e) {
        	throw new ClientException(
                    e, con != null && con.isRequestSent(), logger);
		} finally {
            if (con != null) {
                con.release();
            }
       }
    }

    /**
     * Returns the effective namespace URI for the specified merchant id.
     * Refer to <code>MerchantConfig.getProperty()</code> for the search
     * behavior.  This method is provided so that the nvpSample application
     * can dynamically plug the correct namespace URI into the nvpSample XML
     * inputs.  You do not need to call it if you have the namespace URI
     * hardcoded in your XML documents.
     *
     * @param props      Properties object to look up the properties in.
     * @param merchantID merchant ID whose effective namespace URI is wanted.
     *                   It may be null, in which case, the generic effective
     *                   namespace URI is returned.
     * @throws ClientException if a ConfigException occurs.  Call
     *                         <code>getInnerException()</code> to get at the
     *                         ConfigException.
     */
    public static String getEffectiveNamespaceURI(
            Properties props, String merchantID)
            throws ClientException {
        try {
            MerchantConfig mc = new MerchantConfig(props, merchantID);
            return (mc.getEffectiveNamespaceURI());
        } catch (ConfigException ce) {
            throw new ClientException(ce, false, null);
        }
    }

    /**
     * Sets the merchantID in the request.
     *
     * @param request    request to add the merchantID to.
     * @param merchantID merchantID to add to request.
     * @param nsURI      namespace URI to use.
     */
    private static void setMerchantID(
            Document request, String merchantID, String nsURI) {
        // create merchantID node
        Element merchantIDElem
                = Utility.createElement(request, nsURI, ELEM_MERCHANT_ID, merchantID);

        // add it as the first child of the requestMessage element.
        Element requestMessage
                = Utility.getElement(request, ELEM_REQUEST_MESSAGE, nsURI);
        requestMessage.insertBefore(
                merchantIDElem, requestMessage.getFirstChild());
    }


    /**
     * Sets the version information in the request.
     *
     * @param request request to set the version information in.
     * @param nsURI   namespaceURI to use.
     */
    private static void setVersionInformation(Document request, String nsURI) {
        //
        // First, delete the version fields currently in the request document,
        // if any.
        //

        // get the requestMessage element
        Element requestMessage
                = Utility.getElement(request, ELEM_REQUEST_MESSAGE, nsURI);

        // get the node that is supposed to precede the version fields,
        // which is merchantReferenceCode
        Element previous
                = Utility.getElement(
                request, ELEM_MERCHANT_REFERENCE_CODE, nsURI);

        // if it does not exist, look for the node that is supposed to precede
        // merchantReferenceCode, which is merchantID
        if (previous == null) {
            previous = Utility.getElement(request, ELEM_MERCHANT_ID, nsURI);
        }

        Node currElem, save;

        // if either the merchantReferenceCode or merchantID exists, its
        // next sibling (which may be null) becomes the current element
        if (previous != null) {
            currElem = previous.getNextSibling();
        }
        // else, if neither exists, the first child of requestMessage (which
        // may be null) becomes the current element
        else {
            currElem = requestMessage.getFirstChild();
        }

        // for each of the version fields...
        for (int i = 0; i < VERSION_FIELDS.length; ++i) {
            // if the current element is not null, compare it with
            // the current version field in the loop
            if (currElem != null) {
                // if they match, save the element next to it, delete
                // the current element and make the saved element the current
                // element
                if (VERSION_FIELDS[i].equals(currElem.getNodeName())) {
                    save = currElem.getNextSibling();
                    requestMessage.removeChild(currElem);
                    currElem = save;
                }
            }

            // else, if the current element is null, it means we have reached
            // the end of the requestMessage (parent) element.
            else {
                break;
            }
        }

        // create DocumentFragment for the version-related fields
        DocumentFragment versionsFragment = request.createDocumentFragment();
        versionsFragment.appendChild(Utility.createElement(
                request, nsURI, ELEM_CLIENT_LIBRARY, Utility.XML_LIBRARY));
        versionsFragment.appendChild(Utility.createElement(
                request, nsURI, ELEM_CLIENT_LIBRARY_VERSION, Utility.VERSION));
        versionsFragment.appendChild(Utility.createElement(
                request, nsURI, ELEM_CLIENT_ENVIRONMENT, Utility.ENVIRONMENT));

        // if the current element is not null, it will be the sibling right
        // next to the version fields.
        if (currElem != null) {
            requestMessage.insertBefore(versionsFragment, currElem);
        }
        // else, if the current element is null, the version fields will be
        // added at the end of the requestMessage.
        else {
            requestMessage.appendChild(versionsFragment);
        }
    }

    /**
     * Wraps the given Map object in SOAP envelope and signs it.
     *
     * @param doc    Document object containing the request.
     * @param mc     MerchantConfig object.
     * @param logger LoggerWrapper object to use for logging.
     * @return signed document.
     * @throws SignException if signing fails.
     * @throws SignEncryptException
     * @throws ConfigException
     */
    private static Document soapWrapAndSign(
            Document doc, MerchantConfig mc, DocumentBuilder builder,
            LoggerWrapper logger)
            throws SignException, SignEncryptException, ConfigException {
    	boolean logSignedData = mc.getLogSignedData();

    	if (!logSignedData) {
            logger.log(Logger.LT_REQUEST,
            		"UUID   >  "+(logger.getUniqueKey()) + "\n" +
            		"Input request is" + "\n" +
            		"======================================= \n"
                    + Utility.nodeToString(doc, PCI.REQUEST));
        }

        Document wrappedDoc = soapWrap(doc, mc, builder, logger);
        logger.log(Logger.LT_INFO, "Client, End of soapWrap   ",true);

        Document resultDocument;

        SecurityUtil.loadMerchantP12File(mc,logger);
        logger.log(Logger.LT_INFO, "Client, End of loading Merchant Certificate   ", true);

        // sign Document object
        resultDocument = SecurityUtil.createSignedDoc(wrappedDoc,mc.getKeyAlias(),mc.getKeyPassword(),logger);
        logger.log(Logger.LT_INFO, "Client, End of createSignedDoc   ", true);

        if ( mc.getUseSignAndEncrypted() ) {
        	// Encrypt signed Document
            resultDocument = SecurityUtil.handleMessageCreation(resultDocument , Utility.getElementText(doc, ELEM_MERCHANT_ID, "*") , logger);
            logger.log(Logger.LT_INFO, "Client, End of handleMessageCreation   ", true);
        }
        if (logSignedData) {
           logger.log(Logger.LT_REQUEST,Utility.nodeToString(resultDocument, PCI.REQUEST));
        	//logger.log(Logger.LT_REQUEST,XMLUtils.PrettyDocumentToString(resultDocument));
        }

        return resultDocument ;
    }

    /**
     * Wraps the given Map object in SOAP envelope
     * @param doc
     * @param mc
     * @param builder
     * @param logger
     * @return Document
     * @throws SignException
     */
    private static Document soapWrap(Document doc, MerchantConfig mc, DocumentBuilder builder, LoggerWrapper logger) throws SignException{
    	// look for the requestMessage element
        Element requestMessage
                = Utility.getElement(
                doc, ELEM_REQUEST_MESSAGE, mc.getEffectiveNamespaceURI());

        // wrap in SOAP envelope

        Document wrappedDoc = builder.newDocument();

        wrappedDoc.appendChild(
                wrappedDoc.importNode(soapEnvelope.getFirstChild(), true));

        if (requestMessage != null) {
            wrappedDoc.getFirstChild().getFirstChild().appendChild(
                    wrappedDoc.importNode(requestMessage, true));
        }

        return wrappedDoc;
    }

    /**
     * Convert Document to String
     * @param doc
     * @return String
     * @throws TransformerConfigurationException
     * @throws TransformerException
     * @throws IOException
     */
    private static String documentToString(Document doc)
            throws TransformerConfigurationException, TransformerException,
            IOException {
        ByteArrayOutputStream baos = null;
        try {
            baos = makeStream(doc);
            return baos.toString("utf-8");
        } finally {
            if (baos != null) {
                baos.close();
            }
        }
    }

    /**
     * Tranform document to byte array output stream
     * @param doc
     * @return ByteArrayOutputStream
     * @throws TransformerConfigurationException
     * @throws TransformerException
     */
    private static ByteArrayOutputStream makeStream(Document doc)
            throws TransformerConfigurationException, TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        transformer.transform(
                new DOMSource(doc), new StreamResult(baos));

        return baos;
    }

    /**
     * Extracts the content of the SOAP body from the given Document object
     * inside a SOAP envelope.
     *
     * @param doc    Document object to extract content from.
     * @param mc     MerchantConfig object.
     * @param logger LoggerWrapper object to use for logging.
     * @return content of SOAP body as a Document object.
     * @throws ParserConfigurationException if no suitable parser
     *                                      implementation is found.
     */
    private static Document soapUnwrap(
            Document doc, MerchantConfig mc, DocumentBuilder builder,
            LoggerWrapper logger) {
        boolean logSignedData = mc.getLogSignedData();
        if (logSignedData) {
            logger.log(Logger.LT_REPLY,
                    Utility.nodeToString(doc, PCI.REPLY));
        }

        // look for the replyMessage element
        Node replyMessage
                = Utility.getElement(
                doc, ELEM_REPLY_MESSAGE, mc.getEffectiveNamespaceURI());

        // extract it out into a new Document object

        Document unwrappedDoc = builder.newDocument();

        if (replyMessage != null) {
            unwrappedDoc.appendChild(
                    unwrappedDoc.importNode(replyMessage, true));
        }

        if (!logSignedData) {
            logger.log(
                    Logger.LT_REPLY,
                    Utility.nodeToString(unwrappedDoc, PCI.REPLY));
        }

        return (unwrappedDoc);
    }

    /**
     * Get Merchant Config object based on request and properties
     * @param request
     * @param props
     * @return MerchantConfig
     * @throws ConfigException
     */
    static private MerchantConfig getMerchantConfigObject(Document request, Properties props) throws ConfigException {
        MerchantConfig mc;
        String merchantID = Utility.getElementText(request, ELEM_MERCHANT_ID, "*");
        if (merchantID == null) {
            // if no merchantID is present in the request, get its
            // value from the properties and add it to the request.
            mc = new MerchantConfig(props, null);
            merchantID = mc.getMerchantID();
            String nsURI = mc.getEffectiveNamespaceURI();
            setMerchantID(request, merchantID, nsURI);
        } else {
            mc = new MerchantConfig(props, merchantID);
        }
        //System.out.println("merchant config object got created");
        return mc;
    }
    /**
     * Get Merchant Id from request, If merchantId is null, get it from properties
     * @param request
     * @param props
     * @return String
     */
    private static String getMerchantId(Document request, Properties props) {
        String merchantID = Utility.getElementText(request, ELEM_MERCHANT_ID, "*");
        if (merchantID == null) {
            // if no merchantID is present in the request, get its
            // value from the properties
            merchantID = props.getProperty(ELEM_MERCHANT_ID);
        }
        return merchantID;
    }

    /**
     * get KeyAlias from property, If keyAlias is null, return merchant Id
     * @param request
     * @param props
     * @return String
     */
    private static String getKeyForInstanceMap(Document request, Properties props) {
        String keyAlias = props.getProperty(KEY_ALIAS);
        if(keyAlias != null) {
            return keyAlias;
        }

        return getMerchantId(request, props);
    }
    /**
     * Get Merchant config instance from concurrent hash map in memory cache .
     * If it is empty, it will create new merchant config object and put it in map for reuse.
     * @param request
     * @param props
     * @return MerchantConfig
     * @throws ConfigException
     */
    private static MerchantConfig getInstanceMap(Document request, Properties props) throws ConfigException {
        String midOrKeyAlias = getKeyForInstanceMap(request, props);

        if(!mcObjects.containsKey(midOrKeyAlias)) {
            synchronized (Client.class) {
                if (!mcObjects.containsKey(midOrKeyAlias)) {
                    mcObjects.put(midOrKeyAlias, getMerchantConfigObject(request, props));
                }
            }
        }
        MerchantConfig mc = mcObjects.get(midOrKeyAlias);
        String merchantID = Utility.getElementText(request, ELEM_MERCHANT_ID, "*");
        // if no merchantID is present in the request, get its
        // value from the properties and add it to the request.
        if(StringUtils.isEmpty(merchantID)) {
            merchantID = mc.getMerchantID();
            String nsURI = mc.getEffectiveNamespaceURI();
            setMerchantID(request, merchantID, nsURI);
        }
        return mc;
    }
}


