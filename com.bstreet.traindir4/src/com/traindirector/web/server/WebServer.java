package com.traindirector.web.server;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.traindirector.Application;

public class WebServer extends AbstractHandler implements Runnable {

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {
		response.setContentType("text/html;charset=utf-8");
		
		if (target.startsWith("/switchboard/")) {
			String result = Application._simulator.getSwitchboardPage(target.substring(13), "/switchboard/");
			response.getWriter().println(result);
		} else {
			response.getWriter().println("Request not found: " + target);
		}
		response.setStatus(HttpServletResponse.SC_OK);
		baseRequest.setHandled(true);
	}

	@Override
	public void run() {
		try {
			Server server = new Server(8081);
			server.setHandler(new WebServer());
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
}
