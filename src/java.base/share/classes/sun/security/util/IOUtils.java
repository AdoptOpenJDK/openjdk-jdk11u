/*
 * Copyright (c) 2009, 2017, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/**
 * IOUtils: A collection of IO-related public static methods.
 */

package sun.security.util;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class IOUtils {

    /**
     * Read up to {@code length} of bytes from {@code in}
     * until EOF is detected.
     * @param is input stream, must not be null
     * @param length number of bytes to read
     * @param readAll if true, an EOFException will be thrown if not enough
     *        bytes are read.
     * @return bytes read
     * @throws IOException Any IO error or a premature EOF is detected
     */
    public static byte[] readFully(InputStream is, int length, boolean readAll)
            throws IOException {
        if (length < 0) {
            throw new IOException("Invalid length");
        }
        byte[] output = {};
        int pos = 0;
        while (pos < length) {
            int bytesToRead;
            if (pos >= output.length) { // Only expand when there's no room
                bytesToRead = Math.min(length - pos, output.length + 1024);
                if (output.length < pos + bytesToRead) {
                    output = Arrays.copyOf(output, pos + bytesToRead);
                }
            } else {
                bytesToRead = output.length - pos;
            }
            int cc = is.read(output, pos, bytesToRead);
            if (cc < 0) {
                if (readAll) {
                    throw new EOFException("Detect premature EOF");
                } else {
                    if (output.length != pos) {
                        output = Arrays.copyOf(output, pos);
                    }
                    break;
                }
            }
            pos += cc;
        }
        return output;
    }

    /**
     * Read {@code length} of bytes from {@code in}. An exception is
     * thrown if there are not enough bytes in the stream.
     *
     * @param is input stream, must not be null
     * @param length number of bytes to read, must not be negative
     * @return bytes read
     * @throws IOException if any IO error or a premature EOF is detected, or
     *      if {@code length} is negative since this length is usually also
     *      read from {@code is}.
     */
    public static byte[] readNBytes(InputStream is, int length) throws IOException {
        if (length < 0) {
            throw new IOException("length cannot be negative: " + length);
        }
        return readFully(is, length, true);
    }
}
