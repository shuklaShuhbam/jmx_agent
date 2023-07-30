package org.example;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws IOException, MalformedObjectNameException {
        System.out.println("Hello world!");
        JMXConnector connect = JMXConnectorFactory.connect(
                new JMXServiceURL("service:jmx:rmi:///jndi/rmi://127.0.0.1:6006/jmxrmi"));
        MBeanServerConnection beanServerConnection = connect.getMBeanServerConnection();
        Set<ObjectInstance> objectInstances = beanServerConnection.queryMBeans(null, null);
        objectInstances.forEach(objectInstance -> {

            try {
                if (objectInstance.getObjectName().getCanonicalName().contains("artemis")) {
                    MBeanInfo mBeanInfo = beanServerConnection.getMBeanInfo(objectInstance.getObjectName());
                    List<String> list = new ArrayList<>();
                    for (MBeanAttributeInfo attribute : mBeanInfo.getAttributes()) {
                        list.add(attribute.getName());
                    }

                    AttributeList attributes = beanServerConnection.getAttributes(objectInstance.getObjectName(), list.toArray(new String[0]));
                    for (Object attribute : attributes) {
                        Attribute attribute1 = (Attribute) attribute;
                        System.out.println("Name: "+ attribute1.getName());
                        System.out.println("Value: "+ attribute1.getValue());
                    }


                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });


    }
}