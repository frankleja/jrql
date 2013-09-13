package com.hlcl.rql.as;

import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Utilities for String formatting, manipulation, and queries. More information about this class is available from <a target="_top"
 * href= "http://ostermiller.org/utils/StringHelper.html">ostermiller.org</a>.
 * 
 * @author Stephen Ostermiller http://ostermiller.org/contact.pl?regarding=Java+Utilities
 * @since ostermillerutils 1.00.00
 */
public class StringHelper {
	private final static String TEXT_EDITOR_LINK_ID = "[ioID]";
	private static HashMap<String, Integer> htmlEntities = new HashMap<String, Integer>();
	static {
		htmlEntities.put("nbsp", new Integer(160));
		htmlEntities.put("iexcl", new Integer(161));
		htmlEntities.put("cent", new Integer(162));
		htmlEntities.put("pound", new Integer(163));
		htmlEntities.put("curren", new Integer(164));
		htmlEntities.put("yen", new Integer(165));
		htmlEntities.put("brvbar", new Integer(166));
		htmlEntities.put("sect", new Integer(167));
		htmlEntities.put("uml", new Integer(168));
		htmlEntities.put("copy", new Integer(169));
		htmlEntities.put("ordf", new Integer(170));
		htmlEntities.put("laquo", new Integer(171));
		htmlEntities.put("not", new Integer(172));
		htmlEntities.put("shy", new Integer(173));
		htmlEntities.put("reg", new Integer(174));
		htmlEntities.put("macr", new Integer(175));
		htmlEntities.put("deg", new Integer(176));
		htmlEntities.put("plusmn", new Integer(177));
		htmlEntities.put("sup2", new Integer(178));
		htmlEntities.put("sup3", new Integer(179));
		htmlEntities.put("acute", new Integer(180));
		htmlEntities.put("micro", new Integer(181));
		htmlEntities.put("para", new Integer(182));
		htmlEntities.put("middot", new Integer(183));
		htmlEntities.put("cedil", new Integer(184));
		htmlEntities.put("sup1", new Integer(185));
		htmlEntities.put("ordm", new Integer(186));
		htmlEntities.put("raquo", new Integer(187));
		htmlEntities.put("frac14", new Integer(188));
		htmlEntities.put("frac12", new Integer(189));
		htmlEntities.put("frac34", new Integer(190));
		htmlEntities.put("iquest", new Integer(191));
		htmlEntities.put("Agrave", new Integer(192));
		htmlEntities.put("Aacute", new Integer(193));
		htmlEntities.put("Acirc", new Integer(194));
		htmlEntities.put("Atilde", new Integer(195));
		htmlEntities.put("Auml", new Integer(196));
		htmlEntities.put("Aring", new Integer(197));
		htmlEntities.put("AElig", new Integer(198));
		htmlEntities.put("Ccedil", new Integer(199));
		htmlEntities.put("Egrave", new Integer(200));
		htmlEntities.put("Eacute", new Integer(201));
		htmlEntities.put("Ecirc", new Integer(202));
		htmlEntities.put("Euml", new Integer(203));
		htmlEntities.put("Igrave", new Integer(204));
		htmlEntities.put("Iacute", new Integer(205));
		htmlEntities.put("Icirc", new Integer(206));
		htmlEntities.put("Iuml", new Integer(207));
		htmlEntities.put("ETH", new Integer(208));
		htmlEntities.put("Ntilde", new Integer(209));
		htmlEntities.put("Ograve", new Integer(210));
		htmlEntities.put("Oacute", new Integer(211));
		htmlEntities.put("Ocirc", new Integer(212));
		htmlEntities.put("Otilde", new Integer(213));
		htmlEntities.put("Ouml", new Integer(214));
		htmlEntities.put("times", new Integer(215));
		htmlEntities.put("Oslash", new Integer(216));
		htmlEntities.put("Ugrave", new Integer(217));
		htmlEntities.put("Uacute", new Integer(218));
		htmlEntities.put("Ucirc", new Integer(219));
		htmlEntities.put("Uuml", new Integer(220));
		htmlEntities.put("Yacute", new Integer(221));
		htmlEntities.put("THORN", new Integer(222));
		htmlEntities.put("szlig", new Integer(223));
		htmlEntities.put("agrave", new Integer(224));
		htmlEntities.put("aacute", new Integer(225));
		htmlEntities.put("acirc", new Integer(226));
		htmlEntities.put("atilde", new Integer(227));
		htmlEntities.put("auml", new Integer(228));
		htmlEntities.put("aring", new Integer(229));
		htmlEntities.put("aelig", new Integer(230));
		htmlEntities.put("ccedil", new Integer(231));
		htmlEntities.put("egrave", new Integer(232));
		htmlEntities.put("eacute", new Integer(233));
		htmlEntities.put("ecirc", new Integer(234));
		htmlEntities.put("euml", new Integer(235));
		htmlEntities.put("igrave", new Integer(236));
		htmlEntities.put("iacute", new Integer(237));
		htmlEntities.put("icirc", new Integer(238));
		htmlEntities.put("iuml", new Integer(239));
		htmlEntities.put("eth", new Integer(240));
		htmlEntities.put("ntilde", new Integer(241));
		htmlEntities.put("ograve", new Integer(242));
		htmlEntities.put("oacute", new Integer(243));
		htmlEntities.put("ocirc", new Integer(244));
		htmlEntities.put("otilde", new Integer(245));
		htmlEntities.put("ouml", new Integer(246));
		htmlEntities.put("divide", new Integer(247));
		htmlEntities.put("oslash", new Integer(248));
		htmlEntities.put("ugrave", new Integer(249));
		htmlEntities.put("uacute", new Integer(250));
		htmlEntities.put("ucirc", new Integer(251));
		htmlEntities.put("uuml", new Integer(252));
		htmlEntities.put("yacute", new Integer(253));
		htmlEntities.put("thorn", new Integer(254));
		htmlEntities.put("yuml", new Integer(255));
		htmlEntities.put("fnof", new Integer(402));
		htmlEntities.put("Alpha", new Integer(913));
		htmlEntities.put("Beta", new Integer(914));
		htmlEntities.put("Gamma", new Integer(915));
		htmlEntities.put("Delta", new Integer(916));
		htmlEntities.put("Epsilon", new Integer(917));
		htmlEntities.put("Zeta", new Integer(918));
		htmlEntities.put("Eta", new Integer(919));
		htmlEntities.put("Theta", new Integer(920));
		htmlEntities.put("Iota", new Integer(921));
		htmlEntities.put("Kappa", new Integer(922));
		htmlEntities.put("Lambda", new Integer(923));
		htmlEntities.put("Mu", new Integer(924));
		htmlEntities.put("Nu", new Integer(925));
		htmlEntities.put("Xi", new Integer(926));
		htmlEntities.put("Omicron", new Integer(927));
		htmlEntities.put("Pi", new Integer(928));
		htmlEntities.put("Rho", new Integer(929));
		htmlEntities.put("Sigma", new Integer(931));
		htmlEntities.put("Tau", new Integer(932));
		htmlEntities.put("Upsilon", new Integer(933));
		htmlEntities.put("Phi", new Integer(934));
		htmlEntities.put("Chi", new Integer(935));
		htmlEntities.put("Psi", new Integer(936));
		htmlEntities.put("Omega", new Integer(937));
		htmlEntities.put("alpha", new Integer(945));
		htmlEntities.put("beta", new Integer(946));
		htmlEntities.put("gamma", new Integer(947));
		htmlEntities.put("delta", new Integer(948));
		htmlEntities.put("epsilon", new Integer(949));
		htmlEntities.put("zeta", new Integer(950));
		htmlEntities.put("eta", new Integer(951));
		htmlEntities.put("theta", new Integer(952));
		htmlEntities.put("iota", new Integer(953));
		htmlEntities.put("kappa", new Integer(954));
		htmlEntities.put("lambda", new Integer(955));
		htmlEntities.put("mu", new Integer(956));
		htmlEntities.put("nu", new Integer(957));
		htmlEntities.put("xi", new Integer(958));
		htmlEntities.put("omicron", new Integer(959));
		htmlEntities.put("pi", new Integer(960));
		htmlEntities.put("rho", new Integer(961));
		htmlEntities.put("sigmaf", new Integer(962));
		htmlEntities.put("sigma", new Integer(963));
		htmlEntities.put("tau", new Integer(964));
		htmlEntities.put("upsilon", new Integer(965));
		htmlEntities.put("phi", new Integer(966));
		htmlEntities.put("chi", new Integer(967));
		htmlEntities.put("psi", new Integer(968));
		htmlEntities.put("omega", new Integer(969));
		htmlEntities.put("thetasym", new Integer(977));
		htmlEntities.put("upsih", new Integer(978));
		htmlEntities.put("piv", new Integer(982));
		htmlEntities.put("bull", new Integer(8226));
		htmlEntities.put("hellip", new Integer(8230));
		htmlEntities.put("prime", new Integer(8242));
		htmlEntities.put("Prime", new Integer(8243));
		htmlEntities.put("oline", new Integer(8254));
		htmlEntities.put("frasl", new Integer(8260));
		htmlEntities.put("weierp", new Integer(8472));
		htmlEntities.put("image", new Integer(8465));
		htmlEntities.put("real", new Integer(8476));
		htmlEntities.put("trade", new Integer(8482));
		htmlEntities.put("alefsym", new Integer(8501));
		htmlEntities.put("larr", new Integer(8592));
		htmlEntities.put("uarr", new Integer(8593));
		htmlEntities.put("rarr", new Integer(8594));
		htmlEntities.put("darr", new Integer(8595));
		htmlEntities.put("harr", new Integer(8596));
		htmlEntities.put("crarr", new Integer(8629));
		htmlEntities.put("lArr", new Integer(8656));
		htmlEntities.put("uArr", new Integer(8657));
		htmlEntities.put("rArr", new Integer(8658));
		htmlEntities.put("dArr", new Integer(8659));
		htmlEntities.put("hArr", new Integer(8660));
		htmlEntities.put("forall", new Integer(8704));
		htmlEntities.put("part", new Integer(8706));
		htmlEntities.put("exist", new Integer(8707));
		htmlEntities.put("empty", new Integer(8709));
		htmlEntities.put("nabla", new Integer(8711));
		htmlEntities.put("isin", new Integer(8712));
		htmlEntities.put("notin", new Integer(8713));
		htmlEntities.put("ni", new Integer(8715));
		htmlEntities.put("prod", new Integer(8719));
		htmlEntities.put("sum", new Integer(8721));
		htmlEntities.put("minus", new Integer(8722));
		htmlEntities.put("lowast", new Integer(8727));
		htmlEntities.put("radic", new Integer(8730));
		htmlEntities.put("prop", new Integer(8733));
		htmlEntities.put("infin", new Integer(8734));
		htmlEntities.put("ang", new Integer(8736));
		htmlEntities.put("and", new Integer(8743));
		htmlEntities.put("or", new Integer(8744));
		htmlEntities.put("cap", new Integer(8745));
		htmlEntities.put("cup", new Integer(8746));
		htmlEntities.put("int", new Integer(8747));
		htmlEntities.put("there4", new Integer(8756));
		htmlEntities.put("sim", new Integer(8764));
		htmlEntities.put("cong", new Integer(8773));
		htmlEntities.put("asymp", new Integer(8776));
		htmlEntities.put("ne", new Integer(8800));
		htmlEntities.put("equiv", new Integer(8801));
		htmlEntities.put("le", new Integer(8804));
		htmlEntities.put("ge", new Integer(8805));
		htmlEntities.put("sub", new Integer(8834));
		htmlEntities.put("sup", new Integer(8835));
		htmlEntities.put("nsub", new Integer(8836));
		htmlEntities.put("sube", new Integer(8838));
		htmlEntities.put("supe", new Integer(8839));
		htmlEntities.put("oplus", new Integer(8853));
		htmlEntities.put("otimes", new Integer(8855));
		htmlEntities.put("perp", new Integer(8869));
		htmlEntities.put("sdot", new Integer(8901));
		htmlEntities.put("lceil", new Integer(8968));
		htmlEntities.put("rceil", new Integer(8969));
		htmlEntities.put("lfloor", new Integer(8970));
		htmlEntities.put("rfloor", new Integer(8971));
		htmlEntities.put("lang", new Integer(9001));
		htmlEntities.put("rang", new Integer(9002));
		htmlEntities.put("loz", new Integer(9674));
		htmlEntities.put("spades", new Integer(9824));
		htmlEntities.put("clubs", new Integer(9827));
		htmlEntities.put("hearts", new Integer(9829));
		htmlEntities.put("diams", new Integer(9830));
		htmlEntities.put("quot", new Integer(34));
		htmlEntities.put("amp", new Integer(38));
		htmlEntities.put("lt", new Integer(60));
		htmlEntities.put("gt", new Integer(62));
		htmlEntities.put("OElig", new Integer(338));
		htmlEntities.put("oelig", new Integer(339));
		htmlEntities.put("Scaron", new Integer(352));
		htmlEntities.put("scaron", new Integer(353));
		htmlEntities.put("Yuml", new Integer(376));
		htmlEntities.put("circ", new Integer(710));
		htmlEntities.put("tilde", new Integer(732));
		htmlEntities.put("ensp", new Integer(8194));
		htmlEntities.put("emsp", new Integer(8195));
		htmlEntities.put("thinsp", new Integer(8201));
		htmlEntities.put("zwnj", new Integer(8204));
		htmlEntities.put("zwj", new Integer(8205));
		htmlEntities.put("lrm", new Integer(8206));
		htmlEntities.put("rlm", new Integer(8207));
		htmlEntities.put("ndash", new Integer(8211));
		htmlEntities.put("mdash", new Integer(8212));
		htmlEntities.put("lsquo", new Integer(8216));
		htmlEntities.put("rsquo", new Integer(8217));
		htmlEntities.put("sbquo", new Integer(8218));
		htmlEntities.put("ldquo", new Integer(8220));
		htmlEntities.put("rdquo", new Integer(8221));
		htmlEntities.put("bdquo", new Integer(8222));
		htmlEntities.put("dagger", new Integer(8224));
		htmlEntities.put("Dagger", new Integer(8225));
		htmlEntities.put("permil", new Integer(8240));
		htmlEntities.put("lsaquo", new Integer(8249));
		htmlEntities.put("rsaquo", new Integer(8250));
		htmlEntities.put("euro", new Integer(8364));
	}

