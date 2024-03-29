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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonatype.nexus.capabilities.client.Capability;
import org.sonatype.nexus.client.core.exception.NexusClientNotFoundException;
import org.sonatype.nexus.client.core.subsystem.repository.maven.MavenHostedRepository;
import org.sonatype.nexus.client.core.subsystem.repository.maven.MavenProxyRepository;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityPropertyResource;

public class ConditionsIT
    extends CapabilitiesITSupport
{

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    public ConditionsIT( final String nexusBundleCoordinates )
    {
        super( nexusBundleCoordinates );
    }

    /**
     * Verify that capability is initially active when created for a repository that is in service
     * Verify that capability becomes inactive when repository is put out of service
     * Verify that capability becomes active when repository is put back in service
     */
    @Test
    public void repositoryInService()
    {
        final String rId = repositoryIdForTest();

        final MavenHostedRepository repository = repositories().create( MavenHostedRepository.class, rId )
            .excludeFromSearchResults()
            .save();

        Capability capability = capabilities().create( "[repositoryIsInService]" )
            .withProperty( "repository", rId )
            .save();
        assertThat( capability.isActive(), is( true ) );

        logRemote( "Put repository '{}' out of service", rId );
        repository.putOutOfService();
        capability.refresh();
        assertThat( capability.isActive(), is( false ) );

        logRemote( "Put repository '{}' back in service", rId );
        repository.putInService();
        capability.refresh();
        assertThat( capability.isActive(), is( true ) );
    }

    /**
     * Verify that capability is initially inactive when created for a repository that is out of service
     * Verify that capability becomes active when repository is put back in service
     */
    @Test
    public void repositoryOutOfService()
    {
        final String rId = repositoryIdForTest();

        final MavenHostedRepository repository = repositories().create( MavenHostedRepository.class, rId )
            .excludeFromSearchResults()
            .save()
            .putOutOfService();

        Capability capability = capabilities().create( "[repositoryIsInService]" )
            .withProperty( "repository", rId )
            .save();
        assertThat( capability.isActive(), is( false ) );

        logRemote( "Put repository '{}' back in service", rId );
        repository.putInService();
        capability.refresh();
        assertThat( capability.isActive(), is( true ) );
    }

    /**
     * Verify that capability becomes inactive when repository is changed and the new repository is out of service
     */
    @Test
    public void changeRepositoryToAnOutOfServiceOne()
    {
        final String rIdActive = repositoryIdForTest( "active" );
        final String rIdInactive = repositoryIdForTest( "inactive" );

        repositories().create( MavenHostedRepository.class, rIdActive )
            .excludeFromSearchResults()
            .save();
        repositories().create( MavenHostedRepository.class, rIdInactive )
            .excludeFromSearchResults()
            .save().putOutOfService();

        Capability capability = capabilities().create( "[repositoryIsInService]" )
            .withProperty( "repository", rIdActive )
            .save();
        assertThat( capability.isActive(), is( true ) );

        logRemote( "Change capability to use repository '{}'", rIdInactive );
        capability.withProperty( "repository", rIdInactive ).save();
        assertThat( capability.isActive(), is( false ) );
    }

    /**
     * Verify that capability is initially active when created for a repository that is not blocked
     * Verify that capability becomes inactive when repository is manually blocked
     * Verify that capability becomes active when repository is unblocked
     */
    @Test
    public void repositoryNotBlocked()
    {
        final String rId = repositoryIdForTest();

        final MavenProxyRepository repository = repositories().create( MavenProxyRepository.class, rId )
            .asProxyOf( repositories().get( "releases" ).contentUri() )
            .save();

        Capability capability = capabilities().create( "[repositoryIsNotBlocked]" )
            .withProperty( "repository", rId )
            .save();
        assertThat( capability.isActive(), is( true ) );

        logRemote( "Block repository '{}'", rId );
        repository.block();
        capability.refresh();
        assertThat( capability.isActive(), is( false ) );

        logRemote( "Unblock repository '{}'", rId );
        repository.unblock();
        capability.refresh();
        assertThat( capability.isActive(), is( true ) );
    }

    /**
     * Verify that capability is initially inactive when created for a repository that is blocked
     * Verify that capability becomes active when repository is unblocked
     */
    @Test
    public void repositoryBlocked()
    {
        final String rId = repositoryIdForTest();

        final MavenProxyRepository repository = repositories().create( MavenProxyRepository.class, rId )
            .asProxyOf( repositories().get( "releases" ).contentUri() )
            .save()
            .block();

        Capability capability = capabilities().create( "[repositoryIsNotBlocked]" )
            .withProperty( "repository", rId )
            .save();
        assertThat( capability.isActive(), is( false ) );

        logRemote( "Unblock repository '{}'", rId );
        repository.unblock();
        capability.refresh();
        assertThat( capability.isActive(), is( true ) );
    }

    /**
     * Verify that capability becomes inactive when repository is changed and the new repository is blocked
     */
    @Test
    public void changeRepositoryToABlockedOne()
    {
        final String rIdNotBlocked = repositoryIdForTest( "notBlocked" );
        final String rIdBlocked = repositoryIdForTest( "blocked" );

        repositories().create( MavenProxyRepository.class, rIdNotBlocked )
            .asProxyOf( repositories().get( "releases" ).contentUri() )
            .save();
        repositories().create( MavenProxyRepository.class, rIdBlocked )
            .asProxyOf( repositories().get( "releases" ).contentUri() )
            .save()
            .block();

        Capability capability = capabilities().create( "[repositoryIsNotBlocked]" )
            .withProperty( "repository", rIdNotBlocked )
            .save();
        assertThat( capability.isActive(), is( true ) );

        logRemote( "Change capability to use repository '{}'", rIdBlocked );
        capability.withProperty( "repository", rIdBlocked ).save();
        assertThat( capability.isActive(), is( false ) );
    }

    /**
     * Verify that a capability is automatically removed when configured repository is removed.
     */
    @Test
    public void capabilityRemovedWhenRepositoryRemoved()
    {
        final String rId = repositoryIdForTest();

        final MavenHostedRepository repository = repositories().create( MavenHostedRepository.class, rId )
            .excludeFromSearchResults()
            .save()
            .putOutOfService();

        Capability capability = capabilities().create( "[repositoryIsInService]" )
            .withProperty( "repository", rId )
            .save();
        assertThat( capability.isActive(), is( false ) );

        logRemote( "Remove repository '{}'", rId );
        repository.remove();

        thrown.expect( NexusClientNotFoundException.class );
        thrown.expectMessage( String.format( "Capability with id '%s' was not found", capability.id() ) );
        capability.refresh();
    }

    /**
     * Verify that a capability, that has an activation condition that another capability of a specific type exists,
     * becomes active/inactive depending on existence of that another capability.
     */
    @Test
    public void capabilityOfTypeExists()
    {
        Capability capability = capabilities().create( "[capabilityOfTypeExists]" )
            .save();
        assertThat( capability.isActive(), is( false ) );

        logRemote( "Create a capability of type [message]" );
        final Capability messageCapability = capabilities().create( "[message]" )
            .withProperty( "repository", "releases" )
            .save();
        capability.refresh();
        assertThat( capability.isActive(), is( true ) );

        logRemote( "Remove capability of type [message]" );
        messageCapability.remove();
        capability.refresh();
        assertThat( capability.isActive(), is( false ) );
    }

    @Test
    public void capabilityOfTypeIsActive()
    {
        Capability capability = capabilities().create( "[capabilityOfTypeActive]" )
            .save();
        assertThat( capability.isActive(), is( false ) );

        logRemote( "Create a capability of type [message]" );
        final Capability messageCapability = capabilities().create( "[message]" )
            .withProperty( "repository", "releases" )
            .save();

        capability.refresh();
        assertThat( capability.isActive(), is( true ) );

        logRemote( "Disable capability of type [message]" );
        messageCapability.disable();
        capability.refresh();
        assertThat( capability.isActive(), is( false ) );

        logRemote( "Enable capability of type [message]" );
        messageCapability.enable();
        capability.refresh();
        assertThat( capability.isActive(), is( true ) );

        logRemote( "Remove capability of type [message]" );
        messageCapability.remove();
        capability.refresh();
        assertThat( capability.isActive(), is( false ) );
    }

    private void logRemote( final String message, Object... params )
    {
        remoteLogger().info( "\n***************** " + message + " *****************", params );
    }

    private CapabilityPropertyResource repository( final String repositoryId )
    {
        return p( "repository", repositoryId );
    }

}
