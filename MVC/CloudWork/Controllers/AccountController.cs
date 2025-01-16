using CloudWork.Model;
using CloudWork.Model.ViewModels;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using System.Security.Claims;

namespace CloudWork.Controllers
{
    public class AccountController : Controller
    {
        private readonly UserManager<User> _userManager;
        private readonly RoleManager<IdentityRole> _roleManager;
        private readonly SignInManager<User> _signInManager;

        public AccountController(UserManager<User> userManager, SignInManager<User> signInManager, RoleManager<IdentityRole> roleManager)
        {
            _userManager = userManager;
            _signInManager = signInManager;
            _roleManager = roleManager;
        }

        public IActionResult Index()
        {
            return View("NotFound");
        }

        [HttpGet]
        public IActionResult Register()
        {
            return View();
        }

        [HttpPost]
        public async Task<IActionResult> Register(RegisterViewModel model)
        {
            if (ModelState.IsValid)
            {
                if (!model.AgreePolicy)
                {
                    ModelState.AddModelError("AgreePolicy", "需要同意账户条款和查看隐私政策");
                    return View(model);
                }
                var user = new User
                {
                    UserName = model.UserName,
                    Email = model.Email,
                    PhoneNumber = model.PhoneNumber
                };
                var result = await _userManager.CreateAsync(user, model.PasswordHash);
                if (result.Succeeded)
                {
                    // 管理员注册后重定向到账户列表
                    if (_signInManager.IsSignedIn(User) && User.IsInRole("Admin"))
                    {
                        return Redirect(nameof(Users));
                    }
                    // 新账户添加到 "User" 角色
                    else if (await _roleManager.RoleExistsAsync("User"))
                    {
                        await _userManager.AddToRoleAsync(user, "User");
                    }

                    await _signInManager.SignInAsync(user, isPersistent: false);
                    return View("Dashboard", user);
                }
                foreach (var error in result.Errors)
                {
                    ModelState.AddModelError(string.Empty, error.Description);
                }
            }
            return View(model);
        }

        [HttpGet]
        public IActionResult Login(string? returnUrl = null)
        {
            var model = new LoginViewModel
            {
                ReturnUrl = returnUrl
            };
            return View(model);
        }

        [HttpPost]
        public async Task<IActionResult> Login(LoginViewModel model)
        {
            if (ModelState.IsValid)
            {
                var result = await _signInManager.PasswordSignInAsync(model.Email, model.Password, model.RememberMe, lockoutOnFailure: false);

                if (result.Succeeded)
                {
                    if (!string.IsNullOrEmpty(model.ReturnUrl) && Url.IsLocalUrl(model.ReturnUrl))
                    {
                        return Redirect(model.ReturnUrl);
                    }
                    else
                    {
                        return RedirectToAction("Index", "Home");
                    }
                }
                if (result.RequiresTwoFactor)
                {
                    // two-factor authentication case
                }
                if (result.IsLockedOut)
                {
                    // Handle lockout scenario
                }
                else
                {
                    ModelState.AddModelError(string.Empty, "失败");
                    return View(model);
                }
            }

            return View(model);
        }

        [HttpPost]
        public async Task<IActionResult> Logout()
        {
            await _signInManager.SignOutAsync();
            return RedirectToAction("index", "home");
        }

        [HttpGet]
        public IActionResult AccessDenied()
        {
            return View("AccessDenied");
        }

        [HttpGet]
        [Authorize(Roles = "Admin")]
        public IActionResult Users()
        {
            var users = _userManager.Users;
            return View(users);
        }

        [HttpGet]
        [Authorize(Roles = "Admin")]
        public async Task<IActionResult> EditUser(string userId)
        {
            var user = await _userManager.FindByIdAsync(userId);

            if (user == null)
            {
                ViewBag.ErrorMessage = $"找不到账户：{userId}";
                return View("NotFound");
            }

            var userClaims = await _userManager.GetClaimsAsync(user);
            var userRoles = await _userManager.GetRolesAsync(user);

            var model = new EditUserViewModel
            {
                Id = user.Id,
                Email = user.Email,
                UserName = user.UserName,
                Claims = userClaims.Select(c => c.Value).ToList(),
                Roles = userRoles
            };

            return View(model);
        }

        [HttpPost]
        [Authorize(Roles = "Admin")]
        public async Task<IActionResult> EditUser(EditUserViewModel model)
        {
            var user = await _userManager.FindByIdAsync(model.Id);

            if (user == null)
            {
                ViewBag.ErrorMessage = $"找不到账户：{model.Id}";
                return View("NotFound");
            }
            else
            {
                user.Email = model.Email;
                user.UserName = model.UserName;

                var result = await _userManager.UpdateAsync(user);

                if (result.Succeeded)
                {
                    return RedirectToAction(nameof(Users));
                }

                    foreach (var error in result.Errors)
                    {
                        ModelState.AddModelError("", error.Description);
                    }

                return View(model);
            }
        }

