/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.capabilities.internal.condition;

import org.sonatype.nexus.eventbus.NexusEventBus;
import org.sonatype.nexus.plugins.capabilities.CapabilityDescriptorRegistry;
import org.sonatype.nexus.plugins.capabilities.CapabilityEvent;
import org.sonatype.nexus.plugins.capabilities.CapabilityReference;
import org.sonatype.nexus.plugins.capabilities.CapabilityRegistry;
import org.sonatype.nexus.plugins.capabilities.CapabilityType;
import com.google.common.eventbus.Subscribe;

/**
 * A condition that is satisfied when a capability of a specified type exists and is in an active state.
 *
 * @since 2.0
 */
public class CapabilityOfTypeActiveCondition
    extends CapabilityOfTypeExistsCondition
{

    public CapabilityOfTypeActiveCondition( final NexusEventBus eventBus,
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

    @Subscribe
    public void handle( final CapabilityEvent.AfterActivated event )
    {
        if ( !isSatisfied() && type.equals( event.getReference().context().type() ) )
        {
            checkAllCapabilities();
        }
    }

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