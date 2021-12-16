package de.core.serialize;

import java.util.List;
import java.util.Map;

import de.core.CoreException;
import de.core.serialize.annotation.ClassOverride;
import de.core.serialize.elements.Array;
import de.core.serialize.elements.ComplexElement;
import de.core.serialize.elements.Element;
import de.core.serialize.elements.PrimitivElement;
import de.core.serialize.elements.Root;

public class SJOSSerializer {
	public ComplexElement serialize(Object obj) throws CoreException {
		if (SJOSDeserializer.isBaseType(obj.getClass()))
			return serializeBaseType(null, null, obj);
		if (!(obj instanceof Serializable))
			CoreException.throwCoreException(obj.getClass() + " must implements de.core.serialize.Serializable");
		Root root = new Root();
		root.setType(getClass(obj).getName());
		try {
			serialize((ComplexElement) root, obj);
		} catch (Exception e) {
			throw CoreException.throwCoreException(e);
		}
		return (ComplexElement) root;
	}

	private void serialize(ComplexElement root, Object obj) throws CoreException {
		if (!(obj instanceof Serializable))
			CoreException.throwCoreException(obj.getClass() + " must implements de.core.serialize.Serializable");
		List<FieldAccessor> fields = FieldAccessor.FieldAccessorFactory.create(obj.getClass());
		for (FieldAccessor f : fields) {
			if (!f.serialize())
				continue;
			if(f.inline()) {
				if(InlineSerializable.class.isAssignableFrom(f.getType(obj))) {
					PrimitivElement child = new PrimitivElement((Element) root);
					child.setName(f.name());
					child.setValue(((InlineSerializable) f.get(obj)).serialize());
					root.add((Element) child);
				} else if(f.inlineCasses() != null && f.inlineCasses().length > 0) {
					for(Class<InlineSerializable> inlineClass:f.inlineCasses()) {
						if(inlineClass.equals(f.getType(obj))) {
							PrimitivElement child = new PrimitivElement((Element) root);
							child.setName(f.name());
							child.setValue(((InlineSerializable) f.get(obj)).serialize());
							root.add((Element) child);
							break;
						}
					}
				}
				continue;
			}

			if (f.getType(obj).isPrimitive() || (!f.isFieldTypeObject() && f.getType(obj).equals(String.class))
					|| (!f.isFieldTypeObject() && f.getType(obj).isEnum())) {
				if (f.get(obj) != null) {
					PrimitivElement child = new PrimitivElement((Element) root);
					child.setName(f.name());
					child.setValue(f.get(obj));
					root.add((Element) child);
				}
				continue;
			}
			if (SJOSDeserializer.isBaseType(f.getType(obj))) {
				ComplexElement complex = serializeBaseType(null, null, f.get(obj));
				complex.setName(f.name());
				complex.setParent((Element) root);
				root.add((Element) complex);
				continue;
			}
			if (f.getType(obj).isArray()) {
				if (SJOSDeserializer.isPrimitvArray(f.getType(obj))) {
					if (f.get(obj) != null)
						root.add(serializePrimitveArray(f, obj, root));
					continue;
				}
				if (Object[].class.equals(f.getType(obj))) {
					Array array1 = new Array((Element) root);
					array1.setName(f.name());
					Object[] arrayOfObject = (Object[]) f.get(obj);
					if (arrayOfObject != null)
						for (Object object : arrayOfObject) {
							if (SJOSDeserializer.isBaseType(object.getClass())) {
								serializeBaseType((ComplexElement) array1, null, object);
							} else if (SJOSDeserializer.isPrimitvArray(object.getClass())) {
								FieldAccessor fa = new FieldAccessor(null) {
									public Object get(Object o) throws CoreException {
										return o;
									}

									public Class<?> getType(Object obj) throws CoreException {
										return obj.getClass();
									}

									public Class<?> getType() throws CoreException {
										return obj.getClass();
									}

									public String name() {
										return null;
									}
								};
								array1.add(serializePrimitveArray(fa, object, (ComplexElement) array1));
							} else {
								ComplexElement complex = new ComplexElement((Element) array1);
								complex.setType(object.getClass().getName());
								serialize(complex, object);
								array1.add((Element) complex);
							}
						}
					root.add((Element) array1);
					continue;
				}
				Array array = new Array((Element) root);
				array.setName(f.name());
				Object[] oarr = (Object[]) f.get(obj);
				if (oarr != null)
					for (Object object : oarr)
						serializeField(object, (ComplexElement) array, null);
				root.add((Element) array);
				continue;
			}
			if (Map.class.isAssignableFrom(f.getType(obj))) {
				ComplexElement child = new ComplexElement((Element) root);
				child.setName(f.name());
				Map map = (Map) f.get(obj);
				if (map != null)
					for (Object key : map.keySet())
						serializeField(map.get(key), child, key.toString());
				root.add((Element) child);
				continue;
			}
			if (List.class.isAssignableFrom(f.getType(obj))) {
				Array array = new Array((Element) root);
				array.setName(f.name());
				List list = (List) f.get(obj);
				if (list != null)
					for (Object object : list)
						serializeField(object, (ComplexElement) array, null);
				root.add((Element) array);
				continue;
			}
			Object o = f.get(obj);
			if (o != null)
				serializeField(f.get(obj), root, f.name());
		}
	}

