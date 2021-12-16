package de.shd.ui;

import de.core.CoreException;
import de.core.serialize.Coding;

public interface Template {

	public default String tempalte() throws CoreException {
		try {
			return new String(Coding.encode(this.getClass().newInstance()));
		} catch (Exception e) {
			CoreException.throwCoreException(e);
		}
		return null;
	}
}
