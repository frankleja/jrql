package com.hlcl.rql.as;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.TimeZone;

/**
 * Diese Klasse beschreibt ein RedDot Datum. RedDot nutzt eine Datumsdarstellung von Microsoft im float Format.
 * 
 * Die Wandlungsroutinen wurden von RedDot, Hr. Apetz geliefert.
 * 
 * @author LEJAFR
 */
public class ReddotDate extends java.util.Date {

	// Wandelt man den 1970-01-01 00:00:00 in ein Microsoft Float Datum erhält
	// man 25569
	private final static double cFirstMSDayInDouble = 25569d;

	// nach Java ist 1970-01-01 00:00:00 die nullte Millisekunde
	// Microsoft hat hier aber schon die 2209161600000 Millisekunde
	private final static double cFirstMSDayInMilliSec = 2209161600000d;

	// eine natürliche Konstante, die Millisekunden eines Tages
	private final static double cMSecondsPerDay = 86400000d;

	// default date format, sortierfähig
	public static final String defaultPattern = "yyyyMMdd";
	private static final SimpleDateFormat defaultPatternFormatter = new SimpleDateFormat(defaultPattern);

	// default view date format
	public static final String defaultViewPattern = "dd MMM yyyy";
	private static final SimpleDateFormat defaultViewPatternFormatter = new SimpleDateFormat(defaultViewPattern);

	private static final long serialVersionUID = -2509164732286258412L;

	/**
	 * Liefert ein ReddotDate Objekt für den gegebenen Tag im Format yyyyMMdd (20040827) zurück.
	 */
	public static ReddotDate build(String date) throws RQLException {

		return build(defaultPattern, date);
	}

	/**
	 * Liefert ein ReddotDate Objekt für den gegebenen Tag zurück.
	 */
	public static ReddotDate build(String formatPattern, String date) throws RQLException {

		SimpleDateFormat df = new SimpleDateFormat(formatPattern);
		Date d = null;
		try {
			d = df.parse(date);
		} catch (ParseException pe) {
			throw new RQLException("Cannot parse the date " + date + " using format " + formatPattern + ".", pe);
		}
		return new ReddotDate(d);
	}

	/**
	 * Erstellt ein SimpleDateFormat Objekt für das default Datumsformat.
	 */
	private static SimpleDateFormat buildDefaultDateFormat() {

		return defaultPatternFormatter;
	}

	/**
	 * Versucht den gegebenen String dateStr im gegebenen Format formatPattern zu parsen.
	 * 
	 * @throws RQLException
	 */
	public static Date parse(String dateStr, String formatPattern) throws RQLException {
		try {
			return new SimpleDateFormat(formatPattern).parse(dateStr);
		} catch (ParseException ex) {
			throw new RQLException("Could not parse date string " + dateStr + " into format " + formatPattern);
		}
	}

	/**
	 * Liefert einen String im gegebenen Format zurück.
	 */
	public static String format(Date date, String formatPattern) {

		return new SimpleDateFormat(formatPattern).format(date);
	}

	/**
	 * Liefert einen String mit dem Format 27 Aug 2006 zurück.
	 */
	public static String formatAsddMMMyyyy(Date date) {

		return defaultViewPatternFormatter.format(date);
	}

	/**
	 * Liefert einen String mit dem Format 27 Aug 2006 6:15 PM zurück.
	 */
	public static String formatAsddMMMyyyyhmma(Date date) {

		return format(date, defaultViewPattern + " h:mm a");
	}

	/**
	 * Liefert einen String mit dem Format 2009-04-22 15:07:00 zurück.
	 * 
	 * @see #parseFromyyyyMMddHHmmss(String)
	 */
	public static String formatAsyyyyMMddHHmmss(Date date) {

		return format(date, "yyyyMMdd HH:mm:ss");
	}

	/**
	 * Liefert ein Datum aus dem gegebenen String zurück.
	 * 
	 * @see #formatAsyyyyMMddHHmmss(Date)
	 */
	public static Date parseFromyyyyMMddHHmmss(String yyyyMMddHHmmss) throws RQLException {

		return parse(yyyyMMddHHmmss, "yyyyMMdd HH:mm:ss");
	}

