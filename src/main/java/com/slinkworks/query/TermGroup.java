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

/*------------------------------------------------------------------
    slinkworks.com

               .---.    (__)
             .'__|__`.  |  |
            ||-~~~~~-||  ||
             |  \ /  |   ||
             \  | |  /   ||
      .--------`   '-------.
     // \       / \  | |  /  \
    ((_/|       | |  /./  |___\
         \______\ /_/_/__/
          THIS IS THE WAY
            CODERS GUILD
-------------------------------------------------------------------*/

package com.slinkworks.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

/**
 * TermGroup aggregates Terms and other TermGroups to form complex queries that can be used in a
 * Standard Solr Query
 *
 * <p>Example:
 *
 * <pre>
 *         TermGroup group = new TermGroup().with(Occur.MUST).withBoost(1.4f);
 *         group.addTerm(new Term("foo", "bar").withProximity(1));
 *
 *         String query = group.toString();
 *
 *         Output: +( foo:bar~1 )^1.4
 * </pre>
 *
 * <p>Example:
 *
 * <pre>
 *         TermGroup group = new TermGroup().withConstantScore(5.0f);
 *         group.addTerm(new Term("foo", "bar").withProximity(1));
 *
 *         String query = group.toString();
 *
 *         Output: ( foo:bar~1 )^=5
 * </pre>
 *
 * <p>Instead of using string manipulation to create complex query strings the TermGroup
 * allows complex queries to be built inside an object model that can be more easily changed.
 *
 * <p>If you need to generate a query like this:
 *
 * <pre>
 * +(
 * 	(
 * 		title:"Grand Illusion"~1
 * 		title:"Paradise Theatre"~1
 * 	)^0.3
 * 	(
 * 		title:"Night At The Opera"~1
 * 		title:"News Of The World"~1
 * 	)^0.3
 * 	(
 * 		title:"Van Halen"~1
 * 		title:1984~1
 * 	)^0.3
 * )
 *
 *
 * The code to do so is as simple this:
 *
 *     TermGroup group = new TermGroup().with(Occur.MUST);
 *
 *     TermGroup favoriteStyx = group.addGroup().withBoost(0.3f);
 *     TermGroup favoriteQueen = group.addGroup().withBoost(0.3f);
 *     TermGroup favoriteVanHalen = group.addGroup().withBoost(0.3f);
 *
 *     favoriteStyx.addTerm(new Term("title","Grand Illusion").with(Occur.SHOULD).withProximity(1));
 *     favoriteStyx.addTerm(new Term("title","Paradise Theatre").with(Occur.SHOULD).withProximity(1));
 *
 *     favoriteQueen.addTerm(new Term("title","Night At The Opera").with(Occur.SHOULD).withProximity(1));
 *     favoriteQueen.addTerm(new Term("title","News Of The World").with(Occur.SHOULD).withProximity(1));
 *
 *     favoriteVanHalen.addTerm(new Term("title","Van Halen").with(Occur.SHOULD).withProximity(1));
 *     favoriteVanHalen.addTerm(new Term("title","1984").with(Occur.SHOULD).withProximity(1));
 * </pre>
 */
public class TermGroup {

  protected static final String OPEN_GROUP_STRING = "(";
  protected static final String CLOSE_GROUP_STRING = ")";
  protected static final String OPEN_COMMENT_STRING = "/* ";
  protected static final String CLOSE_COMMENT_STRING = " */";
  protected static final String DEFAULT_SEPARATOR_STRING = " ";
  protected static final String NEW_LINE_SEPARATOR_STRING = "\n";
  protected static final String PRETTY_PRINT_DEFAULT = "\t";

  protected final List<Term> terms = new ArrayList<>();
  protected String label = "";
  protected final List<TermGroup> groups = new ArrayList<>();
  protected TermGroup parentGroup = null;
  protected Occur occur = Occur.SHOULD;
  protected ConstantScore constantScore = null;
  protected Boost boost = null;
  protected boolean hasGroupingParenthesis = true;

  /**
   * Constructor
   */
  public TermGroup() {
  }

