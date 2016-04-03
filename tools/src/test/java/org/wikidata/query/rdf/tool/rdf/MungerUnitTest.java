package org.wikidata.query.rdf.tool.rdf;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.wikidata.query.rdf.test.StatementHelper.siteLink;
import static org.wikidata.query.rdf.test.StatementHelper.statement;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.IntegerLiteralImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.wikidata.query.rdf.common.uri.Ontology;
import org.wikidata.query.rdf.common.uri.Provenance;
import org.wikidata.query.rdf.common.uri.RDF;
import org.wikidata.query.rdf.common.uri.RDFS;
import org.wikidata.query.rdf.common.uri.SKOS;
import org.wikidata.query.rdf.common.uri.SchemaDotOrg;
import org.wikidata.query.rdf.common.uri.WikibaseUris;
import org.wikidata.query.rdf.common.uri.WikibaseUris.PropertyType;
import org.wikidata.query.rdf.test.StatementHelper;
import org.wikidata.query.rdf.tool.rdf.Munger.BadSubjectException;

import com.carrotsearch.randomizedtesting.RandomizedRunner;
import com.carrotsearch.randomizedtesting.RandomizedTest;

/**
 * Tests Munger.
 */
@RunWith(RandomizedRunner.class)
public class MungerUnitTest extends RandomizedTest {
    private final WikibaseUris uris = WikibaseUris.getURISystem();
    private final String bogus = "http://example.com/bogus";

    @Test
    public void mungesEntityDataOntoEntity() {
        entity("Q23") //
                .retain(statement("Q23", SchemaDotOrg.VERSION, new LiteralImpl("a revision number I promise")), //
                        statement("Q23", SchemaDotOrg.DATE_MODIFIED, new LiteralImpl("a date I promise"))) //
                .test();
    }

    @Test
    public void extraDataIsntModified() {
        entity("Q23") //
                .retain(statement("Q23", "P509", "Q6")) //
                .test();
    }

    @Test
    public void aItemIsRemoved() {
        entity("Q23") //
                .remove(statement("Q23", RDF.TYPE, Ontology.ITEM)) //
                .test();
    }

    @Test(expected = BadSubjectException.class)
    public void complainsAboutExtraSubjects() {
        entity("Q23") //
                .retain(statement("http://example.com/bogus", "Q23", "Q23")) //
                .test();
    }

    @Test
    public void siteLinksGoThrough() {
        entity("Q23") //
                .retain(statement(bogus, RDF.TYPE, SchemaDotOrg.ARTICLE), //
                        statement(bogus, "Q23", new LiteralImpl("Doesn't matter"))) //
                .test();
    }

    @Test
    public void extraLabelsRemoved() {
        entity("Q23") //
                .retain(statement("Q23", RDFS.LABEL, new LiteralImpl("foo", "en"))) //
                .remove(statement("Q23", SKOS.PREF_LABEL, new LiteralImpl("foo", "en")), //
                        statement("Q23", SchemaDotOrg.NAME, new LiteralImpl("foo", "en"))) //
                .test();
    }

    @Test
    public void labelsOnOthersRemoved() {
        entity("Q23") //
                .retain(statement("Q23", RDFS.LABEL, new LiteralImpl("george", "en"))) //
                .remove(statement("Q191789", RDFS.LABEL, new LiteralImpl("martha", "en"))) //
                .test();
    }

    @Test
    public void basicExpandedStatement() {
        String statementUri = uris.statement() + "Q23-ce976010-412f-637b-c687-9fd2d52dc140";
        entity("Q23") //
                // TODO can we rewrite the first statement into something
                // without the repeated property?
                .retain(statement(statementUri, uris.value() + "P509", "Q356405"), //
                        statement(statementUri, Ontology.RANK, Ontology.NORMAL_RANK), //
                        statement("Q23", "P509", statementUri)) //
                .remove(statement(statementUri, RDF.TYPE, Ontology.STATEMENT)) //
                .test();
    }

    @Test
    public void expandedStatementWithReference() {
        String statementUri = uris.statement() + "Q23-9D3713FF-7BCC-489F-9386-C7322C0AC284";
        String referenceUri = uris.reference() + "e36b7373814a0b74caa84a5fc2b1e3297060ab0f";
        entity("Q23") //
                .retain(statement("Q23", "P19", statementUri), //
                        statement(statementUri, uris.value() + "P19", "Q494413"), //
                        statement(statementUri, Ontology.RANK, Ontology.NORMAL_RANK), //
                        statement(referenceUri, uris.value() + "P854", "http://www.anb.org/articles/02/02-00332.html"), //
                        statement(statementUri, Provenance.WAS_DERIVED_FROM, referenceUri)) //
                .remove(statement(statementUri, RDF.TYPE, Ontology.STATEMENT), //
                        statement(referenceUri, RDF.TYPE, Ontology.REFERENCE)) //
                .test();
    }

