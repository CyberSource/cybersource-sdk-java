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

import java.util.Hashtable;

/**
 * Payment Card Information which is sent as part of Transaction details
 * @author sunagara
 *
 */
class PCI {
    public static final int REQUEST = 0;
    public static final int REPLY = 1;

    private static final int BOUNDARY = 0;
    private static final int NON_INDEX = 1;
    private static final int POSSIBLE_INDEX = 2;
    private static final int DONE = 3;

    private static final String REQUEST_MESSAGE = "requestMessage";
    private static final String REPLY_MESSAGE = "replyMessage";

    private static final char UNDERSCORE = '_';

    private static final Hashtable<String, String> safeTable;

    static {
        safeTable = new Hashtable<String, String>();

        safeTable.put("item", "unitPrice quantity productCode productName productSKU productRisk taxAmount cityOverrideAmount cityOverrideRate countyOverrideAmount countyOverrideRate districtOverrideAmount districtOverrideRate stateOverrideAmount stateOverrideRate countryOverrideAmount countryOverrideRate orderAcceptanceCity orderAcceptanceCounty orderAcceptanceCountry orderAcceptanceState orderAcceptancePostalCode orderOriginCity orderOriginCounty orderOeriginCountry orderOriginState orderOriginPostalCode shipFromCity shipFromCounty shipFromCountry shipFromState shipFromPostalCode export noExport nationalTax vatRate sellerRegistration buyerRegistration middlemanRegistration pointOfTitleTransfer giftCategory timeCategory hostHedge timeHedge velocityHedge unitOfMeasure taxRate totalAmount discountAmount discountRate commodityCode grossNetIndicator taxTypeApplied discountIndicator alternateTaxID");

        safeTable.put("ccAuthService", "run cavv commerceIndicator eciRaw xid reconcilationID avsLevel fxQuoteID returnAuthRecord authType verbalAuthCode billPayment");

        safeTable.put("ccCaptureService", "run authType verbalAuthCode authRequestID transactionToken reconciliationID partialPaymentID purchasingLevel industryDataType");

        safeTable.put("ccCreditService", "run captureRequestID transactionToken reconciliationID partialPaymentID purchasingLevel industryDataType commerceIndicator billPayment");

        safeTable.put("ccAuthReversalService", "run authRequestID transactionToken");

        safeTable.put("ecDebitService", "run paymentMode referenceNumber settlementMethod transactionToken verificationLevel partialPaymentID commerceIndicator");

        safeTable.put("ecCreditService", "run referenceNumber settlementMethod transactionToken debitRequestID partialPaymentID commerceIndicator");

        safeTable.put("payerAuthEnrollService", "run httpAccept httpUserAgent merchantName merchantURL purchaseDescription purchaseTime countryCode acquirerBin merchantID");

        safeTable.put("payerAuthValidateService", "run signedPARes");

        safeTable.put("taxService", "run nexus noNexus orderAcceptanceCity orderAcceptanceCounty orderAcceptanceCountry orderAcceptanceState orderAcceptancePostalCode orderOriginCity orderOriginCounty orderOriginCountry orderOriginState orderOriginPostalCode sellerRegistration buyerRegistration middlemanRegistration pointOfTitleTransfer");

        safeTable.put("afsService", "run avsCode cvCode disableAVSScoring");

        safeTable.put("davService", "run");

        safeTable.put("exportService", "run addressOperator addressWeight companyWeight nameWeight");

        safeTable.put("fxRatesService", "run");

        safeTable.put("bankTransferService", "run");

        safeTable.put("bankTransferRefundService", "run bankTransferRequestID reconciliationID");

        safeTable.put("directDebitService", "run dateCollect directDebitText authorizationID transactionType directDebitType validateRequestID");

        safeTable.put("directDebitRefundService", "run directDebitRequestID reconciliationID");

        safeTable.put("directDebitValidateService", "run directDebitValidateText");

        safeTable.put("paySubscriptionCreateService", "run paymentRequestID disableAutoAuth");

        safeTable.put("paySubscriptionUpdateService", "run");

        safeTable.put("paySubscriptionEventUpdateService", "run action");

        safeTable.put("paySubscriptionRetrieveService", "run");

        safeTable.put("payPalPaymentService", "run cancelURL successURL reconciliationID");

        safeTable.put("payPalCreditService", "run payPalPaymentRequestID reconciliationID");

        safeTable.put("voidService", "run voidRequestID");

        safeTable.put("pinlessDebitService", "run reconciliationID commerceIndicator");

        safeTable.put("pinlessDebitValidateService", "run");

        safeTable.put("payPalButtonCreateService", "run buttonType reconciliationID");

        safeTable.put("payPalPreapprovedPaymentService", "run reconciliationID");

        safeTable.put("payPalPreapprovedUpdateService", "run reconciliationID");

        safeTable.put("riskUpdateService", "run actionCode recordID negativeAddress_city negativeAddress_state negativeAddress_postalCode negativeAddress_country");

        safeTable.put("invoiceHeader", "merchantDescriptor merchantDescriptorContact isGift returnsAccepted tenderType merchantVATRegistrationNumber purchaserOrderDate purchaserVATRegistrationNumber vatInvoiceReferenceNumber summaryCommodityCode supplierOrderReference userPO costCenter purchaserCode taxable amexDataTAA1 amexDataTAA2 amexDataTAA3 amexDataTAA4 invoiceDate");

        safeTable.put("businessRules", "ignoreAVSResult ignoreCVResult ignoreDAVResult ignoreExportResult ignoreValidateResult declineAVSFlags scoreThreshold");

        safeTable.put("billTo", "title suffix city county state postalCode country company ipAddress ipNetworkAddress hostname domainName driversLicenseState customerID httpBrowserType httpBrowserCookiesAccepted");

        safeTable.put("shipTo", "title suffix city county state postalCode country company shippingMethod");

        safeTable.put("shipFrom", "title suffix city county state postalCode country company");

        safeTable.put("card", "bin");

        safeTable.put("check", "");

        safeTable.put("bml", "customerBillingAddressChange customerEmailChange customerHasCheckingAccount CustomerHasSavingsAccount customerPasswordChange customerPhoneChange customerRegistrationDate customerTypeFlag grossHouseholdIncome householdIncomeCurrency itemCategory merchantPromotionCode preapprovalNumber productDeliveryTypeIndicator residenceStatus tcVersion yearsAtCurrentResidence yearsWithCurrentEmployer employerCity employerCompanyName employerCountry employerPhoneType employerState employerPostalCode shipToPhoneType billToPhoneType");

        safeTable.put("otherTax", "vatTaxAmount vatTaxRate alternateTaxAmount alternateTaxIndicator alternateTaxID localTaxAmount localTaxIndicator nationalTaxAmount nationalTaxIndicator");

        safeTable.put("purchaseTotals", "currency discountAmount taxAmount dutyAmount grandTotalAmount freightAmount");

        safeTable.put("fundingTotals", "currency grandTotalAmount");

        safeTable.put("gecc", "saleType planNumber sequenceNumber promotionEndDate promotionPlan line");

        safeTable.put("ucaf", "authenticationData collectionIndicator");

        safeTable.put("fundTransfer", "");

        safeTable.put("bankInfo", "bankCode name address city country branchCode swiftCode sortCode");

        safeTable.put("recurringSubscriptionInfo", "status amount numberOfPayments numberOfPaymentsToAdd automaticRenew frequency startDate endDate approvalRequired event_amount event_approvedBy event_number billPayment");

        safeTable.put("subscription", "title paymentMethod");

        safeTable.put("decisionManager", "enabled profile");

        safeTable.put("batch", "batchID recordID");

        safeTable.put("payPal", "");

        safeTable.put("jpo", "paymentMethod bonusAmount bonuses installments");

        safeTable.put(REQUEST_MESSAGE, "merchantID merchantReferenceCode clientLibrary clientLibraryVersion clientEnvironment clientSecurityLibraryVersion clientApplication clientApplicationVersion clientApplicationUser comments");

        safeTable.put("ccAuthReply", "reasonCode amount avsCode avsCodeRaw cvCode cvCodeRaw authorizedDateTime processorResponse authFactorCode reconciliationID transactionToken fundingTotals_currency fundingTotals_grandTotalAmount fxQuoteID fxQuoteRate fxQuoteType fxQuoteExpirationDateTime");

        safeTable.put("ccCaptureReply", "reasonCode requestDateTime amount reconciliationID transactionToken fundingTotals_currency fundingTotals_grandTotalAmount fxQuoteID fxQuoteRate fxQuoteType fxQuoteExpirationDateTime purchasingLevel3Enabled enhancedDataEnabled");

        safeTable.put("ccCreditReply", "reasonCode requestDateTime amount reconciliationID transactionToken purchasingLevel3Enabled enhancedDataEnabled");

        safeTable.put("ccAuthReversalReply", "reasonCode amount processorResponse requestDateTime transactionToken");

        safeTable.put("ecDebitReply", "reasonCode settlementMethod requestDateTime amount verificationLevel reconciliationID processorResponse transactionToken avsCode avsCodeRaw");

        safeTable.put("ecCreditReply", "reasonCode settlementMethod requestDateTime amount reconciliationID processorResponse transactionToken");

        safeTable.put("payerAuthEnrollReply", "reasonCode acsURL commerceIndicator paReq proxyPAN xid proofXML ucafCollectionIndicator");

        safeTable.put("payerAuthValidateReply", "reasonCode authenticationResult authenticationStatusMessage cavv commerceIndicator eci eciRaw xid ucafAuthenticationData ucafCollectionIndicator");

        safeTable.put("taxReply", "reasonCode currency grandTotalAmount totalCityTaxAmount city totalCountyTaxAmount county totalDistrictTaxAmount totalStateTaxAmount state totalTaxAmount postalCode geocode item_cityTaxAmount item_countyTaxAmount item_districtTaxAmount item_stateTaxAmount item_totalTaxAmount");

        safeTable.put("afsReply", "reasonCode afsResult hostSeverity consumerLocalTime afsFactorCode addressInfoCode hotlistInfoCode internetInfoCode phoneInfoCode suspiciousInfoCode velocityInfoCode");

        safeTable.put("davReply", "reasonCode addressType apartmentInfo barCode barCodeCheckDigit cityInfo countryInfo directionalInfo lvrInfo matchScore standardizedCity standardizedCounty standardizedCSP standardizedState standardizedPostalCode standardizedCountry standardizedISOCountry stateInfo streetInfo suffixInfo postalCodeInfo overallInfo usInfo caInfo intlInfo usErrorInfo caErrorInfo intlErrorInfo");

        safeTable.put("deniedPartiesMatch", "list");

        safeTable.put("exportReply", "reasonCode ipCountryConfidence");

        safeTable.put("fxRatesReply", "reasonCode quote_id quote_rate quote_type quote_expirationDateTime quote_currency quote_fundingCurrency quote_receivedDateTime");

        safeTable.put("bankTransferReply", "reasonCode amount bankName bankCity bankCountry paymentReference processorResponse bankSwiftCode bankSpecialID requestDateTime reconciliationID");

        safeTable.put("bankTransferRefundReply", "reasonCode amount requestDateTime reconciliationID processorResponse");

        safeTable.put("directDebitReply", "reasonCode amount requestDateTime reconciliationID processorResponse");

        safeTable.put("directDebitValidateReply", "reasonCode amount requestDateTime reconciliationID processorResponse");

        safeTable.put("directDebitRefundReply", "reasonCode amount requestDateTime reconciliationID processorResponse");

        safeTable.put("paySubscriptionCreateReply", "reasonCode");

        safeTable.put("paySubscriptionUpdateReply", "reasonCode");

        safeTable.put("paySubscriptionEventUpdateReply", "reasonCode");

        safeTable.put("paySubscriptionRetrieveReply", "reasonCode approvalRequired automaticRenew cardType checkAccountType city comments companyName country currency customerAccountID endDate frequency merchantReferenceCode paymentMethod paymentsRemaining postalCode recurringAmount setupAmount startDate state status title totalPayments shipToCity shipToState shipToCompany shipToCountry billPayment merchantDefinedDataField1 merchantDefinedDataField2 merchantDefinedDateField3 merchantDefinedDataField4");

        safeTable.put("payPalPaymentReply", "reasonCode amount requestDateTime reconciliationID");

        safeTable.put("payPalCreditReply", "reasonCode amount requestDateTime reconciliationID processorResponse");

        safeTable.put("voidReply", "reasonCode requestDateTime amount currency");

        safeTable.put("pinlessDebitReply", "reasonCode amount requestDateTime processorResponse receiptNumber reconciliationID");

        safeTable.put("pinlessDebitValidateReply", "reasonCode status requestDateTime");

        safeTable.put("payPalButtonCreateReply", "reasonCode encryptedFormData requestDateTime reconciliationID buttonType");

        safeTable.put("payPalPreapprovedPaymentReply", "reasonCode requestDateTime reconciliationID payerStatus transactionType feeAmount payerCountry pendingReason paymentStatus mpStatus payerBusiness desc mpMax paymentType paymentDate paymentGrossAmount settleAmount taxAmount exchangeRate paymentSourceID");

        safeTable.put("payPalPreapprovedUpdateReply", "reasonCode requestDateTime reconciliationID payerStatus payerCountry mpStatus payerBusiness desc mpMax paymentSourceID");

        safeTable.put("riskUpdateReply", "reasonCode");

        safeTable.put("decisionReply", "activeProfileReply_selectedBy activeProfileReply_name activeProfileReply_destinationQueue activeProfileReply_rulesTriggered_ruleResultItem_name activeProfileReply_rulesTriggered_ruleResultItem_decision activeProfileReply_rulesTriggered_ruleResultItem_evaluation activeProfileReply_rulesTriggered_ruleResultItem_ruleID");

        safeTable.put(REPLY_MESSAGE, "merchantReferenceCode requestID decision reasonCode missingField invalidField");

        safeTable.put("airlineData", "agentCode agentName ticketIssuerCity ticketIssuerState ticketIssuerPostalCode ticketIssuerCountry ticketIssuerCode ticketIssuerName ticketNumber checkDigit restrictedTicketIndicator transactionType extendedPaymentCode carrierName customerCode documentType documentNumber documentNumberOfParts chargeDetails bookingReference leg_carrierCode leg_flightNumber leg_originatingAirportCode leg_class leg_stopoverCode leg_departureDate leg_destination leg_fareBasis leg_departTax");

        safeTable.put("pos", "entryMode cardPresent terminalCapability terminalID terminalType terminalLocation transactionSecurity catLevel conditionCode");

        safeTable.put("merchantDefinedData", "field1 field2 field3 field4");
    }

