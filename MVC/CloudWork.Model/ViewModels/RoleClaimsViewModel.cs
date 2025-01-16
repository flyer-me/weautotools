namespace CloudWork.Model.ViewModels
{
    public class RoleClaimsViewModel
    {
        public RoleClaimsViewModel()
        {
            Claims = new List<RoleClaim>();
        }
        public string RoleId { get; set; } = string.Empty;
        public List<RoleClaim> Claims { get; set; }
    }
}
