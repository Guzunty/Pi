package org.sump.analyzer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;

public class GPIOInputStream extends InputStream {
    int dummy = 0xaa;
	@Override
	public int read() throws IOException {
		Byte result = null;
		result = buffer.poll();
		if (result == null) {
			//dummy = ~dummy;
			return dummy;
			//throw new IOException("GPIOStream read attempted on empty buffer.");
		}
		return result;
	}

	private ArrayDeque<Byte> buffer = new ArrayDeque<Byte>();

	public void privateWrite(int toWrite) {
    	for (int i = 0; i < 4; i++) {
		  buffer.add((byte)(toWrite & 0xff));
		  toWrite = toWrite >> 8;
    	}
	}

}
