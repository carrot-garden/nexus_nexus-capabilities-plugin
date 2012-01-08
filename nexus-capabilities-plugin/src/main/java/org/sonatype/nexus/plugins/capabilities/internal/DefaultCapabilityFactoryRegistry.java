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
package org.sonatype.nexus.plugins.capabilities.internal;

import static com.google.common.base.Preconditions.checkArgument;
import static org.sonatype.appcontext.internal.Preconditions.checkNotNull;
import static org.sonatype.nexus.plugins.capabilities.CapabilityType.capabilityType;

import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.plugins.capabilities.CapabilityFactory;
import org.sonatype.nexus.plugins.capabilities.CapabilityFactoryRegistry;
import org.sonatype.nexus.plugins.capabilities.CapabilityType;
import com.google.common.collect.Maps;

/**
 * Default {@link CapabilityFactoryRegistry} implementation.
 *
 * @since 2.0
 */
@Named
@Singleton
class DefaultCapabilityFactoryRegistry
    extends AbstractLoggingComponent
    implements CapabilityFactoryRegistry
{

    private final Map<CapabilityType, CapabilityFactory> factories;

    @Inject
    DefaultCapabilityFactoryRegistry( final Map<String, CapabilityFactory> factories )
    {
        this.factories = Maps.newHashMap();
        if ( factories != null )
        {
            for ( final Map.Entry<String, CapabilityFactory> entry : factories.entrySet() )
            {
                register( capabilityType( entry.getKey() ), entry.getValue() );
            }
        }
    }

    @Override
    public CapabilityFactoryRegistry register( final CapabilityType type, final CapabilityFactory factory )
    {
        checkNotNull( factory );
        checkArgument( !factories.containsKey( type ), "Factory already registered for %s", type );

        factories.put( type, factory );
        getLogger().debug( "Added {} -> {}", type, factory );

        return this;
    }

    @Override
    public CapabilityFactoryRegistry unregister( final CapabilityType type )
    {
        if ( type != null )
        {
            final CapabilityFactory factory = factories.remove( type );
            getLogger().debug( "Removed {} -> {}", type, factory );
        }

        return this;
    }

    @Override
    public CapabilityFactory get( final CapabilityType type )
    {
        return factories.get( type );
    }

}