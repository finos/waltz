/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package com.khartec.waltz.jobs.example;

import com.khartec.waltz.common.IOUtilities;
import com.khartec.waltz.common.SetUtilities;
import com.khartec.waltz.common.XmlUtilities;
import com.khartec.waltz.model.EntityLifecycleStatus;
import com.khartec.waltz.schema.tables.records.MeasurableCategoryRecord;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.jooq.Record3;
import org.jooq.lambda.Unchecked;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static com.khartec.waltz.common.CollectionUtilities.filter;
import static com.khartec.waltz.common.SetUtilities.map;
import static com.khartec.waltz.schema.Tables.*;
import static java.util.stream.Collectors.toSet;
import static org.jooq.lambda.tuple.Tuple.tuple;

/**
 * This class is a demonstration of how to import
 * an existing taxonomy from an external source.
 *
 * In this example we are loading a taxonomy which
 * represent items in a 'Higher Education' functional
 * domain.
 *
 * The file was downloaded from UCISA [1] and is in
 * Archimate format.
 *
 * 1 - https://www.ucisa.ac.uk/Groups/Enterprise-Architecture-Group/UK-HE-Capability-Model
 *
 */
@Service
public class HigherEducationTaxonomyImport {

    public static final String FILENAME = "taxonomies/higher_education/UCISA UK HE Capability model Final v2 ArchiMate Exchange File.xml";
    public static final XPathFactory xpathFactory = XPathFactory.newInstance();
    public static final String CATEGORY_EXTERNAL_ID = "UCISA_UK_HE_CAPABILITIES";

    private final DSLContext dsl;


    public static void main(String[] args) throws Exception {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        HigherEducationTaxonomyImport importer = ctx.getBean(HigherEducationTaxonomyImport.class);

        importer.go(FILENAME);
    }


    @Autowired
    public HigherEducationTaxonomyImport(DSLContext dsl) {
        this.dsl = dsl;
    }


    private void go(String filename) throws Exception {
        try {

            log("Reading the xml file: %s", filename);

            Document doc = readDoc(filename);

            log("File read, now parsing for capabilities and relationships");

            Set<Tuple3<String, String, String>> capabilityTuples = parseCapabilities(doc);
            Set<Tuple3<String, String, String>> relationshipTuples = parseRelationships(doc);

            log("We have %d capabilities to create and %d relationships between them",
                    capabilityTuples.size(),
                    relationshipTuples.size());


            dsl.transaction(dslCtx -> {
                DSLContext tx = dslCtx.dsl();

                log("We are now inside a tx, any uncaught exception will cause a rollback");

                log("Before we start creating data in Waltz we will need a category for this taxonomy");
                long categoryId = getOrCreateCategoryId(tx);
                log("Category id for this taxonomy is %d", categoryId);

                storeCapabilities(tx, categoryId, capabilityTuples);

                log("Forcing a rollback");
                throw new UnsupportedOperationException("BOOooooOOM");
            });

        } catch (Exception e) {
            throw new Exception("Failed to import " + filename, e);
        }
    }


    /**
     * @param tx  a jOOQ transactional context
     * @param categoryId  The measurable category id to use
     * @param capabilityTuples  Set of (id, name, desc) tuples
     */
    private void storeCapabilities(DSLContext tx,
                                   long categoryId,
                                   Set<Tuple3<String, String, String>> capabilityTuples) {

        Map<String, Tuple2<String, String>> existingExtIdToNameDescTuple = tx
                .select(MEASURABLE.EXTERNAL_ID,
                        MEASURABLE.NAME,
                        MEASURABLE.DESCRIPTION)
                .from(MEASURABLE)
                .where(MEASURABLE.MEASURABLE_CATEGORY_ID.eq(categoryId))
                .fetchMap(
                        Record3::value1,  // key is external id
                        r -> tuple(r.value2(), r.value3())); // value is tuple(name, description)

        Set<String> existingIds = existingExtIdToNameDescTuple.keySet();
        Set<String> requiredIds = map(capabilityTuples, Tuple3::v1);

        // some simple set logic gets us old, new and holdovers
        Set<String> noLongerNeededIds = SetUtilities.minus(existingIds, requiredIds);
        Set<String> idsThatAreNew = SetUtilities.minus(requiredIds, existingIds);
        Set<String> remainingIds = SetUtilities.intersection(existingIds, requiredIds);

        removeNoLongerNeederCapabilities(tx, noLongerNeededIds);
        insertNewCapabilities(tx, filter(capabilityTuples, t -> idsThatAreNew.contains(t.v1())));

    }

