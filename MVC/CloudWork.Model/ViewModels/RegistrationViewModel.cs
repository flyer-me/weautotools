using Microsoft.AspNetCore.Mvc;
using System.ComponentModel.DataAnnotations;

namespace CloudWork.Model.ViewModels
{
    public class RegistrationViewModel
    {
        [Required(ErrorMessage = "名称必填")]
        [StringLength(20, MinimumLength = 3, ErrorMessage = "用户名长度必须在3到20个字符之间")]
        [RegularExpression(@"^[\u4e00-\u9fa5a-zA-Z0-9_-]*$",
            ErrorMessage = "用户名只能包含中英文字符、数字、下划线(_)或连字符(-)")]
        [Remote(action: "IsUserNameAvailable", controller: "RemoteValidation", ErrorMessage = "用户名已被占用")]
        public string UserName { get; set; } = string.Empty;
        [Required]
        [DataType(DataType.Password)]
        public string PasswordHash { get; set; } = string.Empty;

        public string PhoneNumber { get; set; } = string.Empty;

        [Required(ErrorMessage = "Gmail address is required.")]
        //[EmailAddress]
        public string Email { get; set; } = string.Empty;

        [Display(Name = "我已经阅读和同意用户条款和查看隐私政策")]
        [Required(ErrorMessage = "需要同意用户条款和查看隐私政策")]
        public bool AgreePolicy { get; set; }
    }
}
