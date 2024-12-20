import com.cybersource.ics.base.exception.ICSException;
import com.cybersource.ics.base.message.ICSOffer;
import com.cybersource.ics.base.message.ICSReply;
import com.cybersource.ics.client.message.ICSClientRequest;
import com.cybersource.ws.client.Client;
import com.cybersource.ws.client.ClientException;
import com.cybersource.ws.client.FaultException;
import java.io.*;
import java.util.*;

public class Util {
    private static final Properties requestConversionTable;
    private static final Properties responseConversionTable;
    private static final Map<String, String> ICS_APPLICATIONS_LOOKUP_TABLE = new HashMap<>();
    public static final String RECORD_SEPARATOR = "\n";
    public static final String VALUE_SEPARATOR = "=";
    public static final List<String> FIELDS_TO_MASK = Arrays.asList("customer_cc_number", "customer_cc_expyr","customer_cc_expmo","customer_firstname",
            "customer_lastname","bill_address1", "bill_address2", "customer_phone","customer_email","ecp_account_no","ecp_rdfi","billTo_email"
    ,"billTo_lastName","billTo_street1","billTo_street2","billTo_firstName","card_expirationMonth","card_expirationYear","billTo_phoneNumber","card_accountNumber",
    "check_bankTransitNumber","check_accountNumber","ecp_authenticate_id","check_authenticateID","ecp_check_no","check_checkNumber");

    static{
        requestConversionTable = readPropertyFile("scmp_so_mapping.properties");
        responseConversionTable = readPropertyFile("so_scmp_response_mapping.properties");
        setupICSApplicationsLookUpTable();
    }
    public static final String ICS_PAYPAL_CREATE_AGREEMENT = "ics_paypal_create_agreement";
    public static final String ICS_PAYPAL_EC_DO_PAYMENT = "ics_paypal_ec_do_payment";
    public static final String ICS_PAYPAL_EC_GET_DETAILS = "ics_paypal_ec_get_details";
    public static final String ICS_PAYPAL_EC_ORDER_SETUP = "ics_paypal_ec_order_setup";
    public static final String ICS_PAYPAL_EC_SET = "ics_paypal_ec_set";
    public static final String SO_PAYPAL_CREATE_AGREEMENT_SERVICE_PAYPAL_TOKEN = "payPalCreateAgreementService_paypalToken";
    public static final String SO_PAYPAL_EC_DO_PAYMENT_SERVICE_PAYPAL_TOKEN = "payPalEcDoPaymentService_paypalToken";
    public static final String SO_PAYPAL_EC_GET_DETAILS_SERVICE_PAYPAL_TOKEN = "payPalEcGetDetailsService_paypalToken";
    public static final String SO_PAYPAL_EC_ORDER_SETUP_SERVICE_PAYPAL_TOKEN = "payPalEcOrderSetupService_paypalToken";
    public static final String SO_PAYPAL_EC_SET_SERVICE_PAYPAL_TOKEN = "payPalEcSetService_paypalToken";
    public static final String SO_REQUEST_PURCHASE_TOTALS_CURRENCY = "purchaseTotals_currency";
    public static final String SO_REQUEST_CHECK_ACCOUNT_ENCODER_ID = "check_accountEncoderID";
    public static final String SO_REQUEST_CARD_ACCOUNT_ENCODER_ID = "card_accountEncoderID";
    public static final String SO_REQUEST_AUTH_SERVICE_AGGREGATOR_ID ="ccAuthService_aggregatorID";
    public static final String SO_REQUEST_CREDIT_SERVICE_AGGREGATOR_ID ="ccCreditService_aggregatorID";
    public static final String SO_REQUEST_AUTH_SERVICE_AGGREGATOR_NAME = "ccAuthService_aggregatorName";
    public static final String SO_REQUEST_CREDIT_SERVICE_AGGREGATOR_NAME ="ccCreditService_aggregatorName";
    public static final String SO_REQUEST_AP_AUTH_REVERSAL_SERVICE_AUTH_REQUEST_ID = "apAuthReversalService_authRequestID";
    public static final String SO_REQUEST_AP_CAPTURE_SERVICE_AUTH_REQUEST_ID = "apCaptureService_authRequestID";
    public static final String SO_REQUEST_AP_CHECK_STATUS_SERVICE_INITIATE_REQUEST_ID = "apCheckStatusService_apInitiateRequestID";
    public static final String SO_REQUEST_AP_REFUND_SERVICE_INITIATE_REQUEST_ID = "apRefundService_apInitiateRequestID";
    public static final String SO_REQUEST_AUTO_AUTH_REVERSAL_SERVICE_AUTH_CODE = "ccAutoAuthReversalService_authCode";
    public static final String SO_REQUEST_CREDIT_SERVICE_AUTH_CODE = "ccCreditService_authCode";
    public static final String SO_REQUEST_AUTH_REVERSAL_SERVICE_AUTH_REQUEST_ID = "ccAuthReversalService_authRequestID";
    public static final String SO_REQUEST_AUTO_AUTH_REVERSAL_SERVICE_AUTH_REQUEST_ID = "ccAutoAuthReversalService_authRequestID";
    public static final String SO_REQUEST_CAPTURE_SERVICE_AUTH_REQUEST_ID = "ccCaptureService_authRequestID";
    public static final String SO_REQUEST_AUTH_REVERSAL_SERVICE_AUTH_REQUEST_TOKEN = "ccAuthReversalService_authRequestToken";
    public static final String SO_REQUEST_CAPTURE_SERVICE_AUTH_REQUEST_TOKEN = "ccCaptureService_authRequestToken";
    public static final String SO_REQUEST_AUTH_SERVICE_RECONCILIATION_ID = "ccAuthService_reconciliationID";
    public static final String SO_REQUEST_AUTO_AUTH_REVERSAL_SERVICE_RECONCILIATION_ID = "ccAutoAuthReversalService_reconciliationID";
    public static final String SO_REQUEST_AUTH_SERVICE_AUTH_TYPE = "ccAuthService_authType";
    public static final String SO_REQUEST_CAPTURE_SERVICE_AUTH_TYPE = "ccCaptureService_authType";
    public static final String SO_REQUEST_AUTH_SERVICE_BILL_PAYMENT = "ccAuthService_billPayment";
    public static final String SO_REQUEST_AUTO_AUTH_REVERSAL_SERVICE_BILL_PAYMENT = "ccAutoAuthReversalService_billPayment";
    public static final String SO_REQUEST_CREDIT_SERVICE_BILL_PAYMENT = "ccCreditService_billPayment";
    public static final String SO_REQUEST_CREDIT_SERVICE_CAPTURE_REQUEST_ID = "ccCreditService_captureRequestID";
    public static final String SO_REQUEST_DCC_UPDATE_SERVICE_CAPTURE_REQUEST_ID = "ccDCCUpdateService_captureRequestID";
    public static final String SO_REQUEST_AUTH_SERVICE_CHECKSUM_KEY = "ccAuthService_checksumKey";
    public static final String SO_REQUEST_CAPTURE_SERVICE_CHECKSUM_KEY = "ccCaptureService_checksumKey";
    public static final String SO_REQUEST_CREDIT_SERVICE_CHECKSUM_KEY = "ccCreditService_checksumKey";
    public static final String SO_REQUEST_PAYPAL_TRANSACTION_SEARCH_SERVICE_CURRENCY = "payPalTransactionSearchService_currency";
    public static final String SO_REQUEST_DIRECT_DEBIT_REFUND_SERVICE_MANDATE_AUTHENTICATION_DATE = "directDebitRefundService_mandateAuthenticationDate";
    public static final String SO_REQUEST_DIRECT_DEBIT_SERVICE_MANDATE_AUTHENTICATION_DATE = "directDebitService_mandateAuthenticationDate";
    public static final String SO_REQUEST_DIRECT_DEBIT_REFUND_SERVICE_RECURRING_TYPE = "directDebitRefundService_recurringType";
    public static final String SO_REQUEST_DIRECT_DEBIT_SERVICE_RECURRING_TYPE = "directDebitService_recurringType";
    public static final String SO_REQUEST_DIRECT_DEBIT_REFUND_SERVICE_DIRECT_DEBIT_TYPE = "directDebitRefundService_directDebitType";
    public static final String SO_REQUEST_DIRECT_DEBIT_SERVICE_DIRECT_DEBIT_TYPE = "directDebitService_directDebitType";
    public static final String SO_REQUEST_AUTH_SERVICE_COMMERCE_INDICATOR = "ccAuthService_commerceIndicator";
    public static final String SO_REQUEST_AUTO_AUTH_REVERSAL_SERVICE_COMMERCE_INDICATOR = "ccAutoAuthReversalService_commerceIndicator";
    public static final String SO_REQUEST_CREDIT_SERVICE_COMMERCE_INDICATOR = "ccCreditService_commerceIndicator";
    public static final String SO_REQUEST_ECP_CREDIT_SERVICE_COMMERCE_INDICATOR = "ecCreditService_commerceIndicator";
    public static final String SO_REQUEST_ECP_DEBIT_SERVICE_COMMERCE_INDICATOR = "ecDebitService_commerceIndicator";
    public static final String SO_REQUEST_PIN_DEBIT_CREDIT_SERVICE_COMMERCE_INDICATOR = "pinDebitCreditService_commerceIndicator";
    public static final String SO_REQUEST_PIN_DEBIT_SERVICE_COMMERCE_INDICATOR = "pinDebitPurchaseService_commerceIndicator";
    public static final String SO_REQUEST_PIN_LESS_DEBIT_SERVICE_COMMERCE_INDICATOR = "pinlessDebitService_commerceIndicator";
    public static final String SO_REQUEST_ECP_CREDIT_SERVICE_DEBIT_REQUEST_ID="ecCreditService_debitRequestID";
    public static final String SO_REQUEST_ECP_DEBIT_SERVICE_DEBIT_REQUEST_ID="ecDebitService_debitRequestID";
    public static final String SO_REQUEST_ECP_CREDIT_SERVICE_TRANSACTION_TOKEN="ecCreditService_transactionToken";
    public static final String SO_REQUEST_ECP_DEBIT_SERVICE_TRANSACTION_TOKEN="ecDebitService_transactionToken";
    public static final String SO_REQUEST_ECP_AUTHENTICATE_SERVICE_REFERENCE_NUMBER="ecAuthenticateService_referenceNumber";
    public static final String SO_REQUEST_ECP_CREDIT_SERVICE_REFERENCE_NUMBER="ecCreditService_referenceNumber";
    public static final String SO_REQUEST_ECP_DEBIT_SERVICE_REFERENCE_NUMBER="ecDebitService_referenceNumber";
    public static final String SO_REQUEST_ECP_CREDIT_SERVICE_SETTLEMENT_METHOD="ecCreditService_settlementMethod";
    public static final String SO_REQUEST_ECP_DEBIT_SERVICE_SETTLEMENT_METHOD="ecDebitService_settlementMethod";
    public static final String SO_REQUEST_PAYPAL_TRANSACTION_SEARCH_SERVICE_GRAND_TOTAL_AMOUNT="payPalTransactionSearchService_grandTotalAmount";
    public static final String SO_REQUEST_PURCHASE_TOTALS_GRAND_TOTAL_AMOUNT="purchaseTotals_grandTotalAmount";
    public static final String SO_REQUEST_AUTH_SERVICE_INDUSTRY_DATA_TYPE="ccAuthService_industryDatatype";
    public static final String SO_REQUEST_CAPTURE_SERVICE_INDUSTRY_DATA_TYPE="ccCaptureService_industryDatatype";
    public static final String SO_REQUEST_CREDIT_SERVICE_INDUSTRY_DATA_TYPE="ccCreditService_industryDatatype";
    public static final String SO_REQUEST_DIRECT_DEBIT_REFUND_SERVICE_MANDATE_ID="directDebitRefundService_mandateID";
    public static final String SO_REQUEST_DIRECT_DEBIT_SERVICE_MANDATE_ID="directDebitService_mandateID";
    public static final String SO_REQUEST_FRAUD_UPDATE_SERVICE_MARKING_NOTES = "fraudUpdateService_markingNotes";
    public static final String SO_REQUEST_RISK_UPDATE_SERVICE_MARKING_NOTES = "riskUpdateService_markingNotes";
    public static final String SO_REQUEST_FRAUD_UPDATE_SERVICE_MARKING_REASON = "fraudUpdateService_markingReason";
    public static final String SO_REQUEST_RISK_UPDATE_SERVICE_MARKING_REASON = "riskUpdateService_markingReason";
    public static final String SO_REQUEST_CAPTURE_SERVICE_MERCHANT_RECEIPT_NUMBER="ccCaptureService_merchantReceiptNumber";
    public static final String SO_REQUEST_CREDIT_SERVICE_MERCHANT_RECEIPT_NUMBER="ccCreditService_merchantReceiptNumber";
    public static final String SO_REQUEST_PIN_DEBIT_CREDIT_SERVICE_NETWORK_ORDER = "pinDebitCreditService_networkOrder";
    public static final String SO_REQUEST_PIN_DEBIT_SERVICE_NETWORK_ORDER = "pinDebitPurchaseService_networkOrder";
    public static final String SO_REQUEST_CAPTURE_SERVICE_PARTIAL_PAYMENT_ID="ccCaptureService_partialPaymentID";
    public static final String SO_REQUEST_CREDIT_SERVICE_PARTIAL_PAYMENT_ID="ccCreditService_partialPaymentID";
    public static final String SO_REQUEST_ECP_CREDIT_SERVICE_PARTIAL_PAYMENT_ID="ecCreditService_partialPaymentID";
    public static final String SO_REQUEST_ECP_DEBIT_SERVICE_PARTIAL_PAYMENT_ID="ecDebitService_partialPaymentID";
    public static final String SO_REQUEST_PAYPAL_AUTH_REVERSAL_SERVICE_PAYPAL_AUTHORIZATION_ID="payPalAuthReversalService_paypalAuthorizationId";
    public static final String SO_REQUEST_PAYPAL_DO_CAPTURE_SERVICE_PAYPAL_AUTHORIZATION_ID="payPalDoCaptureService_paypalAuthorizationId";
    public static final String SO_REQUEST_PAYPAL_AUTH_REVERSAL_SERVICE_PAYPAL_AUTHORIZATION_REQUEST_ID="payPalAuthReversalService_paypalAuthorizationRequestID";
    public static final String SO_REQUEST_PAYPAL_DO_CAPTURE_SERVICE_PAYPAL_AUTHORIZATION_REQUEST_ID="payPalDoCaptureService_paypalAuthorizationRequestID";
    public static final String SO_REQUEST_PAYPAL_AUTH_REVERSAL_SERVICE_PAYPAL_AUTHORIZATION_REQUEST_TOKEN="payPalAuthReversalService_paypalAuthorizationRequestToken";
    public static final String SO_REQUEST_PAYPAL_DO_CAPTURE_SERVICE_PAYPAL_AUTHORIZATION_REQUEST_TOKEN="payPalDoCaptureService_paypalAuthorizationRequestToken";
    public static final String SO_REQUEST_PAYPAL_EC_SET_SERVICE_PAYPAL_BILLING_AGREEMENT_CUSTOM="payPalEcSetService_paypalBillingAgreementCustom";
    public static final String SO_REQUEST_PAYPAL_UPDATE_AGREEMENT_SERVICE_PAYPAL_BILLING_AGREEMENT_CUSTOM="payPalUpdateAgreementService_paypalBillingAgreementCustom";
    public static final String SO_REQUEST_PAYPAL_DO_REF_TRANSACTION_SERVICE_PAYPAL_BILLING_AGREEMENT_ID="payPalDoRefTransactionService_paypalBillingAgreementId";
    public static final String SO_REQUEST_PAYPAL_UPDATE_AGREEMENT_SERVICE_PAYPAL_BILLING_AGREEMENT_ID="payPalUpdateAgreementService_paypalBillingAgreementId";
    public static final String SO_REQUEST_PAYPAL_AUTHORIZATION_SERVICE_PAYPAL_CUSTOMER_EMAIL="payPalAuthorizationService_paypalCustomerEmail";
    public static final String SO_REQUEST_PAYPAL_EC_DO_PAYMENT_SERVICE_PAYPAL_CUSTOMER_EMAIL="payPalEcDoPaymentService_paypalCustomerEmail";
    public static final String SO_REQUEST_PAYPAL_EC_ORDER_SETUP_SERVICE_PAYPAL_CUSTOMER_EMAIL="payPalEcOrderSetupService_paypalCustomerEmail";
    public static final String SO_REQUEST_PAYPAL_EC_SET_SERVICE_PAYPAL_CUSTOMER_EMAIL="payPalEcSetService_paypalCustomerEmail";
    public static final String SO_REQUEST_PAYPAL_TRANSACTION_SEARCH_SERVICE_PAYPAL_CUSTOMER_EMAIL="payPalTransactionSearchService_paypalCustomerEmail";
    public static final String SO_REQUEST_PAYPAL_DO_REF_TRANSACTION_SERVICE_PAYPAL_DESC="payPalDoRefTransactionService_paypalDesc";
    public static final String SO_REQUEST_PAYPAL_EC_DO_PAYMENT_SERVICE_PAYPAL_DESC="payPalEcDoPaymentService_paypalDesc";
    public static final String SO_REQUEST_PAYPAL_EC_ORDER_SETUP_SERVICE_PAYPAL_DESC="payPalEcOrderSetupService_paypalDesc";
    public static final String SO_REQUEST_PAYPAL_EC_SET_SERVICE_PAYPAL_DESC="payPalEcSetService_paypalDesc";
    public static final String SO_REQUEST_PAYPAL_AUTH_REVERSAL_SERVICE_PAYPAL_EC_DO_PAYMENT_REQUEST_ID="payPalAuthReversalService_paypalEcDoPaymentRequestID";
    public static final String SO_REQUEST_PAYPAL_DO_CAPTURE_SERVICE_PAYPAL_EC_DO_PAYMENT_REQUEST_ID="payPalDoCaptureService_paypalEcDoPaymentRequestID";
    public static final String SO_REQUEST_PAYPAL_AUTH_REVERSAL_SERVICE_PAYPAL_EC_DO_PAYMENT_REQUEST_TOKEN="payPalAuthReversalService_paypalEcDoPaymentRequestToken";
    public static final String SO_REQUEST_PAYPAL_DO_CAPTURE_SERVICE_PAYPAL_EC_DO_PAYMENT_REQUEST_TOKEN="payPalDoCaptureService_paypalEcDoPaymentRequestToken";
    public static final String SO_REQUEST_PAYPAL_AUTH_SERVICE_PAYPAL_EC_ORDER_SETUP_REQUEST_ID="payPalAuthorizationService_paypalEcOrderSetupRequestID";
    public static final String SO_REQUEST_PAYPAL_AUTH_REVERSAL_SERVICE_PAYPAL_EC_ORDER_SETUP_REQUEST_ID="payPalAuthReversalService_paypalEcOrderSetupRequestID";
    public static final String SO_REQUEST_PAYPAL_AUTHORIZATION_SERVICE_PAYPAL_EC_ORDER_SETUP_REQUEST_TOKEN="payPalAuthorizationService_paypalEcOrderSetupRequestToken";
    public static final String SO_REQUEST_PAYPAL_AUTH_REVERSAL_SERVICE_PAYPAL_EC_ORDER_SETUP_REQUEST_TOKEN="payPalAuthReversalService_paypalEcOrderSetupRequestToken";
    public static final String SO_REQUEST_PAYPAL_CREATE_AGREEMENT_SERVICE_PAYPAL_EC_SET_REQUEST_ID="payPalCreateAgreementService_paypalEcSetRequestID";
    public static final String SO_REQUEST_PAYPAL_EC_DO_PAYMENT_SERVICE_PAYPAL_EC_SET_REQUEST_ID="payPalEcDoPaymentService_paypalEcSetRequestID";
    public static final String SO_REQUEST_PAYPAL_EC_GET_DETAILS_SERVICE_PAYPAL_EC_SET_REQUEST_ID="payPalEcGetDetailsService_paypalEcSetRequestID";
    public static final String SO_REQUEST_PAYPAL_EC_ORDER_SETUP_SERVICE_PAYPAL_EC_SET_REQUEST_ID="payPalEcOrderSetupService_paypalEcSetRequestID";
    public static final String SO_REQUEST_PAYPAL_EC_SET_SERVICE_PAYPAL_EC_SET_REQUEST_ID="payPalEcSetService_paypalEcSetRequestID";
    public static final String SO_REQUEST_PAYPAL_CREATE_AGREEMENT_SERVICE_PAYPAL_EC_SET_REQUEST_TOKEN="payPalCreateAgreementService_paypalEcSetRequestToken";
    public static final String SO_REQUEST_PAYPAL_EC_DO_PAYMENT_SERVICE_PAYPAL_EC_SET_REQUEST_TOKEN="payPalEcDoPaymentService_paypalEcSetRequestToken";
    public static final String SO_REQUEST_PAYPAL_EC_GET_DETAILS_SERVICE_PAYPAL_EC_SET_REQUEST_TOKEN="payPalEcGetDetailsService_paypalEcSetRequestToken";
    public static final String SO_REQUEST_PAYPAL_EC_ORDER_SETUP_SERVICE_PAYPAL_EC_SET_REQUEST_TOKEN="payPalEcOrderSetupService_paypalEcSetRequestToken";
    public static final String SO_REQUEST_PAYPAL_EC_SET_SERVICE_PAYPAL_EC_SET_REQUEST_TOKEN="payPalEcSetService_paypalEcSetRequestToken";
    public static final String SO_REQUEST_PAYPAL_DO_CAPTURE_SERVICE_INVOICE_NUMBER="payPalDoCaptureService_invoiceNumber";
    public static final String SO_REQUEST_PAYPAL_DO_REF_TRANSACTION_SERVICE_INVOICE_NUMBER="payPalDoRefTransactionService_invoiceNumber";
    public static final String SO_REQUEST_PAYPAL_EC_DO_PAYMENT_SERVICE_INVOICE_NUMBER="payPalEcDoPaymentService_invoiceNumber";
    public static final String SO_REQUEST_PAYPAL_EC_ORDER_SETUP_SERVICE_INVOICE_NUMBER="payPalEcOrderSetupService_invoiceNumber";
    public static final String SO_REQUEST_PAYPAL_EC_SET_SERVICE_INVOICE_NUMBER="payPalEcSetService_invoiceNumber";
    public static final String SO_REQUEST_PAYPAL_TRANSACTION_SEARCH_SERVICE_INVOICE_NUMBER="payPalTransactionSearchService_invoiceNumber";
    public static final String SO_REQUEST_PAYPAL_PRE_APPROVED_PAYMENT_SERVICE_MP_ID="payPalPreapprovedPaymentService_mpID";
    public static final String SO_REQUEST_PAYPAL_PRE_APPROVED_UPDATE_SERVICE_MP_ID="payPalPreapprovedUpdateService_mpID";
    public static final String SO_REQUEST_PAYPAL_EC_DO_PAYMENT_SERVICE_PAYPAL_PAYER_ID="payPalEcDoPaymentService_paypalPayerId";
    public static final String SO_REQUEST_PAYPAL_EC_ORDER_SETUP_SERVICE_PAYPAL_PAYER_ID="payPalEcOrderSetupService_paypalPayerId";
    public static final String SO_REQUEST_PAYPAL_DO_REF_TRANSACTION_SERVICE_PAYPAL_PAYMENT_TYPE="payPalDoRefTransactionService_paypalPaymentType";
    public static final String SO_REQUEST_PAYPAL_EC_SET_SERVICE_PAYPAL_PAYMENT_TYPE="payPalEcSetService_paypalPaymentType";
    public static final String SO_REQUEST_PAYPAL_DO_REF_TRANSACTION_SERVICE_PAYPAL_REQ_CONFIRM_SHIPPING="payPalDoRefTransactionService_paypalReqconfirmshipping";
    public static final String SO_REQUEST_PAYPAL_EC_SET_SERVICE_PAYPAL_REQ_CONFIRM_SHIPPING="payPalEcSetService_paypalReqconfirmshipping";
    public static final String SO_REQUEST_PAYPAL_GET_TRANSACTION_DETAILS_SERVICE_TRANSACTION_ID="payPalGetTxnDetailsService_transactionID";
    public static final String SO_REQUEST_PAYPAL_TRANSACTION_SEARCH_SERVICE_TRANSACTION_ID="payPalTransactionSearchService_transactionID";
    public static final String SO_REQUEST_CAPTURE_SERVICE_PURCHASING_LEVEL="ccCreditService_purchasingLevel";
    public static final String SO_REQUEST_CREDIT_SERVICE_PURCHASING_LEVEL="ccCreditService_purchasingLevel";

