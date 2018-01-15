package com.zhf.jbpm4.vo;

import java.util.Date;

public class AuditInfoVo {

	private String id;
	
	private String remark;
	
	private String Auditor;
	
	private Date AuditTime;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getAuditor() {
		return Auditor;
	}

	public void setAuditor(String auditor) {
		Auditor = auditor;
	}

	public Date getAuditTime() {
		return AuditTime;
	}

	public void setAuditTime(Date auditTime) {
		AuditTime = auditTime;
	}
}
