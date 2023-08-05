package com.rakuten.metrics;

import org.apache.skywalking.apm.network.language.agent.v3.MeterData;
import org.apache.skywalking.apm.network.language.agent.v3.MeterSingleValue;

public class MetricsBuilderUtils{

    public static MeterData createSingleValueMetrics(String metricName, double value, ServiceDescriptor serviceDescriptor){
        MeterSingleValue meterSingleValue = MeterSingleValue.newBuilder()
                .setName(metricName)
                .setValue(value)
                .addAllLabels(serviceDescriptor.getLabels())
                .build();

        return MeterData.newBuilder()
                .setSingleValue(meterSingleValue)
                .setService(serviceDescriptor.getServiceNameAsJson())
                .setServiceInstance(serviceDescriptor.getInstanceName())
                .setTimestamp(System.currentTimeMillis())
                .build();

    }
}
