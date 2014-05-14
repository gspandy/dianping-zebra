package com.dianping.zebra.admin.admin.page.service;

import com.dianping.zebra.admin.admin.AdminPage;
import org.unidal.web.mvc.view.BaseJspViewer;

public class JspViewer extends BaseJspViewer<AdminPage, Action, Context, Model> {
	@Override
	protected String getJspFilePath(Context ctx, Model model) {
		Action action = model.getAction();

		switch (action) {
		case VIEW:
		case MARKDOWN:
		case MARKUP:
		case REMOVE:
		case GETCONFIG:
			return JspFile.VIEW.getPath();
		}

		throw new RuntimeException("Unknown action: " + action);
	}
}