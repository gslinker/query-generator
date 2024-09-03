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
 * Proximity represents the fuzziness for a single word or the proximity for multiple words in a
 * phrase query.
 * <pre>
 *   color:grey~1             Single Word
 *   title:"pink panther"~2   Phrase
 * </pre>
 */
public class Proximity {

  private int value;

  /**
   * Constructor
   *
   * @param value A valid value for fuzziness or proximity. The value is not validated.
   */
  public Proximity(int value) {
    this.value = value;
  }

  /**
   * Copy Constructor
   *
   * @param source Another Query Term Proximity
   */
  public Proximity(Proximity source) {
    this.value = source.value;
  }

  /**
   * @return proximity
   */
  public int getValue() {
    return value;
  }

  /**
   * @param value A valid value for fuzziness or proximity. The value is not validated.
   */
  public void setValue(Integer value) {
    this.value = value;
  }

  /**
   * The proximity value prepended with the fuzzy/proximity specifier ~
   *
   * @return proximity such as ~2
   */
  @Override
  public String toString() {
    return "~" + value;
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

    if (!(obj instanceof Proximity other)) {
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
