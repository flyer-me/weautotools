using System.ComponentModel.DataAnnotations;

namespace CloudWork.Model.ViewModels
{
    public class ForgotPasswordViewModel
    {
        [Required(ErrorMessage = "必须的字段")]
        public string Email { get; set; } = string.Empty;
    }
}
