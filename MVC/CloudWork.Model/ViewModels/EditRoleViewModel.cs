using System.ComponentModel.DataAnnotations;

namespace CloudWork.Model.ViewModels
{
    public class EditRoleViewModel
    {
        [Required]
        public string Id { get; set; } = string.Empty;
        [Required]
        public string? RoleName { get; set; } = string.Empty;
        public List<string> UserNames { get; set; } = [];
        public List<string> Claims { get; set; } = [];
    }
}
