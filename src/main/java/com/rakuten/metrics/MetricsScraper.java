package com.rakuten.metrics;

import org.apache.skywalking.apm.network.language.agent.v3.MeterData;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import java.util.List;
import java.util.Set;

public interface MetricsScraper {

    List<MeterData> scrapeMetrics();
}
