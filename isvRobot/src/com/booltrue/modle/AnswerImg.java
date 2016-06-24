package com.booltrue.modle;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobFile;

public class AnswerImg extends BmobObject {
	
	public static String ANSWER_IMG = "answerImg";
	public static String ANSWER_TEXT = "answerText";
	public static String QUESTION_ID = "questionId";
	public static String SEQUENCE = "sequence";
 
	/**
	 * 
	 */
	private static final long serialVersionUID = 9059707363299940574L;
	
	private BmobFile answerImg;
	private String answerText;
	private String questionId;
	private Integer sequence;
	public BmobFile getAnswerImg() {
		return answerImg;
	}
	public void setAnswerImg(BmobFile answerImg) {
		this.answerImg = answerImg;
	}
	public String getAnswerText() {
		return answerText;
	}
	public void setAnswerText(String answerText) {
		this.answerText = answerText;
	}
	public String getQuestionId() {
		return questionId;
	}
	public void setQuestionId(String questionId) {
		this.questionId = questionId;
	}
	public Integer getSequence() {
		return sequence;
	}
	public void setSequence(Integer sequence) {
		this.sequence = sequence;
	}
	
	
	
	
}