  /**
   * Copy Constructor This will not copy the parentGroup. This only copies downward.
   *
   * @param other The other Query Term Group
   */
  public TermGroup(TermGroup other) {

    if (null != other) {
      // The parent group is not copied.
      this.setParentGroup(null);

      this.setLabel(other.label);
      this.setOccur(other.occur);
      this.setHasGroupingParenthesis(other.hasGroupingParenthesis);

      if (null != other.boost) {
        this.setBoost(new Boost(other.boost.getValue()));
      }

      if (null != other.constantScore) {
        this.setConstantScore(new ConstantScore(other.constantScore.getValue()));
      }

      for (Term term : other.terms) {
        this.getTerms().add(new Term(term));
      }

      for (TermGroup subgroup : other.groups) {
        TermGroup copiedSubGroup = subgroup.replicate(subgroup);
        this.addGroup(copiedSubGroup);
      }
    }
  }

  /**
   * replicate is overridden in subclasses so that routines such as the copy constructor will create
   * TermGroup's of the correct class type.
   *
   * @param source TermGroup to copy from
   * @return replicated TermGroup
   */
  protected TermGroup replicate(TermGroup source) {
    TermGroup result = null;

    if (null != source) {
      result = new TermGroup(source);
    }

    return result;
  }

  /**
   * @return List of groups
   */
  public List<TermGroup> getGroups() {
    return groups;
  }

  /**
   * @return List of Query Terms
   */
  public List<Term> getTerms() {
    return terms;
  }

  /**
   * @return Parent Group
   */
  public TermGroup getParentGroup() {
    // If this is the top most group (aka root group) then the parent is null.
    return parentGroup;
  }

  /**
   * @param parentGroup TermGroup that will contain this group as a child.
   */
  public void setParentGroup(TermGroup parentGroup) {
    this.parentGroup = parentGroup;
  }

  /**
   * @return top most group be walking the containment hierarchy upwards
   */
  public TermGroup getRootGroup() {
    TermGroup parent = this;
    boolean done = false;
    while (!done) {
      TermGroup nextParent = parent.parentGroup;
      if (null == nextParent) {
        done = true;
      } else {
        parent = nextParent;
      }
    }

    return parent;
  }

  /**
   * @return Occur
   */
  public Occur getOccur() {
    return occur;
  }

  /**
   * @param occur SHOULD, MUST, MUST_NOT
   */
  public void setOccur(Occur occur) {
    // If this has parenthesis then set the occur.
    // +(Field:"data")
    // If this doesn't have parenthesis then it holds query terms that are not wrapped by
    // parenthesis
    // FieldA:"data" FieldB:"data" FieldC:"data"
    // and therefore there is no place to apply the occur value.
    if ((occur == Occur.SHOULD) || (this.hasGroupingParenthesis)) {
      this.occur = occur;
    }
  }

  /**
   * @param occur SHOULD, MUST, MUST_NOT
   * @return this
   */
  public TermGroup with(Occur occur) {
    this.setOccur(occur);
    return this;
  }

  /**
   * addGroup creates a new TermGroup as a child of this group.
   *
   * @return newly created group
   */
  public TermGroup addGroup() {
    TermGroup newGroup = new TermGroup();
    newGroup.setParentGroup(this);
    this.groups.add(newGroup);
    return newGroup;
  }

  /**
   * Referential check, not equivalence check.
   *
   * @param group to check
   * @return true if referential check finds the group
   */
  protected boolean groupsContains(TermGroup group) {
    boolean found = false;
    for (TermGroup termGroup : groups) {
      if (termGroup == group) {
        found = true;
        break;
      }
    }
    return found;
  }

  /**
   * @param groupToAdd Adds group to this groups.
   * @return the input group.
   */
  public TermGroup addGroup(TermGroup groupToAdd) {
    if ((null != groupToAdd) && (!this.groupsContains(groupToAdd))) {

      if (null != groupToAdd.parentGroup) {
        groupToAdd.parentGroup.getGroups().removeIf(group -> group == groupToAdd);
      }

      groupToAdd.setParentGroup(this);
      this.groups.add(groupToAdd);
    }
    return groupToAdd;
  }

  /**
   * removeGroup removes the group if it is in this group.
   *
   * @param groupToRemove is the group to remove
   */
  public void removeGroup(TermGroup groupToRemove) {
    removeGroup(groupToRemove, false);
  }

