/**
 * Copyright 2005-2010 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.engine.http.header;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.restlet.data.CharacterSet;
import org.restlet.data.ClientInfo;
import org.restlet.data.Encoding;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Preference;

/**
 * Preference manipulation utilities.<br>
 * 
 * @author Jerome Louvel
 */
public class PreferenceUtils {
    /**
     * Formats a list of preferences with a comma separator.
     * 
     * @param prefs
     *            The list of preferences.
     * @return The formatted list of preferences.
     * @throws IOException
     */
    public static String format(List<? extends Preference<?>> prefs)
            throws IOException {
        final StringBuilder sb = new StringBuilder();

        Preference<?> pref;
        for (int i = 0; i < prefs.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            pref = prefs.get(i);
            format(pref, sb);
        }

        return sb.toString();
    }

    /**
     * Formats a preference.
     * 
     * @param pref
     *            The preference to format.
     * @param destination
     *            The appendable destination.
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public static void format(Preference pref, Appendable destination)
            throws IOException {
        destination.append(pref.getMetadata().getName());

        if (pref.getMetadata() instanceof MediaType) {
            final MediaType mediaType = (MediaType) pref.getMetadata();

            if (mediaType.getParameters() != null) {
                Parameter param;
                for (final Iterator<Parameter> iter = mediaType.getParameters()
                        .iterator(); iter.hasNext();) {
                    param = iter.next();

                    if (param.getName() != null) {
                        destination.append(';').append(param.getName());

                        if ((param.getValue() != null)
                                && (param.getValue().length() > 0)) {
                            destination.append('=').append(param.getValue());
                        }
                    }
                }
            }
        }

        if (pref.getQuality() < 1F) {
            destination.append(";q=");
            formatQuality(pref.getQuality(), destination);
        }

        if (pref.getParameters() != null) {
            Parameter param;
            for (final Iterator<Parameter> iter = pref.getParameters()
                    .iterator(); iter.hasNext();) {
                param = iter.next();

                if (param.getName() != null) {
                    destination.append(';').append(param.getName());

                    if ((param.getValue() != null)
                            && (param.getValue().length() > 0)) {
                        destination.append('=').append(param.getValue());
                    }
                }
            }
        }
    }

    /**
     * Formats a quality value.<br>
     * If the quality is invalid, an IllegalArgumentException is thrown.
     * 
     * @param quality
     *            The quality value as a float.
     * @param destination
     *            The appendable destination;
     * @throws IOException
     */
    public static void formatQuality(float quality, Appendable destination)
            throws IOException {
        if (!isQuality(quality)) {
            throw new IllegalArgumentException(
                    "Invalid quality value detected. Value must be between 0 and 1.");
        }

        // [ifndef gwt]
        final java.text.NumberFormat formatter = java.text.NumberFormat
                .getNumberInstance(java.util.Locale.US);
        formatter.setMaximumFractionDigits(2);
        destination.append(formatter.format(quality));
        // [enddef]
    }

    /**
     * Indicates if the quality value is valid.
     * 
     * @param quality
     *            The quality value.
     * @return True if the quality value is valid.
     */
    public static boolean isQuality(float quality) {
        return (quality >= 0F) && (quality <= 1F);
    }

    /**
     * Parses character set preferences from a header.
     * 
     * @param acceptCharsetHeader
     *            The header to parse.
     * @param client
     *            The client preferences to update.
     */
    @SuppressWarnings("unchecked")
    public static void parseCharacterSets(String acceptCharsetHeader,
            ClientInfo client) {
        if (acceptCharsetHeader != null) {
            // Implementation according to
            // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.2
            if (acceptCharsetHeader.length() == 0) {
                client.getAcceptedCharacterSets().add(
                        new Preference<CharacterSet>(CharacterSet.ISO_8859_1));
            } else {
                AcceptReader pr = new AcceptReader(
                        AcceptReader.TYPE_CHARACTER_SET, acceptCharsetHeader);
                pr.addValues(client.getAcceptedCharacterSets());
            }
        } else {
            client.getAcceptedCharacterSets().add(
                    new Preference(CharacterSet.ALL));
        }
    }

    /**
     * Parses encoding preferences from a header.
     * 
     * @param acceptEncodingHeader
     *            The header to parse.
     * @param preference
     *            The client preferences to update.
     */
    @SuppressWarnings("unchecked")
    public static void parseEncodings(String acceptEncodingHeader,
            ClientInfo preference) {
        if (acceptEncodingHeader != null) {
            AcceptReader pr = new AcceptReader(AcceptReader.TYPE_ENCODING,
                    acceptEncodingHeader);
            pr.addValues(preference.getAcceptedEncodings());
        } else {
            preference.getAcceptedEncodings().add(
                    new Preference(Encoding.IDENTITY));
        }
    }

    /**
     * Parses language preferences from a header.
     * 
     * @param acceptLanguageHeader
     *            The header to parse.
     * @param preference
     *            The client preferences to update.
     */
    @SuppressWarnings("unchecked")
    public static void parseLanguages(String acceptLanguageHeader,
            ClientInfo preference) {
        if (acceptLanguageHeader != null) {
            AcceptReader pr = new AcceptReader(AcceptReader.TYPE_LANGUAGE,
                    acceptLanguageHeader);
            pr.addValues(preference.getAcceptedLanguages());
        } else {
            preference.getAcceptedLanguages().add(new Preference(Language.ALL));
        }
    }

    /**
     * Parses media type preferences from a header.
     * 
     * @param acceptMediaTypeHeader
     *            The header to parse.
     * @param preference
     *            The client preferences to update.
     */
    @SuppressWarnings("unchecked")
    public static void parseMediaTypes(String acceptMediaTypeHeader,
            ClientInfo preference) {
        if (acceptMediaTypeHeader != null) {
            AcceptReader pr = new AcceptReader(AcceptReader.TYPE_MEDIA_TYPE,
                    acceptMediaTypeHeader);
            pr.addValues(preference.getAcceptedMediaTypes());
        } else {
            preference.getAcceptedMediaTypes().add(
                    new Preference(MediaType.ALL));
        }
    }

    /**
     * Parses a quality value.<br>
     * If the quality is invalid, an IllegalArgumentException is thrown.
     * 
     * @param quality
     *            The quality value as a string.
     * @return The parsed quality value as a float.
     */
    public static float parseQuality(String quality) {
        try {
            final float result = Float.valueOf(quality);

            if (isQuality(result)) {
                return result;
            }

            throw new IllegalArgumentException(
                    "Invalid quality value detected. Value must be between 0 and 1.");
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException(
                    "Invalid quality value detected. Value must be between 0 and 1.");
        }
    }

}
