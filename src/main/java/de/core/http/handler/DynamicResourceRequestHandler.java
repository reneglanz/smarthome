package de.core.http.handler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import de.core.CoreException;
import de.core.Env;
import de.core.http.HttpRequest;
import de.core.log.Logger;
import de.core.rt.Resource;
import de.core.server.admin.AdminService;
import de.core.service.Services;

public class DynamicResourceRequestHandler extends AbstractFileRequestHandler implements Resource {

	protected Map<String,Map<String,HttpResource>> resources=Collections.synchronizedMap(new HashMap<String,Map<String,HttpResource>>());
	protected AdminService adminService;
	protected static final Logger log=Logger.createLogger("DynamicResourceRequestHandler");
	
	@Override
	public HttpResource getRequestedResource(HttpRequest request) {
		String path=request.getRequestPath();
		try {
			String token[]=(path.startsWith("/")?path.substring(1):path).split("/");
			if(token.length==3) {
				String owner=token[1];
				String id=token[2];
				Map<String,HttpResource> list=resources.get(owner);
				if(list!=null) {
					return list.get(id);
				}			
			} else {
				return null;
			}
		} catch(Throwable t) {
			log.error("No such resource found " + path);
		}
		return null;
	}
	
	public String add(String owner, HttpResource res) throws CoreException {
		Map<String,HttpResource> list=resources.get(owner);
		if(list==null) {
			list=Collections.synchronizedMap(new HashMap<>());
			resources.put(owner, list);
		}
		if(adminService==null) {
			adminService=Services.get(AdminService.class);
		}
		String endPoint=adminService.getEndpointUrl(this.forConnector);
		String url=endPoint+(contextPath.startsWith("/")?contextPath:"/"+contextPath)+"/"+owner+"/"+res.getName();
		list.put(res.getName(),res);
		return url;
	}

	public void finish() {
		try {
			Env.put(DynamicResourceRequestHandler.class, this);
			this.adminService=Services.get(AdminService.class);
		} catch(CoreException e) {}
	}
}
