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
package org.apache.polygene.runtime.structure;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import org.apache.polygene.api.activation.Activation;
import org.apache.polygene.api.activation.ActivationEventListener;
import org.apache.polygene.api.activation.ActivationException;
import org.apache.polygene.api.activation.PassivationException;
import org.apache.polygene.api.association.AssociationDescriptor;
import org.apache.polygene.api.common.ConstructionException;
import org.apache.polygene.api.composite.Composite;
import org.apache.polygene.api.composite.ModelDescriptor;
import org.apache.polygene.api.composite.NoSuchTransientException;
import org.apache.polygene.api.composite.TransientBuilder;
import org.apache.polygene.api.composite.TransientBuilderFactory;
import org.apache.polygene.api.composite.TransientDescriptor;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.identity.IdentityGenerator;
import org.apache.polygene.api.metrics.MetricsProvider;
import org.apache.polygene.api.object.NoSuchObjectException;
import org.apache.polygene.api.object.ObjectDescriptor;
import org.apache.polygene.api.object.ObjectFactory;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.property.PropertyDescriptor;
import org.apache.polygene.api.query.QueryBuilder;
import org.apache.polygene.api.query.QueryBuilderFactory;
import org.apache.polygene.api.service.NoSuchServiceException;
import org.apache.polygene.api.service.ServiceFinder;
import org.apache.polygene.api.service.ServiceReference;
import org.apache.polygene.api.structure.LayerDescriptor;
import org.apache.polygene.api.structure.Module;
import org.apache.polygene.api.structure.ModuleDescriptor;
import org.apache.polygene.api.structure.TypeLookup;
import org.apache.polygene.api.type.HasTypes;
import org.apache.polygene.api.unitofwork.UnitOfWorkException;
import org.apache.polygene.api.unitofwork.UnitOfWorkFactory;
import org.apache.polygene.api.util.NullArgumentException;
import org.apache.polygene.api.value.NoSuchValueException;
import org.apache.polygene.api.value.ValueBuilder;
import org.apache.polygene.api.value.ValueBuilderFactory;
import org.apache.polygene.api.value.ValueComposite;
import org.apache.polygene.api.value.ValueDescriptor;
import org.apache.polygene.api.value.ValueSerialization;
import org.apache.polygene.api.value.ValueSerializationException;
import org.apache.polygene.runtime.activation.ActivationDelegate;
import org.apache.polygene.runtime.composite.FunctionStateResolver;
import org.apache.polygene.runtime.composite.StateResolver;
import org.apache.polygene.runtime.composite.TransientBuilderInstance;
import org.apache.polygene.runtime.composite.TransientStateInstance;
import org.apache.polygene.runtime.composite.UsesInstance;
import org.apache.polygene.runtime.injection.InjectionContext;
import org.apache.polygene.runtime.object.ObjectModel;
import org.apache.polygene.runtime.property.PropertyInstance;
import org.apache.polygene.runtime.property.PropertyModel;
import org.apache.polygene.runtime.query.QueryBuilderFactoryImpl;
import org.apache.polygene.runtime.service.ImportedServicesInstance;
import org.apache.polygene.runtime.service.ImportedServicesModel;
import org.apache.polygene.runtime.service.ServicesInstance;
import org.apache.polygene.runtime.service.ServicesModel;
import org.apache.polygene.runtime.value.ValueBuilderInstance;
import org.apache.polygene.runtime.value.ValueBuilderWithPrototype;
import org.apache.polygene.runtime.value.ValueBuilderWithState;
import org.apache.polygene.runtime.value.ValueInstance;
import org.apache.polygene.spi.entitystore.EntityStore;
import org.apache.polygene.spi.metrics.MetricsProviderAdapter;
import org.apache.polygene.spi.module.ModuleSpi;

import static java.util.Arrays.asList;
import static java.util.stream.Stream.concat;

/**
 * Instance of a Polygene Module. Contains the various composites for this Module.
 */
