package com.nice.core.netty.cocdex;

import com.nice.core.utils.TimeUtil;

import java.util.Random;

/**
 * Tea�㷨
 * ÿ�β������Դ���8���ֽ�����
 * KEYΪ16�ֽ�,ӦΪ����4��int������int[]��һ��intΪ4���ֽ�
 * ���ܽ�������ӦΪ8�ı������Ƽ���������Ϊ64��
 */
public class Tea {
    public static int[] KEY = new int[]{//���ܽ������õ�KEY
            0x789f5645, 0xf68bd5a4,
            0x81963ffa, 0x458fac58
    };
    private static int TIMES = 32;

    public static int[] generateSecretKey(){
        Random rand = new Random();
        int[] secretKey = new int[4];
        for(int i=0; i<4; i++){
            secretKey[i] = rand.nextInt();
        }
        return secretKey;
    }
    //��ĳ�ֽڱ����ͳɸ������轫��ת���޷�������
    private static int transform(byte temp){
        int tempInt = (int)temp;
        if(tempInt < 0){
            tempInt += 256;
        }
        return tempInt;
    }

    public static int byteToInt(byte[] content, int offset){
        int result = transform(content[offset+3]) | transform(content[offset+2]) << 8 |
                transform(content[offset+1]) << 16 | (int)content[offset] << 24;
        return result;
    }

    public static void intToByte(byte[] bys, int offset, int content){
        bys[offset+3] = (byte)(content & 0xff);
        bys[offset+2] = (byte)((content >> 8) & 0xff);
        bys[offset+1] = (byte)((content >> 16) & 0xff);
        bys[offset] = (byte)((content >> 24) & 0xff);
    }

    //����
    public static byte[] encrypt(byte[] content, int offset, int[] key, int times){//timesΪ��������
        int delta=0x9e3779b9; //�����㷨��׼����ֵ
        int a = key[0], b = key[1], c = key[2], d = key[3];
        for(int m=offset; m<content.length; m+=8)
        {
            int y = byteToInt(content, m), z = byteToInt(content, m+4), sum = 0, i;
            for (i = 0; i < times; i++) {
                sum += delta;
                y += ((z<<4) + a) ^ (z + sum) ^ ((z>>5) + b);
                z += ((y<<4) + c) ^ (y + sum) ^ ((y>>5) + d);
            }
            intToByte(content, m, y);
            intToByte(content, m+4, z);
        }
        return content;
    }
    //����
    public static byte[] decrypt(byte[] encryptContent, int offset, int[] key, int times){
        int delta = 0x9e3779b9; //�����㷨��׼����ֵ
        int a = key[0], b = key[1], c = key[2], d = key[3];

        for(int m=offset; m<encryptContent.length; m+=8) {
            int y = byteToInt(encryptContent, m), z = byteToInt(encryptContent, m+4), sum = 0xC6EF3720, i;
            for (i = 0; i < times; i++) {
                z -= ((y << 4) + c) ^ (y + sum) ^ ((y >> 5) + d);
                y -= ((z << 4) + a) ^ (z + sum) ^ ((z >> 5) + b);
                sum -= delta;
            }

            intToByte(encryptContent, m, y);
            intToByte(encryptContent, m+4, z);
        }
        return encryptContent;
    }

    public static void shortToByte(byte[] bys, int offset, int content){
        bys[offset+1] = (byte)(content & 0xff);
        bys[offset] = (byte)((content >> 8) & 0xff);
    }

    public static int byteToShort(byte[] content, int offset){
        int result = transform(content[offset+1]) | transform(content[offset]) << 8;
        return result;
    }

    public static byte[] encrypt2(byte[] content, int[] key) {
        int tobalLen = (content.length+7)/8*8 + 2;
        byte[] bys = new byte[tobalLen];
        System.arraycopy(content, 0, bys, 2, content.length);

        byte[] encryptContent = encrypt(bys, 2, key, TIMES);
        shortToByte(encryptContent, 0, content.length);
        return encryptContent;
    }
    public static byte[] decrypt2(byte[] encryptContent, int len, int offset, int[] key){
        byte[] decryptContent = decrypt(encryptContent, offset, key, TIMES);
        if(len == decryptContent.length){
            return decryptContent;
        }
        byte[] bys = new byte[len];
        System.arraycopy(decryptContent, 0, bys, 0, len);
        return bys;
    }
    public static byte[] encrypt3(NetData netData, int[] key) {
        //4:routerId, 1:routerType+reqestType
        //ȡ8������������
        int length = netData.getContent().length + 4 + 1;
        byte[] plainText = new byte[length];

        intToByte(plainText, 0, netData.getRouterId());
        plainText[4] = netData.getRouterType();
        System.arraycopy(netData.getContent(), 0, plainText, 5, netData.getContent().length);
//        System.out.println("plainText->");
//        PrintHex.printBarry(plainText);
        byte[] encrypt = encrypt2(plainText, key);
//        System.out.println("encrypt->");
//        PrintHex.printBarry(encrypt);
        return encrypt;
    }

