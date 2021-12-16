package de.core.serialize;

import de.core.CoreException;
import de.core.serialize.elements.Element;

public interface InlineSerializable extends Serializable {

	public class InlineSerializeException extends CoreException {
		private static final long serialVersionUID = -2192982008392235463L;

		public InlineSerializeException(String message, Throwable cause) {
			super(message, cause);
		}

		public InlineSerializeException(String message) {
			super(message);
		}

		public InlineSerializeException(Throwable cause) {
			super(cause);
		}
	}
	
	String serialize() throws CoreException;
	void deserialize(Element element) throws InlineSerializeException;
}
