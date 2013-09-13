package com.hlcl.rql.util.as;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class BOMUtil {
	public final static int NONE = -1;
	public final static int UTF32BE = 0;
	public final static int UTF32LE = 1;
	public final static int UTF16BE = 2;
	public final static int UTF16LE = 3;
	public final static int UTF8 = 4;

	public final static byte[] UTF32BEBOMBYTES = new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0xFE, (byte) 0xFF, };
	public final static byte[] UTF32LEBOMBYTES = new byte[] { (byte) 0xFF, (byte) 0xFE, (byte) 0x00, (byte) 0x00, };
	public final static byte[] UTF16BEBOMBYTES = new byte[] { (byte) 0xFE, (byte) 0xFF, };
	public final static byte[] UTF16LEBOMBYTES = new byte[] { (byte) 0xFF, (byte) 0xFE, };
	public final static byte[] UTF8BOMBYTES = new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF, };

	public final static byte[][] BOMBYTES = new byte[][] { UTF32BEBOMBYTES, UTF32LEBOMBYTES, UTF16BEBOMBYTES, UTF16LEBOMBYTES, UTF8BOMBYTES, };

	public final static int MAXBOMBYTES = 4;// no bom sequence is longer than 4 byte

	public static int getBOMType(byte[] _bomBytes) {
		return getBOMType(_bomBytes, _bomBytes.length);
	}

	public static int getBOMType(byte[] _bomBytes, int _length) {
		for (int i = 0; i < BOMBYTES.length; i++) {
			for (int j = 0; j < _length && j < BOMBYTES[i].length; j++) {
				if (_bomBytes[j] != BOMBYTES[i][j])
					break;
				if (_bomBytes[j] == BOMBYTES[i][j] && j == BOMBYTES[i].length - 1)
					return i;
			}
		}
		return NONE;
	}

	public static int getBOMType(File _f) throws IOException {
		FileInputStream fIn = new FileInputStream(_f);
		byte[] buff = new byte[MAXBOMBYTES];
		int read = fIn.read(buff);
		int BOMType = getBOMType(buff, read);
		fIn.close();
		return BOMType;
	}

	public static int getSkipBytes(int BOMType) {
		if (BOMType < 0 || BOMType >= BOMBYTES.length)
			return 0;
		return BOMBYTES[BOMType].length;
	}

	/**
	 * Just reads necessary bytes from the stream
	 * 
	 * @param _fIn
	 */
	public static Reader getReader(File _f, String encoding) throws IOException {
		int BOMType = getBOMType(_f);
		int skipBytes = getSkipBytes(BOMType);
		FileInputStream fIn = new FileInputStream(_f);
		fIn.skip(skipBytes);
		Reader reader = new InputStreamReader(fIn, encoding);
		return reader;
	}
}
