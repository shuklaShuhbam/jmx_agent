package com.rakuten.metrics;

import org.apache.skywalking.apm.network.language.agent.v3.Label;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ServiceDescriptor {

    private String serviceName;
    private String instanceName;
    private Map<String,String> labels;

    public String getServiceNameAsJson() {
        return "{'name':'" + serviceName + "','teamID':'faffdafdas','type':'Q','sub':'AR'}";
    }

    public String getInstanceName() {
        return instanceName;
    }


    public Map getLabelsAsMap() {
        return labels;
    }

    public List<Label> getLabels(){
        if (this.labels == null){
            return null;
        }
        List<Label> labels = new ArrayList<>();
        this.labels.forEach((k,v) -> {
            labels.add(Label.newBuilder()
                    .setName(k)
                    .setValue(v)
                    .build());
        });
        return labels;
    }

    public ServiceDescriptor(String serviceName, String instanceName, Map labels) {
        this.serviceName = serviceName;
        this.instanceName = instanceName;
        this.labels = labels;
    }




}
