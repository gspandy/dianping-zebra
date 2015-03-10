package com.dianping.zebra.shard.router.rule;

import com.dianping.zebra.shard.router.rule.engine.GroovyRuleEngine;
import com.dianping.zebra.shard.router.rule.engine.RuleEngine;
import com.dianping.zebra.shard.router.rule.engine.RuleEngineEvalContext;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class GroovyRuleEngineTest {

    @Test
    public void testRule() {
        RuleEngine ruleEngine = new GroovyRuleEngine("(#NoteID#.longValue() % 32).intdiv(8)");
        Map<String, Object> valMap = new HashMap<String, Object>();
        valMap.put("NoteID", 9);
        Object retVal = ruleEngine.eval(new RuleEngineEvalContext(valMap));
        System.out.println(retVal);
    }

    @Test
    public void testRule1() {
        RuleEngine ruleEngine = new GroovyRuleEngine("#NoteID#.longValue() % 8");
        Map<String, Object> valMap = new HashMap<String, Object>();
        valMap.put("NoteID", 25);
        Object retVal = ruleEngine.eval(new RuleEngineEvalContext(valMap));
        System.out.println(retVal);
    }

    @Test
    public void testCRC32() {
        RuleEngine ruleEngine = new GroovyRuleEngine("(crc32(#bid#)/10).toLong()%10");
        Map<String, Object> valMap = new HashMap<String, Object>();
        valMap.put("bid", "2127114697");
        Object retVal = ruleEngine.eval(new RuleEngineEvalContext(valMap));
        System.out.println(retVal);
    }
}