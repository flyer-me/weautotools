using System.ComponentModel.DataAnnotations;

namespace CloudWork.Model
{
    public class User
    {
        public int Id { get; set; }
        public string UserName { get; set; } = string.Empty;
        public string PasswordHash { get; set; } = string.Empty;
        public string PhoneNumber { get; set; } = string.Empty;
        public string Email { get; set; } = string.Empty;
        public bool IsDeleted { get; set; } = false;

        public virtual ICollection<Submission> Submissions { get; set; } = new List<Submission>();
    }
}