    public static PacketNetData decrypt3(int length, byte[] encryptContent, int offset, int[] key){
        byte[] decryptContent = decrypt2(encryptContent, length, offset, key);
        PacketNetData packetNetData = new PacketNetData();
        packetNetData.setPacketData(decryptContent);

        return packetNetData;
    }




    //byte[]������ת��int[]������
    private static int[] byteToIntArray(byte[] content, int offset) {

        int[] result = new int[content.length >> 2]; //����2��n�η� == ����nλ �� content.length / 4 == content.length >> 2
        for (int i = 0, j = offset; j < content.length; i++, j += 4) {
            result[i] = byteToInt(content, j);
        }
        return result;
    }

    //int[]������ת��byte[]������
    private static byte[] intArrayToByte(int[] content, int offset) {
        byte[] result = new byte[content.length << 2]; //����2��n�η� == ����nλ �� content.length * 4 == content.length << 2
        for (int i = 0, j = offset; j < result.length; i++, j += 4) {
            intToByte(result, j, content[i]);
        }
        return result;
    }


    public static ClientDate decrypt4(int length, byte[] encryptContent, int offset, int[] key) {
        byte[] decryptContent = decrypt2(encryptContent, length, offset, key);
        ClientDate clientDate = new ClientDate();
        clientDate.setClientDate(decryptContent);

        return clientDate;
    }

    public static void main(String[] args){
        String plainStr = "To the time to life, rather than to life in time to the time to life, rather than to life in time.";
        System.out.println("plainStr=" + plainStr);
        byte[] encryptBys = encrypt2(plainStr.getBytes(), KEY);
        int length = byteToShort(encryptBys, 0);
        int length2 = (length+7)/8*8;
        byte[] encryptBys2 = new byte[length2];
        System.arraycopy(encryptBys, 2, encryptBys2, 0, length2);
        byte[] decodeBys = decrypt2(encryptBys2, length2, 0, KEY);
        String decryptStr = new String(decodeBys);
        System.out.println("decryptStr=" + decryptStr);

        NetData netData = new NetData();
        long startTime = TimeUtil.currentTimeMillis();
        netData.setContent(new byte[50]);
        for(int i=0; i<netData.getContent().length; i++){
            netData.getContent()[i] = (byte)i;
        }
        netData.setRouterId(1);
        netData.setRouterType((byte)2);
        for(int i=0; i<100000; i++){
            byte[] secretInfo = encrypt3(netData,  KEY);
            length = byteToShort(secretInfo, 0);

            length2 = (length+7)/8*8;
            encryptBys2 = new byte[length2];
            System.arraycopy(secretInfo, 2, encryptBys2, 0, length2);

            PacketNetData packetNetData = decrypt3(length, encryptBys2, 0, KEY );

            if(netData.getRouterId() != packetNetData.getRouterId()){
                System.out.println("netData.routerId != netData2.routerId");
                return;
            }
            if(netData.getRouterType() != packetNetData.getRouterType()){
                System.out.println("netData.routerType != netData2.routerType");
                return;
            }

            byte[] data = packetNetData.getMsgIdAndPBData();
            if(netData.getContent().length != data.length){
                System.out.println("netData.content.length != netData2.content.length");
                return;
            }
            int j;
            for(j=0; j<netData.getContent().length; j++){
                if(netData.getContent()[j] != data[j]) {
                    System.out.println("decryptInfo[j] != info[j]");
                    return;
                }
            }
        }
        long endTime = TimeUtil.currentTimeMillis();
        //�ӽ���10����ʱ91����
        System.out.println("�ӽ���10����ʱ" + (endTime-startTime) + "����");
    }
}
