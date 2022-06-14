package de.shd.alexa.enumlator;

import de.core.CoreException;
import de.core.log.ConsoleAppender;
import de.core.log.Logger;
import de.core.rt.Releasable;
import de.core.service.Function;
import de.core.service.Service;
import de.core.utils.Streams;
import de.shd.device.DeviceProvider;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class DiscoveryResponder implements Releasable, Service {
  Logger logger = Logger.createLogger("WemoEmulator");
  
  protected DeviceProvider deviceStore;
  
  private class MutlticastReceiver extends Thread {
    MulticastSocket socket;
    
    private MutlticastReceiver() {}
    
    public void run() {
      try {
        this.socket = new MulticastSocket(DiscoveryResponder.this.multicastPort);
        InetAddress multicastAddress = InetAddress.getByName(DiscoveryResponder.this.multicastIp);
        this.socket.setReuseAddress(true);
        this.socket.setSoTimeout(15000);
        List<NetworkInterface> interfacse = DiscoveryResponder.this.getMulticastInterfaces();
        if (interfacse.size() > 0) {
          InetSocketAddress socketAddress = new InetSocketAddress(multicastAddress, 65535);
          for (NetworkInterface netIf : interfacse) {
            this.socket.joinGroup(socketAddress, netIf);
            DiscoveryResponder.this.logger.debug("join group on " + netIf.getDisplayName());
          } 
        } else {
          this.socket.joinGroup(multicastAddress);
        } 
        this.socket.setSoTimeout(120000);
        while (true) {
          byte[] rxbuf = new byte[8192];
          DatagramPacket packet = new DatagramPacket(rxbuf, rxbuf.length);
          this.socket.receive(packet);
          DiscoveryResponder.this.logger.debug("Packet received from: " + packet.getAddress() + ":" + packet.getPort());
          DiscoveryResponder.this.logger.debug("Packet: " + new String(packet.getData()));
          DiscoveryResponder.this.handlePacket(packet);
        } 
      } catch (Exception e) {
        e.printStackTrace();
        return;
      } 
    }
  }
  
  protected String multicastIp = "239.255.255.250";
  
  protected int multicastPort = 1900;
  
  protected MutlticastReceiver multicast;
  
  private boolean checkPacket(DatagramPacket packet) throws CoreException {
    try {
      String data = new String(packet.getData(), "UTF-8");
      return (data.indexOf("M-SEARCH") > -1);
    } catch (Throwable e) {
      CoreException.throwCoreException(e);
      return false;
    } 
  }
  
  private void handlePacket(DatagramPacket packet) {
    try {
      if (checkPacket(packet)) {
        String data = new String(Streams.readAll(getClass().getResourceAsStream("/emulator/msearch.txt")), "UTF-8");
        data = data.replace("${device.uuid}", "TestLampe").replace("${server.host}", "192.168.178.26").replace("${server.port}", "80");
        DatagramPacket resoponse = new DatagramPacket(data.getBytes(), data.length(), packet.getAddress(), packet.getPort());
        DatagramSocket client = new DatagramSocket();
        client.send(resoponse);
      } 
    } catch (Throwable t) {
      t.printStackTrace();
    } 
  }
  
  public List<NetworkInterface> getMulticastInterfaces() throws SocketException {
    List<NetworkInterface> viableInterfaces = new ArrayList<>();
    Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
    while (e.hasMoreElements()) {
      NetworkInterface n = e.nextElement();
      Enumeration<InetAddress> ee = n.getInetAddresses();
      while (ee.hasMoreElements()) {
        InetAddress i = ee.nextElement();
        if (i.isSiteLocalAddress() && !i.isAnyLocalAddress() && !i.isLinkLocalAddress() && 
          !i.isLoopbackAddress() && !i.isMulticastAddress())
          viableInterfaces.add(NetworkInterface.getByName(n.getName())); 
      } 
    } 
    return viableInterfaces;
  }
  
  public void release() throws CoreException {}
  
  @Function
  public void discoverable() {
    Logger.appender.add(new ConsoleAppender());
    Logger.setRootLogLevel(2);
    if (this.multicast == null)
      this.multicast = new MutlticastReceiver(); 
    this.multicast.start();
  }
  
  public static void main(String[] args) throws IOException {
    (new DiscoveryResponder()).discoverable();
  }
}
