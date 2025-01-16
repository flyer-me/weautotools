using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace CloudWork.Model.ViewModels
{
    public class RolesForUserViewModel
    {
        public string RoleId { get; set; } = string.Empty;
        public string RoleName { get; set; }
        public bool IsSelected { get; set; }
    }
}