    // Keys for line item/product fields
    public static final String SO_REQUEST_ITEM_UNIT_PRICE = "unitPrice";
    public static final String SO_REQUEST_ITEM_ITEM = "item";
    public static final String SO_REQUEST_ITEM_PRODUCT_SKU = "productSKU";
    public static final String SO_REQUEST_ITEM_PRODUCT_CODE = "productCode";
    public static final String SO_REQUEST_ITEM_PRODUCT_DESCRIPTION = "productDescription";
    public static final String SO_REQUEST_ITEM_PRODUCT_NAME = "productName";
    public static final String SO_REQUEST_ITEM_PRODUCT_RISK = "productRisk";
    public static final String SO_REQUEST_ITEM_PRODUCT_TAX_AMOUNT = "taxAmount";
    public static final String SO_REQUEST_ITEM_PRODUCT_CITY_OVERRIDE_AMOUNT = "cityOverrideAmount";
    public static final String SO_REQUEST_ITEM_CITY_OVERRIDE_RATE = "cityOverrideRate";
    public static final String SO_REQUEST_ITEM_COUNTY_OVERRIDE_AMOUNT = "countyOverrideAmount";
    public static final String SO_REQUEST_ITEM_COUNTY_OVERRIDE_RATE = "countyOverrideRate";
    public static final String SO_REQUEST_ITEM_DISTRICT_OVERRIDE_AMOUNT = "districtOverrideAmount";
    public static final String SO_REQUEST_ITEM_DISTRICT_OVERRIDE_RATE = "districtOverrideRate";
    public static final String SO_REQUEST_ITEM_STATE_OVERRIDE_AMOUNT = "stateOverrideAmount";
    public static final String SO_REQUEST_ITEM_STATE_OVERRIDE_RATE = "stateOverrideRate";
    public static final String SO_REQUEST_ITEM_COUNTRY_OVERRIDE_AMOUNT = "countryOverrideAmount";
    public static final String SO_REQUEST_ITEM_COUNTRY_OVERRIDE_RATE = "countryOverrideRate";

    //Keys for Order acceptance items
    public static final String SO_REQUEST_ORDER_ACCEPTANCE_CITY = "orderAcceptanceCity";
    public static final String SO_REQUEST_ORDER_ACCEPTANCE_COUNTY = "orderAcceptanceCounty";
    public static final String SO_REQUEST_ORDER_ACCEPTANCE_COUNTRY = "orderAcceptanceCountry";
    public static final String SO_REQUEST_ORDER_ACCEPTANCE_STATE = "orderAcceptanceState";
    public static final String SO_REQUEST_ORDER_ACCEPTANCE_POSTAL_CODE = "orderAcceptancePostalCode";

    public static final String SO_REQUEST_ORDER_ORIGIN_CITY = "orderOriginCity";
    public static final String SO_REQUEST_ORDER_ORIGIN_COUNTY = "orderOriginCounty";
    public static final String SO_REQUEST_ORDER_ORIGIN_COUNTRY = "orderOriginCountry";
    public static final String SO_REQUEST_ORDER_ORIGIN_STATE = "orderOriginState";
    public static final String SO_REQUEST_ORDER_ORIGIN_POSTAL_CODE = "orderOriginPostalCode";

    //Keys for ship from
    public static final String SO_REQUEST_ORDER_SHIP_FROM_CITY = "shipFromCity";
    public static final String SO_REQUEST_ORDER_SHIP_FROM_COUNTY = "shipFromCounty";
    public static final String SO_REQUEST_ORDER_SHIP_FROM_COUNTRY = "shipFromCountry";
    public static final String SO_REQUEST_ORDER_SHIP_FROM_STATE = "shipFromState";
    public static final String SO_REQUEST_ORDER_SHIP_FROM_POSTAL_CODE = "shipFromPostalCode";

    ///Keys for export item details
    public static final String SO_REQUEST_ITEM_EXPORT = "export";
    public static final String SO_REQUEST_ITEM_NO_EXPORT = "noExport";
    public static final String SO_REQUEST_ITEM_NATIONAL_TAX = "nationalTax";
    public static final String SO_REQUEST_ITEM_VAT_RATE = "vatRate";

    // Key for buyer registration list of items
    public static final String SO_REQUEST_ITEM_BUYER_REGISTRATION = "buyerRegistration";
    public static final String SO_REQUEST_ITEM_MIDDLEMAN_REGISTRATION = "middlemanRegistration";

