/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.reader;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.dita.dost.util.XMLUtils.close;
import static org.junit.Assert.*;
import static org.dita.dost.util.URLUtils.*;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.util.KeyScope;
import org.dita.dost.util.XMLUtils;
import org.w3c.dom.Document;

import org.custommonkey.xmlunit.XMLUnit;
import org.xml.sax.InputSource;

import org.dita.dost.TestUtils;
import org.junit.Test;

public class TestKeyrefReader {

    private static final File resourceDir = TestUtils.getResourceDir(TestKeyrefReader.class);
    private static final File srcDir = new File(resourceDir, "src");

    @Test
    public void testKeyrefReader() throws Exception {
        final File filename = new File(srcDir, "keyrefreader.xml");

//        final Set <String> set = new HashSet<String> ();
//        set.add("blatview");
//        set.add("blatfeference");
//        set.add("blatintro");
//        set.add("keyword");
//        set.add("escape");
//        set.add("top");
//        set.add("nested");
        final KeyrefReader keyrefreader = new KeyrefReader();
//        keyrefreader.setKeys(set);
        keyrefreader.read(filename.toURI(), readMap(filename));
        final KeyScope act= keyrefreader.getKeyDefinition();

        final Map<String, String> exp = new HashMap<String, String>();
        exp.put("blatfeference", "<topicref keys='blatview blatfeference blatintro' href='blatview.dita' navtitle='blatview' locktitle='yes' class='- map/topicref '/>");
        exp.put("blatview", "<topicref keys='blatview blatfeference blatintro' href='blatview.dita' navtitle='blatview' locktitle='yes' class='- map/topicref '/>");
        exp.put("blatintro", "<topicref keys='blatview blatfeference blatintro' href='blatview.dita' navtitle='blatview' locktitle='yes' class='- map/topicref '/>");
        exp.put("keyword", "<topicref keys='keyword' class='- map/topicref '><topicmeta class='- map/topicmeta '><keywords class='- topic/keywords '><keyword class='- topic/keyword '>keyword value</keyword></keywords></topicmeta></topicref>");
        exp.put("escape", "<topicref keys='escape' class='- map/topicref ' navtitle='&amp;&lt;&gt;&quot;&apos;'><topicmeta class='- map/topicmeta '><keywords class='- topic/keywords '><keyword class='- topic/keyword '>&amp;&lt;&gt;&quot;&apos;</keyword></keywords></topicmeta></topicref>");
        exp.put("top", "<topicref keys='top' class='- map/topicref ' navtitle='top'><topicmeta class='- map/topicmeta '><keywords class='- topic/keywords '><keyword class='- topic/keyword '>top keyword</keyword></keywords></topicmeta><topicref keys='nested' class='- map/topicref ' navtitle='nested'><topicmeta class='- map/topicmeta '><keywords class='- topic/keywords '><keyword class='- topic/keyword '>nested keyword</keyword></keywords></topicmeta></topicref></topicref>");
        exp.put("nested", "<topicref keys='nested' class='- map/topicref ' navtitle='nested'><topicmeta class='- map/topicmeta '><keywords class='- topic/keywords '><keyword class='- topic/keyword '>nested keyword</keyword></keywords></topicmeta></topicref>");
        
        TestUtils.resetXMLUnit();
        XMLUnit.setIgnoreWhitespace(true);
        assertEquals(exp.keySet(), act.keySet());
        for (Map.Entry<String, String> e: exp.entrySet()) {
            final Document ev = keyDefToDoc(e.getValue());
            final Document av = act.get(e.getKey()).element.getOwnerDocument();
            assertXMLEqual(ev, av);
        }
    }

    @Test
    public void testMergeMap() throws Exception {
        final File filename = new File(srcDir, "merged.xml");

        final KeyrefReader keyrefreader = new KeyrefReader();
        keyrefreader.read(filename.toURI(), readMap(filename));
        final KeyScope act= keyrefreader.getKeyDefinition();

        final Map<String, String> exp = new HashMap<String, String>();
        exp.put("toner-specs", "<keydef class=\"+ map/topicref mapgropup-d/keydef \" keys=\"toner-specs\" href=\"toner-type-a-specs.dita\"/>");
        exp.put("toner-handling", "<keydef class=\"+ map/topicref mapgropup-d/keydef \" keys=\"toner-handling\" href=\"toner-type-b-handling.dita\"/>");
        exp.put("toner-disposal", "<keydef class=\"+ map/topicref mapgropup-d/keydef \" keys=\"toner-disposal\" href=\"toner-type-c-disposal.dita\"/>");

        TestUtils.resetXMLUnit();
        XMLUnit.setIgnoreWhitespace(true);
        assertEquals(exp.keySet(), act.keySet());
        for (Map.Entry<String, String> e: exp.entrySet()) {
            final Document ev = keyDefToDoc(e.getValue());
            final Document av = act.get(e.getKey()).element.getOwnerDocument();
            assertXMLEqual(ev, av);
        }
    }
    
