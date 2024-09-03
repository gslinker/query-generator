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
 * TermFilterGroup is a special TermGroup that is used to create filter groups.
 *
 * <p>Suppose you want to create a query like this:
 *
 * <pre>
 *     filter( foo:bar )
 * </pre>
 *
 * <p>The code is as simple as this:
 *
 * <pre>
 *         TermFilterGroup group = new TermFilterGroup();
 *         Term queryTerm = new Term("foo", "bar");
 *         group.addTerm(queryTerm);
 * </pre>
 */
public class TermFilterGroup extends TermGroup {

  protected static final String OPEN_FILTER_GROUP_STRING = "filter(";

  /**
   * Constructor
   */
  public TermFilterGroup() {
    super();
  }

  /**
   * Copy Constructor
   *
   * @param other TermGroup
   */
  public TermFilterGroup(TermGroup other) {
    super(other);
  }

  /**
   * replicate override
   *
   * @param source TermGroup to copy from
   * @return new TermFilterGroup
   */
  @Override
  protected TermGroup replicate(TermGroup source) {
    TermGroup result = null;

    if (null != source) {
      result = new TermFilterGroup(source);
    }

    return result;
  }

  /**
   * Used by toString and pretty print
   *
   * @return string that represents the opening of a group.
   */
  @Override
  protected String openGroup() {
    return TermFilterGroup.OPEN_FILTER_GROUP_STRING;
  }

  /**
   * @param obj to compare
   * @return true or false
   */
  @Override
  public boolean equals(Object obj) {
    return super.equals(obj);
  }

  /**
   * @return hash code
   */
  @Override
  public int hashCode() {
    return super.hashCode();
  }
}
