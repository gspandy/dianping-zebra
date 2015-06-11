package com.dianping.zebra.admin.service.impl;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.zebra.admin.dao.SyncServerMonitorMapper;
import com.dianping.zebra.admin.entity.SyncServerMonitorEntity;
import com.dianping.zebra.admin.service.SyncServerMonitorService;

/**
 * Dozer @ 6/4/15 mail@dozer.cc http://www.dozer.cc
 */
@Service
public class SyncServerMonitorServiceImpl implements SyncServerMonitorService {

	@Autowired
	private SyncServerMonitorMapper syncServerMonitorMapper;

	private String localIpAddress;

	@PostConstruct
	public void init() throws UnknownHostException {
		this.localIpAddress = InetAddress.getLocalHost().getHostAddress();
	}

	@Override
	public SyncServerMonitorEntity chooseOne() {
		List<SyncServerMonitorEntity> servers = syncServerMonitorMapper.getAllAlive();

		SyncServerMonitorEntity target = null;
		for (SyncServerMonitorEntity server : servers) {
			if (target == null || target.getLoad() > server.getLoad()) {
				target = server;
			}
		}
		return target;
	}

	@Override
	public List<SyncServerMonitorEntity> getAllAlive() {
		return syncServerMonitorMapper.getAllAlive();
	}

	@Override
	public void uploadStatus() {
		SyncServerMonitorEntity item = new SyncServerMonitorEntity();
		item.setName(this.localIpAddress);
		OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
		item.setLoad(os.getSystemLoadAverage() / os.getAvailableProcessors());

		int row = syncServerMonitorMapper.update(item);
		if (row == 0) {
			syncServerMonitorMapper.create(item);
		}
	}
}