public class ModuleInstance
    implements Module, ModuleSpi, Activation
{
    // Constructor parameters
    private final ModuleModel model;
    private final LayerDescriptor layer;
    private final TypeLookup typeLookup;
    private final ServicesInstance services;
    private final ImportedServicesInstance importedServices;
    // Eager instance objects
    private final ActivationDelegate activation;
    private final QueryBuilderFactory queryBuilderFactory;
    // Lazy assigned on accessors
    private EntityStore store;
    private IdentityGenerator generator;
    private ValueSerialization valueSerialization;
    private MetricsProvider metrics;
    private UnitOfWorkFactory uowf;

    @SuppressWarnings( "LeakingThisInConstructor" )
    public ModuleInstance( ModuleModel moduleModel, LayerDescriptor layer, TypeLookup typeLookup,
                           ServicesModel servicesModel, ImportedServicesModel importedServicesModel
    )
    {
        // Constructor parameters
        model = moduleModel;
        this.layer = layer;
        this.typeLookup = typeLookup;
        services = servicesModel.newInstance( moduleModel );
        importedServices = importedServicesModel.newInstance( moduleModel );

        // Eager instance objects
        activation = new ActivationDelegate( this );
        queryBuilderFactory = new QueryBuilderFactoryImpl( this );

        // Activation
        services.registerActivationEventListener( activation );
        importedServices.registerActivationEventListener( activation );
    }

    @Override
    public String toString()
    {
        return model.toString();
    }

    @Override
    public ModuleDescriptor descriptor()
    {
        return model;
    }

    // Implementation of Module
    @Override
    public String name()
    {
        return model.name();
    }

    // Implementation of MetaInfoHolder
    @Override
    public <T> T metaInfo( Class<T> infoType )
    {
        return model.metaInfo( infoType );
    }

    // Implementation of ObjectFactory
    @Override
    public <T> T newObject( Class<T> mixinType, Object... uses )
        throws NoSuchObjectException
    {
        NullArgumentException.validateNotNull( "mixinType", mixinType );
        ObjectDescriptor model = typeLookup.lookupObjectModel( mixinType );

        if( model == null )
        {
            throw new NoSuchObjectException( mixinType.getName(), name(),
                                             typeLookup.allObjects().flatMap( HasTypes::types ) );
        }

        InjectionContext injectionContext = new InjectionContext( model.module(), UsesInstance.EMPTY_USES.use( uses ) );
        return mixinType.cast( ( (ObjectModel) model ).newInstance( injectionContext ) );
    }

    @Override
    public void injectTo( Object instance, Object... uses )
        throws ConstructionException
    {
        NullArgumentException.validateNotNull( "instance", instance );
        ObjectDescriptor model = typeLookup.lookupObjectModel( instance.getClass() );

        if( model == null )
        {
            throw new NoSuchObjectException( instance.getClass().getName(), name(),
                                             typeLookup.allObjects().flatMap( HasTypes::types ) );
        }

        InjectionContext injectionContext = new InjectionContext( model.module(), UsesInstance.EMPTY_USES.use( uses ) );
        ( (ObjectModel) model ).inject( injectionContext, instance );
    }

    // Implementation of TransientBuilderFactory
    @Override
    public <T> TransientBuilder<T> newTransientBuilder( Class<T> mixinType )
        throws NoSuchTransientException
    {
        NullArgumentException.validateNotNull( "mixinType", mixinType );
        TransientDescriptor model = typeLookup.lookupTransientModel( mixinType );

        if( model == null )
        {
            throw new NoSuchTransientException( mixinType.getName(), name(), typeLookup );
        }

        Map<AccessibleObject, Property<?>> properties = new HashMap<>();
        model.state().properties().forEach(
            propertyModel ->
            {
                Object initialValue = propertyModel.resolveInitialValue( model.module() );
                Property<?> property = new PropertyInstance<>( ( (PropertyModel) propertyModel ).getBuilderInfo(),
                                                               initialValue );
                properties.put( propertyModel.accessor(), property );
            } );

        TransientStateInstance state = new TransientStateInstance( properties );

        return new TransientBuilderInstance<>( model, state, UsesInstance.EMPTY_USES );
    }

    @Override
    public <T> T newTransient( final Class<T> mixinType, Object... uses )
        throws NoSuchTransientException, ConstructionException
    {
        return newTransientBuilder( mixinType ).use( uses ).newInstance();
    }

    // Implementation of ValueBuilderFactory
    @Override
    public <T> T newValue( Class<T> mixinType )
        throws NoSuchValueException, ConstructionException
    {
        return newValueBuilder( mixinType ).newInstance();
    }

    @Override
    public <T> ValueBuilder<T> newValueBuilder( Class<T> mixinType )
        throws NoSuchValueException
    {
        NullArgumentException.validateNotNull( "mixinType", mixinType );
        ValueDescriptor compositeModelModule = typeLookup.lookupValueModel( mixinType );

        if( compositeModelModule == null )
        {
            throw new NoSuchValueException( mixinType.getName(), name(), typeLookup );
        }

        StateResolver stateResolver = new InitialStateResolver( compositeModelModule.module() );
        return new ValueBuilderInstance<>( compositeModelModule, this, stateResolver );
    }

    @Override
    public <T> ValueBuilder<T> newValueBuilderWithState( Class<T> mixinType,
                                                         Function<PropertyDescriptor, Object> propertyFunction,
                                                         Function<AssociationDescriptor, EntityReference> associationFunction,
                                                         Function<AssociationDescriptor, Stream<EntityReference>> manyAssociationFunction,
                                                         Function<AssociationDescriptor, Stream<Map.Entry<String, EntityReference>>> namedAssociationFunction
    )
    {
        NullArgumentException.validateNotNull( "propertyFunction", propertyFunction );
        NullArgumentException.validateNotNull( "associationFunction", associationFunction );
        NullArgumentException.validateNotNull( "manyAssociationFunction", manyAssociationFunction );
        NullArgumentException.validateNotNull( "namedAssociationFunction", namedAssociationFunction );

        ValueDescriptor compositeModelModule = typeLookup.lookupValueModel( mixinType );

        if( compositeModelModule == null )
        {
            throw new NoSuchValueException( mixinType.getName(), name(), typeLookup );
        }

        StateResolver stateResolver = new FunctionStateResolver(
            propertyFunction, associationFunction, manyAssociationFunction, namedAssociationFunction
        );
        return new ValueBuilderWithState<>( compositeModelModule, this, stateResolver );
    }

    private static class InitialStateResolver
        implements StateResolver
    {
        private final ModuleDescriptor module;

        private InitialStateResolver( ModuleDescriptor module )
        {
            this.module = module;
        }

        @Override
        public Object getPropertyState( PropertyDescriptor propertyDescriptor )
        {
            return propertyDescriptor.resolveInitialValue(module);
        }

        @Override
        public EntityReference getAssociationState( AssociationDescriptor associationDescriptor )
        {
            return null;
        }

        @Override
        public Stream<EntityReference> getManyAssociationState( AssociationDescriptor associationDescriptor )
        {
            return new ArrayList<EntityReference>().stream();
        }

        @Override
        public Stream<Map.Entry<String, EntityReference>> getNamedAssociationState( AssociationDescriptor associationDescriptor )
        {
            return new HashMap<String, EntityReference>().entrySet().stream();
        }
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T> ValueBuilder<T> newValueBuilderWithPrototype( T prototype )
    {
        NullArgumentException.validateNotNull( "prototype", prototype );

        ValueInstance valueInstance = ValueInstance.valueInstanceOf( (ValueComposite) prototype );
        Class<Composite> valueType = (Class<Composite>) valueInstance.types().findFirst().orElse( null );

        ValueDescriptor model = typeLookup.lookupValueModel( valueType );

        if( model == null )
        {
            throw new NoSuchValueException( valueType.getName(), name(), typeLookup );
        }

        return new ValueBuilderWithPrototype<>( model, this, prototype );
    }

    @Override
    public <T> T newValueFromSerializedState( Class<T> mixinType, String serializedState )
        throws NoSuchValueException, ConstructionException
    {
        NullArgumentException.validateNotNull( "mixinType", mixinType );
        ValueDescriptor model = typeLookup.lookupValueModel( mixinType );

        if( model == null )
        {
            throw new NoSuchValueException( mixinType.getName(), name(), typeLookup );
        }

        try
        {
            return valueSerialization().deserialize( model.module(), model.valueType(), serializedState );
        }
        catch( ValueSerializationException ex )
        {
            throw new ConstructionException( "Could not create value from serialized state", ex );
        }
    }

    // Implementation of QueryBuilderFactory
    @Override
    public <T> QueryBuilder<T> newQueryBuilder( final Class<T> resultType )
    {
        return queryBuilderFactory.newQueryBuilder( resultType );
    }

    @Override
    public <T> ServiceReference<T> findService( Class<T> serviceType )
        throws NoSuchServiceException
    {
        return findService( (Type) serviceType );
    }

    @Override
    public <T> ServiceReference<T> findService( Type serviceType )
    {
        ModelDescriptor serviceModel = typeLookup.lookupServiceModel( serviceType );
        if( serviceModel == null )
        {
            throw new NoSuchServiceException( serviceType.getTypeName(), name(),typeLookup );
        }
        return findServiceReferenceInstance( serviceModel );
    }

    @Override
    public <T> Stream<ServiceReference<T>> findServices( final Class<T> serviceType )
    {
        return findServices( (Type) serviceType );
    }

    @Override
    public <T> Stream<ServiceReference<T>> findServices( final Type serviceType )
    {
        List<? extends ModelDescriptor> serviceModels = typeLookup.lookupServiceModels( serviceType );
        if( serviceModels == null )
        {
            return Stream.empty();
        }
        //noinspection unchecked
        return serviceModels.stream()
                            .map( this::findServiceReferenceInstance )
                            .filter( Objects::nonNull )
                            .filter( ref -> ref.hasType( serviceType ) )
                            .map( ref -> (ServiceReference<T>) ref );
    }

    private <T> ServiceReference<T> findServiceReferenceInstance( ModelDescriptor model )
    {
        ModuleInstance moduleInstanceOfModel = (ModuleInstance) model.module().instance();
        Optional<ServiceReference<?>> candidate =
            concat( moduleInstanceOfModel.services.references(), moduleInstanceOfModel.importedServices.references() )
                .filter( ref -> ref.model().equals( model ) )
                .findAny();
        if( candidate.isPresent() )
        {
            ServiceReference<?> serviceReference = candidate.get();
            return (ServiceReference<T>) serviceReference;
        }
        return null;
    }

    // Implementation of Activation
    @Override
    @SuppressWarnings( "unchecked" )
    public void activate()
        throws ActivationException
    {
        activation.activate( model.newActivatorsInstance(), asList( services, importedServices ) );
    }

    @Override
    public void passivate()
        throws PassivationException
    {
        activation.passivate();
    }

    @Override
    public void registerActivationEventListener( ActivationEventListener listener )
    {
        activation.registerActivationEventListener( listener );
    }

    @Override
    public void deregisterActivationEventListener( ActivationEventListener listener )
    {
        activation.deregisterActivationEventListener( listener );
    }

    // Other methods
    ModuleModel model()
    {
        return model;
    }

    public LayerDescriptor layer()
    {
        return layer;
    }

    @Override
    public TypeLookup typeLookup()
    {
        return typeLookup;
    }

    public EntityStore entityStore()
    {
        if( store == null )
        {
            synchronized( this )
            {
                if( store == null )
                {
                    try
                    {
                        ServiceReference<EntityStore> service = findService( EntityStore.class );
                        store = service.get();
                    }
                    catch( NoSuchServiceException e )
                    {
                        throw new UnitOfWorkException( "No EntityStore service available in module " + name() );
                    }
                }
            }
        }
        return store;
    }

    public UnitOfWorkFactory unitOfWorkFactory()
    {
        if( uowf == null )
        {
            synchronized( this )
            {
                if( uowf == null )
                {
                    try
                    {
                        ServiceReference<UnitOfWorkFactory> service = findService( UnitOfWorkFactory.class );
                        uowf = service.get();
                    }
                    catch( NoSuchServiceException e )
                    {
                        throw new UnitOfWorkException( "No UnitOfWorkFactory service available in module " + name() );
                    }
                }
            }
        }
        return uowf;
    }

    @Override
    public ServiceFinder serviceFinder()
    {
        return this;
    }

    @Override
    public ValueBuilderFactory valueBuilderFactory()
    {
        return this;
    }

    @Override
    public TransientBuilderFactory transientBuilderFactory()
    {
        return this;
    }

    @Override
    public ObjectFactory objectFactory()
    {
        return this;
    }

    public IdentityGenerator identityGenerator()
    {
        if( generator == null )
        {
            synchronized( this )
            {
                if( generator == null )
                {
                    ServiceReference<IdentityGenerator> service = findService( IdentityGenerator.class );
                    generator = service.get();
                }
            }
        }
        return generator;
    }

    public ValueSerialization valueSerialization()
    {
        if( valueSerialization == null )
        {
            synchronized( this )
            {
                if( valueSerialization == null )
                {
                    try
                    {
                        ServiceReference<ValueSerialization> service = findService( ValueSerialization.class );
                        valueSerialization = service.get();
                    }
                    catch( NoSuchServiceException e )
                    {
                        throw new ValueSerializationException( "No ValueSeriaservice available in module " + name() );
                    }
                }
            }
        }
        return valueSerialization;
    }

    public MetricsProvider metricsProvider()
    {
        if( metrics == null )
        {
            synchronized( this )
            {
                if( metrics == null )
                {
                    try
                    {
                        ServiceReference<MetricsProvider> service = findService( MetricsProvider.class );
                        metrics = service.get();
                    }
                    catch( NoSuchServiceException e )
                    {
                        metrics = new MetricsProviderAdapter();
                    }
                }
            }
        }
        return metrics;
    }
}