	/**
	 * Liefert ein Datum aus dem gegebenen String zurück.
	 * 
	 * @see #formatAsyyyyMMdd(Date)
	 */
	public static ReddotDate parseFromyyyyMMdd(String yyyyMMdd) throws RQLException {

		return new ReddotDate(parse(yyyyMMdd, "yyyyMMdd"));
	}

	/**
	 * Liefert einen String mit dem Format 15:07:00 zurück.
	 */
	public static String formatAsHHmmss(Date date) {

		return format(date, "HH:mm:ss");
	}

	/**
	 * Liefert einen String mit dem Format 07 May 2008 Di zurück.
	 */
	public static String formatAsddMMMyyyyEEE(Date date) {

		return format(date, defaultViewPattern + " EEE");
	}

	/**
	 * Liefert einen String mit dem Format 200840507 Wed zurück.
	 */
	public static String formatAsyyyyMMddEEE(Date date) {

		return format(date, "yyyyMMdd EEE");
	}

	/**
	 * Liefert einen String mit dem Format 20040827 zurück.
	 */
	public static String formatAsyyyyMMdd(Date date) {

		return buildDefaultDateFormat().format(date);
	}

	/**
	 * Liefert einen String mit dem Format 2004-08-27 zurück.
	 */
	public static String formatAsyyyyMMddMinus(Date date) {

		return format(date, "yyyy-MM-dd");
	}

	/**
	 * Liefert das heutige Datum ohne Zeitanteil.
	 */
	public static ReddotDate now() throws RQLException {

		SimpleDateFormat df = buildDefaultDateFormat();
		Date date = null;
		try {
			date = df.parse(df.format(new Date()));
		} catch (ParseException pe) {
			throw new RQLException("Cannot create a new ReddotDate without time.", pe);
		}
		return new ReddotDate(date);
	}

	
	/**
	 * The stored java time is relative to epoch in GMT.
	 * For OT it may be relative to local time. 
	 */
	public void adjustToLocalTime() {
		TimeZone tz = TimeZone.getDefault();
		long time = getTime();
		int offset = tz.getOffset(time);
		setTime(time + offset);
	}

	
	/**
	 * Allocates a <code>Date</code> object and initializes it so that it represents the time at which it was allocated, measured to
	 * the nearest millisecond.
	 * 
	 * @see java.lang.System#currentTimeMillis()
	 */
	public ReddotDate() {
		super();
	}

	/**
	 * ReddotDate constructor comment.
	 */
	public ReddotDate(Date date) {
		super(date.getTime()); // Epoch-GMT-based
	}

	/**
	 * ReddotDate constructor comment.
	 * 
	 * @param msDate
	 *            a timestamp in the Microsoft format used by RedDot
	 */
	public ReddotDate(double msDate) {

		/*
		 * Public Function MicrosoftFloatDateToJavaMilliSecDate(ByVal
		 * fDateInFloat As Double) As Currency 'desc: wandelt ein in Microsoft
		 * like Float gegebenes Datum in 'desc: ein Java like Millisekunden
		 * Datum um
		 * 
		 * 'eine natürliche Konstante, die Millisekunden eines Tages Const
		 * iMSecondsPerDay As Currency = 86400000
		 * 
		 * 'Wandelt man den 1970-01-01 00:00:00 in ein Microsoft Float Datum
		 * erhält man 25569 Const iFirstMSDay As Double = 25569
		 *  ' Nachkommastellen abschneiden MicrosoftFloatDateToJavaMilliSecDate =
		 * Round((fDateInFloat - iFirstMSDay) * iMSecondsPerDay, 0) End Function
		 */

		super(Math.round((msDate - cFirstMSDayInDouble) * cMSecondsPerDay));
	}

	/**
	 * ReddotDate constructor comment.
	 * 
	 * @param msDate
	 *            a timestamp in the Microsoft format used by RedDot
	 */
	public ReddotDate(Double msDate) {

		this(msDate.doubleValue());
	}

