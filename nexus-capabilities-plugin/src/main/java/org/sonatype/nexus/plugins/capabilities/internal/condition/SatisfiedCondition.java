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

import org.sonatype.nexus.plugins.capabilities.Condition;

/**
 * A condition that is always satisfied.
 *
 * @since 2.0
 */
public class SatisfiedCondition
    implements Condition
{

    private final String reason;

    public SatisfiedCondition( final String reason )
    {
        this.reason = reason;
    }

    @Override
    public boolean isSatisfied()
    {
        return true;
    }

    @Override
    public SatisfiedCondition bind()
    {
        // do nothing
        return this;
    }

    @Override
    public SatisfiedCondition release()
    {
        // do nothing
        return this;
    }

    @Override
    public String toString()
    {
        return reason;
    }

    @Override
    public String explainSatisfied()
    {
        return reason;
    }

    @Override
    public String explainUnsatisfied()
    {
        return "Not " + reason;
    }

}