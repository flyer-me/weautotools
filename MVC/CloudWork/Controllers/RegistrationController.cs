using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using CloudWork.Service;
using CloudWork.Model;
using CloudWork.Model.ViewModels;
using CloudWork.Repository.UnitOfWork;

namespace CloudWork.Controllers
{
    public class RegistrationController : Controller
    {
        private readonly IRegistrationService _registration;
        private readonly IUnitOfWork _unitOfWork;
        public RegistrationController(IRegistrationService registration, IUnitOfWork unitOfWork)
        {
            _registration = registration;
            _unitOfWork = unitOfWork;
        }

        // GET: RegistrationController
        [HttpGet]
        public ActionResult Index()
        {
            var model = new RegistrationViewModel();
            return View("Registration", model);
        }

        // POST: RegistrationController
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<ActionResult> Registration(RegistrationViewModel model)
        {
            try
            {
                if (!ModelState.IsValid)
                {
                    return View(model);
                }
                if (!model.AgreePolicy)
                {
                    ModelState.AddModelError("AgreePolicy", "需要同意用户条款和查看隐私政策");
                    return View(model);
                }
                var user = new User
                {
                    UserName = model.UserName,
                    PasswordHash = model.PasswordHash,
                    PhoneNumber = model.PhoneNumber,
                    Email = model.Email
                };
                await _unitOfWork.Users.AddAsync(user);
                await _unitOfWork.SaveAsync();
                return View("Dashboard", user);
            }
            catch
            {
                return View("error");
            }
        }

        [AcceptVerbs("GET", "POST")]
        public async Task<IActionResult> IsUserNameAvailable(string userName)
        {
            if (string.IsNullOrWhiteSpace(userName))
            {
                return Json("用户名不能为空");
            }
            if (await _unitOfWork.Users.AnyAsync(u => u.UserName == userName))
            {
                var suggestions = await _registration.GenerateUniqueUserNamesAsync(userName);
                var strSuggestions = string.Join(", ", suggestions);
                return Json($"已被占用，可使用： {strSuggestions}");
            }
            return Json(true);
        }

        [HttpGet]
        [ValidateAntiForgeryToken]
        public ActionResult Dashboard(User user)
        {
            return View("Dashboard", user);
        }
    }
}
