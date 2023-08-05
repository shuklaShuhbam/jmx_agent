package com.rakuten.metrics;

import com.google.common.base.Preconditions;

public class MessageCounter {

    private long initialCount;
    private boolean isInitial = true;

    public MessageCounter(String name, long initialCount) {
        this.name = name;
        this.initialCount = initialCount;
        this.setCurrentCount(initialCount);
    }

    private String name;

    private long previousCount;
    private long currentCount;

    private long count;


    public long getCurrentCount() {

        return currentCount;
    }

    public void setCurrentCount(long currentCount) {
        if (this.isInitial){
            this.isInitial = false;
            this.previousCount = this.initialCount;
        }else {
            this.previousCount = this.currentCount;
        }
        this.currentCount = currentCount;
    }

    public long calculate (){
        if(!validateCurrentState()){
            return -1;
        }else {
            this.count = this.currentCount - this.previousCount;
        }
        return this.count;
    }

    private boolean validateCurrentState(){
        if (isInitial)
            return false;
        else
            return  true;
    }

}
