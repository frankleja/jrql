package com.hlcl.rql.as;

/**
 * @author barszczewski on 18.11.13
 */
public class MailSendingException extends RuntimeException {

    public MailSendingException() { super(); }
    public MailSendingException(String message) { super(message); }
    public MailSendingException(String message, Throwable cause) { super(message, cause); }
    public MailSendingException(Throwable cause) { super(cause); }
}