    private static Document keyDefToDoc(final String key) throws Exception {
        final InputSource inputSource = new InputSource(new StringReader(key));
        final DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        return documentBuilder.parse(inputSource);
    }

    // Oberon Technologies' tests

    @Test
    public void testSimpleKeyscope() throws DITAOTException {
        final File filename = new File(srcDir, "simpleKeyscope.ditamap");

        final KeyrefReader keyrefreader = new KeyrefReader();
        keyrefreader.read(filename.toURI(), readMap(filename));
        final KeyScope act = keyrefreader.getKeyDefinition();

        assertEquals(6, act.keySet().size());
        assertEquals("one.dita", act.get("test1").href.toString());
        assertEquals("four.dita", act.get("test2").href.toString());
        assertNull(act.get("test3"));
        assertEquals("two.dita", act.get("scope1.test2").href.toString());
        assertEquals("three.dita", act.get("scope1.test3").href.toString());
        assertEquals("two.dita", act.get("scope2.test2").href.toString());
        assertEquals("three.dita", act.get("scope2.test3").href.toString());

        final KeyScope scope1 = act.getChildScope("scope1");
        assertEquals(7, scope1.keySet().size());
        assertEquals("one.dita", scope1.get("test1").href.toString());
        assertEquals("four.dita", scope1.get("test2").href.toString());
        assertEquals("three.dita", scope1.get("test3").href.toString());
        assertEquals("two.dita", scope1.get("scope1.test2").href.toString());
        assertEquals("three.dita", scope1.get("scope1.test3").href.toString());
        assertEquals("two.dita", scope1.get("scope2.test2").href.toString());
        assertEquals("three.dita", scope1.get("scope2.test3").href.toString());

        final KeyScope scope2 = act.getChildScope("scope2");
        assertEquals(7, scope2.keySet().size());
        assertEquals("one.dita", scope1.get("test1").href.toString());
        assertEquals("four.dita", scope2.get("test2").href.toString());
        assertEquals("three.dita", scope2.get("test3").href.toString());
        assertEquals("two.dita", scope2.get("scope1.test2").href.toString());
        assertEquals("three.dita", scope2.get("scope1.test3").href.toString());
        assertEquals("two.dita", scope2.get("scope2.test2").href.toString());
        assertEquals("three.dita", scope2.get("scope2.test3").href.toString());

//        KeySpace keyspace = loadKeySpace("simpleKeyscope/simpleKeyscope.ditamap");
//        testKeyAttr(keyspace, "test1", "id", "one");
//        testKeyAttr(keyspace, "test2", "id", "four");
//        testKeyAttr(keyspace, "test3", "id", null);
//        testKeyAttr(keyspace, "scope1.test2", "id", "two");
//        testKeyAttr(keyspace, "scope1.test3", "id", "three");
//        testKeyAttr(keyspace, "scope2.test2", "id", "two");
//        testKeyAttr(keyspace, "scope2.test3", "id", "three");
//
//        KeySpace scope1 = keyspace.findChildScope("scope1");
//        assertNotNull(scope1);
//        testKeyAttr(scope1, "test1", "id", "one");
//        testKeyAttr(scope1, "test2", "id", "four");
//        testKeyAttr(scope1, "test3", "id", "three");
//        testKeyAttr(scope1, "scope1.test2", "id", "two");
//        testKeyAttr(scope1, "scope1.test3", "id", "three");
//        testKeyAttr(scope1, "scope2.test2", "id", "two");
//        testKeyAttr(scope1, "scope2.test3", "id", "three");
//
//        KeySpace scope2 = keyspace.findChildScope("scope2");
//        assertNotNull(scope2);
//        testKeyAttr(scope2, "test1", "id", "one");
//        testKeyAttr(scope2, "test2", "id", "four");
//        testKeyAttr(scope2, "test3", "id", "three");
//        testKeyAttr(scope2, "scope1.test2", "id", "two");
//        testKeyAttr(scope2, "scope1.test3", "id", "three");
//        testKeyAttr(scope2, "scope2.test2", "id", "two");
//        testKeyAttr(scope2, "scope2.test3", "id", "three");
    }

