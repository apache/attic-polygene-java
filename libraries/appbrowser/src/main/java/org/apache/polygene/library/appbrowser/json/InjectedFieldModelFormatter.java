/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.polygene.library.appbrowser.json;

import org.json.JSONException;
import org.json.JSONWriter;
import org.apache.polygene.api.composite.DependencyDescriptor;
import org.apache.polygene.api.composite.InjectedFieldDescriptor;
import org.apache.polygene.library.appbrowser.Formatter;

public class InjectedFieldModelFormatter extends AbstractJsonFormatter<InjectedFieldDescriptor, DependencyDescriptor>
{
    public InjectedFieldModelFormatter( JSONWriter writer )
    {
        super(writer);
    }

    @Override
    public void enter( InjectedFieldDescriptor visited )
        throws JSONException
    {
        object();
        field("name", visited.field().getName() );
//        field( "optional", visited.optional() );
//        field( "injectedclass", visited.injectedClass().getName() );
//        field( "injectedannotation", visited.injectionAnnotation().toString() );
//        field( "injectedtype", visited.injectionType().toString() );
//        field( "rawinjectectiontype", visited.rawInjectionType().getName() );
    }

    @Override
    public void leave( InjectedFieldDescriptor visited )
        throws JSONException
    {
        endObject();
    }

    @Override
    public void visit( DependencyDescriptor visited )
        throws JSONException
    {

    }
}
