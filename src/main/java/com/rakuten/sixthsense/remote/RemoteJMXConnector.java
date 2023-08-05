package com.rakuten.sixthsense.remote;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.net.MalformedURLException;

public class RemoteJMXConnector {

    public RemoteJMXConnector(){


    }

    public MBeanServerConnection getConnection() throws IOException {
        JMXConnector connect = JMXConnectorFactory.connect(
                new JMXServiceURL("service:jmx:rmi:///jndi/rmi://127.0.0.1:1100/jmxrmi"));
        MBeanServerConnection beanServerConnection = connect.getMBeanServerConnection();
        return beanServerConnection;
    }


}
