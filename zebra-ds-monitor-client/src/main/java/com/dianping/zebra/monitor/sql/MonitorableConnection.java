/**
 * Project: zebra-sql-monitor-client
 * 
 * File Created at 2011-10-27
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
package com.dianping.zebra.monitor.sql;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;

/**
 * 对于实际<code>Connection</code>的包装，提供monitor功能
 * @author danson.liu
 *
 */
public class MonitorableConnection implements Connection {

	private final Connection innerConnection;

	public MonitorableConnection(Connection connection) {
		this.innerConnection = connection;
	}

	@Override
	public Statement createStatement() throws SQLException {
		return new MonitorableStatement(innerConnection.createStatement(), this);
	}

	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		return new MonitorableStatement(innerConnection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability), this);
	}

	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
		return new MonitorableStatement(innerConnection.createStatement(resultSetType, resultSetConcurrency), this);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		return new MonitorablePreparedStatement(innerConnection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability), sql, this);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
		return new MonitorablePreparedStatement(innerConnection.prepareStatement(sql, autoGeneratedKeys), sql, this);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
		return new MonitorablePreparedStatement(innerConnection.prepareStatement(sql, columnIndexes), sql, this);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
		return new MonitorablePreparedStatement(innerConnection.prepareStatement(sql, columnNames), sql, this);
	}

	@Override
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return new MonitorablePreparedStatement(innerConnection.prepareStatement(sql), sql, this);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		return new MonitorablePreparedStatement(innerConnection.prepareStatement(sql, resultSetType, resultSetConcurrency), sql, this);
	}

	@Override
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		return new MonitorableCallableStatement(innerConnection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability), sql, this);
	}

	@Override
	public CallableStatement prepareCall(String sql) throws SQLException {
		return new MonitorableCallableStatement(innerConnection.prepareCall(sql), sql, this);
	}

	@Override
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		return new MonitorableCallableStatement(innerConnection.prepareCall(sql, resultSetType, resultSetConcurrency), sql, this);
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return innerConnection.unwrap(iface);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return innerConnection.isWrapperFor(iface);
	}

	@Override
	public String nativeSQL(String sql) throws SQLException {
		return innerConnection.nativeSQL(sql);
	}

	@Override
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		innerConnection.setAutoCommit(autoCommit);
	}

	@Override
	public boolean getAutoCommit() throws SQLException {
		return innerConnection.getAutoCommit();
	}

	@Override
	public void commit() throws SQLException {
		innerConnection.commit();
	}

	@Override
	public void rollback() throws SQLException {
		innerConnection.rollback();
	}

	@Override
	public void close() throws SQLException {
		innerConnection.close();
	}

	@Override
	public boolean isClosed() throws SQLException {
		return innerConnection.isClosed();
	}

	@Override
	public DatabaseMetaData getMetaData() throws SQLException {
		return innerConnection.getMetaData();
	}

	@Override
	public void setReadOnly(boolean readOnly) throws SQLException {
		innerConnection.setReadOnly(readOnly);
	}

	@Override
	public boolean isReadOnly() throws SQLException {
		return innerConnection.isReadOnly();
	}

	@Override
	public void setCatalog(String catalog) throws SQLException {
		innerConnection.setCatalog(catalog);
	}

	@Override
	public String getCatalog() throws SQLException {
		return innerConnection.getCatalog();
	}

	@Override
	public void setTransactionIsolation(int level) throws SQLException {
		innerConnection.setTransactionIsolation(level);
	}

	@Override
	public int getTransactionIsolation() throws SQLException {
		return innerConnection.getTransactionIsolation();
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		return innerConnection.getWarnings();
	}

	@Override
	public void clearWarnings() throws SQLException {
		innerConnection.clearWarnings();
	}

	@Override
	public Map<String, Class<?>> getTypeMap() throws SQLException {
		return innerConnection.getTypeMap();
	}

	@Override
	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		innerConnection.setTypeMap(map);
	}

	@Override
	public void setHoldability(int holdability) throws SQLException {
		innerConnection.setHoldability(holdability);
	}

	@Override
	public int getHoldability() throws SQLException {
		return innerConnection.getHoldability();
	}

	@Override
	public Savepoint setSavepoint() throws SQLException {
		return innerConnection.setSavepoint();
	}

	@Override
	public Savepoint setSavepoint(String name) throws SQLException {
		return innerConnection.setSavepoint(name);
	}

	@Override
	public void rollback(Savepoint savepoint) throws SQLException {
		innerConnection.rollback(savepoint);
	}

	@Override
	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		innerConnection.releaseSavepoint(savepoint);
	}

	@Override
	public Clob createClob() throws SQLException {
		return innerConnection.createClob();
	}

	@Override
	public Blob createBlob() throws SQLException {
		return innerConnection.createBlob();
	}

	@Override
	public NClob createNClob() throws SQLException {
		return innerConnection.createNClob();
	}

	@Override
	public SQLXML createSQLXML() throws SQLException {
		return innerConnection.createSQLXML();
	}

	@Override
	public boolean isValid(int timeout) throws SQLException {
		return innerConnection.isValid(timeout);
	}

	@Override
	public void setClientInfo(String name, String value) throws SQLClientInfoException {
		innerConnection.setClientInfo(name, value);
	}

	@Override
	public void setClientInfo(Properties properties) throws SQLClientInfoException {
		innerConnection.setClientInfo(properties);
	}

	@Override
	public String getClientInfo(String name) throws SQLException {
		return innerConnection.getClientInfo(name);
	}

	@Override
	public Properties getClientInfo() throws SQLException {
		return innerConnection.getClientInfo();
	}

	@Override
	public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
		return innerConnection.createArrayOf(typeName, elements);
	}

	@Override
	public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
		return innerConnection.createStruct(typeName, attributes);
	}

	public Connection getInnerConnection() {
		return innerConnection;
	}
}
