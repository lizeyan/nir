package token;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SemanticExpandFilter extends TokenFilter {
    private List<String> semanticExtraString = new ArrayList<>();
    private final CharTermAttribute charTermAttribute = addAttribute(CharTermAttribute.class);

    public SemanticExpandFilter(TokenStream input) {
        super(input);
    }

    @Override
    public boolean incrementToken() throws IOException {
        if (semanticExtraString.isEmpty())
        {
            if (this.input.incrementToken())
            {
                String basic_term = this.input.addAttribute(CharTermAttribute.class).toString();
                semanticExtraString.add(basic_term);
                if (basic_term.equalsIgnoreCase("计算机"))
                    semanticExtraString.add("电脑");
            }
            else
                return false;
        }
        charTermAttribute.setEmpty().append(semanticExtraString.remove(0));
        return true;
    }
}
