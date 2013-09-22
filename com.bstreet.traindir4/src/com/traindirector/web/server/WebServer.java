package com.traindirector.web.server;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.traindirector.Application;
import com.traindirector.uicomponents.WebContent;

public class WebServer extends AbstractHandler implements Runnable {

	Map<String, WebContent> _handlers;
	
	public WebServer() {
		super();
		_handlers = new HashMap<String, WebContent>();
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {
		response.setContentType("text/html;charset=utf-8");
		
		String cmd = target;
		if (cmd.startsWith("/"))
			cmd = cmd.substring(1);
		int indx = cmd.indexOf("/");
		if (indx > 0)
			cmd = cmd.substring(0, indx);
		WebContent provider = _handlers.get(cmd);
		if (provider != null) {
			provider.doLink(target);
			String result = provider.getHTML();
			response.getWriter().println(result);
		} else {
			// TODO: handle non-text files
			File file = WebContent.getResourceFile(target);
			if (file != null) {
				List<String> content = WebContent.getFileContent(file);
				StringBuilder sb = new StringBuilder();
				for (String s : content) {
					sb.append(s);
					sb.append("\n");
				}
				response.getWriter().println(sb.toString());
			}
			else
				response.getWriter().println("Request not found: " + target);
		}
		response.setStatus(HttpServletResponse.SC_OK);
		baseRequest.setHandled(true);
	}

	@Override
	public void run() {
		try {
			Server server = new Server(Application._simulator._options._serverPort._intValue);
			server.setHandler(this);
			server.start();
			server.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void startServer() {
		Thread t = new Thread(this);
		t.start();
	}

	public void addContent(WebContent provider) {
		_handlers.put(provider.getEndpoint(), provider);
	}
}
