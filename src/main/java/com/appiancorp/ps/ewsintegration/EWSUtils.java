package com.appiancorp.ps.ewsintegration;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.log4j.Logger;

import com.appiancorp.suiteapi.content.ContentService;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.WebProxy;
import microsoft.exchange.webservices.data.core.enumeration.property.BodyType;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.credential.WebProxyCredentials;
import microsoft.exchange.webservices.data.property.complex.EmailAddress;
import microsoft.exchange.webservices.data.property.complex.FileAttachment;
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
    String body, Long[] attachments, EmailInlineDoc[] inlineDocs)
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
        if (!"".equalsIgnoreCase(recipient)) {
          message.getToRecipients().add(recipient);
        }
      }
    }

    if (ccRecipients != null) {
      for (String recipient : ccRecipients) {
        if (!"".equalsIgnoreCase(recipient)) {
          message.getCcRecipients().add(recipient);
        }
      }
    }

    if (bccRecipients != null) {
      for (String recipient : bccRecipients) {
        if (!"".equalsIgnoreCase(recipient)) {
          message.getBccRecipients().add(recipient);
        }
      }
    }

    // process attachments
    if (attachments != null) {
      for (Long attachment : attachments) {

        String filePath = cs.getInternalFilename(attachment);

        String fileName = cs.getExternalFilename(attachment);

        message.getAttachments().addFileAttachment(fileName, filePath);

      }
    }

    // process inline Docs
    if (inlineDocs != null) {
      for (EmailInlineDoc inlineDoc : inlineDocs) {

        String filePath = cs.getInternalFilename(inlineDoc.getDocId());

        String fileName = cs.getExternalFilename(inlineDoc.getDocId());

        FileAttachment fileAttachment = message.getAttachments().addFileAttachment(fileName, filePath);
        fileAttachment.setContentId(inlineDoc.getContentId());
        fileAttachment.setIsInline(true);

      }
    }

    message.sendAndSaveCopy();

    if (LOG.isDebugEnabled()) {
      LOG.debug("Email Sent Succesfully.");
    }

  }

}
