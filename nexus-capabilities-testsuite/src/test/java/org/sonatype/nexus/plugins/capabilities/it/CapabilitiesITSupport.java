/**
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
package org.sonatype.nexus.plugins.capabilities.it;

import static org.sonatype.nexus.bundle.launcher.NexusStartAndStopStrategy.Strategy.EACH_TEST;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Before;
import org.sonatype.nexus.bundle.launcher.NexusBundleConfiguration;
import org.sonatype.nexus.bundle.launcher.NexusRunningITSupport;
import org.sonatype.nexus.bundle.launcher.NexusStartAndStopStrategy;
import org.sonatype.nexus.client.BaseUrl;
import org.sonatype.nexus.client.NexusClient;
import org.sonatype.nexus.client.NexusClientFactory;
import org.sonatype.nexus.client.UsernamePasswordAuthenticationInfo;
import org.sonatype.nexus.plugins.capabilities.client.Capabilities;

@NexusStartAndStopStrategy( EACH_TEST )
public abstract class CapabilitiesITSupport
    extends NexusRunningITSupport
{

    @Inject
    @Named( "${NexusITSupport.capabilitiesPluginCoordinates}" )
    private String capabilitiesPluginCoordinates;

    @Inject
    @Named( "${NexusITSupport.capabilitiesTestsuiteHelperCoordinates}" )
    private String capabilitiesTestsuiteHelperCoordinates;

    protected static final String TEST_REPOSITORY = "releases";

    @Inject
    private NexusClientFactory nexusClientFactory;

    private NexusClient nexusClient;

    @Override
    protected NexusBundleConfiguration configureNexus( final NexusBundleConfiguration configuration )
    {
        return configuration.addPlugins(
            resolveArtifact( capabilitiesPluginCoordinates ),
            resolveArtifact( capabilitiesTestsuiteHelperCoordinates )
        );
    }

    @Before
    public void createNexusClient()
    {
        nexusClient = nexusClientFactory.createFor(
            BaseUrl.create( nexus().getUrl() ),
            new UsernamePasswordAuthenticationInfo( "admin", "admin123" )
        );
    }

    protected Capabilities capabilities()
    {
        return nexusClient.getSubsystem( Capabilities.class );
    }

}