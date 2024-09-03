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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;


class TermGroupTest {

  @Test
  void testConstructor() {
    TermGroup group = new TermGroup();
    assertNotNull(group);
    assertEquals("", group.toString());
    assertTrue(group.isEmpty());

    TermGroup grouper2 = new TermGroup(group);
    assertEquals("", grouper2.toString());

    group.setLabel("Root Group");
    assertEquals("Root Group", group.getLabel());

    group.addTerm(new Term("fieldName", "value"));
    assertEquals("( fieldName:value )", group.toString());

    grouper2 = new TermGroup(group);
    assertEquals("( fieldName:value )", grouper2.toString());

    group.setOccur(Occur.MUST);
    assertEquals("+( fieldName:value )", group.toString());

    grouper2 = new TermGroup(group);
    assertEquals("+( fieldName:value )", grouper2.toString());

    group.setBoost(2.3f);
    assertEquals("+( fieldName:value )^2.3", group.toString());

    grouper2 = new TermGroup(group);
    assertEquals("+( fieldName:value )^2.3", grouper2.toString());

    group.setConstantScore(4.0f);
    assertEquals("+( fieldName:value )^=4", group.toString());

    grouper2 = new TermGroup(group);
    assertEquals("+( fieldName:value )^=4", grouper2.toString());

    grouper2 = new TermGroup(group);
    assertEquals("+( fieldName:value )^=4", grouper2.toString());

    TermGroup grouper3 = new TermGroup();
    grouper3.addGroup(group);
    group.setLabel(null);
    grouper3.setLabel("Root Group");
    grouper3.setOccur(Occur.MUST_NOT);
    assertEquals("-( +( fieldName:value )^=4 )", grouper3.toString());

    grouper2 = new TermGroup(grouper3);
    assertEquals("-( +( fieldName:value )^=4 )", grouper2.toString());
  }

  @Test
  void testNestedGroups_Simple_01() {
    TermGroup group =
        new TermGroup().with(Occur.MUST).withBoost(1.4f);
    group.addTerm(new Term("foo", "bar").withProximity(1));

    String query = group.toString();
    assertEquals("+( foo:bar~1 )^1.4", query);
  }

  @Test
  void testNestedGroups_Simple_02() {
    TermGroup group = new TermGroup().withConstantScore(5.0f);
    group.addTerm(new Term("foo", "bar").withProximity(1));

    String query = group.toString();
    assertEquals("( foo:bar~1 )^=5", query);
  }

  @Test
  void testNestedGroupsComplex_01() {
    TermGroup group = new TermGroup();
    TermGroup cdGrouper = group.addGroup();
    TermGroup recordsGroup = group.addGroup();

    cdGrouper.addTerm(new Term("cd", "back in black"));
    cdGrouper.addTerm(new Term("cd", "point of no return"));
    cdGrouper.addTerm(new Term("cd", "night at the opera"));
    cdGrouper.setBoost(0.3f);

    recordsGroup.addTerm(new Term("record", "destroyer"));
    recordsGroup.addTerm(new Term("record", "the grand illusion"));
    recordsGroup.setBoost(0.5f);

    String query = group.prettyPrint();
    String expected =
        "(\n"
            + "\t(\n"
            + "\t\tcd:\"back in black\"\n"
            + "\t\tcd:\"point of no return\"\n"
            + "\t\tcd:\"night at the opera\"\n"
            + "\t)^0.3\n"
            + "\t(\n"
            + "\t\trecord:destroyer\n"
            + "\t\trecord:\"the grand illusion\"\n"
            + "\t)^0.5\n"
            + ")";
    assertEquals(expected, query);
  }

  @Test
  void testNestedGroups_01() {
    TermGroup group = new TermGroup();
    TermGroup subGroupA = group.addGroup();
    TermGroup subGroupB = group.addGroup();

    subGroupA.addTerm(new Term("FirstName", "Geoffrey").with(Occur.MUST));
    subGroupB.addTerm(new Term("LastName", "Slinker").with(Occur.MUST));

    assertEquals("( ( +FirstName:Geoffrey ) ( +LastName:Slinker ) )", group.toString());

    List<TermGroup> subGroups = group.getGroups();
    assertEquals(2, subGroups.size());
    assertEquals("( +FirstName:Geoffrey )", subGroups.get(0).toString());
    assertEquals("( +LastName:Slinker )", subGroups.get(1).toString());

    group = new TermGroup();
    subGroupA = new TermGroup();
    subGroupB = new TermGroup();

    TermGroup addedGroup = group.addGroup(subGroupA);
    assertEquals(subGroupA, addedGroup);

    addedGroup = group.addGroup(subGroupB);
    assertEquals(subGroupB, addedGroup);

    addedGroup = group.addGroup(null);
    assertNull(addedGroup);

    group = new TermGroup();
    subGroupA = new TermGroup();
    subGroupB = null;
    group.addGroup(subGroupA);
    group.addGroup(subGroupA);
    group.addGroup(subGroupA);
    group.addGroup(subGroupB);
    group.addGroup(subGroupB);
    assertEquals(1, group.getGroups().size());

    // ---------------------------------------------
    // Move group by adding it to another group
    group = new TermGroup();
    subGroupA = new TermGroup();
    subGroupB = new TermGroup();

    subGroupA.addTerm(new Term("FirstName", "Geoffrey"));
    subGroupB.addTerm(new Term("LastName", "Slinker"));

    group.addGroup(subGroupA);
    subGroupA.addGroup(subGroupB);

    assertEquals(1, group.getGroups().size());
    assertEquals(1, subGroupA.getGroups().size());
    assertEquals(0, subGroupB.getGroups().size());
    assertEquals("( ( FirstName:Geoffrey ( LastName:Slinker ) ) )", group.toString());
    assertEquals(group, subGroupA.getParentGroup());
    assertEquals(subGroupA, subGroupB.getParentGroup());
    assertEquals(group, subGroupA.getRootGroup());
    assertEquals(group, subGroupB.getRootGroup());

    // Now move B up.
    group.addGroup(subGroupB);
    assertEquals(2, group.getGroups().size());
    assertEquals(0, subGroupA.getGroups().size());
    assertEquals(0, subGroupB.getGroups().size());
    assertEquals("( ( FirstName:Geoffrey ) ( LastName:Slinker ) )", group.toString());
    assertEquals(group, subGroupA.getParentGroup());
    assertEquals(group, subGroupB.getParentGroup());
    // ---------------------------------------------

  }

