package com.rakuten.jmx.artemis;

import com.rakuten.metrics.MessageCounter;
import com.rakuten.metrics.MetricsBuilderUtils;
import com.rakuten.metrics.MetricsScraper;
import com.rakuten.metrics.ServiceDescriptor;
import org.apache.activemq.artemis.api.core.management.QueueControl;
import org.apache.activemq.artemis.commons.shaded.json.Json;
import org.apache.skywalking.apm.network.language.agent.v3.MeterData;
import org.json.JSONObject;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import java.util.*;

public class ArtemisQueueMetricsScraper implements MetricsScraper {


    private MBeanServerConnection serverConnection;
    private Set<ObjectInstance> objectInstances = new HashSet<>();
    private Map<ObjectInstance, QueueControl> queueControlBeans = new HashMap<>();

    public Set<ObjectInstance> getObjectInstances() {
        return objectInstances;
    }

    private Map<String, MessageCounter> counterMap = new HashMap<>();

    public Map<String,String> dlqMap = new HashMap<>();

    public void setObjectInstances(Set<ObjectInstance> objectInstances) {
        for (ObjectInstance objectInstance : objectInstances) {
            if (!this.objectInstances.contains(objectInstance)){
                this.objectInstances.add(objectInstance);
                QueueControl queueControl = JMX.newMBeanProxy(this.serverConnection,
                        objectInstance.getObjectName(), QueueControl.class);
                queueControlBeans.put(objectInstance, queueControl);
            }
        }
    }

    public ArtemisQueueMetricsScraper(MBeanServerConnection serverConnection) {
        this.serverConnection = serverConnection;
    }

    @Override
    public List<MeterData> scrapeMetrics() {
        List<MeterData> meterDataList = new ArrayList<>();

        for (ObjectInstance objectInstance : queueControlBeans.keySet()){
            QueueControl queueControlBean = queueControlBeans.get(objectInstance);
            String broker = objectInstance.getObjectName().getKeyProperty("broker");
            broker = formatBrokerAddress(broker);
            dlqMap.put(queueControlBean.getDeadLetterAddress(),broker);

        }

        for (ObjectInstance objectInstance : queueControlBeans.keySet()) {
            QueueControl queueControlBean = queueControlBeans.get(objectInstance);
            String queueName = queueControlBean.getName();
            String broker = objectInstance.getObjectName().getKeyProperty("broker");
            broker = formatBrokerAddress(broker);
            queueControlBean.getDeadLetterAddress();
            String address = queueControlBean.getAddress();
            String dlq = queueControlBean.getDeadLetterAddress();
            String expiryAddress = queueControlBean.getExpiryAddress();
            long messagesInQueue = queueControlBean.getMessageCount();
            boolean isDLQ = dlqMap.containsKey(queueName)
                    && dlqMap.get(queueName).equals(broker);

            String queueUniqueKey = getQueueUniqueKey(objectInstance);
            long messagesAdded = -1;
            if (counterMap.containsKey(queueUniqueKey)) {
                MessageCounter messageAddedCounter = counterMap.get(queueUniqueKey);
                messageAddedCounter.setCurrentCount(queueControlBean.getMessagesAdded());
                messagesAdded = messageAddedCounter.calculate();
            } else {
                MessageCounter messageAddedCounter = new MessageCounter("messageAdded", queueControlBean.getMessagesAdded());
                counterMap.put(queueUniqueKey, messageAddedCounter);
            }

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("address",address);
            jsonObject.put("broker",broker);
            jsonObject.put("dlq_address",dlq);
            jsonObject.put("is_dlq",isDLQ);
            jsonObject.put("expiry_address",expiryAddress);

            Map<String, String> labels = new HashMap<>();
            labels.put("json",jsonObject.toString());
            labels.put("address",address);
            labels.put("instance_type","queue");

            ServiceDescriptor serviceDescriptor = new ServiceDescriptor("Artemis", queueName, labels);

            meterDataList.add(MetricsBuilderUtils.createSingleValueMetrics("current_message",messagesInQueue,serviceDescriptor));
            if (messagesAdded != -1)
                meterDataList.add(MetricsBuilderUtils.createSingleValueMetrics("current_message_added",messagesAdded,serviceDescriptor));

        }
        return meterDataList;
    }

    private String formatBrokerAddress(String broker) {
        if (broker.startsWith("\"") && broker.endsWith("\"")){
            broker = broker.substring( 1, broker.length() - 1);
        }
        return broker;
    }

    private String getQueueUniqueKey(ObjectInstance objectInstance){
        return  objectInstance.getObjectName().getKeyPropertyListString();
    }
}
