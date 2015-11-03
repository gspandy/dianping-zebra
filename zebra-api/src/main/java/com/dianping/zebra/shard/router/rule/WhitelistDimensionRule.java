/**
 * Project: com.dianping.zebra.zebra-client-0.1.0
 * 
 * File Created at 2011-6-14
 * $Id$
 * 
 * Copyright 2010 dianping.com.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.zebra.shard.router.rule;

import com.dianping.zebra.shard.config.ExceptionConfig;
import com.dianping.zebra.shard.router.rule.engine.GroovyRuleEngine;
import com.dianping.zebra.shard.router.rule.engine.RuleEngine;
import com.dianping.zebra.shard.router.rule.engine.RuleEngineEvalContext;

import java.util.*;

/**
 * @author danson.liu
 *
 */
public class WhitelistDimensionRule extends AbstractDimensionRule {

	private String dataSource;

	private String table;

	private RuleEngine ruleEngine;

	public void init(ExceptionConfig exceptionConfig) {
		this.dataSource = exceptionConfig.getDb();
		this.table = exceptionConfig.getTable();
		String condition = exceptionConfig.getCondition();
		this.ruleEngine = new GroovyRuleEngine(condition);
		this.initShardColumn(condition);
	}

	@Override
	public boolean match(ShardMatchContext matchContext) {
		ShardMatchResult matchResult = matchContext.getMatchResult();
		List<Map<String, Object>> colValues = matchContext.getColValues();

		for (Iterator<Map<String, Object>> iterator = colValues.iterator(); iterator.hasNext();) {
			if ((Boolean) ruleEngine.eval(new RuleEngineEvalContext(iterator.next()))) {
				iterator.remove();
				if (!matchResult.isDbAndTablesSetted()) {
					matchResult.addDBAndTable(dataSource, table);
				}
			}
		}

		return true;
	}

	@Override
	public Map<String, Set<String>> getAllDBAndTables() {
		Map<String, Set<String>> dbAndTables = new HashMap<String, Set<String>>(1);
		Set<String> tableSet = new HashSet<String>(1);
		tableSet.add(table);
		dbAndTables.put(dataSource, tableSet);
		return dbAndTables;
	}
}
