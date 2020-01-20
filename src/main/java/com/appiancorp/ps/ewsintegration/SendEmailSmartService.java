package com.appiancorp.ps.ewsintegration;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import javax.naming.Context;

import org.apache.log4j.Logger;

import com.appiancorp.suiteapi.common.Name;
import com.appiancorp.suiteapi.content.ContentService;
import com.appiancorp.suiteapi.knowledge.DocumentDataType;
import com.appiancorp.suiteapi.personalization.EmailAddressDataType;
import com.appiancorp.suiteapi.process.exceptions.SmartServiceException;
import com.appiancorp.suiteapi.process.framework.AppianSmartService;
import com.appiancorp.suiteapi.process.framework.Input;
import com.appiancorp.suiteapi.process.framework.Order;
import com.appiancorp.suiteapi.process.framework.Required;
import com.appiancorp.suiteapi.process.palette.ConnectivityServices;
import com.appiancorp.suiteapi.security.external.SecureCredentialsStore;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;

@ConnectivityServices
@Order({
  "ServiceUrl", "Domain", "ScsExternalSystemKey", "ConnectedViaProxy", "ProxyURL", "ProxyPort", "ProxyDomain", "SenderDisplayName",
  "SenderEmail",
  "Recipients", "CCRecipients", "BCCRecipients", "Subject", "BodyTypeHtml", "Body", "Attachements", "ErrorOccurred", "ErrorMessage" })
public class SendEmailSmartService extends AppianSmartService {

  private static final Logger LOG = Logger.getLogger(SendEmailSmartService.class);
  private static final String CRED_USERNAME = "username";
  private static final String CRED_PASSWORD = "password";
  private static final String CRED_PROXY_USERNAME = "proxyUsername";
  private static final String CRED_PROXY_PASSWORD = "proxyPassword";

  private final SecureCredentialsStore scs;
  private final ContentService cs;

  // inputs
  private String serviceUrl;
  private String domain;
  private String scsExternalSystemKey;
  private boolean isConnectedViaProxy = false;
  private String proxyURL;
  private Integer proxyPort;
  private String proxyDomain;
  private String senderDisplayName;
  private String senderEmail;
  private String subject;
  private boolean bodyTypeHTML = false;
  private Long body;
  private String[] recipients;
  private String[] ccRecipients;
  private String[] bccRecipients;
  private Long[] attachments;

  // local variables
  private String username;
  private String password;
  private String proxyUsername;
  private String proxyPassword;
  private ExchangeService service;

  // outputs
  private boolean errorOccurred;
  private String errorMessage;

  public SendEmailSmartService(SecureCredentialsStore scs, ContentService cs, Context ctx) {
    super();

    this.scs = scs;
    this.cs = cs;
  }

  @Override
  public void run() throws SmartServiceException {
    Map<String, String> credentials;

    try {
      credentials = scs.getSystemSecuredValues(scsExternalSystemKey);
    } catch (Exception ex) {
      LOG.error("Error retrieving credentials", ex);
      errorOccurred = true;
      errorMessage = ex.getMessage();
      return;
    }

    if (!credentials.containsKey(CRED_USERNAME)) {
      errorOccurred = true;
      errorMessage = String.format("Required field %s does not exist in SCS (%s)", CRED_USERNAME,
        scsExternalSystemKey);
      return;
    } else if (!credentials.containsKey(CRED_PASSWORD)) {
      errorOccurred = true;
      errorMessage = String.format("Required field %s does not exist in SCS (%s)", CRED_PASSWORD,
        scsExternalSystemKey);
      return;
    }

    username = credentials.get(CRED_USERNAME);
    password = credentials.get(CRED_PASSWORD);

    if (isConnectedViaProxy) {
      if (!credentials.containsKey(CRED_PROXY_USERNAME)) {
        errorOccurred = true;
        errorMessage = String.format("Required field %s does not exist in SCS (%s)", CRED_PROXY_USERNAME,
          scsExternalSystemKey);
        return;
      } else if (!credentials.containsKey(CRED_PROXY_PASSWORD)) {
        errorOccurred = true;
        errorMessage = String.format("Required field %s does not exist in SCS (%s)", CRED_PROXY_PASSWORD,
          scsExternalSystemKey);
        return;
      } else if (proxyURL == null || proxyURL.isEmpty()) {
        errorOccurred = true;
        errorMessage = "Required field proxyUrl is empty";
        return;
      } else if (proxyPort == null) {
        errorOccurred = true;
        errorMessage = "Required field proxyPort is empty";
        return;
      }

      proxyUsername = credentials.get(CRED_PROXY_USERNAME);
      proxyPassword = credentials.get(CRED_PROXY_PASSWORD);
    }

    try {

      EWSUtils EWSutils = new EWSUtils(serviceUrl, domain, username, password,
        proxyURL, proxyPort, proxyDomain, proxyUsername, proxyPassword, isConnectedViaProxy);

      service = EWSutils.authService(new ExchangeService(ExchangeVersion.Exchange2010_SP2));

    } catch (Exception ex) {
      LOG.error("Error connecting to EWS", ex);
      errorOccurred = true;
      errorMessage = ex.getMessage();
      return;
    }

    try {

      String bodyFilePath = cs.getInternalFilename(body);

      String bodyString = new String(Files.readAllBytes(Paths.get(bodyFilePath)));

      EWSUtils.sendEmail(cs, service, senderDisplayName, senderEmail, recipients, ccRecipients, bccRecipients, subject, bodyTypeHTML,
        bodyString, attachments);

    } catch (Exception ex) {
      LOG.error("Error sending email through EWS", ex);
      errorOccurred = true;
      errorMessage = ex.getMessage();
      return;
    }

  }

