package com.licheedev.serialtool.comn.message;

import com.licheedev.serialtool.util.TimeUtil;

/**
 * 收到的日志
 */

public class RecvMessage implements IMessage {
    
    private String command;
    private String message;
    private int size;

    public RecvMessage(String command, int size) {
        this.command = command;
        this.size=size;
        this.message = TimeUtil.currentTime() + ":-" + command;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public boolean isToSend() {
        return false;
    }
}
