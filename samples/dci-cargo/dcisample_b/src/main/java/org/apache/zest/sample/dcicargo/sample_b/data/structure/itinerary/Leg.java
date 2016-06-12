/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.zest.sample.dcicargo.sample_b.data.structure.itinerary;

import java.time.LocalDate;
import org.apache.zest.api.association.Association;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.value.ValueComposite;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.location.Location;
import org.apache.zest.sample.dcicargo.sample_b.data.structure.voyage.Voyage;

/**
 * A leg describes an expected segment of a route:
 * - loading onto a voyage at a load location
 * - unloading from the voyage at a unload location
 *
 * All properties are mandatory and immutable.
 */
public interface Leg
    extends ValueComposite
{
    Association<Location> loadLocation();

    Property<LocalDate> loadDate();

    Association<Voyage> voyage();

    Property<LocalDate> unloadDate();

    Association<Location> unloadLocation();
}
