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

class ProximityTest {

  @Test
  void testProximity() {
    Proximity proximity = new Proximity(1);
    assertEquals("~1", proximity.toString());

    proximity = new Proximity(2);
    assertEquals("~2", proximity.toString());

    proximity = new Proximity(proximity);
    assertEquals(2, proximity.getValue());

    proximity.setValue(3);
    assertEquals(3, proximity.getValue());
  }

  @Test
  void testEqualsAndHashCode() {
    Proximity p1 = new Proximity(1);
    Proximity p2 = new Proximity(1);

    assertEquals(p1, p1);

    assertEquals(p1, p2);
    assertEquals(p2, p1);
    assertEquals(p1.hashCode(), p2.hashCode());

    p1.setValue(2);
    assertNotEquals(p1, p2);
    assertNotEquals(p2, p1);
    assertNotEquals(p1.hashCode(), p2.hashCode());

    assertNotEquals(null, p1);
    assertNotEquals("", p1);

    //For coverage
    assertFalse(p1.equals("Junk"));
  }
}
