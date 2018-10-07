package token;

import cutter.Cutter;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class CutterTokenizer extends Tokenizer {

    private Scanner scanner;
    private int position = 0;

    private CharTermAttribute charAttr =
            addAttribute(CharTermAttribute.class);
    private TypeAttribute typeAttr = addAttribute(TypeAttribute.class);
    private final PositionIncrementAttribute positionAttr =
            addAttribute(PositionIncrementAttribute.class);
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
    private Cutter cutter;
    private List<String> segs = new LinkedList<>();

    public CutterTokenizer(Cutter cutter)
    {
        this.cutter = cutter;
    }

    @Override
    public boolean incrementToken() throws IOException {
        clearAttributes();

        while (this.scanner.hasNext())
        {
            String token = this.scanner.next();
//            System.out.println(token);
            segs.addAll(this.cutter.cut(token));
        }

        if (segs.size() > 0) {
            String word = segs.remove(0);
            charAttr.append(word);
            typeAttr.setType("Word");
            positionAttr.setPositionIncrement(position + word.length());
            offsetAtt.setOffset(correctOffset(position), correctOffset(position + word.length()));

            return true;
        }
        else{
            return false;
        }
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        this.position = 0;
        this.scanner = new Scanner(this.input).useDelimiter("[，。；：！？《》（）【】「」·～,.!?<>;()\\[\\]{}\\s]");
    }
}
