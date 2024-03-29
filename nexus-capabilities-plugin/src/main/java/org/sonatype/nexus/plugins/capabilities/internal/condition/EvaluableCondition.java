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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import org.sonatype.nexus.plugins.capabilities.CapabilityContext;
import org.sonatype.nexus.plugins.capabilities.CapabilityContextAware;
import org.sonatype.nexus.plugins.capabilities.CapabilityEvent;
import org.sonatype.nexus.plugins.capabilities.CapabilityIdentity;
import org.sonatype.nexus.plugins.capabilities.Evaluable;
import org.sonatype.nexus.plugins.capabilities.support.condition.ConditionSupport;
import org.sonatype.sisu.goodies.eventbus.EventBus;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

/**
 * A condition that delegates to provided {@link Evaluable} for checking if the condition is satisfied.
 * {@link Evaluable#isSatisfied()} is reevaluated after each update of capability the condition is used for.
 *
 * @since 2.2
 */
public class EvaluableCondition
    extends ConditionSupport
    implements CapabilityContextAware
{

    private CapabilityIdentity capabilityIdentity;

    private final Evaluable evaluable;

    public EvaluableCondition( final EventBus eventBus,
                               final Evaluable evaluable,
                               final CapabilityIdentity capabilityIdentity )
    {
        super( eventBus, evaluable.isSatisfied() );
        this.evaluable = checkNotNull( evaluable );
        this.capabilityIdentity = checkNotNull( capabilityIdentity );
    }

    public EvaluableCondition( final EventBus eventBus,
                               final Evaluable evaluable )
    {
        super( eventBus, false );
        this.evaluable = checkNotNull( evaluable );
    }

    @Override
    public EvaluableCondition setContext( final CapabilityContext context )
    {
        checkState( !isActive(), "Cannot contextualize when already bounded" );
        checkState( capabilityIdentity == null, "Already contextualized with id '" + capabilityIdentity + "'" );
        capabilityIdentity = context.id();

        return this;
    }

    @Override
    protected void doBind()
    {
        checkState( capabilityIdentity != null, "Capability identity not specified" );
        getEventBus().register( this );
        setSatisfied( evaluable.isSatisfied() );
    }

    @Override
    public void doRelease()
    {
        getEventBus().unregister( this );
    }

    @AllowConcurrentEvents
    @Subscribe
    public void handle( final CapabilityEvent.AfterUpdate event )
    {
        if ( event.getReference().context().id().equals( capabilityIdentity ) )
        {
            setSatisfied( evaluable.isSatisfied() );
        }
    }

    @Override
    public String toString()
    {
        return evaluable.toString();
    }

    @Override
    public String explainSatisfied()
    {
        return evaluable.explainSatisfied();
    }

    @Override
    public String explainUnsatisfied()
    {
        return evaluable.explainUnsatisfied();
    }

}
