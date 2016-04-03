package org.wikidata.query.rdf.common.uri;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * GeoSPARQL URIs.
 */
public final class GeoSparql {
    /**
     * geo: namespace.
     */
    public static final String NAMESPACE = "http://www.opengis.net/ont/geosparql#";
    /**
     * geo: prefix.
     */
    public static final String PREFIX = "geo";
    /**
     * geo: namespace.
     */
    public static final Namespace NS = new NamespaceImpl("geo", "http://www.opengis.net/ont/geosparql#");
    /**
     * geo: wktLiteral.
     */
    public static final URI WKT_LITERAL;
    /**
     * geo: wktCRSLiteral.
     */
    public static final URI WKT_CRS_LITERAL;
    static {
        ValueFactoryImpl factory = new ValueFactoryImpl();
        WKT_LITERAL = factory.createURI("http://www.opengis.net/ont/geosparql#", "wktLiteral");
        WKT_CRS_LITERAL = factory.createURI("http://www.opengis.net/ont/geosparql#", "wktCRSLiteral");
    }
    private GeoSparql() {
    }
}
