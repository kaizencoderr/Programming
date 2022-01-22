import java.io.Reader;
import java.io.IOException;
import java.io.StringReader;


/** Translating Reader: a stream that is a translation of an
*  existing reader.
*  @author Tyler Freund
*
*  NOTE: Until you fill in the right methods, the compiler will
*        reject this file, saying that you must declare TrReader
* 	     abstract.  Don't do that; define the right methods instead!
*/
public class TrReader extends Reader {
    /** A new TrReader that produces the stream of characters produced
     *  by STR, converting all characters that occur in FROM to the
     *  corresponding characters in TO.  That is, change occurrences of
     *  FROM.charAt(i) to TO.charAt(i), for all i, leaving other characters
     *  in STR unchanged.  FROM and TO must have the same length. */

    public TrReader(Reader str, String from, String to) {
        // TODO: YOUR CODE HERE
        this._from = from;
        this._to = to;
        this._str = str;
    }
    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        int chars_read = _str.read(cbuf, off, len);
        for (int i = 0; i < len; i++) {
            if (off + i >= cbuf.length) {
                return chars_read;
            } else {
                for (int j = 0; j < _from.length(); j++) {
                    if (_from.charAt(j) == cbuf[off + i]) {
                        cbuf[off + i] = _to.charAt(j);
                        break;
                    }
                }
            }
        }
        return chars_read;
    }
    @Override
    public void close() throws IOException {
        _str.close();
    }
    private String _from;
    private String _to;
    private Reader _str;
}