  @Test
  void testNestedGroups_02() {
    TermGroup group = new TermGroup();
    TermGroup groupLevel1 = group.addGroup().with(Occur.MUST);
    TermGroup groupLevel2 = groupLevel1.addGroup();
    TermGroup groupLevel3 = groupLevel2.addGroup().with(Occur.MUST);

    String queryString = group.toString();
    String expectedString = "";
    assertEquals(expectedString, queryString);

    groupLevel3.addTerm(new Term("FirstName", "Geoffrey"));
    queryString = group.toString();
    expectedString = "( +( ( +( FirstName:Geoffrey ) ) ) )";
    assertEquals(expectedString, queryString);

    // add empty group to groupLevel3
    TermGroup groupLevel4 = groupLevel3.addGroup();
    queryString = group.toString();
    expectedString = "( +( ( +( FirstName:Geoffrey ) ) ) )";
    assertEquals(expectedString, queryString);

    // Now add a query term the previously empty group
    groupLevel4.addTerm(new Term("LastName", "Slinker"));
    queryString = group.toString();
    expectedString = "( +( ( +( FirstName:Geoffrey ( LastName:Slinker ) ) ) ) )";
    assertEquals(expectedString, queryString);

    // add empty groups to groupLevel4
    TermGroup groupLevel5 = groupLevel4.addGroup().with(Occur.MUST);
    TermGroup groupLevel6 = groupLevel5.addGroup().with(Occur.MUST_NOT);
    TermGroup groupLevel7 = groupLevel6.addGroup().with(Occur.MUST);
    queryString = group.toString();
    expectedString = "( +( ( +( FirstName:Geoffrey ( LastName:Slinker ) ) ) ) )";
    assertEquals(expectedString, queryString);

    // Add query term to groupLevel7
    groupLevel7.addTerm(new Term("BirthYear", "1876"));
    queryString = group.toString();
    expectedString =
        "( +( ( +( FirstName:Geoffrey ( LastName:Slinker +( -( +( BirthYear:1876 ) ) ) ) ) ) ) )";
    assertEquals(expectedString, queryString);
  }

  @Test
  void testMultipleQueryTerms() {
    TermGroup group = new TermGroup();
    group.addTerm(new Term("FirstName", "Geoffrey").with(new ConstantScore(2.0f)));
    group.addTerm(new Term("LastName", "Slinker").with(new Boost(3.0f)));
    group.setOccur(Occur.MUST);

    assertEquals("+( FirstName:Geoffrey^=2 LastName:Slinker^3 )", group.toString());

    List<Term> terms = group.getTerms();
    assertEquals(2, terms.size());
    assertEquals("FirstName:Geoffrey^=2", terms.get(0).toString());
    assertEquals("LastName:Slinker^3", terms.get(1).toString());
  }

  @Test
  void testOccur() {
    TermGroup group = new TermGroup().with(Occur.MUST);
    group.addTerm(new Term("FirstName", "Geoffrey"));
    assertEquals(Occur.MUST, group.getOccur());
    assertEquals("+( FirstName:Geoffrey )", group.toString());

    group.setOccur(Occur.SHOULD);
    assertEquals(Occur.SHOULD, group.getOccur());
    assertEquals("( FirstName:Geoffrey )", group.toString());

    group.setOccur(Occur.MUST_NOT);
    assertEquals(Occur.MUST_NOT, group.getOccur());
    assertEquals("-( FirstName:Geoffrey )", group.toString());

    group.setOccur(null);
    assertNull(group.getOccur());
    assertEquals("( FirstName:Geoffrey )", group.toString());

    group.setHasGroupingParenthesis(false);
    assertEquals(Occur.SHOULD, group.getOccur());
    assertEquals("FirstName:Geoffrey", group.toString());

    group.setOccur(Occur.SHOULD);
    assertEquals(Occur.SHOULD, group.getOccur());
    assertEquals("FirstName:Geoffrey", group.toString());

    group.setOccur(Occur.MUST);
    assertEquals(Occur.SHOULD, group.getOccur());
    assertEquals("FirstName:Geoffrey", group.toString());

    group.setOccur(null);
    assertEquals(Occur.SHOULD, group.getOccur());
    assertEquals("FirstName:Geoffrey", group.toString());
  }

  @Test
  void testConstantScore() {
    TermGroup group = new TermGroup().with(new ConstantScore(2.0f));
    assertEquals(2.0f, group.getConstantScore().getValue(), 0.0f);

    group.setBoost(1.0f);
    group.setConstantScore(3.0f);
    assertEquals(3.0f, group.getConstantScore().getValue(), 0.0f);
  }