    public static final String SO_REQUEST_ITEM_SCORE_GIFT_CATEGORY = "giftCategory";
    public static final String SO_REQUEST_ITEM_SCORE_TIME_CATEGORY = "timeCategory";
    public static final String SO_REQUEST_ITEM_SCORE_HOST_HEDGE = "hostHedge";
    public static final String SO_REQUEST_ITEM_SCORE_TIME_HEDGE = "timeHedge";
    public static final String SO_REQUEST_ITEM_SCORE_VELOCITY_HEDGE = "velocityHedge";
    public static final String SO_REQUEST_ITEM_SCORE_NONSENSICAL_HEDGE = "nonsensicalHedge";
    public static final String SO_REQUEST_ITEM_SCORE_PHONE_HEDGE = "phoneHedge";
    public static final String SO_REQUEST_ITEM_SCORE_OBSCENITIES_HEDGE = "obscenitiesHedge";
    public static final String SO_REQUEST_ITEM_UNIT_OF_MEASURE = "unitOfMeasure";
    public static final String SO_REQUEST_ITEM_TAX_RATE = "taxRate";
    public static final String SO_REQUEST_ITEM_TOTAL_AMOUNT = "totalAmount";
    public static final String SO_REQUEST_ITEM_DISCOUNT_AMOUNT = "discountAmount";
    public static final String SO_REQUEST_ITEM_DISCOUNT_RATE = "discountRate";
    public static final String SO_REQUEST_ITEM_COMMODITY_CODE = "commodityCode";
    public static final String SO_REQUEST_ITEM_GROSS_NET_INDICATOR = "grossNetIndicator";
    public static final String SO_REQUEST_ITEM_TAX_TYPE_APPLIED = "taxTypeApplied";
    public static final String SO_REQUEST_ITEM_DISCOUNT_INDICATOR = "discountIndicator";
    public static final String SO_REQUEST_ITEM_ALTERNATE_TAX_ID = "alternateTaxID";
    public static final String SO_REQUEST_ITEM_ALTERNATE_TAX_AMOUNT = "alternateTaxAmount";
    public static final String SO_REQUEST_ITEM_ALTERNATE_TAX_TYPE_APPLIED = "alternateTaxTypeApplied";
    public static final String SO_REQUEST_ITEM_ALTERNATE_TAX_RATE = "alternateTaxRate";
    public static final String SO_REQUEST_ITEM_ALTERNATE_TAX_TYPE_IDENTIFIER = "alternateTaxType";
    public static final String SO_REQUEST_ITEM_LOCAL_TAX = "localTax";
    public static final String SO_REQUEST_ITEM_ZERO_COST_TO_CUSTOMER_INDICATOR = "zeroCostToCustomerIndicator";
    public static final String SO_REQUEST_ITEM_PASSENGER_FIRST_NAME = "passengerFirstName";
    public static final String SO_REQUEST_ITEM_PASSENGER_LAST_NAME = "passengerLastName";
    public static final String SO_REQUEST_ITEM_PASSENGER_ID = "passengerID";
    public static final String SO_REQUEST_ITEM_PASSENGER_STATUS = "passengerStatus";
    public static final String SO_REQUEST_ITEM_PASSENGER_TYPE = "passengerType";
    public static final String SO_REQUEST_ITEM_PASSENGER_EMAIL = "passengerEmail";
    public static final String SO_REQUEST_ITEM_PASSENGER_PHONE = "passengerPhone";
    public static final String SO_REQUEST_ITEM_INVOICE_NUMBER = "invoiceNumber";
    public static final String SO_REQUEST_ITEM_SELLER_REGISTRATION = "sellerRegistration";
    public static final String SO_REQUEST_ITEM_POINT_OF_TITLE_TRANSFER = "pointOfTitleTransfer";

    // Keys for SCMP request
    public static final String SCMP_REQUEST_ICS_APPLICATIONS = "ics_applications";
    public static final String SCM_REQUEST_PAYPAL_TOKEN = "paypal_token";
    public static final String SCMP_REQUEST_PAYPAL_TRANSACTION_ID = "paypal_transaction_id";
    public static final String SCMP_REQUEST_ACCOUNT_ENCODER_ID = "account_encoder_id";
    public static final String SCMP_REQUEST_BANK_TRANSIT_NUMBER = "ecp_rdfi";
    public static final String SCMP_REQUEST_AGGREGATOR_ID = "aggregator_id";
    public static final String SCMP_REQUEST_AGGREGATOR_NAME = "aggregator_name";
    public static final String SCMP_REQUEST_ICS_AUTH = "ics_auth";
    public static final String SCMP_REQUEST_ICS_CREDIT = "ics_credit";
    public static final String SCMP_REQUEST_AP_AUTH_REQUEST_ID = "ap_auth_request_id";
    public static final String SCMP_REQUEST_ICS_AP_AUTH_REVERSAL = "ics_ap_auth_reversal";
    public static final String SCMP_REQUEST_ICS_AP_CAPTURE = "ics_ap_capture";
    public static final String SCMP_REQUEST_AP_INITIATE_REQUEST_ID = "ap_initiate_request_id";
    public static final String SCMP_REQUEST_ICS_AP_CHECK_STATUS = "ics_ap_check_status";
    public static final String SCMP_REQUEST_ICS_AP_REFUND= "ics_ap_refund";
    public static final String SCMP_REQUEST_AUTH_CODE = "auth_code";
    public static final String SCMP_REQUEST_ICS_AUTO_AUTH_REVERSAL = "ics_auto_auth_reversal";
    public static final String SCMP_REQUEST_AUTH_REQUEST_ID = "auth_request_id";
    public static final String SCMP_REQUEST_ICS_AUTH_REVERSAL = "ics_auth_reversal";
    public static final String SCMP_REQUEST_ICS_CAPTURE = "ics_bill";
    public static final String SCMP_REQUEST_AUTH_REQUEST_TOKEN = "auth_request_token";
    public static final String SCMP_REQUEST_AUTH_TRANS_REF_NO = "auth_trans_ref_no";
    public static final String SCMP_REQUEST_AUTH_TYPE = "auth_type";
    public static final String SCMP_REQUEST_BILL_PAYMENT = "bill_payment";
    public static final String SCMP_REQUEST_BILL_REQUEST_ID = "bill_request_id";
    public static final String SCMP_REQUEST_ICS_DCC_UPDATE = "ics_dcc_update";
    public static final String SCMP_REQUEST_CHECKSUM_KEY = "checksum_key";
    public static final String SCMP_REQUEST_CURRENCY = "currency";
    public static final String SCMP_REQUEST_ICS_PAYPAL_TRANSACTION_SEARCH = "ics_paypal_transaction_search";
    public static final String SCMP_REQUEST_ICS_PAYPAL_AUTH_REVERSAL="ics_paypal_auth_reversal";
    public static final String SCMP_REQUEST_ICS_PAYPAL_EC_SET="ics_paypal_ec_set";
    public static final String SCMP_REQUEST_ICS_PAYPAL_UPDATE_AGREEMENT="ics_paypal_update_agreement";
    public static final String SCMP_REQUEST_ICS_PAYPAL_CREATE_AGREEMENT="ics_paypal_create_agreement";
    public static final String SCMP_REQUEST_ICS_PAYPAL_DO_CAPTURE = "ics_paypal_do_capture";
    public static final String SCMP_REQUEST_ICS_PAYPAL_AUTH="ics_paypal_authorization";
    public static final String SCMP_REQUEST_ICS_PAYPAL_EC_DO_PAYMENT="ics_paypal_ec_do_payment";
    public static final String SCMP_REQUEST_ICS_PAYPAL_EC_ORDER_SETUP="ics_paypal_ec_order_setup";
    public static final String SCMP_REQUEST_ICS_PAYPAL_DO_REF_TRANSACTION="ics_paypal_do_ref_transaction";
    public static final String SCMP_REQUEST_ICS_PAYPAL_EC_GET_DETAILS="ics_paypal_ec_get_details";
    public static final String SCMP_REQUEST_ICS_PAYPAL_PRE_APPROVED_PAYMENT="ics_paypal_preapproved_payment";
    public static final String SCMP_REQUEST_ICS_PAYPAL_PRE_APPROVED_UPDATE="ics_paypal_preapproved_update";
    public static final String SCMP_REQUEST_ICS_PAYPAL_GET_TRANSACTION_DETAILS="ics_paypal_get_txn_details";
    public static final String SCMP_REQUEST_PURCHASING_LEVEL="purchasing_level";
    public static final String SCMP_REQUEST_DIRECT_DEBIT_MANDATE_AUTHENTICATION_DATE = "direct_debit_mandate_authentication_date";
    public static final String SCMP_REQUEST_ICS_DIRECT_DEBIT_REFUND = "ics_direct_debit_refund";
    public static final String SCMP_REQUEST_ICS_DIRECT_DEBIT = "SCMP_REQUEST_ICS_DIRECT_DEBIT";
    public static final String SCMP_REQUEST_DIRECT_DEBIT_RECURRING_TYPE = "direct_debit_recurring_type";
    public static final String SCMP_REQUEST_DIRECT_DEBIT_TYPE = "direct_debit_type";
    public static final String SCMP_REQUEST_ECOMMERCE_INDICATOR = "e_commerce_indicator";
    public static final String SCMP_REQUEST_ICS_ECP_CREDIT="ics_ecp_credit";
    public static final String SCMP_REQUEST_ICS_ECP_AUTHENTICATE = "ics_ecp_authenticate";
    public static final String SCMP_REQUEST_ICS_ECP_DEBIT="ics_ecp_debit";
    public static final String SCMP_REQUEST_ICS_PIN_DEBIT_CREDIT="ics_pin_debit_credit";
    public static final String SCMP_REQUEST_ICS_PIN_DEBIT="ics_pin_debit";
    public static final String SCMP_REQUEST_ICS_PIN_LESS_DEBIT="ics_pinless_debit";
    public static final String SCMP_REQUEST_ICS_FRAUD_UPDATE="ics_ifs_update";
    public static final String SCMP_REQUEST_ICS_RISK_UPDATE="ics_risk_update";
    public static final String SCMP_REQUEST_DEBIT_REQUEST_ID = "ecp_debit_request_id";
    public static final String SCMP_REQUEST_ECP_PAYMENT_KEY = "ecp_payment_key";
    public static final String SCMP_REQUEST_ECP_REF_NO = "ecp_ref_no";
    public static final String SCMP_REQUEST_ECP_SETTLEMENT_METHOD = "ecp_settlement_method";
    public static final String SCMP_REQUEST_GRAND_TOTAL_AMOUNT = "grand_total_amount";
    public static final String SCMP_REQUEST_INDUSTRY_DATA_TYPE = "industry_datatype";
    public static final String SCMP_REQUEST_MANDATE_ID = "mandate_id";
    public static final String SCMP_REQUEST_MARKING_NOTES = "marking_notes";
    public static final String SCMP_REQUEST_MARKING_REASON = "marking_reason";
    public static final String SCMP_REQUEST_MERCHANT_RECEIPT_NUMBER = "merchant_receipt_number";
    public static final String SCMP_REQUEST_NETWORK_ORDER="network_order";
    public static final String SCMP_REQUEST_PARTIAL_PAYMENT_ID="partial_payment_id";
    public static final String SCMP_REQUEST_PAYPAL_AUTHORIZATION_ID="paypal_authorization_id";
    public static final String SCMP_REQUEST_PAYPAL_AUTHORIZATION_REQUEST_ID="paypal_authorization_request_id";
    public static final String SCMP_REQUEST_PAYPAL_AUTHORIZATION_REQUEST_TOKEN="paypal_authorization_request_token";
    public static final String SCMP_REQUEST_PAYPAL_BILLING_AGREEMENT_CUSTOM="paypal_billing_agreement_custom";
    public static final String SCMP_REQUEST_PAYPAL_BILLING_AGREEMENT_ID="paypal_billing_agreement_id";
    public static final String SCMP_REQUEST_PAYPAL_CUSTOMER_EMAIL="paypal_customer_email";
    public static final String SCMP_REQUEST_PAYPAL_DESC="paypal_desc";
    public static final String SCMP_REQUEST_PAYPAL_EC_DO_PAYMENT_REQUEST_ID="paypal_ec_do_payment_request_id";
    public static final String SCMP_REQUEST_PAYPAL_EC_DO_PAYMENT_REQUEST_TOKEN="paypal_ec_do_payment_request_token";
    public static final String SCMP_REQUEST_PAYPAL_EC_ORDER_SETUP_REQUEST_ID="paypal_ec_order_setup_request_id";
    public static final String SCMP_REQUEST_PAYPAL_EC_ORDER_SETUP_REQUEST_TOKEN="paypal_ec_order_setup_request_token";

    public static final String SCMP_REQUEST_PAYPAL_EC_SET_REQUEST_ID="paypal_ec_set_request_id";
    public static final String SCMP_REQUEST_PAYPAL_EC_SET_REQUEST_TOKEN="paypal_ec_set_request_token";

    public static final String  SCMP_REQUEST_PAYPAL_INVOICE_NUMBER="paypal_invoice_number";
    public static final String  SCMP_REQUEST_PAYPAL_MP_ID="paypal_mp_id";
    public static final String  SCMP_REQUEST_PAYPAL_PAYER_ID="paypal_payer_id";

    public static final String  SCMP_REQUEST_PAYPAL_PAYMENT_TYPE="paypal_payer_type";

    public static final String  SCMP_REQUEST_PAYPAL_REQ_CONFIRM_SHIPPING="paypal_reqconfirmshipping";
    // Fields for ICSOffer item
    public static final String SCMP_REQUEST_PRODUCT_NAME = "product_name";
    public static final String SCMP_REQUEST_ITEM_PRODUCT_DESCRIPTION = "product_description";
    public static final String SCMP_REQUEST_PRODUCT_CODE = "product_code";
    public static final String SCMP_REQUEST_MERCHANT_PRODUCT_SKU = "merchant_product_sku";
    public static final String SCMP_REQUEST_AMOUNT = "amount";
    public static final String SCMP_REQUEST_QUANTITY = "quantity";
    public static final String SCMP_REQUEST_PRODUCT_RISK = "product_risk";
    public static final String SCMP_REQUEST_TAX_AMOUNT = "tax_amount";
    public static final String SCMP_REQUEST_CITY_OVERRIDE_AMOUNT = "city_override_amount";
    public static final String SCMP_REQUEST_CITY_OVERRIDE_RATE = "city_override_rate";
    public static final String SCMP_REQUEST_COUNTY_OVERRIDE_AMOUNT = "county_override_amount";
    public static final String SCMP_REQUEST_COUNTY_OVERRIDE_RATE = "county_override_rate";
    public static final String SCMP_REQUEST_DISTRICT_OVERRIDE_AMOUNT = "district_override_amount";
    public static final String SCMP_REQUEST_DISTRICT_OVERRIDE_RATE = "district_override_rate";
    public static final String SCMP_REQUEST_STATE_OVERRIDE_AMOUNT = "state_override_amount";
    public static final String SCMP_REQUEST_STATE_OVERRIDE_RATE = "state_override_rate";
    public static final String SCMP_REQUEST_COUNTRY_OVERRIDE_AMOUNT = "country_override_amount";
    public static final String SCMP_REQUEST_COUNTRY_OVERRIDE_RATE = "country_override_rate";

    //Fields for Acceptance Order items
    public static final String SCMP_REQUEST_ORDER_ACCEPTANCE_CITY = "order_acceptance_city";
    public static final String SCMP_REQUEST_ORDER_ACCEPTANCE_COUNTY = "order_acceptance_county";
    public static final String SCMP_REQUEST_ORDER_ACCEPTANCE_COUNTRY = "order_acceptance_country";
    public static final String SCMP_REQUEST_ORDER_ACCEPTANCE_STATE = "order_acceptance_state";
    public static final String SCMP_REQUEST_ORDER_ACCEPTANCE_ZIP = "order_acceptance_zip";

    //Keys for order origin items
    public static final String SCMP_REQUEST_ORDER_ORIGIN_CITY = "order_origin_city";
    public static final String SCMP_REQUEST_ORDER_ORIGIN_COUNTY = "order_origin_county";
    public static final String SCMP_REQUEST_ORDER_ORIGIN_COUNTRY = "order_origin_country";
    public static final String SCMP_REQUEST_ORDER_ORIGIN_STATE = "order_origin_state";
    public static final String SCMP_REQUEST_ORDER_ORIGIN_ZIP = "order_origin_zip";

    //Keys for ship from items
    public static final String SCMP_REQUEST_SHIP_FROM_CITY = "ship_from_city";
    public static final String SCMP_REQUEST_SHIP_FROM_COUNTY = "ship_from_county";
    public static final String SCMP_REQUEST_SHIP_FROM_COUNTRY = "ship_from_country";
    public static final String SCMP_REQUEST_SHIP_FROM_STATE = "ship_from_state";
    public static final String SCMP_REQUEST_SHIP_FROM_ZIP = "ship_from_zip";

