using System;
using System.ComponentModel.DataAnnotations;

namespace CloudWork.Models
{
    public class Submission
    {
        [Key]
        public int Id { get; set; }
        public string Code { get; set; } = String.Empty;
        public ProgramLanguage Language { get; set; }
        public DateTime SubmittedAt { get; set; } = DateTime.UtcNow;

        public int UserId { get; set; }
        public int QuestionId { get; set; }
        public virtual User User { get; set; } = null!;
        public virtual Question Question { get; set; } = null!;
        public virtual SubmissionEvaluation? Evaluation { get; set; }
    }

    public enum ProgramLanguage
    {
        CSharp,
        // 其他支持的语言
    }
}