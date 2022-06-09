package com.unigateway.discovery

import javax.jmdns.JmDNS
import javax.jmdns.ServiceEvent
import javax.jmdns.ServiceInfo
import javax.jmdns.ServiceListener
import javax.jmdns.ServiceTypeListener
import javax.jmdns.impl.ServiceInfoImpl

class JmDnsStub extends JmDNS {

  private final Map<String, List<ServiceListener>> serviceListeners = [:]
  private final InetAddress ipAddress

  JmDnsStub(InetAddress ipAddress) {
    this.ipAddress = ipAddress
  }

  void handleServiceResolved(String type, String name, int port, String ipAddress) {
    serviceListeners.getOrDefault(type, []).each {
      def info = new ServiceInfoImpl(type, name, "", port, 0, 0, true, "")
      info.addAddress(Inet4Address.getByName(ipAddress) as Inet4Address)
      it.serviceResolved(new ServiceEventFake(info))
    }
  }

  void handleServiceRemoved(String type, String name, int port, String ipAddress) {
    serviceListeners.getOrDefault(type, []).each {
      def info = new ServiceInfoImpl(type, name, "", port, 0, 0, true, "")
      info.addAddress(Inet4Address.getByName(ipAddress) as Inet4Address)
      it.serviceRemoved(new ServiceEventFake(info))
    }
  }

  @Override
  String getName() {
    return null
  }

  @Override
  String getHostName() {
    return null
  }

  @Override
  InetAddress getInetAddress() throws IOException {
    return ipAddress
  }

  @Override
  InetAddress getInterface() throws IOException {
    return null
  }

  @Override
  ServiceInfo getServiceInfo(String type, String name) {
    return null
  }

  @Override
  ServiceInfo getServiceInfo(String type, String name, long timeout) {
    return null
  }

  @Override
  ServiceInfo getServiceInfo(String type, String name, boolean persistent) {
    return null
  }

  @Override
  ServiceInfo getServiceInfo(String type, String name, boolean persistent, long timeout) {
    return null
  }

  @Override
  void requestServiceInfo(String type, String name) {

  }

  @Override
  void requestServiceInfo(String type, String name, boolean persistent) {

  }

  @Override
  void requestServiceInfo(String type, String name, long timeout) {

  }

  @Override
  void requestServiceInfo(String type, String name, boolean persistent, long timeout) {

  }

  @Override
  void addServiceTypeListener(ServiceTypeListener listener) throws IOException {

  }

  @Override
  void removeServiceTypeListener(ServiceTypeListener listener) {

  }

  @Override
  void addServiceListener(String type, ServiceListener listener) {
    serviceListeners.put(type, serviceListeners.getOrDefault(type, []) + listener)
  }

  @Override
  void removeServiceListener(String type, ServiceListener listener) {
    serviceListeners.getOrDefault(type, []).remove(listener)
  }

  @Override
  void registerService(ServiceInfo info) throws IOException {

  }

  @Override
  void unregisterService(ServiceInfo info) {

  }

  @Override
  void unregisterAllServices() {

  }

  @Override
  boolean registerServiceType(String type) {
    return false
  }

  @Override
  void printServices() {

  }

  @Override
  ServiceInfo[] list(String type) {
    return new ServiceInfo[0]
  }

  @Override
  ServiceInfo[] list(String type, long timeout) {
    return new ServiceInfo[0]
  }

  @Override
  Map<String, ServiceInfo[]> listBySubtype(String type) {
    return null
  }

  @Override
  Map<String, ServiceInfo[]> listBySubtype(String type, long timeout) {
    return null
  }

  @Override
  Delegate getDelegate() {
    return null
  }

  @Override
  Delegate setDelegate(Delegate value) {
    return null
  }

  @Override
  void close() throws IOException {

  }
}

class ServiceEventFake extends ServiceEvent {

  private final ServiceInfo info

  ServiceEventFake(ServiceInfo info) {
    super(info)
    this.info = info
  }

  @Override
  JmDNS getDNS() {
    return null
  }

  @Override
  String getType() {
    return info.type
  }

  @Override
  String getName() {
    return info.name
  }

  @Override
  ServiceInfo getInfo() {
    return info
  }
}