  @Test
  void testBoost() {
    TermGroup group = new TermGroup().with(new Boost(2.0f));
    assertEquals(2.0f, group.getBoost().getValue(), 0.0f);

    group.setConstantScore(3.0f);
    group.setBoost(1.0f);

    assertEquals(1.0f, group.getBoost().getValue(), 0.0f);
  }

  @Test
  void testGroupingParenthesis() {
    TermGroup group = new TermGroup();
    assertTrue(group.getHasGroupingParenthesis());

    group.addTerm(new Term("FirstName", "Geoffrey"));
    assertEquals("( FirstName:Geoffrey )", group.toString());

    group.setHasGroupingParenthesis(false);
    assertEquals("FirstName:Geoffrey", group.toString());
  }

  @Test
  void testIsBlank() {
    TermGroup group = new TermGroup();
    TermGroup subGrouper = group.addGroup();
    subGrouper = subGrouper.addGroup();
    subGrouper = subGrouper.addGroup();
    assertTrue(group.isEmpty());

    subGrouper.addTerm(new Term("FirstName", "Geoffrey"));
    assertFalse(group.isEmpty());
    assertEquals("( ( ( ( FirstName:Geoffrey ) ) ) )", group.toString());
  }

  @Test
  void testPrettyPrint01() {
    TermGroup group = new TermGroup();
    assertTrue(group.isEmpty());
    assertEquals("", group.toString());

    Term term = new Term("foo", "bar");
    group.addTerm(term);
    assertEquals("( foo:bar )", group.toString());

    String prettyQuery = group.prettyPrint();
    String expectedQuery = "(\n" + "\tfoo:bar\n" + ")";

    assertEquals(expectedQuery, prettyQuery);

    TermGroup outerGrouper = new TermGroup();
    outerGrouper.setOccur(Occur.MUST);
    outerGrouper.addGroup(group);
    prettyQuery = outerGrouper.prettyPrint();
    expectedQuery = "+(\n" + "\t(\n" + "\t\tfoo:bar\n" + "\t)\n" + ")";
    assertEquals(expectedQuery, prettyQuery);
  }

  @Test
  void testPrettyPrint02() {
    TermGroup group = new TermGroup();
    TermGroup birthYears = group.addGroup();
    birthYears.addTerm(new Term("BirthYear", "1856").with(new ConstantScore(1.0f)));

    // Intentionally empty group
    TermGroup firstNames = group.addGroup();
    assertNotNull(firstNames);
    assertTrue(firstNames.isEmpty());

    TermGroup lastNames = group.addGroup();
    assertNotNull(lastNames);
    assertTrue(lastNames.isEmpty());

    // Intentionally empty Term
    Term emptyTerm = new Term("", "");
    assertTrue(emptyTerm.isBlank());
    birthYears.addTerm(emptyTerm);

    Term anotherEmptyTerm = new Term("", "");
    assertTrue(anotherEmptyTerm.isBlank());
    birthYears.addTerm(anotherEmptyTerm);

    String prettyQuery = group.prettyPrint(true, "", " ", TermGroup.NEW_LINE_SEPARATOR_STRING);
    assertTrue(StringUtils.isNotBlank(prettyQuery));

    String expectedQuery = "(\n" +
        " (\n" +
        "  BirthYear:1856^=1\n" +
        " )\n" +
        ")";
    assertEquals(expectedQuery, prettyQuery);

  }

  @Test
  void testPrettyPrintFull_1() {
    TermGroup group = new TermGroup();
    String prettyQuery = group.prettyPrint(true, "", " ", TermGroup.NEW_LINE_SEPARATOR_STRING);
    String expectedQuery = "";
    assertEquals(expectedQuery, prettyQuery);

    group.setLabel("Root Group");
    group.setOccur(Occur.MUST);

    TermGroup firstNames = group.addGroup();
    firstNames.setLabel("First Names");
    firstNames.setOccur(Occur.MUST);
    firstNames.addTerm(
        new Term("FirstName", "Geoffrey").with(new ConstantScore(1.0f)));
    firstNames.addTerm(new Term("FirstName", "Jeff").with(new ConstantScore(1.0f)));
    firstNames.setBoost(0.5f);
    assertEquals("+( FirstName:Geoffrey^=1 FirstName:Jeff^=1 )^0.5", firstNames.toString());

    TermGroup lastNames = group.addGroup();
    lastNames.setLabel("Last Names");
    lastNames.setOccur(Occur.MUST);
    lastNames.addTerm(new Term("LastName", "Slinker").with(new ConstantScore(1.0f)));
    lastNames.addTerm(
        new Term("LastName", "Schlenker").with(new ConstantScore(1.0f)));
    lastNames.setBoost(0.5f);
    assertEquals("+( LastName:Slinker^=1 LastName:Schlenker^=1 )^0.5", lastNames.toString());

    TermGroup birthYears = group.addGroup();
    // Intentionally do not set label
    birthYears.setOccur(Occur.MUST);
    TermGroup birthYears1 = birthYears.addGroup();
    birthYears1.setHasGroupingParenthesis(false);
    birthYears1.addTerm(
        new Term("BirthYear", "(1860 1861)").with(new ConstantScore(1.0f)));
    TermGroup birthYears2 = birthYears.addGroup();
    birthYears2.setHasGroupingParenthesis(false);
    birthYears2.addTerm(new Term("BirthYear", "1878").with(new ConstantScore(1.0f)));
    assertEquals("+( BirthYear:(1860 1861)^=1 BirthYear:1878^=1 )", birthYears.toString());

    prettyQuery = group.prettyPrint(true, "", " ", TermGroup.NEW_LINE_SEPARATOR_STRING);
    expectedQuery =
        "/* Root Group */\n"
            + "+(\n"
            + " /* First Names */\n"
            + " +(\n"
            + "  FirstName:Geoffrey^=1\n"
            + "  FirstName:Jeff^=1\n"
            + " )^0.5\n"
            + " /* Last Names */\n"
            + " +(\n"
            + "  LastName:Slinker^=1\n"
            + "  LastName:Schlenker^=1\n"
            + " )^0.5\n"
            + " +(\n"
            + "  BirthYear:(1860 1861)^=1\n"
            + "  BirthYear:1878^=1\n"
            + " )\n"
            + ")";
    assertEquals(expectedQuery, prettyQuery);

    String query = group.toString();
    expectedQuery =
        "+( +( FirstName:Geoffrey^=1 FirstName:Jeff^=1 )^0.5 +( LastName:Slinker^=1 LastName:Schlenker^=1 )^0.5 +( BirthYear:(1860 1861)^=1 BirthYear:1878^=1 ) )";
    assertEquals(expectedQuery, query);
  }

