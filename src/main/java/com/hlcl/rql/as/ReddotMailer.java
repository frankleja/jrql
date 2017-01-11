package com.hlcl.rql.as;

import java.util.ArrayList;
import java.util.List;

/**
 * Ein Mailer der E-Mails via RQL verschickt, also über das CMS.
 */
public class ReddotMailer implements Mailer {

    private String subject = "";
    private String body = "";
    private List<String> recipients = new ArrayList<String>();
    private String from = "";

    CmsClient cmsClient;

    private StringBuilder rqlBaseRequest = new StringBuilder(256);
    private static final String rqlTail = "></ADMINISTRATION></IODATA>";

    private static final String RECIPIENT_DELIMITER = ",";

    private ReddotMailer (CmsClient client) {

        this.cmsClient = client;

        this.rqlBaseRequest
            .append("<IODATA loginguid=\"").append(this.cmsClient.getLogonGuid())
            .append("\" sessionkey=\"").append(this.cmsClient.getLogonGuid()).append("\">")
            .append("<ADMINISTRATION")
            .append(" action=\"sendmail\"");
    }


    /**
     * Erzeugt einen Mailer für die CMS-Schnittstelle
     *
     * @param cmsClient Schnittstelle zum CMS
     * @return
     */
    public static ReddotMailer forCmsClient(CmsClient cmsClient){

        return new ReddotMailer(cmsClient);
    }


    /**
     * {@inheritDoc}
     */
    public ReddotMailer withSender(String sender){
        this.from = sender;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReddotMailer withSubject(String subject) {

        this.subject = subject;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReddotMailer withBody(String body) {

        this.body = body;
        return this;
    }

    @Override
    public ReddotMailer addRecipient(String recipient) {

        this.recipients.add(recipient);
        return this;
    }


    /**
     * Fügt alle Mitglieder einer Benutzgruppe als Empfänger ein
     *
     * @param userGroup CMS Benutzergruppe
     * @return
     */
    public ReddotMailer addRecipientGroup(UserGroup userGroup){

        try {
            for (User user : userGroup.getUsers()) {
                addRecipient(user.getEmailAddress());
            }
        } catch (RQLException e) {}

        return this;
    }

    /**
     * Erzeugt das RQL, um eine E-Mail über das CMS zu versenden
     *
     * @param mailerType Typ der E-Mail
     * @return
     */
    private String getRql(Mailer.Type mailerType) {

        rqlBaseRequest
            .append(" to=\"").append(getRecipientsAsString()).append("\"")
            .append(" from=\"").append(this.from).append("\"")
            .append(" subject=\"").append(this.subject).append("\"")
            .append(" message=\"").append(this.body).append("\"")
            .append(" plaintext=\"").append(mailerType.equals(Type.PLAIN) ? "1" : "0").append("\"")
            .append(rqlTail);

        return rqlBaseRequest.toString();
    }

    /**
     * Überprüft, ob die E-Mail verschickt werden soll.
     *
     * @return
     */
    private boolean mailerIsValid(){

        return this.recipients.size() > 0;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void sendPlainText() {

        if(mailerIsValid()){

            String rql = getRql(Type.PLAIN);

            System.out.println(rql);

            try {
                this.cmsClient.callCms(rql);

            } catch (RQLException e) {

                throw new MailSendingException("Error during rql execution", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendHtml(){

        if(mailerIsValid()){

            String rql = getRql(Type.HTML);
            try {
                this.cmsClient.callCms(rql);

            } catch (RQLException e) {

                throw new MailSendingException("Error during rql execution", e);
            }
        }
    }

    /**
     * Erzeugt aus der Liste der Empfänger einen RQL-kompatiblen String
     *
     * @return
     */
    private String getRecipientsAsString() {

        String recipients = "";

        if(this.recipients.size() > 0){
            recipients = StringHelper.toString(this.recipients, RECIPIENT_DELIMITER);
        }

        return recipients;
    }
}
