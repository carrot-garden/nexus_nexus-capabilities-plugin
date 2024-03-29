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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Central access point for built in {@link org.sonatype.nexus.plugins.capabilities.Condition}s.
 *
 * @since 2.0
 */
@Named
@Singleton
public class Conditions
{

    private LogicalConditions logicalConditions;

    private CapabilityConditions capabilityConditions;

    private final RepositoryConditions repositoryConditions;

    private final NexusConditions nexusConditions;

    @Inject
    public Conditions( final LogicalConditions logicalConditions,
                       final CapabilityConditions capabilityConditions,
                       final RepositoryConditions repositoryConditions,
                       final NexusConditions nexusConditions )
    {
        this.logicalConditions = checkNotNull( logicalConditions );
        this.capabilityConditions = checkNotNull( capabilityConditions );
        this.repositoryConditions = checkNotNull( repositoryConditions );
        this.nexusConditions = checkNotNull( nexusConditions );
    }

    /**
     * Access to logical conditions.
     *
     * @return logical conditions factory
     */
    public LogicalConditions logical()
    {
        return logicalConditions;
    }

    /**
     * Access to capability related conditions.
     *
     * @return capability related conditions factory
     */
    public CapabilityConditions capabilities()
    {
        return capabilityConditions;
    }

    /**
     * Access to repository specific conditions.
     *
     * @return repository specific conditions factory
     */
    public RepositoryConditions repository()
    {
        return repositoryConditions;
    }

    /**
     * Access to nexus specific conditions.
     *
     * @return nexus specific conditions factory
     */
    public NexusConditions nexus()
    {
        return nexusConditions;
    }

}
