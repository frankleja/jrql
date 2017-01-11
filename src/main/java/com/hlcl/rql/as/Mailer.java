package com.hlcl.rql.as;

/**
 * Schnittstelle eines Mailers
 */
public interface Mailer {

    /**
     * Typ einer E-Mail. Entweder Text oder HTML
     */
    public enum Type {PLAIN, HTML}

    /**
     * Sendet eine Text-E-Mail
     */
    public void sendPlainText();

    /**
     * Sendet eine HTML-E-Mail
     */
    public void sendHtml();

    /**
     * Setzt den Absender in den Mailer
     *
     * @param sender Absender E-Mail-Adresse
     * @return Mailer
     */
    public Mailer withSender(String sender);

    /**
     * Setzt die Betreffzeile für die E-Mail
     *
     * @param subject Betreff
     * @return Mailer
     */
    public Mailer withSubject(String subject);

    /**
     *
     * Setzt den Inhalt einer E-Mail
     *
     * @param body Inhalt der E-Mail
     * @return Mailer
     */
    public Mailer withBody(String body);


    /**
     * Fügt einen E-Mail-Empfänger hinzu
     *
     * @param recipient Empfänger der E-Mail
     * @return Mailer
     */
    public Mailer addRecipient(String recipient);
}