	/**
	 * Build a regular expression that is each of the terms or'd together.
	 * 
	 * @param terms
	 *            a list of search terms.
	 * @param sb
	 *            place to build the regular expression.
	 * @throws IllegalArgumentException
	 *             if the length of terms is zero.
	 * 
	 * @since ostermillerutils 1.02.25
	 */
	public static void buildFindAnyPattern(String[] terms, StringBuffer sb) {
		if (terms.length == 0)
			throw new IllegalArgumentException("There must be at least one term to find.");
		sb.append("(?:");
		for (int i = 0; i < terms.length; i++) {
			if (i > 0)
				sb.append("|");
			sb.append("(?:");
			sb.append(escapeRegularExpressionLiteral(terms[i]));
			sb.append(")");
		}
		sb.append(")");
	}

	/**
	 * Creates the HTML source code for a anchor tag: <a href="....">....</a>
	 * 
	 * @param address
	 *            the value of the href attribute
	 * @param label
	 *            the clickable text of the link
	 * @param shouldOpenInNewWindow
	 *            adds a target="_blank" attribut, if true
	 */
	public static String buildHtmlA(String address, String label, boolean shouldOpenInNewWindow) {
		// set format pattern
		String tagFormat = "<a href=\"{0}\">{1}</a>";
		if (shouldOpenInNewWindow) {
			tagFormat = "<a href=\"{0}\" target=\"_blank\">{1}</a>";
		}

		// build paramter array
		Object[] parms = new Object[2];
		parms[0] = address;
		parms[1] = label;

		// replace
		return MessageFormat.format(tagFormat, parms);
	}

	/**
	 * Creates the HTML source code for a simple HTML comment around the given text.
	 */
	public static String buildHtmlComment(String text) {
		return "<!--" + text + "-->";
	}

	/**
	 * Creates the HTML source code for a simple mailto: anchor tag: <a href="mailto:....">....</a>
	 * 
	 * @param address
	 *            the mail address
	 * @param label
	 *            the clickable text of the link
	 */
	public static String buildHtmlMailto(String address, String label) {
		return buildHtmlA("mailto:" + address, label, false);
	}

	/**
	 * Creates the HTML source code for a simple mailto: anchor tag: <a href="mailto:....">....</a>
	 * 
	 * @param address
	 *            the mail address
	 * @param subject
	 *            the subject information
	 * @param label
	 *            the clickable text of the link
	 */
	public static String buildHtmlMailto(String address, String subject, String label) {
		return buildHtmlMailto(address + "?subject=" + encodeMailTexts(subject), label);
	}

	/**
	 * Changes the first and only the first char of the string s to uppercase. All other letters are converted to lowercase.
	 */
	static public String capitalize(String s) {
		if (s.length() == 0) {
			return s;
		}
		char chars[] = s.toLowerCase().toCharArray();
		chars[0] = Character.toUpperCase(chars[0]);
		return new String(chars);
	}

	/**
	 * Liefert eine liste aller find strings aus findList, die in s vorkommen. Ist die zurückgegebene Liste leer, wurde nichts in s
	 * gefunden.
	 * <p>
	 * Checked case sensitive with indexOf().
	 * 
	 * @param s
	 *            the string to search
	 * @param findList
	 *            the list with strings to check for; for instance HLCL,Container Line,Container Linie
	 * @param delimiter
	 *            the ,
	 * @param caseSensitive
	 *            case sensitive search or not; to ignore case set to false
	 * @return a list of elements of findList
	 */
	public static java.util.List collectContainedText(String s, String findList, String delimiter, boolean caseSensitive) {
		return collectContainedText(s, findList, delimiter, caseSensitive, false);
	}