    @Test
    public void testQualifiedKeyOverride() throws DITAOTException {
        final File filename = new File(srcDir, "qualifiedKeyOverride.ditamap");

        final KeyrefReader keyrefreader = new KeyrefReader();
        keyrefreader.read(filename.toURI(), readMap(filename));
        final KeyScope act = keyrefreader.getKeyDefinition();

        assertEquals(6, act.keySet().size());
        assertEquals("one.dita", act.get("test1").href.toString());
        assertEquals("four.dita", act.get("test2").href.toString());
        assertEquals("one.dita", act.get("scope1.test1").href.toString());
        assertEquals("four.dita", act.get("scope1.test2").href.toString());
        assertEquals("two.dita", act.get("scope2.test1").href.toString());
        assertEquals("three.dita", act.get("scope2.test2").href.toString());

        final KeyScope scope1 = act.getChildScope("scope1");
        assertEquals(6, scope1.keySet().size());
        assertEquals("one.dita", scope1.get("test1").href.toString());
        assertEquals("four.dita", scope1.get("test2").href.toString());
        assertEquals("one.dita", scope1.get("scope1.test1").href.toString());
        // FIXME: Should be three.dita
        assertEquals("four.dita", scope1.get("scope1.test2").href.toString());
        assertEquals("two.dita", scope1.get("scope2.test1").href.toString());
        assertEquals("three.dita", scope1.get("scope2.test2").href.toString());

//        final KeyScope scope2 = act.getChildScope("scope2");
//        assertEquals(6, scope2.keySet().size());
//        assertEquals("one.dita", scope2.get("test1").href.toString());
//        assertEquals("four.dita", scope2.get("test2").href.toString());
//        assertEquals("two.dita", scope2.get("scope1.test1").href.toString());
//        assertEquals("three.dita", scope2.get("scope1.test2").href.toString());
//        assertEquals("two.dita", scope2.get("scope2.test1").href.toString());
//        assertEquals("three.dita", scope2.get("scope2.test2").href.toString());

//        KeySpace keyspace = loadKeySpace("qualifiedKeyOverride/map.ditamap");
//        testKeyAttr(keyspace, "test1", "id", "one");
//        testKeyAttr(keyspace, "test2", "id", "four");
//        testKeyAttr(keyspace, "scope1.test1", "id", "one");
//        testKeyAttr(keyspace, "scope1.test2", "id", "three");
//        testKeyAttr(keyspace, "scope2.test1", "id", "two");
//        testKeyAttr(keyspace, "scope2.test2", "id", "three");
//
//        KeySpace scope1 = keyspace.findChildScope("scope1");
//        assertNotNull(scope1);
//        testKeyAttr(scope1, "test1", "id", "one");
//        testKeyAttr(scope1, "test2", "id", "four");
//        testKeyAttr(scope1, "scope1.test1", "id", "one");
//        testKeyAttr(scope1, "scope1.test2", "id", "three");
//        testKeyAttr(scope1, "scope2.test1", "id", "two");
//        testKeyAttr(scope1, "scope2.test2", "id", "three");
    }

    @Test
    public void testMapWithKeyscopes() throws DITAOTException {
        final File filename = new File(srcDir, "map-with-keyscopes.ditamap");

        final KeyrefReader keyrefreader = new KeyrefReader();
        keyrefreader.read(filename.toURI(), readMap(filename));
        final KeyScope root = keyrefreader.getKeyDefinition();

        assertEquals(6, root.keySet().size());
        assertEquals(null, root.get("dita-europe.conferenceName").href);
        assertEquals("http://www.ihg.com/holidayinn/hotels/us/en/munich/muchb/hoteldetail", root.get("dita-europe.hotel").href.toString());
        assertEquals("images/holidayInn.jpg", root.get("dita-europe.hotelImage").href.toString());
        assertEquals(null, root.get("telematics.conferenceName").href);
        assertEquals("http://www.dolcemunich.com/", root.get("telematics.hotel").href.toString());
        assertEquals("images/dolce.jpg", root.get("telematics.hotelImage").href.toString());

        final KeyScope first = root.getChildScope("dita-europe");
        assertEquals(9, first.keySet().size());
        assertEquals(null, first.get("conferenceName").href);
        assertEquals("http://www.ihg.com/holidayinn/hotels/us/en/munich/muchb/hoteldetail", first.get("hotel").href.toString());
        assertEquals("images/holidayInn.jpg", first.get("hotelImage").href.toString());
        assertEquals(null, first.get("dita-europe.conferenceName").href);
        assertEquals("http://www.ihg.com/holidayinn/hotels/us/en/munich/muchb/hoteldetail", first.get("dita-europe.hotel").href.toString());
        assertEquals("images/holidayInn.jpg", first.get("dita-europe.hotelImage").href.toString());
        assertEquals(null, first.get("telematics.conferenceName").href);
        assertEquals("http://www.dolcemunich.com/", first.get("telematics.hotel").href.toString());
        assertEquals("images/dolce.jpg", first.get("telematics.hotelImage").href.toString());


        final KeyScope second = root.getChildScope("telematics");
        assertEquals(9, second.keySet().size());
        assertEquals(null, second.get("conferenceName").href);
        assertEquals("http://www.dolcemunich.com/", second.get("hotel").href.toString());
        assertEquals("images/dolce.jpg", second.get("hotelImage").href.toString());
        assertEquals(null, second.get("dita-europe.conferenceName").href);
        assertEquals("http://www.ihg.com/holidayinn/hotels/us/en/munich/muchb/hoteldetail", second.get("dita-europe.hotel").href.toString());
        assertEquals("images/holidayInn.jpg", second.get("dita-europe.hotelImage").href.toString());
        assertEquals(null, second.get("telematics.conferenceName").href);
        assertEquals("http://www.dolcemunich.com/", second.get("telematics.hotel").href.toString());
        assertEquals("images/dolce.jpg", second.get("telematics.hotelImage").href.toString());
    }

