using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;

namespace CloudWork.Models
{
    public class User
    {
        public int Id { get; set; }

        [StringLength(50)]
        public string Username { get; set; }

        public string PasswordHash { get; set; }

        public string Email { get; set; }

        // 导航属性
        public virtual ICollection<Submission> Submissions { get; set; } = new List<Submission>();

        public User()
        {
            // 默认构造函数
        }
    }
}