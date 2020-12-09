package com.nowcoder.service;

import com.nowcoder.model.Question;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SearchService {

    private static final String SOLR_URL = "http://localhost:8983/solr/wenda";
    private static final String QUESTION_TITLE_FILED = "question_title";
    private static final String QUESTION_CONTENT_FILED = "question_content";

    private SolrClient client = new HttpSolrClient.Builder(SOLR_URL).build();


    public List<Question> searchQuestion(String keyword,int offset,int count,
                                         String hlPre,String hlPos) throws Exception{
        List<Question> questions = new ArrayList<>();

        SolrQuery query = new SolrQuery(keyword);
        query.setStart(offset);
        query.setRows(count);
        query.setHighlightSimplePre(hlPre);
        query.setHighlightSimplePost(hlPos);
        query.setHighlight(true);
        query.set("hl.fl",QUESTION_TITLE_FILED+","+QUESTION_CONTENT_FILED);
        QueryResponse response = client.query(query);

        for (Map.Entry<String, Map<String, List<String>>> entry : response.getHighlighting().entrySet()) {
            Question question = new Question();
            question.setId(Integer.valueOf(entry.getKey()));
            if (entry.getValue().containsKey(QUESTION_TITLE_FILED)){
                List<String> list = entry.getValue().get(QUESTION_TITLE_FILED);
                question.setTitle(list.get(0));
            }
            if (entry.getValue().containsKey(QUESTION_CONTENT_FILED)){
                List<String> list = entry.getValue().get(QUESTION_CONTENT_FILED);
                question.setContent(list.get(0));
            }
            questions.add(question);
        }
        return questions;
    }

    public boolean indexQuestion(int id,String title,String content) throws Exception{

        SolrInputDocument document = new SolrInputDocument();
        document.setField("id",id);
        document.setField(QUESTION_TITLE_FILED,title);
        document.setField(QUESTION_CONTENT_FILED,content);
        UpdateResponse response = client.add(document, 1000);
        return response != null && response.getStatus() == 0;
    }

}
