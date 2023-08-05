package org.example;

import javax.management.*;
import java.util.*;

import com.rakuten.jmx.artemis.ArtemisBrokerMetricsScraper;
import com.rakuten.jmx.artemis.ArtemisQueueMetricsScraper;
import com.rakuten.sixthsense.remote.ExportMetrics;
import com.rakuten.sixthsense.remote.RemoteJMXConnector;
import org.apache.activemq.artemis.api.core.management.ActiveMQServerControl;
import org.apache.skywalking.apm.network.language.agent.v3.MeterData;


public class SixthSenseJmxApplication {
    public static void main(String[] args) throws Exception {
        RemoteJMXConnector remoteJMXConnector = new RemoteJMXConnector();
        MBeanServerConnection beanServerConnection = remoteJMXConnector.getConnection();
        ArtemisBrokerMetricsScraper brokerMetricsScraper = new ArtemisBrokerMetricsScraper(beanServerConnection);
        ArtemisQueueMetricsScraper queueMetricsScraper = new ArtemisQueueMetricsScraper(beanServerConnection);
        while(true) {
            System.out.println("Hello world!");

            String[] domain = beanServerConnection.getDomains();
            ObjectName brokerObjectName = new ObjectName("org.apache.activemq.artemis:broker=*");
            ObjectName addressObjectName = new ObjectName("org.apache.activemq.artemis:broker=*,component=addresses,address=*");
            ObjectName queueObjectName = new ObjectName("org.apache.activemq.artemis:broker=*,component=addresses,address=*,subcomponent=queues,routing-type=*,queue=*");

            Set<ObjectInstance> brokerMbeans = beanServerConnection.queryMBeans(brokerObjectName, null);
            Set<ObjectInstance> addressMbeans = beanServerConnection.queryMBeans(addressObjectName, null);
            Set<ObjectInstance> queueMbeans = beanServerConnection.queryMBeans(queueObjectName, null);

            brokerMetricsScraper.setObjectInstances(brokerMbeans);
            List<MeterData> meterDataList = new ArrayList<>();


            queueMetricsScraper.setObjectInstances(queueMbeans);

            meterDataList.addAll(queueMetricsScraper.scrapeMetrics());
//        meterDataList.addAll(brokerMetricsScraper.scrapeMetrics());

            ExportMetrics exportMetrics = new ExportMetrics("localhost", 11800);
            for (MeterData meterData : meterDataList){
                exportMetrics.export(Arrays.asList(meterData));
            }

            Thread.sleep(10000);
        }



//        System.out.println(objectInstances.size());
//        System.out.println(objectNames.size());
//        for (ObjectInstance objectInstance : objectInstances) {
//            ActiveMQServerControl activeMQServerControl = JMX.newMBeanProxy(beanServerConnection, objectInstance.getObjectName(), ActiveMQServerControl.class, false);
//            System.out.println(activeMQServerControl.getVersion());
//            System.out.println(activeMQServerControl.getNodeID());
//            System.out.println(activeMQServerControl.getGlobalMaxSize());
//            System.out.println(activeMQServerControl.getQueueCount());
//            System.out.println(activeMQServerControl.getAddressCount());
//            System.out.println(activeMQServerControl.getAcceptors().length);
//            System.out.println(activeMQServerControl.getUptime());
//            System.out.println(activeMQServerControl.getTotalMessageCount());
//            System.out.println(activeMQServerControl.getTotalMessageCount());
//            MeterData meterData = MeterData.newBuilder().setService("atermis")
//                    .setServiceInstance("broker1")
//                    .setSingleValue(MeterSingleValue.newBuilder().setValue(1212))
//                    .build();
//            ManagedChannel localhost = ManagedChannelBuilder.forAddress("localhost", 11800).usePlaintext().build();
//            MeterReportServiceGrpc.MeterReportServiceStub meterReportServiceStub = MeterReportServiceGrpc.newStub(localhost);
//            CountDownLatch countDownLatch = new CountDownLatch(1);
//            StreamObserver<MeterData> collected = meterReportServiceStub.collect(new StreamObserver<Commands>() {
//                @Override
//                public void onNext(Commands value) {
//                    System.out.println("Commands value: "+value);
//                }
//
//                @Override
//                public void onError(Throwable t) {
//                    System.out.println("Error: "+t.getMessage());
//                   t.printStackTrace();
//                   countDownLatch.countDown();
//                }
//
//                @Override
//                public void onCompleted() {
//                    System.out.println("Completed successfully");
//                    countDownLatch.countDown();
//                }
//            });
//
//            collected.onNext(meterData);
//            collected.onCompleted();
//            countDownLatch.await();


//        }







    }

    private static void fetchBrokerMetrics(MBeanServerConnection beanServerConnection, Set<ObjectName> objectNames) {


    }
}