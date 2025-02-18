using System.ComponentModel.DataAnnotations;

namespace CloudWork.Model.ViewModels
{
    public class SetPasswordViewModel
    {
        [Required]
        [DataType(DataType.Password)]
        public string Password { get; set; } = string.Empty;

        [Required]
        [DataType(DataType.Password)]
        [Compare("Password")]
        public string ConfirmPassword { get; set; } = string.Empty;
    }
}
