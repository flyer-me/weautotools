using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace CloudWork.Model.ViewModels
{
    public class ForgotPasswordViewModel
    {
        [Required(ErrorMessage = "必须的字段")]
        public string Email { get; set; }
    }
}
