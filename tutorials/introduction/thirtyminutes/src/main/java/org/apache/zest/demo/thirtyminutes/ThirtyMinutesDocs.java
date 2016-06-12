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
package org.apache.zest.demo.thirtyminutes;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import org.apache.zest.api.common.Optional;
import org.apache.zest.api.concern.ConcernOf;
import org.apache.zest.api.concern.Concerns;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.query.Query;
import org.apache.zest.api.query.QueryBuilderFactory;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.UnitOfWorkFactory;
import org.apache.zest.api.value.ValueBuilder;
import org.apache.zest.api.value.ValueBuilderFactory;
import org.apache.zest.api.value.ValueComposite;
import org.apache.zest.demo.tenminute.Confirmable;
import org.apache.zest.demo.tenminute.HasCustomer;
import org.apache.zest.demo.tenminute.HasLineItems;
import org.apache.zest.demo.tenminute.HasSequenceNumber;
import org.apache.zest.demo.tenminute.InventoryConcern;
import org.apache.zest.demo.tenminute.LineItem;
import org.apache.zest.demo.tenminute.PurchaseLimitConcern;

// START SNIPPET: 6
import static org.apache.zest.api.query.QueryExpressions.eq;
import static org.apache.zest.api.query.QueryExpressions.gt;
import static org.apache.zest.api.query.QueryExpressions.templateFor;

import org.apache.zest.api.query.QueryBuilder;

// END SNIPPET: 6
public class ThirtyMinutesDocs
{
    private QueryBuilderFactory queryBuilderFactory;
    private ValueBuilderFactory valueBuilderFactory;
    private UnitOfWorkFactory unitOfWorkFactory;

// START SNIPPET: 6
    @Structure private UnitOfWorkFactory uowFactory; //Injected
// END SNIPPET: 6

    {
// START SNIPPET: 6
        UnitOfWork uow = uowFactory.currentUnitOfWork();
        QueryBuilder<Order> builder = queryBuilderFactory.newQueryBuilder( Order.class );

        String orderNumber = "12345";
        HasSequenceNumber template = templateFor( HasSequenceNumber.class );
        builder.where( eq( template.number(), orderNumber ) );
        Query<Order> query = uow.newQuery( builder);

        Iterator<Order> result = query.iterator();

        if( result.hasNext() )
        {
            Order order = result.next();
        }
        else
        {
            // Deal with it wasn't found.
        }
// END SNIPPET: 6
    }

    interface Report {
        void addOrderToReport(Order order);
        void addCustomerToReport(String customerName);
    }



    {
        Report report = null;
        UnitOfWork uow = unitOfWorkFactory.currentUnitOfWork();

// START SNIPPET: 7
        QueryBuilder<Order> builder = queryBuilderFactory.newQueryBuilder( Order.class );

        LocalDate last90days = LocalDate.now().minusDays( 90 );
        Order template = templateFor( Order.class );
        builder.where( gt( template.createdDate(), last90days ) );
        Query<Order> query = uow.newQuery(builder);

        for( Order order : query )
        {
            report.addOrderToReport( order );
        }
// END SNIPPET: 7
    }

    {
        Report report = null;
        UnitOfWork uow = unitOfWorkFactory.currentUnitOfWork();

// START SNIPPET: 8
        QueryBuilder<HasCustomer> builder = queryBuilderFactory.newQueryBuilder( HasCustomer.class );

        LocalDate lastMonth = LocalDate.now().minusMonths( 1 );
        Order template1 = templateFor( Order.class );
        builder.where( gt( template1.createdDate(), lastMonth ) );
        Query<HasCustomer> query = uow.newQuery(builder);

        for( HasCustomer hasCustomer : query )
        {
            report.addCustomerToReport( hasCustomer.name().get() );
        }
// END SNIPPET: 8

    }

    public interface Order
    {
        void addLineItem( LineItem item );

        void removeLineItem( LineItem item );

        void completed();

        Property<LocalDate> createdDate();
    }


    // START SNIPPET: 2
    public interface HasAuditTrail<M>
    {
        AuditTrail<M> auditTrail();
    }

    public interface AuditTrail<M> extends Property<List<Action<M>>>
    {}

    public interface Action<T> extends ValueComposite          // [2][3]
    {
        enum Type { added, removed, completed };

        @Optional Property<T> item();                          // [1]

        Property<Type> action();                               // [1]
    }

    public interface Trailable<M>
    {
        void itemAdded( M item );
        void itemRemoved( M item );
        void completed();
    }

    public class TrailableMixin<M>
            implements Trailable<M>
    {
        private @This HasAuditTrail<M> hasTrail;

        @Override
        public void itemAdded( M item )
        {
            addAction( item, Action.Type.added );
        }

        @Override
        public void itemRemoved( M item )
        {
            addAction( item, Action.Type.removed );
        }

        @Override
        public void completed()
        {
            addAction( null, Action.Type.completed );
        }

        private Action<M> addAction( M item, Action.Type type )
        {
            ValueBuilder<Action> builder =
                    valueBuilderFactory.newValueBuilder( Action.class);       // [4]
            Action<M> prototype = builder.prototypeFor( Action.class );
            prototype.item().set( item );
            prototype.action().set( type );
            Action instance = builder.newInstance();
            hasTrail.auditTrail().get().add( instance );
            return instance;
        }
    }
// END SNIPPET: 2

// START SNIPPET: 3
    public abstract class OrderAuditTrailConcern
            extends ConcernOf<Order>
            implements Order
    {
        @This Trailable<LineItem> trail;

        @Override
        public void addLineItem( LineItem item )
        {
            next.addLineItem( item );
            trail.itemAdded( item );
        }

        @Override
        public void removeLineItem( LineItem item )
        {
            next.removeLineItem( item );
            trail.itemRemoved( item );
        }

        @Override
        public void completed()
        {
            next.completed();
            trail.completed();
        }
    }
// END SNIPPET: 3

// START SNIPPET: 4
    public class AuditTrailConcern
            extends ConcernOf<InvocationHandler>
            implements InvocationHandler
    {
        @This Trailable trail;

        @Override
        public Object invoke( Object proxy, Method m, Object[] args )
                throws Throwable
        {
            Object retValue = next.invoke(proxy, m, args);
            String methodName = m.getName();
            if( methodName.startsWith( "add" ) )
            {
                trail.itemAdded( args[0] );
            }
            else if( methodName.startsWith( "remove" ) )
            {
                trail.itemRemoved( args[0] );
            }
            else if( methodName.startsWith( "complete" ) ||
                    methodName.startsWith( "commit" ) )
            {
                trail.completed();
            }

            return retValue;
        }
    }
// END SNIPPET: 4


// START SNIPPET: 5
    @Concerns({
            AuditTrailConcern.class,
            PurchaseLimitConcern.class,
            InventoryConcern.class
    })

    @Mixins( TrailableMixin.class )
    public interface OrderEntity
            extends Order, Confirmable,
            HasSequenceNumber, HasCustomer, HasLineItems,
            EntityComposite
    {
    }
// END SNIPPET: 5
}