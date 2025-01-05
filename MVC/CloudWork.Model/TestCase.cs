namespace CloudWork.Model
{
    public class TestCase
    {
        public int Id { get; set; }
        public string Input { get; set; } = string.Empty;
        public string ExpectedOutput { get; set; } = string.Empty;
        public int QuestionId { get; set; }
        public virtual Question? Question { get; set; }
    }
}