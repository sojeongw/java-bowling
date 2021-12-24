package qna.domain.question;

import qna.CannotDeleteException;
import qna.domain.AbstractEntity;
import qna.domain.deleteHistory.ContentType;
import qna.domain.deleteHistory.DeleteHistory;
import qna.domain.question.answer.Answer;
import qna.domain.question.answer.Answers;
import qna.domain.user.User;

import javax.persistence.*;
import java.util.List;

@Entity
public class Question extends AbstractEntity {
    public static final String UNAUTHORIZED_QUESTION_MESSAGE = "질문을 삭제할 권한이 없습니다.";
    public static final String UNAUTHORIZED_ANSWER_MESSAGE = "다른 사람이 쓴 답변이 있어 삭제할 수 없습니다.";

    @Column(length = 100, nullable = false)
    private String title;

    @Lob
    private String contents;

    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_question_writer"))
    private User writer;

    @Embedded
    private Answers answers = new Answers();

    private boolean deleted = false;

    public Question() {
    }

    public Question(String title, String contents) {
        this.title = title;
        this.contents = contents;
    }

    public Question(long id, String title, String contents) {
        super(id);
        this.title = title;
        this.contents = contents;
    }

    public List<DeleteHistory> delete() {
        this.deleted = true;
        DeleteHistory questionDeleteHistory = DeleteHistory.of(ContentType.QUESTION, this);
        List<DeleteHistory> answerDeleteHistory = this.answers.delete();

        return questionDeleteHistory.merge(answerDeleteHistory);
    }

    public User getWriter() {
        return writer;
    }

    public Question writeBy(User loginUser) {
        this.writer = loginUser;
        return this;
    }

    public void addAnswer(Answer answer) {
        answer.toQuestion(this);
        answers.add(answer);
    }

    public boolean isOwner(User loginUser) throws CannotDeleteException {
        if (!writer.equals(loginUser)) {
            throw new CannotDeleteException(UNAUTHORIZED_QUESTION_MESSAGE);
        }

        if (!answers.isOwner(loginUser)) {
            throw new CannotDeleteException(UNAUTHORIZED_ANSWER_MESSAGE);
        }
        return true;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public List<Answer> answers() {
        return answers.values();
    }

    @Override
    public String toString() {
        return "Question [id=" + getId() + ", title=" + title + ", contents=" + contents + ", writer=" + writer + "]";
    }
}
