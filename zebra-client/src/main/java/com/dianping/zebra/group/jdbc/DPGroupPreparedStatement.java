/**
 * Project: zebra-client
 * 
 * File Created at Feb 20, 2014
 * 
 */
package com.dianping.zebra.group.jdbc;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

import com.dianping.zebra.group.config.DataSourceConfigManager;
import com.dianping.zebra.group.manager.GroupDataSourceManager;
import com.dianping.zebra.group.router.GroupDataSourceRouter;

/**
 * @author Leo Liang
 * 
 */
public class DPGroupPreparedStatement extends DPGroupStatement implements PreparedStatement{

	private String sql;

	public DPGroupPreparedStatement(GroupDataSourceRouter router, GroupDataSourceManager dataSourceManager,
	      DataSourceConfigManager configManager, DPGroupConnection connection, String sql) {
		super(router, dataSourceManager, configManager, connection);
		this.sql = sql;
	}

	/* (non-Javadoc)
    * @see java.sql.PreparedStatement#executeQuery()
    */
   @Override
   public ResultSet executeQuery() throws SQLException {
	   // TODO Auto-generated method stub
	   return null;
   }

	/* (non-Javadoc)
    * @see java.sql.PreparedStatement#executeUpdate()
    */
   @Override
   public int executeUpdate() throws SQLException {
	   // TODO Auto-generated method stub
	   return 0;
   }

	/* (non-Javadoc)
    * @see java.sql.PreparedStatement#setNull(int, int)
    */
   @Override
   public void setNull(int parameterIndex, int sqlType) throws SQLException {
	   // TODO Auto-generated method stub
	   
   }

	/* (non-Javadoc)
    * @see java.sql.PreparedStatement#setBoolean(int, boolean)
    */
   @Override
   public void setBoolean(int parameterIndex, boolean x) throws SQLException {
	   // TODO Auto-generated method stub
	   
   }

	/* (non-Javadoc)
    * @see java.sql.PreparedStatement#setByte(int, byte)
    */
   @Override
   public void setByte(int parameterIndex, byte x) throws SQLException {
	   // TODO Auto-generated method stub
	   
   }

	/* (non-Javadoc)
    * @see java.sql.PreparedStatement#setShort(int, short)
    */
   @Override
   public void setShort(int parameterIndex, short x) throws SQLException {
	   // TODO Auto-generated method stub
	   
   }

	/* (non-Javadoc)
    * @see java.sql.PreparedStatement#setInt(int, int)
    */
   @Override
   public void setInt(int parameterIndex, int x) throws SQLException {
	   // TODO Auto-generated method stub
	   
   }

	/* (non-Javadoc)
    * @see java.sql.PreparedStatement#setLong(int, long)
    */
   @Override
   public void setLong(int parameterIndex, long x) throws SQLException {
	   // TODO Auto-generated method stub
	   
   }

	/* (non-Javadoc)
    * @see java.sql.PreparedStatement#setFloat(int, float)
    */
   @Override
   public void setFloat(int parameterIndex, float x) throws SQLException {
	   // TODO Auto-generated method stub
	   
   }

	/* (non-Javadoc)
    * @see java.sql.PreparedStatement#setDouble(int, double)
    */
   @Override
   public void setDouble(int parameterIndex, double x) throws SQLException {
	   // TODO Auto-generated method stub
	   
   }

