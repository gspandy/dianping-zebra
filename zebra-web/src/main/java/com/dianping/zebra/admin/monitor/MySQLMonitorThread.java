package com.dianping.zebra.admin.monitor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import com.dianping.zebra.admin.monitor.handler.HaHandler;
import com.dianping.zebra.group.config.datasource.entity.DataSourceConfig;

public class MySQLMonitorThread extends Thread {

	private DataSourceConfig config;

	private MonitorConfig monitorConfig;

	private volatile long lastUpdatedTime = 0L;

	private Status currentState = Status.ALIVE;

	private HaHandler hahandler;
	
	public MySQLMonitorThread(MonitorConfig monitorConfig, DataSourceConfig config,HaHandler haHandler) {
		this.monitorConfig = monitorConfig;
		this.config = config;
		this.hahandler = haHandler;
	}

	private void close(Connection con, Statement stmt) {
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException ignore) {
			}
		}
		if (con != null) {
			try {
				con.close();
			} catch (SQLException ingore) {
			}
		}
	}

	public Status getCurrentState() {
		return currentState;
	}

	public long getLastUpdatedTime() {
		return lastUpdatedTime;
	}

	@Override
	public void run() {
		// 如果该库是active=false的状态，则自动ping检测markup
		if (!config.getActive()) {
			currentState = Status.DEAD;
			FixedLengthLinkedList timestamp = new FixedLengthLinkedList(monitorConfig.getPingFailLimit(),
			      monitorConfig.getValidPeriod());

			while (!Thread.currentThread().isInterrupted()) {
				Connection con = null;
				Statement stmt = null;

				try {
					con = DriverManager.getConnection(config.getJdbcUrl(), config.getUsername(), config.getPassword());
					stmt = con.createStatement();
					stmt.setQueryTimeout(monitorConfig.getQueryTimeout());
					stmt.executeQuery(monitorConfig.getTestSql());

					lastUpdatedTime = System.currentTimeMillis();
					timestamp.addLast(lastUpdatedTime);

					if (timestamp.shouldAction()) {
						hahandler.markup(config.getId());

						System.out.println("markup " + config.getId());
						break;
					}
				} catch (SQLException ignore) {
					// 如果不能连上，则清空队列中正常的次数；
					timestamp.clear();
				} finally {
					close(con, stmt);
				}

				try {
					TimeUnit.SECONDS.sleep(monitorConfig.getPingIntervalSeconds());
				} catch (InterruptedException e) {
					break;
				}
			}
		}

		// 如果该库是active=true的状态，则自动ping检测markdown
		FixedLengthLinkedList timestamp = new FixedLengthLinkedList(monitorConfig.getPingFailLimit(),
		      monitorConfig.getValidPeriod());
		currentState = Status.ALIVE;
		while (!Thread.currentThread().isInterrupted()) {
			Connection con = null;
			Statement stmt = null;

			try {
				con = DriverManager.getConnection(config.getJdbcUrl(), config.getUsername(), config.getPassword());
				stmt = con.createStatement();
				stmt.setQueryTimeout(monitorConfig.getQueryTimeout());
				stmt.executeQuery(monitorConfig.getTestSql());

				lastUpdatedTime = System.currentTimeMillis();

				// 如果能连上，则清空队列中的异常；因为要求连续的异常
				timestamp.clear();
			} catch (SQLException e) {
				timestamp.addLast(lastUpdatedTime);

				if (timestamp.shouldAction()) {
					hahandler.markdown(config.getId());
					System.out.println("markdown " + config.getId());

					break;
				}
			} finally {
				close(con, stmt);
			}

			try {
				TimeUnit.SECONDS.sleep(monitorConfig.getPingIntervalSeconds());
			} catch (InterruptedException e) {
				break;
			}
		}
	}

	public void terminate() {
		this.interrupt();
	}

	public static class FixedLengthLinkedList extends LinkedList<Long> {
		private static final long serialVersionUID = 3800705659963203862L;

		private final int maxLength;

		private final long validPeriod;

		public FixedLengthLinkedList(int maxLength, long validPeriod) {
			this.maxLength = maxLength;
			this.validPeriod = validPeriod;
		}

		public void addLast(Long e) {
			if (this.size() >= this.maxLength) {
				super.removeFirst();
			}

			super.addLast(e);
		}

		public long getDistance() {
			return super.getLast() - super.getFirst();
		}

		public boolean shouldAction() {
			return (size() == maxLength) && (getDistance() <= validPeriod);
		}
	}
}