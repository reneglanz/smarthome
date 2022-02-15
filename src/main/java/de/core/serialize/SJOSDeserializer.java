package de.core.serialize;

import de.core.CoreException;
import de.core.Env;
import de.core.serialize.InlineSerializable.InlineSerializeException;
import de.core.serialize.elements.ComplexElement;
import de.core.serialize.elements.Element;
import de.core.serialize.elements.PrimitivElement;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SJOSDeserializer {
	public <T> T deserialze(ComplexElement root, Class<?> clazz) throws CoreException {
		try {
			ClassAccessor classAccessor = null;
			if (clazz == null) {
				classAccessor = ClassAccessor.create(root.getType());
			} else {
				classAccessor = ClassAccessor.create(clazz);
			}
			if (!classAccessor.isSerializable() && !classAccessor.isBaseType())
				CoreException.throwCoreException(clazz + " must implements de.core.serialize.Serializable");
			if (classAccessor.isInterface()) {
				CoreException.throwCoreException("Can not create an instance of an Object");
			}
			if (classAccessor.isBaseType())
				return deserialzeBaseType(root);
			Object object = classAccessor.newInstance();
			List<FieldAccessor> fields = FieldAccessor.FieldAccessorFactory.create(classAccessor.get());
			for (FieldAccessor f : fields) {
				if (f.deserialize()) {
					Element value = root.getChild(f.name());
					if (value != null) {
						handleField(object, f, value);
						continue;
					}
					f.setDefaultValue(object);
					if (!f.inject(object) && f.mandatory())
						CoreException.throwCoreException("Missing madndatory filed " + f.name());
					continue;
				}
				f.setDefaultValue(object);
				if (!f.inject(object) && f.mandatory())
					CoreException.throwCoreException("Missing madndatory filed " + f.name());
			}
			if (classAccessor.isInjectable())
				Env.put(classAccessor.getInjectKey(), object);
			callfinish(object);
			return (T) object;
		} catch (Throwable t) {
			throw CoreException.throwCoreException(t);
		}
	}

	private <T> T deserialzeBaseType(ComplexElement root) throws ClassNotFoundException, CoreException {
		ClassAccessor classAccessor = ClassAccessor.create(root.getType());
		PrimitivElement value = (PrimitivElement) root.getChild(".");
		return (T) value.as(classAccessor.get());
	}

	@SuppressWarnings("unchecked")
	private void handleField(Object obj, FieldAccessor f, Element value) throws CoreException, ClassNotFoundException {
		boolean handledInline=false;
		if(f.inline()) {
			if(InlineSerializable.class.isAssignableFrom(f.getType(obj))){
				ClassAccessor cla = ClassAccessor.create(f.getType(obj));
				if(cla != null) {
					PrimitivElement prim = (PrimitivElement) value;
					InlineSerializable inline = (InlineSerializable) cla.newInstance();
					inline.deserialize(prim);
					f.set(obj, inline);
					handledInline=true;
				}
			} else if(f.inlineCasses() != null && f.inlineCasses().length > 0) {
				for(Class<InlineSerializable> inlineClass:f.inlineCasses()) {
					ClassAccessor cla=ClassAccessor.create(inlineClass);
					if(cla!=null) {
						try {
							InlineSerializable inline=(InlineSerializable)cla.newInstance();
							if(inline!=null) {
								inline.deserialize(value);
								f.set(obj, inline);
								handledInline=true;
								break;
							}
						} catch(InlineSerializeException e) {}
					}
				}
			}
			if(handledInline) {
				return;
			}
		} 

		if (f.getType(obj).isPrimitive() || f.getType(obj) == String.class) {
			if (value instanceof PrimitivElement)
				f.set(obj, ((PrimitivElement) value).as(f.getType(obj)));
		} else if (f.getType(obj).isArray()) {
			if (f.getType(obj).equals(byte[].class)) {
				PrimitivElement prim = (PrimitivElement) value;
				if (!prim.isNull()) {
					byte[] arr = Coding.fromBase64(prim.asString());
					f.set(obj, arr);
				}
			} else {
				ComplexElement complex = (ComplexElement) value;
				Class<?> componentType = f.getType(obj).getComponentType();
				Object[] arr = (Object[]) Array.newInstance(componentType, complex.getSize());
				boolean primArray = isPrimitvArray(f.getType(obj));
				for (int i = 0; i < complex.getSize(); i++) {
					if (primArray) {
						PrimitivElement prim = (PrimitivElement) complex.getChildren().get(i);
						arr[i] = prim.as(componentType);
					} else {
						arr[i] = deserialze((ComplexElement) complex.getChildren().get(i),
								componentType.equals(Object.class) ? null : componentType);
					}
				}
				f.set(obj, arr);
			}
		} else if (Map.class.isAssignableFrom(f.getType(obj))) {
			HashMap<Object, Object> map = new HashMap<>();
			ComplexElement complex = (ComplexElement) value;
			for (Element child : complex.getChildren()) {
				if (child instanceof PrimitivElement) {
					map.put(child.getName(), ((PrimitivElement) child).asString());
					continue;
				}
				if (child instanceof ComplexElement) {
					ComplexElement childcomplex = (ComplexElement) child;
					try {
						ClassAccessor ca = null;
						if (childcomplex.getType() != null) {
							ca = ClassAccessor.create(childcomplex.getType());
						} else {
							ca = ClassAccessor.create(f.getGenericType());
						}
						map.put(child.getName(), deserialze(childcomplex, ca.get()));
					} catch (Exception e) {
						throw CoreException.throwCoreException(e);
					}
				}
			}
			f.set(obj, map);
		} else if (List.class.isAssignableFrom(f.getType(obj))) {
			ArrayList<Object> list = new ArrayList();
			ComplexElement complex = (ComplexElement) value;
			for (Element child : complex.getChildren()) {
				if (child instanceof ComplexElement) {
					list.add(deserialze((ComplexElement) child, Class.forName(((ComplexElement) child).getType())));
					continue;
				}
				if (f.getGenericType() != null) {
					list.add(((PrimitivElement) child).as(f.getGenericType()));
					continue;
				}
				CoreException.throwCoreException(
						"Missing generic type in Element annotation for " + obj.getClass() + "." + f.name());
			}
			f.set(obj, list);
		} else if (f.getType(obj).isEnum()) {
			PrimitivElement prim = (PrimitivElement) value;
			f.set(obj, Enum.valueOf(f.getType(obj), prim.asString()));
		} else if (Class.class.isAssignableFrom(f.getType(obj))) {
			PrimitivElement prim = (PrimitivElement) value;
			f.set(obj, Class.forName(prim.asString()));
		} else {
			ComplexElement complex = (ComplexElement) value;
			ClassAccessor classAccessor = null;
			if (complex.getType() != null) {
				classAccessor = ClassAccessor.create(complex.getType());
			} else {
				classAccessor = ClassAccessor.create(f.getType());
			}
			if (f.getType(obj).isAssignableFrom(classAccessor.get())) {
				f.set(obj, deserialze(complex, classAccessor.get()));
			} else {
				CoreException.throwCoreException(
						"Type of ComplexElement does not match field type. Expected " + f.getType(obj).toString());
			}
		}
	}

	private void callfinish(Object o)
			throws SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		try {
			Method m = o.getClass().getMethod("finish", new Class[0]);
			if (m != null)
				m.invoke(o, new Object[0]);
		} catch (NoSuchMethodException noSuchMethodException) {
		}
	}

	protected static boolean isPrimitvArray(Class<?> clazz) {
		if (int[].class.equals(clazz) || long[].class.equals(clazz) || byte[].class.equals(clazz)
				|| boolean[].class.equals(clazz) || char[].class.equals(clazz) || double[].class.equals(clazz)
				|| float[].class.equals(clazz) || short[].class.equals(clazz) || String[].class.equals(clazz))
			return true;
		return false;
	}

	protected static boolean isBaseType(Class<?> clazz) {
		if (int.class.equals(clazz) || long.class.equals(clazz) || byte.class.equals(clazz)
				|| boolean.class.equals(clazz) || char.class.equals(clazz) || double.class.equals(clazz)
				|| float.class.equals(clazz) || short.class.equals(clazz) || String.class.equals(clazz)
				|| Integer.class.equals(clazz) || Long.class.equals(clazz) || Byte.class.equals(clazz)
				|| Boolean.class.equals(clazz) || Character.class.equals(clazz) || Double.class.equals(clazz)
				|| Float.class.equals(clazz) || clazz.isEnum())
			return true;
		return false;
	}
}
