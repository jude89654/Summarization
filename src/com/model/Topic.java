package com.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constants.Constants;
import com.constants.IdProvider;
import com.processor.SentenceProcessor;

public class Topic {
    List<Summary> summaries;
    List<Document> documents;
    long topicId;
    Map<Long, Sentence> sentenceMap = new HashMap<Long, Sentence>();

    public Map<Long, Sentence> getSentenceMap() {
        return sentenceMap;
    }

    public Topic(String filename, long topicId) {
        documents = SentenceProcessor.getDocuments(filename, sentenceMap);

        //di ko naman alam kung para saan to, kasi false lang ata lagi value nito
        if (Constants.isDUC2002) {
            splitDocuments();
        }
        //initialize na yung summary
        summaries = new ArrayList<>();

        this.topicId = topicId;
    }

    /**
     * Splits the textfile into documents.
     */
    private void splitDocuments() {
        //kukuhain ang pinakaunang document
        Document document = documents.remove(0);

        //kukunin yung mga sentences sa document na yun
        List<Sentence> sentenceList = document.getSentences();

        int size = sentenceList.size() / 10;

        //constant siya bale, dumadagdag siya kada call, kaya ibang topic id kada documents
        long docId = IdProvider.getNextDocumentId();

        //eto yung magiging sentences sa loob ng isang document.
        List<Sentence> list = new ArrayList<Sentence>();


        int added = 0;
        int sentenceIndex = -1;

        boolean getMoreSentences = false;
        long lineNumber = 0;


        //yung documents per topic ay 10, as of now
        for (int i = 0; i < Constants.DOCUMENTS_PER_TOPIC; i++) {

            getMoreSentences = true;

            while (getMoreSentences) {
                //bale nilalagyan ng sentence number at line numper docu
                sentenceList.get(++sentenceIndex).documentId = docId;
                lineNumber++;
                sentenceList.get(sentenceIndex).setPosition(lineNumber);
                list.add(sentenceList.get(sentenceIndex));
                added++;

                if ((added == size && i != Constants.DOCUMENTS_PER_TOPIC - 1)
                            //ang alam ko ito pag yung sentence size ay equal na sa added
                        || (i == Constants.DOCUMENTS_PER_TOPIC - 1 && sentenceIndex == sentenceList.size() - 1)) {
                    Document newDocument = new Document(list, document.filename, docId);
                    lineNumber = 0;
                    documents.add(newDocument);
                    docId = IdProvider.getNextDocumentId();
                    added = 0;
                    list = new ArrayList<Sentence>();
                    getMoreSentences = false;
                }
            }
        }
    }

    //For J-unit purposes
    public Topic() {
        this.summaries = new ArrayList<Summary>();
        this.documents = new ArrayList<Document>();
        this.topicId = 123456;
    }

    public void addDocument(Document d) {
        this.documents.add(d);
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public long getTopicId() {
        return topicId;
    }

    public void addSummary(Summary s) {
        summaries.add(s);
    }

    public List<Summary> getSummaries() {
        return summaries;
    }
}