    @Test
    public void expandedStatementWithQualifier() {
        String statementUri = uris.statement() + "q23-8A2F4718-6159-4E58-A8F9-6F24F5EFEC42";
        entity("Q23")
                //
                .retain(statement("Q23", "P26", statementUri), //
                        statement(statementUri, uris.value() + "P26", "Q191789"), //
                        statement(statementUri, Ontology.RANK, Ontology.NORMAL_RANK), //
                        statement(statementUri, uris.property(PropertyType.QUALIFIER) + "P580", new LiteralImpl("1759-01-06T00:00:00Z",
                                XMLSchema.DATETIME))) //
                .remove(statement(statementUri, RDF.TYPE, Ontology.STATEMENT)) //
                .test();
    }

    @Test
    public void basicExpandedValue() {
        String statementUri = uris.statement() + "q1-someuuid";
        String valueUri = uris.value() + "someotheruuid";
        entity("Q1")
                //
                .retain(statement("Q1", "P580", statementUri), //
                        statement(statementUri, uris.value() + "P580", new LiteralImpl("-13798000000-01-01T00:00:00Z",
                                XMLSchema.DATETIME)), //
                        statement(statementUri, uris.value() + "P580" + "-value", valueUri), //
                        // Currently wikibase exports the deep time values as
                        // strings, not dateTime.
                        statement(valueUri, Ontology.Time.VALUE, "-13798000000-01-01T00:00:00Z"), //
                        statement(valueUri, Ontology.Time.PRECISION, new IntegerLiteralImpl(BigInteger.valueOf(3))), //
                        statement(valueUri, Ontology.Time.TIMEZONE, new IntegerLiteralImpl(BigInteger.valueOf(0))), //
                        statement(valueUri, Ontology.Time.CALENDAR_MODEL, "Q1985727")) //
                .remove(statement(statementUri, RDF.TYPE, Ontology.STATEMENT), //
                        statement(valueUri, RDF.TYPE, Ontology.VALUE)) //
                .testWithoutShuffle();

    }

    @Test
    public void expandedValueOnQualifier() {
        String statementUri = uris.statement() + "q1-someuuid";
        String valueUri = uris.value() + "someotheruuid";
        entity("Q1")
                //
                .retain(statement("Q1", "P580", statementUri), //
                        statement(statementUri, uris.property(PropertyType.QUALIFIER) + "P580", new LiteralImpl(
                                "-13798000000-01-01T00:00:00Z", XMLSchema.DATETIME)), //
                        statement(statementUri, uris.value() + "P580" + "-value", valueUri), //
                        // Currently wikibase exports the deep time values as
                        // strings, not dateTime.
                        statement(valueUri, Ontology.Time.VALUE, "-13798000000-01-01T00:00:00Z"), //
                        statement(valueUri, Ontology.Time.PRECISION, new IntegerLiteralImpl(BigInteger.valueOf(3))), //
                        statement(valueUri, Ontology.Time.TIMEZONE, new IntegerLiteralImpl(BigInteger.valueOf(0))), //
                        statement(valueUri, Ontology.Time.CALENDAR_MODEL, "Q1985727")) //
                .remove(statement(statementUri, RDF.TYPE, Ontology.STATEMENT), //
                        statement(valueUri, RDF.TYPE, Ontology.VALUE)) //
                .testWithoutShuffle();
    }

