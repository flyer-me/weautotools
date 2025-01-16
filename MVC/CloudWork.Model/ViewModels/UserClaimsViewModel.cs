namespace CloudWork.Model.ViewModels
{
    public class UserClaimsViewModel
    {
        public UserClaimsViewModel() => Cliams = new List<UserClaim>();
        public string UserId { get; set; } = string.Empty;
        public List<UserClaim> Cliams { get; set; }
    }
}
