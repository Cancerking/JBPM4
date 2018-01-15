package com.zhf.jbpm4.event;

import org.jbpm.api.listener.EventListener;
import org.jbpm.api.listener.EventListenerExecution;

/**
 * 流程图的事件类
 * @author mike
 *
 */
public class JbpmEventListener implements EventListener {

	@Override
	public void notify(EventListenerExecution execution) throws Exception {
		System.out.println("1111");
	}

}
