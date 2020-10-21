package com.maojianwei.service.framework.incubator.network.lib;

public enum MaoDataType {

    AAA_AUTH(1, 1),
    AAA_ACCEPT(1, 2),
    AAA_REJECT(1, 3),
    AAA_DROP(1, 5),
    ;

    private final int type;
    private final int subType;


    MaoDataType(int type, int subType) {
        this.type = type;
        this.subType = subType;
    }

    public String getHeader() {
        return String.format("%02d,%02d;", type, subType);
    }

    public int getType() {
        return type;
    }

    public int getSubType() {
        return subType;
    }
}





























