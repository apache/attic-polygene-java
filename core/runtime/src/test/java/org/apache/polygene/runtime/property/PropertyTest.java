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

package org.apache.polygene.runtime.property;

import java.io.Serializable;
import java.lang.reflect.Method;
import javax.swing.Icon;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.junit.Test;
import org.apache.polygene.api.common.AppliesTo;
import org.apache.polygene.api.composite.TransientBuilder;
import org.apache.polygene.api.composite.TransientComposite;
import org.apache.polygene.api.concern.GenericConcern;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.property.PropertyMixin;
import org.apache.polygene.api.property.PropertyWrapper;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;

import static org.junit.Assert.assertEquals;

/**
 * Tests for properties
 */
//@Ignore(
//    "This is an incorrect satisfiedBy case. The Property fragment support is not well defined at the moment, so until" +
//    "more work is finalized on exactly what should be supported, this is ignored not to be forgotten." )
public class PropertyTest
    extends AbstractPolygeneTest
{

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( Company.class ).withConcerns( LogConcern.class );
        module.forMixin( Nameable.class )
            .setMetaInfo( new DisplayInfo( "Name", "Name of something", "The name" ) )  // Add UI info
            .setMetaInfo(
                new RdfInfo( "label", "http://www.w3.org/1999/02/22-rdf-syntax-ns#" ) )  // Add persistence info
            .declareDefaults()
            .name()  // Select accessor
            .set( "Hello World" ); // Set default value
    }

    @Test
    public void testProperty()
    {
        Company company;
        {
            TransientBuilder<Company> builder = transientBuilderFactory.newTransientBuilder( Company.class );
            builder.prototype().name().set( "JayWay" );
            company = builder.newInstance();
        }

        company.name().set( "Jayway" );
        assertEquals( "Jayway", company.name().get() );
        System.out.println( "Name is:" + company.name().get() );
    }

    public interface Company
        extends Nameable, StandardComposite, TransientComposite
    {
    }

    public interface StandardComposite
    {
    }

    public interface Nameable
    {
        Property<String> name();
    }

    @AppliesTo( PropertyMixin.PropertyFilter.class )
    public static class LogConcern
        extends GenericConcern
    {
        @Override
        public Object invoke( Object o, final Method method, Object[] objects )
            throws Throwable
        {
            final Property<Object> property = (Property<Object>) next.invoke( o, method, objects );

            return new PropertyWrapper( property )
            {
                @Override
                public Object get()
                {
                    Object result = next.get();

                    System.out.println( "Property " + method.getName() + " accessed with value " + result );

                    return result;
                }

                @Override
                public void set( Object newValue )
                    throws IllegalArgumentException, IllegalStateException
                {
                    Object current = next.get();

                    next.set( newValue );

                    System.out
                        .println( "Property " + method.getName() + " changed from " + current + " to " + newValue );
                }
            };
        }
    }

    public static class DisplayInfo
        implements Serializable
    {
        private String name;
        private String description;
        private String toolTip;
        private Icon icon;

        public DisplayInfo( String name, String description, String toolTip )
        {
            this.name = name;
            this.description = description;
            this.toolTip = toolTip;
        }

        public DisplayInfo( String name, String description, String toolTip, Icon icon )
        {
            this.name = name;
            this.description = description;
            this.toolTip = toolTip;
            this.icon = icon;
        }

        public String getName()
        {
            return name;
        }

        public String getDescription()
        {
            return description;
        }

        public String getToolTip()
        {
            return toolTip;
        }

        public Icon getIcon()
        {
            return icon;
        }
    }

    public static class RdfInfo
        implements Serializable
    {
        private String predicate;
        private String namespace;

        public RdfInfo( String predicate, String namespace )
        {
            this.predicate = predicate;
            this.namespace = namespace;
        }

        public String getPredicate()
        {
            return predicate;
        }

        public String getNamespace()
        {
            return namespace;
        }
    }
}
