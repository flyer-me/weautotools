using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace CloudWork.Model.ViewModels
{
    public class EditRoleViewModel
    {
        [Required]
        public string Id { get; set; } = string.Empty;
        [Required]
        public string? RoleName { get; set; } = string.Empty;

        public List<string>? UserNames { get; set; }
    }
}
