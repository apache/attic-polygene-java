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

import org.qi4j.runtime.structure.qi.ApplicationModel;
import org.qi4j.runtime.structure.qi.LayerModel;
import org.qi4j.runtime.structure.qi.ModuleModel;

/**
 * TODO
 */
public final class BindingContext
{
    private ApplicationModel application;
    private LayerModel layer;
    private ModuleModel module;
    private CompositeModel compositeModel;

    public BindingContext( ApplicationModel application, LayerModel layer, ModuleModel module, CompositeModel compositeModel )
    {
        this.application = application;
        this.layer = layer;
        this.module = module;
        this.compositeModel = compositeModel;
    }

    public ApplicationModel application()
    {
        return application;
    }

    public LayerModel layer()
    {
        return layer;
    }

    public ModuleModel module()
    {
        return module;
    }

    public CompositeModel composite()
    {
        return compositeModel;
    }
}