	/**
	 * Allocates a <code>Date</code> object and initializes it to represent the specified number of milliseconds since the standard
	 * base time known as "the epoch", namely January 1, 1970, 00:00:00 GMT.
	 * 
	 * @param date
	 *            the milliseconds since January 1, 1970, 00:00:00 GMT.
	 * @see java.lang.System#currentTimeMillis()
	 */
	public ReddotDate(long date) {
		super(date);
	}

	/**
	 * ReddotDate constructor comment.
	 * 
	 * @param msDoubleStr
	 *            Format: "37924.6757407407"
	 */
	public ReddotDate(String msDoubleStr) {
		// take care of maybe German number format settings in OS
		// and replace , with .
		this(Double.valueOf(msDoubleStr.replace(',', '.')));
	}

	/**
	 * Vergleicht die beiden Daten ohne den Zeitanteil.
	 * 
	 * @return the value <code>0</code> if the argument Date is equal to this Date; a value less than <code>0</code> if this Date
	 *         is before the Date argument; and a value greater than
	 */
	public int compareWith(ReddotDate anotherDate) throws RQLException {
		return this.getWithoutTime().compareTo(anotherDate.getWithoutTime());
	}

	/**
	 * Liefert dieses Datum als String im Format 27 Aug zurück.
	 */
	public String getAsddMM() {

		return format(this, "dd MMM");
	}

	/**
	 * Liefert dieses Datum als String im Format 27 Aug 2006 6:15 PM zurück.
	 */
	public String getAsddMMyyyyHmma() {

		return formatAsddMMMyyyyhmma(this);
	}

	/**
	 * Liefert dieses Datum als String im Format 27 Aug 2006 zurück.
	 */
	public String getAsddMMyyyy() {

		return format(this, "dd MMM yyyy");
	}

	/**
	 * Liefert den reinen Zeitanteil dieses Datum als String im Format 16:50:04 zurück.
	 */
	public String getAsHmmss() {

		return format(this, "H:mm:ss");
	}

	/**
	 * Liefert dieses Datum als String mit dem Format 20040827 (27 Aug 2004) zurück.
	 */
	public String getAsyyyyMMdd() {

		return buildDefaultDateFormat().format(this);
	}

	/**
	 * Liefert dieses Datum als String mit dem Format 2004_08_27 (27 Aug 2004) zurück.
	 */
	public String getAsyyyy_MM_dd() {

		return format(this, "yyyy_MM_dd");
	}

	/**
	 * Liefert dieses Datum ohne Zeitanteil zurück.
	 */
	public ReddotDate getWithoutTime() throws RQLException {
		SimpleDateFormat df = buildDefaultDateFormat();
		Date date = null;
		try {
			date = df.parse(this.getAsyyyyMMdd());
		} catch (ParseException pe) {
			throw new RQLException("Cannot create a new ReddotDate without time.", pe);
		}
		return new ReddotDate(date);
	}

	/**
	 * Modifiziert dieses Datum, um die gegebene Anzahl von Tagen. Ist numberOfDays positiv, wird dieses Datum vergrößert, falls
	 * negativ verkleinert.
	 */
	public ReddotDate rollDay(int numberOfDays) {
		setTime(getTime() + numberOfDays * 24L * 60L * 60L * 1000L);
		return this;
	}

	/**
	 * Modifiziert dieser Timestamp, um die gegebene Anzahl von Minuten. Ist numberOfMinutes positiv, wird dieses Timestamp vergrößert,
	 * falls negativ verkleinert.
	 */
	public ReddotDate rollMinutes(int numberOfMinutes) {
		setTime(getTime() + numberOfMinutes * 60L * 1000L);
		return this;
	}

	/**
	 * Liefert die Anzahl der Minuten, die zwischen diesem Datum und dem gegebenen liegen.
	 */
	public long minusAsmm(ReddotDate subtrahend) {
		long minuend = this.getTime();
		long subtr = subtrahend.getTime();
		return (minuend - subtr) / (60L * 1000L);
	}

	/**
	 * Modifiziert dieses Datum, um die gegebene Anzahl von Stunden.. Ist numberOfHours positiv, wird dieses Datum vergrößert, falls
	 * negativ verkleinert.
	 */
	public ReddotDate rollHour(int numberOfHours) {
		setTime(getTime() + numberOfHours * 60L * 1000L);
		return this;
	}

