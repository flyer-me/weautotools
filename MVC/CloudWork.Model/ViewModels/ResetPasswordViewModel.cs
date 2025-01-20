using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

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
