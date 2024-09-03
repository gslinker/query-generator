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

import java.util.Objects;
import java.util.StringTokenizer;
import org.apache.commons.lang3.StringUtils;

/**
 * Term is a unit of search. It is composed of two elements, the value of the word as a string and
 * the name of the field.
 *
 * <p>For example:
 *
 * <ul>
 *   <li>+title:"pink panther"~1
 *   <li>title:("pink panther" "treasure island")
 *   <li>year:[1950 TO 1960]^=2
 *   <li>year:1953^0.5
 * </ul>
 *
 * <p>Instantiate a Term and set the values and call toString to get a string that can be used
 * in a Standard Solr Query.
 *
 * <pre>
 *      Term term = new Term("pink panther").withBoost(1.5f);
 *      term.toString()
 *
 *      Output: "pink panther"^1.5
 * </pre>
 *
 * <pre>
 *      Term term = new Term("title", "pink panther").withBoost(1.5f);
 *      term.toString()
 *
 *      Output: title:"pink panther"^1.5
 * </pre>
 */
public class Term {

  private String field = null;
  private String value = null;
  private Boost boost = null;
  private ConstantScore constantScore = null;
  private Proximity proximity = null;
  private Occur occur = Occur.SHOULD;
  private boolean isRangeQuery = false;
  private boolean isGroupingClauseQuery = false;

  /**
   * @param field Field Name
   * @param value Value of Field
   */
  public Term(String field, String value) {
    this.setField(field);
    this.setValue(value);
  }

  /**
   * @param value Value of the Default Field
   */
  public Term(String value) {
    this.setValue(value);
  }

  /**
   * Copy Constructor
   *
   * @param source Another Query Term
   */
  public Term(Term source) {
    this.field = source.field;
    this.value = source.value;
    this.isRangeQuery = source.isRangeQuery;
    this.isGroupingClauseQuery = source.isGroupingClauseQuery;
    this.occur = source.occur;
    if (null != source.boost) {
      this.boost = new Boost(source.boost);
    }
    if (null != source.constantScore) {
      this.constantScore = new ConstantScore(source.constantScore);
    }
    if (null != source.proximity) {
      this.proximity = new Proximity(source.proximity);
    }
  }

  /**
   * @return field Can be null or empty
   */
  public String getField() {
    return field;
  }

  /**
   * @param field Can be null or empty
   */
  protected void setField(String field) {
    this.field = field;
  }

  /**
   * @return Can be null or empty
   */
  public String getValue() {
    return value;
  }

  /**
   * If the clause has multiple tokens then wrap with double quotes. If the clause starts with (
   * then it is Grouping clause. If the clause starts with [ or { then it is a Range clause
   *
   * @param value Can be null or empty
   */
  protected void setValue(String value) {

    if (StringUtils.isNotBlank(value)) {
      isRangeQuery = value.startsWith("[") || value.startsWith("{");
      isGroupingClauseQuery = value.startsWith("(");

      // If the clause has more than one token and it is not a range query
      // and doesn't have an opening parenthesis (Grouping Clause query)
      // then it should be wrapped in double quotes.
      // Suppose the clause is Pink Panther
      // Then the clause should be wrapped to be "Pink Panther".
      StringTokenizer tokenizer = new StringTokenizer(value, " ");
      if ((tokenizer.countTokens() > 1)
          && (!value.startsWith("\""))
          && (!isRangeQuery)
          && (!isGroupingClauseQuery)) {
        value = "\"" + value + "\""; // Wrap in quotes
      }

      if (isRangeQuery) {
        this.proximity = null;
      }

      if (isGroupingClauseQuery) {
        this.proximity = null;
        this.boost = null;
      }
    }
    this.value = value;
  }

  /**
   * @return true if value and field are blank, else return false
   */
  public boolean isBlank() {
    return StringUtils.isBlank(value) && StringUtils.isBlank(field);
  }

  /**
   * A query may have a boost title:"pink panther"^1.5
   *
   * @return Boost
   */
  public Boost getBoost() {
    return boost;
  }

  /**
   * The boost for the query term. Boost values are not validated. Boost value should be a valid
   * Lucene boost value.
   *
   * @param boost may be null, should be a valid Lucene boost value.
   */
  public void setBoost(Boost boost) {
    if (!isGroupingClauseQuery) {
      if (null != boost) {
        this.constantScore = null;
      }
      this.boost = boost;
    }
  }

  /**
   * The boost for the query term. Boost values are not validated.
   *
   * @param boost should be a valid Lucene boost value.
   */
  public void setBoost(float boost) {
    this.setBoost(new Boost(boost));
  }

  /**
   * A query may have a constant score. title:"pink panther"^=2
   *
   * @return ConstantScore
   */
  public ConstantScore getConstantScore() {
    return constantScore;
  }

  /**
   * The constant score for the query term. Constant score values are not validated. Constant score
   * should be a valid Lucene constant score value.
   *
   * @param constantScore may be null, should be a valid Lucene constant score.
   */
  public void setConstantScore(ConstantScore constantScore) {
    if (null != constantScore) {
      this.boost = null;
    }
    this.constantScore = constantScore;
  }

  /**
   * The constant score for the query term. Constant score values are not validated. Constant score
   * should be a valid Lucene constant score value.
   *
   * @param constantScore should be a valid Lucene constant score.
   */
  public void setConstantScore(float constantScore) {
    this.setConstantScore(new ConstantScore(constantScore));
  }

