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
