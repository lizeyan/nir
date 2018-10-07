import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;

public class Searcher {
    private  DirectoryReader reader;
    private IndexSearcher searcher;
    private QueryParser parser;

    public Searcher(Directory indexDirectory, Analyzer analyzer)
    {
        try {
            this.reader = DirectoryReader.open(indexDirectory);
            this.searcher = new IndexSearcher(this.reader);
            this.parser = new QueryParser("摘要", analyzer);
        }
        catch (IOException e)
        {
            System.err.println("[ERROR]: unable to read index directory");
            e.printStackTrace(System.err);
        }
    }

    public List<Document> search(String queryText, int k)
    {
        List<Document> result = new ArrayList<>();
        try {
            Query query = this.parser.parse(queryText);
            System.out.println("Search:[" + query + "]");
            ScoreDoc[] hits = this.searcher.search(query, k).scoreDocs;
            // Iterate through the results:
            for (ScoreDoc hit : hits) {
                Document hitDoc = this.searcher.doc(hit.doc);
                result.add(hitDoc);
            }
        }
        catch (IOException | ParseException e) {
            System.err.println("[ERROR]: unable to do query");
            e.printStackTrace(System.err);
        }
        return result;
    }

    public void close() {
        try {
            this.reader.close();
        }
        catch (IOException e)
        {
            e.printStackTrace(System.err);
        }
    }

}
