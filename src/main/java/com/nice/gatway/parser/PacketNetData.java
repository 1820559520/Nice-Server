package com.nice.gatway.parser;

import com.nice.core.utils.ByteUtil;

public class PacketNetData {
    //
    private byte[] packetData;

    public PacketNetData() {
    }

    public void setPacketData(byte[] packetData) {
        this.packetData = packetData;
    }


    public int getSeq() {
        return ByteUtil.bytesToInt(packetData, 0);
    }

    public int getMsgId() {
        return ByteUtil.bytesToInt(packetData, 4);
    }



    /**
     * ֻ��ȡЭ����Ϣ
     *
     * @return
     */
    public byte[] parseProtoNetData() {
        byte[] data = new byte[packetData.length - 8];
        System.arraycopy(packetData, 8, data, 0, data.length);
        return data;
    }


    /**
     * ��ȡȫ������
     *
     * @return
     */
    public byte[] getPacketData() {
        return packetData;
    }

}