  /**
   * removeGroup removes the group if it is in this group.
   *
   * @param groupToRemove is the group to remove
   * @param splice        if true then add from the groupToRemove its groups and its query terms to
   *                      this.
   */
  public void removeGroup(TermGroup groupToRemove, boolean splice) {
    if ((null != groupToRemove) && (groupToRemove.parentGroup == this)) {
      this.groups.removeIf(group -> group == groupToRemove);
      groupToRemove.parentGroup = null;

      if (splice) {
        List<TermGroup> subGroups = new ArrayList<>(groupToRemove.groups);
        for (TermGroup subGroup : subGroups) {
          this.addGroup(subGroup);
        }

        List<Term> subTerms = new ArrayList<>(groupToRemove.terms);
        for (Term subTerm : subTerms) {
          this.addTerm(subTerm);
        }
      }
    }
  }

  /**
   * Referential check, not equivalence check.
   *
   * @param term to check
   * @return true if referential check finds the group
   */
  protected boolean termsContains(Term term) {
    boolean found = false;
    for (Term t : terms) {
      if (t == term) {
        found = true;
        break;
      }
    }
    return found;
  }

  /**
   * @param term added to this terms.
   */
  public void addTerm(Term term) {
    if ((null != term) && (!termsContains(term))) {
      terms.add(term);
    }
  }

  /**
   * @param term to remove
   */
  public void removeTerm(Term term) {
    if (null != term) {
      this.terms.removeIf(t -> t == term);
    }
  }

  /**
   * A query group may have a constant score. (title:"pink panther")^=2
   *
   * @return ConstantScore
   */
  public ConstantScore getConstantScore() {
    return constantScore;
  }

  /**
   * The constant score for the query group. Constant score values are not validated. Constant score
   * should be a valid Lucene constant score.
   *
   * @param value should be a valid Lucene constant score.
   */
  public void setConstantScore(float value) {
    this.setConstantScore(new ConstantScore(value));
  }

  /**
   * The constant score for the query group. Constant score values are not validated. Constant score
   * should be a valid Lucene constant score.
   *
   * @param value could be null. If not null it should be a valid Lucene constant score.
   */
  public void setConstantScore(ConstantScore value) {
    this.constantScore = value;
    if (null != boost) {
      boost = null;
    }
  }

  /**
   * The constant score for the query group. Constant score values are not validated. Constant score
   * should be a valid Lucene constant score.
   *
   * @param constantScore could be null. If not null it should be a valid Lucene constant score.
   * @return this
   */
  public TermGroup with(ConstantScore constantScore) {
    this.setConstantScore(constantScore);
    return this;
  }

  /**
   * The constant score for the query group. Constant score values are not validated. Constant score
   * should be a valid Lucene constant score.
   *
   * @param constantScore could be null. If not null it should be a valid Lucene constant score.
   * @return this
   */
  public TermGroup withConstantScore(float constantScore) {
    this.setConstantScore(constantScore);
    return this;
  }

  /**
   * A query group may have a boost (title:"pink panther")^1.5
   *
   * @return this
   */
  public Boost getBoost() {
    return boost;
  }

  /**
   * The boost for the query group. Boost values are not validated.
   *
   * @param value should be a valid Lucene boost value.
   */
  public void setBoost(float value) {
    this.setBoost(new Boost(value));
  }

  /**
   * The boost for the query group. Boost values are not validated. Boost value should be a valid
   * Lucene boost value.
   *
   * @param value may be null, if not null it should be a valid Lucene boost value.
   */
  public void setBoost(Boost value) {
    this.boost = value;
    if (null != constantScore) {
      constantScore = null;
    }
  }

  /**
   * The boost for the query group. Boost values are not validated. Boost value should be a valid
   * Lucene boost value.
   *
   * @param boost may be null, if not null it should be a valid Lucene boost value.
   * @return this
   */
  public TermGroup with(Boost boost) {
    this.setBoost(boost);
    return this;
  }

  /**
   * The boost for the query group. Boost values are not validated. Boost value should be a valid
   * Lucene boost value.
   *
   * @param boost should be a valid Lucene boost value.
   * @return this
   */
  public TermGroup withBoost(float boost) {
    this.setBoost(boost);
    return this;
  }

  /**
   * @return true of false
   */
  public boolean getHasGroupingParenthesis() {
    return hasGroupingParenthesis;
  }

