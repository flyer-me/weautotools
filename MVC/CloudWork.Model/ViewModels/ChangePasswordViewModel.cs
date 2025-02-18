using System.ComponentModel.DataAnnotations;

namespace CloudWork.Model.ViewModels
{
    public class ChangePasswordViewModel
    {
        [Required]
        [DataType(DataType.Password)]
        public string CurrentPassword { get; set; } = string.Empty;

        [Required]
        [DataType(DataType.Password)]
        public string NewPassword { get; set; } = string.Empty;

        [Required]
        [DataType(DataType.Password)]
        [Compare("NewPassword", ErrorMessage = "两次密码输入不相同")]
        public string ConfirmPassword { get; set; } = string.Empty;
    }
}
