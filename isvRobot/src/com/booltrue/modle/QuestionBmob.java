package com.booltrue.modle;

import cn.bmob.v3.BmobObject;

public class QuestionBmob extends BmobObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6549014220282533912L;
	
	public static final String QUESTION_TITLE =  "questionTitle";
	public static final String QUESTION_ANSWER = "questionAnswer";
	
	private String questionTitle;
	private String questionAnswer;
	
	public String getQuestionTitle() {
		return questionTitle;
	}
	public void setQuestionTitle(String questionTitle) {
		this.questionTitle = questionTitle;
	}
	public String getQuestionAnswer() {
		return questionAnswer;
	}
	public void setQuestionAnswer(String questionAnswer) {
		this.questionAnswer = questionAnswer;
	}
	
	

}
