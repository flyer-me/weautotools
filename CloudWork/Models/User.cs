using Microsoft.EntityFrameworkCore;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;

namespace CloudWork.Models
{
    public class User
    {
        public int Id { get; set; }
        public string Username { get; set; } = String.Empty;
        public string PasswordHash { get; set; } = String.Empty;

        [EmailAddress]
        public string Email { get; set; } = String.Empty;
        public virtual ICollection<Submission> Submissions { get; set; } = new List<Submission>();

        public User()
        {

        }
    }
}