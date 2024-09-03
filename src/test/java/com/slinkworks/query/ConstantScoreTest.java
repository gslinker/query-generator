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

class ConstantScoreTest {

  @Test
  void testConstantScore() {
    ConstantScore constantScore = new ConstantScore(0.5f);
    assertEquals("^=0.5", constantScore.toString());

    constantScore.setValue(0.3f);
    assertEquals("^=0.3", constantScore.toString());

    constantScore = new ConstantScore(constantScore);
    assertEquals("^=0.3", constantScore.toString());
    assertEquals(0.3f, constantScore.getValue(), 0);
  }

  @Test
  void testEqualsAndHashCode() {
    ConstantScore cs1 = new ConstantScore(1.2f);
    ConstantScore cs2 = new ConstantScore(1.2f);

    assertEquals(cs1, cs1);

    assertEquals(cs1, cs2);
    assertEquals(cs2, cs1);
    assertEquals(cs1.hashCode(), cs2.hashCode());

    cs1.setValue(3.2f);
    assertNotEquals(cs1, cs2);
    assertNotEquals(cs2, cs1);
    assertNotEquals(cs1.hashCode(), cs2.hashCode());

    assertNotEquals(null, cs1);
    assertNotEquals("", cs1);

    //For coverage
    assertFalse(cs1.equals("Junk"));
  }
}
