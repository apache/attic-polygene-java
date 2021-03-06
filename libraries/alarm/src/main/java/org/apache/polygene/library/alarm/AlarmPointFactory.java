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

package org.apache.polygene.library.alarm;

import org.apache.polygene.api.entity.EntityBuilder;
import org.apache.polygene.api.identity.Identity;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.api.unitofwork.UnitOfWorkFactory;
import org.apache.polygene.api.value.ValueBuilder;
import org.apache.polygene.api.value.ValueBuilderFactory;

@Mixins( AlarmPointFactory.Mixin.class )
public interface AlarmPointFactory
{
    AlarmPoint create(Identity identity, String systemName, String categoryName, AlarmClass alarmClass );

    abstract class Mixin
        implements AlarmPointFactory
    {

        @Structure
        private UnitOfWorkFactory uowf;

        @Structure
        private ValueBuilderFactory vbf;

        @Override
        public AlarmPoint create( Identity identity, String systemName, String categoryName, AlarmClass alarmClass )
        {
            UnitOfWork uow = uowf.currentUnitOfWork();
            EntityBuilder<AlarmPoint> builder = uow.newEntityBuilder( AlarmPoint.class, identity );
            builder.instance().category().set( createCategory( categoryName ) );
            builder.instance().alarmClass().set( alarmClass );

            AlarmPoint.AlarmState prototype = builder.instanceFor( AlarmPoint.AlarmState.class );
            AlarmStatus normal = createNormalAlarmStatus();
            prototype.systemName().set( systemName );
            prototype.currentStatus().set( normal );

            return builder.newInstance();
        }

        private AlarmStatus createNormalAlarmStatus()
        {
            ValueBuilder<AlarmStatus> builder = vbf.newValueBuilder( AlarmStatus.class );
            builder.prototypeFor( AlarmStatus.State.class ).name().set( AlarmPoint.STATUS_NORMAL );
            return builder.newInstance();
        }

        private AlarmCategory createCategory( String categoryName )
        {
            ValueBuilder<AlarmCategory> builder = vbf.newValueBuilder( AlarmCategory.class );
            builder.prototype().name().set( categoryName );
            return builder.newInstance();
        }
    }
}