    // Keys for export items
    public static final String SCMP_REQUEST_ITEM_EXPORT = "export";
    public static final String SCMP_REQUEST_ITEM_NO_EXPORT = "no_export";
    public static final String SCMP_REQUEST_ITEM_NATIONAL_TAX = "national_tax";
    public static final String SCMP_REQUEST_ITEM_VAT_RATE = "vat_rate";

    //Key for buyer registration items
    public static final String SCMP_REQUEST_ITEM_BUYER_REGISTRATION = "buyer_registration";
    public static final String SCMP_REQUEST_ITEM_MIDDLEMAN_REGISTRATION = "middleman_registration";

    public static final String SCMP_REQUEST_ITEM_SCORE_CATEGORY_GIFT = "score_category_gift";
    public static final String SCMP_REQUEST_ITEM_SCORE_CATEGORY_TIME = "score_category_time";
    public static final String SCMP_REQUEST_ITEM_SCORE_HOST_HEDGE = "score_host_hedge";
    public static final String SCMP_REQUEST_ITEM_SCORE_TIME_HEDGE = "score_time_hedge";
    public static final String SCMP_REQUEST_ITEM_SCORE_VELOCITY_HEDGE = "score_velocity_hedge";
    public static final String SCMP_REQUEST_ITEM_SCORE_NONSENSICAL_HEDGE = "score_nonsensical_hedge";
    public static final String SCMP_REQUEST_ITEM_SCORE_PHONE_HEDGE = "score_phone_hedge";
    public static final String SCMP_REQUEST_ITEM_SCORE_OBSCENITIES_HEDGE = "score_obscenities_hedge";
    public static final String SCMP_REQUEST_ITEM_UNIT_OF_MEASURE = "unit_of_measure";
    public static final String SCMP_REQUEST_ITEM_TAX_RATE = "tax_rate";
    public static final String SCMP_REQUEST_ITEM_TOTAL_AMOUNT = "total_amount";
    public static final String SCMP_REQUEST_ITEM_DISCOUNT_AMOUNT = "discount_amount";
    public static final String SCMP_REQUEST_ITEM_DISCOUNT_RATE = "discount_rate";
    public static final String SCMP_REQUEST_ITEM_COMMODITY_CODE = "commodity_code";
    public static final String SCMP_REQUEST_ITEM_GROSS_NET_INDICATOR = "gross_net_indicator";
    public static final String SCMP_REQUEST_ITEM_TAX_TYPE_APPLIED = "tax_type_applied";
    public static final String SCMP_REQUEST_ITEM_DISCOUNT_INDICATOR = "discount_indicator";
    public static final String SCMP_REQUEST_ITEM_ALTERNATE_TAX_ID = "alternate_tax_id";
    public static final String SCMP_REQUEST_ITEM_ALTERNATE_TAX_AMOUNT = "alternate_tax_amount";
    public static final String SCMP_REQUEST_ITEM_ALTERNATE_TAX_TYPE_APPLIED = "alternate_tax_type_applied";
    public static final String SCMP_REQUEST_ITEM_ALTERNATE_TAX_RATE = "alternate_tax_rate";
    public static final String SCMP_REQUEST_ITEM_ALTERNATE_TAX_TYPE_IDENTIFIER = "alternate_tax_type_identifier";
    public static final String SCMP_REQUEST_ITEM_LOCAL_TAX = "local_tax";
    public static final String SCMP_REQUEST_ITEM_ZERO_COST_TO_CUSTOMER_INDICATOR = "zero_cost_to_customer_indicator";
    public static final String SCMP_REQUEST_ITEM_PASSENGER_FIRST_NAME = "passenger_firstname";
    public static final String SCMP_REQUEST_ITEM_PASSENGER_LAST_NAME = "passenger_lastname";
    public static final String SCMP_REQUEST_ITEM_PASSENGER_ID = "passenger_id";
    public static final String SCMP_REQUEST_ITEM_PASSENGER_STATUS = "passenger_status";
    public static final String SCMP_REQUEST_ITEM_PASSENGER_TYPE = "passenger_type";
    public static final String SCMP_REQUEST_ITEM_PASSENGER_EMAIL = "passenger_email";
    public static final String SCMP_REQUEST_ITEM_PASSENGER_PHONE = "passenger_phone";
    public static final String SCMP_REQUEST_ITEM_INVOICE_NUMBER = "invoice_number";
    public static final String SCMP_REQUEST_ITEM_SELLER_REGISTRATION = "seller_registration_";
    public static final String SCMP_REQUEST_ITEM_POINT_OF_TITLE_TRANSFER = "point_of_title_transfer";

    public static final String SO_RESPONSE_PAYMENT_TYPE_INDICATOR ="paymentTypeIndicator";
    public static final String SO_RESPONSE_CC_CREDIT_REPLY_RECONCILIATION_REFERENCE_NUMBER ="ccCreditReply_reconciliationReferenceNumber";
    public static final String SO_RESPONSE_CC_AUTH_REPLY_MERCHANT_ADVICE_CODE ="ccAuthReply_merchantAdviceCode";
    public static final String SO_RESPONSE_CC_AUTH_REPLY_MERCHANT_ADVICE_CODE_RAW ="ccAuthReply_merchantAdviceCodeRaw";

    public static final String SO_RESPONSE_AUTH_PAYMENT_TYPE_INDICATOR ="auth_payment_type_indicator";
    public static final String SO_RESPONSE_AUTH_REVERSAL_PAYMENT_TYPE_INDICATOR ="auth_reversal_payment_type_indicator";
    public static final String SO_RESPONSE_CREDIT_AUTH_RECONCILIATION_REFERENCE_NUMBER ="credit_auth_reconciliation_reference_number";
    public static final String SO_RESPONSE_CREDIT_RECONCILIATION_REFERENCE_NUMBER ="credit_reconciliation_reference_number";
    public static final String SO_RESPONSE_AUTH_MERCHANT_ADVICE_CODE ="auth_merchant_advice_code";
    public static final String SO_RESPONSE_MERCHANT_ADVICE_CODE ="merchant_advice_code";
    public static final String SO_RESPONSE_AUTH_MERCHANT_ADVICE_CODE_RAW ="auth_merchant_advice_code_raw";
    public static final String SO_RESPONSE_MERCHANT_ADVICE_CODE_RAW ="merchant_advice_code_raw";

    /**
     * Set up the lookup table of ics_applications from the ics_applications.properties file
     */
    public static void setupICSApplicationsLookUpTable(){
        Properties icsApplicationsProperties = readPropertyFile("ics_applications.properties");
        if(icsApplicationsProperties == null){
            System.out.println("Cannot load ics_applications.properties file");
            return;
        }
        for(String key: icsApplicationsProperties.stringPropertyNames()){
            String value = icsApplicationsProperties.getProperty(key);
            if(value != null && !value.isEmpty()){
                ICS_APPLICATIONS_LOOKUP_TABLE.put(key, value);
            }
            else{
                System.out.println("ics_applications: " + key + " has no mapping");
            }
        }
    }

    /**
     * Process the given {@code ICSClientRequest} as a simple order request
     * @param icsClientRequest the ICSClientRequest object to process
     * @param cybsProperties the {@code Properties} object that contains the Simple Order service access credentials
     * @return an {@code ICSReply} object
     */
    public static ICSReply processRequest(ICSClientRequest icsClientRequest, Properties cybsProperties){
        if(icsClientRequest == null){
            System.out.println("Cannot process null ICS client request");
            return null;
        }
        if(cybsProperties == null){
            System.out.println("Cannot process null cybsProperties file");
            return null;
        }
        Map<String, String> nvpRequest = Util.convertICSClientRequestToNVPRequest(icsClientRequest);
        Util.displayMap("\n\nSIMPLE ORDER REQUEST:", nvpRequest) ;
        ICSReply icsReply = new ICSReply();
        try {
            Map<String, String> nvpResponse = Client.runTransaction(nvpRequest, cybsProperties);
            Util.displayMap("\nSIMPLE ORDER REPLY:", nvpResponse);
            icsReply = Util.convertNVPResponseToICSReply(nvpResponse, icsClientRequest);
        } catch (FaultException | ClientException e) {
            System.out.println("Error processing Simple Order request" + e.getMessage());
            return null;
        }
        return icsReply;
    }

    /**
     * Converts an SCMP request to a simple order NVP request
     * @param icsClientRequest the SCMP request to convert
     * @return a {@code HashMap} representing the simple order NVP request
     */
    public static Map<String, String> convertICSClientRequestToNVPRequest(ICSClientRequest icsClientRequest){
        if(icsClientRequest == null){
            System.out.println("Cannot convert null ICSClientRequest");
            return null;
        }

        //In case Developers don't want to use this Util class but only grab this method to their code, then we need to setup the ics_applications lookup table
        if(ICS_APPLICATIONS_LOOKUP_TABLE == null || ICS_APPLICATIONS_LOOKUP_TABLE.isEmpty()){
            setupICSApplicationsLookUpTable();
        }
        HashMap<String, String> nvpRequest = new HashMap<>();
        String icsApplications = icsClientRequest.getField(SCMP_REQUEST_ICS_APPLICATIONS);
        List<String> icsApplicationsList = new ArrayList<String>();


        if(icsApplications != null){
            if(icsApplications.contains(",")){
                //this is a bundle call(ex. auth+capture or sale)
                for(String icsApp:icsApplications.split(",")){
                    String icsApplication=ICS_APPLICATIONS_LOOKUP_TABLE.get(icsApp.trim());
                    if(icsApplication != null && !icsApplication.isEmpty()){
                        nvpRequest.put(icsApplication, "true");
                        icsApplicationsList.add(icsApplication);
                    }
                    else{
                        System.out.println("ics_applications: " + icsApp + " has no mapping. Check the ics_applications.properties file");
                    }
                }
            }
            else{
                String icsApplication=ICS_APPLICATIONS_LOOKUP_TABLE.get(icsApplications.trim());
                if(icsApplication != null && !icsApplication.isEmpty()){
                    nvpRequest.put(icsApplication, "true");
                    icsApplicationsList.add(icsApplication.trim());
                }
                else{
                    System.out.println("ics_applications: " + icsApplication + " has no mapping. Check the ics_applications.properties file");
                }
            }
        }
        else{
            System.out.println(SCMP_REQUEST_ICS_APPLICATIONS + "=null");
        }
        //handle all the offer items
        mapSCMPOffersToSimpleOrderItem(icsClientRequest, nvpRequest);

        //process the rest of the fields
        Hashtable<String, String> icsRequestHashtable = icsClientRequest.getHashtable();
        Enumeration<String> e = icsRequestHashtable.keys();
        while (e.hasMoreElements()) {
            String scmpKey = e.nextElement();
            String scmpValue = icsRequestHashtable.get(scmpKey);

            //get the equivalent Simple Order key for this SCMP key from the conversion table
            String nvpSOKey = requestConversionTable.getProperty(scmpKey);
            if(scmpValue == null || scmpValue.isEmpty()){
                System.out.println("SCMP Key=" + scmpKey + " has null value");
            }
            else if(nvpSOKey == null || nvpSOKey.isEmpty()){
                //check if this SCMP field has multiple mapping to Simple Order. If so, derive its value based on some conditions like the payment method used(ex. card, check), or the transaction type
                mapNonDistinctSCMPFields(icsClientRequest, scmpKey, icsApplicationsList, nvpRequest, scmpValue);
            }
            else {
                nvpRequest.put(nvpSOKey, scmpValue);
            }
        }
        return nvpRequest;
    }