    /**
     * Mask the data which needs to be secured.
     * @param type - Type of data
     * @param field - field
     * @param val - value of the field.
     * @return
     */
    public static String maskIfNotSafe(int type, String field, String val) {
        return isSafe(type, field) ? val : mask(field, val);
    }

    // the masking rules in this method were per Jason Hengels.
    public static String mask(String field, String val) {
        int len = val != null ? val.length() : 0;
        if (len == 0) return "";

/*	    
        if (field.equals( "nvpRequest" ) ||
	        field.equals( "nvpReply" )) {
		    // field will be nvpRequest or nvpReply only when the client in use is
		    // NVP (obviously) and logNonPCICompliantSignedData is true, in which
		    // case, we replace the entire content with "masked".  I could have
		    // parsed the content into a map so as to mask the individual fields
		    // but I thought it was too much for an undocumented feature.  Plus,
		    // if, for some reason, the content contained "nvpRequest" and
		    // "nvpReply" fields (I know they're not valid fields but...), then it
		    // would cause an infinite loop.  Anyway, we'd only ask them to turn
		    // on "logNonPCICompliantSignedData" if we wanted to look at the SOAP
		    // headers.  We would not be interested in the actual request.
		    //
		    // Since no credit card numbers will be logged when
		    // logNonPCICompliantSignedData is on, then technically, it's
		    // PCI-compliant.  However, I'm sticking with that name as the behavior
		    // may change in the future.  Also, if/when they find out about this
		    // config setting, they'd know from its name that they should not be
		    // messing with it.
		    // 
		    // k... I've said too much.
			return "masked";
	    }
*/

        // calculate number of end-chars to display on each
        // side of the string.
        int offset;
        if (field == null ||
                field.length() == 0 ||
                field.toLowerCase().indexOf("trackdata") != -1 ||
                len <= 9) {
            offset = 0;
        } else if (len >= 10 && len <= 15) {
            offset = 2;
        } else { // if >= 16
            offset = 4;
        }

        StringBuffer sb = new StringBuffer(val);
        int upperLimit = len - offset;
        for (int i = offset; i < upperLimit; ++i) {
            sb.setCharAt(i, 'x');
        }

        return sb.toString();
    }

