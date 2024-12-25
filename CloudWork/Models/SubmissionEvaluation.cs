namespace CloudWork.Models
{
    public class SubmissionEvaluation
    {
        public int Id { get; set; }
        public bool IsPassed { get; set; }
        public string Message { get; set; } = String.Empty;
        public DateTime EvaluatedAt { get; set; }

        public int SubmissionId { get; set; }
        public virtual Submission Submission { get; set; } = null!;
    }
}