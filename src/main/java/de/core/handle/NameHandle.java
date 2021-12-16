package de.core.handle;

import de.core.CoreException;
import de.core.serialize.InlineSerializable;
import de.core.serialize.annotation.Element;
import de.core.serialize.elements.PrimitivElement;

public class NameHandle implements Handle, InlineSerializable {
	@Element
	protected String name;

	protected NameHandle() {
	}

	public NameHandle(String name) {
		this.name = name;
	}

	public String serialize() throws CoreException {
		return this.name;
	}

	public void deserialize(de.core.serialize.elements.Element element) throws InlineSerializeException {
		if(element instanceof PrimitivElement) {
			this.name = ((PrimitivElement)element).asString();
		} else {
			throw new InlineSerializeException("Only primitive Elements allowed");
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NameHandle other = (NameHandle) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public String toString() {
		return this.name;
	}
}
