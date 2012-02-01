/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.runtime.object;

import org.qi4j.functional.HierarchicalVisitor;
import org.qi4j.functional.VisitableHierarchy;

import java.util.List;

/**
 * JAVADOC
 */
public class ObjectsModel
    implements VisitableHierarchy<Object, Object>
{
    private final List<ObjectModel> objectModels;

    public ObjectsModel( List<ObjectModel> objectModels )
    {
        this.objectModels = objectModels;
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( HierarchicalVisitor<? super Object, ? super Object, ThrowableType> visitor ) throws ThrowableType
    {
        if (visitor.visitEnter( this ))
        {
            for( ObjectModel objectModel : objectModels )
            {
                if (!objectModel.accept(visitor))
                    break;
            }
        }
        return visitor.visitLeave( this );
    }

    public Iterable<ObjectModel> models()
    {
        return objectModels;
    }
}