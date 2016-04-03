package org.wikidata.query.rdf.common;

import java.util.Locale;

/**
 * Representation of a coordinate point in Wikibase.
 */
public class WikibasePoint {

    /**
     * Longitude, as string.
     */
    private final String longitude;
    /**
     * Latitude, as string.
     */
    private final String latitude;
    /**
     * Globe URI, as string.
     */
    private final String globe;

    /**
     * Coordinate order options.
     * Either latitude-longitude, or longitude-latitude.
     */
    @SuppressWarnings({"javadocvariable", "visibilitymodifier"})
    public enum CoordinateOrder {
        LAT_LONG, LONG_LAT;

        /**
         * Other option.
         */
        public CoordinateOrder other;

        static {
            LAT_LONG.other = LONG_LAT;
            LONG_LAT.other = LAT_LONG;
        }
    };

    /**
     * Default coordinate order in the system.
     * FIXME: for now it's lat-long, needs to be changed to long-lat.
     */
    public static final CoordinateOrder DEFAULT_ORDER = CoordinateOrder.LAT_LONG;

    /**
     * Get longitude.
     * @return
     */
    public String getLongitude() {
        return longitude;
    }

    /**
     * Get latitude.
     * @return
     */
    public String getLatitude() {
        return latitude;
    }

    /**
     * Get globe.
     * @return
     */
    public String getGlobe() {
        return globe;
    }

    /**
     * Create point from WKT literal.
     * @param literalString
     * @param coordOrder
     */
    public WikibasePoint(String literalString, CoordinateOrder coordOrder) {
        if (literalString.charAt(0) == '<') {
            // Extended OpenGIS format: "<URI> Point(1.2 3.4)"
            int endURI = literalString.indexOf('>');
            if (endURI <= 2) {
                throw new IllegalArgumentException("Invalid format for the WKT value");
            }
            globe = literalString.substring(1, endURI);
            literalString = literalString.substring(endURI + 2);
        } else {
            globe = null;
        }

        literalString = literalString.trim();
        if (!literalString.toLowerCase(Locale.ROOT).startsWith("point(") || !literalString.endsWith(")")) {
            throw new IllegalArgumentException("Invalid format for the WKT value");
        }

        // The point format is Point(123.45 -56.78)
        String[] coords = literalString.substring(6, literalString.length() - 1).split("[\\s,]");
        if (coords.length != 2) {
            throw new IllegalArgumentException("Invalid format for the WKT value");
        }
        if (coordOrder == CoordinateOrder.LAT_LONG) {
            latitude = coords[0];
            longitude = coords[1];
        } else {
            longitude = coords[0];
            latitude = coords[1];
        }
    }

    public WikibasePoint(String literalString) {
        this(literalString, DEFAULT_ORDER);
    }

    /**
     * Create point from array of strings.
     * @param components
     * @param globe
     * @param order
     */
    public WikibasePoint(String[] components, String globe, CoordinateOrder order) {
        if (order == CoordinateOrder.LAT_LONG) {
            latitude = components[0];
            longitude = components[1];
        } else {
            longitude = components[0];
            latitude = components[1];
        }
        this.globe = globe;
    }

    public WikibasePoint(String[] components, String globe) {
        this(components, globe, DEFAULT_ORDER);
    }

    public WikibasePoint(String[] components) {
        this(components, null, DEFAULT_ORDER);
    }

    public WikibasePoint(String[] components, CoordinateOrder order) {
        this(components, null, order);
    }

    /**
     * Get string representation in WKT format.
     */
    public String toString() {
        return toOrder(DEFAULT_ORDER);
    }

    /**
     * String representation in given coordinate order.
     * @param order
     * @return
     */
    public String toOrder(CoordinateOrder order) {
        final StringBuffer buf = new StringBuffer();
        if (globe != null) {
            return null;
        }
        buf.append("POINT(");
        if (order == CoordinateOrder.LAT_LONG) {
            buf.append(latitude);
            buf.append(" ");
            buf.append(longitude);
        } else {
            buf.append(longitude);
            buf.append(" ");
            buf.append(latitude);
        }
        buf.append(")");
        return buf.toString();
    }
}
