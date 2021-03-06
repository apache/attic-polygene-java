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
package org.apache.polygene.library.locking;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import org.apache.polygene.api.common.AppliesTo;
import org.apache.polygene.api.concern.ConcernOf;
import org.apache.polygene.api.injection.scope.This;

/**
 * Applies read-lock to Composite
 */
@AppliesTo( ReadLock.class )
public class ReadLockConcern
    extends ConcernOf<InvocationHandler>
    implements InvocationHandler
{
    private
    @This
    ReadWriteLock lock;

    @Override
    public Object invoke( Object o, Method method, Object[] objects )
        throws Throwable
    {
        Lock readLock = lock.readLock();
        lock( readLock );
        try
        {
            return next.invoke( o, method, objects );
        }
        finally
        {
            try
            {
                lock.readLock().unlock();
            }
            catch( Exception e )
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * Fix for this bug:
     * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6822370
     *
     * @param lock the lock to be acquired.
     */
    protected void lock( Lock lock )
    {
        while(true)
        {
            try
            {
                //noinspection StatementWithEmptyBody
                while( !lock.tryLock( 1000, TimeUnit.MILLISECONDS ) )
                {
                    // On timeout, try again
                }
                return; // Finally got a lock
            }
            catch( InterruptedException e )
            {
                // Try again
            }
        }
    }
}
