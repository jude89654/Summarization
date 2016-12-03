package com.model;

import java.util.ArrayList;
import java.util.List;

public class Document
{
	List<Sentence> sentences;
	long documentId;
	String filename;
	
	public Document(List<Sentence> sentences, String filename, long documentId)
	{
		this.sentences = sentences;
		this.filename = filename;
		this.documentId = documentId;
	}

	
	public Document(){
		this.sentences = new ArrayList<>();
	}
	
	public void addSentence(Sentence sentence){
		this.sentences.add(sentence);
	}
	
	public List<Sentence> getSentences()
	{
		return this.sentences;
	}

	public long getDocumentId()
	{
		return documentId;
	}

	public int getNumberOfSentences()
	{
		return sentences.size();
	}

	public String toString(){

		StringBuilder sb = new StringBuilder();
		for (Sentence sentence:
			 sentences) {
			sb.append(sentence.getRefSentence())
					.append(" ");

		}
		return sb.toString();
	}
} 
