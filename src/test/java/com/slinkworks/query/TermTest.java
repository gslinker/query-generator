/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.slinkworks.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class TermTest {

  @Test
  void testConstruction() {
    Term term = new Term("title", "pink panther");
    assertEquals("title:\"pink panther\"", term.toString());
    assertFalse(term.isBlank());

    term.setProximity(new Proximity(2));
    assertEquals("title:\"pink panther\"~2", term.toString());

    term = new Term(term);
    assertEquals("title:\"pink panther\"~2", term.toString());

    term.setConstantScore(new ConstantScore(2.0f));
    assertEquals("title:\"pink panther\"~2^=2", term.toString());

    term = new Term(term);
    assertEquals("title:\"pink panther\"~2^=2", term.toString());

    term.setBoost(new Boost(2.5f));
    assertEquals("title:\"pink panther\"~2^2.5", term.toString());

    term = new Term(term);
    assertEquals("title:\"pink panther\"~2^2.5", term.toString());

    term.setOccur(Occur.MUST);
    assertEquals("+title:\"pink panther\"~2^2.5", term.toString());

    term = new Term(term);
    assertEquals("+title:\"pink panther\"~2^2.5", term.toString());

    // -----------------------------------------------------------
    term = new Term("title", "dinosaurs");
    assertEquals("title:dinosaurs", term.toString());

    term.setProximity(new Proximity(2));
    assertEquals("title:dinosaurs~2", term.toString());

    term.setConstantScore(new ConstantScore(2.0f));
    assertEquals("title:dinosaurs~2^=2", term.toString());

    term.setBoost(new Boost(2.5f));
    assertEquals("title:dinosaurs~2^2.5", term.toString());

    term.setOccur(Occur.MUST_NOT);
    assertEquals("-title:dinosaurs~2^2.5", term.toString());

    // -----------------------------------------------------------
    term = new Term("years", "[1900 TO 1963]");
    assertEquals("years:[1900 TO 1963]", term.toString());

    // There is no proximity or fuzziness for a range query.
    term.setProximity(new Proximity(2));
    assertEquals("years:[1900 TO 1963]", term.toString());

    term.setConstantScore(new ConstantScore(2.0f));
    assertEquals("years:[1900 TO 1963]^=2", term.toString());

    term.setBoost(new Boost(2.5f));
    assertEquals("years:[1900 TO 1963]^2.5", term.toString());

    // -----------------------------------------------------------
    term = new Term("names", "{jack TO jake}");
    assertEquals("names:{jack TO jake}", term.toString());

    // There is no proximity or fuzziness for a range query.
    term.setProximity(new Proximity(2));
    assertEquals("names:{jack TO jake}", term.toString());

    term.setConstantScore(new ConstantScore(2.0f));
    assertEquals("names:{jack TO jake}^=2", term.toString());

    term.setBoost(new Boost(2.5f));
    assertEquals("names:{jack TO jake}^2.5", term.toString());

    // -----------------------------------------------------------
    term = new Term("years", "(1900 1963 1964)");
    assertEquals("years:(1900 1963 1964)", term.toString());

    term.setProximity(new Proximity(2));
    assertEquals("years:(1900 1963 1964)", term.toString());

    term.setBoost(new Boost(2.0f));
    assertEquals("years:(1900 1963 1964)", term.toString());

    term.setConstantScore(new ConstantScore(2.0f));
    assertEquals("years:(1900 1963 1964)^=2", term.toString());
  }

  @Test
  void testDefaultField() {
    Term term = new Term("pink panther");
    assertFalse(term.isBlank());
    assertEquals("\"pink panther\"", term.toString());

    term.setProximity(new Proximity(2));
    assertEquals("\"pink panther\"~2", term.toString());

    term.setConstantScore(new ConstantScore(1.5f));
    assertEquals("\"pink panther\"~2^=1.5", term.toString());

    term.setBoost(new Boost(2.5f));
    assertEquals("\"pink panther\"~2^2.5", term.toString());

    term.setOccur(Occur.MUST);
    assertEquals("+\"pink panther\"~2^2.5", term.toString());
  }

  @Test
  void getField() {
    Term term = new Term("title", "pink panther");
    assertEquals("title", term.getField());
  }

  @Test
  void getClause() {
    Term term = new Term("title", "pink panther");
    assertEquals("\"pink panther\"", term.getValue());

    term = new Term("title", "\"pink panther\"");
    assertEquals("\"pink panther\"", term.getValue());
  }

  @Test
  void getBoost() {
    Term term = new Term("title", "pink panther");
    assertNull(term.getBoost());

    term.setBoost(new Boost(2.0f));
    assertNotNull(term.getBoost());

    term.setBoost(null);
    assertNull(term.getBoost());

    term = new Term("title", "pink panther");
    term.setBoost(2.0f);
    assertEquals(2.0f, term.getBoost().getValue(), 0.0f);
  }

  @Test
  void getConstantScore() {
    Term term = new Term("title", "pink panther");
    assertNull(term.getConstantScore());

    term.setConstantScore(new ConstantScore(0.666f));
    assertNotNull(term.getConstantScore());

    term.setConstantScore(null);
    assertNull(term.getConstantScore());

    term.setConstantScore(1.4f);
    assertEquals(1.4f, term.getConstantScore().getValue(), 0.0f);
  }

  @Test
  void getProximity() {
    Term term = new Term("title", "pink panther");
    assertNull(term.getProximity());

    term.setProximity(new Proximity(1));
    assertNotNull(term.getProximity());

    term.setProximity(null);
    assertNull(term.getProximity());

    term = new Term(term);
    assertNull(term.getProximity());

    term.setProximity(2);
    assertEquals(2, term.getProximity().getValue());
  }

  @Test
  void getOccur() {
    Term term = new Term("title", "pink panther");
    assertEquals(Occur.SHOULD, term.getOccur());

    term.setOccur(Occur.MUST);
    assertEquals(Occur.MUST, term.getOccur());
  }

  @Test
  void withBoost() {
    Term term = new Term("title", "pink panther").with(new Boost(2.0f));
    assertNotNull(term.getBoost());

    term = term.withBoost(1.3f);
    assertEquals(1.3f, term.getBoost().getValue(), 0.0f);
  }

  @Test
  void testWithConstantScore() {
    Term term = new Term("title", "pink panther").with(new ConstantScore(2.0f));
    assertNotNull(term.getConstantScore());

    term = term.withConstantScore(1.3f);
    assertEquals(1.3f, term.getConstantScore().getValue(), 0.0f);
  }

  @Test
  void testWithProximity() {
    Term term = new Term("title", "pink panther").with(new Proximity(2));
    assertNotNull(term.getProximity());

    term = term.withProximity(2);
    assertEquals(2, term.getProximity().getValue());
  }

  @Test
  void testWithOccur() {
    Term term = new Term("title", "pink panther").with(Occur.MUST_NOT);
    assertEquals(Occur.MUST_NOT, term.getOccur());
  }

  @Test
  void testToString() {
    Term term = new Term("", "");
    assertTrue(term.isBlank());
    assertEquals("", term.toString());

    term = new Term(null, "");
    assertEquals("", term.toString());

    term = new Term("", null);
    assertEquals("", term.toString());

    term = new Term("title", "");
    assertEquals("", term.toString());

    term = new Term("title", null);
    assertEquals("", term.toString());

    term = new Term("", "dinosaurs");
    assertEquals("dinosaurs", term.toString());

    term = new Term(null, "dinosaurs");
    assertEquals("dinosaurs", term.toString());

    term = new Term("title", "dinosaurs");
    assertNotNull(term.toString());
    assertEquals("title:dinosaurs", term.toString());
  }

  @Test
  void testEqualsAndHashCode1() {
    Term term1 = new Term("title", "pink panther");
    Term term2 = null;

    assertEquals(term1, term1);

    assertNotEquals(term1, term2);
    assertNotEquals(term2, term1);

    assertNotEquals(null, term1);
    assertNotEquals("", term1);

    term2 = new Term("title", "pink panther");
    assertEquals(term1, term2);
    assertEquals(term2, term1);
    assertEquals(term1.hashCode(), term2.hashCode());

  }

  @Test
  void testEqualsAndHashCode2() {
    Term term1 = new Term("pink panther");
    Term term2 = null;

    assertNotEquals(term1, term2);
    assertNotEquals(term2, term1);

    term2 = new Term("pink panther");

    assertEquals(term1, term2);
    assertEquals(term2, term1);

  }

  @Test
  void testEqualsAndHashCode3() {
    Term term1 = new Term("a", "pink panther");
    Term term2 = new Term("pink panther");

    assertNotEquals(term1, term2);
    assertNotEquals(term2, term1);
    assertNotEquals(term1.hashCode(), term2.hashCode());

    term2.setField("a");

    assertEquals(term1, term2);
    assertEquals(term2, term1);
    assertEquals(term1.hashCode(), term2.hashCode());

  }

  @Test
  void testEqualsAndHashCode4() {
    Term term1 = new Term("a", "pink");
    Term term2 = new Term("a", "panther");

    assertNotEquals(term1, term2);
    assertNotEquals(term2, term1);
    assertNotEquals(term1.hashCode(), term2.hashCode());

    term2.setValue("pink");

    assertEquals(term1, term2);
    assertEquals(term2, term1);
    assertEquals(term1.hashCode(), term2.hashCode());

  }

  @Test
  void testEqualsAndHashCode5() {
    Term term1 = new Term("a", "pink panther").withBoost(1.3f);
    Term term2 = new Term("a", "pink panther");

    assertNotEquals(term1, term2);
    assertNotEquals(term2, term1);
    assertNotEquals(term1.hashCode(), term2.hashCode());

    term2.setBoost(1.3f);

    assertEquals(term1, term2);
    assertEquals(term2, term1);
    assertEquals(term1.hashCode(), term2.hashCode());

  }

  @Test
  void testEqualsAndHashCode6() {
    Term term1 = new Term("a", "pink panther").withConstantScore(1.3f);
    Term term2 = new Term("a", "pink panther");

    assertNotEquals(term1, term2);
    assertNotEquals(term2, term1);
    assertNotEquals(term1.hashCode(), term2.hashCode());

    term2.setConstantScore(1.3f);

    assertEquals(term1, term2);
    assertEquals(term2, term1);
    assertEquals(term1.hashCode(), term2.hashCode());

  }

  @Test
  void testEqualsAndHashCode7() {
    Term term1 = new Term("a", "pink panther").withProximity(2);
    Term term2 = new Term("a", "pink panther");

    assertNotEquals(term1, term2);
    assertNotEquals(term2, term1);
    assertNotEquals(term1.hashCode(), term2.hashCode());

    term2.setProximity(2);

    assertEquals(term1, term2);
    assertEquals(term2, term1);
    assertEquals(term1.hashCode(), term2.hashCode());

  }

  @Test
  void testEqualsAndHashCode8() {
    Term term1 = new Term("a", "pink panther").withOccur(Occur.MUST);
    Term term2 = new Term("a", "pink panther");

    assertNotEquals(term1, term2);
    assertNotEquals(term2, term1);
    assertNotEquals(term1.hashCode(), term2.hashCode());

    term2.setOccur(Occur.MUST);

    assertEquals(term1, term2);
    assertEquals(term2, term1);
    assertEquals(term1.hashCode(), term2.hashCode());

  }

  @Test
  void testEqualsAndHashCode9() {
    Term term1 = new Term("a", "a");
    Term term2 = new Term("a", "a");
    Term term3 = new Term("b", "b");

    List<Term> terms1 = new ArrayList<Term>();
    terms1.add(term1);
    terms1.add(term2);
    terms1.add(term3);

    Term term4 = new Term("a", "a");
    Term term5 = new Term("a", "a");
    Term term6 = new Term("b", "b");

    List<Term> terms2 = new ArrayList<Term>();
    terms2.add(term4);
    terms2.add(term5);
    terms2.add(term6);

    assertEquals(terms1, terms2);
    assertEquals(terms2, terms1);
    assertEquals(terms1.hashCode(), terms2.hashCode());

    Term term7 = new Term("a", "a");
    Term term8 = new Term("b", "b");
    Term term9 = new Term("a", "a");

    List<Term> terms3 = new ArrayList<Term>();
    terms3.add(term7);
    terms3.add(term8);
    terms3.add(term9);

    assertNotEquals(terms1, terms3);
    assertNotEquals(terms3, terms1);
    assertNotEquals(terms1.hashCode(), terms3.hashCode());


    //For coverage
    assertFalse(term1.equals("junk"));
  }

}
