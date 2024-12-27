using System.ComponentModel.DataAnnotations;

namespace CloudWork.Model
{
    public class User
    {
        public int Id { get; set; }
        public string Username { get; set; } = string.Empty;
        public string PasswordHash { get; set; } = string.Empty;

        [EmailAddress]
        public string Email { get; set; } = string.Empty;
        public virtual ICollection<Submission> Submissions { get; set; } = new List<Submission>();

        public User()
        {

        }
    }
}