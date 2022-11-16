package com.facebook.presto.features.config;

import com.facebook.presto.plugin.base.security.ForwardingConnectorAccessControl;
import com.facebook.presto.spi.connector.ConnectorAccessControl;
import org.testng.annotations.Test;

import static com.facebook.presto.spi.testing.InterfaceTestUtils.assertAllMethodsOverridden;

public class ForwardingFeaturesConfigurationTest
{
    @Test
    public void testEverythingDelegated()
    {
        assertAllMethodsOverridden(ConnectorAccessControl.class, ForwardingConnectorAccessControl.class);
    }
}
