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

/**
 * ConstantScore represents the constant score value used in a Standard Solr Query For example:
 * <pre>
 *
 *    title:"pink panther"^=1.5   where 1.5 is a constant score
 * </pre>
 */
public class ConstantScore {

  private float value;

  /**
   * Copy Constructor
   *
   * @param other Another Query Term Constant Score
   */
  public ConstantScore(ConstantScore other) {
    this.value = other.value;
  }

  /**
   * Constructor
   *
   * @param constantScore A valid constant score for a Lucene query. Value is not validated.
   */
  public ConstantScore(float constantScore) {
    this.value = constantScore;
  }

  /**
   * @return the constant score
   */
  public float getValue() {
    return value;
  }

  /**
   * @param value A valid constant score for a Lucene query. Value is not validated.
   */
  public void setValue(float value) {
    this.value = value;
  }

  /**
   * The constant score prepended with the constant score specifier ^=
   *
   * @return constant score such as ^=0.666
   */
  @Override
  public String toString() {
    return "^=" + TermGroup.formatFloat(value);
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

    if (!(obj instanceof ConstantScore other)) {
      return false;
    }

    return this.value == other.value;
  }

  /**
   * @return hash code
   */
  @Override
  public int hashCode() {
    return Float.valueOf(value).hashCode();
  }
}
