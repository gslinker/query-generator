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

import org.junit.jupiter.api.Test;

class BoostTest {

  @Test
  void testBoost() {
    Boost boost = new Boost(1.2f);
    assertEquals("^1.2", boost.toString());

    boost = new Boost(boost);
    assertEquals("^1.2", boost.toString());
    assertEquals(1.2f, boost.getValue(), 0.0);

    boost.setValue(3.2f);
    assertEquals("^3.2", boost.toString());
  }

  @Test
  void testEqualsAndHashCode() {
    Boost boost1 = new Boost(1.2f);
    Boost boost2 = new Boost(1.2f);

    assertEquals(boost1, boost1);

    assertEquals(boost1, boost2);
    assertEquals(boost2, boost1);
    assertEquals(boost1.hashCode(), boost2.hashCode());

    boost1.setValue(3.2f);
    assertNotEquals(boost1, boost2);
    assertNotEquals(boost2, boost1);
    assertNotEquals(boost1.hashCode(), boost2.hashCode());

    assertNotEquals(null, boost1);
    assertNotEquals("", boost1);

    //For coverage
    assertFalse(boost1.equals("boost2"));

  }
}
