package org.sump.analyzer;

import java.io.InputStream;
import java.io.OutputStream;

public class GPIOPort {

	InputStream is = null;
	OutputStream os = null;
	
	public static GPIOPort open(String string, int i) {
		// TODO Auto-generated method stub
		return new GPIOPort();
	}

	public OutputStream getOutputStream() {
		if (os == null) {
			os = new GPIOOutputStream();
		}
		return os;
	}
    byte[] buffer = new byte[32];
	public InputStream getInputStream() {
		if (is == null) {
			is = new GPIOInputStream();
			GPIOOutputStream.setIs((GPIOInputStream)is);
		}
		return is;
	}

	public void close() {
		is = null;
		os = null;
	}

}
