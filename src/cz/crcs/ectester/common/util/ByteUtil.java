package cz.crcs.ectester.common.util;

/**
 * Utility class, some byte/hex manipulation, convenient byte[] methods.
 *
 * @author Petr Svenda petr@svenda.com
 * @author Jan Jancar johny@neuromancer.sk
 */
public class ByteUtil {
    public static short getShort(byte[] array, int offset) {
        return (short) (((array[offset] & 0xFF) << 8) | (array[offset + 1] & 0xFF));
    }

    public static void setShort(byte[] array, int offset, short value) {
        array[offset + 1] = (byte) (value & 0xFF);
        array[offset] = (byte) ((value >> 8) & 0xFF);
    }

    public static int diffBytes(byte[] one, int oneOffset, byte[] other, int otherOffset, int length) {
        for (int i = 0; i < length; ++i) {
            byte a = one[i + oneOffset];
            byte b = other[i + otherOffset];
            if (a != b) {
                return i;
            }
        }
        return length;
    }

    public static boolean compareBytes(byte[] one, int oneOffset, byte[] other, int otherOffset, int length) {
        return diffBytes(one, oneOffset, other, otherOffset, length) == length;
    }

    public static boolean allValue(byte[] array, byte value) {
        for (byte a : array) {
            if (a != value)
                return false;
        }
        return true;
    }

    public static byte[] hexToBytes(String hex) {
        return hexToBytes(hex, true);
    }

    public static byte[] hexToBytes(String hex, boolean bigEndian) {
        hex = hex.replace(" ", "");
        int len = hex.length();
        StringBuilder sb = new StringBuilder();

        if (len % 2 == 1) {
            sb.append("0");
            ++len;
        }

        if (bigEndian) {
            sb.append(hex);
        } else {
            for (int i = 0; i < len / 2; ++i) {
                if (sb.length() >= 2) {
                    sb.insert(sb.length() - 2, hex.substring(2 * i, 2 * i + 2));
                } else {
                    sb.append(hex.substring(2 * i, 2 * i + 2));
                }

            }
        }

        String data = sb.toString();
        byte[] result = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            result[i / 2] = (byte) ((Character.digit(data.charAt(i), 16) << 4)
                    + (Character.digit(data.charAt(i + 1), 16)));
        }
        return result;
    }

    public static String byteToHex(byte data) {
        return String.format("%02x", data);
    }

    public static String bytesToHex(byte[] data) {
        return bytesToHex(data, true);
    }

    public static String bytesToHex(byte[] data, boolean addSpace) {
        return bytesToHex(data, 0, data.length, addSpace);
    }

    public static String bytesToHex(byte[] data, int offset, int len) {
        return bytesToHex(data, offset, len, true);
    }

    public static String bytesToHex(byte[] data, int offset, int len, boolean addSpace) {
        StringBuilder buf = new StringBuilder();
        for (int i = offset; i < (offset + len); i++) {
            buf.append(byteToHex(data[i]));
            if (addSpace && i != (offset + len - 1)) {
                buf.append(" ");
            }
        }
        return (buf.toString());
    }

    public static byte[] concatenate(byte[]... arrays) {
        int len = 0;
        for (byte[] array : arrays) {
            if (array == null)
                continue;
            len += array.length;
        }
        byte[] out = new byte[len];
        int offset = 0;
        for (byte[] array : arrays) {
            if (array == null || array.length == 0)
                continue;
            System.arraycopy(array, 0, out, offset, array.length);
            offset += array.length;
        }
        return out;
    }
}