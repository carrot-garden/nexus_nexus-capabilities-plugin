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
package org.sonatype.nexus.plugins.capabilities.testsuite;

import static org.sonatype.nexus.testsuite.support.NexusStartAndStopStrategy.Strategy.EACH_TEST;
import static org.sonatype.nexus.testsuite.support.ParametersLoaders.firstAvailableTestParameters;
import static org.sonatype.nexus.testsuite.support.ParametersLoaders.systemTestParameters;
import static org.sonatype.nexus.testsuite.support.ParametersLoaders.testParameters;
import static org.sonatype.sisu.goodies.common.Varargs.$;

import java.util.Collection;

import org.junit.runners.Parameterized;
import org.sonatype.nexus.bundle.launcher.NexusBundleConfiguration;
import org.sonatype.nexus.capabilities.client.Capabilities;
import org.sonatype.nexus.client.core.subsystem.repository.Repositories;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityPropertyResource;
import org.sonatype.nexus.testsuite.support.NexusRunningParametrizedITSupport;
import org.sonatype.nexus.testsuite.support.NexusStartAndStopStrategy;

@NexusStartAndStopStrategy( EACH_TEST )
public abstract class CapabilitiesITSupport
    extends NexusRunningParametrizedITSupport
{

    @Parameterized.Parameters
    public static Collection<Object[]> data()
    {
        return firstAvailableTestParameters(
            systemTestParameters(),
            testParameters(
                $( "${it.nexus.bundle.groupId}:${it.nexus.bundle.artifactId}:zip:bundle" )
            )
        ).load();
    }

    public CapabilitiesITSupport( final String nexusBundleCoordinates )
    {
        super( nexusBundleCoordinates );
    }

    @Override
    protected NexusBundleConfiguration configureNexus( final NexusBundleConfiguration configuration )
    {
        return configuration
            .setLogPattern( "%d{HH:mm:ss.SSS} %-5level - %msg%n" )
            .setLogLevel( "org.sonatype.nexus.plugins.capabilities", "DEBUG" )
            .setLogLevel( "org.sonatype.sisu.goodies.eventbus", "DEBUG" )
            .setSystemProperty( "guava.eventBus", "default" )
            .addPlugins(
                artifactResolver().resolvePluginFromDependencyManagement(
                    "org.sonatype.nexus.plugins", "nexus-capabilities-plugin"
                ),
                artifactResolver().resolvePluginFromDependencyManagement(
                    "org.sonatype.nexus.capabilities", "nexus-capabilities-testsuite-helper"
                )
            );
    }

    protected Capabilities capabilities()
    {
        return client().getSubsystem( Capabilities.class );
    }

    protected Repositories repositories()
    {
        return client().getSubsystem( Repositories.class );
    }

    protected CapabilityPropertyResource p( final String name, final String value )
    {
        return new CapabilityPropertyResource().withKey( name ).withValue( value );
    }

}
