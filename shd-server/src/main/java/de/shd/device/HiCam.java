package de.shd.device;

import de.core.CoreException;
import de.core.data.Data;
import de.core.ftp.IFtpFileHandler;
import de.core.http.Http;
import de.core.http.HttpHeader;
import de.core.http.HttpRequest;
import de.core.http.HttpResponse;
import de.core.http.handler.FixedLengthHttpResponse;
import de.core.http.handler.HttpRequestHandler;
import de.core.http.mime.MimeTypes;
import de.core.log.Logger;
import de.core.rt.Launchable;
import de.core.serialize.Coding;
import de.core.serialize.Serializable;
import de.core.serialize.annotation.Element;
import de.core.service.Function;
import de.core.service.Param;
import de.core.utils.Streams;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public class HiCam extends AbstractDevice implements Switch, Launchable, IFtpFileHandler, HttpRequestHandler {
  public static class Capture implements Serializable {
    @Element
    protected String id = UUID.randomUUID().toString();
    
    @Element
    protected HiCam.CaptureType type;
    
    @Element
    protected long date;
    
    @Element
    protected String filename;
  }
  
  private enum CaptureType {
    PICTURE, VIDEO;
  }
  
  public static class HiCamExportData implements Data {
    @Element
    List<HiCam.Capture> captures = new ArrayList<>();
    
    public void sort() {
      this.captures.sort(new Comparator<HiCam.Capture>() {
            public int compare(HiCam.Capture o1, HiCam.Capture o2) {
              return (new Long(o2.date)).compareTo(Long.valueOf(o1.date));
            }
          });
    }
  }
  
  public static class HiCamOverviewData implements Data {
    @Element
    Switch.State state;
    
    @Element
    long events;
    
    protected HiCamOverviewData() {}
    
    public HiCamOverviewData(Switch.State state, long events) {
      this.state = state;
      this.events = events;
    }
  }
  
  private Logger log = null;
  
  @Element
  public String captureDir;
  
  @Element
  public String host;
  
  @Element
  public String user;
  
  @Element
  public String password;
  
  @Element
  public String cron = "* * * * *";
  
  @Element
  public String ftpUser;
  
  private Switch.State state = Switch.State.UNKNOWN;
  
  private HiCamExportData detailsData;
  
  public void searchCaptures() {
    try {
      if (this.detailsData == null) {
        this.detailsData = new HiCamExportData();
      } else {
        this.detailsData.captures.clear();
      } 
      try (Stream<Path> stream = Files.walk(Paths.get(this.captureDir, new String[0]), 3, new java.nio.file.FileVisitOption[0])) {
        stream.forEach(p -> handleFile(p));
      } 
      this.detailsData.sort();
    } catch (IOException e) {
      e.printStackTrace();
    } 
  }
  
  public void handleFile(Path p) {
    try {
      if (!Files.isDirectory(p, new java.nio.file.LinkOption[0]) && p
        .getFileName().toString().endsWith(".jpg") && 
        !p.getFileName().toString().startsWith("ACK.")) {
        Capture c = new Capture();
        c.type = CaptureType.PICTURE;
        c.filename = p.getFileName().toString();
        c.date = Files.getLastModifiedTime(p, new java.nio.file.LinkOption[0]).toMillis();
        this.detailsData.captures.add(c);
        this.detailsData.sort();
      } 
    } catch (Throwable throwable) {}
  }
  
  public ExportData createExportData() {
    return new ExportData(getDeviceHandle(), name, new HiCamOverviewData(this.state, countEvents()));
  }
  
  public Switch.State toggle() throws CoreException {
    if (this.state == Switch.State.ON) {
      this.state = Switch.State.OFF;
    } else {
      this.state = Switch.State.ON;
    } 
    setState(this.state);
    export();
    return this.state;
  }
  
  public Switch.State getState() throws CoreException {
    return this.state;
  }
  
  public Switch.State setState(Switch.State state) throws CoreException {
    String value = (state == Switch.State.ON) ? "PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP" : "NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN";
    String url = "http://" + this.host + "/web/cgi-bin/hi3510/param.cgi";
    String data = "cmd=setplanrecattr&cururl=http%3A%2F%2F127.0.0.1%2Fweb%2Fscheduleex.html&recswitch=&recstream=&planrec_time=&cmd=setscheduleex&-ename=md";
    for (int i = 0; i < 7; i++)
      data = data + "&-week" + i + "=" + value; 
    try {
      Http.post(url, getHeader(), data.getBytes());
    } catch (Exception e) {
      CoreException.throwCoreException(e);
    } 
    data = "cmd=setinfrared&cururl=http%3A%2F%2F127.0.0.1%2Fweb%2Fdisplay.html&-infraredstat=" + ((state == Switch.State.ON) ? "auto" : "close");
    try {
      Http.post(url, getHeader(), data.getBytes());
    } catch (Exception e) {
      CoreException.throwCoreException(e);
    } 
    return state;
  }
  
  @Function
  public boolean acknowledge(@Param("capture") Capture capture) throws CoreException {
    Capture local = getCapture(capture);
    if (local != null) {
      Path file = getCaptureFile(local);
      if (file != null && Files.exists(file, new java.nio.file.LinkOption[0])) {
        Path ack = Paths.get(file.getParent().toString(), new String[] { "ACK." + file.getFileName().toString() });
        try {
          Files.move(file, ack, new java.nio.file.CopyOption[0]);
          this.detailsData.captures.remove(local);
          export();
          return true;
        } catch (IOException e) {
          this.log.error("failed to acknowledge capture " + capture.id, e);
        } 
      } 
    } 
    return false;
  }
  
  @Function
  public boolean delete(@Param("capture") Capture capture) throws CoreException {
    Capture local = getCapture(capture);
    if (local != null) {
      Path file = getCaptureFile(local);
      if (file != null && Files.exists(file, new java.nio.file.LinkOption[0]))
        try {
          Files.delete(file);
        } catch (IOException e) {
          this.log.error("failed to delete capture " + capture.id, e);
        }  
      this.detailsData.captures.remove(local);
      export();
      return true;
    } 
    return false;
  }
  
  @Function
  public boolean deleteAll() throws CoreException {
    for (int i = this.detailsData.captures.size() - 1; i >= 0; i--) {
      Path p = getCaptureFile(this.detailsData.captures.get(i));
      if (p != null && Files.exists(p, new java.nio.file.LinkOption[0]))
        try {
          Files.delete(p);
          this.detailsData.captures.remove(i);
        } catch (IOException e) {
          return false;
        }  
    } 
    export();
    return true;
  }
  
  @Function
  public long countEvents() {
    return this.detailsData.captures.size();
  }
  
  @Function
  public HiCamExportData getDetailsData() {
    return this.detailsData;
  }
  
  private Capture getCapture(@Param("capture") Capture cap) {
    for (Capture c : this.detailsData.captures) {
      if (c.id.equals(cap.id))
        return c; 
    } 
    return null;
  }
  
  private Capture getCapture(String id) {
    for (Capture c : this.detailsData.captures) {
      if (c.id.equals(id))
        return c; 
    } 
    return null;
  }
  
  private Path getCaptureFile(Capture capture) {
    String date = "20" + capture.filename.substring(1, 7);
    Path file = null;
    if (capture.type == CaptureType.PICTURE)
      file = Paths.get(this.captureDir, new String[] { date, "images", capture.filename }); 
    return file;
  }
  
  private void updateState() {
    try {
      HttpResponse resp=Http.get("http://" + this.host + "/web/cgi-bin/hi3510/param.cgi?cmd=getlanguage&cmd=getscheduleex&-ename=md", getHeader());
      byte[] data = Streams.readAll(resp.getContent());
      parseSchedules(new String(data));
    } catch (Exception e) {
      e.printStackTrace();
    } 
  }
  
  private Map<String, String> getHeader() throws UnsupportedEncodingException {
    Map<String, String> header = new HashMap<>();
    header.put("Authorization", "Basic " + Base64.getEncoder().encodeToString((this.user + ":" + this.password).getBytes("UTF-8")));
    return header;
  }
  
  private void parseSchedules(String response) {
    String[] lines = response.split("\r\n");
    boolean[] active = new boolean[7];
    int counter = 0;
    for (String line : lines) {
      if (line.startsWith("var week")) {
        if (line.contains("PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP")) {
          active[counter] = true;
        } else {
          active[counter] = false;
        } 
        counter++;
      } 
    } 
    boolean a0 = true;
    for (boolean b : active)
      a0 &= b; 
    if (a0) {
      this.state = Switch.State.ON;
    } else {
      this.state = Switch.State.OFF;
    } 
  }
  
  public void launch() throws CoreException {
    this.log = Logger.createLogger("HiCam_" + this.id);
    updateState();
    searchCaptures();
    registerFtpFileHandler();
    registerHttpRequestHandler();
  }
  
  public static void main(String[] args) throws CoreException {
    HiCam cam = new HiCam();
    cam.captureDir = "D:\\dev\\workspaces\\dashboard\\deploy\\data\\ftp\\cam";
    cam.searchCaptures();
    cam.acknowledge(cam.detailsData.captures.get(0));
    cam.host = "127.0.0.1";
    cam.user = "admin";
    cam.password = "admin";
    cam.updateState();
    cam.setState(Switch.State.ON);
    System.out.println(cam.getState());
    System.out.println(new String(Coding.encode(cam.detailsData, "json")));
  }
  
  public boolean canHandle(String user, String file) {
    return this.ftpUser.equals(user);
  }
  
  public void onFile(Path path) {
    try {
      Capture capture = new Capture();
      capture.filename = path.getFileName().toString();
      capture.date = Files.getLastModifiedTime(path, new java.nio.file.LinkOption[0]).toMillis();
      capture.type = path.getParent().getFileName().toString().contains("images") ? CaptureType.PICTURE : CaptureType.VIDEO;
      this.detailsData.captures.add(capture);
      this.log.debug("Got new file Capture" + path.toString());
      export();
    } catch (Throwable t) {
      this.log.error(t);
    } 
    System.out.println("new File");
  }
  
  public HttpResponse handleRequest(HttpRequest request) {
    String capid = request.getRequestParameter("id");
    Capture c = getCapture(capid);
    if (c != null)
      try {
        FixedLengthHttpResponse resp = new FixedLengthHttpResponse(Files.readAllBytes(getCaptureFile(c)));
        resp.setHeader(new HttpHeader("Content-Type", MimeTypes.getMimeType("jpeg")));
        return (HttpResponse)resp;
      } catch (Throwable t) {
        return (HttpResponse)new FixedLengthHttpResponse("ERROR".getBytes(), 500);
      }  
    return (HttpResponse)new FixedLengthHttpResponse("NOT_FOUND".getBytes(), 404);
  }
  
  public boolean canHandleRequest(HttpRequest request) {
    return request.getRequestPath().startsWith("/resources/cam/" + this.id);
  }
  
  public boolean keepAlive() {
    return false;
  }
}
