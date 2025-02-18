namespace CloudWork.Model
{
    public class TestCase
    {
        public string Id { get; set; } = string.Empty;
        public string Input { get; set; } = string.Empty;
        public string ExpectedOutput { get; set; } = string.Empty;
        public string QuestionId { get; set; } = string.Empty;
        public virtual Question? Question { get; set; }
    }
}