	/* (non-Javadoc)
    * @see java.sql.PreparedStatement#setBigDecimal(int, java.math.BigDecimal)
    */
   @Override
   public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
	   // TODO Auto-generated method stub
	   
   }

	/* (non-Javadoc)
    * @see java.sql.PreparedStatement#setString(int, java.lang.String)
    */
   @Override
   public void setString(int parameterIndex, String x) throws SQLException {
	   // TODO Auto-generated method stub
	   
   }

	/* (non-Javadoc)
    * @see java.sql.PreparedStatement#setBytes(int, byte[])
    */
   @Override
   public void setBytes(int parameterIndex, byte[] x) throws SQLException {
	   // TODO Auto-generated method stub
	   
   }

	/* (non-Javadoc)
    * @see java.sql.PreparedStatement#setDate(int, java.sql.Date)
    */
   @Override
   public void setDate(int parameterIndex, Date x) throws SQLException {
	   // TODO Auto-generated method stub
	   
   }

	/* (non-Javadoc)
    * @see java.sql.PreparedStatement#setTime(int, java.sql.Time)
    */
   @Override
   public void setTime(int parameterIndex, Time x) throws SQLException {
	   // TODO Auto-generated method stub
	   
   }

	/* (non-Javadoc)
    * @see java.sql.PreparedStatement#setTimestamp(int, java.sql.Timestamp)
    */
   @Override
   public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
	   // TODO Auto-generated method stub
	   
   }

	/* (non-Javadoc)
    * @see java.sql.PreparedStatement#setAsciiStream(int, java.io.InputStream, int)
    */
   @Override
   public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
	   // TODO Auto-generated method stub
	   
   }

	/* (non-Javadoc)
    * @see java.sql.PreparedStatement#setUnicodeStream(int, java.io.InputStream, int)
    */
   @Override
   public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
	   // TODO Auto-generated method stub
	   
   }

	/* (non-Javadoc)
    * @see java.sql.PreparedStatement#setBinaryStream(int, java.io.InputStream, int)
    */
   @Override
   public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
	   // TODO Auto-generated method stub
	   
   }

	/* (non-Javadoc)
    * @see java.sql.PreparedStatement#clearParameters()
    */
   @Override
   public void clearParameters() throws SQLException {
	   // TODO Auto-generated method stub
	   
   }

	/* (non-Javadoc)
    * @see java.sql.PreparedStatement#setObject(int, java.lang.Object, int)
    */
   @Override
   public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
	   // TODO Auto-generated method stub
	   
   }

	/* (non-Javadoc)
    * @see java.sql.PreparedStatement#setObject(int, java.lang.Object)
    */
   @Override
   public void setObject(int parameterIndex, Object x) throws SQLException {
	   // TODO Auto-generated method stub
	   
   }

	/* (non-Javadoc)
    * @see java.sql.PreparedStatement#execute()
    */
   @Override
   public boolean execute() throws SQLException {
	   // TODO Auto-generated method stub
	   return false;
   }

	/* (non-Javadoc)
    * @see java.sql.PreparedStatement#addBatch()
    */
   @Override
   public void addBatch() throws SQLException {
	   // TODO Auto-generated method stub
	   
   }

	/* (non-Javadoc)
    * @see java.sql.PreparedStatement#setCharacterStream(int, java.io.Reader, int)
    */
   @Override
   public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
	   // TODO Auto-generated method stub
	   
   }

	/* (non-Javadoc)
    * @see java.sql.PreparedStatement#setRef(int, java.sql.Ref)
    */
   @Override
   public void setRef(int parameterIndex, Ref x) throws SQLException {
	   // TODO Auto-generated method stub
	   
   }

	/* (non-Javadoc)
    * @see java.sql.PreparedStatement#setBlob(int, java.sql.Blob)
    */
   @Override
   public void setBlob(int parameterIndex, Blob x) throws SQLException {
	   // TODO Auto-generated method stub
	   
   }

	/* (non-Javadoc)
    * @see java.sql.PreparedStatement#setClob(int, java.sql.Clob)
    */
   @Override
   public void setClob(int parameterIndex, Clob x) throws SQLException {
	   // TODO Auto-generated method stub
	   
   }

	/* (non-Javadoc)
    * @see java.sql.PreparedStatement#setArray(int, java.sql.Array)
    */
   @Override
   public void setArray(int parameterIndex, Array x) throws SQLException {
	   // TODO Auto-generated method stub
	   
   }

	/* (non-Javadoc)
    * @see java.sql.PreparedStatement#getMetaData()
    */
   @Override
   public ResultSetMetaData getMetaData() throws SQLException {
	   // TODO Auto-generated method stub
	   return null;
   }

	/* (non-Javadoc)
    * @see java.sql.PreparedStatement#setDate(int, java.sql.Date, java.util.Calendar)
    */
   @Override
   public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
	   // TODO Auto-generated method stub
	   
   }

	/* (non-Javadoc)
    * @see java.sql.PreparedStatement#setTime(int, java.sql.Time, java.util.Calendar)
    */
   @Override
   public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
	   // TODO Auto-generated method stub
	   
   }

	/* (non-Javadoc)
    * @see java.sql.PreparedStatement#setTimestamp(int, java.sql.Timestamp, java.util.Calendar)
    */
   @Override
   public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
	   // TODO Auto-generated method stub
	   
   }

	/* (non-Javadoc)
    * @see java.sql.PreparedStatement#setNull(int, int, java.lang.String)
    */
   @Override
   public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
	   // TODO Auto-generated method stub
	   
   }

	/* (non-Javadoc)
    * @see java.sql.PreparedStatement#setURL(int, java.net.URL)
    */
   @Override
   public void setURL(int parameterIndex, URL x) throws SQLException {
	   // TODO Auto-generated method stub
	   
   }

	/* (non-Javadoc)
    * @see java.sql.PreparedStatement#getParameterMetaData()
    */
   @Override
   public ParameterMetaData getParameterMetaData() throws SQLException {
	   // TODO Auto-generated method stub
	   return null;
   }

	/* (non-Javadoc)
    * @see java.sql.PreparedStatement#setRowId(int, java.sql.RowId)
    */
   @Override
   public void setRowId(int parameterIndex, RowId x) throws SQLException {
	   // TODO Auto-generated method stub
	   
   }

	/* (non-Javadoc)
    * @see java.sql.PreparedStatement#setNString(int, java.lang.String)
    */
   @Override
   public void setNString(int parameterIndex, String value) throws SQLException {
	   // TODO Auto-generated method stub
	   
   }

	/* (non-Javadoc)
    * @see java.sql.PreparedStatement#setNCharacterStream(int, java.io.Reader, long)
    */
   @Override
   public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
	   // TODO Auto-generated method stub
	   
   }

	/* (non-Javadoc)
    * @see java.sql.PreparedStatement#setNClob(int, java.sql.NClob)
    */
   @Override
   public void setNClob(int parameterIndex, NClob value) throws SQLException {
	   // TODO Auto-generated method stub
	   
   }

	/* (non-Javadoc)
    * @see java.sql.PreparedStatement#setClob(int, java.io.Reader, long)
    */
   @Override
   public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
	   // TODO Auto-generated method stub
	   
   }

	/* (non-Javadoc)
    * @see java.sql.PreparedStatement#setBlob(int, java.io.InputStream, long)
    */
   @Override
   public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
	   // TODO Auto-generated method stub
	   
   }

	/* (non-Javadoc)
    * @see java.sql.PreparedStatement#setNClob(int, java.io.Reader, long)
    */
   @Override
   public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
	   // TODO Auto-generated method stub
	   
   }

	/* (non-Javadoc)
    * @see java.sql.PreparedStatement#setSQLXML(int, java.sql.SQLXML)
    */
   @Override
   public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
	   // TODO Auto-generated method stub
	   
   }

	/* (non-Javadoc)
    * @see java.sql.PreparedStatement#setObject(int, java.lang.Object, int, int)
    */
   @Override
   public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
	   // TODO Auto-generated method stub
	   
   }

	/* (non-Javadoc)
    * @see java.sql.PreparedStatement#setAsciiStream(int, java.io.InputStream, long)
    */
   @Override
   public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
	   // TODO Auto-generated method stub
	   
   }

	/* (non-Javadoc)
    * @see java.sql.PreparedStatement#setBinaryStream(int, java.io.InputStream, long)
    */
   @Override
   public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
	   // TODO Auto-generated method stub
	   
   }

	/* (non-Javadoc)
    * @see java.sql.PreparedStatement#setCharacterStream(int, java.io.Reader, long)
    */
   @Override
   public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
	   // TODO Auto-generated method stub
	   
   }

	/* (non-Javadoc)
    * @see java.sql.PreparedStatement#setAsciiStream(int, java.io.InputStream)
    */
   @Override
   public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
	   // TODO Auto-generated method stub
	   
   }

	/* (non-Javadoc)
    * @see java.sql.PreparedStatement#setBinaryStream(int, java.io.InputStream)
    */
   @Override
   public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
	   // TODO Auto-generated method stub
	   
   }

	/* (non-Javadoc)
    * @see java.sql.PreparedStatement#setCharacterStream(int, java.io.Reader)
    */
   @Override
   public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
	   // TODO Auto-generated method stub
	   
   }

	/* (non-Javadoc)
    * @see java.sql.PreparedStatement#setNCharacterStream(int, java.io.Reader)
    */
   @Override
   public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
	   // TODO Auto-generated method stub
	   
   }

	/* (non-Javadoc)
    * @see java.sql.PreparedStatement#setClob(int, java.io.Reader)
    */
   @Override
   public void setClob(int parameterIndex, Reader reader) throws SQLException {
	   // TODO Auto-generated method stub
	   
   }

	/* (non-Javadoc)
    * @see java.sql.PreparedStatement#setBlob(int, java.io.InputStream)
    */
   @Override
   public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
	   // TODO Auto-generated method stub
	   
   }

	/* (non-Javadoc)
    * @see java.sql.PreparedStatement#setNClob(int, java.io.Reader)
    */
   @Override
   public void setNClob(int parameterIndex, Reader reader) throws SQLException {
	   // TODO Auto-generated method stub
	   
   }

}