  @Test
  void testPrettyPrintFull_2() {
    TermGroup group = new TermGroup();
    String prettyQuery = group.prettyPrint(true, "", " ", TermGroup.NEW_LINE_SEPARATOR_STRING);
    String expectedQuery = "";
    assertEquals(expectedQuery, prettyQuery);

    group.setOccur(Occur.MUST);

    TermGroup firstNames = group.addGroup();
    firstNames.setLabel("First Names");
    firstNames.setOccur(null);
    firstNames.addTerm(
        new Term("FirstName", "Geoffrey").with(new ConstantScore(1.0f)));
    firstNames.addTerm(new Term("FirstName", "Jeff").with(new ConstantScore(1.0f)));
    firstNames.setBoost(0.5f);
    assertEquals("( FirstName:Geoffrey^=1 FirstName:Jeff^=1 )^0.5", firstNames.toString());

    TermGroup lastNames = group.addGroup();
    lastNames.setOccur(Occur.MUST);
    lastNames.addTerm(new Term("LastName", "Slinker").with(new ConstantScore(1.0f)));
    lastNames.setConstantScore(1.0f);
    assertEquals("+( LastName:Slinker^=1 )^=1", lastNames.toString());

    TermGroup birthYears = group.addGroup();
    birthYears.setOccur(Occur.MUST);
    TermGroup birthYears1 = birthYears.addGroup();
    birthYears1.setHasGroupingParenthesis(false);
    birthYears1.addTerm(
        new Term("BirthYear", "(1860 1861)").with(new ConstantScore(1.0f)));

    prettyQuery = group.prettyPrint(true, "", " ", TermGroup.NEW_LINE_SEPARATOR_STRING);
    expectedQuery =
        "+(\n"
            + " /* First Names */\n"
            + " (\n"
            + "  FirstName:Geoffrey^=1\n"
            + "  FirstName:Jeff^=1\n"
            + " )^0.5\n"
            + " +(\n"
            + "  LastName:Slinker^=1\n"
            + " )^=1\n"
            + " +(\n"
            + "  BirthYear:(1860 1861)^=1\n"
            + " )\n"
            + ")";
    assertEquals(expectedQuery, prettyQuery);

    String query = group.toString();
    expectedQuery =
        "+( ( FirstName:Geoffrey^=1 FirstName:Jeff^=1 )^0.5 +( LastName:Slinker^=1 )^=1 +( BirthYear:(1860 1861)^=1 ) )";
    assertEquals(expectedQuery, query);
  }

  @Test
  void testPrettyPrintFull_3() {
    TermGroup group = new TermGroup();
    String prettyQuery = group.prettyPrint(true, "", " ", TermGroup.NEW_LINE_SEPARATOR_STRING);
    String expectedQuery = "";
    assertEquals(expectedQuery, prettyQuery);

    group.setOccur(Occur.MUST);

    TermGroup firstNames = group.addGroup();
    firstNames.setLabel("First Names");
    firstNames.setOccur(null);
    firstNames.addTerm(
        new Term("FirstName", "Geoffrey").with(new ConstantScore(1.0f)));
    firstNames.setBoost(0.5f);
    assertEquals("( FirstName:Geoffrey^=1 )^0.5", firstNames.toString());

    TermGroup birthYears = group.addGroup();
    birthYears.setOccur(Occur.MUST);
    birthYears.addTerm(new Term("BirthYear", "1856").with(new ConstantScore(1.0f)));
    TermGroup birthYears1 = birthYears.addGroup();
    birthYears1.setHasGroupingParenthesis(false);
    birthYears1.addTerm(
        new Term("BirthYear", "(1860 1861)").with(new ConstantScore(1.0f)));
    TermGroup birthYears2 = birthYears.addGroup();
    birthYears2.setHasGroupingParenthesis(false);
    birthYears2.addTerm(new Term("BirthYear", "1878").with(new ConstantScore(1.0f)));
    assertEquals(
        "+( BirthYear:1856^=1 BirthYear:(1860 1861)^=1 BirthYear:1878^=1 )", birthYears.toString());

    prettyQuery = group.prettyPrint(true, "", " ", TermGroup.NEW_LINE_SEPARATOR_STRING);
    expectedQuery =
        "+(\n" +
            " /* First Names */\n" +
            " (\n" +
            "  FirstName:Geoffrey^=1\n" +
            " )^0.5\n" +
            " +(\n" +
            "  BirthYear:1856^=1\n" +
            "  BirthYear:(1860 1861)^=1\n" +
            "  BirthYear:1878^=1\n" +
            " )\n" +
            ")";
    assertEquals(expectedQuery, prettyQuery);

    String query = group.toString();
    expectedQuery =
        "+( ( FirstName:Geoffrey^=1 )^0.5 +( BirthYear:1856^=1 BirthYear:(1860 1861)^=1 BirthYear:1878^=1 ) )";
    assertEquals(expectedQuery, query);
  }

