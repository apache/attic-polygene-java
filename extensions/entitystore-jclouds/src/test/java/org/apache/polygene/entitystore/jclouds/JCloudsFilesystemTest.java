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

package org.apache.polygene.entitystore.jclouds;

import java.util.Collections;
import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.entitystore.jclouds.assembly.JCloudsEntityStoreAssembler;
import org.apache.polygene.test.EntityTestAssembler;
import org.apache.polygene.test.entity.AbstractEntityStoreTest;
import org.apache.polygene.valueserialization.orgjson.OrgJsonValueSerializationAssembler;
import org.jclouds.filesystem.reference.FilesystemConstants;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

public class JCloudsFilesystemTest
    extends AbstractEntityStoreTest
{
    @Rule
    public TemporaryFolder tmpDir = new TemporaryFolder();

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        super.assemble( module );
        ModuleAssembly config = module.layer().module( "config" );
        new EntityTestAssembler().assemble( config );
        new OrgJsonValueSerializationAssembler().assemble( module );
        new JCloudsEntityStoreAssembler().withConfig( config, Visibility.layer ).assemble( module );
        JCloudsMapEntityStoreConfiguration defaults = config.forMixin( JCloudsMapEntityStoreConfiguration.class )
                                                            .declareDefaults();
        defaults.provider().set( "filesystem" );
        defaults.properties().set( Collections.singletonMap( FilesystemConstants.PROPERTY_BASEDIR,
                                                             tmpDir.getRoot().getAbsolutePath() ) );
    }
}