  /**
   * A query may have a fuzzy value or a proximity value. If the clause is a phrase it will be
   * surrounded by quotes and will be a proximity value. title:"pink panther"~2 If the clause is a
   * single term it will not be surrounded by quotes and will be a fuzzy search value.
   * title:apache~1 All values must be valid Lucene values. Value is not checked for validity.
   *
   * @return Proximity
   */
  public Proximity getProximity() {
    return proximity;
  }

  /**
   * A query may have a fuzzy value or a proximity value. If the clause is a phrase it will be
   * surrounded by quotes and will be a proximity value. title:"pink panther"~2 If the clause is a
   * single term it will not be surrounded by quotes and will be a fuzzy search value.
   * title:apache~1 All values must be valid Lucene values. Value is not checked for validity.
   *
   * @param proximity maybe null, should be a valid Lucene value for proximity or fuzziness.
   */
  public void setProximity(Proximity proximity) {
    if ((!isGroupingClauseQuery) && (!isRangeQuery)) {
      this.proximity = proximity;
    }
  }

  /**
   * A query may have a fuzzy value or a proximity value. If the clause is a phrase it will be
   * surrounded by quotes and will be a proximity value. title:"pink panther"~2 If the clause is a
   * single term it will not be surrounded by quotes and will be a fuzzy search value.
   * title:apache~1 All values must be valid Lucene values. Value is not checked for validity.
   *
   * @param proximity should be a valid Lucene value for proximity or fuzziness.
   */
  public void setProximity(int proximity) {
    this.setProximity(new Proximity(proximity));
  }

  /**
   * Occur will be, SHOULD, MUST, or MUST_NOT
   *
   * @return Occur. Results can be null.
   */
  public Occur getOccur() {
    return occur;
  }

  /**
   * @param occur SHOULD, MUST, MUST_NOT
   */
  public void setOccur(Occur occur) {
    this.occur = occur;
  }

  /**
   * @param occur SHOULD, MUST, MUST_NOT
   */
  public Term withOccur(Occur occur) {
    this.setOccur(occur);
    return this;
  }

  /**
   * @param occur SHOULD, MUST, MUST_NOT
   * @return this
   */
  public Term with(Occur occur) {
    return withOccur(occur);
  }

  /**
   * The boost for the query term. Boost values are not validated. Boost value should be a valid
   * Lucene boost value.
   *
   * @param boost may be null, should be a valid Lucene boost value.
   * @return this
   */
  public Term with(Boost boost) {
    this.setBoost(boost);
    return this;
  }

  /**
   * The boost for the query term. Boost values are not validated. Boost value should be a valid
   * Lucene boost value.
   *
   * @param boost may be null, should be a valid Lucene boost value.
   * @return this
   */
  public Term withBoost(float boost) {
    this.setBoost(boost);
    return this;
  }

  /**
   * The constant score for the query term. Constant score values are not validated. Constant score
   * should be a valid Lucene constant score value.
   *
   * @param constantScore may be null, should be a valid Lucene constant score.
   * @return this
   */
  public Term with(ConstantScore constantScore) {
    this.setConstantScore(constantScore);
    return this;
  }

  /**
   * The constant score for the query term. Constant score values are not validated. Constant score
   * should be a valid Lucene constant score value.
   *
   * @param constantScore may be null, should be a valid Lucene constant score.
   * @return this
   */
  public Term withConstantScore(float constantScore) {
    this.setConstantScore(constantScore);
    return this;
  }

  /**
   * A query may have a fuzzy value or a proximity value. If the clause is a phrase it will be
   * surrounded by quotes and will be a proximity value. title:"pink panther"~2 If the clause is a
   * single term it will not be surrounded by quotes and will be a fuzzy search value.
   * title:apache~1 All values must be valid Lucene values. Value is not checked for validity.
   *
   * @param proximity maybe null, should be a valid Lucene value for proximity or fuzziness.
   * @return this
   */
  public Term with(Proximity proximity) {
    this.setProximity(proximity);
    return this;
  }

  /**
   * A query may have a fuzzy value or a proximity value. If the clause is a phrase it will be
   * surrounded by quotes and will be a proximity value. title:"pink panther"~2 If the clause is a
   * single term it will not be surrounded by quotes and will be a fuzzy search value.
   * title:apache~1 All values must be valid Lucene values. Value is not checked for validity.
   *
   * @param proximity maybe null, should be a valid Lucene value for proximity or fuzziness.
   * @return this
   */
  public Term withProximity(int proximity) {
    this.setProximity(proximity);
    return this;
  }

  /**
   * @return String representation of Query Term that is valid for as a Lucene query term, or is
   * empty.
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    // There has to be a clause
    // If the field is blank then it is a query on the default field.
    if (StringUtils.isNotBlank(value)) {
      sb.append(occur.toString());

      if (StringUtils.isNotBlank(field)) {
        sb.append(field).append(":");
      }

      sb.append(value);

      if (null != proximity) {
        sb.append(proximity);
      }

      if (null != boost) {
        sb.append(boost);
      } else if (null != constantScore) {
        sb.append(constantScore);
      }
    }

    return sb.toString();
  }

  /**
   * @param obj to compare
   * @return true or false
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }

    if (null == obj) {
      return false;
    }

    if (!(obj instanceof Term other)) {
      return false;
    }

    return ((Objects.equals(field, other.field))
        && (Objects.equals(value, other.value))
        && (Objects.equals(boost, other.boost))
        && (Objects.equals(constantScore, other.constantScore))
        && (Objects.equals(proximity, other.proximity))
        && (Objects.equals(occur, other.occur)));
  }

  /**
   * @return hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(field, value, boost, constantScore, proximity, occur);
  }
}
