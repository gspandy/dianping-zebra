/**
 * Project: zebra-client
 *
 * File Created at Feb 19, 2014
 *
 */
package com.dianping.zebra.group.jdbc;

import com.dianping.zebra.group.datasources.SingleConnection;
import com.dianping.zebra.group.exception.DalNotSupportException;
import com.dianping.zebra.group.filter.JdbcContext;
import com.dianping.zebra.group.filter.JdbcFilter;
import com.dianping.zebra.group.filter.DefaultJdbcFilterChain;
import com.dianping.zebra.group.util.JDBCExceptionUtils;
import com.dianping.zebra.group.util.SqlType;
import com.dianping.zebra.group.util.SqlUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Leo Liang
 */
public class GroupStatement implements Statement {

	protected List<String> batchedSqls;

	protected boolean closed = false;

	protected GroupResultSet currentResultSet = null;

	protected GroupConnection dpGroupConnection;

	protected int fetchSize;

	protected List<JdbcFilter> filters;

	protected int maxRows;

	protected JdbcContext context;

	protected boolean moreResults = false;

	protected Statement openedStatement = null;

	protected int queryTimeout = 0;

	protected int resultSetConcurrency = ResultSet.CONCUR_READ_ONLY;

	protected int resultSetHoldability = -1;

	protected int resultSetType = ResultSet.TYPE_FORWARD_ONLY;

	protected int updateCount;

