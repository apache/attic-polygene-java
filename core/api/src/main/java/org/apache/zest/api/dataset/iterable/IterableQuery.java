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
package org.apache.zest.api.dataset.iterable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import org.apache.zest.api.dataset.Query;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.query.QueryException;
import org.apache.zest.functional.Iterables;
import org.apache.zest.functional.Visitor;

/**
 * TODO
 */
public class IterableQuery<T> implements Query<T>
{
    private Iterable<T> iterable;
    private int skip;
    private int limit;
    private Map<String, Object> variables = new HashMap<String, Object>();

    public IterableQuery( Iterable<T> iterable )
    {
        this.iterable = iterable;
    }

    @Override
    public Query filter( Predicate<T> filter )
    {
        iterable = Iterables.filter( filter, iterable );

        return this;
    }

    @Override
    public Query orderBy( Property<?> property, Order order )
    {
        return this;
    }

    @Override
    public Query skip( int skipNrOfResults )
    {
        this.skip = skipNrOfResults;

        return this;
    }

    @Override
    public Query limit( int maxNrOfResults )
    {
        this.limit = maxNrOfResults;
        return this;
    }

    @Override
    public Query<T> setVariable( String name, Object value )
    {
        variables.put( name, value );
        return this;
    }

    @Override
    public Object getVariable( String name )
    {
        return variables.get( name );
    }

    @Override
    public long count()
    {
        return Iterables.count( Iterables.limit( limit, Iterables.skip( skip, iterable ) ) );
    }

    @Override
    public T first()
    {
        return Iterables.first( Iterables.limit( limit, Iterables.skip( skip, iterable ) ) );
    }

    @Override
    public T single()
        throws QueryException
    {
        return Iterables.single( Iterables.limit( limit, Iterables.skip( skip, iterable ) ) );
    }

    @Override
    public <ThrowableType extends Throwable> boolean execute( Visitor<T, ThrowableType> resultVisitor )
        throws ThrowableType
    {
        for( T t : toIterable() )
        {
            if( !resultVisitor.visit( t ) )
            {
                return false;
            }
        }

        return true;
    }

    @Override
    public Iterable<T> toIterable()
        throws QueryException
    {
        return Iterables.limit( limit, Iterables.skip( skip, iterable ) );
    }
}