        [HttpPost]
        public async Task<IActionResult> DeleteUser(string userId)
        {
            var user = await _userManager.FindByIdAsync(userId);

            if (user == null)
            {
                ViewBag.ErrorMessage = $"找不到账户：{userId}";
                return View("NotFound");
            }
            else
            {
                var result = await _userManager.DeleteAsync(user);

                if (result.Succeeded)
                {
                    return RedirectToAction(nameof(Users));
                }
                else
                {
                    foreach (var error in result.Errors)
                    {
                        ModelState.AddModelError("", error.Description);
                    }
                }

                return View("Users");
            }
        }

        [HttpGet]
        public async Task<IActionResult> EditUserRoles(string userId)
        {
            var user = await _userManager.FindByIdAsync(userId);
            if (user == null) {
                ViewBag.ErrorMessage = $"找不到账户：{userId}";
                return View("NotFound");
            }

            ViewBag.userId = userId;    // User -- Role 一对多关系避免重复
            ViewBag.userName = user.UserName;

            var model = new List<RolesForUserViewModel>();
            foreach (var role in _roleManager.Roles)
            {
                var rolesForUserViewModel = new RolesForUserViewModel
                {
                    RoleId = role.Id,
                    RoleName = role.Name ?? string.Empty
                };

                if (await _userManager.IsInRoleAsync(user, role.Name ?? string.Empty))
                {
                    rolesForUserViewModel.IsSelected = true;
                }

                model.Add(rolesForUserViewModel);
            }

            return View(model);
        }

        [HttpPost]
        public async Task<IActionResult> EditUserRoles(List<RolesForUserViewModel> model, string userId)
        {
            var user = await _userManager.FindByIdAsync(userId);

            if (user == null)
            {
                ViewBag.ErrorMessage = $"找不到账户：{userId}";
                return View("NotFound");
            }
            var roles = await _userManager.GetRolesAsync(user);

            var result = await _userManager.RemoveFromRolesAsync(user, roles);

            if (!result.Succeeded)
            {
                ModelState.AddModelError("", "删除账户角色失败");
                return View(model);
            }

            List<string> RolesToBeAssigned = model.Where(x => x.IsSelected).Select(y => y.RoleName).ToList();

            if (RolesToBeAssigned.Count != 0)
            {
                result = await _userManager.AddToRolesAsync(user, RolesToBeAssigned);
                if (!result.Succeeded)
                {
                    ModelState.AddModelError("", "添加选定的角色到账户失败");
                    return View(model);
                }
            }

            return RedirectToAction("EditUser", new { userId });
        }

        [HttpGet]
        public async Task<IActionResult> EditUserClaims(string userId)
        {
            var user = await _userManager.FindByIdAsync(userId);

            if (user == null)
            {
                ViewBag.ErrorMessage = $"找不到账户：{userId}";
                return View("NotFound");
            }

            ViewBag.UserName = user.UserName;

            var model = new UserClaimsViewModel
            {
                UserId = userId
            };

            var existingUserClaims = await _userManager.GetClaimsAsync(user);

            foreach (Claim claim in Claims.GetAllClaims())
            {
                UserClaim userClaim = new UserClaim
                {
                    ClaimType = claim.Type
                };

                if (existingUserClaims.Any(c => c.Type == claim.Type))
                {
                    userClaim.IsSelected = true;
                }

                model.Cliams.Add(userClaim);
            }

            return View(model);
        }

        [HttpPost]
        public async Task<IActionResult> EditUserClaims(UserClaimsViewModel model)
        {
            var user = await _userManager.FindByIdAsync(model.UserId);

            if (user == null)
            {
                ViewBag.ErrorMessage = $"找不到账户：{model.UserId}";
                return View("NotFound");
            }

            var claims = await _userManager.GetClaimsAsync(user);
            var result = await _userManager.RemoveClaimsAsync(user, claims);

            if (!result.Succeeded)
            {
                ModelState.AddModelError("", "账户移除声明出错");
                return View(model);
            }


            var AllSelectedClaims = model.Cliams.Where(c => c.IsSelected)
                        .Select(c => new Claim(c.ClaimType, c.ClaimType))
                        .ToList();

            if (AllSelectedClaims.Count != 0)
            {
                result = await _userManager.AddClaimsAsync(user, AllSelectedClaims);

                if (!result.Succeeded)
                {
                    ModelState.AddModelError("", "账户增加声明出错");
                    return View(model);
                }
            }

            return RedirectToAction("EditUser", new { userId = model.UserId });
        }
    }
}
