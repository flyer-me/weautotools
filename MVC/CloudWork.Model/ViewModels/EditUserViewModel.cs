using System.ComponentModel.DataAnnotations;

namespace CloudWork.Model.ViewModels
{
    public class EditUserViewModel
    {
        public EditUserViewModel()
        {
            Claims = [];
            Roles = [];
        }
        [Required]
        public string Id { get; set; } = string.Empty;
        [Required]
        public string? UserName { get; set; }
        [Required]
        public string? Email { get; set; }
        public List<string> Claims { get; set; }
        public IList<string> Roles { get; set; }
    }
}
