using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using CloudWork.Model;
using CloudWork.Common.DB;
using CloudWork.Repository.Base;
using CloudWork.Service;
using CloudWork.Service.Interface;

namespace CloudWork.Web.Controllers
{
    public class QuestionsController : Controller
    {
        private readonly IBaseService<Question> _baseService;
        public QuestionsController(IBaseService<Question> baseService)
        {
            _baseService = baseService;
        }

        // GET: Questions
        public async Task<IActionResult> Index()
        {
            var questions = await _baseService.GetAllAsync();
            return View(questions);
        }

        // GET: Questions/Details/5
        public async Task<IActionResult> Details(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var question = await _baseService.GetByIdAsync(id.Value);
            if (question == null)
            {
                return NotFound();
            }

            return View(question);
        }

        // GET: Questions/Create
        public IActionResult Create()
        {
            return View();
        }

        // POST: Questions/Create
        // To protect from overposting attacks, enable the specific properties you want to bind to.
        // For more details, see http://go.microsoft.com/fwlink/?LinkId=317598.
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Create([Bind("Id,Title,Description,IsPublic")] Question question)
        {
            if (ModelState.IsValid)
            {
                await _baseService.AddAsync(question);
                return RedirectToAction(nameof(Index));
            }
            return View(question);
        }

        // GET: Questions/Edit/5
        public async Task<IActionResult> Edit(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var question = await _baseService.GetByIdAsync(id.Value);
            if (question == null)
            {
                return NotFound();
            }
            return View(question);
        }

        // POST: Questions/Edit/5
        // To protect from overposting attacks, enable the specific properties you want to bind to.
        // For more details, see http://go.microsoft.com/fwlink/?LinkId=317598.
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Edit(int id, [Bind("Id,Title,Description,IsPublic")] Question question)
        {
            if (id != question.Id)
            {
                return NotFound();
            }

            if (ModelState.IsValid)
            {
                try
                {
                    await _baseService.UpdateAsync(question);
                }
                catch (DbUpdateConcurrencyException)
                {
                    var que = await _baseService.GetByIdAsync(question.Id);
                    if (que == null)
                    {
                        return NotFound();
                    }
                    else
                    {
                        throw;
                    }
                }
                return RedirectToAction(nameof(Index));
            }
            return View(question);
        }

        // GET: Questions/Delete/5
        public async Task<IActionResult> Delete(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var question = await _baseService.GetByIdAsync(id.Value);
            if (question == null)
            {
                return NotFound();
            }

            return View(question);
        }

        // POST: Questions/Delete/5
        [HttpPost, ActionName("Delete")]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> DeleteConfirmed(int id)
        {
            var question = await _baseService.GetByIdAsync(id);
            if (question != null)
            {
                await _baseService.DeleteAsync(id);
            }

            return RedirectToAction(nameof(Index));
        }

    }
}
