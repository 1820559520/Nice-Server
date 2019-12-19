package com.nice.core.utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.util.Random;

public class ByteUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ByteUtil.class);

    /**
     * bytesת����int
     * @param data
     * @param offset
     * @return
     */
    public static int bytesToInt(byte[] data, int offset) {
        int num = 0;
        for (int i = offset; i < offset + 4; i++) {
            num <<= 8;
            num |= (data[i] & 0xff);
        }
        return num;
    }

    /**
     * intת����byte����
     * @param num
     * @return
     */
    public static byte[] intToBytes(int num) {
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            b[i] = (byte) (num >>> (24 - i * 8));
        }
        return b;
    }

    public static byte[] short2Bytes(short s){
        byte[] b = new byte[2];
        for(int i = 0; i < 2; i++){
            int offset = 16 - (i+1)*8; //��Ϊbyteռ4���ֽڣ�����Ҫ����ƫ����
            b[i] = (byte)((s >> offset)&0xff); //��16λ��Ϊ2��8λ���зֱ�洢
        }
        return b;
    }

    public static short bytes2Short(byte[] b, int offset){
        short l = 0;
        for (int i = offset; i < offset + 2; i++) {
            l<<=8; //<<=�����ǵ� +=��һ���ģ���˼���� l = l << 8
            l |= (b[i] & 0xff); //������Ҳ��һ����  l = l | (b[i]&0xff)
        }
        return l;
    }

    /*
     * MD5 ����
     * */
    public static String MD5(String inStr) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            LOGGER.error("MD5->msg={}", e.getMessage(), e);
            return "";
        }
        char[] charArray = inStr.toCharArray();
        byte[] byteArray = new byte[charArray.length];

        for (int i = 0; i < charArray.length; i++)
            byteArray[i] = (byte) charArray[i];

        byte[] md5Bytes = md5.digest(byteArray);

        StringBuffer hexValue = new StringBuffer();

        for (int i = 0; i < md5Bytes.length; i++) {
            int val = ((int) md5Bytes[i]) & 0xff;
            if (val < 16)
                hexValue.append("0");
            hexValue.append(Integer.toHexString(val));
        }

        return hexValue.toString();
    }


    /*
     * ��ȡ����ַ���
     * */
    public static String getRandStr(int num){
        String[] str = {"1","2","3","4","5","6","7","8","9","A","B","C","D","E","F","G","H","J","K","L","M","N","P","Q","R","S","T","U","V","W","X","Y","Z"};
        int strLength =str.length;
        Random random = new Random();
        int rand = 0 ;
        StringBuffer stringBuffer = new StringBuffer();
        for (int i=0; i< num;i++){
            rand = random.nextInt(strLength);
            stringBuffer.append(str[rand]);
        }
        String randStr = String.valueOf(stringBuffer);
        return randStr;
    }


}
