package com.mqgateway.utils


import com.pi4j.io.serial.Baud
import com.pi4j.io.serial.DataBits
import com.pi4j.io.serial.FlowControl
import com.pi4j.io.serial.Parity
import com.pi4j.io.serial.Serial
import com.pi4j.io.serial.SerialConfig
import com.pi4j.io.serial.SerialDataEvent
import com.pi4j.io.serial.SerialDataEventListener
import com.pi4j.io.serial.StopBits
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.Charset

class SerialStub implements Serial {

	List<SerialDataEventListener> eventListeners = new ArrayList<>()

	@Override
	void open(String device, int baud, int dataBits, int parity, int stopBits, int flowControl) throws IOException {

	}

	@Override
	void open(String device, int baud) throws IOException {

	}

	@Override
	void open(String device, Baud baud, DataBits dataBits, Parity parity, StopBits stopBits, FlowControl flowControl) throws IOException {

	}

	@Override
	void open(SerialConfig serialConfig) throws IOException {

	}

	@Override
	void close() throws IllegalStateException, IOException {

	}

	@Override
	boolean isOpen() {
		return false
	}

	@Override
	boolean isClosed() {
		return false
	}

	@Override
	void flush() throws IllegalStateException, IOException {

	}

	@Override
	void discardInput() throws IllegalStateException, IOException {

	}

	@Override
	void discardOutput() throws IllegalStateException, IOException {

	}

	@Override
	void discardAll() throws IllegalStateException, IOException {

	}

	@Override
	void sendBreak(int duration) throws IllegalStateException, IOException {

	}

	@Override
	void sendBreak() throws IllegalStateException, IOException {

	}

	@Override
	void setBreak(boolean enabled) throws IllegalStateException, IOException {

	}

	@Override
	void setRTS(boolean enabled) throws IllegalStateException, IOException {

	}

	@Override
	void setDTR(boolean enabled) throws IllegalStateException, IOException {

	}

	@Override
	boolean getRTS() throws IllegalStateException, IOException {
		return false
	}

	@Override
	boolean getDTR() throws IllegalStateException, IOException {
		return false
	}

	@Override
	boolean getCTS() throws IllegalStateException, IOException {
		return false
	}

	@Override
	boolean getDSR() throws IllegalStateException, IOException {
		return false
	}

	@Override
	boolean getRI() throws IllegalStateException, IOException {
		return false
	}

	@Override
	boolean getCD() throws IllegalStateException, IOException {
		return false
	}

	@Override
	void addListener(SerialDataEventListener... listener) {
		eventListeners.add(listener[0])
	}

	@Override
	void removeListener(SerialDataEventListener... listener) {

	}

	@Override
	int getFileDescriptor() {
		return 0
	}

	@Override
	InputStream getInputStream() {
		return null
	}

	@Override
	OutputStream getOutputStream() {
		return null
	}

	@Override
	boolean isBufferingDataReceived() {
		return false
	}

	@Override
	void setBufferingDataReceived(boolean enabled) {

	}

	@Override
	int available() throws IllegalStateException, IOException {
		return 0
	}

	@Override
	void discardData() throws IllegalStateException, IOException {

	}

	@Override
	byte[] read() throws IllegalStateException, IOException {
		return new byte[0]
	}

	@Override
	byte[] read(int length) throws IllegalStateException, IOException {
		return new byte[0]
	}

	@Override
	void read(ByteBuffer buffer) throws IllegalStateException, IOException {

	}

	@Override
	void read(int length, ByteBuffer buffer) throws IllegalStateException, IOException {

	}

	@Override
	void read(OutputStream stream) throws IllegalStateException, IOException {

	}

	@Override
	void read(int length, OutputStream stream) throws IllegalStateException, IOException {

	}

	@Override
	void read(Collection<ByteBuffer> collection) throws IllegalStateException, IOException {

	}

	@Override
	void read(int length, Collection<ByteBuffer> collection) throws IllegalStateException, IOException {

	}

	@Override
	CharBuffer read(Charset charset) throws IllegalStateException, IOException {
		return null
	}

	@Override
	CharBuffer read(int length, Charset charset) throws IllegalStateException, IOException {
		return null
	}

	@Override
	void read(Charset charset, Writer writer) throws IllegalStateException, IOException {

	}

	@Override
	void read(int length, Charset charset, Writer writer) throws IllegalStateException, IOException {

	}

	@Override
	void write(byte[] data, int offset, int length) throws IllegalStateException, IOException {

	}

	@Override
	void write(byte ... data) throws IllegalStateException, IOException {

	}

	@Override
	void write(byte[] ... data) throws IllegalStateException, IOException {

	}

	@Override
	void write(ByteBuffer... data) throws IllegalStateException, IOException {

	}

	@Override
	void write(InputStream input) throws IllegalStateException, IOException {

	}

	@Override
	void write(Charset charset, char[] data, int offset, int length) throws IllegalStateException, IOException {

	}

	@Override
	void write(Charset charset, char ... data) throws IllegalStateException, IOException {

	}

	@Override
	void write(char ... data) throws IllegalStateException, IOException {

	}

	@Override
	void write(Charset charset, CharBuffer... data) throws IllegalStateException, IOException {

	}

	@Override
	void write(CharBuffer... data) throws IllegalStateException, IOException {

	}

	@Override
	void write(Charset charset, CharSequence... data) throws IllegalStateException, IOException {

	}

	@Override
	void write(CharSequence... data) throws IllegalStateException, IOException {

	}

	@Override
	void write(Charset charset, Collection<? extends CharSequence> data) throws IllegalStateException, IOException {

	}

	@Override
	void write(Collection<? extends CharSequence> data) throws IllegalStateException, IOException {

	}

	@Override
	void writeln(Charset charset, CharSequence... data) throws IllegalStateException, IOException {

	}

	@Override
	void writeln(CharSequence... data) throws IllegalStateException, IOException {

	}

	@Override
	void writeln(Charset charset, Collection<? extends CharSequence> data) throws IllegalStateException, IOException {

	}

	@Override
	void writeln(Collection<? extends CharSequence> data) throws IllegalStateException, IOException {

	}

	void sendFakeMessage(String message) {
		eventListeners.each {
			it.dataReceived(new SerialDataEvent(this, message.bytes))
		}
	}
}