    @Test
    public void basicExpandedValueOnReference() {
        String statementUri = uris.statement() + "q1-someuuid";
        String valueUri = uris.value() + "someotheruuid";
        String referenceUri = uris.reference() + "yetanotheruuid";
        entity("Q1")
                //
                .retain(statement("Q1", "P580", statementUri), //
                        statement(statementUri, uris.property(PropertyType.STATEMENT) + "P580", new LiteralImpl("-13798000000-01-01T00:00:00Z",
                                XMLSchema.DATETIME)), //
                        statement(statementUri, Provenance.WAS_DERIVED_FROM, referenceUri), //
                        statement(referenceUri, uris.property(PropertyType.REFERENCE_VALUE) + "P580", valueUri), //
                        // Currently wikibase exports the deep time values as
                        // strings, not dateTime.
                        statement(valueUri, Ontology.Time.VALUE, "-13798000000-01-01T00:00:00Z"), //
                        statement(valueUri, Ontology.Time.PRECISION, new IntegerLiteralImpl(BigInteger.valueOf(3))), //
                        statement(valueUri, Ontology.Time.TIMEZONE, new IntegerLiteralImpl(BigInteger.valueOf(0))), //
                        statement(valueUri, Ontology.Time.CALENDAR_MODEL, "Q1985727")) //
                .remove(statement(statementUri, RDF.TYPE, Ontology.STATEMENT), //
                        statement(referenceUri, RDF.TYPE, Ontology.REFERENCE), //
                        statement(valueUri, RDF.TYPE, Ontology.VALUE)) //
                .testWithoutShuffle();
    }

    // TODO somevalue and novalue

    // TODO badges
    @Test
    public void limitLanguagesLabel() {
        limitLanguagesTestCase(RDFS.LABEL);
    }

    @Test
    public void limitLanguagesDescription() {
        limitLanguagesTestCase(SchemaDotOrg.DESCRIPTION);
    }

    @Test
    public void limitLanguagesAlias() {
        limitLanguagesTestCase(SKOS.ALT_LABEL);
    }

    private void limitLanguagesTestCase(String predicate) {
        entity("Q23") //
                .retain(statement("Q23", predicate, new LiteralImpl("foo", "en")), //
                        statement("Q23", predicate, new LiteralImpl("foo", "de")))//
                .remove(statement("Q23", predicate, new LiteralImpl("foo", "it")), //
                        statement("Q23", predicate, new LiteralImpl("foo", "fr"))) //
                .limitLabelLanguages("en", "de") //
                .testWithoutShuffle();
    }

    @Test
    public void singleLabelModeLabel() {
        singleLabelModeTestCases(RDFS.LABEL);
    }

    @Test
    public void singleLabelModeDescription() {
        singleLabelModeTestCases(SchemaDotOrg.DESCRIPTION);
    }

    private void singleLabelModeTestCases(String predicate) {
        List<Statement> toRemove = new ArrayList<Statement>();
        Collections.addAll(toRemove, //
                statement("Q23", predicate, new LiteralImpl("foo", "de")), //
                statement("Q23", predicate, new LiteralImpl("foo", "it")), //
                statement("Q23", predicate, new LiteralImpl("foo", "fr")));
        // Extra garbage entityData information shouldn't break the single label
        // mode.
        toRemove.addAll(entity("Q44").statements);
        toRemove.addAll(entity("Q78").statements);
        // Neither should labels for other entities.
        Collections.addAll(toRemove, statement("Q2344", predicate, new LiteralImpl("sneaky", "en")));
        singleLabelModeTestCase1(predicate, toRemove);
        singleLabelModeTestCase2(predicate, toRemove);
    }

    private void singleLabelModeTestCase1(String predicate, List<Statement> toRemove) {
        entity("Q23") //
                .retain(statement("Q23", predicate, new LiteralImpl("foo", "en"))) //
                .remove(toRemove) //
                .singleLabelMode("en", "de") //
                .test();
    }

    private void singleLabelModeTestCase2(String predicate, List<Statement> toRemove) {
        List<Statement> statements = entity("Q23") //
                .remove(statement("Q23", predicate, new LiteralImpl("foo", "en"))) //
                .remove(toRemove) //
                .singleLabelMode("ja") //
                .test();
        for (Statement statement : statements) {
            // There aren't any labels if none are in the languages
            assertThat(statement.getPredicate().stringValue(), not(equalTo(RDFS.LABEL)));
        }
    }

    /**
     * Combined single label mode with limit label languages. The trouble with
     * doing both is that sometimes statements are removed twice.
     */
    @Test
    public void singleLabelAndLimitLanguage() {
        entity("Q23") //
                .retain(statement("Q23", RDFS.LABEL, new LiteralImpl("foo", "en"))) //
                .remove(statement("Q23", RDFS.LABEL, new LiteralImpl("foo", "de")), //
                        statement("Q23", RDFS.LABEL, new LiteralImpl("foo", "it")), //
                        statement("Q23", RDFS.LABEL, new LiteralImpl("foo", "fr"))) //
                .singleLabelMode("en", "de") //
                .limitLabelLanguages("en")//
                .test();
    }

