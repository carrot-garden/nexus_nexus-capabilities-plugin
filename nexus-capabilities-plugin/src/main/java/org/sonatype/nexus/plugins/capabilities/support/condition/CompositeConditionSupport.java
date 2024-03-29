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
package org.sonatype.nexus.plugins.capabilities.support.condition;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.sonatype.nexus.plugins.capabilities.CapabilityContext;
import org.sonatype.nexus.plugins.capabilities.CapabilityContextAware;
import org.sonatype.nexus.plugins.capabilities.Condition;
import org.sonatype.nexus.plugins.capabilities.ConditionEvent;
import org.sonatype.sisu.goodies.eventbus.EventBus;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

/**
 * Composite {@link Condition} implementation support.
 *
 * @since 2.0
 */
public abstract class CompositeConditionSupport
    extends ConditionSupport
    implements CapabilityContextAware
{

    private final Condition[] conditions;

    public CompositeConditionSupport( final EventBus eventBus,
                                      final Condition... conditions )
    {
        super( eventBus, false );
        this.conditions = checkNotNull( conditions );
        checkArgument( conditions.length > 1, "A composite mush have at least 2 conditions" );
    }

    public CompositeConditionSupport( final EventBus eventBus,
                                      final Condition condition )
    {
        super( eventBus, false );
        this.conditions = new Condition[]{ checkNotNull( condition ) };
    }

    @Override
    protected void doBind()
    {
        for ( final Condition condition : conditions )
        {
            condition.bind();
        }
        getEventBus().register( this );
        setSatisfied( reevaluate( conditions ) );
    }

    @Override
    public void doRelease()
    {
        getEventBus().unregister( this );
        for ( final Condition condition : conditions )
        {
            condition.release();
        }
    }

    @AllowConcurrentEvents
    @Subscribe
    public void handle( final ConditionEvent.Satisfied event )
    {
        if ( shouldReevaluateFor( event.getCondition() ) )
        {
            setSatisfied( reevaluate( conditions ) );
        }
    }

    @AllowConcurrentEvents
    @Subscribe
    public void handle( final ConditionEvent.Unsatisfied event )
    {
        if ( shouldReevaluateFor( event.getCondition() ) )
        {
            setSatisfied( reevaluate( conditions ) );
        }
    }

    @Override
    public CompositeConditionSupport setContext( final CapabilityContext context )
    {
        for ( final Condition condition : conditions )
        {
            if ( condition instanceof CapabilityContextAware )
            {
                ( (CapabilityContextAware) condition ).setContext( context );
            }
        }
        return this;
    }

    @Override
    public String toString()
    {
        return "Re-evaluate " + CompositeConditionSupport.this;
    }

    /**
     * Whether or not the composite conditions are satisfied as a unit.
     *
     * @param conditions to be checked (there are at least 2 conditions passed in)
     * @return true, if conditions are satisfied as a unit
     */
    protected abstract boolean reevaluate( final Condition... conditions );

    protected Condition[] getConditions()
    {
        return conditions;
    }

    private boolean shouldReevaluateFor( final Condition condition )
    {
        for ( final Condition watched : conditions )
        {
            if ( watched == condition )
            {
                return true;
            }
        }
        return false;
    }

}
