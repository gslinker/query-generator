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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TermFilterGroupTest {

  @Test
  void testToString() {
    TermFilterGroup grouper = new TermFilterGroup();
    assertTrue(grouper.isEmpty());
    assertEquals("", grouper.toString());

    Term term = new Term("foo", "bar");
    grouper.addTerm(term);
    assertEquals("filter( foo:bar )", grouper.toString());

    term.setProximity(new Proximity(2));
    assertEquals("filter( foo:bar~2 )", grouper.toString());

    term.setConstantScore(new ConstantScore(3.0f));
    assertEquals("filter( foo:bar~2^=3 )", grouper.toString());

    term = new Term("foo", "bar");
    grouper.getTerms().clear();
    grouper.addTerm(term);
    grouper.setConstantScore(new ConstantScore(3.0f));
    assertEquals("filter( foo:bar )^=3", grouper.toString());
  }

  @Test
  void testPrettyPrint() {
    TermFilterGroup grouper = new TermFilterGroup();
    assertTrue(grouper.isEmpty());
    assertEquals("", grouper.toString());

    Term term = new Term("foo", "bar");
    grouper.addTerm(term);
    assertEquals("filter( foo:bar )", grouper.toString());

    String prettyQuery = grouper.prettyPrint();
    String expectedQuery = "filter(\n" + "\tfoo:bar\n" + ")";

    assertEquals(expectedQuery, prettyQuery);

    TermGroup outerGrouper = new TermGroup();
    outerGrouper.setOccur(Occur.MUST);
    outerGrouper.addGroup(grouper);
    prettyQuery = outerGrouper.prettyPrint();
    expectedQuery = "+(\n" + "\tfilter(\n" + "\t\tfoo:bar\n" + "\t)\n" + ")";
    assertEquals(expectedQuery, prettyQuery);
  }

  @Test
  void testCreation() {
    TermFilterGroup grouper = new TermFilterGroup();
    assertNotNull(grouper);

    grouper = new TermFilterGroup(null);
    assertNotNull(grouper);

    grouper = new TermFilterGroup(grouper);
    assertNotNull(grouper);

    TermGroup replicant = grouper.replicate(null);
    assertNull(replicant);

    replicant = grouper.replicate(grouper);
    assertNotNull(replicant);
  }

  @Test
  void testEqualsAndHashCode() {
    TermFilterGroup group1 = new TermFilterGroup();
    TermFilterGroup group2 = new TermFilterGroup();

    assertNotEquals(null, group1);
    assertNotEquals("Hello", group1);

    assertEquals(group1, group2);
    assertEquals(group1.hashCode(), group2.hashCode());

    group1.setHasGroupingParenthesis(false);
    assertNotEquals(group1, group2);
    assertNotEquals(group1.hashCode(), group2.hashCode());

  }
}
