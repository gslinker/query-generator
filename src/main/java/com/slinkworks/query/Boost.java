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
 * Boost represents the boost value used in a Standard Solr Query. For example:
 * <pre>
 *
 * title:"pink panther"^2.0   where 2.0 is the boost value
 * </pre>
 */
public class Boost {

  private float value;

  /**
   * Copy Constructor
   *
   * @param other Another Query Term Boost
   */
  public Boost(Boost other) {
    this.value = other.value;
  }

  /**
   * Constructor
   *
   * @param boost A valid boost value for a Lucene query. Value is not validated.
   */
  public Boost(float boost) {
    this.value = boost;
  }

  /**
   * @return the boost value
   */
  public float getValue() {
    return value;
  }

  /**
   * @param value A valid boost value for a Lucene query. Value is not validated.
   */
  public void setValue(float value) {
    this.value = value;
  }

  /**
   * The boost value prepended with the boost specifier ^
   *
   * @return boost such as ^1.3
   */
  @Override
  public String toString() {
    return "^" + TermGroup.formatFloat(value);
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

    if (!(obj instanceof Boost other)) {
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
