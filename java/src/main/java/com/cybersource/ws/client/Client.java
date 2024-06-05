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
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import static com.cybersource.ws.client.Utility.*;

/**
 * Containing runTransaction() methods that accept the requests in the
 * form of a Map object.
 */
public class Client {
    private static final String SOAP_ENVELOPE1 = "<soap:Envelope xmlns:soap=\"" +
            "http://schemas.xmlsoap.org/soap/envelope/\">\n<soap:Body id=\"body1\">\n" +
            "<nvpRequest xmlns=\"{0}\">\n{1}</nvpRequest>" +
            "\n</soap:Body>\n</soap:Envelope>";

    private static final String ELEM_NVP_REPLY = "nvpReply";

    private static ConcurrentHashMap<String, MerchantConfig> mcObjects = new ConcurrentHashMap<String, MerchantConfig>();

    /**
     * Runs a transaction.
     *
     * @param request request to send.
     * @param props   properties the client needs to run the transaction.
     *                See README for more information.
     * @throws FaultException  if a fault occurs.
     * @throws ClientException if any other exception occurs.
     */
    public static Map<String,String> runTransaction(Map<String,String> request, Properties props)
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
	public static Map<String, String> runTransaction(
            Map<String, String> request, Properties props,
            Logger _logger, boolean prepare, boolean logTranStart)
            throws FaultException, ClientException {
        MerchantConfig mc;
        LoggerWrapper logger = null;
        Connection con = null;
        try {
            long startTime = System.currentTimeMillis();
            boolean isMerchantConfigCacheEnabled = Boolean.parseBoolean(props.getProperty("merchantConfigCacheEnabled", "false"));
            if(isMerchantConfigCacheEnabled) {
                mc = getInstanceMap(request, props);
            } else {
                mc = getMerchantConfigObject(request, props);
            }

            setVersionInformation(request);

            logger = new LoggerWrapper(_logger, prepare, logTranStart, mc);

            String isAuthService = request.get(AUTH_SERVICE_NVP);
            if (Boolean.valueOf(isAuthService) && mc.getUseHttpClientWithConnectionPool()){
                String mtiField = request.get(MERCHANT_TRANSACTION_IDENTIFIER);
                if(StringUtils.isBlank(mtiField)) {
                    throw new ClientException(HTTP_BAD_REQUEST, MTI_FIELD_ERR_MSG, false, logger);
                }
            }

            DocumentBuilder builder = Utility.newDocumentBuilder();

            Document signedDoc
                    = soapWrapAndSign(request, mc, builder,logger);

//          FileWriter writer = new FileWriter(new File("nvp_signedDoc.xml"));
//          writer.write(XMLUtils.PrettyDocumentToString(signedDoc));
//          writer.close();
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
            Document wrappedReply = con.post(signedDoc, startTime);
            Map<String, String> replyMap = soapUnwrap(wrappedReply, mc, logger);
            logger.log(Logger.LT_INFO, "Client, End of runTransaction Call   ",false);

            return replyMap;
        } catch (IOException e) {
            throw new ClientException(
                    e, con != null && con.isRequestSent(), logger);
        } catch (ParserConfigurationException e) {
            throw new ClientException(
                    e, con != null && con.isRequestSent(), logger);
        } catch (SignException e) {
            throw new ClientException(
                    e, con != null && con.isRequestSent(), logger);
        } catch (ConfigException e) {
            throw new ClientException(
                    e, con != null && con.isRequestSent(), logger);
        } catch (SAXException e) {
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
     * Runs a transaction.
     *
     * @param request      request to send.
     * @param mc           merchant config object	
     *                     See README for more information.
     * @param _logger      Logger object to used for logging.
     * @param prepare      Flag as to whether or not the logger's
     *                     prepare() method should be called.
     * @param logTranStart Flag as to whether or not the logger's
     *                     logTransactionStart() method should be called.
     * @throws FaultException  if a fault occurs.
     * @throws ClientException if any other exception occurs.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public static Map runTransaction(
            Map<String, String> request,  MerchantConfig mc,
            Logger _logger, boolean prepare, boolean logTranStart)
            throws FaultException, ClientException {
        ;
        LoggerWrapper logger = null;
        Connection con = null;

        try {
            setVersionInformation(request);

            logger = new LoggerWrapper(_logger, prepare, logTranStart, mc);

            DocumentBuilder builder = Utility.newDocumentBuilder();

            Document signedDoc
                    = soapWrapAndSign(request, mc, builder,logger);
            
//          FileWriter writer = new FileWriter(new File("signedDoc.xml"));
//          writer.write(XMLUtils.PrettyDocumentToString(signedDoc));
//          writer.close();
            if(mc.isCustomHttpClassEnabled()){
				Class<Connection> customConnectionClass;
				try {
					customConnectionClass = (Class<Connection>) Class.forName(mc.getcustomHttpClass());
					Class[] constructor_Args = new Class[] {mc.getClass(), javax.xml.parsers.DocumentBuilder.class, com.cybersource.ws.client.LoggerWrapper.class}; 
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
            Document wrappedReply = con.post(signedDoc);
            Map<String, String> replyMap = soapUnwrap(wrappedReply, mc, logger);
            logger.log(Logger.LT_INFO, "Client, End of runTransaction Call   ",false);
            
            return replyMap;
        } catch (IOException e) {
            throw new ClientException(
                    e, con != null && con.isRequestSent(), logger);
        } catch (ParserConfigurationException e) {
            throw new ClientException(
                    e, con != null && con.isRequestSent(), logger);
        } catch (SignException e) {
            throw new ClientException(
                    e, con != null && con.isRequestSent(), logger);
        } catch (ConfigException e) {
            throw new ClientException(
                    e, con != null && con.isRequestSent(), logger);
        } catch (SAXException e) {
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
     * Sets the version information in the request.
     *
     * @param request request to set the version information in.
     *
     */
    private static void setVersionInformation(Map<String, String> request) {
        request.put(ELEM_CLIENT_LIBRARY, Utility.NVP_LIBRARY);
        request.put(ELEM_CLIENT_LIBRARY_VERSION, Utility.VERSION);
        request.put(ELEM_CLIENT_ENVIRONMENT, Utility.ENVIRONMENT);
    }

    /**
     * Wraps the given Map object in SOAP envelope and signs it.
     *
     * @param request Map object containing the request.
     * @param mc      MerchantConfig object.
     * @param builder	    DocumentBuilder object.
     * @param logger  LoggerWrapper object to use for logging.
     * @return signed document.
     * @throws IOException   if reading from string fails.
     * @throws SignException if signing fails.
     * @throws SAXException
     * @throws SignEncryptException
     * @throws ConfigException
     */
    private static Document soapWrapAndSign(
            Map<String, String> request, MerchantConfig mc, DocumentBuilder builder,
            LoggerWrapper logger)
            throws
            IOException, SignException, SAXException, SignEncryptException, ConfigException {
        boolean logSignedData = mc.getLogSignedData();
        if (!logSignedData) {
            logger.log(
            		Logger.LT_REQUEST,
            		"UUID   >  "+(logger.getUniqueKey()) + "\n" +
            		"Input request is" + "\n" +
            		"======================================= \n"
            		+ mapToString(request, true, PCI.REQUEST));
        }

        Document wrappedDoc = soapWrap(request, mc, builder);
        logger.log(Logger.LT_INFO, "Client, End of soapWrap   ",true);

        Document resultDocument;

        SecurityUtil.loadMerchantP12File(mc,logger);
        logger.log(Logger.LT_INFO, "Client, End of loading Merchant Certificates ", true);

        // sign Document object
        resultDocument = SecurityUtil.createSignedDoc(wrappedDoc, mc.getKeyAlias(), mc.getKeyPassword(), logger);
        logger.log(Logger.LT_INFO, "Client, End of createSignedDoc   ", true);

        if ( mc.getUseSignAndEncrypted() ) {
        	// Encrypt signed Document
            resultDocument = SecurityUtil.handleMessageCreation(resultDocument, request.get(ELEM_MERCHANT_ID), logger);
            logger.log(Logger.LT_INFO, "Client, End of handleMessageCreation   ", true);
        }
        if (logSignedData) {
           logger.log(Logger.LT_REQUEST,Utility.nodeToString(resultDocument, PCI.REQUEST));
        	//logger.log(Logger.LT_REQUEST,XMLUtils.PrettyDocumentToString(resultDocument));
        }

        return resultDocument ;
    }


    /**
     * Wraps the given Map object in SOAP envelope.
     *
     * @param request Map object containing the request.
     * @param mc      MerchantConfig object.
     * @param builder	    DocumentBuilder object.
     * @return document.
     * @throws IOException   if reading from string fails.
     * @throws SAXException
     */
    private static Document soapWrap(Map request, MerchantConfig mc, DocumentBuilder builder) throws SAXException, IOException{
        Object[] arguments
                = {mc.getEffectiveNamespaceURI(),
                mapToString(request, false, PCI.REQUEST)};
        String xmlString = MessageFormat.format(SOAP_ENVELOPE1, arguments);
        // load XML string into a Document object
        StringReader sr = new StringReader( xmlString );
        Document wrappedDoc = builder.parse( new InputSource( sr ) );
        sr.close();
        return wrappedDoc;
    }

    /**
     * Extracts the content of the SOAP body from the given Document object
     * inside a SOAP envelope.
     *
     * @param doc    Document object to extract content from.
     * @param mc     MerchantConfig object.
     * @param logger LoggerWrapper object to use for logging.
     * @return content of SOAP body as a Map object.
     */
    private static HashMap<String, String> soapUnwrap(
            Document doc, MerchantConfig mc, LoggerWrapper logger) {

    	// 3/8/2016 if the message was encrypted we need to decrypt it

        boolean logSignedData = mc.getLogSignedData();

        if (logSignedData) {
            logger.log(Logger.LT_REPLY,
                    Utility.nodeToString(doc, PCI.REPLY));
        }

        // look for the nvpReply element
        Node nvpReply
                = Utility.getElement(
                doc, ELEM_NVP_REPLY, mc.getEffectiveNamespaceURI());

        Text nvpString = (Text) nvpReply.getFirstChild();

        String replyString = nvpString.getNodeValue();

        HashMap<String, String> reply = Utility.stringToMap(replyString);

        if (!logSignedData) {
            logger.log(
                    Logger.LT_REPLY,
                    mapToString(reply, true, PCI.REPLY));
        }

        return reply;
    }

    /**
     * Converts the contents of a Map object into a string, one name-value pair
     * to a line and the name and value are separated by an equal sign.
     *
     * @param src  Map object whose contents are being converted.
     * @param mask Flag whether or not to mask "unsafe" fields.
     * @param type Relevant only when mask is true, indicates whether this
     *             is the request or the reply map.  Pass either
     *             PCI.REQUEST or PCI.REPLY.
     * @return resulting string; will be empty if the Map object was null or
     *         empty.
     */
    static String mapToString(Map src, boolean mask, int type) {
       return Utility.mapToString(src, mask, type);
    }

    /**
     * Get Merchant Config object based on request and properties
     * @param request
     * @param props
     * @return MerchantConfig
     * @throws ConfigException
     */
    static private MerchantConfig getMerchantConfigObject(Map<String, String> request, Properties props) throws ConfigException {
        MerchantConfig mc;
        String merchantID = request.get(ELEM_MERCHANT_ID);
        if (merchantID == null) {
            // if no merchantID is present in the request, get its
            // value from the properties and add it to the request.
            mc = new MerchantConfig(props, null);
            merchantID = mc.getMerchantID();
            request.put(ELEM_MERCHANT_ID, merchantID);
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
    private static String getMerchantId(Map<String, String> request, Properties props) {
        String merchantID = request.get(ELEM_MERCHANT_ID);
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
    private static String getKeyForInstanceMap(Map<String, String> request, Properties props) {
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
    private static MerchantConfig getInstanceMap(Map<String, String> request, Properties props) throws ConfigException {
        String midOrKeyAlias = getKeyForInstanceMap(request, props);

        if(!mcObjects.containsKey(midOrKeyAlias)) {
            synchronized (Client.class) {
                if (!mcObjects.containsKey(midOrKeyAlias)) {
                    mcObjects.put(midOrKeyAlias, getMerchantConfigObject(request, props));
               }
            }
        }
        MerchantConfig mc = mcObjects.get(midOrKeyAlias);
        String merchantID = request.get(ELEM_MERCHANT_ID);
        // if no merchantID is present in the request, get its
        // value from the properties and add it to the request.
        if(StringUtils.isEmpty(merchantID)) {
            merchantID = mc.getMerchantID();
            request.put(ELEM_MERCHANT_ID, merchantID);
        }
        return mc;
    }
}

