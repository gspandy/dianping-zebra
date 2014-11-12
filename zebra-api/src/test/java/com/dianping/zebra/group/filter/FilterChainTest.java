package com.dianping.zebra.group.filter;

/**
 * Created by Dozer on 9/24/14.
 */

import com.dianping.zebra.group.jdbc.GroupStatement;
import junit.framework.Assert;
import org.junit.Test;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class FilterChainTest {
	@Test
	public void test_execute_with_order() throws SQLException {
		final AtomicInteger counter = new AtomicInteger();

		JdbcFilter filter1 = new DefaultJdbcFilter() {
			@Override public <T> T execute(GroupStatement source, JdbcFilter chain) throws SQLException {
				Assert.assertEquals(3, counter.incrementAndGet());
				T executeResult = chain.execute(source, chain);
				Assert.assertEquals(5, counter.incrementAndGet());
				return executeResult;
			}

			@Override public int getOrder() {
				return 1;
			}
		};

		JdbcFilter filter2 = new DefaultJdbcFilter() {
			@Override public <T> T execute(GroupStatement source, JdbcFilter chain) throws SQLException {

				Assert.assertEquals(2, counter.incrementAndGet());
				T executeResult = chain.execute(source, chain);
				Assert.assertEquals(6, counter.incrementAndGet());
				return executeResult;
			}

			@Override public int getOrder() {
				return 2;
			}
		};

		JdbcFilter filter3 = new DefaultJdbcFilter() {
			@Override public <T> T execute(GroupStatement source, JdbcFilter chain) throws SQLException {

				Assert.assertEquals(1, counter.incrementAndGet());
				T executeResult = chain.execute(source, chain);
				Assert.assertEquals(7, counter.incrementAndGet());
				return executeResult;
			}

			@Override public int getOrder() {
				return 3;
			}
		};

		FilterManagerFactory.getFilterManager().addFilter("1", filter1);
		FilterManagerFactory.getFilterManager().addFilter("2", filter2);
		FilterManagerFactory.getFilterManager().addFilter("3", filter3);

		List<JdbcFilter> filters = FilterManagerFactory.getFilterManager().loadFilters("1,2,3");
		JdbcFilter chain = new DefaultJdbcFilterChain(filters) {
			@Override public <T> T execute(GroupStatement source, JdbcFilter chain) throws SQLException {
				if (index < filters.size()) {
					return filters.get(index++).execute(source, chain);
				} else {
					Assert.assertEquals(4, counter.incrementAndGet());
					return null;
				}
			}
		};
		chain.execute(null, chain);
	}
}