	/**
	 * Liefert eine liste aller find strings aus findList, die in s vorkommen. Ist die zurückgegebene Liste leer, wurde nichts in s
	 * gefunden.
	 * <p>
	 * Checked case sensitive with indexOf().
	 * 
	 * @param s
	 *            the string to search
	 * @param findList
	 *            the list with strings to check for; for instance HLCL,Container Line,Container Linie
	 * @param delimiter
	 *            the ,
	 * @param caseSensitive
	 *            case sensitive search or not; to ignore case set to false
	 * @param htmlEncodeFindList
	 *            =true, try a HTML encode find text search in addition
	 * @return a list of elements of findList
	 */
	public static java.util.List collectContainedText(String s, String findList, String delimiter, boolean caseSensitive,
			boolean htmlEncodeFindList) {
		// let s unchanged to return find text in correct case
		String[] listArray = StringHelper.split(findList, delimiter);
		java.util.List result = new ArrayList();
		for (int i = 0; i < listArray.length; i++) {
			String find = (String) listArray[i];
			// html encode find text
			if (htmlEncodeFindList) {
			}
			// case sensitive
			int pos = caseSensitive ? s.indexOf(find) : StringHelper.indexOfIgnoreCase(s, find);
			if (pos >= 0) {
				// found
				result.add(s.substring(pos, pos + find.length()));
			} else if (htmlEncodeFindList) {
				// try again html encoded
				find = StringHelper.escapeHTML(find);
				pos = caseSensitive ? s.indexOf(find) : StringHelper.indexOfIgnoreCase(s, find);
				if (pos >= 0) {
					// found
					result.add(s.substring(pos, pos + find.length()));
				}
			}
		}
		return result;
	}

	/**
	 * Liefert true, wenn find in s enthalten ist. Dabei wird Groß- und Kleinschreibung beachtet, ja nach gegebenem ignoreCase.
	 */
	public static boolean contains(String s, String find, boolean ignoreCase) {
		return ignoreCase ? s.toLowerCase().contains(find.toLowerCase()) : s.contains(find);
	}