    /**
     * check for safety of field details.
     * @param type
     * @param field
     * @return
     */
    public static boolean isSafe(int type, String field) {
        field = removeIndices(field);

        // if fieldname is null or empty, then it's safe to
        // treat it as unsafe.  pardon the pun.
        if (field == null || field.length() == 0) return false;

        String parent, child;
        int pos = field.indexOf(UNDERSCORE);
        if (pos != -1) {
            parent = field.substring(0, pos);
            child = field.substring(pos + 1);
            return isSafe(parent, child);
        } else {
            return isSafe(type == REQUEST ? REQUEST_MESSAGE : REPLY_MESSAGE,
                    field);
        }
    }

    private static boolean isSafe(String parent, String child) {
        String list = safeTable.get(parent);

        // if none, then this field is definitely not safe
        if (list == null) return (false);

        // return whether or not this child is on the list
        return (list.indexOf(child) != -1);
    }

    // removes indices, e.g. item_0_unitPrice becomes item_unitPrice.
    private static String removeIndices(String field) {
        int len = field != null ? field.length() : 0;
        if (len == 0) return (field);

        char ch;
        boolean isDigit, isPastEnd;
        int indexStart = 0;
        int state = BOUNDARY;
        StringBuffer sb = new StringBuffer();

        for (int src = 0; state != DONE; ++src) {
            isPastEnd = src >= len;
            ch = !isPastEnd ? field.charAt(src) : UNDERSCORE;
            isDigit = Character.isDigit(ch);

            switch (state) {
                case BOUNDARY:
                    if (isDigit) {
                        state = POSSIBLE_INDEX;
                        indexStart = sb.length();
                    } else {
                        state = NON_INDEX;
                    }
                    break;
                case NON_INDEX:
                    if (ch == UNDERSCORE) {
                        state = BOUNDARY;
                    }
                    break;
                case POSSIBLE_INDEX:

                    if (ch == UNDERSCORE) {
                        if (indexStart == 0) {
                            // we found an index at the start of
                            // the string; let's remove it and the
                            // underscore after it.
                            sb.delete(0, sb.length());
                            if (!isPastEnd) continue;
                        }
                        // we found an index either in the middle
                        // or at the end of the string; let's
                        // remove it and the underscore before
                        // it.
                        else {
                            sb.delete(indexStart - 1, sb.length());
                        }
                        state = BOUNDARY;

                    } else if (!isDigit) {
                        // it wasn't an index after all
                        state = NON_INDEX;
                    }
                    // else if still a digit, then
                    // it's still a possible index
                    break;
            } // switch

            if (!isPastEnd) {
                sb.append(ch);
            } else {
                state = DONE;
            }

        } // for

        return sb.toString();

    } // removeIndices
}

/* Copyright 2006 CyberSource Corporation */