  /**
   * If set to false when converted to a string the results will not be wrapped in parenthesis.
   *
   * @param hasGroupingParenthesis true of false
   */
  public void setHasGroupingParenthesis(boolean hasGroupingParenthesis) {
    this.hasGroupingParenthesis = hasGroupingParenthesis;
    if (!hasGroupingParenthesis) {
      // If this doesn't have parenthesis then it holds query terms that are not wrapped by
      // parenthesis
      // FieldA:"data" FieldB:"data" FieldC:"data"
      // and therefore there is no place to apply the occur value.
      this.occur = Occur.SHOULD;
    }
  }

  /**
   * @return true or false
   */
  public boolean isEmpty() {
    return termsAreEmpty() && groupsAreEmpty();
  }

  /**
   * @return true or false
   */
  protected boolean termsAreEmpty() {
    boolean isEmpty = terms.isEmpty();

    if (!isEmpty) {
      for (Term term : terms) {
        isEmpty = term.isBlank();
        if (!isEmpty) {
          break;
        }
      }
    }

    return isEmpty;
  }

  /**
   * @return true or false
   */
  protected boolean groupsAreEmpty() {
    boolean empty = groups.isEmpty();
    if (!empty) {
      for (TermGroup g : groups) {
        empty = g.isEmpty();
        if (!empty) {
          break;
        }
      }
    }
    return empty;
  }

  /**
   * Used by toString and pretty print
   *
   * @return string that represents the opening of a group.
   */
  protected String openGroup() {
    return TermGroup.OPEN_GROUP_STRING;
  }

  /**
   * Used by toString and pretty print
   *
   * @return string that represents the closing of a group
   */
  protected String closeGroup() {
    return TermGroup.CLOSE_GROUP_STRING;
  }

  /**
   * @return String representation of a Query Group that is valid for a Lucene query, or it is
   * empty.
   */
  @Override
  public String toString() {
    return prettyPrint(false, "", "", TermGroup.DEFAULT_SEPARATOR_STRING);
  }

  /**
   * @return formatted string
   */
  public String prettyPrint() {
    return prettyPrint(false, "", TermGroup.PRETTY_PRINT_DEFAULT,
        TermGroup.NEW_LINE_SEPARATOR_STRING);
  }

  /**
   * @param includeLabels      true to print labels as comments
   * @param currentIndentation current level of indentation
   * @param indentation        string to use as indentation. Usually a string of spaces or tabs
   * @param separator          string to use as a separator
   * @return formatted string
   */
  public String prettyPrint(final boolean includeLabels, String currentIndentation,
      final String indentation, final String separator) {

    StringBuilder sb = new StringBuilder();

    if (!isEmpty()) {
      String localIndentation = currentIndentation;

      // ------------------------------------------------
      // Labels
      if (includeLabels && StringUtils.isNotBlank(this.label)) {
        sb.append(localIndentation)
            .append(TermGroup.OPEN_COMMENT_STRING)
            .append(this.label)
            .append(TermGroup.CLOSE_COMMENT_STRING)
            .append(TermGroup.NEW_LINE_SEPARATOR_STRING);
      }

      // ------------------------------------------------
      // Start the group (
      if (hasGroupingParenthesis) {
        // Indent for beginning of this group.
        sb.append(currentIndentation);

        // ------------------------------------------------
        // Add the occur rule

        if (null != occur) {
          sb.append(occur);
        }

        // ------------------------------------------------
        // Add the open brace
        sb.append(this.openGroup());

        // ------------------------------------------------
        // increase indentation to be nested inside of the open brace that was just added.
        currentIndentation += indentation;
      }

      // ------------------------------------------------
      // Inside of group
      if (!termsAreEmpty()) {
        for (Term term : terms) {
          String termString = term.toString();
          if (StringUtils.isNotBlank(termString)) {
            //If there is something in the string builder append a separator before appending
            if (!sb.isEmpty()) {
              sb.append(separator);
            }

            String query = currentIndentation + termString;
            sb.append(query);
          }
        }
      }

      // ------------------------------------------------
      // Recur into the sub groups
      for (TermGroup subGroup : groups) {
        String subGroupString = subGroup.prettyPrint(includeLabels, currentIndentation, indentation,
            separator);
        if (StringUtils.isNotBlank(subGroupString)) {
          //If there is something in the string builder append a separator before appending
          if (!sb.isEmpty()) {
            sb.append(separator);
          }

          sb.append(subGroupString);
        }
      }

      // ------------------------------------------------
      // End the group )
      if (hasGroupingParenthesis) {
        sb.append(separator)
            .append(localIndentation)
            .append(this.closeGroup());

        // ------------------------------------------------
        // Add the boost or constant score
        if (null != constantScore) {
          sb.append(constantScore);

        } else if (null != boost) {
          sb.append(boost);
        }
      }
    }

    return sb.toString();
  }

