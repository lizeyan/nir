import cutter.THULACCutter;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import token.CutterTokenizer;
import token.SemanticExpandFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.List;

public class NIR {
    private int TOPK = 3;
    private List<String> DISPLAYED_FIELDS = Arrays.asList("题名", "作者", "摘要", "单位");
    private List<Document> lastQueryResult = new ArrayList<>();
    private Searcher searcher;
    public NIR(Searcher searcher)
    {
        this.searcher = searcher;
    }
    private void cli()
    {
        Scanner input = new Scanner(System.in);
        System.out.print(">>");
        while (input.hasNext()) {
            String line = input.nextLine();
            if (line.startsWith("SET_TOP_K"))
                set_top_k(line);
            else if (line.startsWith("SET_DISPLAYED_FIELDS"))
                set_displayed_fields(line);
            else if (line.equalsIgnoreCase("QUIT"))
                break;
            else
                query(line);
            System.out.print(">>");
        }
    }
    private void set_top_k(String line) {
        Scanner scanner = new Scanner(line);
        scanner.next("SET_TOP_K");
        int top_k = scanner.nextInt();
        System.out.println("Set new k:" + top_k);
        this.TOPK = top_k;
    }
    private void set_displayed_fields(String line) {
        Scanner scanner = new Scanner(line);
        scanner.next("SET_DISPLAYED_FIELDS");
        List<String> displayed_fields = new ArrayList<>();
        while (scanner.hasNext("\\S+"))
        {
            displayed_fields.add(scanner.next("\\S+"));
        }
        System.out.println("Set new displayed fields:" + displayed_fields);
        this.DISPLAYED_FIELDS = displayed_fields;
    }
    private void query(String line)
    {
        List<Document> documents = this.searcher.search(line, this.TOPK);
        for (Document document : documents) {
            System.out.println("{");
            for (String field_name: this.DISPLAYED_FIELDS)
            {
                try {
                    System.out.println("\t" + field_name + ":" + document.getField(field_name).stringValue());
                }
                catch (NullPointerException e)
                {
                    System.out.println("\t" + field_name + " not found.");
                }
            }
            System.out.println("}");
        }
    }
    public static void main (String[] argv) throws IOException
    {
//        Directory directory = new RAMDirectory();
        Analyzer basic_analyzer = new StandardAnalyzer();
        Analyzer analyzer = new Analyzer() {
            @Override
            protected TokenStreamComponents createComponents(String s) {
                Tokenizer src = new CutterTokenizer(new THULACCutter());
//                Tokenizer src = new StandardTokenizer();
                TokenStream tok = new StandardFilter(src);
                tok = new LowerCaseFilter(tok);
                tok = new SemanticExpandFilter(tok);
                return new TokenStreamComponents(src, tok);
            }
        };
        Directory directory = FSDirectory.open(Paths.get("index"));

        new IndexBuilder("data/CNKI_journal_v2.txt", directory, analyzer);

        Searcher searcher = new Searcher(directory, analyzer);
        new NIR(searcher).cli();

        try {
            searcher.close();
            directory.close();
        }
        catch (IOException e)
        {
            e.printStackTrace(System.err);
        }
    }
}
