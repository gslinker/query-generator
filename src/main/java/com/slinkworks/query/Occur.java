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
 * A subset of the Lucene BooleanClause MUST, SHOULD, MUST_NOT
 */
public enum Occur {
  /**
   * Use this operator for clauses that <i>must</i> appear in the matching documents.
   */
  MUST {
    @Override
    public String toString() {
      return "+";
    }
  },

  /**
   * Use this operator for clauses that <i>should</i> appear in the matching documents. For a
   * BooleanQuery with no <code>MUST</code> clauses one or more <code>SHOULD</code> clauses must
   * match a document for the BooleanQuery to match.
   */
  SHOULD {
    @Override
    public String toString() {
      return "";
    }
  },

  /**
   * Use this operator for clauses that <i>must not</i> appear in the matching documents. Note that
   * it is not possible to search for queries that only consist of a <code>MUST_NOT</code> clause.
   * These clauses do not contribute to the score of documents.
   */
  MUST_NOT {
    @Override
    public String toString() {
      return "-";
    }
  }

}