	/**
	 * Liefert das Datum konvertiert in das von RedDot genutzte Microsoft float Format.
	 */
	public double toMsDouble() {

		/*
		 * Public Function JavaMilliSecDateToMicrosoftFloatDate(ByVal
		 * iDateInMSec As Currency) As Double 'desc: wandelt ein in
		 * Millisekunden gegebenes Datum (Java like) in 'desc: eine Microsoft
		 * like Float um
		 * 
		 * 'eine natürliche Konstante, die Millisekunden eines Tages Const
		 * iMSecondsPerDay As Currency = 86400000
		 * 
		 * 'nach Java ist 1970-01-01 00:00:00 die nullte Millisekunde 'Microsoft
		 * hat hier aber schon die 2209161600000 Millisekunde Const
		 * iFirstMSDayInMSec As Currency = 2209161600000#
		 * 
		 * 'bevor das Ergebnis zurueck gegeben wird, (return value) auf 10
		 * Nachkommastellen runden JavaMilliSecDateToMicrosoftFloatDate =
		 * Round(((iDateInMSec + iFirstMSDayInMSec) / iMSecondsPerDay), 10)
		 * 
		 * End Function
		 */

		/*
		 * // for day round to values without fraction final double ten =
		 * 10000000000d; double time = new Long(getTime()).doubleValue();
		 * //return Math.round((time + cFirstMSDayInMilliSec) / cMSecondsPerDay *
		 * ten) / ten; return Math.round((time + cFirstMSDayInMilliSec) /
		 * cMSecondsPerDay);
		 */

		BigDecimal bd = new BigDecimal((getTime() + cFirstMSDayInMilliSec) / cMSecondsPerDay);
		return bd.setScale(10, BigDecimal.ROUND_CEILING).doubleValue();
	}

	/**
	 * Liefert das Datum konvertiert in das von RedDot genutzt Microsoft float Format.
	 */
	public String toMsDoubleString() {

		return Double.toString(toMsDouble());
	}

	/**
	 * Try to parse a given date string from a Smart Tree segment request (e.g. publishing job reports and deleted date from recycle
	 * bin). Try a German and US style date or use publishingJobParseDatePattern from com/hlcl/rql/as/rql_fw.properties.
	 * V6.5.0.41=27.02.2006 V7.5.0.48=7/3/2007 12:24:43
	 */
	public static ReddotDate parseSmartTreeDate(String dateStr) throws RQLException {
		ReddotDate result = null;
		java.util.Locale defaultLocale = java.util.Locale.getDefault();
		// us short
		if (StringHelper.countChar(dateStr, '/') == 2) {
			defaultLocale = java.util.Locale.US;
		}
		// de short
		if (StringHelper.countChar(dateStr, '.') == 2) {
			defaultLocale = java.util.Locale.GERMANY;
		}
		DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, defaultLocale);
		try {
			result = new ReddotDate(dateFormat.parse(dateStr));
		} catch (ParseException pe) {
			// no Germany or us short date format, use parameters
			ResourceBundle rb = ResourceBundle.getBundle("com.hlcl.rql.as.rql_fw");
			String pattern = rb.getString("publishingJobParseDatePattern");

			String locale = rb.getString("publishingJobParseDateLocale");
			String[] locArr = StringHelper.split(locale, "-");
			java.util.Locale loc = new java.util.Locale(locArr[0], locArr[1], "");

			SimpleDateFormat sdf = new SimpleDateFormat(pattern, loc);
			try {
				result = new ReddotDate(sdf.parse(dateStr));
			} catch (ParseException ex) {
				throw new RQLException(
						"The Smart Tree tree segment date cannot be parsed, because "
								+ dateStr
								+ " could not be parsed with pattern "
								+ pattern
								+ " for locale "
								+ locale
								+ ". Please adjust the parameters publishingJobParseDate* in com.hlcl.rlq.as.rql_fw.properties; for symbols see http://download.oracle.com/javase/tutorial/i18n/format/simpleDateFormat.html",
						pe);
			}
		}
		return result;
	}
}
