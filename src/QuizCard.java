

public class QuizCard {
	private int cardID=0;
	private String	question;
	private String	answer;

	public QuizCard(String question, String answer, int cardID) {
		this.cardID=cardID;
		this.question = question;
		this.answer = answer;
	}

	public String getQuestion() {
		return this.question;
	}

	public String getAnswer() {
		return this.answer;
	}
	
	public int getCardID() {
		return this.cardID;
	}
}
