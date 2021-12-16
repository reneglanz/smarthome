package de.core.serialize.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public interface Parser {
  void parse(Reader paramInputStream, CodingReadHandler paramCodingReadHandler) throws IOException;
}
