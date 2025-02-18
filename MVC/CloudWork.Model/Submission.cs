namespace CloudWork.Model
{
    public class Submission
    {
        public string Id { get; set; } = string.Empty;
        public string Code { get; set; } = string.Empty;
        public ProgramLanguage Language { get; set; }
        public DateTime SubmittedAt { get; set; } = DateTime.UtcNow;

        public string UserId { get; set; } = string.Empty;
        public string QuestionId { get; set; } = string.Empty;
        public virtual User User { get; set; } = null!;
        public virtual Question Question { get; set; } = null!;
        public virtual SubmissionEvaluation? Evaluation { get; set; }
    }

    public enum ProgramLanguage
    {
        CSharp,
        Python
    }
}