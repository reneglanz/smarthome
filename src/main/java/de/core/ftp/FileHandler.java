package de.core.ftp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileHandler {
  List<IFtpFileHandler> handler = Collections.synchronizedList(new ArrayList<>());
}