	private void serializeField(Object o, ComplexElement root, String name) throws CoreException {
		if (o.getClass().isPrimitive() || o.getClass().equals(String.class)) {
			PrimitivElement primitiv = new PrimitivElement((Element) root);
			primitiv.setName((name != null) ? name : o.toString());
			primitiv.setValue(o.toString());
			root.add((Element) primitiv);
		} else if (o instanceof Class) {
			PrimitivElement primitiv = new PrimitivElement((Element) root);
			primitiv.setValue(((Class) o).getName());
			primitiv.setName(name);
			root.add((Element) primitiv);
		} else if (SJOSDeserializer.isBaseType(o.getClass())) {
			serializeBaseType(root, name, o);
		} else {
			ComplexElement complex = new ComplexElement((Element) root);
			complex.setType(getClass(o).getName());
			complex.setName(name);
			serialize(complex, o);
			root.add((Element) complex);
		}
	}

	private Element serializePrimitveArray(FieldAccessor f, Object obj, ComplexElement root) throws CoreException {
		Array array = new Array((Element) root);
		array.setName(f.name());
		if (int[].class.equals(f.getType(obj))) {
			for (int o : (int[]) f.get(obj))
				array.add((Element) createPrimitive((ComplexElement) array, Integer.valueOf(o)));
		} else if (long[].class.equals(f.getType(obj))) {
			for (long o : (long[]) f.get(obj))
				array.add((Element) createPrimitive((ComplexElement) array, Long.valueOf(o)));
		} else if (double[].class.equals(f.getType(obj))) {
			for (double o : (double[]) f.get(obj))
				array.add((Element) createPrimitive((ComplexElement) array, Double.valueOf(o)));
		} else if (char[].class.equals(f.getType(obj))) {
			for (char o : (char[]) f.get(obj))
				array.add((Element) createPrimitive((ComplexElement) array, Character.valueOf(o)));
		} else if (boolean[].class.equals(f.getType(obj))) {
			for (boolean o : (boolean[]) f.get(obj))
				array.add((Element) createPrimitive((ComplexElement) array, Boolean.valueOf(o)));
		} else if (float[].class.equals(f.getType(obj))) {
			for (float o : (float[]) f.get(obj))
				array.add((Element) createPrimitive((ComplexElement) array, Float.valueOf(o)));
		} else if (String[].class.equals(f.getType(obj))) {
			for (String o : (String[]) f.get(obj))
				array.add((Element) createPrimitive((ComplexElement) array, o));
		} else if (byte[].class.equals(f.getType(obj))) {
			PrimitivElement prim = createPrimitive(root, Coding.toBase64((byte[]) f.get(obj)));
			prim.setName(f.name());
			return (Element) prim;
		}
		return (Element) array;
	}

	private PrimitivElement createPrimitive(ComplexElement root, Object value) {
		PrimitivElement p = new PrimitivElement((Element) root);
		p.setValue(value);
		return p;
	}

	private ComplexElement serializeBaseType(ComplexElement root, String name, Object o) {
		ComplexElement comp = new ComplexElement((Element) root);
		comp.setType(o.getClass().getName());
		comp.setName(name);
		PrimitivElement value = new PrimitivElement((Element) comp);
		value.setName(".");
		value.setValue(o.toString());
		comp.add((Element) value);
		if (root != null)
			root.add((Element) comp);
		return (root != null) ? root : comp;
	}

	private Class<?> getClass(Object obj) {
		ClassOverride anno = obj.getClass().<ClassOverride>getAnnotation(ClassOverride.class);
		if (anno != null)
			return anno.overrides();
		return obj.getClass();
	}
}
