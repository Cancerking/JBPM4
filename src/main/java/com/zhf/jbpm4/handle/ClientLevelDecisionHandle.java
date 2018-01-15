package com.zhf.jbpm4.handle;

import org.jbpm.api.jpdl.DecisionHandler;
import org.jbpm.api.model.OpenExecution;

/**
 * 流程判断的表达式
 * 然后配置到流程图中的handlerClass
 * @author mike
 *
 */
public class ClientLevelDecisionHandle implements DecisionHandler {

	@Override
	public String decide(OpenExecution execution) {
		String clientLevel = (String)execution.getVariable("clientLevel");
		return "一级管理职位".equals(clientLevel) ? "to ProjectAudit" : "to DistrictManagerReview";
	}

}
