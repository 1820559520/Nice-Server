package com.nice.core.netty.handler;

import com.nice.core.netty.message.Msg;

/**
 * Created by justin on 14-7-30.
 * ��Ϣ������
 * ������Ϣ�����඼�̳���
 */
public abstract class BaseHandler {

    public abstract void process(Msg msg) throws Exception;

}
