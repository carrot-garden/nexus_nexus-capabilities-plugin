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

import org.sonatype.sisu.goodies.eventbus.EventBus;
import org.sonatype.nexus.plugins.capabilities.CapabilityDescriptorRegistry;
import org.sonatype.nexus.plugins.capabilities.CapabilityEvent;
import org.sonatype.nexus.plugins.capabilities.CapabilityReference;
import org.sonatype.nexus.plugins.capabilities.CapabilityRegistry;
import org.sonatype.nexus.plugins.capabilities.CapabilityType;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

/**
 * A condition that is satisfied when a capability of a specified type exists and is in an active state.
 *
 * @since 2.0
 */
public class CapabilityOfTypeActiveCondition
    extends CapabilityOfTypeExistsCondition
{

    public CapabilityOfTypeActiveCondition( final EventBus eventBus,
                                            final CapabilityDescriptorRegistry descriptorRegistry,
                                            final CapabilityRegistry capabilityRegistry,
                                            final CapabilityType type )
    {
        super( eventBus, descriptorRegistry, capabilityRegistry, type );
    }

    @Override
    boolean isSatisfiedBy( final CapabilityReference reference )
    {
        return super.isSatisfiedBy( reference ) && reference.context().isActive();
    }

    @AllowConcurrentEvents
    @Subscribe
    public void handle( final CapabilityEvent.AfterActivated event )
    {
        if ( !isSatisfied() && type.equals( event.getReference().context().type() ) )
        {
            checkAllCapabilities();
        }
    }

    @AllowConcurrentEvents
    @Subscribe
    public void handle( final CapabilityEvent.BeforePassivated event )
    {
        if ( isSatisfied() && type.equals( event.getReference().context().type() ) )
        {
            checkAllCapabilities();
        }
    }

    @Override
    public String toString()
    {
        return "Active " + type;
    }

    @Override
    public String explainSatisfied()
    {
        return typeName + " is active";
    }

    @Override
    public String explainUnsatisfied()
    {
        return typeName + " is not active";
    }

}