  @Test
  void testToString() {
    TermGroup group = new TermGroup();
    TermGroup birthYears = group.addGroup();
    birthYears.addTerm(new Term("BirthYear", "1856").with(new ConstantScore(1.0f)));

    String expectedQuery = "( ( BirthYear:1856^=1 ) )";
    String query = group.toString();
    assertEquals(expectedQuery, query);

    // Intentionally empty group
    TermGroup firstNames = group.addGroup();
    assertNotNull(firstNames);
    assertTrue(firstNames.isEmpty());

    query = group.toString();
    assertEquals(expectedQuery, query);

    TermGroup lastNames = group.addGroup();
    assertNotNull(lastNames);
    assertTrue(lastNames.isEmpty());

    query = group.toString();
    assertEquals(expectedQuery, query);

    // Intentionally empty Term
    Term emptyTerm = new Term("", "");
    assertTrue(emptyTerm.isBlank());
    birthYears.addTerm(emptyTerm);

    query = group.toString();
    assertEquals(expectedQuery, query);

    Term anotherEmptyTerm = new Term("", "");
    assertTrue(anotherEmptyTerm.isBlank());
    birthYears.addTerm(anotherEmptyTerm);

    query = group.toString();
    assertEquals(expectedQuery, query);

    String prettyQuery = group.prettyPrint(true, "", " ", TermGroup.NEW_LINE_SEPARATOR_STRING);
    assertTrue(StringUtils.isNotBlank(prettyQuery));

  }

  @Test
  void testToString2() {
    TermGroup root = new TermGroup();
    root.setHasGroupingParenthesis(false);
    TermGroup parent = new TermGroup();
    root.addGroup(parent);
    TermGroup groupA = parent.addGroup(new TermGroup());
    TermGroup groupB = parent.addGroup(new TermGroup());
    TermGroup groupC = parent.addGroup(new TermGroup());
    groupC.setHasGroupingParenthesis(false);
    TermGroup groupD = parent.addGroup(new TermGroup()); //Empty
    TermGroup groupE = parent.addGroup(new TermGroup());

    TermGroup groupAChild = groupA.addGroup(new TermGroup());

    //We have two groups.
    Term ta = new Term("A").with(Occur.SHOULD);
    groupA.addTerm(ta);

    Term tb = new Term("B").with(Occur.SHOULD);
    groupB.addTerm(tb);

    Term tac = new Term("AC").with(Occur.SHOULD);
    groupAChild.addTerm(tb);

    groupC.addTerm(new Term("C1").with(Occur.SHOULD));
    groupC.addTerm(new Term("C2").with(Occur.SHOULD));

    groupE.addTerm(new Term("D1").with(Occur.SHOULD));
    groupE.addTerm(new Term("D2").with(Occur.SHOULD));

    String results = root.toString();
    assertEquals("( ( A ( B ) ) ( B ) C1 C2 ( D1 D2 ) )", results);
  }


  @Test
  void testToString3() {
    TermGroup group = new TermGroup().with(Occur.MUST);

    TermGroup favoriteStyx = group.addGroup().withBoost(0.3f);
    TermGroup favoriteQueen = group.addGroup().withBoost(0.3f);
    TermGroup favoriteVanHalen = group.addGroup().withBoost(0.3f);

    favoriteStyx.addTerm(new Term("title", "Grand Illusion").with(Occur.SHOULD).withProximity(1));
    favoriteStyx.addTerm(new Term("title", "Paradise Theatre").with(Occur.SHOULD).withProximity(1));

    favoriteQueen.addTerm(
        new Term("title", "Night At The Opera").with(Occur.SHOULD).withProximity(1));
    favoriteQueen.addTerm(
        new Term("title", "News Of The World").with(Occur.SHOULD).withProximity(1));

    favoriteVanHalen.addTerm(new Term("title", "Van Halen").with(Occur.SHOULD).withProximity(1));
    favoriteVanHalen.addTerm(new Term("title", "1984").with(Occur.SHOULD).withProximity(1));

    String query = group.toString();
    String expected = "+( ( title:\"Grand Illusion\"~1 title:\"Paradise Theatre\"~1 )^0.3 ( title:\"Night At The Opera\"~1 title:\"News Of The World\"~1 )^0.3 ( title:\"Van Halen\"~1 title:1984~1 )^0.3 )";

    assertEquals(expected, query);

    String prettyQUery = group.prettyPrint();
    expected = "+(\n"
        + "\t(\n"
        + "\t\ttitle:\"Grand Illusion\"~1\n"
        + "\t\ttitle:\"Paradise Theatre\"~1\n"
        + "\t)^0.3\n"
        + "\t(\n"
        + "\t\ttitle:\"Night At The Opera\"~1\n"
        + "\t\ttitle:\"News Of The World\"~1\n"
        + "\t)^0.3\n"
        + "\t(\n"
        + "\t\ttitle:\"Van Halen\"~1\n"
        + "\t\ttitle:1984~1\n"
        + "\t)^0.3\n"
        + ")";
    assertEquals(expected, prettyQUery);


  }