    @Test
    public void skipSiteLinks() {
        entity("Q23") //
                .remove(siteLink("Q23", "http://en.wikipedia.org/wiki/George_Washington", "en", randomBoolean())) //
                .removeSiteLinks() //
                .test();
    }

    @Test
    public void formatVersions() {
        List<Statement> result = entity("Q23")
            .format("test")
            .retain(statement("Q23", RDFS.LABEL, new LiteralImpl("george", "en")))
            .remove(statement("Q23", RDF.TYPE, new LiteralImpl(Ontology.ITEM)))
            .remove(statement("Q23", uris.property(PropertyType.DIRECT) + "P1", new LiteralImpl("deleteme", "en")))
            .remove(statement("Q23", uris.property(PropertyType.DIRECT) + "P2", new LiteralImpl("modifyme", "en")))
            .retain(statement("Q23", uris.property(PropertyType.DIRECT) + "P3", new LiteralImpl("keepme", "en")))
            .testWithoutShuffle();
        Statement expected = statement("Q23", uris.property(PropertyType.DIRECT) + "P2", new LiteralImpl("test modified"));
        assertThat(result, hasItem(expected));
    }

//    @Test
 //   public void coordinateSwitch() {
 //       List<Statement> result = entity("Q23")
  //              .remove(statement("Q23", uris.property(PropertyType.DIRECT) + "P9", new LiteralImpl("POINT(1.2 3.4)", (GeoSparql.WKT_LITERAL))))
  //              .testWithoutShuffle();
  //      Statement expected = statement("Q23", uris.property(PropertyType.DIRECT) + "P9", new LiteralImpl("POINT(3.4 1.2)", GeoSparql.WKT_LITERAL));
  //      assertThat(result, hasItem(expected));
 //   }

    private Mungekin entity(String id) {
        return new Mungekin(uris, id);
    }

    private final class Mungekin {
        /**
         * Entity id.
         */
        private final String id;
        /**
         * Statements to munge.
         */
        private final List<Statement> statements;
        /**
         * Statements we expect the munger to retain.
         */
        private final List<Statement> toRetain = new ArrayList<Statement>();
        /**
         * Statements we expect the munger to remove.
         */
        private final List<Statement> toRemove = new ArrayList<Statement>();

        /**
         * Our very own Munger instance so we don't conflict with external
         * references.
         */
        private Munger munger;

        private Mungekin(WikibaseUris uris, String id) {
            this.id = id;
            munger = new Munger(uris);
            statements = StatementHelper.basicEntity(uris, id);
        }

        private Mungekin retain(Statement... xs) {
            return retain(Arrays.asList(xs));
        }

        private Mungekin retain(Collection<Statement> xs) {
            statements.addAll(xs);
            toRetain.addAll(xs);
            return this;
        }

        private Mungekin remove(Statement... xs) {
            return remove(Arrays.asList(xs));
        }

        private Mungekin remove(Collection<Statement> xs) {
            statements.addAll(xs);
            toRemove.addAll(xs);
            return this;
        }

        private Mungekin singleLabelMode(String... languages) {
            munger = munger.singleLabelMode(languages);
            return this;
        }

        private Mungekin limitLabelLanguages(String... languages) {
            munger = munger.limitLabelLanguages(languages);
            return this;
        }

        private Mungekin removeSiteLinks() {
            munger = munger.removeSiteLinks();
            return this;
        }

        private List<Statement> test() {
            Collections.shuffle(statements);
            return testWithoutShuffle();
        }

        private List<Statement> testWithoutShuffle() {
            munger.munge(id, statements);
            for (Statement x : toRetain) {
                assertThat(statements, hasItem(x));
            }
            for (Statement x : toRemove) {
                assertThat(statements, not(hasItem(x)));
            }
            return statements;
        }

        private Mungekin format(String version) {
            remove(statement(uris.entityData() + id, SchemaDotOrg.SOFTWARE_VERSION, new LiteralImpl(version)));
            munger.addFormatHandler(version, new TestFormatHandler());
            return this;
        }
    }

    private final class TestFormatHandler implements Munger.FormatHandler {

        @Override
        public Statement handle(Statement statement) {
            // Delete P1
            if (statement.getPredicate().stringValue().endsWith("P1")) {
                return null;
            }
            // Modify P2
            if (statement.getPredicate().stringValue().endsWith("P2")) {
                return new StatementImpl(statement.getSubject(), statement.getPredicate(),
                        new LiteralImpl("test modified"));
            }

            return statement;
        }
    }
}
