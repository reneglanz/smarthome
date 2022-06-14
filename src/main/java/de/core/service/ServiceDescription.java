package de.core.service;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.core.serialize.Serializable;
import de.core.serialize.annotation.Element;

public class ServiceDescription implements Serializable {

	public static class MethodDescription implements Serializable {
		@Element protected String name;
		@Element protected List<ParamDescription> params = new ArrayList<ParamDescription>();
		@Element protected String returns;

		public void addParam(ParamDescription param) {
			this.params.add(param);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			MethodDescription other = (MethodDescription) obj;
			return Objects.equals(name, other.name);
		}
	}

	public static class ParamDescription implements Serializable {
		@Element
		protected String name;
		@Element
		protected String type;

		public ParamDescription(String name, String type) {
			super();
			this.name = name;
			this.type = type;
		}

		protected ParamDescription() {
		}

		@Override
		public int hashCode() {
			return Objects.hash(name);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ParamDescription other = (ParamDescription) obj;
			return Objects.equals(name, other.name);
		}
	}

	@Element
	protected String name;

	@Element
	protected List<MethodDescription> methods=new ArrayList<>();

	protected ServiceDescription() {}
	
	public static ServiceDescription forService(Service service) {
		ServiceDescription desc = new ServiceDescription();
		desc.name = service.getServiceHandle().toString();
		desc.findMethod(service.getClass());
		return desc;
	}

	private void findMethod(Class<?> clazz) {
		try {
			findMethods0(clazz);
			if (clazz.getSuperclass() != null) {
				findMethod(clazz.getSuperclass());
			}
			Class<?>[] interfaces = clazz.getInterfaces();
			for (Class<?> _interface : interfaces) {
				findMethod(_interface);
			}
		} catch (Throwable t) {}
	}

	private void findMethods0(Class clazz) {
		Method[] methods = clazz.getDeclaredMethods();
		for (Method m : methods) {
			Parameter[] methodParams = m.getParameters();
			Function functionAnn = m.<Function>getAnnotation(Function.class);
			if (functionAnn != null) {
				String mName = (functionAnn.value().length() > 0) ? functionAnn.value() : m.getName();
				MethodDescription desc = new MethodDescription();
				desc.name = mName;
				desc.returns=m.getReturnType().toString();
				for (Parameter p : methodParams) {
					Param paramAnno = p.getAnnotation(Param.class);
					if (paramAnno != null) {
						desc.addParam(new ParamDescription(paramAnno.value(), p.getType().toString()));
					}
				}
				addMethod(desc);
			}
		}
	}
	
	protected void addMethod(MethodDescription mdesc) {
		if(!methods.contains(mdesc)) {
			this.methods.add(mdesc);
		}
	}
}
