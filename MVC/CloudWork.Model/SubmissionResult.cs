namespace CloudWork.Model
{
    public class SubmissionResult
    {
        public int Id { get; set; }
        public bool IsPassed { get; set; }
        public string Message { get; set; } = string.Empty;
        public DateTime EvaluatedAt { get; set; } = DateTime.UtcNow;

        public int SubmissionId { get; set; }
        public virtual Submission Submission { get; set; } = null!;
    }
}