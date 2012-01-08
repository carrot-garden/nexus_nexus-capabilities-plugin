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
package org.sonatype.nexus.plugins.capabilities;

import java.util.Map;

/**
 * Provides access to capability context.
 *
 * @since 2.0
 */
public interface CapabilityContext
{

    /**
     * Returns an unique capability identifier.
     *
     * @return identifier
     */
    CapabilityIdentity id();

    /**
     * Returns type of capability.
     *
     * @return capability type (never null)
     */
    CapabilityType type();

    /**
     * Returns descriptor of capability.
     *
     * @return capability descriptor (never null)
     */
    CapabilityDescriptor descriptor();

    /**
     * Returns capability notes.
     *
     * @return capability notes (can be null)
     */
    String notes();

    /**
     * Current capability properties.
     *
     * @return properties.
     */
    Map<String, String> properties();

    /**
     * Whether the capability is enabled.
     *
     * @return true, if capability is enabled
     */
    boolean isEnabled();

    /**
     * Whether the capability is active.
     *
     * @return true, if capability was activated and not yet passivated
     */
    boolean isActive();

    /**
     * Whether the capability had failed a lifecycle callback method (create/load/update/activate/passivate).
     *
     * @return true, if capability had failed a callback method
     */
    boolean hasFailure();

    /**
     * Last exception thrown during a lifecycle callback method (create/load/update/activate/passivate).
     *
     * @return last exception thrown during a lifecycle callback method or null if it not failed
     */
    Exception failure();

    /**
     * Describe current state.
     *
     * @return state description
     */
    String stateDescription();

}