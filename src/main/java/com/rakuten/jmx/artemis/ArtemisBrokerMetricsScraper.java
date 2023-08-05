package com.rakuten.jmx.artemis;

import com.rakuten.metrics.MetricsBuilderUtils;
import com.rakuten.metrics.MetricsScraper;
import com.rakuten.metrics.ServiceDescriptor;
import org.apache.activemq.artemis.api.core.management.ActiveMQServerControl;
import org.apache.skywalking.apm.network.language.agent.v3.MeterData;
import org.json.JSONArray;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import java.util.*;

public class ArtemisBrokerMetricsScraper implements MetricsScraper {

    private MBeanServerConnection serverConnection;
    private Set<ObjectInstance> objectInstances = new HashSet<>();
    private Set<ActiveMQServerControl> serverControlMbeans = new HashSet<>();

    public Set<ObjectInstance> getObjectInstances() {
        return objectInstances;
    }

    public void setObjectInstances(Set<ObjectInstance> objectInstances) {
        for (ObjectInstance objectInstance : objectInstances) {
            if (!this.objectInstances.contains(objectInstance)){
                this.objectInstances.add(objectInstance);
                ActiveMQServerControl activeMQServerControl = JMX.newMBeanProxy(this.serverConnection,
                        objectInstance.getObjectName(), ActiveMQServerControl.class);
                serverControlMbeans.add(activeMQServerControl);
            }
        }
    }


    public ArtemisBrokerMetricsScraper(MBeanServerConnection serverConnection) {
        this.serverConnection = serverConnection;
    }

    @Override
    public List<MeterData> scrapeMetrics()  {
        List<MeterData> meterDataList = new ArrayList<>();
        for (ActiveMQServerControl serverControlMbean : serverControlMbeans) {
            String nodeId = serverControlMbean.getNodeID();
            String version = serverControlMbean.getVersion();
            String name = serverControlMbean.getName();
            long memoryLimit = serverControlMbean.getGlobalMaxSize();
            int addressCount = serverControlMbean.getAddressCount();
            int queueCount = serverControlMbean.getQueueCount();
            String uptime = serverControlMbean.getUptime();
            long consumerCount = serverControlMbean.getTotalConsumerCount();
            long currentMemoryUsageBytes = serverControlMbean.getAddressMemoryUsage();
            int currentMemoryPercentage = serverControlMbean.getAddressMemoryUsagePercentage();
            int acceptorCount = -1;

            try {
                acceptorCount = serverControlMbean.getAcceptors().length;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            int producerCount = -1;
            try {
                JSONArray producers = new JSONArray(serverControlMbean.listProducersInfoAsJSON());
                producerCount = producers.length();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            Map<String, String> labels = new HashMap<>();
            labels.put("node_id",nodeId);
            labels.put("instance_type","broker");
            labels.put("version",version);
            labels.put("uptime",uptime);
            ServiceDescriptor serviceDescriptor = new ServiceDescriptor("Artemis", name, labels);

            meterDataList.add(MetricsBuilderUtils.createSingleValueMetrics("queue_count",queueCount,serviceDescriptor));
            meterDataList.add(MetricsBuilderUtils.createSingleValueMetrics("memory_limit",memoryLimit,serviceDescriptor));
            meterDataList.add(MetricsBuilderUtils.createSingleValueMetrics("address_count",addressCount,serviceDescriptor));
            meterDataList.add(MetricsBuilderUtils.createSingleValueMetrics("consumer_count",consumerCount,serviceDescriptor));
            meterDataList.add(MetricsBuilderUtils.createSingleValueMetrics("current_memory_percentage",currentMemoryPercentage,serviceDescriptor));
            meterDataList.add(MetricsBuilderUtils.createSingleValueMetrics("current_memory_usage_bytes",currentMemoryUsageBytes,serviceDescriptor));

            if (acceptorCount != -1)
                meterDataList.add(MetricsBuilderUtils.createSingleValueMetrics("acceptor_count",acceptorCount,serviceDescriptor));
            if (producerCount != -1)
                meterDataList.add(MetricsBuilderUtils.createSingleValueMetrics("producer_count",producerCount,serviceDescriptor));

        }
        return meterDataList;
    }
}
