namespace CloudWork.Models
{
    public class TestCase
    {
        public int Id { get; set; }
        public int ProblemId { get; set; }
        public string Input { get; set; } = String.Empty;
        public string ExpectedOutput { get; set; } = String.Empty;

        public virtual Problem Problem { get; set; }
    }
}