namespace CloudWork.Models
{
    public class TestCase
    {
        public int Id { get; set; }
        public string Input { get; set; } = String.Empty;
        public string ExpectedOutput { get; set; } = String.Empty;
        public int QuestionId { get; set; }

        public virtual Question? Question { get; set; }
    }
}