  @Test
  void testOuterOccur() {
    TermGroup group = new TermGroup();
    TermGroup birthYears = group.addGroup();
    birthYears.addTerm(new Term("BirthYear", "1856").with(new ConstantScore(1.0f)));

    String queryString = group.toString();
    String expectedString = "( ( BirthYear:1856^=1 ) )";
    assertEquals(expectedString, queryString);

    birthYears.setOuterOccur(Occur.MUST);
    queryString = group.toString();
    expectedString = "+( ( BirthYear:1856^=1 ) )";
    assertEquals(expectedString, queryString);

    group.setHasGroupingParenthesis(false);
    queryString = group.toString();
    expectedString = "( BirthYear:1856^=1 )";
    assertEquals(expectedString, queryString);

    // setting the outer occur on the top most group will do nothing.
    group.setHasGroupingParenthesis(true);
    group.setOuterOccur(Occur.MUST_NOT);
    queryString = group.toString();
    expectedString = "( ( BirthYear:1856^=1 ) )";
    assertEquals(expectedString, queryString);

    birthYears.setOuterOccur(Occur.MUST_NOT);
    queryString = group.toString();
    expectedString = "-( ( BirthYear:1856^=1 ) )";
    assertEquals(expectedString, queryString);

    // ---------------------------------------------
    TermGroup level1 = new TermGroup();
    TermGroup level2 = level1.addGroup();
    TermGroup level3 = level2.addGroup();

    level2.setHasGroupingParenthesis(false);
    level3.addTerm(new Term("foo", "bar").with(new Proximity(2)));

    queryString = level1.toString();
    expectedString = "( ( foo:bar~2 ) )";
    assertEquals(expectedString, queryString);

    level3.setOuterOccur(Occur.MUST_NOT);
    queryString = level1.toString();
    expectedString = "-( ( foo:bar~2 ) )";
    assertEquals(expectedString, queryString);
  }

  @Test
  void testGroupLabels() {
    TermGroup group = new TermGroup();
    group.setLabel("ROOT");
    assertTrue(group.hasLabel("ROOT"));
    assertFalse(group.hasLabel(null));
    assertFalse(group.hasLabel(""));

    TermGroup birthYears = group.addGroup();
    birthYears.addTerm(new Term("BirthYear", "1856").with(new ConstantScore(1.0f)));
    birthYears.setLabel("BIRTH");

    List<TermGroup> foundGroups = group.findByLabel("BIRTH");
    assertEquals(1, foundGroups.size());
    assertEquals(foundGroups.get(0), birthYears);

    group = new TermGroup().withLabel("PARENT");
    assertTrue(group.hasLabel("PARENT"));
  }

  @Test
  void testIsValid() {
    TermGroup group = new TermGroup();
    assertFalse(group.isValid());
    TermGroup anotherGrouper = group.addGroup();
    assertFalse(anotherGrouper.isValid());
    assertFalse(group.isValid());

    anotherGrouper.addTerm(new Term("foo", "bar").with(new Boost(1.0f)));
    assertTrue(anotherGrouper.isValid());
    assertTrue(group.isValid());
  }

  @Test
  void testWrapWith_01() {
    TermGroup level1 = new TermGroup();
    TermGroup level2 = level1.addGroup();
    TermGroup level3 = level2.addGroup();
    TermGroup level4a = level3.addGroup();
    TermGroup level4b = level3.addGroup();
    TermGroup level4c = level3.addGroup();

    level4a.addTerm(new Term("foo", "bar").with(new Boost(1.0f)));
    level4b.addTerm(new Term("chocolate", "bar").with(new Proximity(2)));
    level4c.addTerm(new Term("potato", "bar").with(new Proximity(2)));

    String queryString = level1.toString();
    String expectedString = "( ( ( ( foo:bar^1 ) ( chocolate:bar~2 ) ( potato:bar~2 ) ) ) )";
    assertEquals(expectedString, queryString);

    TermGroup wrapperGrouper = new TermGroup();
    wrapperGrouper.setOccur(Occur.MUST_NOT);
    level4b.wrapWith(wrapperGrouper);

    queryString = level1.toString();
    expectedString = "( ( ( ( foo:bar^1 ) -( ( chocolate:bar~2 ) ) ( potato:bar~2 ) ) ) )";
    assertEquals(expectedString, queryString);
  }

  @Test
  void testWrapWith_02() {
    TermGroup level1 = new TermGroup();
    level1.addTerm(new Term("foo", "bar").with(new Proximity(2)));

    String queryString = level1.toString();
    String expectedString = "( foo:bar~2 )";
    assertEquals(expectedString, queryString);

    TermGroup wrapperGrouper = new TermGroup();
    wrapperGrouper.setOccur(Occur.MUST_NOT);
    level1.wrapWith(wrapperGrouper);
    assertEquals(1, wrapperGrouper.getGroups().size());
    assertEquals(0, level1.getGroups().size());
    assertEquals(1, level1.getTerms().size());

    queryString = wrapperGrouper.toString();
    expectedString = "-( ( foo:bar~2 ) )";
    assertEquals(expectedString, queryString);
  }

  @Test
  void testReplication() {
    TermGroup group = new TermGroup();
    group.addGroup(new TermFilterGroup());

    assertEquals(1, group.getGroups().size());
    assertInstanceOf(TermFilterGroup.class, group.getGroups().get(0));

    TermGroup replicant = new TermGroup(group);
    assertEquals(1, replicant.getGroups().size());
    assertInstanceOf(TermFilterGroup.class, replicant.getGroups().get(0));

  }

