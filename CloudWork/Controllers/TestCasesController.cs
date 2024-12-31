using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Rendering;
using Microsoft.EntityFrameworkCore;
using CloudWork.Model;
using CloudWork.Service.Interface;
using CloudWork.Repository.Base;

namespace CloudWork.Controllers
{
    public class TestCasesController : Controller
    {
        private readonly ITestCaseService _service;

        public TestCasesController(ITestCaseService testCaseService)
        {
            _service = testCaseService;
        }

        // GET: TestCases 默认需要导航属性
        public async Task<IActionResult> Index()
        {
            return View(await _service.GetTestCasesWithQuestionAsync());
        }

        // GET: TestCases/Details/5
        public async Task<IActionResult> Details(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var testCase = await _service.GetTestCaseWithQuestionAsync(id.Value);

            if (testCase == null)
            {
                return NotFound();
            }

            return View(testCase);
        }

        // GET: TestCases/Create
        public async Task<IActionResult> Create()
        {
            IEnumerable<Question> questions = await _service.GetQuestionsAsync();
            ViewData["QuestionId"] = new SelectList(questions, "Id", nameof(Question.Title));
            return View();
        }

        // POST: TestCases/Create
        // To protect from overposting attacks, enable the specific properties you want to bind to.
        // For more details, see http://go.microsoft.com/fwlink/?LinkId=317598.
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Create([Bind("Id,QuestionId,Input,ExpectedOutput")] TestCase testCase)
        {
            if (ModelState.IsValid)
            {
                try
                {
                    await _service.AddAsync(testCase);
                    await _service.SaveAsync();
                    return RedirectToAction(nameof(Index));
                }
                catch (Exception)
                {
                    // _unitOfWork.RollbackTransaction();
                    throw;
                }
            }
            IEnumerable<Question> questions = await _service.GetQuestionsAsync();
            ViewData["QuestionId"] = new SelectList(questions, "Id", nameof(Question.Title), testCase.QuestionId);
            return View(testCase);
        }

        // GET: TestCases/Edit/5
        public async Task<IActionResult> Edit(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var testCase = await _service.GetTestCaseWithQuestionAsync(id.Value);
            if (testCase == null)
            {
                return NotFound();
            }
            IEnumerable<Question> questions = await _service.GetQuestionsAsync();
            ViewData["QuestionId"] = new SelectList(questions, "Id", nameof(Question.Title), testCase.QuestionId);
            return View(testCase);
        }

        // POST: TestCases/Edit/5
        // To protect from overposting attacks, enable the specific properties you want to bind to.
        // For more details, see http://go.microsoft.com/fwlink/?LinkId=317598.
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Edit(int id, [Bind("Id,QuestionId,Input,ExpectedOutput")] TestCase testCase)
        {
            if (id != testCase.Id)
            {
                return NotFound();
            }

            if (ModelState.IsValid)
            {
                try
                {
                    _service.Update(testCase);
                    await _service.SaveAsync();
                    return RedirectToAction(nameof(Index));
                }
                catch (DbUpdateConcurrencyException)
                {
                    var test = await _service.GetByIdAsync(id);
                    if (test == null)
                    {
                        return NotFound();
                    }
                    else
                    {
                        throw;
                    }
                }
            }
            IEnumerable<Question> questions = await _service.GetQuestionsAsync();
            ViewData["QuestionId"] = new SelectList(questions, "Id", nameof(Question.Title), testCase.QuestionId);
            return View(testCase);
        }

        // GET: TestCases/Delete/5
        public async Task<IActionResult> Delete(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var testCase = await _service.GetTestCaseWithQuestionAsync(id.Value);
            if (testCase == null)
            {
                return NotFound();
            }

            return View(testCase);
        }

        // POST: TestCases/Delete/5
        [HttpPost, ActionName("Delete")]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> DeleteConfirmed(int id)
        {
            var testCase = await _service.GetTestCaseWithQuestionAsync(id);
            if (testCase != null)
            {
                try
                {
                    await _service.DeleteAsync(id);
                    await _service.SaveAsync();
                }
                catch (Exception)
                {
                    throw;
                }
            }
            return RedirectToAction(nameof(Index));

        }
    }
}
