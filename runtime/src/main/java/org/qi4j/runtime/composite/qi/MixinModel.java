/*
 * Copyright (c) 2008, Rickard �berg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.runtime.composite.qi;

import java.lang.reflect.InvocationHandler;
import java.util.HashSet;
import java.util.Set;
import org.qi4j.composite.State;
import org.qi4j.composite.scope.This;
import org.qi4j.runtime.composite.FragmentInvocationHandler;
import org.qi4j.runtime.composite.GenericFragmentInvocationHandler;
import org.qi4j.runtime.composite.TypedFragmentInvocationHandler;

/**
 * TODO
 */
public final class MixinModel
{
    // Model
    private Class mixinClass;
    private ConstructorsModel constructorsModel;
    private InjectedFieldsModel injectedFieldsModel;
    private InjectedMethodsModel injectedMethodsModel;

    public MixinModel( Class mixinClass )
    {
        this.mixinClass = mixinClass;

        constructorsModel = new ConstructorsModel( mixinClass );
        injectedFieldsModel = new InjectedFieldsModel( mixinClass );
        injectedMethodsModel = new InjectedMethodsModel( mixinClass );
    }

    public Class type()
    {
        return mixinClass;
    }

    // Binding
    public void bind( BindingContext context )
    {
        constructorsModel.bind( context );
        injectedFieldsModel.bind( context );
        injectedMethodsModel.bind( context );
    }

    // Context
    public Object newInstance( CompositeInstance compositeInstance, Set<Object> uses, State state )
    {
        InjectionContext injectionContext = new InjectionContext( compositeInstance, uses, state );
        Object mixin = constructorsModel.newInstance( injectionContext );
        injectedFieldsModel.inject( injectionContext, mixin );
        injectedMethodsModel.inject( injectionContext, mixin );
        return mixin;
    }

    public FragmentInvocationHandler newInvocationHandler( Class methodClass )
    {
        if( InvocationHandler.class.isAssignableFrom( mixinClass ) && !methodClass.isAssignableFrom( mixinClass ) )
        {
            return new GenericFragmentInvocationHandler();
        }
        else
        {
            return new TypedFragmentInvocationHandler();
        }

    }

    public Set<Class> thisMixinTypes()
    {
        final Set<Class> mixinTypes = new HashSet<Class>();

        DependencyVisitor visitor = new DependencyVisitor()
        {
            public void visit( DependencyModel dependencyModel )
            {
                if( dependencyModel.injectionAnnotation().annotationType().equals( This.class ) )
                {
                    mixinTypes.add( dependencyModel.injectionClass() );
                }
            }
        };

        constructorsModel.visitDependencies( visitor );
        injectedFieldsModel.visitDependencies( visitor );
        injectedMethodsModel.visitDependencies( visitor );

        return mixinTypes;
    }
}