  @Input(required = Required.ALWAYS)
  @Name("ServiceUrl")
  public void setServiceUrl(String val) {
    this.serviceUrl = val;
  }

  @Input(required = Required.OPTIONAL)
  @Name("Domain")
  public void setDomain(String val) {
    this.domain = val;
  }

  @Input(required = Required.ALWAYS)
  @Name("ConnectedViaProxy")
  public void setConnectedViaProxy(boolean val) {
    this.isConnectedViaProxy = val;
  }

  @Input(required = Required.OPTIONAL)
  @Name("ProxyURL")
  public void setProxyURL(String val) {
    this.proxyURL = val;
  }

  @Input(required = Required.OPTIONAL)
  @Name("ProxyPort")
  public void setProxyPort(Integer val) {
    this.proxyPort = val;
  }

  @Input(required = Required.OPTIONAL)
  @Name("ProxyDomain")
  public void setProxyDomain(String proxyDomain) {
    this.proxyDomain = proxyDomain;
  }

  @Input(required = Required.ALWAYS)
  @Name("ScsExternalSystemKey")
  public void setScsExternalSystemKey(String val) {
    this.scsExternalSystemKey = val;
  }

  @Input(required = Required.OPTIONAL)
  @Name("SenderDisplayName")
  public void setSenderDisplayName(String val) {
    this.senderDisplayName = val;
  }

  @Input(required = Required.OPTIONAL)
  @Name("SenderEmail")
  public void setSenderEmail(String val) {
    this.senderEmail = val;
  }

  @Input(required = Required.ALWAYS)
  @Name("Recipients")
  @EmailAddressDataType
  public void setRecipients(String[] val) {
    this.recipients = val;
  }

  @Input(required = Required.OPTIONAL)
  @Name("CCRecipients")
  @EmailAddressDataType
  public void setCCRecipients(String[] val) {
    this.ccRecipients = val;
  }

  @Input(required = Required.OPTIONAL)
  @Name("BCCRecipients")
  @EmailAddressDataType
  public void setBccRecipients(String[] val) {
    this.bccRecipients = val;
  }

  @Input(required = Required.OPTIONAL)
  @Name("Subject")
  public void setSubject(String val) {
    this.subject = val;
  }

  @Input(required = Required.ALWAYS)
  @Name("BodyTypeHtml")
  public void setBodyTypeHtml(boolean val) {
    this.bodyTypeHTML = val;
  }

  @Input(required = Required.ALWAYS)
  @Name("Body")
  @DocumentDataType
  public void setBody(Long val) {
    this.body = val;
  }

  @Input(required = Required.OPTIONAL)
  @Name("Attachments")
  @DocumentDataType
  public void setAttachments(Long[] val) {
    this.attachments = val;
  }

  @Name("ErrorOccurred")
  public boolean getErrorOccurred() {
    return errorOccurred;
  }

  @Name("ErrorMessage")
  public String getErrorMessage() {
    return errorMessage;
  }

}
