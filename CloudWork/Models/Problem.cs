namespace CloudWork.Models
{
    public class Problem
    {
        public int Id { get; set; }
        public string Title { get; set; } = String.Empty;
        public string Description { get; set; } = String.Empty;
        public bool IsPublic { get; set; }

        public virtual ICollection<TestCase>? TestCases { get; set; }
        public virtual ICollection<Submission>? Submissions { get; set; }
    }
}