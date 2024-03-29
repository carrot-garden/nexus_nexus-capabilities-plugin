/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.capabilities.internal.condition;

import static com.google.common.base.Preconditions.checkState;

import org.sonatype.nexus.plugins.capabilities.CapabilityContext;
import org.sonatype.nexus.plugins.capabilities.CapabilityContextAware;
import org.sonatype.nexus.plugins.capabilities.CapabilityEvent;
import org.sonatype.nexus.plugins.capabilities.CapabilityIdentity;
import org.sonatype.nexus.plugins.capabilities.support.condition.ConditionSupport;
import org.sonatype.sisu.goodies.eventbus.EventBus;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

/**
 * A condition that is becoming unsatisfied before an capability is updated and becomes satisfied after capability was
 * updated.
 *
 * @since 2.0
 */
public class PassivateCapabilityDuringUpdateCondition
    extends ConditionSupport
    implements CapabilityContextAware
{

    private CapabilityIdentity id;

    private final String[] propertyNames;

    public PassivateCapabilityDuringUpdateCondition( final EventBus eventBus,
                                                     final String... propertyNames )
    {
        super( eventBus, true );
        this.propertyNames = propertyNames == null || propertyNames.length == 0 ? null : propertyNames;
    }

    @Override
    public PassivateCapabilityDuringUpdateCondition setContext( final CapabilityContext context )
    {
        checkState( !isActive(), "Cannot contextualize when already bounded" );
        checkState( id == null, "Already contextualized with id '" + id + "'" );
        id = context.id();

        return this;
    }

    @Override
    protected void doBind()
    {
        checkState( id != null, "Capability identity not specified" );
        getEventBus().register( this );
    }

    @Override
    public void doRelease()
    {
        getEventBus().unregister( this );
    }

    @AllowConcurrentEvents
    @Subscribe
    public void handle( final CapabilityEvent.BeforeUpdate event )
    {
        if ( event.getReference().context().id().equals( id ) )
        {
            if ( propertyNames == null )
            {
                setSatisfied( false );
            }
            else
            {
                for ( final String propertyName : propertyNames )
                {
                    String oldValue = event.properties().get( propertyName );
                    if ( oldValue == null )
                    {
                        oldValue = "";
                    }
                    String newValue = event.previousProperties().get( propertyName );
                    if ( newValue == null )
                    {
                        newValue = "";
                    }
                    if ( isSatisfied() && !oldValue.equals( newValue ) )
                    {
                        setSatisfied( false );
                    }
                }
            }
        }
    }

    @AllowConcurrentEvents
    @Subscribe
    public void handle( final CapabilityEvent.AfterUpdate event )
    {
        if ( event.getReference().context().id().equals( id ) )
        {
            setSatisfied( true );
        }
    }

    @Override
    public String toString()
    {
        return "Passivate during update of " + id;
    }

    @Override
    public String explainSatisfied()
    {
        return "Capability is currently being updated";
    }

    @Override
    public String explainUnsatisfied()
    {
        return "Capability is not currently being updated";
    }

}