    @Test
    public void testMaprefKeyscope() throws DITAOTException {
        final File filename = new File(srcDir, "maprefKeyscope.ditamap");

        final KeyrefReader keyrefreader = new KeyrefReader();
        keyrefreader.read(filename.toURI(), readMap(filename));
        final KeyScope act = keyrefreader.getKeyDefinition();

        assertEquals(9, act.keySet().size());
        assertEquals("one.dita", act.get("scope1.key1").href.toString());
        assertEquals("two.dita", act.get("scope1.key2").href.toString());
        assertEquals("nested-one.dita", act.get("scope1.mapref.key1").href.toString());
        assertEquals("nested-three.dita", act.get("scope1.mapref.key3").href.toString());
        assertEquals("nested-one.dita", act.get("scope1.map.key1").href.toString());
        assertEquals("nested-three.dita", act.get("scope1.map.key3").href.toString());

        final KeyScope scope1 = act.getChildScope("scope1");
        assertEquals(11, scope1.keySet().size());
        assertEquals("one.dita", scope1.get("key1").href.toString());
        assertEquals("two.dita", scope1.get("key2").href.toString());
        assertEquals("one.dita", scope1.get("scope1.key1").href.toString());
        assertEquals("two.dita", scope1.get("scope1.key2").href.toString());
        assertEquals("nested-one.dita", scope1.get("scope1.mapref.key1").href.toString());
        assertEquals("nested-three.dita", scope1.get("scope1.mapref.key3").href.toString());
        assertEquals("nested-one.dita", scope1.get("scope1.map.key1").href.toString());
        assertEquals("nested-three.dita", scope1.get("scope1.map.key3").href.toString());


        final KeyScope scope3 = scope1.getChildScope("mapref");
        assertEquals(12, scope3.keySet().size());
        assertEquals("one.dita", scope3.get("key1").href.toString());
        assertEquals("two.dita", scope3.get("key2").href.toString());
        assertEquals("nested-three.dita", scope3.get("key3").href.toString());
        assertEquals("one.dita", scope3.get("scope1.key1").href.toString());
        assertEquals("two.dita", scope3.get("scope1.key2").href.toString());
        assertEquals("nested-one.dita", scope3.get("scope1.mapref.key1").href.toString());
        assertEquals("nested-three.dita", scope3.get("scope1.mapref.key3").href.toString());
        assertEquals("nested-one.dita", scope3.get("scope1.map.key1").href.toString());
        assertEquals("nested-three.dita", scope3.get("scope1.map.key3").href.toString());

        final KeyScope scope4 = scope1.getChildScope("map");
        assertEquals(12, scope4.keySet().size());
        assertEquals("one.dita", scope4.get("key1").href.toString());
        assertEquals("two.dita", scope4.get("key2").href.toString());
        assertEquals("nested-three.dita", scope4.get("key3").href.toString());
        assertEquals("one.dita", scope4.get("scope1.key1").href.toString());
        assertEquals("two.dita", scope4.get("scope1.key2").href.toString());
        assertEquals("nested-one.dita", scope4.get("scope1.mapref.key1").href.toString());
        assertEquals("nested-three.dita", scope4.get("scope1.mapref.key3").href.toString());
        assertEquals("nested-one.dita", scope4.get("scope1.map.key1").href.toString());
        assertEquals("nested-three.dita", scope4.get("scope1.map.key3").href.toString());


        final KeyScope scope2 = act.getChildScope("scope2");
        assertEquals(12, scope2.keySet().size());
        assertEquals("one.dita", scope2.get("key1").href.toString());
        assertEquals("two.dita", scope2.get("key2").href.toString());
        assertEquals("nested-three.dita", scope2.get("key3").href.toString());
        assertEquals("one.dita", scope2.get("scope1.key1").href.toString());
        assertEquals("two.dita", scope2.get("scope1.key2").href.toString());
        assertEquals("nested-one.dita", scope2.get("scope1.mapref.key1").href.toString());
        assertEquals("nested-three.dita", scope2.get("scope1.mapref.key3").href.toString());
        assertEquals("nested-one.dita", scope2.get("scope1.map.key1").href.toString());
        assertEquals("nested-three.dita", scope2.get("scope1.map.key3").href.toString());
    }