	public GroupStatement(GroupConnection connection, JdbcContext context, List<JdbcFilter> filters) {
		this.dpGroupConnection = connection;
		this.context = context;
		this.filters = filters;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Statement#addBatch(java.lang.String)
	 */
	@Override
	public void addBatch(String sql) throws SQLException {
		checkClosed();

		if (batchedSqls == null) {
			batchedSqls = new ArrayList<String>();
		}
		if (sql != null) {
			batchedSqls.add(sql);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Statement#cancel()
	 */
	@Override
	public void cancel() throws SQLException {
		throw new UnsupportedOperationException("cancel");
	}

	protected void checkClosed() throws SQLException {
		if (closed) {
			throw new SQLException("No operations allowed after statement closed.");
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Statement#clearBatch()
	 */
	@Override
	public void clearBatch() throws SQLException {
		checkClosed();
		if (batchedSqls != null) {
			batchedSqls.clear();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Statement#clearWarnings()
	 */
	@Override
	public void clearWarnings() throws SQLException {
		checkClosed();
		if (openedStatement != null) {
			openedStatement.clearWarnings();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Statement#close()
	 */
	@Override
	public void close() throws SQLException {
		if (closed) {
			return;
		}
		closed = true;

		try {
			if (currentResultSet != null) {
				currentResultSet.close();
			}
		} finally {
			currentResultSet = null;
		}

		try {
			if (this.openedStatement != null) {
				this.openedStatement.close();
			}
		} finally {
			this.openedStatement = null;
		}
	}

	protected void closeCurrentResultSet() throws SQLException {
		if (currentResultSet != null) {
			try {
				currentResultSet.close();
			} catch (SQLException e) {
				// ignore it
			} finally {
				currentResultSet = null;
			}
		}
	}

	public void closeOnCompletion() throws SQLException {
		throw new DalNotSupportException();
	}

	private Statement createStatementInternal(Connection conn, boolean isBatch) throws SQLException {
		Statement stmt;
		if (isBatch) {
			stmt = conn.createStatement();
		} else {
			int tmpResultSetHoldability = this.resultSetHoldability;
			if (tmpResultSetHoldability == -1) {
				tmpResultSetHoldability = conn.getHoldability();
			}

			stmt = conn.createStatement(this.resultSetType, this.resultSetConcurrency, tmpResultSetHoldability);
		}

		setRealStatement(stmt);
		stmt.setQueryTimeout(queryTimeout);
		stmt.setFetchSize(fetchSize);
		stmt.setMaxRows(maxRows);

		return stmt;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Statement#execute(java.lang.String)
	 */
	@Override
	public boolean execute(String sql) throws SQLException {
		return executeInternal(sql, -1, null, null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Statement#execute(java.lang.String, int)
	 */
	@Override
	public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
		return executeInternal(sql, autoGeneratedKeys, null, null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Statement#execute(java.lang.String, int[])
	 */
	@Override
	public boolean execute(String sql, int[] columnIndexes) throws SQLException {
		return executeInternal(sql, -1, columnIndexes, null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Statement#execute(java.lang.String, java.lang.String[])
	 */
	@Override
	public boolean execute(String sql, String[] columnNames) throws SQLException {
		return executeInternal(sql, -1, null, columnNames);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Statement#executeBatch()
	 */
	@Override
	public int[] executeBatch() throws SQLException {
		try {
			checkClosed();
			closeCurrentResultSet();

			if (batchedSqls == null || batchedSqls.isEmpty()) {
				return new int[0];
			}

			return executeWithFilter(new JDBCOperationCallback<int[]>() {
				@Override
				public int[] doAction(Connection conn) throws SQLException {
					return executeBatchOnConnection(conn, batchedSqls);
				}
			}, null, null, true, true);
		} finally {
			if (batchedSqls != null) {
				batchedSqls.clear();
			}
		}
	}

	private int[] executeBatchOnConnection(final Connection conn, final List<String> batchedSqls) throws SQLException {

		Statement stmt = createStatementInternal(conn, true);
		for (String sql : batchedSqls) {
			stmt.addBatch(processSQL(conn, sql));
		}
		return stmt.executeBatch();
	}

	private boolean executeInternal(String sql, int autoGeneratedKeys, int[] columnIndexes, String[] columnNames)
		  throws SQLException {
		SqlType sqlType = SqlUtils.getSqlType(sql);
		if (sqlType.isQuery()) {
			executeQuery(sql);
			return true;
		} else {
			if (autoGeneratedKeys == -1 && columnIndexes == null && columnNames == null) {
				executeUpdate(sql);
			} else if (autoGeneratedKeys != -1) {
				executeUpdate(sql, autoGeneratedKeys);
			} else if (columnIndexes != null) {
				executeUpdate(sql, columnIndexes);
			} else if (columnNames != null) {
				executeUpdate(sql, columnNames);
			} else {
				executeUpdate(sql);
			}

			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Statement#executeQuery(java.lang.String)
	 */
	@Override
	public ResultSet executeQuery(final String sql) throws SQLException {
		checkClosed();
		closeCurrentResultSet();

		return executeWithFilter(new JDBCOperationCallback<ResultSet>() {
			@Override
			public ResultSet doAction(Connection conn) throws SQLException {
				return executeQueryOnConnection(conn, sql);
			}
		}, sql, null, false, false);
	}

	private ResultSet executeQueryOnConnection(Connection conn, String sql) throws SQLException {
		sql = processSQL(conn, sql);
		Statement stmt = createStatementInternal(conn, false);
		currentResultSet = new GroupResultSet(this.context.clone(), this.filters, stmt.executeQuery(sql));
		return currentResultSet;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Statement#executeUpdate(java.lang.String)
	 */
	@Override
	public int executeUpdate(String sql) throws SQLException {
		return executeUpdateInternal(sql, -1, null, null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Statement#executeUpdate(java.lang.String, int)
	 */
	@Override
	public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
		return executeUpdateInternal(sql, autoGeneratedKeys, null, null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Statement#executeUpdate(java.lang.String, int[])
	 */
	@Override
	public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
		return executeUpdateInternal(sql, -1, columnIndexes, null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Statement#executeUpdate(java.lang.String, java.lang.String[])
	 */
	@Override
	public int executeUpdate(String sql, String[] columnNames) throws SQLException {
		return executeUpdateInternal(sql, -1, null, columnNames);
	}

	private int executeUpdateInternal(final String sql, final int autoGeneratedKeys, final int[] columnIndexes,
		  final String[] columnNames) throws SQLException {

		checkClosed();
		closeCurrentResultSet();

		return executeWithFilter(new JDBCOperationCallback<Integer>() {
			@Override
			public Integer doAction(Connection conn) throws SQLException {
				try {
					updateCount = executeUpdateOnConnection(conn, sql, autoGeneratedKeys, columnIndexes, columnNames);
				} catch (SQLException e) {
					if (conn instanceof SingleConnection) {
						((SingleConnection) conn).getDataSource().getPunisher().countAndPunish(e);
					}
					JDBCExceptionUtils.throwWrappedSQLException(e);
				}
				return updateCount;
			}
		}, sql, null, false, true);
	}

	private int executeUpdateOnConnection(Connection conn, String sql, int autoGeneratedKeys, int[] columnIndexes,
		  String[] columnNames) throws SQLException {
		sql = processSQL(conn, sql);

		Statement stmt = createStatementInternal(conn, false);

		if (autoGeneratedKeys == -1 && columnIndexes == null && columnNames == null) {
			return stmt.executeUpdate(sql);
		} else if (autoGeneratedKeys != -1) {
			return stmt.executeUpdate(sql, autoGeneratedKeys);
		} else if (columnIndexes != null) {
			return stmt.executeUpdate(sql, columnIndexes);
		} else if (columnNames != null) {
			return stmt.executeUpdate(sql, columnNames);
		} else {
			return stmt.executeUpdate(sql);
		}
	}

	protected <T> T executeWithFilter(final JDBCOperationCallback<T> callback, final String sql, Object params,
		  boolean isBatch
		  , final boolean forceWriter) throws SQLException {

		this.context.setSql(sql);
		this.context.setBatch(isBatch);
		this.context.setParams(params);
		this.context.setTransaction(!this.dpGroupConnection.getAutoCommit());
		this.context.setBatchedSqls(batchedSqls);
		final Connection conn = this.dpGroupConnection.getRealConnection(sql, forceWriter);
		this.context.setRealJdbcContext(getRealJdbcMetaData(conn));

		if (filters != null && filters.size() > 0) {
			JdbcFilter chain = new DefaultJdbcFilterChain(filters) {
				@Override
				public T execute(GroupStatement source, JdbcFilter chain) throws SQLException {
					if (index < filters.size()) {
						return filters.get(index++).execute(source, chain);
					} else {
						return source.executeWithFilterOrigin(callback, conn);
					}
				}
			};
			return chain.execute(this, chain);
		} else {
			return executeWithFilterOrigin(callback, conn);
		}
	}

	private <T> T executeWithFilterOrigin(JDBCOperationCallback<T> callback, Connection conn) throws SQLException {
		return callback.doAction(conn);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Statement#getConnection()
	 */
	@Override
	public Connection getConnection() throws SQLException {
		return this.dpGroupConnection;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Statement#getFetchDirection()
	 */
	@Override
	public int getFetchDirection() throws SQLException {
		throw new UnsupportedOperationException("getFetchDirection");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Statement#setFetchDirection(int)
	 */
	@Override
	public void setFetchDirection(int direction) throws SQLException {
		throw new UnsupportedOperationException("setFetchDirection");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Statement#getFetchSize()
	 */
	@Override
	public int getFetchSize() throws SQLException {
		return this.fetchSize;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Statement#setFetchSize(int)
	 */
	@Override
	public void setFetchSize(int fetchSize) throws SQLException {
		this.fetchSize = fetchSize;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Statement#getGeneratedKeys()
	 */
	@Override
	public ResultSet getGeneratedKeys() throws SQLException {
		if (this.openedStatement != null) {
			return new GroupResultSet(this.context.clone(), this.filters, this.openedStatement.getGeneratedKeys());
		} else {
			throw new SQLException("No update operations executed before getGeneratedKeys");
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Statement#getMaxFieldSize()
	 */
	@Override
	public int getMaxFieldSize() throws SQLException {
		throw new UnsupportedOperationException("getMaxFieldSize");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Statement#setMaxFieldSize(int)
	 */
	@Override
	public void setMaxFieldSize(int max) throws SQLException {
		throw new UnsupportedOperationException("setMaxFieldSize");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Statement#getMaxRows()
	 */
	@Override
	public int getMaxRows() throws SQLException {
		return maxRows;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Statement#setMaxRows(int)
	 */
	@Override
	public void setMaxRows(int maxRows) throws SQLException {
		this.maxRows = maxRows;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Statement#getMoreResults()
	 */
	@Override
	public boolean getMoreResults() throws SQLException {
		return moreResults;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Statement#getMoreResults(int)
	 */
	@Override
	public boolean getMoreResults(int current) throws SQLException {
		throw new UnsupportedOperationException("getMoreResults");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Statement#getQueryTimeout()
	 */
	@Override
	public int getQueryTimeout() throws SQLException {
		return queryTimeout;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Statement#setQueryTimeout(int)
	 */
	@Override
	public void setQueryTimeout(int queryTimeout) throws SQLException {
		this.queryTimeout = queryTimeout;
	}

	private JdbcContext getRealJdbcMetaData(Connection conn) {
		if (conn instanceof SingleConnection) {
			return ((SingleConnection) conn).getJdbcContext();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Statement#getResultSet()
	 */
	@Override
	public ResultSet getResultSet() throws SQLException {
		return currentResultSet;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Statement#getResultSetConcurrency()
	 */
	@Override
	public int getResultSetConcurrency() throws SQLException {
		return this.resultSetConcurrency;
	}

	public void setResultSetConcurrency(int resultSetConcurrency) {
		this.resultSetConcurrency = resultSetConcurrency;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Statement#getResultSetHoldability()
	 */
	@Override
	public int getResultSetHoldability() throws SQLException {
		return this.resultSetHoldability;
	}

	public void setResultSetHoldability(int resultSetHoldability) {
		this.resultSetHoldability = resultSetHoldability;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Statement#getResultSetType()
	 */
	@Override
	public int getResultSetType() throws SQLException {
		return this.resultSetType;
	}

	public void setResultSetType(int resultSetType) {
		this.resultSetType = resultSetType;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Statement#getUpdateCount()
	 */
	@Override
	public int getUpdateCount() throws SQLException {
		return this.updateCount;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Statement#getWarnings()
	 */
	@Override
	public SQLWarning getWarnings() throws SQLException {
		checkClosed();
		if (openedStatement != null) {
			return openedStatement.getWarnings();
		}
		return null;
	}

	public boolean isCloseOnCompletion() throws SQLException {
		throw new DalNotSupportException();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Statement#isClosed()
	 */
	@Override
	public boolean isClosed() throws SQLException {
		return closed;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Statement#isPoolable()
	 */
	@Override
	public boolean isPoolable() throws SQLException {
		throw new DalNotSupportException();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Statement#setPoolable(boolean)
	 */
	@Override
	public void setPoolable(boolean poolable) throws SQLException {
		throw new DalNotSupportException();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
	 */
	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return this.getClass().isAssignableFrom(iface);
	}

	protected String processSQL(Connection conn, final String sql) throws SQLException {
		if (filters != null && filters.size() > 0) {
			SingleConnection singleConn = conn instanceof SingleConnection ? (SingleConnection) conn : null;

			JdbcFilter chain = new DefaultJdbcFilterChain(filters) {
				@Override
				public String sql(SingleConnection conn, String sql, JdbcFilter chain) throws SQLException {
					if (index < filters.size()) {
						return filters.get(index++).sql(conn, sql, chain);
					} else {
						return sql;
					}
				}
			};
			return chain.sql(singleConn, sql, chain);
		} else {
		}
		return sql;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Statement#setCursorName(java.lang.String)
	 */
	@Override
	public void setCursorName(String name) throws SQLException {
		throw new UnsupportedOperationException("setCursorName");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Statement#setEscapeProcessing(boolean)
	 */
	@Override
	public void setEscapeProcessing(boolean enable) throws SQLException {
		throw new UnsupportedOperationException("setEscapeProcessing");
	}

	void setRealStatement(Statement realStatement) {
		if (this.openedStatement != null) {
			try {
				this.openedStatement.close();
			} catch (SQLException e) {
				// ignore it
			}
		}
		this.openedStatement = realStatement;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Wrapper#unwrap(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		try {
			return (T) this;
		} catch (Exception e) {
			throw new SQLException(e);
		}
	}

}