    private void insertNewCapabilities(DSLContext tx, Collection<Tuple3<String, String, String>> newCapabilities) {

    }


    private int removeNoLongerNeederCapabilities(DSLContext tx, Set<String> noLongerNeededIds) {
        return tx
                .update(MEASURABLE)
                .set(MEASURABLE.ENTITY_LIFECYCLE_STATUS, EntityLifecycleStatus.REMOVED.name())
                .where(MEASURABLE.ID.in(noLongerNeededIds))
                .execute();
    }


    private long getOrCreateCategoryId(DSLContext tx) {
        log("Attempting to find category id using external id: %s", CATEGORY_EXTERNAL_ID);

        return tx
            .select(MEASURABLE_CATEGORY.ID)
            .from(MEASURABLE_CATEGORY)
            .where(MEASURABLE_CATEGORY.EXTERNAL_ID.eq(CATEGORY_EXTERNAL_ID))
            .fetchOptional(MEASURABLE_CATEGORY.ID)
            .orElseGet(() -> {
                log("Could not find an existing category, therefore creating a new one");

                MeasurableCategoryRecord record = tx.newRecord(MEASURABLE_CATEGORY);
                record.setName("UK Higher Education Capabilities");
                record.setDescription("UK Higher Education Capabilities sourced from UCISA");
                record.setEditable(false);
                record.setLastUpdatedBy("admin");
                record.setRatingSchemeId(1L);

                log("Storing the new category and getting returning it's Waltz id");
                record.store();
                return record.getId();
            });
    }


    private Set<Tuple3<String, String, String>> parseRelationships(Document doc) throws XPathExpressionException {
        XPathExpression relationshipsXPath = xpathFactory
                .newXPath()
                .compile("/model/relationships/relationship[@type='Composition']");

        return XmlUtilities
                .stream((NodeList) relationshipsXPath.evaluate(doc, XPathConstants.NODESET))
                .map(Node::getAttributes)
                .map(attrs -> tuple(
                        attrs.getNamedItem("identifier").getTextContent(),
                        attrs.getNamedItem("source").getTextContent(),
                        attrs.getNamedItem("target").getTextContent()))
                .collect(toSet());
    }


    /**
     * Capabilities are recorded as model elements with a type of 'Capability'
     *
     * Each capability has an identifier, a name
     *
     * @param doc  xml document
     * @return tuple(id, name, documentation)
     * @throws XPathExpressionException
     */
    private Set<Tuple3<String, String, String>> parseCapabilities(Document doc) throws XPathExpressionException {
        XPathExpression capabilitiesXPath = xpathFactory
                .newXPath()
                .compile("/model/elements/element[@type='Capability']");

        XPathExpression nameXPath = xpathFactory.newXPath().compile("name");
        XPathExpression documentationXPath = xpathFactory.newXPath().compile("documentation");

        Function<Node, String> readNameFn = Unchecked.function(nameXPath::evaluate);
        Function<Node, String> readDocFn = Unchecked.function(documentationXPath::evaluate);

        return XmlUtilities
                .stream((NodeList) capabilitiesXPath.evaluate(doc, XPathConstants.NODESET))
                .map(node -> tuple(
                        node.getAttributes().getNamedItem("identifier").getTextContent(),
                        readNameFn.apply(node),
                        readDocFn.apply(node)))
                .collect(toSet());
    }


    private Document readDoc(String fileName) throws SAXException, IOException, ParserConfigurationException {
        return XmlUtilities
                    .createNonValidatingDocumentBuilderFactory()
                    .newDocumentBuilder()
                    .parse(IOUtilities.getFileResource(fileName).getInputStream());
    }


    private void log(String msg, Object... args) {
        System.out.printf(msg + "\n", args);
    }



}
