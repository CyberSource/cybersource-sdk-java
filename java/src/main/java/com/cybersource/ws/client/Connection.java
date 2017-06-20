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

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;


/**
 * Connection class is a factory  class for creating an instance for HttpClientConnection or
 * JDKHttpURLConnection.
 *
 * @author sunagara
 */
abstract class Connection {
    final MerchantConfig mc;
    private final DocumentBuilder builder;
    final LoggerWrapper logger;
    
   /**
    * It initializes three arguments MerchantConfig, DocumentBuilder and Logger
    * Any class extending this class must implement three argument constructor
    * @param mc
    * @param builder
    * @param logger
 */
protected Connection(MerchantConfig mc, DocumentBuilder builder,
               LoggerWrapper logger) {
        this.mc = mc;
        this.builder = builder;
        this.logger = logger;
    }

    public static Connection getInstance(
            MerchantConfig mc, DocumentBuilder builder, LoggerWrapper logger) {
        if (mc.getUseHttpClient()) {
            return new HttpClientConnection(mc, builder, logger);
        }
        // HttpClient is not set in properties file then JDKHttpURLConnection class instance.
        return new JDKHttpURLConnection(mc, builder, logger);
    }

    abstract public boolean isRequestSent();

    abstract public void release();

    abstract void postDocument(Document request)
            throws IOException, TransformerConfigurationException,
            TransformerException, MalformedURLException,
            ProtocolException;

    abstract int getHttpResponseCode()
            throws IOException;

    abstract InputStream getResponseStream()
            throws IOException;

    abstract InputStream getResponseErrorStream()
            throws IOException;

    /**
     * Post the request document and validate the response for any faults from the Server.
     * @param request - Request document
     * @return - Response XML as Document object.
     * @throws ClientException
     * @throws FaultException
     */
    public Document post(Document request)
            throws ClientException, FaultException {
        try {
            postDocument(request);
            checkForFault();
            return (parseReceivedDocument());
        } catch (IOException e) {
            throw new ClientException(e, isRequestSent(), logger);
        } catch (TransformerConfigurationException e) {
            throw new ClientException(e, isRequestSent(), logger);
        } catch (TransformerException e) {
            throw new ClientException(e, isRequestSent(), logger);
        } catch (SAXException e) {
            throw new ClientException(e, isRequestSent(), logger);
        }

    }


    /**
     * Validate the Http response for any faults returned from the server. 
     * @throws FaultException
     * @throws ClientException
     */
    private void checkForFault()
            throws FaultException, ClientException {
        try {
            logger.log(Logger.LT_INFO, "Reading response...");

            int responseCode = getHttpResponseCode();

            // if successful, there's nothing left to do here.
            // we'll process the response in a later method.
            if (responseCode == HttpURLConnection.HTTP_OK) return;

            InputStream errorStream = getResponseErrorStream();

            // if there is no error stream, then it is not a fault
            if (errorStream == null) {
                throw new ClientException(responseCode, logger);
            }

            // read error stream into a byte array
            byte[] errorBytes;
            try {
                errorBytes = Utility.read(errorStream);
                errorStream.close();
            } catch (IOException ioe) {
                throw new ClientException(
                        responseCode, "Failed to read additional HTTP error",
                        true, logger);
            }

            // server will return HTTP 500 on a fault
            if (responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR) {
                try {
                    ByteArrayInputStream bais
                            = new ByteArrayInputStream(errorBytes);

                    Document faultDoc = builder.parse(bais);
                    bais.close();

                    throw new FaultException(
                            faultDoc, mc.getEffectiveNamespaceURI(), logger);

                } catch (IOException ioe) {
                    // If an IO exception occurs, we're not sure whether
                    // or not it was a fault.  So we mark it as critical.
                    String text = new String(errorBytes);
                    throw new ClientException(
                            responseCode, text, true, logger);
                } catch (SAXException ioe) {
                    // If parsing fails, it means it's not a fault after all.
                    String text = new String(errorBytes);
                    throw new ClientException(responseCode, text, logger);
                }
            } else {
                // non-500 return codes are definitely not faults
                String text = new String(errorBytes);
                throw new ClientException(responseCode, text, logger);
            }
        }
        // catch other IOException's that we have not already handled.
        catch (IOException e) {
            throw new ClientException(e, true, logger);
        }
    }

    /**
     * Method helps to parse/read the response xml into Document object.
     * @return - returns a Document object
     * @throws IOException
     * @throws SAXException
     */
    private Document parseReceivedDocument()
            throws IOException, SAXException {
        logger.log(Logger.LT_INFO, "Parsing response...");
        return builder.parse(getResponseStream());
    }

    /**
     * Converts the Document object into ByteArrayStream.
     * @param doc  - Document object
     * @return ByteArrayStream
     * @throws TransformerConfigurationException
     * @throws TransformerException
     */
    static ByteArrayOutputStream makeStream(Document doc)
            throws TransformerConfigurationException, TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        transformer.transform(
                new DOMSource(doc), new StreamResult(baos));

        return baos;
    }
}

/* Copyright 2006 CyberSource Corporation */