  @Test
  void testRemovingGroups() {
    TermGroup group = new TermGroup();
    group.addGroup(null); // Add null group
    TermGroup grouper1 = group.addGroup();
    TermGroup grouper2 = group.addGroup();
    assertEquals(2, group.getGroups().size());

    group.removeGroup(null); // remove null group
    assertEquals(2, group.getGroups().size());

    // Group isn't in the group.
    group.removeGroup(new TermGroup());
    assertEquals(2, group.getGroups().size());

    group.removeGroup(grouper1);
    assertEquals(1, group.getGroups().size());
    assertNull(grouper1.parentGroup);

    // Try twice
    group.removeGroup(grouper1);
    assertEquals(1, group.getGroups().size());

    group.removeGroup(grouper2);
    assertEquals(0, group.getGroups().size());
  }

  @Test
  void testRemoveGroupsWithSplice() {
    TermGroup group = new TermGroup();
    TermGroup grouper1 = group.addGroup();
    TermGroup grouper2 = group.addGroup();
    assertEquals(2, group.getGroups().size());
    assertEquals(0, group.getTerms().size());

    TermGroup subGroup11 = grouper1.addGroup();
    grouper1.addGroup(subGroup11); // add same group twice
    TermGroup subGroup12 = grouper1.addGroup();

    subGroup11.addTerm(new Term("foo", "bar"));
    subGroup11.addTerm(null); // add null term
    subGroup11.addTerm(subGroup11.getTerms().get(0)); // add same term twice

    subGroup12.addTerm(new Term("chocolate", "bar"));

    TermGroup subGroup21 = grouper2.addGroup();
    subGroup21.addTerm(new Term("potato", "bar"));

    String expectedQuery = "( ( ( foo:bar ) ( chocolate:bar ) ) ( ( potato:bar ) ) )";
    String query = group.toString();
    assertEquals(expectedQuery, query);

    group.removeGroup(grouper1, true);
    assertEquals(3, group.getGroups().size());
    assertEquals(0, group.getTerms().size());

    // Note that the order is not maintained. foo and chocolate are now after potato.
    expectedQuery = "( ( ( potato:bar ) ) ( foo:bar ) ( chocolate:bar ) )";
    query = group.toString();
    assertEquals(expectedQuery, query);
    assertEquals(0, group.getTerms().size());

    group.removeGroup(subGroup11, true);
    assertEquals(2, group.getGroups().size());
    assertEquals(1, group.getTerms().size());

    expectedQuery = "( foo:bar ( ( potato:bar ) ) ( chocolate:bar ) )";
    query = group.toString();
    assertEquals(expectedQuery, query);

    group.removeGroup(subGroup12, true);
    group.removeGroup(grouper2, true);
    group.removeGroup(subGroup21, true);

    assertEquals(0, group.getGroups().size());
    assertEquals(3, group.getTerms().size());

    expectedQuery = "( foo:bar chocolate:bar potato:bar )";
    query = group.toString();
    assertEquals(expectedQuery, query);
  }

  @Test
  void testRemoveTerm() {
    TermGroup group = new TermGroup();
    Term termA = new Term("foo", "bar");
    Term termB = new Term("chocolate", "bar");
    Term termC = new Term("potato", "bar");

    group.addTerm(termA);
    group.addTerm(termB);
    group.addTerm(termC);

    String expectedQuery = "( foo:bar chocolate:bar potato:bar )";
    String query = group.toString();
    assertEquals(expectedQuery, query);

    group.removeTerm(null);
    query = group.toString();
    assertEquals(expectedQuery, query);

    group.removeTerm(new Term("foo", "bar"));
    query = group.toString();
    assertEquals(expectedQuery, query);

    group.removeTerm(termB);
    expectedQuery = "( foo:bar potato:bar )";
    query = group.toString();
    assertEquals(expectedQuery, query);

    group.removeTerm(termA);
    expectedQuery = "( potato:bar )";
    query = group.toString();
    assertEquals(expectedQuery, query);

    group.removeTerm(termC);
    expectedQuery = "";
    query = group.toString();
    assertEquals(expectedQuery, query);
  }

  @Test
  void testCreation() {
    TermGroup group = new TermGroup();
    assertNotNull(group);

    group = new TermGroup(null);
    assertNotNull(group);

    group = new TermGroup(group);
    assertNotNull(group);

    TermGroup replicant = group.replicate(null);
    assertNull(replicant);

    replicant = group.replicate(group);
    assertNotNull(replicant);
  }

  @Test
  void testTermsAreEmpty() {
    TermGroup group = new TermGroup();
    group.addTerm(new Term("", ""));
    assertTrue(group.termsAreEmpty());

    group.addTerm(new Term("", ""));
    assertTrue(group.termsAreEmpty());

    group.addTerm(new Term("", ""));
    assertTrue(group.termsAreEmpty());

    group.addTerm(new Term("", "pink panther"));
    assertFalse(group.termsAreEmpty());

    String expected = "( \"pink panther\" )";
    String query = group.toString();
    assertEquals(expected, query);

    group.addTerm(new Term("cd", "back in black"));
    group.addTerm(new Term("cd", "destroyer"));

    group.addTerm(new Term("", ""));
    group.addTerm(new Term("", ""));
    group.addTerm(new Term("", ""));

    expected = "( \"pink panther\" cd:\"back in black\" cd:destroyer )";
    query = group.toString();
    assertEquals(expected, query);
  }

  @Test
  void testEqualsAndHashCode1() {
    TermGroup group1 = new TermGroup();
    TermGroup group2 = new TermGroup();

    assertNotEquals(null, group1);
    assertNotEquals("Hello", group1);
    assertEquals(group1, group1);

    assertNotEquals(null, group1);
    assertNotEquals("string", group1);

  }

