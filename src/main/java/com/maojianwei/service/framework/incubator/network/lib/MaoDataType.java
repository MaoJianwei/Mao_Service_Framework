package com.maojianwei.service.framework.incubator.network.lib;

public enum MaoDataType {

    AAA(1),;

    private final int code;

    MaoDataType(int code) {
        this.code = code;
    }

    public String getHeader(int subType) {
        return String.format("%02d,%02d;", code, subType);
    }

    public int get() {
        return code;
    }
}
