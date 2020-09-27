import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

class VByteEncoder {
    public static void main(String[] args) throws IOException {
        VByteEncoder vb = new VByteEncoder();
        Integer[] a = {1, 2, 1, 6, 1, 3, 6, 11, 180, 1, 1, 1};  // 81 82 81 86 81 83 86 8B 01 B4 81 81 81
        ByteBuffer byteBuffer = ByteBuffer.allocate(4 * a.length);
        ByteBuffer enc = vb.encode(a, byteBuffer);
        byte[] b = enc.array();
        int j = b.length-1;
        while(b[j] == 0)
        {
            --j;
        }
        byte[] temp = new byte[j + 1];
        for (int i=0; i<temp.length; i++)
            temp[i] = b[i];

        IntBuffer intBuffer = IntBuffer.allocate(a.length);
        vb.decode(temp, intBuffer);
    }

    public Map<String, byte[]> generateVByteEncodedIndex(Map<String, Integer[]> invertedIndex) throws IOException {
        Map<String, byte[]> vByteCompressedIndex = new HashMap<>();
        for (Map.Entry<String,Integer[]> entry : invertedIndex.entrySet()) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(4 * entry.getValue().length);
            ByteBuffer enc = encode(entry.getValue(), byteBuffer);
            byte[] b = enc.array();
            int j = b.length-1;
            while(b[j] == 0) {
                --j;
            }
            byte[] postingList = new byte[j + 1];
            for (int i=0; i<postingList.length; i++)
                postingList[i] = b[i];
            vByteCompressedIndex.put(entry.getKey(), postingList);
//            vByteCompressedIndex.put(entry.getKey(), b);
        }
        return vByteCompressedIndex;
    }
    public ByteBuffer encode ( Integer[] input, ByteBuffer output) throws IOException {

        for (int i : input ) {
            while ( i >= 128 ) {
                output.put( (byte) (i & 0x7F) ) ;
                i >>>= 7 ; // logical shift, no sign bit extension
            }
            output.put((byte) (i | 0x80) );
        }
        return output;
    }

    public IntBuffer decode ( byte [] input, IntBuffer ib ) {
        int x = input.length;
        for ( int i = 0; i < input.length; i++ ) {
            int position = 0;
            int result = ((int) input[i] & 0x7F);
            while ( (input[i] & 0x80) == 0 ) {
                i += 1;
                position += 1;
                int unsignedByte = ((int) input[i] & 0x7F);
                result |= (unsignedByte << (7 * position));
            }
            ib.put(result);

        }
        return ib;
    }
}