  /**
   * @param f floating point number
   * @return formatted string
   */
  protected static String formatFloat(float f) {
    String results = String.format((Locale) null, "%1.4f", f);
    results = StringUtils.stripEnd(results, ".0");

    return results;
  }

  /**
   * Set the parent group's Occur value.
   *
   * @param occur SHOULD, MUST, MUST_NOT
   */
  public void setOuterOccur(Occur occur) {
    // Find a parent group that supports having Occur
    // and set the occurrence if such group is found.

    if (null != parentGroup) {
      if (parentGroup.hasGroupingParenthesis) {
        parentGroup.setOccur(occur);
      } else {
        parentGroup.setOuterOccur(occur);
      }
    }
  }

  /**
   * @return group label
   */
  public String getLabel() {
    return label;
  }

  /**
   * @param label A label that helps identify the group.
   */
  public void setLabel(String label) {
    this.label = label;
  }

  /**
   * @param label A label that helps identify the group.
   */
  public TermGroup withLabel(String label) {
    this.setLabel(label);
    return this;
  }

  /**
   * @param label to look for
   * @return true of this group has the label, false otherwise.
   */
  public boolean hasLabel(String label) {
    return StringUtils.equals(this.label, label);
  }

  /**
   * @param label to look for
   * @return list of all groups that have that label
   */
  public List<TermGroup> findByLabel(String label) {
    List<TermGroup> labeledGroups = new ArrayList<>();
    findByLabel(label, labeledGroups);
    return labeledGroups;
  }

  /**
   * @param label       to look for
   * @param foundGroups is a container to hold the groups that were found
   */
  protected void findByLabel(String label, List<TermGroup> foundGroups) {
    if (hasLabel(label)) {
      foundGroups.add(this);
    }
    for (TermGroup group : this.groups) {
      group.findByLabel(label, foundGroups);
    }
  }

  /**
   * @return true of false
   */
  public boolean isValid() {
    // This could be more sophisticated, but for now it can't be empty.
    return !isEmpty();
  }

  /**
   * @param wrapperGroup is the group that will now be the new parent of this group.
   */
  public void wrapWith(TermGroup wrapperGroup) {
    TermGroup theParentGroup = this.parentGroup;
    int myIndexLocation = -1;
    if (null != theParentGroup) {
      myIndexLocation = theParentGroup.groups.indexOf(this);
      theParentGroup.groups.removeIf(group -> group == this);
      this.parentGroup = null;
    }

    wrapperGroup.addGroup(this);

    if (null != theParentGroup) {
      wrapperGroup.setParentGroup(theParentGroup);
      theParentGroup.groups.add(myIndexLocation, wrapperGroup);
    }
  }

  /**
   * @param obj to compare
   * @return true or false. They could have different parent groups but are still considered equal.
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }

    if (null == obj) {
      return false;
    }

    if (!(obj instanceof TermGroup other)) {
      return false;
    }

    //Do not include the parent group or it will cause infinite recursion.
    //They could have different parent groups but are still considered equal.
    return (
        (Objects.equals(terms, other.terms)) &&
            (Objects.equals(label, other.label)) &&
            (Objects.equals(groups, other.groups)) &&
            (Objects.equals(occur, other.occur)) &&
            (Objects.equals(constantScore, other.constantScore)) &&
            (Objects.equals(boost, other.boost)) &&
            (hasGroupingParenthesis == other.hasGroupingParenthesis)
    );

  }

  /**
   * @return hash code
   */
  @Override
  public int hashCode() {

    //Do not include the parent group or it will cause infinite recursion.
    return Objects.hash(terms,
        label,
        groups,
        occur,
        constantScore,
        boost,
        hasGroupingParenthesis);
  }
}
