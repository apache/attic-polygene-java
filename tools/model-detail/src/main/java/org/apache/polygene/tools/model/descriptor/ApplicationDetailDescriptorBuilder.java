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
package org.apache.polygene.tools.model.descriptor;

import java.util.HashMap;
import java.util.Map;
import org.apache.polygene.api.activation.ActivatorDescriptor;
import org.apache.polygene.api.composite.ConstructorDescriptor;
import org.apache.polygene.api.composite.InjectedFieldDescriptor;
import org.apache.polygene.api.composite.InjectedMethodDescriptor;
import org.apache.polygene.api.composite.InjectedParametersDescriptor;
import org.apache.polygene.api.composite.MethodDescriptor;
import org.apache.polygene.api.composite.TransientDescriptor;
import org.apache.polygene.api.concern.ConcernDescriptor;
import org.apache.polygene.api.concern.ConcernsDescriptor;
import org.apache.polygene.api.constraint.ConstraintDescriptor;
import org.apache.polygene.api.constraint.ConstraintsDescriptor;
import org.apache.polygene.api.entity.EntityDescriptor;
import org.apache.polygene.api.mixin.MixinDescriptor;
import org.apache.polygene.api.object.ObjectDescriptor;
import org.apache.polygene.api.service.ImportedServiceDescriptor;
import org.apache.polygene.api.service.ServiceDescriptor;
import org.apache.polygene.api.sideeffect.SideEffectDescriptor;
import org.apache.polygene.api.sideeffect.SideEffectsDescriptor;
import org.apache.polygene.api.structure.ApplicationDescriptor;
import org.apache.polygene.api.structure.LayerDescriptor;
import org.apache.polygene.api.structure.ModuleDescriptor;
import org.apache.polygene.api.util.HierarchicalVisitor;
import org.apache.polygene.api.value.ValueDescriptor;

public final class ApplicationDetailDescriptorBuilder
{
    private ApplicationDetailDescriptorBuilder()
    {
    }

    public static ApplicationDetailDescriptor createApplicationDetailDescriptor( ApplicationDescriptor anApplication )
    {
        ApplicationDescriptorVisitor visitor = new ApplicationDescriptorVisitor();
        anApplication.accept( visitor );

        return visitor.applicationDescriptor;
    }

