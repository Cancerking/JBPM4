package com.zhf.jbpm;

import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

import org.hibernate.Session;
import org.jbpm.api.Configuration;
import org.jbpm.api.ExecutionService;
import org.jbpm.api.ProcessDefinition;
import org.jbpm.api.ProcessDefinitionQuery;
import org.jbpm.api.ProcessEngine;
import org.jbpm.api.RepositoryService;
import org.jbpm.api.TaskService;
import org.jbpm.api.task.Task;
import org.jbpm.pvm.internal.env.EnvironmentFactory;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.hibernate.DbSessionImpl;
import org.junit.Test;

import com.zhf.jbpm4.vo.AuditInfoVo;
import com.zhf.jbpm4.vo.TurnCardVo;

public class JbpmTest {
	
	private static ProcessEngine processEngine;
	
	private static RepositoryService repositoryService;

	private static ExecutionService executionService;
	
	private static TaskService taskService;

	static{
		//创建ProcessEngine，同时创建sessionFactory并初始表
		processEngine = new Configuration().buildProcessEngine();
		//获取部署repositoryService
		repositoryService = processEngine.getRepositoryService();
		//获取创建实例executionService
		executionService = processEngine.getExecutionService();
		//获取taskService
		taskService = processEngine.getTaskService(); 

	}
	
	/**
	 * 部署流程
	 */
	@Test
	public void testJbpm(){
		InputStream in = this.getClass().getResourceAsStream("/Jbpm/TurnCard.zip");
		repositoryService.createDeployment().addResourcesFromZipInputStream(new ZipInputStream(in)).deploy();
	}
	
	/**
	 * 创建流程实例
	 */
	@Test
	public void testJbpm2() throws Exception {
		//取得最新版本的流程定义,因为从大到小的按照pdversion排序，所有获取第一个
//		ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
//		.orderDesc(ProcessDefinitionQuery.PROPERTY_VERSION)
//		.list()
//		.get(0);
		
		ProcessDefinition processDefinition = processDefinition = repositoryService.createProcessDefinitionQuery()
					.orderDesc(ProcessDefinitionQuery.PROPERTY_VERSION)
					.page(0, 1)	
					.uniqueResult();
		executionService.startProcessInstanceById(processDefinition.getId());
	}
	
	/**
	 * 取得zhangsan的待办任务
	 * @throws Exception
	 */
	@Test
	public void testJbpmZhangSan() throws Exception {
		Task task = taskService.findPersonalTasks("zhangsan").get(0);
		System.out.println(task.getId() + ", " + task.getActivityName() + ", " + task.getAssignee() + ", " + task.getExecutionId());
		
		//从JBPM4中取得hibernate session 这样共用一个事务
		EnvironmentFactory environmentFactory = (EnvironmentFactory)processEngine;
		EnvironmentImpl environmentImpl = environmentFactory.openEnvironment();
		DbSessionImpl dbSessionImpl = environmentImpl.getFromCurrent(DbSessionImpl.class);
		Session session = dbSessionImpl.getSession();

		try {
			session.getTransaction().begin();
			TurnCardVo turnCard = new TurnCardVo();
			turnCard.setClientLevel("一级管理职位");
			turnCard.setClientName("CEO");
			turnCard.setCreator("zhangsan");
			turnCard.setCreateTime(new Date());
			session.save(turnCard);
			//将转向单的信息放到流程变量中,为了减少冗余只存放主键
			Map<String, Object> variables = new HashMap<String, Object>();
			variables.put("turnCardId", turnCard.getId());
			variables.put("clientLevel", turnCard.getClientLevel()); //如果不是一级管理职位，将直接被wangwu处理
			taskService.setVariables(task.getId(), variables);
			//完成待办任务
			taskService.completeTask(task.getId());
			session.getTransaction().commit();
		}catch(Exception ex) {
			ex.printStackTrace();
			session.getTransaction().rollback();
		}finally {
			session.close();
		}

	}
	
