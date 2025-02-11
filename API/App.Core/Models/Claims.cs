using System.Security.Claims;

namespace App.Core.Models
{
    public static class Claims
    {
        public static List<Claim> GetAllClaims()
        {
            return new List<Claim>()
            {
                new Claim("Create Role", "Create Role"),
                new Claim("Edit Role", "Edit Role"),
                new Claim("Delete Role", "Delete Role")
            };
        }
    }
}