	/**
	 * Liefert true, wenn in element (index egal) mindestens ein String aus list enthalten ist, sonst false.
	 * <p>
	 * Split list at delimiter and check value.indexOf(each). returns true, if list has no delimiter at all
	 * 
	 * @param list
	 *            the list; for instance e1,e2,e3
	 * @param delimiter
	 *            the ,
	 * @param value
	 *            is this String contained in at least one element of the given list
	 */
	public static boolean contains(String list, String delimiter, String value) {
		String[] listArray = StringHelper.split(list, delimiter);
		for (int i = 0; i < listArray.length; i++) {
			String substring = (String) listArray[i];
			if (value.indexOf(substring) >= 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Liefert true, wenn gegebenes element ein Element von list ist, sonst false.
	 * <p>
	 * Split list at delimiter and check each.equals(element).
	 * 
	 * @param list
	 *            the list; for instance e1,e2,e3
	 * @param delimiter
	 *            the ,
	 * @param element
	 *            is this element an element of the given list
	 * @param isCheckCaseSensitive
	 *            =true check with equals =false check with equalsIgnoreCase
	 */
	public static boolean contains(String list, String delimiter, String element, boolean isCheckCaseSensitive) {

		String[] listArray = StringHelper.split(list, delimiter);
		for (int i = 0; i < listArray.length; i++) {
			String s = (String) listArray[i];
			if (isCheckCaseSensitive) {
				if (s.equals(element)) {
					return true;
				}
			} else {
				if (s.equalsIgnoreCase(element)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Convert in given string s all links starting with 'http:' to a HTML link (&lta href...&gt...&lt/a&gt). All text starting with
	 * http: until the next blank or tag '&lt' is converted.
	 * 
	 * @param s
	 *            the string to convert
	 */
	public static String convertAllHttpLinks(String s) {
		return convertAllHttpLinks(s, false);
	}

	/**
	 * Convert in given string s all links starting with 'http:' to a HTML link (&lta href...&gt...&lt/a&gt). All text starting with
	 * http: until the next blank or tag '&lt' is converted.
	 * 
	 * @param s
	 *            the string to convert
	 * @param correctUppercase
	 *            converts the visible mail address to lowercase
	 */
	public static String convertAllHttpLinks(String s, boolean correctUppercase) {

		String work = s.toLowerCase();
		String pattern = "http:";
		int start = work.indexOf(pattern, 0);
		while (start >= 0) {
			// find end by blank
			int end = work.indexOf(" ", start);
			if (end < 0) {
				end = work.length();
			}
			// find end by next tag
			int end2 = work.indexOf("<", start);
			if (end2 < 0) {
				end2 = work.length();
			}
			end = Math.min(end, end2);

			// convert + replace
			String url = s.substring(start, end);
			if (correctUppercase) {
				url = StringHelper.correctUppercase(url);
			}
			String aTag = buildHtmlA(url, url, true);
			StringBuffer b = new StringBuffer(s);
			s = b.replace(start, end, aTag).toString();

			// next
			work = s.toLowerCase();
			start = work.indexOf(pattern, start + aTag.length());
		}
		return s;
	}

	/**
	 * Convert in given string s all mail links (containing a '@' to a HTML link (&lta href...&gt...&lt/a&gt). Start and end is found
	 * by next blank or tag '&lt or &gt;'.
	 * 
	 * @param s
	 *            the string to convert
	 */
	public static String convertAllMailLinks(String s) {
		return convertAllMailLinks(s, false);
	}

	/**
	 * Convert in given string s all mail links (containing a '@' to a HTML link (&lta href...&gt...&lt/a&gt). Start and end is found
	 * by next blank or tag '&lt or &gt;'.
	 * 
	 * @param s
	 *            the string to convert
	 * @param correctUppercase
	 *            converts the visible mail address to lowercase
	 */
	public static String convertAllMailLinks(String s, boolean correctUppercase) {

		String work = s.toLowerCase();
		String pattern = "@";
		int pos = work.indexOf(pattern, 0);
		while (pos >= 0) {
			// find start by blank
			int start = work.lastIndexOf(" ", pos);
			if (start < 0) {
				start = 0;
			} else {
				start += 1;
			}
			// find start by >
			int start2 = work.lastIndexOf(">", pos);
			if (start2 < 0) {
				start2 = 0;
			} else {
				start2 += 1;
			}
			start = Math.max(start, start2);

			// find end by blank
			int end = work.indexOf(" ", pos);
			if (end < 0) {
				end = work.length();
			}
			// find end by <br
			int end2 = work.indexOf("<", pos);
			if (end2 < 0) {
				end2 = work.length();
			}
			end = Math.min(end, end2);

			// convert + replace
			String address = s.substring(start, end);
			if (correctUppercase) {
				address = StringHelper.correctUppercase(address);
			}
			String aTag = buildHtmlA("mailto:" + address, address, false);
			StringBuffer b = new StringBuffer(s);
			s = b.replace(start, end, aTag).toString();

			// next
			work = s.toLowerCase();
			pos = work.indexOf(pattern, pos + aTag.length());
		}
		return s;
	}

	/**
	 * Liefert den String '0', falls aBoolean false ist oder '1', falls aBoolean true ist.
	 */
	public static String convertTo01(boolean aBoolean) {

		return aBoolean ? "1" : "0";
	}

	/**
	 * Liefert den String 'yes', falls aBoolean true ist, sonst 'no'.
	 */
	public static String convertToYesNo(boolean aBoolean) {

		return aBoolean ? "yes" : "no";
	}

	/**
	 * Liefert boolean true, if and only if the given String is 'true'.
	 */
	public static boolean convertToBoolean(String trueOrFalseString) {

		return "true".equals(trueOrFalseString);
	}

	/**
	 * Liefert true, if and only if the given String is '1'. Returns false, if zeroOr1String is empty or 0.
	 */
	public static boolean convertToBooleanFrom01(String zeroOr1String) {

		return "1".equals(zeroOr1String);
	}

	/**
	 * Liefert string "true", if and only if the given boolean is true.
	 */
	public static String convertToString(boolean trueOrFalse) {

		return trueOrFalse ? "true" : "false";
	}

	/**
	 * Konvertiert eine Liste von Key-Value Paaren in eine Map.
	 * 
	 * @param values
	 *            e.g. SUBJECT=Subject,VESSEL=Vessel,VOY-I=Voyage I
	 * @param pairSeparator
	 *            the ,
	 * @param keyValueSeparator
	 *            the =
	 */
	public static Map<String, String> convertToMap(String values, String pairSeparator, String keyValueSeparator) {

		Map<String, String> map = new HashMap<String, String>();
		String[] pairs = StringHelper.split(values, pairSeparator);
		for (int i = 0; i < pairs.length; i++) {
			String pair = pairs[i];
			String[] keyVal = StringHelper.split(pair, keyValueSeparator);
			map.put(keyVal[0], keyVal[1]);
		}

		return map;
	}

	/**
	 * Konvertiert s zu einem HTML quelltext, wobei ein Seitenlink zwischen start und end zur gegebenen Seite erstellt wird. Die beiden
	 * Marker start und end werden dabei entfernt.\n Falls start oder end nicht gefunden wird, bleibt s unverändert.
	 */
	public static String convertToPageLink(String s, String start, String end, Page page) {

		String linkText = null;
		int from = s.indexOf(start);
		if (from >= 0) {
			int to = s.indexOf(end, from);
			if (to >= 0) {
				linkText = s.substring(from + start.length(), to);
				// TODO extra method
				String linkSource = buildHtmlA(buildTextEditorAHref(page), linkText, false);
				return replaceText(s, start, end, linkSource);
			}
		}
		return s;
	}

	/**
	 * Prefix the page GUID of given page with [ioID] constant.
	 */
	static public String buildTextEditorAHref(Page targetPage) {
		return TEXT_EDITOR_LINK_ID + targetPage.getPageGuid();
	}

	/**
	 * Converts the String s to lowercase, but only if s is all uppercase.
	 * 
	 * Example: 'ABC' is converted to 'abc', but 'Abc' is returned unchanged
	 */
	static public String correctUppercase(String s) {
		String upper = s.toUpperCase();
		return upper.equals(s) ? s.toLowerCase() : s;
	}

	/**
	 * Converts the given array of integers into an array of strings using String.valueOf() for each.
	 */
	static public String[] convertArray(int[] integers) {
		String[] result = new String[integers.length];
		// convert
		for (int i = 0; i < integers.length; i++) {
			int integer = integers[i];
			result[i] = String.valueOf(integer);
		}
		return result;
	}

	/**
	 * Converts the given * pattern, like hip_*_filter.zip, into a regular expression, like hip\_([\w[\.]]*)\_filter\.zip.
	 * <p>
	 * Used to check filename matching. Pattern is created per default without grouping.
	 * 
	 * @param wildcardPattern
	 *            a string with * as a wildcard
	 */
	static public Pattern convert2Regex(String wildcardPattern) {
		return convert2Regex(wildcardPattern, false);
	}

	/**
	 * Converts the given * pattern, like hip_*_filter.zip, into a regular expression, like hip\_([\w[\.]]*)\_filter\.zip.
	 * <p>
	 * Used to check filename matching.
	 * 
	 * @param wildcardPattern
	 *            a string with * as a wildcard
	 * @param groupWildcard
	 *            if true, brackets ( and ) are added around the * and you can use matcher.group(1) to get the group value
	 */
	static public Pattern convert2Regex(String wildcardPattern, boolean groupWildcard) {
		String regex = StringHelper.escapeRegularExpressionLiteral(wildcardPattern);
		// * => word or .
		String replace = "[\\w[\\.]]*";
		if (groupWildcard) {
			replace = "(" + replace + ")";
		}
		regex = StringHelper.replace(regex, "\\*", replace);
		return Pattern.compile(regex);
	}

	/**
	 * Liefert die Regex aus dem gegebenen namePattern. namePattern muss ein {0} entthalten.
	 * <p>
	 * Die gelieferte Regex erzeugt eine Gruppe für {0}.
	 */
	static public Pattern convertFormatPattern2Regex(String namePattern) {
		return StringHelper.convert2Regex(namePattern.replace("{0}", "*"), true);
	}

	/**
	 * Converts a HTML text editor source code to normal text fitting into a StandardField Text.
	 * <p>
	 * Replaces: &nbsp to blank,
	 * <p>
	 * Removes: <BR>,
	 * <P>,
	 * </P>
	 */
	static public String convertFormattedText2Plain(String htmlCode) {
		String result = StringHelper.replace(htmlCode, "&nbsp;", " ");
		result = StringHelper.replace(result, "<BR>", "");
		result = StringHelper.replace(result, "<P>", "");
		result = StringHelper.replace(result, "</P>", "");
		return result;
	}

	/**
	 * Löscht von s den eventuell vorhandenen String trail am Ende.
	 */
	public static String deleteTrail(String s, String trail) {

		StringBuffer b = new StringBuffer(s);
		if (s.endsWith(trail)) {
			b.delete(s.lastIndexOf(trail), s.length());
		}
		return b.toString();
	}

	/**
	 * Encodes given string s into the html entities.
	 * 
	 * @param s
	 *            String to be encoded
	 */
	public static String encodeHtml(String s) {
		// prepare
		int length = s.length();
		// reverse html entities, map Integer to strings
		Map encoder = new HashMap(htmlEntities.size());
		Set keys = htmlEntities.keySet();
		for (Iterator iter = keys.iterator(); iter.hasNext();) {
			String key = (String) iter.next();
			encoder.put((Integer) htmlEntities.get(key), key);
		}

		// for all characters do
		StringBuffer sb = new StringBuffer(length);
		for (int i = 0; i < length; i++) {
			char c = s.charAt(i);
			int cint = 0xffff & c;
			if (cint < 32) {
				switch (c) {
				case '\r':
				case '\n':
				case '\t':
				case '\f': {
					sb.append(c);
				}
					break;
				default: {
					// Remove this character
				}
				}
			} else {
				// > 32
				// replace from encoder map
				Integer key = new Integer(cint);
				String entityOrNull = (String) encoder.get(key);
				if (entityOrNull == null) {
					// append unencoded
					sb.append(c);
				} else {
					// append encoded with entity
					sb.append("&" + entityOrNull + ";");
				}
			}
		}
		return sb.toString();
	}

	/**
	 * Encode mail subject and body texts. Use URLEncoder, but replace the + with space again.
	 * 
	 * @param subjectOrBodyText
	 *            text to convert
	 */
	public static String encodeMailTexts(String subjectOrBodyText) {
		return URLEncoder.encode(subjectOrBodyText).replace('+', ' ');
	}

	/**
	 * Liefert true, wenn der gegebene name auf mindestens einen der gegebenen suffixe endet.
	 * 
	 * @param suffixes
	 *            list of possible suffixes
	 * @param name
	 *            string to check
	 */
	public static boolean endsWithOneOf(String name, String[] suffixes) {

		for (int i = 0; i < suffixes.length; i++) {
			String suffix = (String) suffixes[i];
			if (name.endsWith(suffix)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Liefert true, wenn der gegebene name mit mindestens einem der gegebenen prefixe beginnt.
	 */
	public static boolean startsWithOneOf(String name, String listOfPrefixes, String delimiter) {

		String[] prefixes = StringHelper.split(listOfPrefixes, delimiter);
		for (int i = 0; i < prefixes.length; i++) {
			String prefix = (String) prefixes[i];
			if (name.startsWith(prefix)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Liefert true, wenn der gegebene name auf mindestens einen der gegebenen suffixe endet.
	 * 
	 * @param suffixes
	 *            list of possible suffixes
	 * @param name
	 *            string to check
	 */
	public static boolean endsWithOneOf(String name, java.util.List<String> suffixes) {
		return endsWithOneOf(name, suffixes.toArray(new String[suffixes.size()]));
	}

	/**
	 * Liefert true, wenn der gegebene name auf mindestens einen der gegebenen suffixe endet.
	 * 
	 * @param listOfSuffixes
	 *            list of possible suffixes
	 * @param name
	 *            string to check
	 */
	public static boolean endsWithOneOf(String name, String listOfSuffixes, String delimiter) {
		return endsWithOneOf(name, split(listOfSuffixes, delimiter));
	}

	/**
	 * Replaces characters that may be confused by a HTML parser with their equivalent character entity references.
	 * <p>
	 * Any data that will appear as text on a web page should be be escaped. This is especially important for data that comes from
	 * untrusted sources such as Internet users. A common mistake in CGI programming is to ask a user for data and then put that data
	 * on a web page. For example:
	 * 
	 * <pre>
	 * Server: What is your name?
	 * User: &lt;b&gt;Joe&lt;b&gt;
	 * Server: Hello &lt;b&gt;Joe&lt;/b&gt;, Welcome
	 * </pre>
	 * 
	 * If the name is put on the page without checking that it doesn't contain HTML code or without sanitizing that HTML code, the user
	 * could reformat the page, insert scripts, and control the the content on your web server.
	 * <p>
	 * This method will replace HTML characters such as &gt; with their HTML entity reference (&amp;gt;) so that the html parser will
	 * be sure to interpret them as plain text rather than HTML or script.
	 * <p>
	 * This method should be used for both data to be displayed in text in the html document, and data put in form elements. For
	 * example:<br>
	 * <code>&lt;html&gt;&lt;body&gt;<i>This in not a &amp;lt;tag&amp;gt;
	 * in HTML</i>&lt;/body&gt;&lt;/html&gt;</code><br>
	 * and<br>
	 * <code>&lt;form&gt;&lt;input type="hidden" name="date" value="<i>This data could
	 * be &amp;quot;malicious&amp;quot;</i>"&gt;&lt;/form&gt;</code><br>
	 * In the second example, the form data would be properly be resubmitted to your cgi script in the URLEncoded format:<br>
	 * <code><i>This data could be %22malicious%22</i></code>
	 * 
	 * @param s
	 *            String to be escaped
	 * @return escaped String
	 * @throws NullPointerException
	 *             if s is null.
	 * 
	 * @since ostermillerutils 1.00.00
	 */
	public static String escapeHTML(String s) {
		int length = s.length();
		int newLength = length;
		boolean someCharacterEscaped = false;
		// first check for characters that might
		// be dangerous and calculate a length
		// of the string that has escapes.
		for (int i = 0; i < length; i++) {
			char c = s.charAt(i);
			int cint = 0xffff & c;
			if (cint < 32) {
				switch (c) {
				case '\r':
				case '\n':
				case '\t':
				case '\f': {
				}
					break;
				default: {
					newLength -= 1;
					someCharacterEscaped = true;
				}
				}
			} else {
				switch (c) {
				case '\"': {
					newLength += 5;
					someCharacterEscaped = true;
				}
					break;
				case '&':
				case '\'': {
					newLength += 4;
					someCharacterEscaped = true;
				}
					break;
				case '<':
				case '>': {
					newLength += 3;
					someCharacterEscaped = true;
				}
					break;
				}
			}
		}
		if (!someCharacterEscaped) {
			// nothing to escape in the string
			return s;
		}
		StringBuffer sb = new StringBuffer(newLength);
		for (int i = 0; i < length; i++) {
			char c = s.charAt(i);
			int cint = 0xffff & c;
			if (cint < 32) {
				switch (c) {
				case '\r':
				case '\n':
				case '\t':
				case '\f': {
					sb.append(c);
				}
					break;
				default: {
					// Remove this character
				}
				}
			} else {
				switch (c) {
				case '\"': {
					sb.append("&quot;");
				}
					break;
				case '\'': {
					sb.append("&#39;");
				}
					break;
				case '&': {
					sb.append("&amp;");
				}
					break;
				case '<': {
					sb.append("&lt;");
				}
					break;
				case '>': {
					sb.append("&gt;");
				}
					break;
				default: {
					sb.append(c);
				}
				}
			}
		}
		return sb.toString();
	}

	/**
	 * Replaces characters that are not allowed in a Java style string literal with their escape characters. Specifically quote ("),
	 * single quote ('), new line (\n), carriage return (\r), and backslash (\), and tab (\t) are escaped.
	 * 
	 * @param s
	 *            String to be escaped
	 * @return escaped String
	 * @throws NullPointerException
	 *             if s is null.
	 * 
	 * @since ostermillerutils 1.00.00
	 */
	public static String escapeJavaLiteral(String s) {
		int length = s.length();
		int newLength = length;
		// first check for characters that might
		// be dangerous and calculate a length
		// of the string that has escapes.
		for (int i = 0; i < length; i++) {
			char c = s.charAt(i);
			switch (c) {
			case '\"':
			case '\'':
			case '\n':
			case '\r':
			case '\t':
			case '\\': {
				newLength += 1;
			}
				break;
			}
		}
		if (length == newLength) {
			// nothing to escape in the string
			return s;
		}
		StringBuffer sb = new StringBuffer(newLength);
		for (int i = 0; i < length; i++) {
			char c = s.charAt(i);
			switch (c) {
			case '\"': {
				sb.append("\\\"");
			}
				break;
			case '\'': {
				sb.append("\\\'");
			}
				break;
			case '\n': {
				sb.append("\\n");
			}
				break;
			case '\r': {
				sb.append("\\r");
			}
				break;
			case '\t': {
				sb.append("\\t");
			}
				break;
			case '\\': {
				sb.append("\\\\");
			}
				break;
			default: {
				sb.append(c);
			}
			}
		}
		return sb.toString();
	}

	/**
	 * Escapes characters that have special meaning to regular expressions
	 * 
	 * @param s
	 *            String to be escaped
	 * @return escaped String
	 * @throws NullPointerException
	 *             if s is null.
	 * 
	 * @since ostermillerutils 1.02.25
	 */
	public static String escapeRegularExpressionLiteral(String s) {
		// According to the documentation in the Pattern class:
		//
		// The backslash character ('\') serves to introduce escaped constructs,
		// as defined in the table above, as well as to quote characters that
		// otherwise would be interpreted as unescaped constructs. Thus the
		// expression \\ matches a single backslash and \{ matches a left brace.
		//
		// It is an error to use a backslash prior to any alphabetic character
		// that does not denote an escaped construct; these are reserved for future
		// extensions to the regular-expression language. A backslash may be used
		// prior to a non-alphabetic character regardless of whether that character
		// is part of an unescaped construct.
		//
		// As a result, escape everything except [0-9a-zA-Z]

		int length = s.length();
		int newLength = length;
		// first check for characters that might
		// be dangerous and calculate a length
		// of the string that has escapes.
		for (int i = 0; i < length; i++) {
			char c = s.charAt(i);
			if (!((c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z'))) {
				newLength += 1;
			}
		}
		if (length == newLength) {
			// nothing to escape in the string
			return s;
		}
		StringBuffer sb = new StringBuffer(newLength);
		for (int i = 0; i < length; i++) {
			char c = s.charAt(i);
			if (!((c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z'))) {
				sb.append('\\');
			}
			sb.append(c);
		}
		return sb.toString();
	}

	/**
	 * Replaces characters that may be confused by an SQL parser with their equivalent escape characters.
	 * <p>
	 * Any data that will be put in an SQL query should be be escaped. This is especially important for data that comes from untrusted
	 * sources such as Internet users.
	 * <p>
	 * For example if you had the following SQL query:<br>
	 * <code>"SELECT * FROM addresses WHERE name='" + name + "' AND private='N'"</code><br>
	 * Without this function a user could give <code>" OR 1=1 OR ''='"</code> as their name causing the query to be:<br>
	 * <code>"SELECT * FROM addresses WHERE name='' OR 1=1 OR ''='' AND private='N'"</code><br>
	 * which will give all addresses, including private ones.<br>
	 * Correct usage would be:<br>
	 * <code>"SELECT * FROM addresses WHERE name='" + StringHelper.escapeSQL(name) + "' AND private='N'"</code><br>
	 * <p>
	 * Another way to avoid this problem is to use a PreparedStatement with appropriate placeholders.
	 * 
	 * @param s
	 *            String to be escaped
	 * @return escaped String
	 * @throws NullPointerException
	 *             if s is null.
	 * 
	 * @since ostermillerutils 1.00.00
	 */
	public static String escapeSQL(String s) {
		int length = s.length();
		int newLength = length;
		// first check for characters that might
		// be dangerous and calculate a length
		// of the string that has escapes.
		for (int i = 0; i < length; i++) {
			char c = s.charAt(i);
			switch (c) {
			case '\\':
			case '\"':
			case '\'':
			case '\0': {
				newLength += 1;
			}
				break;
			}
		}
		if (length == newLength) {
			// nothing to escape in the string
			return s;
		}
		StringBuffer sb = new StringBuffer(newLength);
		for (int i = 0; i < length; i++) {
			char c = s.charAt(i);
			switch (c) {
			case '\\': {
				sb.append("\\\\");
			}
				break;
			case '\"': {
				sb.append("\\\"");
			}
				break;
			case '\'': {
				sb.append("\\\'");
			}
				break;
			case '\0': {
				sb.append("\\0");
			}
				break;
			default: {
				sb.append(c);
			}
			}
		}
		return sb.toString();
	}

	/**
	 * Returns the value of attribute (first occurence!) in markupTag or null if not found. Case ignored.
	 */
	public static String getAttributeValue(String markupTag, String attributeName) {
		int pos = markupTag.toLowerCase().indexOf(" " + attributeName.toLowerCase());
		if (pos < 0) {
			return null;
		} else {
			int start = markupTag.indexOf("=\"", pos);
			int end = markupTag.indexOf("\"", start + 2);
			return markupTag.substring(start + 2, end);
		}
	}

	/**
	 * Returns the first letter of the given string or null, if length = 0.
	 */
	static public String getFirstLetter(String s) {
		if (s.length() == 0) {
			return null;
		}
		return Character.toString(s.charAt(0));
	}

	/**
	 * Liefert das erste Wort (bis zum ersten blank) oder den gesamten string, falls kein blank enthalten.
	 */
	public static String getFirstWord(String s) {

		String word;

		int pos = s.indexOf(" ");
		if ((pos < 0)) {
			word = s;
		} else {
			word = s.substring(0, pos);
		}

		return word;
	}

	/**
	 * Liefert den Text aus s trailLength Zeichen vor der maximalen Länge maxLength.
	 * <p>
	 * Liefert einen Leerstring, falls maxLength > s.length() and s, falls trailLength >= maxLength.
	 */
	public static String getMaxLengthHint(String s, int maxLength, int trailLength) {
		// check parms
		int len = s.length();
		if (maxLength > len) {
			return "";
		}
		if (maxLength - trailLength < 0) {
			return s;
		}
		return right(s.substring(0, maxLength), trailLength);
	}

	/**
	 * Returns the tag from the markup (html, xml) which contains find or null if not found at all in markup. Case ignored.
	 */
	public static String getStartTag(String markup, String find) {
		int pos = markup.toLowerCase().indexOf(find.toLowerCase());
		if (pos < 0) {
			return null;
		} else {
			int start = markup.lastIndexOf("<", pos - 1);
			int end = markup.indexOf(">", pos + 1);
			return markup.substring(start, end + 1);
		}
	}

	/**
	 * Liefert den Text aus s zwischen den beiden markern, falls start gefunden wurde, sonst null.
	 */
	public static String getTextBetween(String s, String start, String end) {

		int startPos = s.indexOf(start);
		if (startPos < 0) {
			return null; // not found
		} else {
			int endPos = s.indexOf(end, startPos);
			if (endPos < 0) {
				return null;
			} else {
				return s.substring(startPos + start.length(), endPos);
			}
		}
	}

	/**
	 * Liefert den Text aus s zwischen den beiden markern, falls start gefunden wurde, sonst den notFoundValue.
	 */
	public static String getTextBetween(String s, String start, String end, String notFoundValue) {
		String result = getTextBetween(s, start, end);
		return result != null ? result : notFoundValue;
	}

	/**
	 * Liefert den Text aus source (inkl. des tags) des ersten gefundenen Tags mit dem Namen tagName. Liefert null, falls das Tag gar
	 * nicht vorkommt.
	 */
	public static String getTag(String source, String tagName) {
		return surroundTag(getTextBetweenTag(source, tagName), tagName);
	}

	/**
	 * Umgibt den gegebenen source mit einem Tag mit dem gegegenen Namen.
	 */
	public static String surroundTag(String source, String tagName) {
		return "<" + tagName + ">" + source + "</" + tagName + ">";
	}

	/**
	 * Enclose the given source with the reddot block mark tagName.
	 * <p>
	 * For instance for tagName = IoRangeRedDotMode you will get this: <!IoRangeRedDotMode>...<!/IoRangeRedDotMode>
	 */
	public static String surroundReddotBlockMark(String source, String tagName) {
		return "<!" + tagName + ">" + source + "<!/" + tagName + ">";
	}

	/**
	 * Enclose the given source with the reddot block mark for SmartEdit mode: <!IoRangeRedDotMode>source<!/IoRangeRedDotMode> 
	 */
	public static String surroundIoRangeRedDotMode(String source) {
		return surroundReddotBlockMark(source, "IoRangeRedDotMode");
	}

	/**
	 * Liefert den Text aus sourceCode zwischen dem ersten gefundenen Tag mit dem Namen tagName. Liefert null, falls das Tag gar nicht
	 * vorkommt.
	 */
	public static String getTextBetweenTag(String source, String tagName) {

		// calculate start and end position
		String s = source.toLowerCase();
		String tn = tagName.toLowerCase();
		int p1 = s.indexOf("<" + tn);
		if (p1 < 0) {
			return null; // not found
		} else {
			// next >
			int p2 = s.indexOf(">", p1);
			if (p2 < 0) {
				return null; // invalid source, missing closing >
			} else {
				// search closing tag </tagName>
				int p3 = s.indexOf("</" + tn);
				if (p3 < 0) {
					return null; // invalid source, missing closing tag
				} else {
					return source.substring(p2 + 1, p3);
				}
			}
		}
	}

	/**
	 * Liefert true, wenn in der gegebenen Liste von Strings mindestens ein Wert (nach trim()) enthalten ist, sonst false.
	 * 
	 * @param list
	 *            list of strings
	 */
	public static boolean hasListAtLeastOneValue(java.util.List list) {

		for (Iterator iter = list.iterator(); iter.hasNext();) {
			String element = (String) iter.next();
			if (element.trim().length() > 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the index of the substring find in s ignoring case.
	 */
	public static int indexOfIgnoreCase(String s, String find) {
		return s.toLowerCase().indexOf(find.toLowerCase());
	}

	/**
	 * Liefert true, falls der gegebenen String s dem pattern entspricht.
	 * 
	 * @param s
	 *            String to check
	 * @param pattern
	 *            ein String, der genau einen wildcard * enthält; alle checks sind case sensitive!
	 */
	public static boolean matches(String s, String pattern) {
		String wildcard = "*";
		int pos = pattern.indexOf(wildcard.charAt(0));
		int length = pattern.length();
		if (pos < 0) {
			return s.equalsIgnoreCase(pattern);
		} else {
			// remove the *
			String toCheck = StringHelper.replace(pattern, wildcard, "");
			// *pattern
			if (pos == 0) {
				return s.endsWith(toCheck);
			}
			// pattern*
			if (pos == length - 1) {
				return s.startsWith(toCheck);
			} else {
				// * in the middle
				String p1 = pattern.substring(0, pos);
				String p2 = pattern.substring(pos + 1);
				return s.startsWith(p1) && s.endsWith(p2);
			}
		}
	}

	/**
	 * Pad the beginning and end of the given String with spaces until the String is of the given length. The result is that the
	 * original String is centered in the middle of the new string.
	 * <p>
	 * If the number of characters to pad is even, then the padding will be split evenly between the beginning and end, otherwise, the
	 * extra character will be added to the end.
	 * <p>
	 * If a String is longer than the desired length, it will not be truncated, however no padding will be added.
	 * 
	 * @param s
	 *            String to be padded.
	 * @param length
	 *            desired length of result.
	 * @return padded String.
	 * @throws NullPointerException
	 *             if s is null.
	 * 
	 * @since ostermillerutils 1.00.00
	 */
	public static String midpad(String s, int length) {
		return midpad(s, length, ' ');
	}

	/**
	 * Pad the beginning and end of the given String with the given character until the result is the desired length. The result is
	 * that the original String is centered in the middle of the new string.
	 * <p>
	 * If the number of characters to pad is even, then the padding will be split evenly between the beginning and end, otherwise, the
	 * extra character will be added to the end.
	 * <p>
	 * If a String is longer than the desired length, it will not be truncated, however no padding will be added.
	 * 
	 * @param s
	 *            String to be padded.
	 * @param length
	 *            desired length of result.
	 * @param c
	 *            padding character.
	 * @return padded String.
	 * @throws NullPointerException
	 *             if s is null.
	 * 
	 * @since ostermillerutils 1.00.00
	 */
	public static String midpad(String s, int length, char c) {
		int needed = length - s.length();
		if (needed <= 0) {
			return s;
		}
		int beginning = needed / 2;
		int end = beginning + needed % 2;
		StringBuffer sb = new StringBuffer(length);
		for (int i = 0; i < beginning; i++) {
			sb.append(c);
		}
		sb.append(s);
		for (int i = 0; i < end; i++) {
			sb.append(c);
		}
		return (sb.toString());
	}

	/**
	 * Splits the line using fixed positions and collect into an list of strings.
	 * 
	 * @param positionsStr
	 *            separated list of start positions of each field e.g. 0,8,12,20
	 * @param separator
	 *            the one size string separating field positions in positions
	 * @param line
	 *            string to split
	 * @return list of trimmed strings; length same as given in positions
	 */
	public static java.util.List posSplit(String positionsStr, String separator, String line) {

		java.util.List fields = new ArrayList();

		String[] positions = split(positionsStr, separator);
		for (int i = 0; i < positions.length; i++) {
			int startPos = Integer.parseInt(positions[i]);
			String value = null;
			if (startPos >= line.length()) {
				value = "";
			} else {
				if (i == positions.length - 1) {
					// last position reached
					value = line.substring(startPos);
				} else {
					// next field position available
					int endPos = Integer.parseInt(positions[i + 1]);
					value = endPos > line.length() ? line.substring(startPos) : line.substring(startPos, endPos);
				}
			}
			fields.add(value.trim());
		}

		return fields;

	}

	/**
	 * Pad the end of the given String with spaces until the String is of the given length.
	 * <p>
	 * If a String is longer than the desired length, it will not be truncated, however no padding will be added.
	 * 
	 * @param s
	 *            String to be padded.
	 * @param length
	 *            desired length of result.
	 * @return padded String.
	 * @throws NullPointerException
	 *             if s is null.
	 * 
	 * @since ostermillerutils 1.00.00
	 */
	public static String postpad(String s, int length) {
		return postpad(s, length, ' ');
	}

	/**
	 * Append the given character to the String until the result is the desired length.
	 * <p>
	 * If a String is longer than the desired length, it will not be truncated, however no padding will be added.
	 * 
	 * @param s
	 *            String to be padded.
	 * @param length
	 *            desired length of result.
	 * @param c
	 *            padding character.
	 * @return padded String.
	 * @throws NullPointerException
	 *             if s is null.
	 * 
	 * @since ostermillerutils 1.00.00
	 */
	public static String postpad(String s, int length, char c) {
		int needed = length - s.length();
		if (needed <= 0) {
			return s;
		}
		StringBuffer sb = new StringBuffer(length);
		sb.append(s);
		for (int i = 0; i < needed; i++) {
			sb.append(c);
		}
		return (sb.toString());
	}

	/**
	 * Pad the beginning of the given String with spaces until the String is of the given length.
	 * <p>
	 * If a String is longer than the desired length, it will not be truncated, however no padding will be added.
	 * 
	 * @param s
	 *            String to be padded.
	 * @param length
	 *            desired length of result.
	 * @return padded String.
	 * @throws NullPointerException
	 *             if s is null.
	 * 
	 * @since ostermillerutils 1.00.00
	 */
	public static String prepad(String s, int length) {
		return prepad(s, length, ' ');
	}

	/**
	 * Pre-pend the given character to the String until the result is the desired length.
	 * <p>
	 * If a String is longer than the desired length, it will not be truncated, however no padding will be added.
	 * 
	 * @param s
	 *            String to be padded.
	 * @param length
	 *            desired length of result.
	 * @param c
	 *            padding character.
	 * @return padded String.
	 * @throws NullPointerException
	 *             if s is null.
	 * 
	 * @since ostermillerutils 1.00.00
	 */
	public static String prepad(String s, int length, char c) {
		int needed = length - s.length();
		if (needed <= 0) {
			return s;
		}
		StringBuffer sb = new StringBuffer(length);
		for (int i = 0; i < needed; i++) {
			sb.append(c);
		}
		sb.append(s);
		return (sb.toString());
	}

	/**
	 * Replace occurrences of a substring.
	 * 
	 * StringHelper.replace("1-2-3", "-", "|");<br>
	 * result: "1|2|3"<br>
	 * StringHelper.replace("-1--2-", "-", "|");<br>
	 * result: "|1||2|"<br>
	 * StringHelper.replace("123", "", "|");<br>
	 * result: "123"<br>
	 * StringHelper.replace("1-2---3----4", "--", "|");<br>
	 * result: "1-2|-3||4"<br>
	 * StringHelper.replace("1-2---3----4", "--", "---");<br>
	 * result: "1-2----3------4"<br>
	 * 
	 * @param s
	 *            String to be modified.
	 * @param find
	 *            String to find.
	 * @param replace
	 *            String to replace.
	 * @return a string with all the occurrences of the string to find replaced.
	 * @throws NullPointerException
	 *             if s is null.
	 * 
	 * @since ostermillerutils 1.00.00
	 */
	public static String replace(String s, String find, String replace) {
		int findLength;
		// the next statement has the side effect of throwing a null pointer
		// exception if s is null.
		int stringLength = s.length();
		if (find == null || (findLength = find.length()) == 0) {
			// If there is nothing to find, we won't try and find it.
			return s;
		}
		if (replace == null) {
			// a null string and an empty string are the same
			// for replacement purposes.
			replace = "";
		}
		int replaceLength = replace.length();

		// We need to figure out how long our resulting string will be.
		// This is required because without it, the possible resizing
		// and copying of memory structures could lead to an unacceptable runtime.
		// In the worst case it would have to be resized n times with each
		// resize having a O(n) copy leading to an O(n^2) algorithm.
		int length;
		if (findLength == replaceLength) {
			// special case in which we don't need to count the replacements
			// because the count falls out of the length formula.
			length = stringLength;
		} else {
			int count;
			int start;
			int end;

			// Scan s and count the number of times we find our target.
			count = 0;
			start = 0;
			while ((end = s.indexOf(find, start)) != -1) {
				count++;
				start = end + findLength;
			}
			if (count == 0) {
				// special case in which on first pass, we find there is nothing
				// to be replaced. No need to do a second pass or create a string buffer.
				return s;
			}
			length = stringLength - (count * (findLength - replaceLength));
		}

		int start = 0;
		int end = s.indexOf(find, start);
		if (end == -1) {
			// nothing was found in the string to replace.
			// we can get this if the find and replace strings
			// are the same length because we didn't check before.
			// in this case, we will return the original string
			return s;
		}
		// it looks like we actually have something to replace
		// *sigh* allocate memory for it.
		StringBuffer sb = new StringBuffer(length);

		// Scan s and do the replacements
		while (end != -1) {
			sb.append(s.substring(start, end));
			sb.append(replace);
			start = end + findLength;
			end = s.indexOf(find, start);
		}
		end = stringLength;
		sb.append(s.substring(start, end));

		return (sb.toString());
	}

	/**
	 * Ersetzt den Text aus s zwischen dem Tag <tagName> und </tagName>, falls beide gefunden wurden, durch with.\n Die Tags bleiben
	 * beide erhalten, sie werden nicht entfernt.\n
	 */
	public static String replaceTagValue(String s, String tagName, String with) {
		return replaceTextBetween(s, "<" + tagName + ">", "</" + tagName + ">", with);
	}

	/**
	 * Ersetzt alle werte aus der liste findList (getrennt duch delimiter) mit replace.
	 */
	public static String replaceListValues(String s, String findList, String delimiter, String replace) {
		java.util.List<String> finds = StringHelper.split(findList, delimiter.charAt(0));
		// try to replace all in findList
		for (Iterator iterator = finds.iterator(); iterator.hasNext();) {
			String find = (String) iterator.next();
			s = StringHelper.replace(s, find, replace);
		}
		return s;
	}

	/**
	 * Ersetzt den Text aus s von start bis end, falls start gefunden wurde, durch with.\n Die beiden Marker start und end werden dabei
	 * entfernt.\n Falls start oder end nicht gefunden wird, bleibt s unverändert.
	 */
	public static String replaceText(String s, String start, String end, String with) {

		int startPos = s.indexOf(start);
		if (startPos >= 0) {
			int endPos = s.indexOf(end, startPos);
			if (endPos >= 0) {
				return s.substring(0, startPos) + with + s.substring(endPos + end.length(), s.length());
			}
		}
		return s;
	}

	/**
	 * Ersetzt den Text aus s zwischen den beiden markern start und end, falls start gefunden wurde durch with.\n Die beiden Marker
	 * start und end bleiben erhalten.\n Falls start oder end nicht gefunden wird, bleibt s unverändert.
	 */
	public static String replaceTextBetween(String s, String start, String end, String with) {

		return replaceText(s, start, end, start + with + end);
	}

	/**
	 * Returns the length character from the right of the given string s.
	 */
	static public String right(String s, int fromRight) {
		int l = s.length();
		return s.substring(l - fromRight, l);
	}

	/**
	 * Try to removes prefix from s. If s doen't starts with prefix return s unchanged.
	 */
	static public String removePrefix(String s, String prefix) {
		if (s.startsWith(prefix)) {
			return s.substring(prefix.length(), s.length());
		}
		return s;
	}

	/**
	 * Try to removes all blanks from s. If s doesn't have any blanks s is returned unchanged.
	 */
	static public String removeBlanks(String s) {
		return replace(s, " ", "");
	}

	/**
	 * Try to removes prefix from all entries of given array. If an entry doesn't starts with prefix the entry is returned unchanged.
	 */
	static public String[] removePrefix(String[] array, String prefix) {
		String[] result = new String[array.length];
		for (int i = 0; i < array.length; i++) {
			String entry = array[i];
			result[i] = StringHelper.removePrefix(entry, prefix);
		}
		return result;
	}

	/**
	 * Try to removes every given suffix from s in given order. If it doesn't ends with one of suffixes at all s is returned unchanged.
	 */
	static public String removeSuffixes(String s, String[] suffixes, boolean ignoreCase) {
		for (String suffix : suffixes) {
			s = removeSuffix(s, suffix, ignoreCase);
		}
		return s;
	}

	/**
	 * Try to removes suffix from s. If s doen't ends with suffix return s unchanged.
	 */
	static public String removeSuffix(String s, String suffix) {
		return removeSuffix(s, suffix, false);
	}

	/**
	 * Try to removes suffix from s. If s doen't ends with suffix return s unchanged.
	 */
	static public String removeSuffix(String s, String suffix, boolean ignoreCase) {
		String ns = s;
		String nSuffix = suffix;
		if (ignoreCase) {
			ns = s.toLowerCase();
			nSuffix = suffix.toLowerCase();
		}
		if (ns.endsWith(nSuffix)) {
			return s.substring(0, ns.lastIndexOf(nSuffix));
		}
		return s;
	}

	/**
	 * Returns s unchanged if length is <= maxLength, otherwise s is cutted to maxLength.
	 */
	static public String ensureLength(String s, int maxLength) {
		return s.length() <= maxLength ? s : s.substring(0, maxLength);
	}

	/**
	 * Ensures that given path ends with suffix (e.g. /).
	 */
	static public String ensureEnding(String pathName, String suffix) {
		return pathName.endsWith(suffix) ? pathName : pathName + suffix;
	}

	/**
	 * Converts the camelcase string s (SanFranciscoBay) into a words (=San Francisco Bay) separated by given separator.
	 */
	static public String convertCamelCase2words(String s, String separator) {
		StringBuffer result = new StringBuffer();
		// go through all characters
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			// check for uppercase
			if (Character.isUpperCase(c)) {
				// insert separator
				// not at beginning
				if (i > 0) {
					result.append(separator);
				}
				result.append(c);
			} else {
				// simply append
				result.append(c);
			}
		}
		return result.toString();
	}

	/**
	 * Shorten the given text to max length targetLength. The ... are inserted in the middle.
	 */
	static public String shorten(String s, int targetLength) {
		String middle = "...";
		int l = s.length();
		// nothing to do
		if (l <= targetLength)
			return s;
		// check minimum length
		if (targetLength < 5)
			return s;
		int end = targetLength / 2;
		if (targetLength < 7)
			end -= 1;
		int rest = targetLength - end - middle.length();
		return s.substring(0, end) + middle + right(s, rest);
	}

	/**
	 * This methods counts how often the character c is contained in string s. Returns -1, if c is not within s.
	 */
	public static int countChar(String s, char c) {
		if (s.indexOf(c) < 0) {
			return -1;
		}
		return split(s, c).size() - 1;
	}

	/**
	 * Diese Methode spaltet einen String am gegebenen separator auf.
	 * 
	 * @param list
	 *            string which needs to be splitted
	 * @param separator
	 *            delimiter of items
	 * @return <code>List</code>
	 */
	public static java.util.List<String> split(String list, char separator) {

		// in contrast to other split an empty list string will return empty list and not a list with one entry
		if (list.length() == 0) {
			return new ArrayList<String>();
		}

		// use split to array
		String[] array = split(list, String.valueOf(separator));

		// copy to a real array list; Arrays.asList() returns a fixed size list only
		java.util.List<String> returnList = new ArrayList<String>();
		for (int i = 0; i < array.length; i++) {
			returnList.add(array[i]);
		}

		return returnList;
	}

	/**
	 * Split the given String into tokens.
	 * <P>
	 * This method is meant to be similar to the split function in other programming languages but it does not use regular expressions.
	 * Rather the String is split on a single String literal.
	 * <P>
	 * Unlike java.util.StringTokenizer which accepts multiple character tokens as delimiters, the delimiter here is a single String
	 * literal.
	 * <P>
	 * Each null token is returned as an empty String. Delimiters are never returned as tokens.
	 * <P>
	 * If there is no delimiter because it is either empty or null, the only element in the result is the original String.
	 * <P>
	 * StringHelper.split("1-2-3", "-");<br>
	 * result: {"1", "2", "3"}<br>
	 * StringHelper.split("-1--2-", "-");<br>
	 * result: {"", "1", ,"", "2", ""}<br>
	 * StringHelper.split("123", "");<br>
	 * result: {"123"}<br>
	 * StringHelper.split("1-2---3----4", "--");<br>
	 * result: {"1-2", "-3", "", "4"}<br>
	 * 
	 * @param s
	 *            String to be split.
	 * @param delimiter
	 *            String literal on which to split.
	 * @return an array of tokens.
	 * @throws NullPointerException
	 *             if s is null.
	 * 
	 * @since ostermillerutils 1.00.00
	 */
	public static String[] split(String s, String delimiter) {
		int delimiterLength;
		// the next statement has the side effect of throwing a null pointer
		// exception if s is null.
		int stringLength = s.length();
		if (delimiter == null || (delimiterLength = delimiter.length()) == 0) {
			// it is not inherently clear what to do if there is no delimiter
			// On one hand it would make sense to return each character because
			// the null String can be found between each pair of characters in
			// a String. However, it can be found many times there and we don'
			// want to be returning multiple null tokens.
			// returning the whole String will be defined as the correct behavior
			// in this instance.
			return new String[] { s };
		}

		// a two pass solution is used because a one pass solution would
		// require the possible resizing and copying of memory structures
		// In the worst case it would have to be resized n times with each
		// resize having a O(n) copy leading to an O(n^2) algorithm.

		int count;
		int start;
		int end;

		// Scan s and count the tokens.
		count = 0;
		start = 0;
		while ((end = s.indexOf(delimiter, start)) != -1) {
			count++;
			start = end + delimiterLength;
		}
		count++;

		// allocate an array to return the tokens,
		// we now know how big it should be
		String[] result = new String[count];

		// Scan s again, but this time pick out the tokens
		count = 0;
		start = 0;
		while ((end = s.indexOf(delimiter, start)) != -1) {
			result[count] = (s.substring(start, end));
			count++;
			start = end + delimiterLength;
		}
		end = stringLength;
		result[count] = s.substring(start, end);

		return (result);
	}

	/**
	 * Teilt den gegebenen String s am ersten von links gefundenen Auftreten von delimiter. Der delimiter selbst wird nicht
	 * zurückgegeben. Liefert ein Array zurück:
	 * <p>
	 * [0] - links des String delimiter (oder empty String, falls delimiter nicht in s) [1] - rechts von delimiter (oder der gesamte
	 * Strings, falls delimiter nicht in s)
	 */
	public static String[] splitAt1stOccurenceFromLeft(String s, String delimiter) {
		String[] result = new String[2];
		int pos = s.indexOf(delimiter);
		if (pos < 0) {
			// not found
			result[0] = "";
			result[1] = s;
		} else {
			// found, split it
			result[0] = s.substring(0, pos);
			result[1] = s.substring(pos + delimiter.length());
		}
		return result;
	}

	/**
	 * Converts the given list into a String separated by the given delimiter.
	 */
	static public String toString(java.util.List<String> list, String delimiter) {
		if (list.size() == 0) {
			return "";
		}
		StringBuffer buffer = new StringBuffer();
		for (Iterator<String> iter = list.iterator(); iter.hasNext();) {
			String s = iter.next();
			buffer.append(s).append(delimiter);
		}
		// remove last
		int len = buffer.length();
		buffer.delete(len - delimiter.length(), len);
		return buffer.toString();
	}

	/**
	 * Order the given set of elements by the delimiter separated list of elements in elementOrder.
	 * <p>
	 * Returns only elements contained in elementOrder!
	 */
	static public java.util.List<String> orderElements(Set<String> elements, String elementOrder, String delimiter) {

		java.util.List<String> result = new ArrayList<String>();

		// check each element
		String[] parts = split(elementOrder, delimiter);
		for (int i = 0; i < parts.length; i++) {
			String part = parts[i];
			if (elements.contains(part)) {
				result.add(part);
			}
		}
		return result;
	}

	/**
	 * Trim any of the characters contained in the second string from the beginning and end of the first.
	 * 
	 * @param s
	 *            String to be trimmed.
	 * @param c
	 *            list of characters to trim from s.
	 * @return trimmed String.
	 * @throws NullPointerException
	 *             if s is null.
	 * 
	 * @since ostermillerutils 1.00.00
	 */
	public static String trim(String s, String c) {
		int length = s.length();
		if (c == null) {
			return s;
		}
		int cLength = c.length();
		if (c.length() == 0) {
			return s;
		}
		int start = 0;
		int end = length;
		boolean found; // trim-able character found.
		int i;
		// Start from the beginning and find the
		// first non-trim-able character.
		found = false;
		for (i = 0; !found && i < length; i++) {
			char ch = s.charAt(i);
			found = true;
			for (int j = 0; found && j < cLength; j++) {
				if (c.charAt(j) == ch)
					found = false;
			}
		}
		// if all characters are trim-able.
		if (!found)
			return "";
		start = i - 1;
		// Start from the end and find the
		// last non-trim-able character.
		found = false;
		for (i = length - 1; !found && i >= 0; i--) {
			char ch = s.charAt(i);
			found = true;
			for (int j = 0; found && j < cLength; j++) {
				if (c.charAt(j) == ch)
					found = false;
			}
		}
		end = i + 2;
		return s.substring(start, end);
	}

	/**
	 * Turn any HTML escape entities in the string into characters and return the resulting string.
	 * 
	 * @param s
	 *            String to be unescaped.
	 * @return unescaped String.
	 * @throws NullPointerException
	 *             if s is null.
	 * 
	 * @since ostermillerutils 1.00.00
	 */
	public static String unescapeHTML(String s) {
		StringBuffer result = new StringBuffer(s.length());
		int ampInd = s.indexOf("&");
		int lastEnd = 0;
		while (ampInd >= 0) {
			int nextAmp = s.indexOf("&", ampInd + 1);
			int nextSemi = s.indexOf(";", ampInd + 1);
			if (nextSemi != -1 && (nextAmp == -1 || nextSemi < nextAmp)) {
				int value = -1;
				String escape = s.substring(ampInd + 1, nextSemi);
				try {
					if (escape.startsWith("#")) {
						value = Integer.parseInt(escape.substring(1), 10);
					} else {
						if (htmlEntities.containsKey(escape)) {
							value = ((Integer) (htmlEntities.get(escape))).intValue();
						}
					}
				} catch (NumberFormatException x) {
				}
				result.append(s.substring(lastEnd, ampInd));
				lastEnd = nextSemi + 1;
				if (value >= 0 && value <= 0xffff) {
					result.append((char) value);
				} else {
					result.append("&").append(escape).append(";");
				}
			}
			ampInd = nextAmp;
		}
		result.append(s.substring(lastEnd));
		return result.toString();
	}

	/**
	 * Returns the line end separator from the respective system property.
	 */
	public static String cr() {
		return System.getProperty("line.separator");
	}
}
