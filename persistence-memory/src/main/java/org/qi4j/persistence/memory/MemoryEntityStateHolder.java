/*  Copyright 2007 Niclas Hedhman.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.persistence.memory;

import org.qi4j.spi.persistence.EntityStateHolder;
import org.qi4j.api.model.CompositeModel;
import org.qi4j.api.persistence.EntityComposite;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryEntityStateHolder<T extends EntityComposite>
    implements EntityStateHolder<T>
{
    private String identity;
    private CompositeModel<T> model;
    private MemoryPersistentStore memoryPersistentStore;

    private ConcurrentHashMap<Class<?>, Object> mixinStates; // shared

    public MemoryEntityStateHolder( String identity, CompositeModel<T> model, MemoryPersistentStore memoryPersistentStore )
    {
        this.identity = identity;
        this.model = model;
        this.memoryPersistentStore = memoryPersistentStore;
    }

    public String getIdentity()
    {
        return identity;
    }

    public CompositeModel<T> getCompositeModel()
    {
        return model;
    }

    public void refresh()
    {
    }

    public void putMixin( Class mixinType, Object object )
    {
    }

    public Object getMixin( Class mixinType )
    {
        Object mixin = mixinStates.get( mixinType );
        if( mixin == null )
        {
            mixin = memoryPersistentStore.getMixin( this, mixinType );
            mixinStates.put( mixinType, mixin );
        }
        return mixin;
    }
}
