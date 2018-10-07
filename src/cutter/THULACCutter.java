package cutter;

import base.POCGraph;
import base.SegmentedSentence;
import base.TaggedSentence;
import character.CBTaggingDecoder;
import manage.*;

import java.io.IOException;
import java.util.List;

public class THULACCutter implements Cutter {
    private CBTaggingDecoder cws_tagging_decoder = new CBTaggingDecoder();
    private Preprocesser preprocesser = new Preprocesser();
    private Postprocesser nsDict;
    private Postprocesser idiomDict;
    private Punctuation punctuation;
    private TimeWord timeword;
    private NegWord negword;

    public THULACCutter()
    {
        try {
            init_thulac();
        }
        catch (IOException e)
        {
            System.err.println("[ERROR]:init cutter.THULACCutter cutter failed.");
            e.printStackTrace(System.err);
        }
    }
    private void init_thulac() throws IOException
    {
        Character separator = '_';
        String prefix = "data/thulac_models/";
        this.cws_tagging_decoder.threshold = 0;
        this.cws_tagging_decoder.separator = separator;
        this.cws_tagging_decoder.init((prefix+"cws_model.bin"),(prefix+"cws_dat.bin"),(prefix+"cws_label.txt"));
        this.cws_tagging_decoder.setLabelTrans();
        this.preprocesser.setT2SMap((prefix+"t2s.dat"));
        this.nsDict = new Postprocesser((prefix+"ns.dat"), "ns", false);
        this.idiomDict = new Postprocesser((prefix+"idiom.dat"), "i", false);
        this.punctuation = new Punctuation((prefix+"singlepun.dat"));
        this.timeword = new TimeWord();
        this.negword = new NegWord((prefix+"neg.dat"));
    }
    public List<String> cut(String sentence)  {
        POCGraph poc_cands = new POCGraph();
        TaggedSentence tagged = new TaggedSentence();
        SegmentedSentence segged = new SegmentedSentence();
        String raw = preprocesser.clean(sentence,poc_cands);
        if(raw.length()>0) {
            this.cws_tagging_decoder.segment(raw, poc_cands, tagged);
            this.cws_tagging_decoder.get_seg_result(segged);
            this.nsDict.adjust(segged);
            this.idiomDict.adjust(segged);
            this.punctuation.adjust(segged);
            this.timeword.adjust(segged);
            this.negword.adjust(segged);
        }
        return segged;
    }
    static public void main (String[] args) {
        // just test this class
        THULACCutter tokenFilter = new THULACCutter();
        System.out.println(tokenFilter.cut("我爱北京天安门"));
        System.out.println(tokenFilter.cut("THULAC是一个中文分词工具包"));
        System.out.println(tokenFilter.cut("这是对THULAC分词工具Wrapper的测试函数。"));
        System.out.println(tokenFilter.cut("合肥市某小区住宅楼地下一层地上三十三层"));
    }
}
