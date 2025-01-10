using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Filters;
using System.Security.Claims;

namespace CloudWork.Filter
{
    public class RedirectToLoginOnUnauthorizedFilter : IAuthorizationFilter
    {
        public void OnAuthorization(AuthorizationFilterContext context)
        {
            if (!IsAuthorized(context.HttpContext.User))
            {
                // If not authenticated, redirect to the login page
                context.Result = new RedirectToRouteResult(new RouteValueDictionary
                {
                    { "controller", "Home" },
                    { "action", "Login" }
                });
            }
        }

        private bool IsAuthorized(ClaimsPrincipal user)
        {
            // TODO: Custom Authorization Logic
            return false;
        }
    }
}