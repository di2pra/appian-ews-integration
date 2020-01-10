package com.appiancorp.ps.ewsintegration;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.log4j.Logger;

import com.appiancorp.suiteapi.content.ContentConstants;
import com.appiancorp.suiteapi.content.ContentService;
import com.appiancorp.suiteapi.knowledge.Document;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.WebProxy;
import microsoft.exchange.webservices.data.core.enumeration.property.BodyType;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.credential.WebProxyCredentials;
import microsoft.exchange.webservices.data.property.complex.EmailAddress;
import microsoft.exchange.webservices.data.property.complex.MessageBody;

public class EWSUtils {
  private static final Logger LOG = Logger.getLogger(EWSUtils.class);

  private String serviceUrl;
  private String domain;
  private String username;
  private String password;

  private String proxyURL;
  private Integer proxyPort;
  private String proxyDomain;
  private String proxyUsername;
  private String proxyPassword;
  private boolean isConnectedViaProxy = false;

  public EWSUtils(String serviceUrl, String domain, String username, String password,
    String proxyURL, Integer proxyPort, String proxyDomain, String proxyUsername, String proxyPassword,
    boolean isConnectedViaProxy) {

    this.serviceUrl = serviceUrl;
    this.domain = domain;
    this.username = username;
    this.password = password;
    this.proxyURL = proxyURL;
    this.proxyPort = proxyPort;
    this.proxyDomain = proxyDomain;
    this.proxyUsername = proxyUsername;
    this.proxyPassword = proxyPassword;
    this.isConnectedViaProxy = isConnectedViaProxy;
  }

  public ExchangeService authService(ExchangeService service) throws URISyntaxException {

    if (LOG.isDebugEnabled()) {
      LOG.debug("Starting to poll for new email from " + username + " @ " + serviceUrl);
      LOG.debug("Passed Parameters: ");
      LOG.debug("serviceUrl: " + serviceUrl);
      LOG.debug("domain: " + domain);
      LOG.debug("username: " + username);
      LOG.debug("proxyURL: " + proxyURL);
      LOG.debug("proxyPort: " + proxyPort);
      LOG.debug("proxyDomain: " + proxyDomain);
      LOG.debug("proxyUsername: " + proxyUsername);
    }

    WebCredentials credentials;

    if (domain != null && !"".equalsIgnoreCase(domain)) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Setting Username, password, domain");
      }
      credentials = new WebCredentials(username, password, domain);
    } else {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Setting Username, password");
      }
      credentials = new WebCredentials(username, password);
    }

    if (isConnectedViaProxy) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Setting Proxy");
      }
      WebProxy webProxy = new WebProxy(proxyURL, proxyPort,
        new WebProxyCredentials(proxyUsername, proxyPassword, proxyDomain));
      service.setWebProxy(webProxy);
    }

    service.setCredentials(credentials);
    service.setUrl(new URI(serviceUrl));

    if (LOG.isDebugEnabled()) {
      LOG.debug("Service should be ready: " + service);
    }

    return service;
  }

  public static void sendEmail(ContentService cs, ExchangeService service, String senderDisplayName, String senderEmail,
    String[] recipients, String[] ccRecipients,
    String[] bccRecipients, String subject, boolean bodyTypeHTML,
    String body, Long[] attachments)
    throws Exception {

    EmailMessage message = new EmailMessage(service);

    // set subject
    message.setSubject(subject);

    // set body
    message.setBody(new MessageBody(bodyTypeHTML ? BodyType.HTML : BodyType.Text, body));

    // set sender (to send on behalf of)
    if (senderEmail != null && !senderEmail.isEmpty()) {

      if (senderDisplayName != null && !senderDisplayName.isEmpty()) {
        message.setFrom(new EmailAddress(senderDisplayName, senderEmail));
      } else {
        message.setFrom(new EmailAddress(senderEmail));
      }

    }

    if (recipients != null) {
      for (String recipient : recipients) {
        message.getToRecipients().add(recipient);
      }
    }

    if (ccRecipients != null) {
      for (String recipient : ccRecipients) {
        message.getCcRecipients().add(recipient);
      }
    }

    if (bccRecipients != null) {
      for (String recipient : bccRecipients) {
        message.getBccRecipients().add(recipient);
      }
    }

    if (attachments != null) {
      for (Long attachment : attachments) {

        Document doc = cs.download(attachment, ContentConstants.VERSION_CURRENT, false)[0];

        message.getAttachments().addFileAttachment(doc.getInternalFilename());

      }
    }

    message.sendAndSaveCopy();

    if (LOG.isDebugEnabled()) {
      LOG.debug("Email Sent Succesfully.");
    }

  }

}
