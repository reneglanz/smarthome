package de.core.log;

import de.core.CoreException;
import de.core.rt.Releasable;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileAppender implements Appender, Releasable {
	protected BufferedWriter writer;
	protected Path logdir;
	protected Path logfile;
	protected String logfileNamePattern = "unifiedserver.%s.%d.log";
	protected String dateFormat = "yyyyMMdd";
	protected SimpleDateFormat df = new SimpleDateFormat(this.dateFormat);
	protected String startDate;

	public FileAppender(Path logdir) {
		this.logdir = logdir;
	}

	public void write(String text) {
		synchronized (this) {
			if (this.logfile == null || !checkDate())
				try {
					createFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			try {
				this.writer.write(text);
				this.writer.newLine();
				this.writer.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void createFile() throws IOException {
		String fileName = "";
		int i = 0;
		checkDate();
		while (true) {
			fileName = String.format(this.logfileNamePattern, new Object[] { this.startDate, Integer.valueOf(i) });
			Path path = Paths.get(this.logdir.toString(), new String[] { fileName });
			if (!Files.exists(path, new java.nio.file.LinkOption[0])) {
				this.logfile = path;
				Files.createFile(this.logfile, (FileAttribute<?>[]) new FileAttribute[0]);
				this.writer = new BufferedWriter(new FileWriter(this.logfile.toFile()));
				break;
			}
			i++;
		}
	}

	private boolean checkDate() {
		if (this.startDate == null) {
			this.startDate = this.df.format(new Date());
			return false;
		}
		String tmp = this.df.format(new Date());
		if (!this.startDate.equals(tmp)) {
			this.startDate = tmp;
			return false;
		}
		return true;
	}

	public void release() throws CoreException {
		if (this.writer != null)
			try {
				this.writer.close();
			} catch (IOException iOException) {
			}
	}
}
