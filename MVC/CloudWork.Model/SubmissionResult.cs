namespace CloudWork.Model
{
    public class SubmissionResult
    {
        public bool IsPassed { get; set; }
        public bool CompileError { get; set; }
        public string Message { get; set; } = string.Empty;
        public TimeSpan ExecutionTime { get; set; }
    }
}