    /**
     * Maps all SCMP fields that have multiple mappings to the simple order API
     * @param icsClientRequest SCMP request
     * @param scmpKey scmp key
     * @param icsApplications list of ics_applications
     * @param nvpRequest simple order NVP request
     * @param scmpValue SCMP value
     */
    public static void mapNonDistinctSCMPFields(ICSClientRequest icsClientRequest, String scmpKey, List<String> icsApplications, HashMap<String, String> nvpRequest, String scmpValue) {
        switch(scmpKey){
            case SCMP_REQUEST_ACCOUNT_ENCODER_ID:{
                if(icsClientRequest.getField(SCMP_REQUEST_BANK_TRANSIT_NUMBER) != null){
                    nvpRequest.put(SO_REQUEST_CHECK_ACCOUNT_ENCODER_ID, scmpValue);
                }
                else{
                    nvpRequest.put(SO_REQUEST_CARD_ACCOUNT_ENCODER_ID, scmpValue);
                }
            }break;
            case SCMP_REQUEST_AGGREGATOR_ID:{
                if(icsApplications.contains(SCMP_REQUEST_ICS_AUTH)){
                    nvpRequest.put(SO_REQUEST_AUTH_SERVICE_AGGREGATOR_ID, scmpValue);
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_CREDIT)){
                    nvpRequest.put(SO_REQUEST_CREDIT_SERVICE_AGGREGATOR_ID, scmpValue);
                }
            }break;
            case SCMP_REQUEST_AGGREGATOR_NAME:{
                if(icsApplications.contains(SCMP_REQUEST_ICS_AUTH)){
                    nvpRequest.put(SO_REQUEST_AUTH_SERVICE_AGGREGATOR_NAME, scmpValue);
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_CREDIT)){
                    nvpRequest.put(SO_REQUEST_CREDIT_SERVICE_AGGREGATOR_NAME, scmpValue);
                }
            }break;
            case SCMP_REQUEST_AP_AUTH_REQUEST_ID:{
                if(icsApplications.contains(SCMP_REQUEST_ICS_AP_AUTH_REVERSAL)){
                    nvpRequest.put(SO_REQUEST_AP_AUTH_REVERSAL_SERVICE_AUTH_REQUEST_ID, scmpValue);
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_AP_CAPTURE)){
                    nvpRequest.put(SO_REQUEST_AP_CAPTURE_SERVICE_AUTH_REQUEST_ID, scmpValue);
                }
            }break;
            case SCMP_REQUEST_AP_INITIATE_REQUEST_ID:{
                if(icsApplications.contains(SCMP_REQUEST_ICS_AP_CHECK_STATUS)){
                    nvpRequest.put(SO_REQUEST_AP_CHECK_STATUS_SERVICE_INITIATE_REQUEST_ID, scmpValue);
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_AP_REFUND)){
                    nvpRequest.put(SO_REQUEST_AP_REFUND_SERVICE_INITIATE_REQUEST_ID, scmpValue);
                }
            }break;
            case SCMP_REQUEST_AUTH_CODE:{
                if(icsApplications.contains(SCMP_REQUEST_ICS_AUTO_AUTH_REVERSAL)){
                    nvpRequest.put(SO_REQUEST_AUTO_AUTH_REVERSAL_SERVICE_AUTH_CODE, scmpValue);
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_CREDIT)){
                    nvpRequest.put(SO_REQUEST_CREDIT_SERVICE_AUTH_CODE, scmpValue);
                }
            }break;
            case SCMP_REQUEST_AUTH_REQUEST_ID:{
                if(icsApplications.contains(SCMP_REQUEST_ICS_AUTH_REVERSAL)){
                    nvpRequest.put(SO_REQUEST_AUTH_REVERSAL_SERVICE_AUTH_REQUEST_ID, scmpValue);
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_AUTO_AUTH_REVERSAL)){
                    nvpRequest.put(SO_REQUEST_AUTO_AUTH_REVERSAL_SERVICE_AUTH_REQUEST_ID, scmpValue);
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_CAPTURE)){
                    nvpRequest.put(SO_REQUEST_CAPTURE_SERVICE_AUTH_REQUEST_ID, scmpValue);
                }
            }break;
            case SCMP_REQUEST_AUTH_REQUEST_TOKEN:{
                if(icsApplications.contains(SCMP_REQUEST_ICS_AUTH_REVERSAL)){
                    nvpRequest.put(SO_REQUEST_AUTH_REVERSAL_SERVICE_AUTH_REQUEST_TOKEN, scmpValue);
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_CAPTURE)){
                    nvpRequest.put(SO_REQUEST_CAPTURE_SERVICE_AUTH_REQUEST_TOKEN, scmpValue);
                }
            }break;
            case SCMP_REQUEST_AUTH_TRANS_REF_NO:{
                if(icsApplications.contains(SCMP_REQUEST_ICS_AUTH)){
                    nvpRequest.put(SO_REQUEST_AUTH_SERVICE_RECONCILIATION_ID, scmpValue);
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_AUTO_AUTH_REVERSAL)){
                    nvpRequest.put(SO_REQUEST_AUTO_AUTH_REVERSAL_SERVICE_RECONCILIATION_ID, scmpValue);
                }
            }break;
            case SCMP_REQUEST_AUTH_TYPE:{
                if(icsApplications.contains(SCMP_REQUEST_ICS_AUTH)){
                    nvpRequest.put(SO_REQUEST_AUTH_SERVICE_AUTH_TYPE, scmpValue);
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_CAPTURE)){
                    nvpRequest.put(SO_REQUEST_CAPTURE_SERVICE_AUTH_TYPE, scmpValue);
                }
            }break;
            case SCMP_REQUEST_BILL_PAYMENT:{
                if(icsApplications.contains(SCMP_REQUEST_ICS_AUTH)){
                    nvpRequest.put(SO_REQUEST_AUTH_SERVICE_BILL_PAYMENT, scmpValue);
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_AUTO_AUTH_REVERSAL)){
                    nvpRequest.put(SO_REQUEST_AUTO_AUTH_REVERSAL_SERVICE_BILL_PAYMENT, scmpValue);
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_CREDIT)){
                    nvpRequest.put(SO_REQUEST_CREDIT_SERVICE_BILL_PAYMENT, scmpValue);
                }
            }break;
            case SCMP_REQUEST_BILL_REQUEST_ID:{
                if(icsApplications.contains(SCMP_REQUEST_ICS_CREDIT)){
                    nvpRequest.put(SO_REQUEST_CREDIT_SERVICE_CAPTURE_REQUEST_ID, scmpValue);
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_AUTO_AUTH_REVERSAL)){
                    nvpRequest.put(SO_REQUEST_AUTO_AUTH_REVERSAL_SERVICE_BILL_PAYMENT, scmpValue);
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_DCC_UPDATE)){
                    nvpRequest.put(SO_REQUEST_DCC_UPDATE_SERVICE_CAPTURE_REQUEST_ID, scmpValue);
                }
            }break;
            case SCMP_REQUEST_CHECKSUM_KEY:{
                if(icsApplications.contains(SCMP_REQUEST_ICS_AUTH)){
                    nvpRequest.put(SO_REQUEST_AUTH_SERVICE_CHECKSUM_KEY, scmpValue);
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_CAPTURE)){
                    nvpRequest.put(SO_REQUEST_CAPTURE_SERVICE_CHECKSUM_KEY, scmpValue);
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_CREDIT)){
                    nvpRequest.put(SO_REQUEST_CREDIT_SERVICE_CHECKSUM_KEY, scmpValue);
                }
            }break;
            case SCMP_REQUEST_CURRENCY:{
                if(icsApplications.contains(SCMP_REQUEST_ICS_PAYPAL_TRANSACTION_SEARCH)){
                    nvpRequest.put(SO_REQUEST_PAYPAL_TRANSACTION_SEARCH_SERVICE_CURRENCY, scmpValue);
                }
                else  {
                    nvpRequest.put(SO_REQUEST_PURCHASE_TOTALS_CURRENCY, scmpValue);
                }
            }break;
            case SCMP_REQUEST_DIRECT_DEBIT_MANDATE_AUTHENTICATION_DATE:{
                if(icsApplications.contains(SCMP_REQUEST_ICS_DIRECT_DEBIT_REFUND)){
                    nvpRequest.put(SO_REQUEST_DIRECT_DEBIT_REFUND_SERVICE_MANDATE_AUTHENTICATION_DATE, scmpValue);//
                }
                else  {
                    nvpRequest.put(SO_REQUEST_DIRECT_DEBIT_SERVICE_MANDATE_AUTHENTICATION_DATE, scmpValue);
                }
            }break;
            case SCMP_REQUEST_DIRECT_DEBIT_RECURRING_TYPE:{
                if(icsApplications.contains(SCMP_REQUEST_ICS_DIRECT_DEBIT_REFUND)){
                    nvpRequest.put(SO_REQUEST_DIRECT_DEBIT_REFUND_SERVICE_RECURRING_TYPE, scmpValue);//
                }
                else  {
                    nvpRequest.put(SO_REQUEST_DIRECT_DEBIT_SERVICE_RECURRING_TYPE, scmpValue);
                }
            }break;
            case SCMP_REQUEST_DIRECT_DEBIT_TYPE:{
                if(icsApplications.contains(SCMP_REQUEST_ICS_DIRECT_DEBIT_REFUND)){
                    nvpRequest.put(SO_REQUEST_DIRECT_DEBIT_REFUND_SERVICE_DIRECT_DEBIT_TYPE, scmpValue);//
                }
                else  {
                    nvpRequest.put(SO_REQUEST_DIRECT_DEBIT_SERVICE_DIRECT_DEBIT_TYPE, scmpValue);
                }
            }break;
            case SCMP_REQUEST_ECOMMERCE_INDICATOR:{
                if(icsApplications.contains(SCMP_REQUEST_ICS_AUTH)){
                    nvpRequest.put(SO_REQUEST_AUTH_SERVICE_COMMERCE_INDICATOR, scmpValue);//
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_AUTO_AUTH_REVERSAL)){
                    nvpRequest.put(SO_REQUEST_AUTO_AUTH_REVERSAL_SERVICE_COMMERCE_INDICATOR, scmpValue);
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_CREDIT)){
                    nvpRequest.put(SO_REQUEST_CREDIT_SERVICE_COMMERCE_INDICATOR, scmpValue);
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_ECP_CREDIT)){
                    nvpRequest.put(SO_REQUEST_ECP_CREDIT_SERVICE_COMMERCE_INDICATOR, scmpValue);
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_ECP_DEBIT)){
                    nvpRequest.put(SO_REQUEST_ECP_DEBIT_SERVICE_COMMERCE_INDICATOR, scmpValue);
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_PIN_DEBIT_CREDIT)){
                    nvpRequest.put(SO_REQUEST_PIN_DEBIT_CREDIT_SERVICE_COMMERCE_INDICATOR, scmpValue);
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_PIN_DEBIT)){
                    nvpRequest.put(SO_REQUEST_PIN_DEBIT_SERVICE_COMMERCE_INDICATOR, scmpValue);
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_PIN_LESS_DEBIT)){
                    nvpRequest.put(SO_REQUEST_PIN_LESS_DEBIT_SERVICE_COMMERCE_INDICATOR, scmpValue);
                }
            }break;
            case SCMP_REQUEST_DEBIT_REQUEST_ID:{
                if(icsApplications.contains(SCMP_REQUEST_ICS_ECP_CREDIT)){
                    nvpRequest.put(SO_REQUEST_ECP_CREDIT_SERVICE_DEBIT_REQUEST_ID, scmpValue);//
                }
                else  {
                    nvpRequest.put(SO_REQUEST_ECP_DEBIT_SERVICE_DEBIT_REQUEST_ID, scmpValue);
                }
            }break;
            case SCMP_REQUEST_ECP_PAYMENT_KEY:{
                if(icsApplications.contains(SCMP_REQUEST_ICS_ECP_CREDIT)){
                    nvpRequest.put(SO_REQUEST_ECP_CREDIT_SERVICE_TRANSACTION_TOKEN, scmpValue);//
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_ECP_DEBIT)){
                    nvpRequest.put(SO_REQUEST_ECP_DEBIT_SERVICE_TRANSACTION_TOKEN, scmpValue);
                }
            }break;
            case SCMP_REQUEST_ECP_REF_NO:{
                if(icsApplications.contains(SCMP_REQUEST_ICS_ECP_AUTHENTICATE)){
                    nvpRequest.put(SO_REQUEST_ECP_AUTHENTICATE_SERVICE_REFERENCE_NUMBER, scmpValue);//
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_ECP_CREDIT)){
                    nvpRequest.put(SO_REQUEST_ECP_CREDIT_SERVICE_REFERENCE_NUMBER, scmpValue);
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_ECP_DEBIT)){
                    nvpRequest.put(SO_REQUEST_ECP_DEBIT_SERVICE_REFERENCE_NUMBER, scmpValue);
                }
            }break;
            case SCMP_REQUEST_ECP_SETTLEMENT_METHOD:{
                if(icsApplications.contains(SCMP_REQUEST_ICS_ECP_CREDIT)){
                    nvpRequest.put(SO_REQUEST_ECP_CREDIT_SERVICE_SETTLEMENT_METHOD, scmpValue);//
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_ECP_DEBIT)){
                    nvpRequest.put(SO_REQUEST_ECP_DEBIT_SERVICE_SETTLEMENT_METHOD, scmpValue);
                }
            }break;
            case SCMP_REQUEST_GRAND_TOTAL_AMOUNT:{
                if(icsApplications.contains(SCMP_REQUEST_ICS_PAYPAL_TRANSACTION_SEARCH)){
                    nvpRequest.put(SO_REQUEST_PAYPAL_TRANSACTION_SEARCH_SERVICE_GRAND_TOTAL_AMOUNT, scmpValue);//
                }
                else{
                    nvpRequest.put(SO_REQUEST_PURCHASE_TOTALS_GRAND_TOTAL_AMOUNT, scmpValue);
                }
            }break;
            case SCMP_REQUEST_INDUSTRY_DATA_TYPE:{
                if(icsApplications.contains(SCMP_REQUEST_ICS_AUTH)){
                    nvpRequest.put(SO_REQUEST_AUTH_SERVICE_INDUSTRY_DATA_TYPE, scmpValue);//
                }
                if(icsApplications.contains(SCMP_REQUEST_ICS_CAPTURE)){
                    nvpRequest.put(SO_REQUEST_CAPTURE_SERVICE_INDUSTRY_DATA_TYPE, scmpValue);
                }
                if(icsApplications.contains(SCMP_REQUEST_ICS_CREDIT)){
                    nvpRequest.put(SO_REQUEST_CREDIT_SERVICE_INDUSTRY_DATA_TYPE, scmpValue);
                }
            }break;
            case SCMP_REQUEST_MANDATE_ID:{
                if(icsApplications.contains(SCMP_REQUEST_ICS_DIRECT_DEBIT_REFUND)){
                    nvpRequest.put(SO_REQUEST_DIRECT_DEBIT_REFUND_SERVICE_MANDATE_ID, scmpValue);//
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_DIRECT_DEBIT)){
                    nvpRequest.put(SO_REQUEST_DIRECT_DEBIT_SERVICE_MANDATE_ID, scmpValue);
                }
            }break;
            case SCMP_REQUEST_MARKING_NOTES:{
                if(icsApplications.contains(SCMP_REQUEST_ICS_FRAUD_UPDATE)){
                    nvpRequest.put(SO_REQUEST_FRAUD_UPDATE_SERVICE_MARKING_NOTES, scmpValue);//
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_RISK_UPDATE)){
                    nvpRequest.put(SO_REQUEST_RISK_UPDATE_SERVICE_MARKING_NOTES, scmpValue);
                }
            }break;
            case SCMP_REQUEST_MARKING_REASON:{
                if(icsApplications.contains(SCMP_REQUEST_ICS_FRAUD_UPDATE)){
                    nvpRequest.put(SO_REQUEST_FRAUD_UPDATE_SERVICE_MARKING_REASON, scmpValue);//
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_RISK_UPDATE)){
                    nvpRequest.put(SO_REQUEST_RISK_UPDATE_SERVICE_MARKING_REASON, scmpValue);
                }
            }break;
            case SCMP_REQUEST_MERCHANT_RECEIPT_NUMBER:{
                if(icsApplications.contains(SCMP_REQUEST_ICS_CAPTURE)){
                    nvpRequest.put(SO_REQUEST_CAPTURE_SERVICE_MERCHANT_RECEIPT_NUMBER, scmpValue);//
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_CREDIT)){
                    nvpRequest.put(SO_REQUEST_CREDIT_SERVICE_MERCHANT_RECEIPT_NUMBER, scmpValue);
                }
            }break;
            case SCMP_REQUEST_NETWORK_ORDER:{
                if(icsApplications.contains(SCMP_REQUEST_ICS_PIN_DEBIT_CREDIT)){
                    nvpRequest.put(SO_REQUEST_PIN_DEBIT_CREDIT_SERVICE_NETWORK_ORDER, scmpValue);//
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_PIN_DEBIT)){
                    nvpRequest.put(SO_REQUEST_PIN_DEBIT_SERVICE_NETWORK_ORDER, scmpValue);
                }
            }break;
            case SCMP_REQUEST_PARTIAL_PAYMENT_ID:{
                if(icsApplications.contains(SCMP_REQUEST_ICS_CAPTURE)){
                    nvpRequest.put(SO_REQUEST_CAPTURE_SERVICE_PARTIAL_PAYMENT_ID, scmpValue);//
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_CREDIT)){
                    nvpRequest.put(SO_REQUEST_CREDIT_SERVICE_PARTIAL_PAYMENT_ID, scmpValue);
                }
                if(icsApplications.contains(SCMP_REQUEST_ICS_ECP_CREDIT)){
                    nvpRequest.put(SO_REQUEST_ECP_CREDIT_SERVICE_PARTIAL_PAYMENT_ID, scmpValue);//
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_ECP_DEBIT)){
                    nvpRequest.put(SO_REQUEST_ECP_DEBIT_SERVICE_PARTIAL_PAYMENT_ID, scmpValue);
                }
            }break;
            case SCMP_REQUEST_PAYPAL_AUTHORIZATION_ID:{
                if(icsApplications.contains(SCMP_REQUEST_ICS_PAYPAL_AUTH_REVERSAL)){
                    nvpRequest.put(SO_REQUEST_PAYPAL_AUTH_REVERSAL_SERVICE_PAYPAL_AUTHORIZATION_ID, scmpValue);//
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_PAYPAL_DO_CAPTURE)){
                    nvpRequest.put(SO_REQUEST_PAYPAL_DO_CAPTURE_SERVICE_PAYPAL_AUTHORIZATION_ID, scmpValue);
                }
            }break;
            case SCMP_REQUEST_PAYPAL_AUTHORIZATION_REQUEST_ID:{
                if(icsApplications.contains(SCMP_REQUEST_ICS_PAYPAL_AUTH_REVERSAL)){
                    nvpRequest.put(SO_REQUEST_PAYPAL_AUTH_REVERSAL_SERVICE_PAYPAL_AUTHORIZATION_REQUEST_ID, scmpValue);//
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_PAYPAL_DO_CAPTURE)){
                    nvpRequest.put(SO_REQUEST_PAYPAL_DO_CAPTURE_SERVICE_PAYPAL_AUTHORIZATION_REQUEST_ID, scmpValue);
                }
            }break;
            case SCMP_REQUEST_PAYPAL_AUTHORIZATION_REQUEST_TOKEN:{
                if(icsApplications.contains(SCMP_REQUEST_ICS_PAYPAL_AUTH_REVERSAL)){
                    nvpRequest.put(SO_REQUEST_PAYPAL_AUTH_REVERSAL_SERVICE_PAYPAL_AUTHORIZATION_REQUEST_TOKEN, scmpValue);//
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_PAYPAL_DO_CAPTURE)){
                    nvpRequest.put(SO_REQUEST_PAYPAL_DO_CAPTURE_SERVICE_PAYPAL_AUTHORIZATION_REQUEST_TOKEN, scmpValue);
                }
            }break;
            case SCMP_REQUEST_PAYPAL_BILLING_AGREEMENT_CUSTOM:{
                if(icsApplications.contains(SCMP_REQUEST_ICS_PAYPAL_EC_SET)){
                    nvpRequest.put(SO_REQUEST_PAYPAL_EC_SET_SERVICE_PAYPAL_BILLING_AGREEMENT_CUSTOM, scmpValue);//
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_PAYPAL_UPDATE_AGREEMENT)){
                    nvpRequest.put(SO_REQUEST_PAYPAL_UPDATE_AGREEMENT_SERVICE_PAYPAL_BILLING_AGREEMENT_CUSTOM, scmpValue);
                }
            }break;
            case SCMP_REQUEST_PAYPAL_BILLING_AGREEMENT_ID:{
                if(icsApplications.contains(SCMP_REQUEST_ICS_PAYPAL_EC_SET)){
                    nvpRequest.put(SO_REQUEST_PAYPAL_DO_REF_TRANSACTION_SERVICE_PAYPAL_BILLING_AGREEMENT_ID, scmpValue);//
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_PAYPAL_UPDATE_AGREEMENT)){
                    nvpRequest.put(SO_REQUEST_PAYPAL_UPDATE_AGREEMENT_SERVICE_PAYPAL_BILLING_AGREEMENT_ID, scmpValue);
                }
            }break;
            case SCMP_REQUEST_PAYPAL_CUSTOMER_EMAIL:{
                if(icsApplications.contains(SCMP_REQUEST_ICS_PAYPAL_AUTH)){
                    nvpRequest.put(SO_REQUEST_PAYPAL_AUTHORIZATION_SERVICE_PAYPAL_CUSTOMER_EMAIL, scmpValue);//
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_PAYPAL_EC_DO_PAYMENT)){
                    nvpRequest.put(SO_REQUEST_PAYPAL_EC_DO_PAYMENT_SERVICE_PAYPAL_CUSTOMER_EMAIL, scmpValue);
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_PAYPAL_EC_ORDER_SETUP)){
                    nvpRequest.put(SO_REQUEST_PAYPAL_EC_ORDER_SETUP_SERVICE_PAYPAL_CUSTOMER_EMAIL, scmpValue);
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_PAYPAL_EC_SET)){
                    nvpRequest.put(SO_REQUEST_PAYPAL_EC_SET_SERVICE_PAYPAL_CUSTOMER_EMAIL, scmpValue);
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_PAYPAL_TRANSACTION_SEARCH)){
                    nvpRequest.put(SO_REQUEST_PAYPAL_TRANSACTION_SEARCH_SERVICE_PAYPAL_CUSTOMER_EMAIL, scmpValue);
                }
            }break;
            case SCMP_REQUEST_PAYPAL_DESC:{
                if(icsApplications.contains(SCMP_REQUEST_ICS_PAYPAL_DO_REF_TRANSACTION)){
                    nvpRequest.put(SO_REQUEST_PAYPAL_DO_REF_TRANSACTION_SERVICE_PAYPAL_DESC, scmpValue);//
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_PAYPAL_EC_DO_PAYMENT)){
                    nvpRequest.put(SO_REQUEST_PAYPAL_EC_DO_PAYMENT_SERVICE_PAYPAL_DESC, scmpValue);
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_PAYPAL_EC_ORDER_SETUP)){
                    nvpRequest.put(SO_REQUEST_PAYPAL_EC_ORDER_SETUP_SERVICE_PAYPAL_DESC, scmpValue);
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_PAYPAL_EC_SET)){
                    nvpRequest.put(SO_REQUEST_PAYPAL_EC_SET_SERVICE_PAYPAL_DESC, scmpValue);
                }
            }break;
            case SCMP_REQUEST_PAYPAL_EC_DO_PAYMENT_REQUEST_ID:{
                if(icsApplications.contains(SCMP_REQUEST_ICS_PAYPAL_AUTH_REVERSAL)){
                    nvpRequest.put(SO_REQUEST_PAYPAL_AUTH_REVERSAL_SERVICE_PAYPAL_EC_DO_PAYMENT_REQUEST_ID, scmpValue);//
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_PAYPAL_DO_CAPTURE)){
                    nvpRequest.put(SO_REQUEST_PAYPAL_DO_CAPTURE_SERVICE_PAYPAL_EC_DO_PAYMENT_REQUEST_ID, scmpValue);
                }
            }break;
            case SCMP_REQUEST_PAYPAL_EC_DO_PAYMENT_REQUEST_TOKEN:{
                if(icsApplications.contains(SCMP_REQUEST_ICS_PAYPAL_AUTH_REVERSAL)){
                    nvpRequest.put(SO_REQUEST_PAYPAL_AUTH_REVERSAL_SERVICE_PAYPAL_EC_DO_PAYMENT_REQUEST_TOKEN, scmpValue);//
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_PAYPAL_DO_CAPTURE)){
                    nvpRequest.put(SO_REQUEST_PAYPAL_DO_CAPTURE_SERVICE_PAYPAL_EC_DO_PAYMENT_REQUEST_TOKEN, scmpValue);
                }
            }break;
            case SCMP_REQUEST_PAYPAL_EC_ORDER_SETUP_REQUEST_ID:{
                if(icsApplications.contains(SCMP_REQUEST_ICS_PAYPAL_AUTH)){
                    nvpRequest.put(SO_REQUEST_PAYPAL_AUTH_SERVICE_PAYPAL_EC_ORDER_SETUP_REQUEST_ID, scmpValue);//
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_PAYPAL_AUTH_REVERSAL)){
                    nvpRequest.put(SO_REQUEST_PAYPAL_AUTH_REVERSAL_SERVICE_PAYPAL_EC_ORDER_SETUP_REQUEST_ID, scmpValue);
                }
            }break;
            case SCMP_REQUEST_PAYPAL_EC_ORDER_SETUP_REQUEST_TOKEN:{
                if(icsApplications.contains(SCMP_REQUEST_ICS_PAYPAL_AUTH)){
                    nvpRequest.put(SO_REQUEST_PAYPAL_AUTHORIZATION_SERVICE_PAYPAL_EC_ORDER_SETUP_REQUEST_TOKEN, scmpValue);//
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_PAYPAL_AUTH_REVERSAL)){
                    nvpRequest.put(SO_REQUEST_PAYPAL_AUTH_REVERSAL_SERVICE_PAYPAL_EC_ORDER_SETUP_REQUEST_TOKEN, scmpValue);
                }
            }break;
            case SCMP_REQUEST_PAYPAL_EC_SET_REQUEST_ID:{
                if(icsApplications.contains(SCMP_REQUEST_ICS_PAYPAL_CREATE_AGREEMENT)){
                    nvpRequest.put(SO_REQUEST_PAYPAL_CREATE_AGREEMENT_SERVICE_PAYPAL_EC_SET_REQUEST_ID, scmpValue);//
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_PAYPAL_EC_DO_PAYMENT)){
                    nvpRequest.put(SO_REQUEST_PAYPAL_EC_DO_PAYMENT_SERVICE_PAYPAL_EC_SET_REQUEST_ID, scmpValue);
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_PAYPAL_EC_GET_DETAILS)){
                    nvpRequest.put(SO_REQUEST_PAYPAL_EC_GET_DETAILS_SERVICE_PAYPAL_EC_SET_REQUEST_ID, scmpValue);
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_PAYPAL_EC_ORDER_SETUP)){
                    nvpRequest.put(SO_REQUEST_PAYPAL_EC_ORDER_SETUP_SERVICE_PAYPAL_EC_SET_REQUEST_ID, scmpValue);
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_PAYPAL_EC_SET)){
                    nvpRequest.put(SO_REQUEST_PAYPAL_EC_SET_SERVICE_PAYPAL_EC_SET_REQUEST_ID, scmpValue);
                }
            }break;
            case SCMP_REQUEST_PAYPAL_EC_SET_REQUEST_TOKEN:{
                if(icsApplications.contains(SCMP_REQUEST_ICS_PAYPAL_CREATE_AGREEMENT)){
                    nvpRequest.put(SO_REQUEST_PAYPAL_CREATE_AGREEMENT_SERVICE_PAYPAL_EC_SET_REQUEST_TOKEN, scmpValue);//
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_PAYPAL_EC_DO_PAYMENT)){
                    nvpRequest.put(SO_REQUEST_PAYPAL_EC_DO_PAYMENT_SERVICE_PAYPAL_EC_SET_REQUEST_TOKEN, scmpValue);
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_PAYPAL_EC_GET_DETAILS)){
                    nvpRequest.put(SO_REQUEST_PAYPAL_EC_GET_DETAILS_SERVICE_PAYPAL_EC_SET_REQUEST_TOKEN, scmpValue);
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_PAYPAL_EC_ORDER_SETUP)){
                    nvpRequest.put(SO_REQUEST_PAYPAL_EC_ORDER_SETUP_SERVICE_PAYPAL_EC_SET_REQUEST_TOKEN, scmpValue);
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_PAYPAL_EC_SET)){
                    nvpRequest.put(SO_REQUEST_PAYPAL_EC_SET_SERVICE_PAYPAL_EC_SET_REQUEST_TOKEN, scmpValue);
                }
            }break;
            case SCMP_REQUEST_PAYPAL_INVOICE_NUMBER:{
                if(icsApplications.contains(SCMP_REQUEST_ICS_PAYPAL_DO_CAPTURE)){
                    nvpRequest.put(SO_REQUEST_PAYPAL_DO_CAPTURE_SERVICE_INVOICE_NUMBER, scmpValue);//
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_PAYPAL_DO_REF_TRANSACTION)){
                    nvpRequest.put(SO_REQUEST_PAYPAL_DO_REF_TRANSACTION_SERVICE_INVOICE_NUMBER, scmpValue);
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_PAYPAL_EC_DO_PAYMENT)){
                    nvpRequest.put(SO_REQUEST_PAYPAL_EC_DO_PAYMENT_SERVICE_INVOICE_NUMBER, scmpValue);
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_PAYPAL_EC_ORDER_SETUP)){
                    nvpRequest.put(SO_REQUEST_PAYPAL_EC_ORDER_SETUP_SERVICE_INVOICE_NUMBER, scmpValue);
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_PAYPAL_EC_SET)){
                    nvpRequest.put(SO_REQUEST_PAYPAL_EC_SET_SERVICE_INVOICE_NUMBER, scmpValue);
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_PAYPAL_TRANSACTION_SEARCH)){
                    nvpRequest.put(SO_REQUEST_PAYPAL_TRANSACTION_SEARCH_SERVICE_INVOICE_NUMBER, scmpValue);
                }
            }break;

            case SCMP_REQUEST_PAYPAL_MP_ID: {
                if (icsApplications.contains(SCMP_REQUEST_ICS_PAYPAL_PRE_APPROVED_PAYMENT)) {
                    nvpRequest.put(SO_REQUEST_PAYPAL_PRE_APPROVED_PAYMENT_SERVICE_MP_ID, scmpValue);//
                } else if (icsApplications.contains(SCMP_REQUEST_ICS_PAYPAL_PRE_APPROVED_UPDATE)) {
                    nvpRequest.put(SO_REQUEST_PAYPAL_PRE_APPROVED_UPDATE_SERVICE_MP_ID, scmpValue);
                }
            }break;
            case SCMP_REQUEST_PAYPAL_PAYER_ID:{
                if(icsApplications.contains(SCMP_REQUEST_ICS_PAYPAL_EC_DO_PAYMENT)){
                    nvpRequest.put(SO_REQUEST_PAYPAL_EC_DO_PAYMENT_SERVICE_PAYPAL_PAYER_ID, scmpValue);//
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_PAYPAL_EC_ORDER_SETUP)){
                    nvpRequest.put(SO_REQUEST_PAYPAL_EC_ORDER_SETUP_SERVICE_PAYPAL_PAYER_ID, scmpValue);
                }
            }break;
            case SCMP_REQUEST_PAYPAL_PAYMENT_TYPE:{
                if(icsApplications.contains(SCMP_REQUEST_ICS_PAYPAL_DO_REF_TRANSACTION)){
                    nvpRequest.put(SO_REQUEST_PAYPAL_DO_REF_TRANSACTION_SERVICE_PAYPAL_PAYMENT_TYPE, scmpValue);//
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_PAYPAL_EC_SET)){
                    nvpRequest.put(SO_REQUEST_PAYPAL_EC_SET_SERVICE_PAYPAL_PAYMENT_TYPE, scmpValue);
                }
            }break;
            case SCMP_REQUEST_PAYPAL_REQ_CONFIRM_SHIPPING:{
                if(icsApplications.contains(SCMP_REQUEST_ICS_PAYPAL_DO_REF_TRANSACTION)){
                    nvpRequest.put(SO_REQUEST_PAYPAL_DO_REF_TRANSACTION_SERVICE_PAYPAL_REQ_CONFIRM_SHIPPING, scmpValue);//
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_PAYPAL_EC_SET)){
                    nvpRequest.put(SO_REQUEST_PAYPAL_EC_SET_SERVICE_PAYPAL_REQ_CONFIRM_SHIPPING, scmpValue);
                }
            }break;
            case SCMP_REQUEST_PAYPAL_TRANSACTION_ID:{
                if(icsApplications.contains(SCMP_REQUEST_ICS_PAYPAL_GET_TRANSACTION_DETAILS)){
                    nvpRequest.put(SO_REQUEST_PAYPAL_GET_TRANSACTION_DETAILS_SERVICE_TRANSACTION_ID, scmpValue);//
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_PAYPAL_TRANSACTION_SEARCH)){
                    nvpRequest.put(SO_REQUEST_PAYPAL_TRANSACTION_SEARCH_SERVICE_TRANSACTION_ID, scmpValue);
                }
            }break;
            case SCMP_REQUEST_PURCHASING_LEVEL:{
                if(icsApplications.contains(SCMP_REQUEST_ICS_CAPTURE)){
                    nvpRequest.put(SO_REQUEST_CAPTURE_SERVICE_PURCHASING_LEVEL, scmpValue);//
                }
                else if(icsApplications.contains(SCMP_REQUEST_ICS_CREDIT)){
                    nvpRequest.put(SO_REQUEST_CREDIT_SERVICE_PURCHASING_LEVEL, scmpValue);
                }
            }break;
            case SCM_REQUEST_PAYPAL_TOKEN:{
                mapSCMPRequestPayPalToken(icsApplications, nvpRequest);
            }break;
            default:{
                //ics_applications is not part of the scmp_so_mapping.properties as this is maintained in ics_applications.properties mapping file
                if(!SCMP_REQUEST_ICS_APPLICATIONS.equals(scmpKey)) {
                    System.out.println("SCMP Key=" + scmpKey + " is unknown or not handled yet.");
                }
            }
        }
    }

    /**
     * Maps the SCMP request paypal_token field based on the paypal transaction or the ics_applications
     * @param icsApps the list of ics_applications or the PayPal transaction type
     * @param nvpRequest simple order NVP request
     */
    public static void mapSCMPRequestPayPalToken(List<String> icsApps, HashMap<String, String> nvpRequest) {
        if(icsApps != null && !icsApps.isEmpty()){
            if (icsApps.contains(ICS_PAYPAL_CREATE_AGREEMENT)) {
                nvpRequest.put(SCM_REQUEST_PAYPAL_TOKEN, SO_PAYPAL_CREATE_AGREEMENT_SERVICE_PAYPAL_TOKEN);
            } else if (icsApps.contains(ICS_PAYPAL_EC_DO_PAYMENT)) {
                nvpRequest.put(SCM_REQUEST_PAYPAL_TOKEN, SO_PAYPAL_EC_DO_PAYMENT_SERVICE_PAYPAL_TOKEN);
            } else if (icsApps.contains(ICS_PAYPAL_EC_GET_DETAILS)) {
                nvpRequest.put(SCM_REQUEST_PAYPAL_TOKEN, SO_PAYPAL_EC_GET_DETAILS_SERVICE_PAYPAL_TOKEN);
            } else if (icsApps.contains(ICS_PAYPAL_EC_ORDER_SETUP)) {
                nvpRequest.put(SCM_REQUEST_PAYPAL_TOKEN, SO_PAYPAL_EC_ORDER_SETUP_SERVICE_PAYPAL_TOKEN);
            } else if (icsApps.contains(ICS_PAYPAL_EC_SET)) {
                nvpRequest.put(SCM_REQUEST_PAYPAL_TOKEN, SO_PAYPAL_EC_SET_SERVICE_PAYPAL_TOKEN);
            }
        }
    }


    /**
     * Convert SCMP's {@code ICSOffer} object to Simple Order items
     * @param icsClientRequest the {@code ICSClientRequest} request that contains the list of {@code ICSOffer} objects
     * @param nvpRequest simple order NVP request
     */
    public static void mapSCMPOffersToSimpleOrderItem(ICSClientRequest icsClientRequest, HashMap<String, String> nvpRequest) {
        if(icsClientRequest.getAllOffers() != null){
            for(int i=0; i<icsClientRequest.getNumOffers(); ++i){
                ICSOffer offer = icsClientRequest.getOffer(i);
                if(offer.getField(SCMP_REQUEST_AMOUNT) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ITEM_UNIT_PRICE, offer.getField(SCMP_REQUEST_AMOUNT));
                }
                if(offer.getField(SCMP_REQUEST_QUANTITY) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SCMP_REQUEST_QUANTITY, offer.getField(SCMP_REQUEST_QUANTITY));
                }
                if(offer.getField(SCMP_REQUEST_PRODUCT_NAME) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ITEM_PRODUCT_NAME, offer.getField(SCMP_REQUEST_PRODUCT_NAME));
                }
                if(offer.getField(SCMP_REQUEST_ITEM_PRODUCT_DESCRIPTION) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ITEM_PRODUCT_DESCRIPTION, offer.getField(SCMP_REQUEST_ITEM_PRODUCT_DESCRIPTION));
                }
                if(offer.getField(SCMP_REQUEST_PRODUCT_CODE) != null){
                    //item_#_productCode
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ITEM_PRODUCT_CODE, offer.getField(SCMP_REQUEST_PRODUCT_CODE));
                }

                if(offer.getField(SCMP_REQUEST_MERCHANT_PRODUCT_SKU) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ITEM_PRODUCT_SKU, offer.getField(SCMP_REQUEST_MERCHANT_PRODUCT_SKU));
                }
                if(offer.getField(SCMP_REQUEST_PRODUCT_RISK) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ITEM_PRODUCT_RISK, offer.getField(SCMP_REQUEST_PRODUCT_RISK));
                }
                if(offer.getField(SCMP_REQUEST_TAX_AMOUNT) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ITEM_PRODUCT_TAX_AMOUNT, offer.getField(SCMP_REQUEST_TAX_AMOUNT));
                }
                if(offer.getField(SCMP_REQUEST_CITY_OVERRIDE_AMOUNT) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ITEM_PRODUCT_CITY_OVERRIDE_AMOUNT, offer.getField(SCMP_REQUEST_CITY_OVERRIDE_AMOUNT));
                }
                if(offer.getField(SCMP_REQUEST_CITY_OVERRIDE_RATE) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ITEM_CITY_OVERRIDE_RATE, offer.getField(SCMP_REQUEST_CITY_OVERRIDE_RATE));
                }
                if(offer.getField(SCMP_REQUEST_COUNTY_OVERRIDE_AMOUNT) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ITEM_COUNTY_OVERRIDE_AMOUNT, offer.getField(SCMP_REQUEST_COUNTY_OVERRIDE_AMOUNT));
                }
                if(offer.getField(SCMP_REQUEST_COUNTY_OVERRIDE_RATE) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ITEM_COUNTY_OVERRIDE_RATE, offer.getField(SCMP_REQUEST_COUNTY_OVERRIDE_RATE));
                }
                if(offer.getField(SCMP_REQUEST_DISTRICT_OVERRIDE_AMOUNT) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ITEM_DISTRICT_OVERRIDE_AMOUNT, offer.getField(SCMP_REQUEST_DISTRICT_OVERRIDE_AMOUNT));
                }
                if(offer.getField(SCMP_REQUEST_DISTRICT_OVERRIDE_RATE) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ITEM_DISTRICT_OVERRIDE_RATE, offer.getField(SCMP_REQUEST_DISTRICT_OVERRIDE_RATE));
                }
                if(offer.getField(SCMP_REQUEST_STATE_OVERRIDE_AMOUNT) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ITEM_STATE_OVERRIDE_AMOUNT, offer.getField(SCMP_REQUEST_STATE_OVERRIDE_AMOUNT));
                }
                if(offer.getField(SCMP_REQUEST_STATE_OVERRIDE_RATE) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ITEM_STATE_OVERRIDE_RATE, offer.getField(SCMP_REQUEST_STATE_OVERRIDE_RATE));
                }
                if(offer.getField(SCMP_REQUEST_COUNTRY_OVERRIDE_AMOUNT) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ITEM_COUNTRY_OVERRIDE_AMOUNT, offer.getField(SCMP_REQUEST_COUNTRY_OVERRIDE_AMOUNT));
                }
                if(offer.getField(SCMP_REQUEST_COUNTRY_OVERRIDE_RATE) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ITEM_COUNTRY_OVERRIDE_RATE, offer.getField(SCMP_REQUEST_COUNTRY_OVERRIDE_RATE));
                }
                //Order acceptance items
                if(offer.getField(SCMP_REQUEST_ORDER_ACCEPTANCE_CITY) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ORDER_ACCEPTANCE_CITY, offer.getField(SCMP_REQUEST_ORDER_ACCEPTANCE_CITY));
                }
                if(offer.getField(SCMP_REQUEST_ORDER_ACCEPTANCE_COUNTY) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ORDER_ACCEPTANCE_COUNTY, offer.getField(SCMP_REQUEST_ORDER_ACCEPTANCE_COUNTY));
                }
                if(offer.getField(SCMP_REQUEST_ORDER_ACCEPTANCE_COUNTRY) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ORDER_ACCEPTANCE_COUNTRY, offer.getField(SCMP_REQUEST_ORDER_ACCEPTANCE_COUNTRY));
                }
                if(offer.getField(SCMP_REQUEST_ORDER_ACCEPTANCE_STATE) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ORDER_ACCEPTANCE_STATE, offer.getField(SCMP_REQUEST_ORDER_ACCEPTANCE_STATE));
                }
                if(offer.getField(SCMP_REQUEST_ORDER_ACCEPTANCE_ZIP) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ORDER_ACCEPTANCE_POSTAL_CODE, offer.getField(SCMP_REQUEST_ORDER_ACCEPTANCE_ZIP));
                }
                if(offer.getField(SCMP_REQUEST_ORDER_ORIGIN_CITY) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ORDER_ORIGIN_CITY, offer.getField(SCMP_REQUEST_ORDER_ORIGIN_CITY));
                }
                if(offer.getField(SCMP_REQUEST_ORDER_ORIGIN_COUNTY) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ORDER_ORIGIN_COUNTY, offer.getField(SCMP_REQUEST_ORDER_ORIGIN_COUNTY));
                }
                if(offer.getField(SCMP_REQUEST_ORDER_ORIGIN_COUNTRY) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ORDER_ORIGIN_COUNTRY, offer.getField(SCMP_REQUEST_ORDER_ORIGIN_COUNTRY));
                }
                if(offer.getField(SCMP_REQUEST_ORDER_ORIGIN_STATE) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ORDER_ORIGIN_STATE, offer.getField(SCMP_REQUEST_ORDER_ORIGIN_STATE));
                }
                if(offer.getField(SCMP_REQUEST_ORDER_ORIGIN_ZIP) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ORDER_ORIGIN_POSTAL_CODE, offer.getField(SCMP_REQUEST_ORDER_ORIGIN_ZIP));
                }
                if(offer.getField(SCMP_REQUEST_SHIP_FROM_CITY) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ORDER_SHIP_FROM_CITY, offer.getField(SCMP_REQUEST_SHIP_FROM_CITY));
                }
                if(offer.getField(SCMP_REQUEST_SHIP_FROM_COUNTY) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ORDER_SHIP_FROM_COUNTY, offer.getField(SCMP_REQUEST_SHIP_FROM_COUNTY));
                }
                if(offer.getField(SCMP_REQUEST_SHIP_FROM_COUNTRY) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ORDER_SHIP_FROM_COUNTRY, offer.getField(SCMP_REQUEST_SHIP_FROM_COUNTRY));
                }
                if(offer.getField(SCMP_REQUEST_SHIP_FROM_STATE) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ORDER_SHIP_FROM_STATE, offer.getField(SCMP_REQUEST_SHIP_FROM_STATE));
                }
                if(offer.getField(SCMP_REQUEST_SHIP_FROM_ZIP) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ORDER_SHIP_FROM_POSTAL_CODE, offer.getField(SCMP_REQUEST_SHIP_FROM_ZIP));
                }
                if(offer.getField(SCMP_REQUEST_ITEM_EXPORT) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ITEM_EXPORT, offer.getField(SCMP_REQUEST_ITEM_EXPORT));
                }
                if(offer.getField(SCMP_REQUEST_ITEM_NO_EXPORT) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ITEM_NO_EXPORT, offer.getField(SCMP_REQUEST_ITEM_NO_EXPORT));
                }
                if(offer.getField(SCMP_REQUEST_ITEM_NATIONAL_TAX) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ITEM_NATIONAL_TAX, offer.getField(SCMP_REQUEST_ITEM_NATIONAL_TAX));
                }
                if(offer.getField(SCMP_REQUEST_ITEM_VAT_RATE) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ITEM_VAT_RATE, offer.getField(SCMP_REQUEST_ITEM_VAT_RATE));
                }
                if(offer.getField(SCMP_REQUEST_ITEM_BUYER_REGISTRATION) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ITEM_BUYER_REGISTRATION, offer.getField(SCMP_REQUEST_ITEM_BUYER_REGISTRATION));
                }
                if(offer.getField(SCMP_REQUEST_ITEM_MIDDLEMAN_REGISTRATION) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ITEM_MIDDLEMAN_REGISTRATION, offer.getField(SCMP_REQUEST_ITEM_MIDDLEMAN_REGISTRATION));
                }
                if(offer.getField(SCMP_REQUEST_ITEM_SCORE_CATEGORY_GIFT) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ITEM_SCORE_GIFT_CATEGORY, offer.getField(SCMP_REQUEST_ITEM_SCORE_CATEGORY_GIFT));
                }
                if(offer.getField(SCMP_REQUEST_ITEM_SCORE_CATEGORY_TIME) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ITEM_SCORE_TIME_CATEGORY, offer.getField(SCMP_REQUEST_ITEM_SCORE_CATEGORY_TIME));
                }
                if(offer.getField(SCMP_REQUEST_ITEM_SCORE_HOST_HEDGE) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ITEM_SCORE_HOST_HEDGE, offer.getField(SCMP_REQUEST_ITEM_SCORE_HOST_HEDGE));
                }
                if(offer.getField(SCMP_REQUEST_ITEM_SCORE_TIME_HEDGE) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ITEM_SCORE_TIME_HEDGE, offer.getField(SCMP_REQUEST_ITEM_SCORE_TIME_HEDGE));
                }
                if(offer.getField(SCMP_REQUEST_ITEM_SCORE_VELOCITY_HEDGE) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ITEM_SCORE_VELOCITY_HEDGE, offer.getField(SCMP_REQUEST_ITEM_SCORE_VELOCITY_HEDGE));
                }
                if(offer.getField(SCMP_REQUEST_ITEM_SCORE_NONSENSICAL_HEDGE) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ITEM_SCORE_NONSENSICAL_HEDGE, offer.getField(SCMP_REQUEST_ITEM_SCORE_NONSENSICAL_HEDGE));
                }
                if(offer.getField(SCMP_REQUEST_ITEM_SCORE_PHONE_HEDGE) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ITEM_SCORE_PHONE_HEDGE, offer.getField(SCMP_REQUEST_ITEM_SCORE_PHONE_HEDGE));
                }
                if(offer.getField(SCMP_REQUEST_ITEM_SCORE_OBSCENITIES_HEDGE) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ITEM_SCORE_OBSCENITIES_HEDGE, offer.getField(SCMP_REQUEST_ITEM_SCORE_OBSCENITIES_HEDGE));
                }
                if(offer.getField(SCMP_REQUEST_ITEM_UNIT_OF_MEASURE) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ITEM_UNIT_OF_MEASURE, offer.getField(SCMP_REQUEST_ITEM_UNIT_OF_MEASURE));
                }
                if(offer.getField(SCMP_REQUEST_ITEM_TAX_RATE) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ITEM_TAX_RATE, offer.getField(SCMP_REQUEST_ITEM_TAX_RATE));
                }
                if(offer.getField(SCMP_REQUEST_ITEM_TOTAL_AMOUNT) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ITEM_TOTAL_AMOUNT, offer.getField(SCMP_REQUEST_ITEM_TOTAL_AMOUNT));
                }
                if(offer.getField(SCMP_REQUEST_ITEM_DISCOUNT_AMOUNT) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ITEM_DISCOUNT_AMOUNT, offer.getField(SCMP_REQUEST_ITEM_DISCOUNT_AMOUNT));
                }
                if(offer.getField(SCMP_REQUEST_ITEM_DISCOUNT_RATE) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ITEM_DISCOUNT_RATE, offer.getField(SCMP_REQUEST_ITEM_DISCOUNT_RATE));
                }
                if(offer.getField(SCMP_REQUEST_ITEM_COMMODITY_CODE) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ITEM_COMMODITY_CODE, offer.getField(SCMP_REQUEST_ITEM_COMMODITY_CODE));
                }
                if(offer.getField(SCMP_REQUEST_ITEM_GROSS_NET_INDICATOR) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ITEM_GROSS_NET_INDICATOR, offer.getField(SCMP_REQUEST_ITEM_GROSS_NET_INDICATOR));
                }
                if(offer.getField(SCMP_REQUEST_ITEM_TAX_TYPE_APPLIED) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ITEM_TAX_TYPE_APPLIED, offer.getField(SCMP_REQUEST_ITEM_TAX_TYPE_APPLIED));
                }
                if(offer.getField(SCMP_REQUEST_ITEM_DISCOUNT_INDICATOR) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ITEM_DISCOUNT_INDICATOR, offer.getField(SCMP_REQUEST_ITEM_DISCOUNT_INDICATOR));
                }
                if(offer.getField(SCMP_REQUEST_ITEM_ALTERNATE_TAX_ID) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ITEM_ALTERNATE_TAX_ID, offer.getField(SCMP_REQUEST_ITEM_ALTERNATE_TAX_ID));
                }
                if(offer.getField(SCMP_REQUEST_ITEM_ALTERNATE_TAX_AMOUNT) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ITEM_ALTERNATE_TAX_AMOUNT, offer.getField(SCMP_REQUEST_ITEM_ALTERNATE_TAX_AMOUNT));
                }
                if(offer.getField(SCMP_REQUEST_ITEM_ALTERNATE_TAX_TYPE_APPLIED) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ITEM_ALTERNATE_TAX_TYPE_APPLIED, offer.getField(SCMP_REQUEST_ITEM_ALTERNATE_TAX_TYPE_APPLIED));
                }
                if(offer.getField(SCMP_REQUEST_ITEM_ALTERNATE_TAX_RATE) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ITEM_ALTERNATE_TAX_RATE, offer.getField(SCMP_REQUEST_ITEM_ALTERNATE_TAX_RATE));
                }
                if(offer.getField(SCMP_REQUEST_ITEM_ALTERNATE_TAX_TYPE_IDENTIFIER) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ITEM_ALTERNATE_TAX_TYPE_IDENTIFIER, offer.getField(SCMP_REQUEST_ITEM_ALTERNATE_TAX_TYPE_IDENTIFIER));
                }
                if(offer.getField(SCMP_REQUEST_ITEM_LOCAL_TAX) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ITEM_LOCAL_TAX, offer.getField(SCMP_REQUEST_ITEM_LOCAL_TAX));
                }
                if(offer.getField(SCMP_REQUEST_ITEM_ZERO_COST_TO_CUSTOMER_INDICATOR) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ITEM_ZERO_COST_TO_CUSTOMER_INDICATOR, offer.getField(SCMP_REQUEST_ITEM_ZERO_COST_TO_CUSTOMER_INDICATOR));
                }
                if(offer.getField(SCMP_REQUEST_ITEM_PASSENGER_FIRST_NAME) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ITEM_PASSENGER_FIRST_NAME, offer.getField(SCMP_REQUEST_ITEM_PASSENGER_FIRST_NAME));
                }
                if(offer.getField(SCMP_REQUEST_ITEM_PASSENGER_LAST_NAME) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ITEM_PASSENGER_LAST_NAME, offer.getField(SCMP_REQUEST_ITEM_PASSENGER_LAST_NAME));
                }
                if(offer.getField(SCMP_REQUEST_ITEM_PASSENGER_ID) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ITEM_PASSENGER_ID, offer.getField(SCMP_REQUEST_ITEM_PASSENGER_ID));
                }
                if(offer.getField(SCMP_REQUEST_ITEM_PASSENGER_STATUS) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ITEM_PASSENGER_STATUS, offer.getField(SCMP_REQUEST_ITEM_PASSENGER_STATUS));
                }
                if(offer.getField(SCMP_REQUEST_ITEM_PASSENGER_TYPE) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ITEM_PASSENGER_TYPE, offer.getField(SCMP_REQUEST_ITEM_PASSENGER_TYPE));
                }
                if(offer.getField(SCMP_REQUEST_ITEM_PASSENGER_EMAIL) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ITEM_PASSENGER_EMAIL, offer.getField(SCMP_REQUEST_ITEM_PASSENGER_EMAIL));
                }
                if(offer.getField(SCMP_REQUEST_ITEM_PASSENGER_PHONE) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ITEM_PASSENGER_PHONE, offer.getField(SCMP_REQUEST_ITEM_PASSENGER_PHONE));
                }
                if(offer.getField(SCMP_REQUEST_ITEM_INVOICE_NUMBER) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ITEM_INVOICE_NUMBER, offer.getField(SCMP_REQUEST_ITEM_INVOICE_NUMBER));
                }
                if(offer.getField(SCMP_REQUEST_ITEM_SELLER_REGISTRATION) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ITEM_SELLER_REGISTRATION, offer.getField(SCMP_REQUEST_ITEM_SELLER_REGISTRATION));
                }
                if(offer.getField(SCMP_REQUEST_ITEM_POINT_OF_TITLE_TRANSFER) != null){
                    nvpRequest.put(SO_REQUEST_ITEM_ITEM + "_" + i + "_" + SO_REQUEST_ITEM_POINT_OF_TITLE_TRANSFER, offer.getField(SCMP_REQUEST_ITEM_POINT_OF_TITLE_TRANSFER));
                }
            }
        }
    }

    /**
     * Converts a simple order response to SCMP's ICSReply object
     * @param nvpResponse the simple order response
     * @return SCMP's {@code ICSReply} object
     */
    public static ICSReply convertNVPResponseToICSReply(Map<String, String> nvpResponse, ICSClientRequest icsClientRequest){
        if(nvpResponse == null || nvpResponse.isEmpty()){
            System.out.println("NVP Response is null or empty");
            return null;
        }
        ICSReply icsReply = new ICSReply();
        for(String key:nvpResponse.keySet()){
            String value = nvpResponse.get(key);
            if(value != null && !value.isEmpty()){
                //get the key from our response mapping table
                String scmpKey = responseConversionTable.getProperty(key);
                if(scmpKey == null || scmpKey.isEmpty()){
                    mapNonDistinctSOResponseFields(icsReply, key, value,icsClientRequest);
                }
                else {
                    icsReply.setField(scmpKey, value);
                }
            }
        }

        return icsReply;
    }

    /**
     * Maps Simple order response fields that have multiple mapping to its SCMP equivalent
     * @param icsReply ICSReply the SCMP reply
     * @param nvpKey simple order response field key
     * @param nvpValue simple order response field value
     * @param icsClientRequest ICS client request
     */
    public static void mapNonDistinctSOResponseFields(ICSReply icsReply, String nvpKey, String nvpValue, ICSClientRequest icsClientRequest) {
        String icsApplications = icsClientRequest.getField(SCMP_REQUEST_ICS_APPLICATIONS);
        List<String> icsApplicationsList = new ArrayList<String>();
        if (icsApplications != null) {
            if (icsApplications.contains(",")) {
                for (String icsApp : icsApplications.split(",")) {
                    String icsApplication = ICS_APPLICATIONS_LOOKUP_TABLE.get(icsApp.trim());
                    if (icsApplication != null && !icsApplication.isEmpty()) {
                        icsApplicationsList.add(icsApplication);
                    } else {
                        System.out.println("ics_applications: " + icsApp + " is not mapped. Check the ics_applications.properties file");
                    }
                }
            } else {
                String icsApplication = ICS_APPLICATIONS_LOOKUP_TABLE.get(icsApplications.trim());
                if (icsApplication != null && !icsApplication.isEmpty()) {
                    icsApplicationsList.add(icsApplication);
                } else {
                    System.out.println("ICS applications: " + icsApplications + " not found in ics_applications.properties mapping file");
                }
            }
        }

        switch (nvpKey) {
            case SO_RESPONSE_PAYMENT_TYPE_INDICATOR: {
                if (icsApplicationsList.contains(SCMP_REQUEST_ICS_AUTH)) {
                    icsReply.setField(SO_RESPONSE_AUTH_PAYMENT_TYPE_INDICATOR, nvpValue);
                } else {
                    icsReply.setField(SO_RESPONSE_AUTH_REVERSAL_PAYMENT_TYPE_INDICATOR, nvpValue);
                }
            }
            break;
            case SO_RESPONSE_CC_CREDIT_REPLY_RECONCILIATION_REFERENCE_NUMBER: {
                if (icsApplicationsList.contains(SCMP_REQUEST_ICS_CREDIT)) {
                    icsReply.setField(SO_RESPONSE_CREDIT_AUTH_RECONCILIATION_REFERENCE_NUMBER, nvpValue);
                } else {
                    icsReply.setField(SO_RESPONSE_CREDIT_RECONCILIATION_REFERENCE_NUMBER, nvpValue);
                }
            }
            break;
            case SO_RESPONSE_CC_AUTH_REPLY_MERCHANT_ADVICE_CODE: {
                if (icsApplicationsList.contains(SCMP_REQUEST_ICS_AUTH)) {
                    icsReply.setField(SO_RESPONSE_AUTH_MERCHANT_ADVICE_CODE, nvpValue);
                } else {
                    icsReply.setField(SO_RESPONSE_MERCHANT_ADVICE_CODE, nvpValue);
                }
            }
            break;
            case SO_RESPONSE_CC_AUTH_REPLY_MERCHANT_ADVICE_CODE_RAW: {
                if (icsApplicationsList.contains(SCMP_REQUEST_ICS_AUTH)) {
                    icsReply.setField(SO_RESPONSE_AUTH_MERCHANT_ADVICE_CODE_RAW, nvpValue);
                } else {
                    icsReply.setField(SO_RESPONSE_MERCHANT_ADVICE_CODE_RAW, nvpValue);
                }
            }
            break;
            default: {
                System.out.println("Simple Order key: " + nvpKey + " is unknown");
            }

        }
    }

    /**
     * Display the content of a map object into the console
     * @param header the header
     * @param map the map to display
     */
    public static void displayMap(String header, Map map) {
        StringBuilder dest = new StringBuilder(128);
        System.out.println(header);
        if (map != null && !map.isEmpty()) {
            Iterator iter = map.keySet().iterator();
            String key, val;
            while (iter.hasNext()) {
                key = (String) iter.next();
                val = (String) map.get(key);
                if(FIELDS_TO_MASK.contains(key)){
                    dest.append(key + "=" + maskContent(val) + "\n");
                }
                else {
                    dest.append(key + "=" + val + "\n");
                }
            }
        }
        System.out.println(dest.toString());
    }

    /**
     * Load the given property file to a Properties object
     * @param file the property file to load
     * @return the {@code Properties} object with the file content
     */
    public static Properties readPropertyFile(String file) {
        if(file == null){
            System.out.println("File to read is null");
            return null;
        }
        Properties cybsProps = new Properties();
        try(FileInputStream fis =new FileInputStream(file)) {
            cybsProps.load(fis);
        } catch (IOException ioe) {
            System.out.println("Error reading properties file " + file + ": " + ioe.getMessage());
            return null;
        }
        return cybsProps;
    }

    /**
     * Loads the given property file and convert the content to an ICSClientRequest object
     * @param properties the property object to load and convert
     * @return an {@code ICSClientRequest}  object with the contents of the property file
     */
    public static ICSClientRequest convertPropertyFileToICSClientRequest(Properties properties) throws ICSException {
        if(properties == null){
            System.out.println("Cannot convert a null property file to ICSClientRequest");
            return null;
        }
        ICSClientRequest icsClientRequest = new ICSClientRequest();
        Map<String, ICSOffer> offerTable = new HashMap<>();
        for(String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            if(value == null || value.isEmpty()) {
                System.out.println("Property key=" + key + " is null or empty");
            }
            else{
                if(isSCMPItemField(key)){
                    String offerKey = key.substring(key.lastIndexOf("_"));
                    ICSOffer offer = offerTable.get(offerKey);
                    String scmpOfferKey = key.substring(0,key.lastIndexOf("_"));
                    if(offer == null){
                        offer = new ICSOffer();
                        offer.setField(scmpOfferKey,value);
                        offerTable.put(offerKey, offer);
                    }
                    else{
                        offer.setField(scmpOfferKey,value);
                    }
                }
                else {
                    if(key.startsWith("offer")){
                        //the ics.jar setField logic will treat field names that starts with "offer" as an item and as such will create a new ICCOffer object.
                        //Need to access the hashtable directly here instead of using the setField method.
                        icsClientRequest.getHashtable().put(key, value);
                    }
                    else {
                        icsClientRequest.setField(key, value);
                    }
                }
            }
        }
        //add all offer fields if present
        if(!offerTable.isEmpty()){
            for(String key:offerTable.keySet()) {
                ICSOffer offerValue = offerTable.get(key);
                if(offerValue != null){
                    icsClientRequest.addOffer(offerValue);
                }
            }
        }
        return icsClientRequest;
    }

    /**
     * Test whether the given field is one of the SCMP's item/offer field
     * @param field the field to test
     * @return return {@code true} if the field is an SCMP's item field
     */
    public static boolean isSCMPItemField(String field) {
        if(field != null && !field.isEmpty()){
            return field.startsWith("amount_") || field.startsWith("quantity_")
                    || field.startsWith("price_") || field.startsWith("product_code_")
                    || field.startsWith("merchant_product_sku_") || field.startsWith("product_risk_")
                    || field.startsWith("tax_amount_") || field.startsWith("city_override_amount_")
                    || field.startsWith("city_override_rate_") || field.startsWith("county_override_amount_")
                    || field.startsWith("county_override_rate_") || field.startsWith("district_override_amount_")
                    || field.startsWith("district_override_rate_") || field.startsWith("state_override_amount_")
                    || field.startsWith("state_override_rate_") || field.startsWith("country_override_amount_")
                    || field.startsWith("country_override_rate_") || field.startsWith("product_name_");
        }
        else{
            return false;
        }
    }

    /**
     * Mask the given text
     * @param text the text to mask
     * @return the masked equivalent of the given text
     */
    public static String maskContent(String text){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < text.length(); i++){
            sb.append("*");
        }

        return sb.toString();
    }

    /**
     * Returns a loggable version of the ICS client request which masks the sensitive data
     * @param icsClientRequest the ICS client request
     * @return loggable text of the given ICS client request object
     */
    public static String getLoggableICSClientRequest(ICSClientRequest icsClientRequest) {
        if(icsClientRequest == null){
            return "";
        }
        StringBuilder sb = new StringBuilder(1500);
        Enumeration e = icsClientRequest.getHashtable().keys();
        while (e.hasMoreElements()) {
            Object next_key = e.nextElement();
            if (next_key instanceof String) {
                String name = (String) next_key;
                String value = (String) icsClientRequest.getHashtable().get(name);
                sb.append(name);
                if (value != null) {
                    sb.append("=");
                    if (FIELDS_TO_MASK.contains(name)) {
                        sb.append(maskContent(value));
                    } else {
                        sb.append(value);
                    }
                }
                if (e.hasMoreElements()) {
                    sb.append("\n");
                }
            }
        }

        for(int i = 0; i < icsClientRequest.getAllOffers().size(); ++i) {
            sb.append(RECORD_SEPARATOR + "offer" + i + VALUE_SEPARATOR + icsClientRequest.getAllOffers().elementAt(i));
        }

        return sb.toString();
    }
}
