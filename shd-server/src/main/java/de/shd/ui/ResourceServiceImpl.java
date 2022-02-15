package de.shd.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;

import de.core.CoreException;
import de.core.rt.Launchable;
import de.core.serialize.Coding;
import de.core.serialize.annotation.Element;
import de.core.service.Function;
import de.shd.ui.ResourceFile.Entry;

public class ResourceServiceImpl implements Launchable, ResourceService {
	@Element protected String id;
	@Element protected String baseDir;
	@Element(defaultValue = "resources.sjos") protected String filename = "resources.sjos";
	@Element(defaultValue="true") protected boolean relative=true;
	
	protected int maxfilesize=1024*1024*10;
	protected Path root;
	protected ResourceFile resources;

	public ResourceFile create() throws Exception {
		ResourceFile rf = new ResourceFile();
		root = Paths.get(baseDir);
		create(root, rf);
		return rf;
	}

	private void create(Path root, ResourceFile rf) throws Exception {
		DirectoryStream<Path> dirStream = Files.newDirectoryStream(root);
		for (Path path : dirStream) {
			if (Files.isDirectory(path)) {
				create(path, rf);
				continue;
			}
			rf.add(relative?path.toString().replace(this.root.toString()+File.separator, ""):path.toString(), createHash(path));
		}
	}

	@Override
	@Function
	public void update() throws CoreException {
		try {
			ResourceFile rfOld = get();
			Path root = Paths.get(baseDir, new String[0]);
			Path path = Paths.get(root.toString(), new String[] { "resources.sjos" });
			ResourceFile rfNew = create();
			if (rfOld != null && !rfOld.equals(rfNew)) {
				rfNew.updateVersion(rfOld);
				Files.write(path, Coding.encode(rfNew));
			} 
		} catch (Throwable t) {
			throw CoreException.throwCoreException(t);
		}
	}

	@Override
	@Function
	public ResourceFile get() throws CoreException {
		if(this.resources==null) {
			Path root = Paths.get(baseDir, new String[0]);
			Path path = Paths.get(root.toString(), new String[] { "resources.sjos" });
			if (Files.exists(path, new java.nio.file.LinkOption[0])) {
				this.resources = (ResourceFile) Coding.decode(path);
			} else {
				try {
					this.resources=create();
					Files.write(path, Coding.encode(this.resources));
				} catch (Exception e) {
					CoreException.throwCoreException(e);
				}
			}
		}
		return resources;
	}

	private String createHash(Path path) throws Exception {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		byte[] ba = new byte[16384];
		int read = 0;
		try (FileInputStream fis = new FileInputStream(path.toFile())) {
			while ((read = fis.read(ba)) != -1)
				digest.update(ba, 0, read);
			byte[] hash = digest.digest();
			return Coding.toBase64(hash);
		}
	}

	@Override
	public void launch() throws CoreException {
		get();
	}
	
	@Override
	public String getServiceHandle() {
		return id;
	}

	@Override
	public byte[] getContent(Entry entry) throws CoreException {
		int localIndex=resources.files.indexOf(entry);
		if(localIndex>-1) {
			Entry local=resources.files.get(localIndex);
			Path path=Paths.get(baseDir, local.path);
			try {
				if(Files.size(path)<maxfilesize) {
					return Files.readAllBytes(path);
				} else {
					CoreException.throwCoreException(path.toString()+" file is to big");
				}
			} catch(IOException e) {
				CoreException.throwCoreException(e);
			}
			
		} else {
			CoreException.throwCoreException("Entry not in list");
		}
		return null;
	}
}
