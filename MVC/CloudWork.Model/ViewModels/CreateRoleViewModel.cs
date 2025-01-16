using System.ComponentModel.DataAnnotations;

namespace CloudWork.Model.ViewModels
{
    public class CreateRoleViewModel
    {
        [Required]
        [Display(Name = "Role")]
        public string RoleName { get; set; } = string.Empty;
    }
}
