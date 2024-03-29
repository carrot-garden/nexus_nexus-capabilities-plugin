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

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Provider;

import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.plugins.capabilities.Condition;
import org.sonatype.nexus.plugins.capabilities.ConditionEvent;
import org.sonatype.sisu.goodies.eventbus.EventBus;

/**
 * {@link Condition} implementation support.
 *
 * @since 2.0
 */
public abstract class ConditionSupport
    extends AbstractLoggingComponent
    implements Condition
{

    private final Provider<EventBus> eventBusProvider;

    private boolean satisfied;

    private boolean active;

    protected ConditionSupport( final EventBus eventBus )
    {
        this( eventBus, false );
    }

    protected ConditionSupport( final EventBus eventBus, final boolean satisfied )
    {
        this( new Provider<EventBus>()
        {
            @Override
            public EventBus get()
            {
                return eventBus;
            }
        }, satisfied );
    }

    protected ConditionSupport( final Provider<EventBus> eventBusProvider )
    {
        this( eventBusProvider, false );
    }

    protected ConditionSupport( final Provider<EventBus> eventBusProvider, final boolean satisfied )
    {
        this.eventBusProvider = checkNotNull( eventBusProvider );
        this.satisfied = satisfied;
        active = false;
    }

    public EventBus getEventBus()
    {
        return eventBusProvider.get();
    }

    @Override
    public boolean isSatisfied()
    {
        return satisfied;
    }

    public boolean isActive()
    {
        return active;
    }

    @Override
    public final Condition bind()
    {
        if ( !active )
        {
            active = true;
            doBind();
        }
        return this;
    }

    @Override
    public final Condition release()
    {
        if ( active )
        {
            doRelease();
            active = false;
        }
        return this;
    }

    @Override
    public String explainSatisfied()
    {
        return this + " is satisfied";
    }

    @Override
    public String explainUnsatisfied()
    {
        return this + " is not satisfied";
    }

    /**
     * Template method to be implemented by subclasses for doing specific binding.
     */
    protected abstract void doBind();

    /**
     * Template method to be implemented by subclasses for doing specific releasing.
     */
    protected abstract void doRelease();

    /**
     * Sets the satisfied status and if active, notify about this condition being satisfied/unsatisfied.
     *
     * @param satisfied true, if condition is satisfied
     */
    protected void setSatisfied( final boolean satisfied )
    {
        if ( this.satisfied != satisfied )
        {
            this.satisfied = satisfied;
            if ( active )
            {
                if ( this.satisfied )
                {
                    getEventBus().post( new ConditionEvent.Satisfied( this ) );
                }
                else
                {
                    getEventBus().post( new ConditionEvent.Unsatisfied( this ) );
                }
            }
        }
    }

}
