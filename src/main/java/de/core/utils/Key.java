package de.core.utils;

import java.security.MessageDigest;

import de.core.CoreException;
import de.core.serialize.Coding;

public class Key {

	public static String createKey(String s) throws CoreException {
		try {
			MessageDigest digest=MessageDigest.getInstance("MD5");
			return Coding.toBase64(digest.digest(s.getBytes("UTF-8")));
		} catch(Throwable t) {
			CoreException.throwCoreException(t);
		}
		return null;
	}
}
