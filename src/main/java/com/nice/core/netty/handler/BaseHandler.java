package com.nice.core.netty.handler;

import com.nice.core.netty.message.Msg;
import com.nice.core.netty.session.Session;

/**
 * Created by justin on 14-7-30.
 * ��Ϣ������
 * ������Ϣ�����඼�̳���
 */
public abstract class BaseHandler {

    public abstract void process(Session session, byte[] proto) throws Exception;

}