    static final class ApplicationDescriptorVisitor
        implements HierarchicalVisitor<Object, Object, RuntimeException>
    {
        // Temp: application
        private ApplicationDetailDescriptor applicationDescriptor;

        // Temp: Layer variables
        private LayerDetailDescriptor currLayerDescriptor;
        // Cache to lookup layer descriptor -> node
        private final Map<LayerDescriptor, LayerDetailDescriptor> layerDescToDetail;

        // Temp: current module
        private ModuleDetailDescriptor currModuleDescriptor;

        // Temp: current composite
        private CompositeDetailDescriptor currCompositeDescriptor;

        // Temp: curr activator
        private ActivatorDetailDescriptor currActivatorDescriptor;

        // Temp: curr mixin
        private MixinDetailDescriptor currMixinDescriptor;

        // Temp: current constructor
        private ConstructorDetailDescriptor currConstructorDescriptor;

        // Temp: current injected method
        private InjectedMethodDetailDescriptor currInjectedMethodDescriptor;

        // Temp: current composite method
        private CompositeMethodDetailDescriptor currMethodDesciptor;

        // Temp: current method constraints
        private MethodConstraintsDetailDescriptor currMethodConstraintsDescriptor;

        // Temp: current object
        private ObjectDetailDescriptor currObjectDescriptor;

        // Temp: current method concerns
        private MethodConcernsDetailDescriptor currMethodConcernsDescriptor;

        // Temp: current method concern
        private MethodConcernDetailDescriptor currMethodConcernDescriptor;

        // Temp: current method side effects
        private MethodSideEffectsDetailDescriptor currMethodSideEffectsDescriptor;

        // Temp: current side effect
        private MethodSideEffectDetailDescriptor currMethodSideEffectDescriptor;

        private ApplicationDescriptorVisitor()
        {
            layerDescToDetail = new HashMap<>();
        }

        @Override
        public boolean visitEnter( Object visited )
            throws RuntimeException
        {
            if( visited instanceof ApplicationDescriptor )
            {
                applicationDescriptor = new ApplicationDetailDescriptor( (ApplicationDescriptor) visited );
            }
            else if( visited instanceof LayerDescriptor )
            {
                LayerDescriptor layerDescriptor = (LayerDescriptor) visited;
                currLayerDescriptor = getLayerDetailDescriptor( layerDescriptor );
                applicationDescriptor.addLayer( currLayerDescriptor );

                layerDescriptor.usedLayers().layers().forEach(
                    usedLayer ->
                    {
                        LayerDetailDescriptor usedLayerDetailDesc = getLayerDetailDescriptor( usedLayer );
                        currLayerDescriptor.addUsedLayer( usedLayerDetailDesc );
                    }
                );
            }
            else if( visited instanceof ModuleDescriptor )
            {
                ModuleDescriptor moduleDescriptor = (ModuleDescriptor) visited;
                currModuleDescriptor = new ModuleDetailDescriptor( moduleDescriptor );
                currLayerDescriptor.addModule( currModuleDescriptor );
            }
            else if( visited instanceof ActivatorDescriptor )
            {
                ActivatorDescriptor activatorDescriptor = (ActivatorDescriptor) visited;
                currActivatorDescriptor = new ActivatorDetailDescriptor( activatorDescriptor );
                if( currCompositeDescriptor != null )
                {
                    if( currCompositeDescriptor instanceof ServiceDetailDescriptor )
                    {
                        ( (ServiceDetailDescriptor) currCompositeDescriptor ).addActivator( currActivatorDescriptor );
                    }
                    else if( currCompositeDescriptor instanceof ImportedServiceDetailDescriptor )
                    {
                        ( (ImportedServiceDetailDescriptor) currCompositeDescriptor ).addActivator( currActivatorDescriptor );
                    }
                    else
                    {
                        throw new IllegalStateException( "ActivatorDescriptor is only valid for "
                                                         + "services, modules, layers and application." );
                    }
                }
                else if( currModuleDescriptor != null )
                {
                    currModuleDescriptor.addActivator( currActivatorDescriptor );
                }
                else if( currLayerDescriptor != null )
                {
                    currLayerDescriptor.addActivator( currActivatorDescriptor );
                }
                else if( applicationDescriptor != null )
                {
                    applicationDescriptor.addActivator( currActivatorDescriptor );
                }
                else
                {
                    throw new IllegalStateException( "ActivatorDescriptor is only valid for "
                                                     + "services, modules, layers and application." );
                }
            }
            else if( visited instanceof ServiceDescriptor )
            {
                ServiceDetailDescriptor descriptor = new ServiceDetailDescriptor( (ServiceDescriptor) visited );
                currModuleDescriptor.addService( descriptor );
                currCompositeDescriptor = descriptor;
            }
            else if( visited instanceof ImportedServiceDescriptor )
            {
                ImportedServiceDetailDescriptor descriptor = new ImportedServiceDetailDescriptor( new ImportedServiceCompositeDescriptor( (ImportedServiceDescriptor) visited ) );
                currModuleDescriptor.addImportedService( descriptor );
                currCompositeDescriptor = descriptor;
            }
            else if( visited instanceof EntityDescriptor )
            {
                EntityDetailDescriptor descriptor = new EntityDetailDescriptor( (EntityDescriptor) visited );
                currModuleDescriptor.addEntity( descriptor );
                currCompositeDescriptor = descriptor;
            }
            else if( visited instanceof ValueDescriptor )
            {
                ValueDetailDescriptor descriptor = new ValueDetailDescriptor( (ValueDescriptor) visited );
                currModuleDescriptor.addValue( descriptor );
                currCompositeDescriptor = descriptor;
            }
            else if( visited instanceof TransientDescriptor )
            {
                TransientDetailDescriptor descriptor = new TransientDetailDescriptor( (TransientDescriptor) visited );
                currModuleDescriptor.addTransient( descriptor );
                currCompositeDescriptor = descriptor;
            }
            else if( visited instanceof MethodDescriptor )
            {
                if( currCompositeDescriptor == null )
                {
                    // Service via CompositeDescriptor in progress )
                    return false;
                }
                currMethodDesciptor = new CompositeMethodDetailDescriptor( (MethodDescriptor) visited );
                currCompositeDescriptor.addMethod( currMethodDesciptor );
            }
            else if( visited instanceof ConstraintsDescriptor )
            {
                if( currCompositeDescriptor == null )
                {
                    // Service via CompositeDescriptor in progress )
                    return false;
                }
                currMethodConstraintsDescriptor = new MethodConstraintsDetailDescriptor( (ConstraintsDescriptor) visited );
                currMethodDesciptor.setConstraints( currMethodConstraintsDescriptor );
            }
            else if( visited instanceof ConcernsDescriptor )
            {
                if( currCompositeDescriptor == null )
                {
                    // Service via CompositeDescriptor in progress )
                    return false;
                }
                currMethodConcernsDescriptor = new MethodConcernsDetailDescriptor( (ConcernsDescriptor) visited );
                currMethodDesciptor.setConcerns( currMethodConcernsDescriptor );
            }
            else if( visited instanceof ConcernDescriptor )
            {
                if( currCompositeDescriptor == null )
                {
                    // Service via CompositeDescriptor in progress )
                    return false;
                }
                resetInjectableRelatedVariables();

                currMethodConcernDescriptor = new MethodConcernDetailDescriptor( (ConcernDescriptor) visited );
                currMethodConcernsDescriptor.addConcern( currMethodConcernDescriptor );
            }
            else if( visited instanceof SideEffectsDescriptor )
            {
                if( currCompositeDescriptor == null )
                {
                    // Service via CompositeDescriptor in progress )
                    return false;
                }
                currMethodSideEffectsDescriptor = new MethodSideEffectsDetailDescriptor( (SideEffectsDescriptor) visited );
                currMethodDesciptor.setSideEffects( currMethodSideEffectsDescriptor );
            }
            else if( visited instanceof SideEffectDescriptor )
            {
                if( currCompositeDescriptor == null )
                {
                    // Service via CompositeDescriptor in progress )
                    return false;
                }
                resetInjectableRelatedVariables();

                currMethodSideEffectDescriptor = new MethodSideEffectDetailDescriptor( (SideEffectDescriptor) visited );
                currMethodSideEffectsDescriptor.addSideEffect( currMethodSideEffectDescriptor );
            }
            else if( visited instanceof MixinDescriptor )
            {
                if( currCompositeDescriptor == null )
                {
                    // Service via CompositeDescriptor in progress )
                    return false;
                }
                resetInjectableRelatedVariables();

                currMixinDescriptor = new MixinDetailDescriptor( (MixinDescriptor) visited );
                currCompositeDescriptor.addMixin( currMixinDescriptor );
            }
            else if( visited instanceof ObjectDescriptor )
            {
                resetInjectableRelatedVariables();

                currObjectDescriptor = new ObjectDetailDescriptor( (ObjectDescriptor) visited );
                currModuleDescriptor.addObject( currObjectDescriptor );
            }
            else if( visited instanceof ConstructorDescriptor )
            {
                if( currCompositeDescriptor == null )
                {
                    // Service via CompositeDescriptor in progress )
                    return false;
                }
                currConstructorDescriptor = new ConstructorDetailDescriptor( (ConstructorDescriptor) visited );
                currInjectedMethodDescriptor = null;

                // Invoked for mixin and object
                if( currActivatorDescriptor != null )
                {
                    currActivatorDescriptor.addConstructor( currConstructorDescriptor );
                }
                else if( currMixinDescriptor != null )
                {
                    currMixinDescriptor.addConstructor( currConstructorDescriptor );
                }
                else if( currObjectDescriptor != null )
                {
                    currObjectDescriptor.addConstructor( currConstructorDescriptor );
                }
                else if( currMethodConcernDescriptor != null )
                {
                    currMethodConcernDescriptor.addConstructor( currConstructorDescriptor );
                }
                else if( currMethodSideEffectDescriptor != null )
                {
                    currMethodSideEffectDescriptor.addConstructor( currConstructorDescriptor );
                }
                else
                {
                    throw new IllegalStateException( "ConstructorDescriptor is only valid for "
                                                     + "activator, mixin, object, concern and side-effect. "
                                                     + "Visiting [" + visited + "]" );
                }
            }
            else if( visited instanceof InjectedParametersDescriptor )
            {
                if( currCompositeDescriptor == null )
                {
                    // Service via CompositeDescriptor in progress )
                    return false;
                }
                InjectedParametersDetailDescriptor detailDescriptor = new InjectedParametersDetailDescriptor( (InjectedParametersDescriptor) visited );

                // Invoked for constructor and injected method
                if( currConstructorDescriptor != null )
                {
                    currConstructorDescriptor.setInjectedParameter( detailDescriptor );
                }
                else if( currInjectedMethodDescriptor != null )
                {
                    currInjectedMethodDescriptor.setInjectedParameter( detailDescriptor );
                }
                else
                {
                    throw new IllegalStateException( "InjectedParametersDescriptor is only valid for "
                                                     + "constructor and injector method descriptor." );
                }
            }
            else if( visited instanceof InjectedMethodDescriptor )
            {
                if( currCompositeDescriptor == null )
                {
                    // Service via CompositeDescriptor in progress )
                    return false;
                }
                // Invoked for mixin and object
                currInjectedMethodDescriptor = new InjectedMethodDetailDescriptor( (InjectedMethodDescriptor) visited );
                currConstructorDescriptor = null;

                // Invoked for mixin and object
                if( currActivatorDescriptor != null )
                {
                    currActivatorDescriptor.addInjectedMethod( currInjectedMethodDescriptor );
                }
                else if( currMixinDescriptor != null )
                {
                    currMixinDescriptor.addInjectedMethod( currInjectedMethodDescriptor );
                }
                else if( currObjectDescriptor != null )
                {
                    currObjectDescriptor.addInjectedMethod( currInjectedMethodDescriptor );
                }
                else if( currMethodConcernDescriptor != null )
                {
                    currMethodConcernDescriptor.addInjectedMethod( currInjectedMethodDescriptor );
                }
                else if( currMethodSideEffectDescriptor != null )
                {
                    currMethodSideEffectDescriptor.addInjectedMethod( currInjectedMethodDescriptor );
                }
                else
                {
                    throw new IllegalStateException( "InjectedMethodDescriptor is only valid for "
                                                     + "mixin, object, concern and side-effect." );
                }
            }

            return true;
        }

        @Override
        public boolean visitLeave( Object visited )
            throws RuntimeException
        {
            return true;
        }

        @Override
        public boolean visit( Object visited )
            throws RuntimeException
        {
            if( visited instanceof ConstraintDescriptor )
            {
                if( currCompositeDescriptor == null )
                {
                    // Service via CompositeDescriptor in progress )
                    return false;
                }
                MethodConstraintDetailDescriptor detailDescriptor = new MethodConstraintDetailDescriptor( (ConstraintDescriptor) visited );
                currMethodConstraintsDescriptor.addConstraint( detailDescriptor );
            }
            else if( visited instanceof InjectedFieldDescriptor )
            {
                if( currCompositeDescriptor == null )
                {
                    // Service via CompositeDescriptor in progress )
                    return false;
                }
                InjectedFieldDetailDescriptor detailDescriptor = new InjectedFieldDetailDescriptor( (InjectedFieldDescriptor) visited );

                // Invoked for mixin and object
                if( currActivatorDescriptor != null )
                {
                    currActivatorDescriptor.addInjectedField( detailDescriptor );
                }
                else if( currMixinDescriptor != null )
                {
                    currMixinDescriptor.addInjectedField( detailDescriptor );
                }
                else if( currObjectDescriptor != null )
                {
                    currObjectDescriptor.addInjectedField( detailDescriptor );
                }
                else if( currMethodConcernDescriptor != null )
                {
                    currMethodConcernDescriptor.addInjectedField( detailDescriptor );
                }
                else if( currMethodSideEffectDescriptor != null )
                {
                    currMethodSideEffectDescriptor.addInjectedField( detailDescriptor );
                }
                else
                {
                    throw new IllegalStateException( "InjectedFieldDescriptor is only valid for "
                                                     + "mixin, object, concern and side-effect." );
                }
            }

            return true;
        }

        private LayerDetailDescriptor getLayerDetailDescriptor( LayerDescriptor aDescriptor )
        {
            return layerDescToDetail.computeIfAbsent( aDescriptor, LayerDetailDescriptor::new );
        }

        private void resetInjectableRelatedVariables()
        {
            currMixinDescriptor = null;
            currObjectDescriptor = null;
            currMethodConcernDescriptor = null;
            currMethodSideEffectDescriptor = null;
        }
    }
}
