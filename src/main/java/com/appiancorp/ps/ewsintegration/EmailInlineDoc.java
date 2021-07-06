package com.appiancorp.ps.ewsintegration;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

@XmlRootElement(namespace = EmailInlineDoc.NAMESPACE, name = EmailInlineDoc.LOCAL_PART)

@XmlType(name = EmailInlineDoc.LOCAL_PART, namespace = EmailInlineDoc.NAMESPACE, propOrder = {
  EmailInlineDoc.FIELDNAME_CONTENT_ID, EmailInlineDoc.FIELDNAME_DOC_ID })
public class EmailInlineDoc {

  public static final String NAMESPACE = "urn:com:appian:ps:ewsintegration";
  public static final String LOCAL_PART = "EmailInlineDoc";

  public static final QName QNAME = new QName(NAMESPACE, LOCAL_PART);
  public static final String FIELDNAME_CONTENT_ID = "contentId";
  public static final String FIELDNAME_DOC_ID = "docId";

  private String contentId;

  private Long docId;

  public EmailInlineDoc() {
    super();
  }

  @XmlElement
  public String getContentId() {
    return contentId;
  }

  public void setContentId(String contentId) {
    this.contentId = contentId;
  }

  @XmlElement
  public Long getDocId() {
    return docId;
  }

  public void setDocId(Long docId) {
    this.docId = docId;
  }

  @Override
  public String toString() {
    return "EmailInlineDocBean [docId=" + docId + ", contentId=" + contentId + "]";
  }

}