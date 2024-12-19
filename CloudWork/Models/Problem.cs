namespace CloudWork.Models
{
    public class Problem
    {
        public int Id { get; set; }
        public string Title { get; set; }
        public string Description { get; set; }
        // 可以添加字段如DifficultyLevel, Category等

        public virtual ICollection<TestCase> TestCases { get; set; }
        public virtual ICollection<Submission> Submissions { get; set; }
    }
}