    // DITA 1.3 specificiation examples

    @Test
    public void testExample7() throws DITAOTException {
        final File filename = new File(srcDir, "example7.ditamap");

        final KeyrefReader keyrefreader = new KeyrefReader();
        keyrefreader.read(filename.toURI(), readMap(filename));
        final KeyScope root = keyrefreader.getKeyDefinition();

        assertEquals(2, root.keySet().size());
        assertNull(root.get("key-1"));
        assertEquals("topic-1.dita", root.get("scope-1.key-1").href.toString());
        assertEquals("topic-2.dita", root.get("scope-2.key-1").href.toString());

        final KeyScope scope1 = root.getChildScope("scope-1");
        assertEquals(3, scope1.keySet().size());
        assertEquals("topic-1.dita", scope1.get("key-1").href.toString());
        assertEquals("topic-1.dita", scope1.get("scope-1.key-1").href.toString());
        assertEquals("topic-2.dita", scope1.get("scope-2.key-1").href.toString());

        final KeyScope scope2 = root.getChildScope("scope-2");
        assertEquals(3, scope2.keySet().size());
        assertEquals("topic-2.dita", scope2.get("key-1").href.toString());
        assertEquals("topic-1.dita", scope2.get("scope-1.key-1").href.toString());
        assertEquals("topic-2.dita", scope2.get("scope-2.key-1").href.toString());
    }

    @Test
    public void testExample8() throws DITAOTException {
        final File filename = new File(srcDir, "example8.ditamap");

        final KeyrefReader keyrefreader = new KeyrefReader();
        keyrefreader.read(filename.toURI(), readMap(filename));
        final KeyScope root = keyrefreader.getKeyDefinition();

        log(root, "");

        final KeyScope a2 = root.getChildScope("A").getChildScope("A-2");
        assertEquals(10, a2.keySet().size());
        assertEquals("a1", a2.get("a").element.getAttribute("id"));
        assertEquals("d", a2.get("d").element.getAttribute("id"));
        // FIXME
//        assertEquals("d", a2.get("A-2.d").element.getAttribute("id"));
        assertNull(a2.get("c"));
        // FIXME
//        assertEquals("c", a2.get("A-1.c").element.getAttribute("id"));
        assertEquals("c", a2.get("A.A-1.c").element.getAttribute("id"));

        final KeyScope b = root.getChildScope("B");
        assertEquals(9, b.keySet().size());
        assertEquals("e", b.get("e").element.getAttribute("id"));
        assertEquals("a1", b.get("a").element.getAttribute("id"));
        assertEquals("a2", b.get("B.a").element.getAttribute("id"));
        assertNull(b.get("g"));
        // FIXME
//        assertEquals("g", b.get("B-2.g").element.getAttribute("id"));

    }

    /** Debug logging. */
    private void log(final KeyScope scope, final String indent) {
        System.err.println(indent + "scope: " + scope.name);
        for (final String key: scope.keySet()) {
            System.err.println(indent + " * " + key);
        }
        for (final KeyScope child: scope.childScopes.values()) {
            log(child, indent + "  ");
        }
    }

    private Document readMap(final File file) throws DITAOTException {
        InputSource in = null;
        try {
            in = new InputSource(file.toURI().toString());
            return XMLUtils.getDocumentBuilder().parse(in);
        } catch (final Exception e) {
            throw new DITAOTException("Failed to parse map: " + e.getMessage(), e);
        } finally {
            try {
                close(in);
            } catch (IOException e) {
                // NOOP
            }
        }
    }

}
