namespace CloudWork.Model
{
    public class SubmissionEvaluation
    {
        public string Id { get; set; } = string.Empty;
        public bool IsPassed { get; set; }
        public string Message { get; set; } = string.Empty;
        public TimeSpan ExecutionTime { get; set; }
        public DateTime EvaluatedAt { get; set; } = DateTime.UtcNow;

        public string SubmissionId { get; set; } = string.Empty;
        public virtual Submission Submission { get; set; } = null!;
    }
}