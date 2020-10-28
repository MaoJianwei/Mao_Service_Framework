package com.maojianwei.service.framework.incubator.network.lib;

import static com.maojianwei.service.framework.incubator.network.lib.MaoNetworkConst.DATA_SPLITER_INDEX;
import static com.maojianwei.service.framework.incubator.network.lib.MaoNetworkConst.TYPE_SPLITER_INDEX;

public enum MaoDataType {

    INVALID(0, 0),
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


    public static MaoDataType parseDataType(String originData) {
        int type = parseType(originData);
        int subType = parseSubType(originData);
        if (type == AAA_AUTH.type) {
                if (subType == AAA_AUTH.subType) {
                    return AAA_AUTH;
                } else if (subType == AAA_ACCEPT.subType) {
                    return AAA_ACCEPT;
                } else if (subType == AAA_REJECT.subType) {
                    return AAA_REJECT;
                } else if (subType == AAA_DROP.subType) {
                    return AAA_DROP;
                }
        }
        return INVALID;
    }

    private static int parseType(String originData) {
        return Integer.parseInt(originData.substring(0, TYPE_SPLITER_INDEX));
    }

    private static int parseSubType(String originData) {
        return Integer.parseInt(originData.substring(TYPE_SPLITER_INDEX + 1, DATA_SPLITER_INDEX));
    }
}





