  @Test
  void testEqualsAndHashCode2() {
    TermGroup group1 = new TermGroup();
    TermGroup group2 = new TermGroup();
    assertEquals(group1, group2);
    assertEquals(group2, group1);
    assertEquals(group1.hashCode(), group2.hashCode());

    group1.addTerm(new Term("", ""));
    assertNotEquals(group1.hashCode(), group2.hashCode());
    assertNotEquals(group1, group2);

    group2.addTerm(new Term("", ""));
    assertEquals(group1.hashCode(), group2.hashCode());
    assertEquals(group1, group2);
    assertEquals(group2, group1);
  }

  @Test
  void testEqualsAndHashCode3() {
    TermGroup group1 = new TermGroup();
    TermGroup group2 = new TermGroup();

    group1.setLabel("l");
    assertNotEquals(group1.hashCode(), group2.hashCode());
    assertNotEquals(group1, group2);

    group2.setLabel("l");
    assertEquals(group1.hashCode(), group2.hashCode());
    assertEquals(group1, group2);
    assertEquals(group2, group1);
  }

  @Test
  void testEqualsAndHashCode4() {
    TermGroup group1 = new TermGroup();
    TermGroup group2 = new TermGroup();

    group1.setOccur(Occur.MUST);
    assertNotEquals(group1.hashCode(), group2.hashCode());
    assertNotEquals(group1, group2);
    assertNotEquals(group2, group1);

    group2.setOccur(Occur.MUST);
    assertEquals(group1.hashCode(), group2.hashCode());
    assertEquals(group1, group2);
    assertEquals(group2, group1);

    group2.setOccur(null);
    assertNotEquals(group1.hashCode(), group2.hashCode());
    assertNotEquals(group1, group2);
    assertNotEquals(group2, group1);

  }

  @Test
  void testEqualsAndHashCode5() {
    TermGroup group1 = new TermGroup();
    TermGroup group2 = new TermGroup();

    group1.setConstantScore(1.0f);
    assertNotEquals(group1.hashCode(), group2.hashCode());
    assertNotEquals(group1, group2);

    group2.setConstantScore(2.0f);
    assertNotEquals(group1.hashCode(), group2.hashCode());
    assertNotEquals(group1, group2);

    group2.setConstantScore(1.0f);
    assertEquals(group1.hashCode(), group2.hashCode());
    assertEquals(group1, group2);
    assertEquals(group2, group1);
  }

  @Test
  void testEqualsAndHashCode6() {
    TermGroup group1 = new TermGroup();
    TermGroup group2 = new TermGroup();

    group1.setBoost(1.0f);
    assertNotEquals(group1.hashCode(), group2.hashCode());
    assertNotEquals(group1, group2);
    assertNotEquals(group2, group1);

    group2.setBoost(2.0f);
    assertNotEquals(group1.hashCode(), group2.hashCode());
    assertNotEquals(group1, group2);
    assertNotEquals(group2, group1);

    group2.setBoost(1.0f);
    assertEquals(group1.hashCode(), group2.hashCode());
    assertEquals(group1, group2);
    assertEquals(group2, group1);

    group1.setBoost(null);
    assertNotEquals(group1, group2);
    assertNotEquals(group2, group1);
  }

  @Test
  void testEqualsAndHashCode7() {
    TermGroup group1 = new TermGroup();
    TermGroup group2 = new TermGroup();

    group1.setHasGroupingParenthesis(false);
    assertNotEquals(group1.hashCode(), group2.hashCode());
    assertNotEquals(group1, group2);

    group2.setHasGroupingParenthesis(false);
    assertEquals(group1.hashCode(), group2.hashCode());
    assertEquals(group1, group2);
    assertEquals(group2, group1);
  }

  @Test
  void testEqualsAndHashCode8() {
    TermGroup group1 = new TermGroup();
    TermGroup group2 = new TermGroup();

    group1.addGroup(new TermGroup());
    assertNotEquals(group1.hashCode(), group2.hashCode());
    assertNotEquals(group1, group2);

    group2.addGroup(new TermGroup());
    assertEquals(group1.hashCode(), group2.hashCode());
    assertEquals(group1, group2);
    assertEquals(group2, group1);

    //For coverage
    assertFalse(group1.equals(null));
    assertFalse(group1.equals(new Object()));

  }


  @Test
  void testAddingAndRemovingGroups() {
    TermGroup root = new TermGroup();
    TermGroup group1 = root.addGroup();
    TermGroup group2 = root.addGroup();

    assertSame(group1, root.groups.get(0));
    assertSame(group2, root.groups.get(1));
    assertEquals(2, root.groups.size());

    root.removeGroup(group1);
    assertSame(root.groups.get(0), group2);

    root.removeGroup(group1);
    assertSame(root.groups.get(0), group2);

    root.addGroup(group2);
    assertEquals(1, root.groups.size());

  }

  @Test
  void testAddingAndRemovingTerms() {
    TermGroup root = new TermGroup();
    Term term1 = new Term("", "");
    Term term2 = new Term("", "");
    root.addTerm(term1);
    root.addTerm(term2);

    assertSame(term1, root.terms.get(0));
    assertSame(term2, root.terms.get(1));
    assertEquals(2, root.terms.size());

    root.removeTerm(term1);
    assertSame(root.terms.get(0), term2);

    root.removeTerm(term1);
    assertSame(root.terms.get(0), term2);

    root.addTerm(term2);
    assertEquals(1, root.terms.size());

  }

}
