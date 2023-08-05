package com.rakuten.sixthsense.remote;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.apache.skywalking.apm.network.common.v3.Commands;
import org.apache.skywalking.apm.network.language.agent.v3.MeterData;
import org.apache.skywalking.apm.network.language.agent.v3.MeterReportServiceGrpc;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ExportMetrics {

     private String url;
     private int port;

    public ExportMetrics(String url, int port) {
        this.url = url;
        this.port = port;
    }

    public void export(List<MeterData> meterDataList) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        ManagedChannel managedChannel = ManagedChannelBuilder.forAddress(this.url, this.port).usePlaintext().build();
        MeterReportServiceGrpc.MeterReportServiceStub meterReportServiceStub = MeterReportServiceGrpc.newStub(managedChannel);
        StreamObserver<MeterData> collected = meterReportServiceStub.collect(new StreamObserver<Commands>() {
            @Override
            public void onNext(Commands value) {
                System.out.println("Got response: "+value);

            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
                countDownLatch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("Request completed successfully");
                countDownLatch.countDown();
            }
        });
        for (MeterData meterData : meterDataList) {
            collected.onNext(meterData);
        }
        collected.onCompleted();
        countDownLatch.await();
    }
}
