import java.util.List;

public class Question {
    String text;
    List<String> answers;
    int right;
    int num = 0;

    public Question(String text, List<String> answers, int right) {
        this.text = text;
        this.answers = answers;
        this.right = right;
    }

    public List<String> getAnswers() {
        return answers;
    }

    public int getRight() {
        return right;
    }

    public String getText() {
        return text;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }
}
