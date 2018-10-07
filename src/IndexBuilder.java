import token.SemanticExpandFilter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.AnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

class IndexBuilder {
    private List<Document> documentList = new ArrayList<>();
    private Document currentDocument = null;
    private String currentFieldName = null;
    private String currentFieldValue = null;
    private Directory indexDirectory;
    private Analyzer analyzer;

    IndexBuilder(String filename, Directory indexDirectory, Analyzer analyzer) {
        this.indexDirectory = indexDirectory;
        this.analyzer = new AnalyzerWrapper(analyzer.getReuseStrategy()) {
            @Override
            protected Analyzer getWrappedAnalyzer(String s) {
                return analyzer;
            }

            @Override
            protected TokenStreamComponents wrapComponents(String fieldName, TokenStreamComponents components) {
                return new TokenStreamComponents(components.getTokenizer(), new SemanticExpandFilter(components.getTokenStream()));
            }
        };
        this.parseDataFile(filename);
        System.out.println("[INFO]" + documentList.size() + " documents parsed");
        this.buildIndex();
    }

    private void buildIndex() {
        try {
            IndexWriterConfig indexWriterConfig = new IndexWriterConfig(this.analyzer);
            indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            IndexWriter indexWriter = new IndexWriter(this.indexDirectory, indexWriterConfig);
            indexWriter.addDocuments(documentList);
            indexWriter.close();
        } catch (IOException e) {
            System.err.println("[ERROR]: Write index failed");
            e.printStackTrace(System.err);
        }
    }

    private void parseDataFile(String fileName) {
        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
            stream.forEach(this::parseLine);
        } catch (IOException e) {
            System.err.println("[ERROR]parse data file " + fileName + " failed.");
            e.printStackTrace(System.err);
        }
    }

    private void parseLine(String line) {
        String key;
        String value;
        char[] char_array = line.toCharArray();
        int key_start = -1, key_end = -1;
        int value_start = -1;
        for (int i = 0; i < line.length(); ++i) {
            char chr = char_array[i];
            if (chr == '<' && key_start == -1)
                key_start = i;
            else if (chr == '>' && key_end == -1)
                key_end = i;
            else if (chr == '=' && value_start == -1)
                value_start = i;
        }
        if (key_start != -1) {
            key = new String(char_array, key_start + 1, key_end - key_start - 1);
            if (value_start != -1)
                value = new String(char_array, value_start + 1, line.length() - value_start - 1);
            else
                value = "";
        } else {
            key = "";
            value = line;
        }
        assert !(value.equals("")) || key.equalsIgnoreCase("REC");
        if (key.equalsIgnoreCase("REC"))
            this.newDocument();
        else if (!key.equals("")) {
            this.newField(key);
            this.appendFieldContent(value);
        } else
            this.appendFieldContent(value);
    }

    private void appendFieldContent(String value) {
        if (this.currentFieldValue == null)
            this.currentFieldValue = "";
        this.currentFieldValue += value;
    }

    private void newField(String key) {
        assert this.currentDocument != null;
        if (this.currentFieldName != null && this.currentFieldValue != null) {
//            if (this.currentFieldName.equals("摘要")) {
//                this.currentDocument.add(new TextField(this.currentFieldName, this.currentFieldValue, Field.Store.YES));
//            }
            if (this.currentFieldName.equals("共引文献") || this.currentFieldName.equals("同被引文献") || this.currentFieldName.equals("二级引证文献")) {
                this.currentDocument.add(new TextField(this.currentFieldName, this.currentFieldValue, Field.Store.YES));
            } else {
                this.currentDocument.add(new TextField(this.currentFieldName, this.currentFieldValue, Field.Store.YES));
            }
        }
        this.currentFieldName = key;
        this.currentFieldValue = null;
    }

    private void newDocument() {
        if (this.currentDocument != null) {
            this.documentList.add(this.currentDocument);
        }
        this.currentDocument = new Document();
        this.currentFieldName = null;
        this.currentFieldValue = null;
    }
}
