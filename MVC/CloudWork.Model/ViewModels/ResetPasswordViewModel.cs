using System.ComponentModel.DataAnnotations;

namespace CloudWork.Model.ViewModels
{
    public class ResetPasswordViewModel
    {
        [Required]
        public string Email { get; set; } = string.Empty;
        [Required]
        [DataType(DataType.Password)]
        public string PasswordHash { get; set; } = string.Empty;
        [Required]
        [DataType(DataType.Password)]
        [Display(Name = "Confirm Password")]
        [Compare("PasswordHash", ErrorMessage = "密码与上一次输入不一致")]
        public string ConfirmPasswordHash { get; set; } = string.Empty;
        [Required]
        public string Token { get; set; } = string.Empty;
        public bool IsSuccess { get; set; } = false;
    }
}