	/**
	 * 取得lisi的待办任务
	 * @throws Exception
	 */
	@Test
	public void testJbpmLisi() throws Exception {
		Task task = taskService.findPersonalTasks("lisi").get(0);
		System.out.println(task.getId() + ", " + task.getActivityName() + ", " + task.getAssignee() + ", " + task.getExecutionId());
		//从流程变量中取得流向单id
		String turnCardId = (String)taskService.getVariable(task.getId(), "turnCardId");
		EnvironmentFactory environmentFactory = (EnvironmentFactory)processEngine;
		EnvironmentImpl environmentImpl = environmentFactory.openEnvironment();
		DbSessionImpl dbSessionImpl = environmentImpl.getFromCurrent(DbSessionImpl.class);
		Session session = dbSessionImpl.getSession();
		try {
			session.getTransaction().begin();
			//根据流向单id取得流向单信息
			TurnCardVo turnCard = (TurnCardVo)session.load(TurnCardVo.class, turnCardId);
			
			//添加审批意见
			AuditInfoVo auditInfo = new AuditInfoVo();
			auditInfo.setRemark("pass");
			auditInfo.setAuditor("lisi");
			auditInfo.setAuditTime(new Date());
			session.save(auditInfo);
			//完成任务
			taskService.completeTask(task.getId());
			session.getTransaction().commit();
		}catch(Exception ex) {
			ex.printStackTrace();
			session.getTransaction().rollback();
		}finally {
			session.close();
		}		
	}
	
	
	/**
	 * 取得zhaoliu的待办任务
	 * @throws Exception
	 */
	@Test
	public void testJbpmzhaoliu() throws Exception {
		Task task = taskService.findPersonalTasks("zhaoliu").get(0);
		System.out.println(task.getId() + ", " + task.getActivityName() + ", " + task.getAssignee() + ", " + task.getExecutionId());
		//从流程变量中取得流向单id
		String turnCardId = (String)taskService.getVariable(task.getId(), "turnCardId");
		EnvironmentFactory environmentFactory = (EnvironmentFactory)processEngine;
		EnvironmentImpl environmentImpl = environmentFactory.openEnvironment();
		DbSessionImpl dbSessionImpl = environmentImpl.getFromCurrent(DbSessionImpl.class);
		Session session = dbSessionImpl.getSession();
		try {
			session.getTransaction().begin();
			//根据流向单id取得流向单信息
			TurnCardVo turnCard = (TurnCardVo)session.load(TurnCardVo.class, turnCardId);
			
			//添加审批意见
			AuditInfoVo auditInfo = new AuditInfoVo();
			auditInfo.setRemark("pass");
			auditInfo.setAuditor("zhaoliu");
			auditInfo.setAuditTime(new Date());
			session.save(auditInfo);
			//完成任务
			taskService.completeTask(task.getId());
			session.getTransaction().commit();
		}catch(Exception ex) {
			ex.printStackTrace();
			session.getTransaction().rollback();
		}finally {
			session.close();
		}		
	}
	
	
	/**
	 * 取得wangwu的待办任务
	 * @throws Exception
	 */
	@Test
	public void testJbpmWangwu() throws Exception {
		Task task = taskService.findPersonalTasks("wangwu").get(0);
		System.out.println(task.getId() + ", " + task.getActivityName() + ", " + task.getAssignee() + ", " + task.getExecutionId());
		//从流程变量中取得流向单id
		String turnCardId = (String)taskService.getVariable(task.getId(), "turnCardId");
		EnvironmentFactory environmentFactory = (EnvironmentFactory)processEngine;
		EnvironmentImpl environmentImpl = environmentFactory.openEnvironment();
		DbSessionImpl dbSessionImpl = environmentImpl.getFromCurrent(DbSessionImpl.class);
		Session session = dbSessionImpl.getSession();
		try {
			session.getTransaction().begin();
			//根据流向单id取得流向单信息
			TurnCardVo turnCard = (TurnCardVo)session.load(TurnCardVo.class, turnCardId);
			
			//添加审批意见
			AuditInfoVo auditInfo = new AuditInfoVo();
			auditInfo.setRemark("ok");
			auditInfo.setAuditor("wangwu");
			auditInfo.setAuditTime(new Date());
			session.save(auditInfo);
			//完成任务
			taskService.completeTask(task.getId());
			session.getTransaction().commit();
		}catch(Exception ex) {
			ex.printStackTrace();
			session.getTransaction().rollback();
		}finally {
			session.close();
		}
	}
}
